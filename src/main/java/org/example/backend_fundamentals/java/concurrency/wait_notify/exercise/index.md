---
order: 10
search: false
---

# wait/notify Practice

## Exercise: single-slot-buffer - Single Slot Buffer

### Goal
Implement the monitor wait pattern.

### Task
Create `put(T)` and `take()` for one shared slot. Producer waits when full; consumer waits when empty.

### Checks
- Use `while`, not `if`.
- Call `wait()` only while holding the monitor.
- Call `notifyAll()` after changing the condition.

## Exercise: bounded-buffer - Bounded Buffer

### Goal
Generalize one slot to a queue.

### Task
Implement `put`, `take`, and `size` using `ArrayDeque`, `synchronized`, `wait()`, and `notifyAll()`.

### Checks
- Reject non-positive capacity.
- `ArrayDeque` is accessed only while holding the monitor.
- Explain the queue-size invariant.

## Exercise: producer-consumer-shutdown - Producer Consumer Shutdown

### Goal
Terminate consumers cleanly.

### Task
Run two producers and two consumers against a bounded buffer. Use one poison pill per consumer after producers finish.

### Checks
- Poison pills are inserted after producer joins.
- One poison pill is not used for multiple consumers.

## Exercise: interruption-aware-buffer - Interruption Aware Buffer

### Goal
Do not swallow cancellation.

### Task
Rewrite the buffer methods to declare `throws InterruptedException`.

### Checks
- No conversion to unchecked exception inside the buffer.
- Caller owns retry/exit/shutdown policy.

## Exercise: alternate-odd-even - Alternate Odd And Even

### Goal
Build turn-based coordination muscle memory.

### Task
Use two threads to print numbers `1..20` in order. One thread prints odd numbers; the other prints even numbers.

### Checks
- Use one shared lock.
- Use `while` around the turn condition.
- Use `notifyAll()` after advancing the number.
