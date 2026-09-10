# RetailOrderHub — Day 3 Model Copy (through Lab 4)

System Design Training Program case study. This is the **facilitator/answer-key
state of the project as of Day 3, Lab 4** — after the SRP→DIP refactor
(Day 2 Lab 1), the OCP Strategy Pattern exercise (Day 2 Lab 2), and the
Circuit Breaker exercise (Day 3 Lab 4). It does **not** include Day 2 Lab 3's
DebitCardStrategy — that's the change participants make themselves during the
GitHub Git Workflow lab, via their own branch and PR — and it assumes the
`Thread.sleep` simulation from Day 3 Lab 3 has already been removed.

## What changed since Day 2 (Day 3, Lab 4 — Circuit Breaker)

- Added `resilience4j-spring-boot3` and `spring-boot-starter-aop` to
  `pom.xml` — Resilience4j needs Spring AOP to intercept `@CircuitBreaker`-
  annotated method calls.
- `PaymentService.charge(String, double)` is now wrapped with
  `@CircuitBreaker(name = "paymentGateway", fallbackMethod = "chargeFallback")`.
  The whole method is annotated, not just the strategy lookup, because
  Spring's proxy-based AOP only intercepts calls made from outside the class.
- `chargeFallback(String, double, Throwable)` returns `false` and logs when
  the breaker is Open or the wrapped call throws.
- Added `service/FailingPaymentStrategy.java` (`@Component("SIMULATE_FAILURE")`)
  — always throws, so the breaker can be tripped on demand. Wired into the
  UI as "Simulate Failure (testing)" in the payment dropdown.
- `application.properties` configures the `paymentGateway` breaker instance:
  sliding window of 4 calls, 50% failure-rate threshold, 10s wait in Open,
  2 trial calls in Half-Open, automatic Open→Half-Open transition. Also
  exposes `health`, `circuitbreakers`, and `circuitbreakerevents` actuator
  endpoints (`curl http://localhost:8080/actuator/circuitbreakers`).

## What changed since Day 1

- `OrderManager` (the Day 1 God Object) is gone. It's now three focused
  collaborators:
  - `service/OrderService.java` — orchestration only (validate → check stock
    → charge → persist → update stock)
  - `service/InventoryService.java` — stock checks and updates
  - `service/PaymentService.java` — payment, now via the Strategy pattern
    instead of an if/else chain
- `validateCustomer()` / `validateItems()` were deleted — they duplicated
  inline checks and were never called (Day 1 Lab 3's Duplicated Code finding).
- `InventoryService` depends on `repository/InventoryRepository.java` (an
  interface), not on `EntityManager` directly (DIP). The only implementation
  today is `JpaInventoryRepository`.
- Payment methods are now individual `PaymentStrategy` classes
  (`CreditCardStrategy`, `PayPalStrategy`, `GiftCardStrategy`,
  `ApplePayStrategy`), auto-collected by Spring into a `Map<String,
  PaymentStrategy>`. Adding a payment method no longer touches
  `PaymentService.java` at all.

## UI redesign — Product Catalog + Place an Order

`templates/index.html` was rebuilt as a single page with:
- A **Product Catalog** table (Name / Price / In Stock) reading live from the
  `product` table via the new `model/Product.java` (read-only JPA entity) and
  `repository/ProductRepository.java` (plain `JpaRepository`, used only for
  this listing — inventory checks/decrements for placing an order still go
  through the existing `InventoryRepository` native-query path, unchanged).
- A **Place an Order** form below it, with the Item Names field labelled
  "must match catalog names exactly" — name matching is case-sensitive, so
  type item names exactly as shown in the catalog (e.g. `Laptop`, not
  `laptop`).
- `data.sql` now seeds a `price` column and five catalog items: `Laptop`
  ($999.99, qty 15), `Mouse` ($19.99, qty 100), `Keyboard` ($49.99, qty 60),
  `Monitor` ($199.99, qty 30), `Headphones` ($79.99, qty 45) — replacing the
  earlier placeholder items (`widget`/`gadget`/`gizmo`).

## Known fix applied (pre-existing bug, unrelated to Lab 4)

`Order.java` had no `@Table` override, so Hibernate defaulted its table name
to `order` — a reserved SQL keyword. Any order that got past the stock check
and payment (success or Lab 4's fallback path) would 500 with an H2 syntax
error on the insert. Fixed by mapping the entity to `@Table(name = "orders")`.

## What's deliberately NOT fixed

Day 2 is a SOLID exercise, not a security pass. Two Day 1 findings are
**intentionally still present** so they stay valid discussion points:

- **SQL injection** in `JpaInventoryRepository` — both native queries are
  still built by string concatenation.
- **TOCTOU race condition** in `OrderService.processOrder()` — stock is
  checked in one loop and decremented in a separate later loop, with payment
  in between.

If you're using this as an answer key, don't "helpfully" patch these without
flagging it to participants — the point is that a SOLID refactor doesn't
automatically fix structural/security issues.

## Running it

```
mvn spring-boot:run
```

Then open `http://localhost:8080`. The Product Catalog table and sample stock
(`Laptop`, `Mouse`, `Keyboard`, `Monitor`, `Headphones`) are seeded on startup
via `data.sql`. The H2 console is available at `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:mem:retailorderhub`).

## Lab-by-lab map

| Lab | What it does to this code |
| --- | --- |
| Day 2 Demo | Extracts `PaymentService` from `OrderManager` (if/else version) |
| Lab 1 | Extracts `InventoryService` + `OrderService`, renames `OrderManager`, applies DIP to `InventoryService` |
| Lab 2 | Rewrites `PaymentService` to the Strategy pattern, adds `ApplePayStrategy` |
| Lab 3 | Participant's own repo — adds `DebitCardStrategy` via branch → commit → push → PR → merge |
| Day 3 Lab 4 | Wraps `PaymentService.charge()` with a Resilience4j Circuit Breaker; adds `FailingPaymentStrategy` to trip it on demand |
