---
order: 130
---

# Splitwise LLD

Design the smallest in-memory expense-sharing application that can explain one hard fact: an expense changes
several people’s balances, so split calculation and balance mutation must happen in one flow.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).

## Scope and invariants

The MVP supports users, optional groups, direct or group expenses, equal/exact/percentage splits, pairwise
directional balances, settlement, and expense removal. It deliberately excludes persistence, payment collection,
notifications, multi-currency, debt simplification, and concurrent writes.

- An expense has a positive amount, one payer, and unique participants.
- Calculated shares must equal the expense amount. The current equal strategy rounds down to two decimals and
  assigns the remainder to the last participant; exact amounts and percentages must total the supplied amount or 100.
- `balances[debtor][creditor] > 0` means the debtor owes the creditor that amount. The reciprocal entry is negative;
  the pair therefore always nets to zero.
- A group expense is valid only when its payer and every participant are current members of that group.
- A settlement is positive, between distinct existing users, and cannot exceed the recorded directional balance.

## Representative flow — Alice pays dinner

Alice pays 100.00 for Alice and Bob equally. `ExpenseService` first asks the selected `SplitStrategy` to calculate
both 50.00 shares. It validates the user IDs (and group membership when a group ID is present), skips Alice’s own
share, then records `balances[Bob][Alice] += 50.00` and the reciprocal `balances[Alice][Bob] -= 50.00` through one
private helper. When Bob settles 20.00, the same helper applies the reverse debt, leaving Bob owing Alice 30.00.

That is the core interview boundary: split strategies decide **what each participant owes**; `ExpenseService` is the
only current owner of **how those shares mutate balances**. The runnable `playground/SplitwiseRun.java` is the source
of truth for this MVP.

## Why these responsibilities exist

| Responsibility | Current owner | Reason |
| --- | --- | --- |
| User/group registry and membership checks | `GroupService` | An expense must validate membership without making `Expense` own group state. |
| Share calculation and input validation | `SplitStrategy` implementations | Equal, exact, and percentage inputs vary independently of ledger mutation. |
| Selecting a strategy | `SplitStrategyResolver` | Expense creation does not branch on each calculation rule. |
| Expense registry, settlement, and pairwise balance projection | `ExpenseService` | One mutation path preserves reciprocal balances and makes removal reversible. |

Do not add a payment service or a debt-simplification strategy to this pass: neither is required to record an expense
correctly. See [design notes](design/) for the deliberately separate simplification and persisted-concurrency follow-ups.

## Interview delivery order

1. Confirm direct versus group expenses, required split types, money precision, and whether actual payments are in scope.
2. State the share-total and reciprocal-balance invariants.
3. Walk the dinner flow before naming classes.
4. Derive `GroupService`, `SplitStrategy`, and `ExpenseService` from that flow.
5. Discuss simplification, persistence, and concurrent writes only as explicit extensions.

## Quick recall

- A split must account for the complete expense amount.
- A reciprocal balance is an invariant, not a second business operation.
- Keep the initial design in memory; persistence and concurrent writes change the boundary.
