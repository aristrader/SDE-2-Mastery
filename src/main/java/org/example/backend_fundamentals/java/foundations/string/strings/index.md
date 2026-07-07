---
order: 30
---

# Strings

## User understanding

User explained:

- Strings are immutable.
- Immutability probably exists because of memory/safety reasons.
- String Pool exists inside heap.
- String literals are stored there.
- Multiple identical literals point to same object.
- `new String("ABC")` creates another object.
- Already understood `==` vs `.equals()`.

---

## Review

Most understanding was correct.

---

## Immutability

Example:

```java
String s = "Hello";

s.concat(" World");
```

Does NOT modify `s`.

Creates a new String.

---

## String Pool

Example:

```java
String a = "ABC";
String b = "ABC";
```

Both variables point to same pooled String object.

---

## new String()

Example:

```java
String a = "ABC";

String b = new String("ABC");
```

Creates another String object.

Therefore:

```java
a == b
```

false

---

`.equals()`

returns

true

because contents are identical.

---

# Why Strings are Immutable

Three reasons discussed.

---

## 1. String Pool

If pooled Strings could change:

Everyone sharing the pooled String would unexpectedly observe mutations.

Immutability makes pooling safe.

---

## 2. Security

Strings commonly represent:

- file paths
- URLs
- SQL
- class names
- configuration

Immutability prevents accidental modification after validation.

---

## 3. Thread Safety

Immutable objects can safely be shared across threads.

---
