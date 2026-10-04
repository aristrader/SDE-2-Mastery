# Splitwise — entity identification and class diagrams

## Goal

Model the smallest useful in-memory Splitwise application.

This is the agreed scope after discussing the initial [interviewer prompt](../exercise/problem_statement/) and
[candidate clarifications](../exercise/candidate_discussion/).

## Requirements

- Users can belong to groups.
- Users can create expenses directly with other users or within a group.
- Support equal, exact, and percentage splits.
- Validate that a split accounts for the complete expense amount.
- Track the directional balances between users.
- Settle an outstanding balance between two users.

## Constraints

- Use a money representation that avoids floating-point precision errors.
- Keep database, REST APIs, notifications, and payment-gateway integration out of the first pass.
- Do not introduce a pattern until a changing requirement makes it useful.

## Test scenarios

1. Alice pays 100.00 for Alice and Bob equally; Bob owes Alice 50.00.
2. An exact split whose inputs do not total the expense, or percentages that do not total 100, is rejected.
3. A group expense is rejected when its payer or a participant is not a member; a direct expense has no group check.
4. Bob settles 20.00 of the first debt; the remaining Bob → Alice balance is 30.00. A larger settlement is rejected.
5. Removing a recorded expense reverses the debts it originally added.

## Interview follow-ups

- How would you add a new split type without changing expense creation?
- How would you simplify a cycle of debts?
- What changes when multiple expense updates happen concurrently?
- How would you persist expenses and reconstruct balances?

## Entities

- User
- Group
- Expense
- Split
- SplitType

## Membership model

`Group.memberIds` is the forward lookup: who belongs to this group. `GroupService.userGroups` is the
reverse lookup: which groups contain this user. They describe the same membership from opposite directions;
the service updates both together. The group keeps IDs rather than mutable `User` objects, so membership is
not affected if a user's profile changes.

## Current playground class diagram

```text
User
- id: int
- name: String
- phoneNo: Long (at least one of phoneNo or email is non-empty)
- email: String

Group
- id: int
- name: String
- memberIds: Set<Integer>
+ addUser(userId): boolean
+ removeUser(userId): boolean

UserPair
- creditorId: int
- debtorId: int
- immutable value object; currently not used by balance storage

Expense
- id: int
- amount: BigDecimal
- paidByUserId: Integer
- splits: List<Split>
- splitType: SplitType
- groupId: Integer (optional for a direct expense)

SplitType
- EQUAL
- EXACT
- PERCENTAGE

Split
- userId: Integer
- amountOwed: BigDecimal (calculated by a strategy; may be null on creation)
- inputValue: BigDecimal (optional for equal split; exact amount or percentage for other split types)

Settlement
- id: int
- debtorId: Integer
- creditorId: Integer
- amount: BigDecimal
- groupId: Integer (optional)

GroupService
- groups: Map<Integer, Group>
- users: Map<Integer, User>
- userGroups: Map<Integer, Set<Integer>>
+ createUser(name, phoneNo, email): int
+ createGroup(name): int
+ deleteUser(userId): boolean
+ addUser(userId, groupId): boolean
+ addUsers(userIds, groupId): boolean
+ groupExists(groupId): boolean
+ usersExist(userIds): boolean
+ areUsersInGroup(groupId, userIds): boolean

GroupService -> Group: owns group registry
GroupService -> User: owns user registry
GroupService -> userGroups: reverse membership index
Group -> memberIds: forward membership index

ExpenseService
- groupBalances: Map<Integer, Map<Integer, Map<Integer, BigDecimal>>>
- nonGroupBalances: Map<Integer, Map<Integer, BigDecimal>>
- settlements: Map<Integer, Settlement>
- expenses: Map<Integer, Expense>
- splitStrategyResolver: SplitStrategyResolver
- groupService: GroupService
+ addExpense(expense): void
+ removeExpense(expenseId): void
+ settleBalance(settlement): void
+ getBalance(userId, counterpartId, groupId): BigDecimal
- validateUniqueParticipants(splits): void
- validateUsersExist(userIds): void
- addGroupExpenses(expense, userIds): void
- addNonGroupExpenses(expense): void
- addDebt(balances, debtorId, creditorId, amount): void

ExpenseService -> GroupService: shared user and group registry
ExpenseService -> SplitStrategyResolver: selects split calculation

SplitStrategyResolver
- resolve(splitType): SplitStrategy

interface SplitStrategy
- calculate(amount, List<Splits>) : List<Splits>

EqualSplitStrategy implements SplitStrategy
ExactSplitStrategy implements SplitStrategy
PercentageSplitStrategy implements SplitStrategy
```

## Current implementation boundary

- `ExpenseService` validates that referenced users exist and, for a group expense, that the group exists and
  contains the payer and every participant.
- Each participant can appear only once in an expense; duplicate IDs are rejected before split calculation.
- Balances are stored from both users' perspectives: a positive entry means the outer user owes the counterpart;
  the reciprocal entry is negative. A payer's own split is skipped so no self-debt is recorded.
- A settlement validates that the debtor currently owes the creditor at least the settled amount. It records
  the payment and reverses that amount through the same balance helper used by expenses.
- Removing an expense reverses its recorded debts before deleting it from the expense registry.
- `Split.inputValue` is optional at construction. Exact and percentage strategies validate the input value that
  their respective split types require.
