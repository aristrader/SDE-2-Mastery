---
order: 10
search: false
---

# volatile Practice

## Exercise: volatile-stop-signal - Stop Signal

### Goal
Use `volatile` for a visibility-only flag.

### Task
Build a worker thread that runs until `stop()` sets a flag to false.

Use `volatile boolean running`.

### Checks
- The worker eventually exits after `stop()`.
- You do not use `volatile` for a counter increment.

## Exercise: broken-volatile-counter - Broken Volatile Counter

### Goal
Separate visibility from atomicity.

### Task
Create `volatile int count`. Start four threads, each incrementing it 100,000 times. Observe that the final value can be below `400000`.

### Checks
- Explain read-modify-write interleaving.
- Fix it with `AtomicInteger` or `synchronized`.

## Exercise: immutable-config-snapshot - Immutable Config Snapshot

### Goal
Use volatile reference replacement safely.

### Task
Create an immutable `AppSettings` record and a holder with `volatile AppSettings settings`. Readers call `get()`, and writers replace the entire settings object.

### Checks
- Do not mutate fields inside the settings object.
- Explain why readers see old or new complete snapshots, not partial updates.
