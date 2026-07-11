---
order: 10
search: false
---

# Locks Practice

## Exercise: trylock-timeout - tryLock Timeout

### Goal
Use explicit locks when lock acquisition needs more control than `synchronized`.

### Task
Use `ReentrantLock.tryLock(timeout, unit)` to attempt a protected operation.

Always release the lock in `finally` only if it was acquired.

### Checks
- No unlock happens when the lock was not acquired.
- Explain why `finally` is required.
