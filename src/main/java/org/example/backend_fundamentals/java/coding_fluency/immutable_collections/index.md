---
order: 40
---

# Immutable Collections

## Why this matters
KYC workflows pass lookup tables (country codes, document types, risk tiers) across service boundaries. Returning `List.of(...)` instead of a mutable list prevents callers from corrupting shared state. Knowing the exact failure modes — and how they differ from `Arrays.asList` or `Collections.unmodifiableList` — keeps you out of subtle production bugs.

## Quick recall

**Q.** What exception does `List.of("a","b").add("c")` throw, and at what layer?
**A.** `UnsupportedOperationException`, thrown by the list implementation — not a compile error.

**Q.** Does `Map.of` allow null values?
**A.** No. Both null keys and null values throw `NullPointerException` at construction time.

**Q.** What's the difference between `List.copyOf(src)` and `Arrays.asList(arr)`?
**A.** `copyOf` takes an independent snapshot; `Arrays.asList` returns a fixed-size view backed by the original array.

**Q.** What is the difference between `List.of()` and `Collections.unmodifiableList()`?
**A.** `List.of()` is a true immutable snapshot. `Collections.unmodifiableList()` is a view — mutations to the backing list are visible through it.

**Q.** How many entries can `Map.of()` hold, and what do you use beyond that?
**A.** Up to 10 key-value pairs. For more, use `Map.ofEntries(Map.entry("k", v), ...)`.

**Q.** Why does `Set.of("a","a")` throw `IllegalArgumentException` while `new HashSet<>(Arrays.asList("a","a"))` does not?
**A.** `Set.of` validates uniqueness eagerly at construction and fails fast; `HashSet` silently deduplicates via `equals`/`hashCode`.

**Q.** Can you rely on `instanceof ArrayList` to detect a mutable list?
**A.** No. `List.of` returns internal implementation classes, not `ArrayList`. Check mutability by catching `UnsupportedOperationException` or by tracking the source of the list.
