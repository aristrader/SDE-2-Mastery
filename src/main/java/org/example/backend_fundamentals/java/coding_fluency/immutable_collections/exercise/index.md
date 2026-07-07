---
order: 10
search: false
---

# Immutable Collections Practice

## Domain model

```java
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

## Exercise: list-of-mutation - List.of mutation

### Goal
Build the muscle memory for which operations throw and which don't.

### Task
1. Create `List<String> types = List.of("PASSPORT", "NATIONAL_ID", "DRIVING_LICENSE")`.
2. In separate try-catch blocks, attempt: `add("VISA")`, `remove(0)`, `set(0, "VISA")`. Print which exception is thrown each time.
3. Now create `List<String> mutable = new ArrayList<>(types)` and repeat the same three operations — they must all succeed.
4. Print both lists to confirm the original is untouched.
5. Create `List<String> backing = new ArrayList<>(List.of("PASSPORT", "NATIONAL_ID"))`. Then create `List<String> unmod = Collections.unmodifiableList(backing)`. Add `"DRIVING_LICENSE"` to `backing` and print `unmod` — the new element is visible. Key difference from `List.of`: `Collections.unmodifiableList` is a view, not a snapshot.

## Exercise: map-of-null - Map.of null key

### Goal
Internalize that `Map.of` and `HashMap` have different null contracts.

### Task
1. Wrap `Map.of(null, "PENDING")` in a try-catch. Print the exception class name and message.
2. Create a `HashMap<String, String>`, call `put(null, "PENDING")`, and print the value back via `get(null)` — confirm it works.
3. Also try `Map.of("ID_CHECK", null)` — what happens? (It's the same exception family.)
4. Write a one-line comment in your code stating the rule: "Map.of forbids null keys **and** null values by spec."

## Exercise: set-of-duplicate - Set.of duplicate

### Goal
Understand that `Set.of` validates uniqueness eagerly; `HashSet` does not.

### Task
1. Wrap `Set.of("PASSPORT", "PASSPORT")` in a try-catch. Print the exception.
2. Create `new HashSet<>(Arrays.asList("PASSPORT", "PASSPORT"))`. Print the set — confirm it has one element.
3. Now try `Set.of("PASSPORT", "passport")` (different case). Does it throw? Print the result.

## Exercise: copyof-independence - copyOf independence

### Goal
Confirm that `List.copyOf` / `Set.copyOf` take a snapshot — they are not views.

### Task
1. Create `List<String> original = new ArrayList<>(List.of("PASSPORT", "NATIONAL_ID"))`.
2. Create `List<String> snapshot = List.copyOf(original)`.
3. Add `"DRIVING_LICENSE"` to `original`.
4. Print `original.size()` and `snapshot.size()` — they must differ.
5. Now model the `DocumentTypeConfig` class from the domain model above (or inline the pattern): store the constructor argument via `List.copyOf` and show that external mutation of the passed-in list after construction doesn't affect the stored copy.

## Exercise: sorted-iteration - Sorted iteration

### Goal
Experience first-hand that `Map.of` gives no iteration-order guarantee.

### Task
1. Create `Map<String, Integer> riskTiers = Map.of("LOW", 1, "MEDIUM", 2, "HIGH", 3, "CRITICAL", 4)`.
2. Iterate with `riskTiers.entrySet().forEach(e -> System.out.println(e.getKey() + "=" + e.getValue()))`. Run it three times; for a clearer check, put the loop inside `for (int run = 0; run < 3; run++)` and observe the order.
3. Create `Map<String, Integer> ordered = new LinkedHashMap<>()` and insert the same entries in LOW → CRITICAL order. Iterate and confirm insertion order is preserved.
4. Create `Map<String, Integer> sorted = new TreeMap<>(riskTiers)`. Iterate and confirm alphabetical order.

### Gotcha
`Map.of` and `Set.of` iteration order is unspecified. Use `LinkedHashMap` / `LinkedHashSet` for insertion order and `TreeMap` / `TreeSet` for sorted order.
