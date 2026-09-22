---
order: 20
search: false
---

# Solution

## Solution: repair-order-schema - Normalize an Order Capture Table

The starting table mixes four facts: the customer, the order, the current catalogue item, and the item
snapshot sold with this order. That makes a change to one fact require updates to rows that describe another.

### Start with the anomalies

- **Update:** changing a customer's email or a product category requires changing every matching order-item
  row; a missed row leaves two answers to the same question.
- **Insertion:** a new product or category cannot be recorded until somebody buys it.
- **Deletion:** deleting an order's last item can accidentally remove the only stored copy of product or
  customer information.

Those outcomes are the pressure to split—not normal-form names by themselves.

### Follow the dependencies

```text
CustomerId → CustomerEmail
ProductId  → current ProductName, CategoryId
CategoryId → CategoryName
OrderId    → CustomerId, order-level state
(OrderId, ProductId) → Quantity, UnitPriceAtPurchase, ProductNameAtPurchase
```

With composite key `(OrderId, ProductId)`, customer and current-product fields depend on only one part of
the key, so they are **partial dependencies**. `ProductId → CategoryId → CategoryName` is a transitive
dependency. A first split also makes each cell atomic; do not store several products or category IDs in one
column.

### Give each mutable fact one owner

```text
Customers(CustomerId PK, Email UNIQUE, ...)
Categories(CategoryId PK, Name UNIQUE)
Products(ProductId PK, CategoryId FK, CurrentName, CurrentPrice, ...)
Orders(OrderId PK, CustomerId FK, Status, CreatedAt, ...)
OrderItems(OrderId FK, ProductId FK, Quantity,
           UnitPriceAtPurchase, ProductNameAtPurchase,
           PRIMARY KEY (OrderId, ProductId))
```

`Customers`, `Products`, and `Categories` are the live 3NF model: each non-key fact depends on its table's
key and is stored once. `OrderItems` is the relationship between one order and its products, so quantity
depends on the whole `(OrderId, ProductId)` key.

The two `...AtPurchase` fields are distinct, time-bound facts. An invoice must describe what was sold, not
silently change when the catalogue is edited. Their owner is the immutable order item, and their refresh
rule is **never refresh after checkout**. They are not redundant copies of the *current* catalogue facts,
so this is a domain snapshot compatible with 3NF, not accidental denormalization.

### Enforce the boundary

Use primary keys, foreign keys, `NOT NULL`, and `CHECK (quantity > 0)` for facts the database can verify
locally. Add `UNIQUE` only where the product rules require it—for example, an email may be unique if it is
the account identifier, while two category names may be valid in different catalogues. The
transaction/application service still owns cross-row business rules such as “the order is editable only
before payment” and “requested quantity is currently reservable”; it reads, validates, and writes
atomically. Constraints remain the final guard against invalid rows even if another writer bypasses a
service path.

## Solution: choose-read-model - Justify a Deliberately Denormalized Read Model

Keep normalized `Orders`, `OrderItems`, `Payments`, `Sellers`, and `Customers` as the source of truth. Start
by measuring the dashboard query and adding the indexes it actually needs. If that still consumes a material
share of database time because the same aggregation and joins are repeatedly requested, create an
`SellerOrderSummary` read model keyed by `(seller_id, order_id)`.

The write that marks an order paid updates the normalized transaction and records a summary-update event in
the same local transaction (for example, an outbox row). A worker applies that event idempotently to the
summary. The dashboard then has a simple seller/time query; it accepts that a newly paid order can appear a
short time later.

The recovery story matters more than the table name:

```text
write succeeds but worker is down → outbox remains pending → worker retries
event is delivered twice          → upsert by order/version is harmless
summary is suspected stale        → rebuild from normalized source of truth
```

Cache the completed summary only when its access pattern is hot enough to justify another stale copy; cache
misses must fall back to the summary or source of truth, not invent a second write authority. Remove or
redesign the read model if freshness becomes a strict requirement, its refresh lag breaches the product
promise, or its maintenance cost exceeds the saved query load.

## Quick recall

**Q. Why is an order-item price allowed to look like the product price?**
A. It records a different historical sale fact. It has an immutable order-item owner, unlike the mutable
current catalogue price.

**Q. What makes a denormalized read model safe to operate?**
A. One normalized source of truth, an idempotent refresh path, an explicit staleness promise, and a way to
rebuild the copy.
