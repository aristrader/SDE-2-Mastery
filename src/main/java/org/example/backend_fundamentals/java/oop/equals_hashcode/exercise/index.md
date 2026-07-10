---
order: 10
search: false
---

# Practice

## Exercise: default-equality - Default Equality

### Goal
Observe default reference-based equality.

### Task
Create a `Student` class with one field: `String id`.

Do not override anything.

Create:

```java
Student s1 = new Student("101");
Student s2 = new Student("101");
```

Print:

- `s1 == s2`
- `s1.equals(s2)`
- `s1.hashCode()`
- `s2.hashCode()`

### Checks
- Explain why `==` is false.
- Explain why default `equals()` is false.
- Explain why the two hash codes are identity-based.

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
Implement equality correctly using the identity field.

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

## Exercise: comparison-apis - Comparison APIs

### Goal
Choose the right equality API.

### Task
Experiment with:

- `==`
- `.equals()`
- `Objects.equals()`

Include:

- string literals
- two different `new String(...)` objects with same content
- `null` values

### Checks
- Explain when to use `==`.
- Explain when to use `.equals()`.
- Explain when `Objects.equals()` is safer.

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
