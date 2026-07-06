---
order: 40
---

# Immutable Collections — Coding Exercises

## Why this matters
KYC workflows pass lookup tables (country codes, document types, risk tiers) across service boundaries. Returning `List.of(...)` instead of a mutable list prevents callers from corrupting shared state. Knowing the exact failure modes — and how they differ from `Arrays.asList` or `Collections.unmodifiableList` — keeps you out of subtle production bugs.

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

## Exercise 1: List.of mutation (~10 min)

**Goal:** Build the muscle memory for which operations throw and which don't.

**Task:**
1. Create `List<String> types = List.of("PASSPORT", "NATIONAL_ID", "DRIVING_LICENSE")`.
2. In separate try-catch blocks, attempt: `add("VISA")`, `remove(0)`, `set(0, "VISA")`. Print which exception is thrown each time.
3. Now create `List<String> mutable = new ArrayList<>(types)` and repeat the same three operations — they must all succeed.
4. Print both lists to confirm the original is untouched.
5. Create `List<String> backing = new ArrayList<>(List.of("PASSPORT", "NATIONAL_ID"))`. Then create `List<String> unmod = Collections.unmodifiableList(backing)`. Add `"DRIVING_LICENSE"` to `backing` and print `unmod` — the new element is visible. Key difference from `List.of`: `Collections.unmodifiableList` is a view, not a snapshot.

**Gotcha:** `List.of` does NOT return `java.util.ArrayList` — it returns an internal class (`List12`, `ListN`, etc.). Never write `instanceof ArrayList` against a `List.of` result. `List.of` also disallows `null` elements — `List.of(null)` throws `NullPointerException` at construction time; `Collections.unmodifiableList` has no such restriction.

---

## Exercise 2: Map.of null key (~5 min)

**Goal:** Internalize that `Map.of` and `HashMap` have different null contracts.

**Task:**
1. Wrap `Map.of(null, "PENDING")` in a try-catch. Print the exception class name and message.
2. Create a `HashMap<String, String>`, call `put(null, "PENDING")`, and print the value back via `get(null)` — confirm it works.
3. Also try `Map.of("ID_CHECK", null)` — what happens? (It's the same exception family.)
4. Write a one-line comment in your code stating the rule: "Map.of forbids null keys **and** null values by spec."

**Gotcha:** The NPE is thrown at construction time, not at first access. This matters in static initializers or lazy-init code where the failure site can be surprising.

---

## Exercise 3: Set.of duplicate (~5 min)

**Goal:** Understand that `Set.of` validates uniqueness eagerly; `HashSet` does not.

**Task:**
1. Wrap `Set.of("PASSPORT", "PASSPORT")` in a try-catch. Print the exception.
2. Create `new HashSet<>(Arrays.asList("PASSPORT", "PASSPORT"))`. Print the set — confirm it has one element.
3. Now try `Set.of("PASSPORT", "passport")` (different case). Does it throw? Print the result.

**Gotcha:** Case sensitivity matters — `Set.of` uses `equals()`, so `"PASSPORT"` and `"passport"` are different elements and won't throw. Real-world trap: normalise strings before building a `Set.of` config table.

`Set.of` has no guaranteed iteration order — insertion order is not preserved. For predictable order, use `new LinkedHashSet<>()`.

---

## Exercise 4: copyOf independence (~10 min)

**Goal:** Confirm that `List.copyOf` / `Set.copyOf` take a snapshot — they are not views.

**Task:**
1. Create `List<String> original = new ArrayList<>(List.of("PASSPORT", "NATIONAL_ID"))`.
2. Create `List<String> snapshot = List.copyOf(original)`.
3. Add `"DRIVING_LICENSE"` to `original`.
4. Print `original.size()` and `snapshot.size()` — they must differ.
5. Now model the `DocumentTypeConfig` class from the domain model above (or inline the pattern): store the constructor argument via `List.copyOf` and show that external mutation of the passed-in list after construction doesn't affect the stored copy.

**Gotcha:** `Arrays.asList(arr)` IS backed by the array — mutating the array changes the list. `List.copyOf` is NOT. Know which factory gives a view and which gives a snapshot.

---

## Exercise 5: Sorted iteration (~10 min)

**Goal:** Experience first-hand that `Map.of` gives no iteration-order guarantee.

**Task:**
1. Create `Map<String, Integer> riskTiers = Map.of("LOW", 1, "MEDIUM", 2, "HIGH", 3, "CRITICAL", 4)`.
2. Iterate with `riskTiers.entrySet().forEach(e -> System.out.println(e.getKey() + "=" + e.getValue()))`. Run it three times (put the loop inside a `for (int run = 0; run < 3; run++)` block) and observe the order.
3. Create `Map<String, Integer> ordered = new LinkedHashMap<>()` and insert the same entries in LOW → CRITICAL order. Iterate and confirm insertion order is preserved.
4. Create `Map<String, Integer> sorted = new TreeMap<>(riskTiers)`. Iterate and confirm alphabetical order.

**Gotcha:** `Map.of` with ≤10 entries uses a compact internal class. Iteration order can look stable in small tests but is not guaranteed — it depends on hash values and internal layout. Never sort by iterating a `Map.of`.

`Map.of` accepts up to 10 key-value pairs. For 11+, use `Map.ofEntries(Map.entry("K1", v1), Map.entry("K2", v2), ...)`. Try 11 entries with `Map.of` to see the compile error.

`Map.copyOf(existingMap)` creates an independent immutable snapshot, like `List.copyOf`. Add a step: create a `HashMap`, populate it, create `Map.copyOf(map)`, mutate the original, and confirm the copy is unaffected.

---

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

