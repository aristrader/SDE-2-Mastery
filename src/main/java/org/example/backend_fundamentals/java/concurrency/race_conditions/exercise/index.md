---
order: 10
search: false
---

# Race Conditions Practice

## Exercise: lost-update-counter - Lost Update Counter

### Goal
See why `count++` is not atomic.

### Task
Start multiple threads. Each thread increments the same `int` counter many times.

Run the program several times and compare the final count with the expected count.

### Checks
- Explain read-modify-write.
- Explain why the result changes between runs.
- Fix it with `AtomicInteger` or synchronization.
