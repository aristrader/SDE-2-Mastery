---
order: 10
search: false
---

# Custom Exceptions and Design Practice

## Exercise: custom-exception - Custom Exception

### Goal
Create a meaningful domain exception.

### Task
Create `InvalidAgeException extends RuntimeException`.

Throw it from `validateAge()` and catch it in `main`.

Print the message and stack trace.

## Exercise: exception-translation - Exception translation

### Goal
Translate exceptions without losing the original cause.

### Task
Write `loadUserConfig(String filename)`.

It should call a helper `readFile(String filename)` that throws a simulated checked `IOException`.

`loadUserConfig` must not declare `throws IOException`; instead, translate it into an unchecked `ConfigLoadException` and preserve the original cause.

### Checks
- `loadUserConfig("missing.cfg")` throws `ConfigLoadException`.
- `ex.getCause().getClass().getSimpleName()` prints `IOException`.

## Exercise: exception-design - Exception Design

### Goal
Choose between return values, checked exceptions, and unchecked exceptions.

### Task
For each scenario, choose a handling style and justify it:

- invalid login credentials
- database unavailable
- file missing
- insufficient balance
- network timeout
- invalid API request
- coupon already redeemed

## Exercise: reflection-questions - Reflection Questions

### Goal
Check the whole exception model.

### Task
Answer:

1. Why were exceptions introduced?
2. Difference between `Error` and `Exception`?
3. Difference between checked and unchecked exceptions?
4. Difference between `throw` and `throws`?
5. Why is returning from `finally` discouraged?
6. Why is `try-with-resources` preferred?
7. Why do Spring applications commonly use `RuntimeException`?
8. When should a business failure return a result instead of throwing?
9. When would you create a custom checked exception?
10. When would you create a custom unchecked exception?
