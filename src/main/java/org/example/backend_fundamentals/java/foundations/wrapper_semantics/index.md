---
order: 20
---

# Wrapper Object Semantics

`int` is a value; `Integer` is an object reference that can be `null` and has identity. That difference matters at generic API boundaries and whenever Java silently converts between the two.

## Where wrappers change the contract

Use a wrapper when absence is meaningful or an API requires an object: `List<Integer>` is valid, while `List<int>` is not. Use a primitive when every valid state has a value and the code performs ordinary arithmetic.

```java
Integer configuredLimit = request.getLimit(); // null means “not supplied”
int retryCount = 0;                           // zero is a valid value, never absent
```

The important decision is the null policy at the boundary. Translate absence to a default only when the business contract defines one; otherwise reject it. Do not carry an unexplained nullable wrapper into arithmetic-heavy code.

## Conversion mechanics and the null failure

Autoboxing converts a primitive to its matching wrapper; unboxing converts the wrapper back to a primitive. The compiler inserts the equivalent conversion calls.

```java
Integer boxed = 10; // Integer.valueOf(10)
int value = boxed;  // boxed.intValue()
```

Unboxing is not null-safe. A missing value can fail far from the repository or DTO that produced it.

```java
Integer count = map.get("pending");
int next = count + 1; // unboxes count; throws NullPointerException when count is null
```

Make the policy explicit before the arithmetic:

```java
Integer count = map.get("pending");
int next = (count == null ? 0 : count) + 1; // only if zero is the agreed default
```

## Equality and the `Integer` cache

`==` has different meanings depending on its operands:

| Operands | What `==` compares | Safe value comparison |
| --- | --- | --- |
| `int` and `int` | numeric values | `==` |
| `Integer` and `Integer` | object identity | `Objects.equals(left, right)` when either can be `null`; otherwise `left.equals(right)` |
| `Integer` and `int` | the `Integer` is unboxed, then numeric values | `==`, after proving the wrapper is non-null |

The cache makes the second row deceptive:

```java
Integer smallLeft = 100;
Integer smallRight = 100;
System.out.println(smallLeft == smallRight); // true: same cached object

Integer largeLeft = 1_000;
Integer largeRight = 1_000;
System.out.println(largeLeft == largeRight); // do not rely on this result
System.out.println(largeLeft.equals(largeRight)); // true: same numeric value
```

The language guarantees identity for two boxing conversions of qualifying constant-expression integral values from `-128` to `127`. `Integer.valueOf` must cache that range and may cache more. Neither fact makes `==` a wrapper-value comparison; it only explains why an identity bug can appear to pass.

Avoid `new Integer(...)`: its constructors are deprecated for removal. Let boxing or `Integer.valueOf` create a wrapper when one is actually needed.

## Interview answer

"Wrappers let primitives participate in object-only APIs and represent absence, but they add identity and nullability. Boxing and unboxing are compiler-inserted conversions; unboxing `null` throws `NullPointerException`. I compare two wrappers by value with `equals` or `Objects.equals`, never by `==`; the small `Integer` cache only makes an identity comparison look correct."

Try the cache and null-unboxing traps in `playground/WrapperSemanticsExercise.java`.

## Further reading

- [JLS: boxing conversion](https://docs.oracle.com/javase/specs/jls/se24/html/jls-5.html#jls-5.1.7)
- [JLS: unboxing conversion](https://docs.oracle.com/javase/specs/jls/se24/html/jls-5.html#jls-5.1.8)
- [Integer API: `valueOf`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/Integer.html#valueOf(int))
- [Interview reference: autoboxing traps](https://www.skillveris.com/interview-questions/java/what-is-autoboxing)
- [Interview reference: boxing and unboxing pitfalls](https://prepforge.net/java/autoboxing-unboxing-pitfalls)

## Quick recall

**Q. Why use `Integer` instead of `int`?**

A. Use it for object-only APIs such as generics or when `null` is a meaningful contract state; otherwise prefer `int` for required numeric values.

**Q. What does `int total = boxed;` do?**

A. It unboxes by calling the matching value method, such as `boxed.intValue()` for `Integer`; a `null` reference throws `NullPointerException`.

**Q. How should two nullable `Integer` values be compared?**

A. `Objects.equals(left, right)`. It compares values and safely handles two `null` references.

**Q. Why can `Integer a = 100; Integer b = 100; a == b` be true?**

A. Boxing qualifying constant-expression values from `-128` to `127` has an identity guarantee. `==` still compares references, not numeric values.

**Q. Is `left == 5` safe when `left` is an `Integer`?**

A. It compares numeric values after unboxing, but throws `NullPointerException` if `left` is `null`.
