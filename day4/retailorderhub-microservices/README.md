# RetailOrderHub — Microservices Decomposition (Day 4/5)

This is the Day 2 monolith (`retailorderhub`, one Spring Boot app) split into
three business-capability services plus the infrastructure to run them as a
real distributed system: a Eureka service registry and a Spring Cloud Gateway
API gateway. It's the hands-on version of Day 4's Decomposition Pattern,
Database-per-Service, and Communication Pattern slides, using the same
RetailOrderHub case study and the same code you already reviewed on Day 2.

## Architecture

```
                        ┌──────────────────┐
                        │    frontend       │  :5500 (static site)
                        │ (plain HTML/JS)   │
                        └─────────┬─────────┘
                                  │ fetch()
                                  ▼
                        ┌──────────────────┐
                        │   api-gateway     │  :8080
                        │ (Spring Cloud     │  <- one address for the
                        │  Gateway)         │     outside world
                        └─────────┬─────────┘
                                  │ routes by path
              ┌───────────────────┼───────────────────┐
              ▼                   ▼                   ▼
     ┌─────────────────┐ ┌──────────────────┐ ┌──────────────────┐
     │  order-service   │ │ inventory-service │ │ payment-service   │
     │  :8081           │ │ :8082             │ │ :8083             │
     │  orderdb (H2)     │ │ inventorydb (H2)   │ │ (no database)     │
     └────────┬─────────┘ └─────────┬─────────┘ └─────────┬─────────┘
              │  Feign, via Eureka  │                      │
              ├─────────────────────┘                      │
              └─────────────────────────────────────────────┘
                                  │
                                  ▼
                        ┌──────────────────┐
                        │  eureka-server    │  :8761
                        │  (service         │  <- everyone registers
                        │   registry)        │     here on startup
                        └──────────────────┘
```

## From monolith to services — what moved where

| Monolith file | Now lives in |
| --- | --- |
| `model/Order.java`, `repository/OrderRepository.java` | `order-service` (unchanged) |
| `service/OrderService.java` | `order-service`, rewritten to call `InventoryClient`/`PaymentClient` (Feign) instead of injected `InventoryService`/`PaymentService` beans |
| `controller/OrderController.java` | `order-service`, now a `@RestController` (`POST /api/orders`, `GET /api/orders`) instead of a Thymeleaf `@Controller` |
| `model/Product.java`, `repository/ProductRepository.java` | `inventory-service` (unchanged) |
| `repository/InventoryRepository.java`, `repository/JpaInventoryRepository.java` | `inventory-service` (unchanged, **including the SQL injection** — see below) |
| `service/InventoryService.java` | `inventory-service` (unchanged) |
| — (new) | `inventory-service`'s `InventoryController` — `GET /api/inventory/products`, `GET /api/inventory/{item}/quantity`, `GET /api/inventory/{item}/in-stock`, `POST /api/inventory/{item}/decrement` |
| `service/PaymentService.java`, `PaymentStrategy.java` and all five strategy classes | `payment-service` (unchanged) |
| — (new) | `payment-service`'s `PaymentController` — `POST /api/payments/charge` |
| `data.sql` | `inventory-service` (unchanged — same five products) |
| `application.properties` | split three ways, one per service, each with its own `server.port` and its own `spring.datasource.url` |

Every carried-forward class kept its original public contract (method names,
parameters, return types) — the point of this exercise is the seam between
services, not a rewrite of the business logic.

## Patterns from Day 4, made concrete

- **Decomposition Pattern** — boundaries follow business capability (Order,
  Inventory, Payment), not a technical split like "web layer" vs. "data
  layer."
- **Database-per-Service** — `order-service` and `inventory-service` each
  have their own in-memory H2 instance (`orderdb`, `inventorydb`); neither
  can see the other's tables. `payment-service` has no database at all,
  because it doesn't need one.
- **Communication Pattern** — all inter-service calls here are synchronous
  REST (`order-service` → `inventory-service`, `order-service` →
  `payment-service`), via declarative Feign clients. There's no async/event
  path in this version — see "What this doesn't do yet" below.
- **Service Discovery** — every service registers with `eureka-server` under
  its `spring.application.name`. Feign clients and the gateway's `lb://`
  routes resolve that name to a live address at call time; nothing in the
  code has another service's host or port hardcoded.
- **API Gateway** — `api-gateway` is the only address a client needs.
  `GET http://localhost:8080/api/orders` and
  `GET http://localhost:8081/api/orders` (hitting order-service directly)
  return the same thing today, but only the gateway address is meant to be
  public.

## What's intentionally still broken

Two Day 1/2 findings were carried forward on purpose and are **not** fixed by
this decomposition:

- **SQL injection** in `inventory-service`'s `JpaInventoryRepository` — both
  native queries are still built by string concatenation. Splitting this
  into its own service doesn't fix it, and arguably makes it more exposed:
  `itemName` now arrives over a REST path variable
  (`/api/inventory/{itemName}/quantity`), reachable by anything that can
  reach this service, not just code inside the same JVM.
- **TOCTOU race** in `order-service`'s `OrderService.processOrder()` — stock
  is still checked in one loop and decremented in a separate later loop,
  with payment in between. This is a *sharper* problem now than it was in
  the monolith: it used to be one `@Transactional` method against one
  database; now it's three separate network calls against two other
  services' own databases, with no rollback if something fails partway
  through or two orders race for the same last unit of stock.

If you're using this as an answer key, don't quietly patch either of these —
they're meant to stay open findings for later discussion (see "Next steps").

## A real bug the decomposition introduced (now fixed)

Unlike the two findings above, this one was never in the monolith — it's a
regression the decomposition itself caused, and worth walking through if it
comes up in a session:

`JpaInventoryRepository.decrementQuantity()` runs a native `UPDATE` via
`entityManager.createNativeQuery(...).executeUpdate()`. JPA requires an
active transaction for any write, or it throws
`jakarta.persistence.TransactionRequiredException`. In the monolith this
worked without anyone writing a line of transaction-handling code for it,
because it ran inside `OrderService.processOrder()`, which *was*
`@Transactional` — that transaction propagated across the in-process method
calls into `InventoryService` and down into the repository. Once
`inventory-service` became its own process, that borrowed transaction was
gone, and nothing about calling this service over HTTP created a new one.
`getQuantity()` (a plain `SELECT`) never needed a transaction, which is why
the stock check earlier in `processOrder()` succeeds and only the decrement
call fails, with a 500 surfacing on the `order-service` side as a
`FeignException$InternalServerError`.

Fixed by adding `@Transactional` to `InventoryService.decrementQuantity()`
— see the comment there. The general lesson: decomposing a monolith doesn't
just relocate code, it can strip away transactional (or security, or
validation) context that calling code was silently relying on. Each service
has to establish its own instead of inheriting the caller's by accident.

## What changed instead of being carried forward

- **The Thymeleaf UI is gone, replaced by `frontend/`.** The monolith's
  `index.html` (catalog + order form) and `orders.html` (order list)
  depended on `ProductRepository` and `OrderRepository` being in the same
  process as the controller rendering the page. Once those repositories
  moved to different services, that stopped being possible without a
  server-side aggregation layer. `frontend/` is a plain HTML/CSS/JS static
  site — no framework, no build step — that calls `api-gateway` the same
  way any other client would, and covers the same three things the old
  pages did: browse the catalog, place an order, see the order list. See
  `frontend/README.md` for how to run it (and why `api-gateway`'s
  `application.yml` now has a CORS block).
- **`OrderService.processOrder()` returns the `Order`, not a `boolean`** —
  needed so `OrderController` can hand back the new order's ID in
  `OrderResponse`. The monolith's version only ever returned success/failure.

## Running it

Requires JDK 17 and Maven. Build everything once from the repo root:

```
cd retailorderhub-microservices
mvn clean install
```

Then start each service in its own terminal, **in this order** — Eureka
first, then the two services with no dependencies on each other, then
order-service (which needs both of them reachable), then the gateway last:

```
cd eureka-server        && mvn spring-boot:run   # 1. registry
cd inventory-service     && mvn spring-boot:run   # 2. )
cd payment-service       && mvn spring-boot:run   # 2. ) any order, both independent
cd order-service         && mvn spring-boot:run   # 3. needs 1 and 2 registered
cd api-gateway            && mvn spring-boot:run   # 4. needs 1 (routes resolve via Eureka)
```

Give each service 15-30 seconds to register with Eureka before starting the
next one that depends on it. Confirm everyone's registered at the Eureka
dashboard: **http://localhost:8761**.

Then, optionally, serve the frontend (see `frontend/README.md`):

```
cd frontend
python3 -m http.server 5500   # 5. static site, talks to the gateway on :8080
```

Open **http://localhost:5500**.

## Try it end-to-end

All of these go through the gateway on :8080. (Swap in :8081/:8082/:8083 to
call a service directly and skip the gateway, if you want to show that
difference live.)

```bash
# Catalog - should list Laptop, Mouse, Keyboard, Monitor, Headphones
curl http://localhost:8080/api/inventory/products

# Place an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": "CUST-001",
        "itemNames": ["Laptop", "Mouse"],
        "paymentMethod": "CREDIT_CARD",
        "amount": 1019.98
      }'

# List orders
curl http://localhost:8080/api/orders

# Confirm stock actually decremented
curl http://localhost:8080/api/inventory/Laptop/quantity

# Try an unknown payment method - should fail cleanly, no order created
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-002","itemNames":["Mouse"],"paymentMethod":"BITCOIN","amount":19.99}'
```

## Next steps (not built here)

- **Saga Pattern** for `processOrder()` — today's version has no
  compensating transaction if payment succeeds but the later decrement
  calls fail, or if inventory changes between the check and the charge.
  This is exactly the scenario Day 4's Saga Pattern slide walks through
  (reserve stock → charge → confirm, with compensating steps on failure) —
  a natural follow-on lab against this exact code.
- **Resilience** on the Feign calls — Day 3 covered the Circuit Breaker
  pattern; none of these Feign clients have one yet, so a slow or down
  inventory-service currently just makes `order-service` wait (bounded by
  the timeouts in `order-service`'s `application.properties`) rather than
  failing fast or falling back.
- **API Gateway cross-cutting concerns** — auth, rate limiting, and request
  logging would normally live in `api-gateway` rather than in each service;
  none are configured here.
