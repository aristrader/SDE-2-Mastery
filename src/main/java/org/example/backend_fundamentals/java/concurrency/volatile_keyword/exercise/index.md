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
