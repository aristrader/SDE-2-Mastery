---
order: 40
---

# Java Collections

## User understanding

User initially said:

- Arrays are similar to C++.
- Arrays are for primitive types.
- For objects, Java uses `ArrayList` and similar collections.

---

## Correction

### Misconception

Arrays are only for primitive types.

### Correction

Java arrays can store BOTH:

- Primitive types
- Objects

Examples:

Primitive array:

```java
int[] nums = {1, 2, 3};
```

Object array:

```java
String[] names = {"Alice", "Bob"};
```

or

```java
Person[] people = new Person[10];
```

Arrays store:

- Primitive values directly.
- References for objects.

---

## Array vs ArrayList

Important distinction.

### Arrays

- Fixed size.
- Built into the language.
- Can store primitives.
- Can store object references.

Example:

```java
int[] arr = new int[10];
```

---

### ArrayList

- Dynamic size.
- Part of Java Collections Framework.
- Stores objects only.

Example:

```java
ArrayList<Integer> list = new ArrayList<>();
```

---

### Generics Reminder

This is why:

```java
ArrayList<int>
```

is invalid.

Need:

```java
ArrayList<Integer>
```

because generics work only with objects.

---

## Array Length

Interview reminder:

Correct:

```java
arr.length
```

Incorrect:

```java
arr.length()
```

---

## Default Values

Primitive arrays:

```java
int[] arr = new int[3];
```

Contents:

0

0

0

---

Object arrays:

```java
String[] arr = new String[3];
```

Contents:

null

null

null

---

## Final Revision

Know:

- Arrays can store primitives and objects.
- Arrays are fixed size.
- ArrayList is dynamic.
- Arrays use `.length`.
- ArrayList uses `.size()` (implicitly discussed through comparison).

---
