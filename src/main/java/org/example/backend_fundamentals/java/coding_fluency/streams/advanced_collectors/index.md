---
order: 50
---

# Advanced Collectors

These collectors are useful after the common cases are comfortable.

| Collector | Use it for |
| --- | --- |
| `partitioningBy(predicate)` | exactly two buckets: `true` and `false` |
| `collectingAndThen(downstream, finisher)` | transform the collector result |
| `maxBy(comparator)` | maximum element, usually downstream of `groupingBy` |
| `teeing(a, b, merger)` | run two collectors in one pass, Java 12+ |

Prefer the simpler collector when it fits. Advanced collectors are for reducing extra passes or making the result type exact.

## Quick recall

- **Always both boolean keys?** `partitioningBy`.
- **Convert `Long` count to `Integer` inside collector?** `collectingAndThen(counting(), Long::intValue)`.
- **Why does `maxBy` return `Optional`?** The collector cannot prove the group is non-empty.
