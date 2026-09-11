# RetailOrderHub Frontend

A plain HTML/CSS/JavaScript single-page app for the RetailOrderHub
microservices — no framework, no build step, no `npm install`. It replaces
the monolith's old Thymeleaf pages (`index.html`'s catalog + order form,
`orders.html`'s order list), now backed by the decomposed services instead
of in-process repositories.

It talks to **`api-gateway` only** — never directly to `order-service`,
`inventory-service`, or `payment-service` — the same way any real client
would.

## What it does

- **Product Catalog** — `GET /api/inventory/products`, refreshes after every
  order so stock changes are visible immediately.
- **Place an Order** — check the items you want; the amount field
  auto-totals their prices (edit it if you need to test a mismatched
  amount). Submits `POST /api/orders`.
- **Orders** — `GET /api/orders`, newest first.
- A small status dot in the header pings the catalog endpoint every 15
  seconds so you can see at a glance whether the gateway (and the chain
  behind it) is reachable.

The gateway URL is an editable field in the header (defaults to
`http://localhost:8080`) and is remembered in `localStorage`, in case your
`api-gateway` ends up on a different port.

## Running it

This is a static site - any static file server works. From this directory:

```bash
python3 -m http.server 5500
# or: npx serve -l 5500
```

Then open **http://localhost:5500**.

You cannot just double-click `index.html` and open it as a `file://` URL —
browsers send `Origin: null` for `file://` pages, and Spring's CORS handling
won't reliably allow that. A real (even tiny) HTTP server avoids this.

## CORS

Browsers block cross-origin `fetch()` calls unless the server explicitly
allows them. This frontend runs on its own origin (e.g. `:5500`) and calls
the gateway on `:8080`, so `api-gateway`'s `application.yml` has a
`globalcors` block allowing all origins - see the comment there. That's
fine for a training exercise; a real deployment would list the frontend's
actual origin(s) instead of `"*"`.

## Before you open this

All five backend services need to be up first - `eureka-server`,
`inventory-service`, `payment-service`, `order-service`, then
`api-gateway`. See the root `README.md`'s "Running it" section for the
exact order. If the status dot shows **Unreachable**, that's almost always
either the gateway not running yet, or a service that hadn't finished
registering with Eureka when a dependent service started.

## Why plain JS instead of React/Vue/etc.

Keeps this piece zero-dependency and readable without a build step, in
keeping with the rest of the course's "run it and read it" materials. If
you'd rather build this out further (multiple pages, client-side routing,
component state), it's a reasonable place to introduce a framework - but
for a single form and two tables, plain JS keeps the number of new concepts
down to the REST calls themselves.
