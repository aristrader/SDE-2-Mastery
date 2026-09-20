---
search: false
---

# Candidate discussion — Splitwise

Start with expense recording and balance updates rather than debt simplification or payments:

> “I’ll model users, expenses, supported split types, and directional balances in memory. Groups can be a
> context for an expense, but direct expenses should work too.”

## Sample clarification conversation

**Candidate:** “May an expense be direct as well as within a group, and which split types are required?”

**Interviewer:** “Both contexts are needed. Support equal, exact amounts, and percentages.”

**Decision:** `groupId` is optional on `Expense`; calculation varies behind `SplitStrategy`, while balance mutation does not.

**Candidate:** “Do we collect money or merely record who owes whom? What precision should the MVP use?”

**Interviewer:** “Record balances and manual settlement only. Avoid floating-point rounding errors.”

**Decision:** use `BigDecimal`; represent settlement as a reversal of an existing directional balance. Payment-provider calls are out of scope.

**Candidate:** “Should I solve debt cycles, persistence, notifications, multi-currency, or simultaneous updates now?”

**Interviewer:** “No. Explain them as follow-ups after the in-memory flow works.”

**Decision:** preserve expense history and pairwise balances in memory for the exercise. Simplification is a group-only payment suggestion; persisted concurrency needs a transaction and coordination boundary.

## Agreed scope for this exercise

- Support groups and direct expenses among users.
- Support equal, exact, and percentage splits, validating that splits account for the whole expense.
- Maintain directional balances and allow settlement between two users.
- Keep the model in memory and use a precise money representation.
- Payment collection, debt simplification, persistence, notifications, multi-currency, and concurrency
  are follow-ups.

The final [exercise](../) records the requirements and likely extensions.
