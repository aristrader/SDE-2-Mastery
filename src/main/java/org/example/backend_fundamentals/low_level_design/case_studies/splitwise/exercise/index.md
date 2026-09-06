---
order: 10
search: false
---

# Splitwise LLD Exercise

## Exercise: splitwise-lld - Expense Sharing

### Goal

Model the smallest useful in-memory Splitwise application.

### First-pass requirements

- Users can belong to groups.
- A group member can add an expense paid by one user and shared by selected users.
- Support equal, exact, and percentage splits.
- Validate that a split accounts for the complete expense amount.
- Track the directional balances between users.
- Settle an outstanding balance between two users.

### Constraints

- Keep runnable Java under `playground/` when you begin implementation.
- Use a money representation that avoids floating-point precision errors.
- Keep database, REST APIs, notifications, and payment-gateway integration out of the first pass.
- Do not introduce a pattern until a changing requirement makes it useful.

### Interview follow-ups

- How would you add a new split type without changing expense creation?
- How would you simplify a cycle of debts?
- What changes when multiple expense updates happen concurrently?
- How would you persist expenses and reconstruct balances?
