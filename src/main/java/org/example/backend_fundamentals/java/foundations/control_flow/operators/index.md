---
order: 30
---

# Operators

## User understanding

Operators are essentially the same as C++.

Mentioned:

- +
- -
- =
- ==
- &&
- &
- Bitwise operators
- Logical operators

---

## Review

Correct.

Java operators are almost identical to C++.

Includes:

Arithmetic:
- +
- -
- *
- /
- %

Comparison:
- ==
- !=
- >
- <
- >=
- <=

Logical:
- &&
- ||
- !

Bitwise:
- &
- |
- ^
- ~

Assignment:
- =
- +=
- -=
- *=
- /=

Increment/Decrement:
- ++
- --

Ternary:
- ?:

---

## Important Interview Point

### && vs &

Example:

```java
if (a != null && a.isValid()) {

}
```

Short-circuit evaluation.

If first condition is false:

Second condition is never evaluated.

---

Example:

```java
if (a != null & a.isValid()) {

}
```

Both sides always execute.

Can cause:

NullPointerException

Example:

```java
Person p = null;

if (p != null && p.getAge() > 18)
```

Safe.

---

```java
Person p = null;

if (p != null & p.getAge() > 18)
```

Throws:

NullPointerException

---

Same applies to:

|| vs |

---

## == for primitives vs objects

Primitive:

```java
int a = 5;
int b = 5;
```

a == b

true

---

Objects:

```java
Integer a = new Integer(5);
Integer b = new Integer(5);
```

a == b

false

Reason:

Reference comparison.

Use:

equals()

for value comparison.

---

## Integer Division

Example:

```java
5 / 2
```

Result:

2

not

2.5

---

Need:

```java
5 / 2.0
```

or

```java
(double)5 / 2
```

---

## Final Revision

Know:

- && vs &
- || vs |
- == vs equals()
- Integer division truncates.

---
