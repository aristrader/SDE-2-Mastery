---
order: 10
search: false
---

# JMM Practice

## Exercise: visibility-flag - Visibility Flag

### Goal
Understand why one thread's write may not become visible to another thread immediately.

### Task
Create a worker loop controlled by a shared boolean flag.

First use a plain `boolean`. Then change it to `volatile`.

### Checks
- Explain why the plain flag is unsafe.
- Explain what `volatile` changes.
- Explain why `volatile` still does not make compound updates atomic.
