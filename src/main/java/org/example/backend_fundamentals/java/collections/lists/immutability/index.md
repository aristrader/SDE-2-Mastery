---
order: 20
---

# List Immutability

Java has several list-looking APIs with different mutation rules. The trap is assuming they all behave like `ArrayList`.

## Three common forms

| API | Can `set`? | Can `add/remove`? | Nulls? | Backing can change? |
| --- | --- | --- | --- | --- |
| `Arrays.asList(...)` | Yes | No | Yes | Yes, backed by the array |
| `List.of(...)` | No | No | No | No |
| `Collections.unmodifiableList(list)` | No through wrapper | No through wrapper | Depends | Yes, if original list mutates |

## `Arrays.asList` fixed-size trap

```java
List<String> list = Arrays.asList("A", "B", "C");

list.set(0, "X"); // works
list.add("D");    // UnsupportedOperationException
```

It is a fixed-size view over an array. Element replacement works; resizing does not.

## `List.of` is fully unmodifiable

```java
List<String> list = List.of("A", "B", "C");

list.set(0, "X"); // UnsupportedOperationException
list.add("D");    // UnsupportedOperationException
List.of("A", null); // NullPointerException
```

## Unmodifiable view is not an immutable copy

```java
List<String> backing = new ArrayList<>(List.of("A"));
List<String> view = Collections.unmodifiableList(backing);

backing.add("B");
System.out.println(view); // [A, B]
```

Use `List.copyOf(backing)` when you need a snapshot.

## Demo code

| Demo | Shows |
| --- | --- |
| `../playground/immutability/ArraysAsListTrapRun` | `set` works; `add` throws. |
| `../playground/immutability/ListOfImmutabilityRun` | `List.of` rejects mutation and nulls. |
| `../playground/immutability/UnmodifiableListViewRun` | Unmodifiable view reflects backing-list changes. |

## Quick recall

- **Fixed-size but `set` works?** `Arrays.asList`.
- **Fully read-only and null-rejecting?** `List.of`.
- **Read-only wrapper over mutable backing list?** `Collections.unmodifiableList`.
- **Need immutable snapshot?** `List.copyOf`.
