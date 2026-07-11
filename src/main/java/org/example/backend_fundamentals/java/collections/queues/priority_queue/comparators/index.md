---
order: 20
---

# PriorityQueue Comparators

Use a comparator when natural order is not the priority rule.

## Quick recall

- **One field priority?** `Comparator.comparing...`.
- **Descending one field?** Apply `reversed()` to that comparator before tie-breakers.
- **Stable total rule?** Add `thenComparing...` tie-breakers.
