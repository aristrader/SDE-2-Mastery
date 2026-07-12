---
order: 10
search: false
---

# Atomic Classes Practice

## Exercise: atomic-counter - Atomic Counter

### Goal
Use the smallest correct construct for one shared counter.

### Task
Create a counter backed by `AtomicInteger`. Run four threads, each incrementing it 100,000 times, then print the final value.

### Checks
- Final value is `400000`.
- Explain why `volatile int` would not be enough.
- Explain the difference between `incrementAndGet()` and `getAndIncrement()`.

## Exercise: bounded-stock-cas - Bounded Stock CAS

### Goal
Fix a check-then-act race without `synchronized`.

### Task
Implement `boolean purchase()` using `AtomicInteger.compareAndSet()` so stock never goes below zero.

### Checks
- Two concurrent buyers cannot both buy the final item.
- The CAS loop exits when stock is already zero.
- Explain what a failed CAS means.

## Exercise: read-and-reset - Atomic Read And Reset

### Goal
Use one atomic operation for metrics reset.

### Task
Implement `int readAndReset()` for a metrics counter.

### Checks
- Use `getAndSet(0)`.
- Explain why `get(); set(0);` can lose increments.

## Exercise: two-counter-snapshot - Two Counter Snapshot

### Goal
Recognize when atomics are too small.

### Task
Design a metrics component with request and failure counts. It must read and reset both counts consistently.

### Checks
- Use one monitor or another combined coordination mechanism.
- Explain why two independent atomics are only acceptable for approximate metrics.
