---
order: 10
search: false
---

# Exercise

## Exercise: repair-order-schema - Normalize an Order Capture Table

An early checkout service stores one row per purchased item:

```text
OrderId | CustomerId | CustomerEmail | ProductId | ProductName | UnitPrice | Quantity | CategoryName
```

The primary key is `(OrderId, ProductId)`. A product belongs to one category; a customer can have many
orders; an order can contain many products. The business also requires the historical price and product
name shown on a completed order to remain unchanged after the catalogue later changes.

Work through this as you would in an interview:

1. Name one update, insertion, and deletion anomaly in the starting shape.
2. State the useful functional dependencies. Identify the partial and transitive dependencies.
3. Propose tables, primary keys, and foreign-key relationships that reach 3NF for live customer and
   catalogue data.
4. Explain why keeping `UnitPriceAtPurchase` and `ProductNameAtPurchase` on `OrderItems` is a separate,
   historical fact—not an accidental duplicate of the current catalogue field or a 3NF mistake.
5. Say which database constraints enforce the model and which rule still belongs in the transaction or
   application service.

Do not recite normal-form definitions first. Start from the anomaly, then show how each split gives one
fact a clear owner.

## Exercise: choose-read-model - Justify a Deliberately Denormalized Read Model

An operations dashboard needs the latest 30 paid orders for one seller, including buyer display name,
item count, total, and payment status. It is called frequently; the transactional write path is much less
frequent.

Choose between querying normalized tables on every request, a materialized/precomputed summary, or a cache.
Give an interview-ready answer covering:

1. the default normalized source of truth;
2. the measured pressure that justifies copying data;
3. the owner and refresh path for the copied fields;
4. the stale-data and failure behaviour; and
5. one signal that would make you remove or redesign the read model.

## Quick recall

**Q. Is every repeated column a normalization bug?**
A. No. A repeated value can be a deliberate snapshot or read model when its owner, refresh rule, and
staleness boundary are explicit.

**Q. What is the fastest way to explain a split?**
A. Name the fact, its determinant, and its single owner—for example, `ProductId → current product name`
belongs in `Products`, while the purchased name belongs to the immutable order item.
