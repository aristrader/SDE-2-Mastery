---
order: 10
---

# String Pool

The pool makes equal literals share a canonical reference. It is why a reference comparison can look correct in a tiny example—and why it is the wrong production comparison.

```java
String a = "hello";
String b = "hello";
System.out.println(a == b);      // true: same interned literal
System.out.println(a.equals(b)); // true: same text
```

## Predict identity versus value

| Construction | `a == value` | `a.equals(value)` | Why |
| --- | --- | --- | --- |
| `"hello"` | `true` | `true` | equal literal is interned |
| `"he" + "llo"` | `true` | `true` | compile-time constant expression is interned |
| `new String("hello")` | `false` | `true` | explicitly creates a distinct object |
| `prefix + "llo"` where `prefix` is a variable | do not rely on it | `true` when text matches | run-time concatenation is a separate result |

```java
String a = "hello";
String c = new String("hello");
String prefix = "he";
String runtime = prefix + "llo";

System.out.println(a == c);      // false
System.out.println(a.equals(c)); // true
System.out.println(a == runtime); // false: do not use this as value comparison
```

## `intern()` is identity canonicalization, not normal comparison

`runtime.intern()` returns the pool's canonical reference for that text. It can make `a == runtime.intern()` true, but calling it just to compare strings hides intent and adds global-pool work. Use `.equals()` for values. Consider interning only after measuring a workload that repeatedly holds many duplicate, long-lived strings; otherwise let normal allocation and garbage collection do their jobs.

Do not build correctness around the pool's physical memory location or lifetime policy. The language guarantee that matters here is literal/constant-expression interning, not a heap diagram.

## Null-safe comparison

```java
if ("ACTIVE".equals(status)) {
    // safe even when status is null
}
```

`status.equals("ACTIVE")` throws when `status` is null. If both values can be null, use `Objects.equals(left, right)`.

## Quick recall

- **What does `==` compare?** References, never String content.
- **Which expressions are automatically interned?** Literals and string-valued constant expressions.
- **What does `intern()` return?** The canonical pooled reference equal to its receiver.
- **Default comparison?** `.equals()`, or `Objects.equals()` when both sides may be null.
