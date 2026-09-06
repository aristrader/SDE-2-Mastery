---
order: 20
search: false
---

# Splitwise Design Notes

Use this page to record the decisions made while implementing the exercise.

## Invariants to protect

- Every expense has one payer, a positive amount, and at least one participant.
- Shares total exactly to the expense amount after any rounding rule is applied.
- A balance is directional: an amount one user owes another is not the same entry in reverse.
- Settling a balance cannot create a negative settlement amount.

## Decisions to make

- Which class owns split validation: expense, split strategy, or a dedicated validator?
- Should `Group` own balances, or should a ledger service own them?
- How will exact and percentage splits represent and validate money?
- Is a settlement a separate transaction record or only a direct balance adjustment in the MVP?

## Extensions after the MVP

- Debt simplification across a group.
- Persistent expense and settlement history.
- Concurrent updates and idempotency.
- Multi-currency support and rounding policy.

## Quick recall

- Use integer minor units or `BigDecimal` for money; never `double`.
- Model balances as an explicit ledger concern instead of recalculating every screen from scratch.
- Keep split calculation separate from balance mutation when different split types are required.
