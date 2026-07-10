---
order: 20
---

# Arrays

Arrays are built into the Java language. They are fixed-size containers and can store either primitive values or object references.

## Primitive and object arrays

Primitive array:

```java
int[] nums = {1, 2, 3};
```

Object array:

```java
String[] names = {"Alice", "Bob"};
Person[] people = new Person[10];
```

Primitive arrays store values directly. Object arrays store references; each empty slot defaults to `null`.

## Array vs ArrayList

| Feature | Array | ArrayList |
| --- | --- | --- |
| Size | Fixed | Dynamic |
| Language/library | Built into Java | Collections Framework |
| Primitive support | Yes: `int[]` | No: use `ArrayList<Integer>` |
| Length API | `arr.length` | `list.size()` |

This is why `ArrayList<int>` is invalid. Java generics work with object types, so use `ArrayList<Integer>`.

## Default values

```java
int[] ints = new int[3];       // [0, 0, 0]
String[] strings = new String[3]; // [null, null, null]
```

Local array variables still must be initialized before use; array elements get defaults after the array object is created.

## Quick recall

**Q. Can Java arrays store objects?**
A. Yes. They store object references.

**Q. Are arrays fixed size?**
A. Yes. Use `ArrayList` when the size should grow or shrink.

**Q. Why is `ArrayList<int>` invalid?**
A. Generics require object types, so use `ArrayList<Integer>`.

**Q. Array length syntax?**
A. `arr.length`, not `arr.length()`.
