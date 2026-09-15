---
order: 10
search: false
---

# Final exercise — Splitwise LLD

## Exercise: splitwise-lld - Expense Sharing

### Goal

Model the smallest useful in-memory Splitwise application.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### First-pass requirements

- Users can belong to groups.
- Users can create expenses directly with other users or within a group.
- Support equal, exact, and percentage splits.
- Validate that a split accounts for the complete expense amount.
- Track the directional balances between users.
- Settle an outstanding balance between two users.

### Constraints

- Keep runnable Java under `playground/` when you begin implementation.
- Start from `playground/SplitwiseRun.java`; create `model/`, `split/`, or `service/` packages only when
  a class needs them.
- Use a money representation that avoids floating-point precision errors.
- Keep database, REST APIs, notifications, and payment-gateway integration out of the first pass.
- Do not introduce a pattern until a changing requirement makes it useful.

### Interview follow-ups

- How would you add a new split type without changing expense creation?
- How would you simplify a cycle of debts?
- What changes when multiple expense updates happen concurrently?
- How would you persist expenses and reconstruct balances?
