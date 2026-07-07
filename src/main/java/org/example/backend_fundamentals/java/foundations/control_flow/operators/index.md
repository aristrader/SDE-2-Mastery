---
order: 30
---

# Operators

Java operators perform operations on one, two, or three operands. They are fundamental to manipulating primitive values and object references.

---

## Types of Operators

### 1. Arithmetic Operators
`+`, `-`, `*`, `/`, `%`
Be careful with integer division (`/`): `5 / 2` evaluates to `2` (the decimal part is truncated).
The modulo operator (`%`) returns the remainder: `5 % 2` evaluates to `1`.

### 2. Relational Operators
`==`, `!=`, `>`, `<`, `>=`, `<=`
Always use `==` for primitives. **Never use `==` to compare object values** (like Strings or wrapper classes), as it compares reference identity, not content equality.

### 3. Logical Operators (Short-Circuit)
`&&` (AND), `||` (OR), `!` (NOT)
These operators **short-circuit**.
- In `A && B`, if `A` is false, `B` is never evaluated because the entire expression must be false.
- In `A || B`, if `A` is true, `B` is never evaluated because the entire expression must be true.

This is critical for safety checks:
```java
if (person != null && person.getAge() >= 18) { ... }
```
If `person` is null, the right side is not evaluated, preventing a `NullPointerException`.

### 4. Bitwise Operators
`&` (AND), `|` (OR), `^` (XOR), `~` (Complement), `<<` (Left shift), `>>` (Signed right shift), `>>>` (Unsigned right shift).
These operate on individual bits. Unlike `&&` and `||`, the bitwise `&` and `|` **do not short-circuit** when used with booleans.

### 5. Assignment Operators
`=`, `+=`, `-=`, `*=`, `/=`, `%=`
Compound assignments contain an implicit cast. `byte b = 1; b += 2;` compiles, whereas `b = b + 2;` does not (because `b + 2` is evaluated as an `int`).

### 6. Ternary Operator
`condition ? trueValue : falseValue`
A shorthand for if-else that returns a value.

---

## Quick recall

**Q. What is short-circuit evaluation?**
A. When evaluating `&&` or `||`, if the result is determined by the left operand, the right operand is completely skipped. This prevents unnecessary work and runtime exceptions.

**Q. Why does `byte b = 1; b = b + 1;` fail to compile?**
A. Any arithmetic operation involving types smaller than `int` (like `byte` or `short`) promotes them to `int` before the calculation. The result is an `int`, which cannot be directly assigned back to a `byte` without a manual cast.
