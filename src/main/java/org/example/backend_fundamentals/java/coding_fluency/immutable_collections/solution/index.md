---
order: 20
search: false
---

# Immutable Collections Solutions

## Solution: list-of-mutation - List.of mutation
```java
List<String> types = List.of("PASSPORT", "NATIONAL_ID", "DRIVING_LICENSE");
// All mutation attempts throw UnsupportedOperationException
try { types.add("VISA"); } catch (UnsupportedOperationException e) { System.out.println("add throws UOE"); }
```
`Collections.unmodifiableList` reflects changes to the backing array, whereas `List.of` is completely isolated.

## Solution: map-of-null - Map.of null key
```java
try { Map.of(null, "PENDING"); } catch (NullPointerException e) { System.out.println("NPE on null key"); }
try { Map.of("ID_CHECK", null); } catch (NullPointerException e) { System.out.println("NPE on null value"); }
// Rule: Map.of forbids null keys and null values by spec.
```

## Solution: set-of-duplicate - Set.of duplicate
```java
try { Set.of("PASSPORT", "PASSPORT"); } catch (IllegalArgumentException e) { System.out.println("IAE on duplicate"); }
Set<String> set = Set.of("PASSPORT", "passport"); // Works! Case-sensitive.
```

## Solution: copyof-independence - copyOf independence
```java
List<String> original = new ArrayList<>(List.of("PASSPORT", "NATIONAL_ID"));
List<String> snapshot = List.copyOf(original);
original.add("DRIVING_LICENSE");
System.out.println(original.size() != snapshot.size()); // true
```

## Solution: sorted-iteration - Sorted iteration
```java
Map<String, Integer> riskTiers = Map.of("LOW", 1, "MEDIUM", 2, "HIGH", 3, "CRITICAL", 4);
// Order is random and not guaranteed.
Map<String, Integer> ordered = new LinkedHashMap<>(); // Maintains insertion order
Map<String, Integer> sorted = new TreeMap<>(riskTiers); // Maintains alphabetical order
```
