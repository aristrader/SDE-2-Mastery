---
order: 10
search: false
---

# Garbage Collection Practice

## Exercise: reachability-and-leak - Reachability and Memory Leak

### Goal
Separate unreachable objects from reachable-but-unwanted objects.

### Task
Write two small examples:

1. create objects inside a method and let the method return
2. keep adding objects to a static `List`

Explain which objects are eligible for GC and which remain reachable.

### Checks
- You use reachability, not "no variable name", as the rule.
- You explain why the static list can cause a memory leak.
