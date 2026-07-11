---
order: 10
search: false
---

# toMap Practice

## Exercise: tomap-unique - toMap unique keys

### Goal
Build a lookup map from unique keys.

### Task
Produce `Map<String, Order>` keyed by order ID.

### Checks
- Use `Function.identity()` for the value.
- Explain why duplicate IDs would fail.

## Exercise: tomap-duplicate - toMap duplicate key handler

### Goal
Handle duplicate keys with a merge function.

### Task
Build `Map<String, BigDecimal>` keyed by `customerId`, where the value is the sum of all order totals for that customer.

Try the two-arg form first, then switch to the three-arg form.

### Checks
- Customer `C1` totals all its orders.
- The merge function adds, not overwrites.
