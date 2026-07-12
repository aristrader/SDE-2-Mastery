---
order: 100
---

# Stream Interview Drills

Use this page after the topic pages. The goal is to pick the right stream operation quickly, not to memorize one long pipeline.

## How to approach

1. Decide the shape: keep, transform, flatten, group, reduce, or test.
2. Pick intermediate operations first.
3. Pick exactly one terminal operation.
4. Check traps: duplicate map keys, empty `Optional`, reused streams, parallel mutation, and `peek` side effects.

## Quick recall

- **Keep elements?** `filter`.
- **Transform elements?** `map`.
- **Flatten nested values?** `flatMap`.
- **One key to many values?** `groupingBy`.
- **One key to one value?** `toMap`.
- **Boolean split?** `partitioningBy`.
- **Existence check?** `anyMatch`.
