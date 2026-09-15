## Constraints

- Use a money representation that avoids floating-point precision errors.

## Interview follow-ups

- How would you add a new split type without changing expense creation?
- How would you simplify a cycle of debts?
- What changes when multiple expense updates happen concurrently?
- How would you persist expenses and reconstruct balances?

## Requirements

- Users can belong to groups.
- Users can create expenses directly with other users or within a group.
- A group member can add an expense paid by one user and shared by selected users.
- Support equal, exact, and percentage splits.
- Validate that a split accounts for the complete expense amount.
- Track directional balances between users.
- Settle an outstanding balance between two users.

## Entities

- User
- Group
- Expense
- Split
- SplitTypes (Enums - equal, )

# Class diagram

```text
User
- id
- name
- phoneNo (at least one of phone no or email non-empty)
- email

Group
- id
- name
- List<Users>
+ addUsers
+ removeUsers

@EqualsandHashcode
UserPair
- creditorId
- debtorId

Expense
- id: int
- amount: BigDecimal
- paidBy: userId
- splits: List<Split>
- SplitType
- groupId (optional)

SplitType
- equal
- exact
- percentage

Split
- userId
- amountOwed
- inputValue

Settlement
- id
- debtorId
- creditorId
- amount: BigDecimal
- groupId (optional)

groupService
- groups : Map<groupId, Group>
- users : Map<userId, Users>
- userGroups: Map<userId, Set<GroupId>>
+ createUser(User): bool
+ deleteUser(User): bool
+ addUsers(User, groupId) : bool
+ RemoveUsers(User, groupId) : bool
+ createGroup(List<Users>) : bool
+ deleteGroup(groupId) : bool

expenseService
- groupBalances: Map<GroupId, Map<UserPair, Amount>>
- nonGroupBalances: Map<UserPair, Amount>
- settlements: Map<settlementId, Settlement>
- expenses: Map<expenseId, Expense>
+ addExpense(Expense) : bool
+ removeExpense(ExpenseId) : bool
+ settleBalance(debtorId, creditorId, amount, groupId) : bool

SplitStrategyResolver
- resolve(splitType): SplitStrategy

interface SplitStrategy
- calculate(amount, List<Splits>) : List<Splits>

EqualSplitStrategy implements SplitStrategy
ExactSplitStrategy implements SplitStrategy
PercentageSplitStrategy implements SplitStrategy
```
