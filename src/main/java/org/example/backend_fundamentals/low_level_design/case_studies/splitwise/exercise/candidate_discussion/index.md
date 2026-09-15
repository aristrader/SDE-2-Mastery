---
search: false
---

# Candidate discussion — Splitwise

Start with expense recording and balance updates rather than debt simplification or payments:

> “I’ll model users, expenses, supported split types, and directional balances in memory. Groups can be a
> context for an expense, but direct expenses should work too.”

## Questions to ask

1. Which split types are required: equal, exact amounts, percentages, or all three?
2. Can an expense be shared directly between users as well as inside a group?
3. What money representation avoids rounding errors for the first implementation?
4. Should the system track balances only, or actually collect payments and simplify debt cycles?
5. Are persistence, notifications, multi-currency, and concurrent expense updates in scope now?

## Agreed scope for this exercise

- Support groups and direct expenses among users.
- Support equal, exact, and percentage splits, validating that splits account for the whole expense.
- Maintain directional balances and allow settlement between two users.
- Keep the model in memory and use a precise money representation.
- Payment collection, debt simplification, persistence, notifications, multi-currency, and concurrency
  are follow-ups.

The final [exercise](../) records the requirements and likely extensions.
