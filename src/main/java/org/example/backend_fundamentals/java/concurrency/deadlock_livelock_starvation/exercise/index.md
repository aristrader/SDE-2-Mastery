---
order: 10
search: false
---

# Deadlock, Livelock, Starvation Practice

## Exercise: create-deadlock - Create A Deadlock

### Goal
Recognize circular lock dependency.

### Task
Create two locks. Thread A locks `lock1` then `lock2`; Thread B locks `lock2` then `lock1`.

### Checks
- Explain the circular wait.
- Explain why `sleep()` only makes the bug easier to reproduce.

## Exercise: fix-deadlock-ordering - Fix Deadlock With Ordering

### Goal
Remove circular wait.

### Task
Rewrite the two-lock example so every thread acquires locks in the same order.

### Checks
- Every path locks `lock1` before `lock2`.
- Explain why consistent ordering prevents the cycle.

## Exercise: account-transfer-ordering - Account Transfer Lock Ordering

### Goal
Apply lock ordering to a backend-style invariant.

### Task
Implement transfer between two accounts using account ID ordering.

### Checks
- Reject self-transfer and non-positive amounts.
- Check balance while holding both account locks.
- Explain why one `AtomicLong` per account is insufficient.

## Exercise: pool-starvation - Thread Pool Starvation

### Goal
Identify progress failure without explicit monitors.

### Task
Describe what happens when two tasks in a two-thread pool each submit another task to the same pool and wait for it.

### Checks
- Identify queued tasks with no free worker.
- Propose one fix.
