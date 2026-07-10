---
order: 10
---

# List Basics

A `List` is ordered, allows duplicates, and supports index-based access.

## List options

| Type | Backed by | Allows null | Thread-safe | When to use |
| --- | --- | --- | --- | --- |
| `ArrayList` | Dynamic array | Yes | No | Default list. Fast `get(i)`, fast append, slow insert/remove near the front. |
| `LinkedList` | Doubly-linked list | Yes | No | Rarely the best list. Use only when you specifically need linked-list/deque behavior. |
| `List.of(...)` | Immutable array | No | Yes | Fixed read-only list. |
| `Arrays.asList(...)` | Fixed-size array view | Yes | No | `set` works; `add` and `remove` throw. |
| `Collections.unmodifiableList(...)` | Wrapper | Depends on backing list | Depends | Read-only view; backing list can still mutate. |
| `CopyOnWriteArrayList` | Copy-on-write array | Yes | Yes | Read-heavy listener/config lists. Every write copies the array. |

## Array vs ArrayList

- **Array:** fixed size, can hold primitives directly, covariant and runtime-checked.
- **`ArrayList<T>`:** dynamic size, objects only, generic and compile-time checked.

```java
Object[] arr = new String[3];
arr[0] = 42;                 // compiles, ArrayStoreException at runtime

List<String> list = new ArrayList<>();
// List<Object> objects = list; // does not compile
```

Compile-time errors beat runtime errors. That is the main reason `List<T>` is safer than mutable object arrays in normal Java code.

## Basic operations

```java
List<String> names = new ArrayList<>();

names.add("A");
names.add("B");
names.add("C");

names.get(0);
names.set(1, "X");
names.remove(0);
names.remove("C");
names.contains("X");
names.size();
names.isEmpty();
```

## `remove(index)` vs `remove(value)`

For `List<Integer>`, this overload is a real trap:

```java
List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));

nums.remove(1);                  // removes index 1 => 20
nums.remove(Integer.valueOf(10)); // removes value 10
```

Use `Integer.valueOf(x)` when you mean "remove this integer value".

## Sorting, reversing, and copying

```java
List<Integer> nums = new ArrayList<>(List.of(3, 1, 2));

Collections.sort(nums);     // [1, 2, 3]
Collections.reverse(nums);  // [3, 2, 1]
```

When the input list should stay unchanged, copy first:

```java
List<Integer> sorted = new ArrayList<>(input);
Collections.sort(sorted);
```

Custom object sorting is covered in `../sorting/`.

## `subList`

```java
List<Integer> slice = list.subList(1, 3);
```

The start index is inclusive and the end index is exclusive. `subList` returns a view backed by the original list, so make a defensive copy when returning it:

```java
return new ArrayList<>(list.subList(from, to));
```

## Demo code

| Demo | Shows |
| --- | --- |
| `../playground/basics/ArrayListBasicsRun` | Basic `ArrayList` usage. |
| `../playground/basics/LinkedListBasicsRun` | Basic `LinkedList` usage. |

## Quick recall

- **Default growable list?** `ArrayList`.
- **Why is `LinkedList` rarely best?** Poor random access and cache locality; `ArrayDeque` is usually better for queue/deque work.
- **Remove integer by value?** `list.remove(Integer.valueOf(x))`.
- **Return a safe sublist?** `new ArrayList<>(list.subList(from, to))`.
- **Sort without mutating input?** Copy first, then sort the copy.
