---
order: 160
---

# Vending Machine LLD

Practice modelling inventory, a cash transaction, change, and the lifecycle around one purchase.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Use the [design notes](design/) to follow the cash purchase, responsibility boundaries, and follow-up evolution.

## Working order

1. Clarify the payment method, change requirement, and transaction scope.
2. Identify the state that belongs to inventory versus the active purchase before drawing classes.
3. Model the success, cancellation, and insufficient-change paths.
4. Add extension points only after the one-item cash flow is correct.

## Quick recall

- Inventory changes only after payment and change validation both succeed.
- The active transaction owns selected item and inserted cash; the machine owns long-lived stock.
- A cancelled or failed purchase must refund the inserted amount and clear transaction state.
