---
order: 10
search: false
---

# Practice

## Exercise: equals-override - Override equals

### Goal
Implement a correct, 5-rule `equals` method.

### Task
Create a `Person` class with fields `String name` and `int age`. Override `equals` following the five-rule shape:
1. same reference shortcut
2. null-safe + type check (instanceof)
3. cast
4. compare fields

### Checks
- Verify in `main`:
  - `p1.equals(p1)` → true (reflexive)
  - `p1.equals(p2)` → true when same name+age (symmetric)
  - `p1.equals(p3)` → false (different values)
  - `p1.equals(null)` → false (null-safe)
  - `p1.equals("Alice")` → false (type check)

## Exercise: hashcode-override - Override hashCode

### Goal
Implement `hashCode` matching the `equals` contract.

### Task
Add `hashCode` to your `Person` class using `Objects.hash(...)`. 

### Checks
- Verify that `p1.equals(p2)` implies `p1.hashCode() == p2.hashCode()`.

## Exercise: comparable-sort - Comparable sorting

### Goal
Implement natural ordering via `Comparable`.

### Task
Make `Person implements Comparable<Person>` — sort by `age` ascending. Use `Integer.compare`, not subtraction, to avoid underflow bugs.

### Checks
- `Collections.sort(list)` on a `List<Person>` in random order → sorted by age
- `new TreeSet<>(list)` → also sorted by age

## Exercise: comparator-sort - Comparator sorting

### Goal
Sort objects using external `Comparator`s.

### Task
In `main`, create a `List<Person> people`.
1. Sort `people` by name using `Comparator.comparing`
2. Sort by age descending using `.reversed()`
3. Sort by age ascending then name as tiebreaker using `.thenComparing`

### Checks
- Print the list after each sort to verify correct ordering.
