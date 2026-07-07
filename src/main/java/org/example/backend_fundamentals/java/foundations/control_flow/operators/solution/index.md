---
order: 20
search: false
---

# Solutions

## Solution: short-circuit-trap - Short-Circuit vs Bitwise

```java
String str = null;

// Case 1: Short-circuit AND
if (str != null && str.length() > 0) {
    System.out.println("Valid string");
}

// Case 2: Bitwise AND
// if (str != null & str.length() > 0) {
//     System.out.println("Valid string");
// }
```

In **Case 1**, the program runs smoothly and does nothing. `str != null` evaluates to `false`. Because `&&` short-circuits, the JVM immediately skips the right side of the condition. `str.length()` is never called.

In **Case 2**, the program crashes with a `NullPointerException`. The single `&` is a bitwise/logical operator that **does not short-circuit**. It forcefully evaluates both the left side and the right side before combining them. It attempts to call `.length()` on the null reference, causing a crash. Always use `&&` and `||` for control flow!
