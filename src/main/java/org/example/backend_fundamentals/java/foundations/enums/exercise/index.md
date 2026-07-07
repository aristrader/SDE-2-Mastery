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
