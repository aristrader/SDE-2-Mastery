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

## Exercise: employee-tomap - Employee Maps

### Goal
Build one-key-to-one-value maps and handle duplicates intentionally.

### Task
Using `Employee`:

1. Create `id -> Employee`.
2. Create `id -> employeeName`.
3. Explain what happens if duplicate employee IDs exist.
4. Keep the latest employee when duplicate IDs exist.

### Checks
- Use `Function.identity()` when the whole employee is the value.
- Add a merge function for duplicate IDs.
