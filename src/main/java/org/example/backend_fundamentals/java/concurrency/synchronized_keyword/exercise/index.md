---
order: 10
search: false
---

# synchronized Practice

## Exercise: synchronized-counter - Synchronized Counter

### Goal
Protect a compound update with a monitor.

### Task
Create a `Counter` with `increment()` and `value()`.

Make both methods `synchronized`. Run multiple threads that increment the counter.

### Checks
- Final count matches expected count.
- Explain which object is used as the monitor.

## Exercise: wrong-lock-object - Wrong Lock Object

### Goal
Identify lock identity bugs.

### Task
Explain why this does not protect `count`:

```java
synchronized (new Object()) {
    count++;
}
```

### Checks
- Explain why every call uses a different monitor.
- Replace it with a private final lock object.

## Exercise: partial-synchronization - Partial Synchronization

### Goal
Keep check and update in the same critical section.

### Task
Fix a withdraw method where `if (balance >= amount)` is outside the synchronized block but `balance -= amount` is inside.

### Checks
- Explain the check-then-act race.
- Put the complete invariant inside one monitor.

## Exercise: lock-scope - Lock Scope

### Goal
Avoid holding a lock during slow work.

### Task
Refactor a synchronized order method that validates, calls remote payment, updates balance, and sends email.

### Checks
- Keep remote I/O outside the critical section when business rules allow.
- Explain what invariant remains protected.

## Exercise: synchronized-block-counter - Synchronized Block Counter

### Goal
Practice locking only the critical section.

### Task
Rewrite the counter with a private final lock object and a `synchronized (lock)` block.

### Checks
- The lock object is `private final`.
- `value()` uses the same lock as `increment()`.

## Exercise: read-write-same-lock - Reader And Writer Same Lock

### Goal
Avoid false confidence from synchronizing only writes.

### Task
Create a counter with synchronized `increment()` and unsynchronized `value()`. Explain the visibility issue, then fix it.

### Checks
- Reads and writes coordinate through the same monitor or an atomic type.
- Explain why using a different lock for reads is also wrong.
