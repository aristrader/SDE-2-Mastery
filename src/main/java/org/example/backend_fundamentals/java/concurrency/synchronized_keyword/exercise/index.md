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
