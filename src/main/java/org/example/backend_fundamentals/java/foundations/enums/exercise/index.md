---
order: 10
search: false
---

# Practice

## Exercise: enum-with-state - Enums with State

### Goal
Learn how to attach data fields to enum constants, a common pattern for error codes and statuses.

### Task
Create an enum called `ErrorCode`.
Add two constants: `USER_NOT_FOUND` and `INVALID_TOKEN`.
Give the enum two `private final` fields: a `String code` (e.g., "E001") and a `String message` (e.g., "User not found").
Add a constructor to initialize these fields, and public getters to access them.

### Checks
- In `main`, iterate over `ErrorCode.values()` and print each constant's code and message.

## Exercise: enum-methods - Enums with Behavior

### Goal
Understand that enums are full classes and can contain business logic methods.

### Task
Add a `public boolean isCritical()` method to your `ErrorCode` enum.
Make it return `true` if the enum instance is `INVALID_TOKEN`, and `false` otherwise.

### Checks
- Can you call `ErrorCode.INVALID_TOKEN.isCritical()` and get `true`?

## Exercise: transaction-status-behavior - Constant-Specific Behavior

### Goal
Model a closed transaction lifecycle without spreading status rules across callers.

### Task
Create `TransactionStatus` with `PENDING`, `SETTLED`, and `REJECTED`.

- Declare `boolean isFinal()` as an abstract enum method.
- Implement it per constant: `PENDING` is not final; the other two are final.
- Implement a small `Labeled` interface with `String label()` and have the enum implement it using `name().toLowerCase(Locale.ROOT)`.

### Checks
- The enum must not compile if one constant omits `isFinal()`.
- `PENDING.isFinal()` is `false`; `SETTLED.isFinal()` is `true`.

## Exercise: exhaustive-status-switch - Compiler-Checked Decisions

### Goal
Use a switch expression that cannot silently ignore a new transaction status.

### Task
Write `String nextAction(TransactionStatus status)` using a switch expression:

- `PENDING` returns `"wait"`.
- `SETTLED` returns `"notify"`.
- `REJECTED` returns `"investigate"`.
- Do not add a `default` branch.

### Checks
- All three constants have an explicit arm.
- Add a temporary fourth constant and observe that the switch must be updated before it compiles.
