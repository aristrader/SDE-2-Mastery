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


## Domain model

```java
// A snapshot of document types supported for a given country.
// Imagine this comes back from a config service and is shared read-only.
public class DocumentTypeConfig {
    private final String countryCode;
    private final List<String> supportedTypes; // should be immutable

    public DocumentTypeConfig(String countryCode, List<String> supportedTypes) {
        this.countryCode = countryCode;
        this.supportedTypes = List.copyOf(supportedTypes); // defensive copy
    }

    public List<String> getSupportedTypes() { return supportedTypes; }
    public String getCountryCode()          { return countryCode; }
}
```

---


## Common Gotchas

- Work through the practice prompts with compiler errors and runtime output visible; each one is proving a specific collection factory rule.
- `List.of` does NOT return `java.util.ArrayList` — it returns an internal class (`List12`, `ListN`, etc.). Never write `instanceof ArrayList` against a `List.of` result. `List.of` also disallows `null` elements — `List.of(null)` throws `NullPointerException` at construction time; `Collections.unmodifiableList` has no such restriction.
- The NPE is thrown at construction time, not at first access. This matters in static initializers or lazy-init code where the failure site can be surprising.
- Case sensitivity matters — `Set.of` uses `equals()`, so `"PASSPORT"` and `"passport"` are different elements and won't throw. Real-world trap: normalise strings before building a `Set.of` config table.
- `Arrays.asList(arr)` IS backed by the array — mutating the array changes the list. `List.copyOf` is NOT. Know which factory gives a view and which gives a snapshot.
- `Set.of` has no guaranteed iteration order. If order matters, use `new LinkedHashSet<>()` or another ordered collection explicitly.
- `Map.of` with ≤10 entries uses a compact internal class. Iteration order can look stable in small tests but is not guaranteed — it depends on hash values and internal layout. Never sort by iterating a `Map.of`.
- `Map.of` accepts up to 10 key-value pairs. For 11 or more, use `Map.ofEntries(Map.entry("K1", v1), Map.entry("K2", v2), ...)`.
- `Map.copyOf(existingMap)` creates an independent immutable snapshot. Mutating the original map after `copyOf` does not mutate the copy.
