---
title: Interview Recap
order: 110
---

# Streams Interview Recap

Use this after the streams pages for quick interview recall.

| API | One-line recall |
| --- | --- |
| `filter` | Keep matching elements. |
| `map` | Convert one element to one value. |
| `flatMap` | Flatten one-to-many output. |
| `sorted` | Sort before `limit` when selecting top records. |
| `distinct` | Deduplicate using `equals()` and `hashCode()`. |
| `limit` | Keep first `n` elements after earlier operations. |
| `skip` | Drop first `n` elements; combine with `limit` for pagination. |
| `peek` | Debug/log only; avoid business mutation. |
| `collect` | Materialize into a collection or aggregate result. |
| `toMap` | Add a merge function when duplicate keys are possible. |
| `groupingBy` | Build one-key-to-many-values grouping. |
| `partitioningBy` | Split into true/false groups. |

## Quick recall

- **Main model?** Build a lazy pipeline, run it with a terminal operation.
- **No terminal operation?** Nothing runs.
- **Nested list to flat list?** `flatMap`.
- **Most common bug?** Reusing a consumed stream.
- **Most common collector trap?** Duplicate keys in `toMap`.
