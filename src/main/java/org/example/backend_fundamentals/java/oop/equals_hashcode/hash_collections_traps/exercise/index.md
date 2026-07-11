---
order: 10
search: false
---

# Hash Collection Traps Practice

## Exercise: equals-only-breaks-hash-collections - Override equals Only

### Goal
See why overriding only `equals()` breaks hash-based collections.

### Task
Create a `Student` class with `String id`.

Override only `equals()` using `id`.

Do not override `hashCode()`.

Use logically equal objects in:

- `HashSet<Student>`
- `HashMap<Student, String>`

### Checks
- Add `new Student("101")` to a set, then test `contains(new Student("101"))`.
- Put `new Student("101")` in a map, then test `get(new Student("101"))`.
- Explain why lookup can fail.

## Exercise: hashcode-only-breaks-equality - Override hashCode Only

### Goal
See why overriding only `hashCode()` is also incomplete.

### Task
Create a `Student` class with `String id`.

Override only `hashCode()` using `id`.

Keep default `equals()`.

Test with:

- `HashSet<Student>`
- `HashMap<Student, String>`

### Checks
- Add or put two different `Student("101")` objects.
- Explain why same bucket is not enough.
- Explain why default `equals()` still treats them as different objects.

## Exercise: correct-equals-hashcode - Correct equals and hashCode

### Goal
Implement equality correctly using one stable identity field.

### Task
Create a `Student` class with:

- `String id`
- `String name`
- `int age`

Use only `id` inside `equals()` and `hashCode()`.

### Checks
- `new Student("101", "A", 20).equals(new Student("101", "B", 30))` is true.
- `HashSet` detects duplicates by `id`.
- `HashMap` lookup works with a new logically equal key.

## Exercise: mutable-key-breaks-lookup - Mutable Key

### Goal
Observe why mutable identity fields are unsafe in hash-based collections.

### Task
Create a mutable key class where `id` is used in `equals()` and `hashCode()`.

Insert an object into a `HashMap`, then mutate its `id`.

### Checks
- Test `map.get(key)` after mutation.
- Test `map.containsKey(key)` after mutation.
- Explain why the object is still present but lookup searches the wrong bucket.
