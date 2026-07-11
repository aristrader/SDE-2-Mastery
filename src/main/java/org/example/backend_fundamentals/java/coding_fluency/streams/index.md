---
order: 30
---

# Streams and Collectors

Streams turn a sequence of elements into a result. This module focuses on collectors because they connect directly to Lists, Sets, Maps, grouping, and reporting code.

Read this after Collections. The collector result has the same trade-offs as the target collection: mutability, ordering, duplicate keys, equality, and hash behavior still matter.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | `toList`, `Stream.toList`, `toSet`, and mutability. |
| 2 | `grouping` | `groupingBy`, downstream collectors, counts, sums, averages, mapping. |
| 3 | `to_map` | lookup maps, duplicate-key traps, and merge functions. |
| 4 | `joining` | turning streams of strings into delimited output. |
| 5 | `advanced_collectors` | `partitioningBy`, `collectingAndThen`, `maxBy`, and `teeing`. |

## Quick recall

- **Need a list from a stream?** Decide whether it should be mutable before choosing `Collectors.toList()` or `Stream.toList()`.
- **Need one key to many values?** Use `groupingBy`.
- **Need one key to one value?** Use `toMap`, and add a merge function when duplicate keys are possible.
- **Need a CSV-style string?** Map to `String`, then use `joining`.
