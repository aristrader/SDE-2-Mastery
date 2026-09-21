---
order: 30
---

# Operators

## Operators that cause production bugs

### Numeric operations

`+`, `-`, `*`, `/`, and `%` operate on numeric operands. Integer division truncates toward zero, and `%` keeps the dividend's sign:

```java
int pages = 5 / 2;       // 2
int remainder = -5 % 2; // -1
```

Convert deliberately before division when a fractional result matters: `double average = (double) total / count;`.

### Equality and ordering

`==`, `!=`, `>`, `<`, `>=`, and `<=` compare primitives as values. For object references, `==` asks whether both references point to the same object; use `equals` for value equality.

```java
boolean sameStatus = expectedStatus.equals(actualStatus); // expectedStatus is known non-null
```

Putting the known non-null value on the left avoids a null dereference. For nullable values, use `Objects.equals(expectedStatus, actualStatus)`.

### Short-circuit guards

`&&`, `||`, and `!` are boolean operators. `&&` skips its right operand when the left is false; `||` skips it when the left is true. This is both a safety rule and observable behavior when the right operand has work or side effects.

```java
if (person != null && person.getAge() >= 18) {
    approve(person);
}
```

`&` and `|` are bitwise operators for integral values. With boolean operands they are legal but always evaluate both sides, so they are not substitutes for guards. Use them only when evaluating both boolean checks is intentional.

Other integral bitwise operators are `^`, `~`, `<<`, `>>`, and `>>>`; reserve them for explicit bit-field or binary operations.

### Assignment and conditional expressions

Compound assignment includes an implicit narrowing conversion:

```java
byte count = 1;
count += 2;          // compiles
// count = count + 2; // does not compile: right side is int
```

The conditional operator returns one of two values: `condition ? whenTrue : whenFalse`. Use it for a simple value choice, not a multi-step branch or side effects.

---

## Quick recall

**Q. What is short-circuit evaluation?**
A. `&&` skips the right side when the left is false; `||` skips it when the left is true. Guard null before dereferencing it.

**Q. `&` versus `&&` with booleans?**
A. `&` evaluates both operands; `&&` may skip the right operand. Use `&&` for normal control-flow guards.

**Q. `==` versus `equals` for objects?**
A. `==` compares identity; `equals` compares value according to the type's contract. Use `Objects.equals` when either value can be null.

**Q. Why does `byte b = 1; b = b + 1;` fail to compile?**
A. Any arithmetic operation involving types smaller than `int` (like `byte` or `short`) promotes them to `int` before the calculation. The result is an `int`, which cannot be directly assigned back to a `byte` without a manual cast.

**Q. Integer `5 / 2`?**
A. `2`: integer division truncates toward zero. Cast before division if the fraction matters.
