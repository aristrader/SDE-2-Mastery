---
order: 130
---

# Splitwise LLD

Workspace for designing an in-memory expense-sharing application.

Start with users, groups, equal/exact/percentage splits, balances, and settlement. Keep persistence,
notifications, debt simplification, multi-currency, and payment collection out of the first pass unless
they are explicitly requested.

## Working order

1. Clarify supported split types and whether a group is required.
2. Define the money and balance invariants before choosing classes.
3. Model expense creation and balance updates.
4. Add settlement only after the expense flow is correct.

## Quick recall

- A split must account for the complete expense amount.
- Balance updates should be derived from one expense, not scattered across callers.
- Keep an initial design in memory; persistence is an extension, not the starting point.
