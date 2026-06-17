# equals / hashCode / Comparable / Comparator — Practice Exercises

---

## Topic 1 — `equals`

1. **`Person` class** — fields `String name` and `int age`. Override `equals` following the five-rule shape:
   - same reference shortcut
   - null-safe + type check
   - cast
   - compare fields

   Verify in `main`:
   - `p1.equals(p1)` → true (reflexive)
   - `p1.equals(p2)` → true when same name+age (symmetric)
   - `p1.equals(p3)` → false (different values)
   - `p1.equals(null)` → false (null-safe)
   - `p1.equals("Alice")` → false (type check)

---

## Topic 2 — `hashCode`

*Coming after Topic 1 discussion.*

---

## Topic 3 — `Comparable`

1. **`Person implements Comparable<Person>`** — sort by `age` ascending. Use `Integer.compare`, not subtraction.

   Verify in `main`:
   - `Collections.sort(list)` on a `List<Person>` in random order → sorted by age
   - `new TreeSet<>(list)` → also sorted by age

---

## Topic 4 — `Comparator`

All three in `main` of `EqualsHashCodePractice.java`:

1. Sort `people` by name using `Comparator.comparing`
2. Sort by age descending using `.reversed()`
3. Sort by age ascending then name as tiebreaker using `.thenComparing`
