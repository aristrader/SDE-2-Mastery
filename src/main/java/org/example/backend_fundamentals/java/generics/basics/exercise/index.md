---
order: 10
search: false
---

# Generic Basics Practice

Write these from scratch in a runnable class. The point is to practice the generic syntax, not fill blanks in a mostly completed skeleton.

## Exercise: generic-box - Generic Box

### Goal
Create a generic class and verify type safety at the call site.

### Task
Implement `Box<T>` with `set(T value)` and `get()`.

Use it with `Box<String>`, `Box<Integer>`, and `Box<Student>`.

### Checks
- You write the `Box<T>` declaration yourself.
- You decide where the field, setter, and getter belong.
- No casts are needed when reading values.
- Wrong-type `set` calls fail at compile time.

## Exercise: generic-pair - Generic Pair

### Goal
Practice multiple type parameters.

### Task
Create `Pair<K, V>` and print pairs for:

- `Student -> Marks`
- `Employee -> Department`
- `Integer -> String`

### Checks
- You write the `Pair<K, V>` declaration yourself.
- Each pair keeps its key and value types separate.
- Printing does not require casts.

## Exercise: generic-pair-stack - Generic containers

### Goal
Build small generic types and see how type parameters remove casts at the call site.

### Task
Implement:

- `Pair<A, B>` with `getFirst()`, `getSecond()`, and `swap()` returning `Pair<B, A>`.
- `SimpleStack<T>` backed by `ArrayList` with `push`, `pop`, `peek`, and `isEmpty`.
- A raw-list bug fix: replace a raw `List` with `List<String>` so bad inserts fail at compile time.

### Checks
- `Pair<String, Integer>("age", 30).swap()` becomes `Pair<Integer, String>`.
- Stack pops in LIFO order.
- `names.add(42)` fails when `names` is `List<String>`.

## Exercise: generic-print-method - Generic Method

### Goal
Write a generic method without making the whole class generic.

### Task
Implement:

```java
public static <T> void print(T value)
```

Call it with `Integer`, `Double`, `String`, and `Student`.

### Checks
- You write the containing runnable class yourself.
- The `<T>` appears before the return type.
- The method accepts all four types without overloads.
