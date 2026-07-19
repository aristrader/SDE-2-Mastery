---
order: 50
---

# Streams and Collectors

Streams turn a sequence of elements into a result. This module focuses on collectors because they connect directly to Lists, Sets, Maps, grouping, and reporting code.

Read this after Collections. The collector result has the same trade-offs as the target collection: mutability, ordering, duplicate keys, equality, and hash behavior still matter.

## Laziness mental model

Intermediate operations build a recipe; a terminal operation runs it.

```text
source -> filter -> map -> limit -> terminal
          recipe    recipe  recipe   execution starts here
```

This is why `peek`, `filter`, and `map` appear to do nothing until `collect`, `count`, `forEach`, `findFirst`, or another terminal operation is called.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `foundations` | stream lifecycle, laziness, terminal operations, and single-use streams. |
| 2 | `intermediate_operations` | `filter`, `map`, `flatMap`, sorting, dedupe, slicing, and `peek`. |
| 3 | `terminal_operations` | `collect`, `forEach`, `count`, `findFirst`, `findAny`, match operations, and `reduce`. |
| 4 | `basics` | `toList`, `Stream.toList`, `toSet`, and mutability. |
| 5 | `joining` | turning streams of strings into delimited output. |
| 6 | `to_map` | lookup maps, duplicate-key traps, and merge functions. |
| 7 | `grouping` | `groupingBy`, downstream collectors, counts, sums, averages, mapping. |
| 8 | `advanced_collectors` | `partitioningBy`, `collectingAndThen`, `maxBy`, and `teeing`. |
| 9 | `primitive_parallel` | primitive streams, parallel stream trade-offs, and performance pitfalls. |
| 10 | `interview_drills` | mixed pipelines, output prediction, bug finding, loop conversion, and API choice. |

## Quick recall

- **No terminal operation?** The stream pipeline does not run.
- **Need a list from a stream?** Decide whether it should be mutable before choosing `Collectors.toList()` or `Stream.toList()`.
- **Need one key to many values?** Use `groupingBy`.
- **Need one key to one value?** Use `toMap`, and add a merge function when duplicate keys are possible.
- **Need a CSV-style string?** Map to `String`, then use `joining`.
- **Parallel streams always faster?** No. Use only for large CPU-bound work with no shared mutable state.
