---
order: 20
search: false
---

# Solutions

## Solution: integer-cache-trap - The Cache Trap

```java
Integer a = 50;
Integer b = 50;
System.out.println(a == b); // true: identity happens to be shared

Integer x = 500;
Integer y = 500;
System.out.println(x == y); // do not rely on this result

System.out.println(x.equals(y)); // Prints true (Safe content comparison)
```
Java guarantees identity for two boxing conversions of qualifying constant-expression `int` values from -128 to 127. Outside that range, identity is not a value-comparison rule, even if one JDK happens to reuse an object. `==` checks identity here; use `.equals()` for known non-null wrappers or `Objects.equals(left, right)` when either value may be `null`.

## Solution: unboxing-npe - Hidden NullPointerException

```java
Map<String, Integer> map = new HashMap<>();

Integer storedCount = map.get("missing_key");
// int count = storedCount; // throws NullPointerException
int count = storedCount == null ? 0 : storedCount; // only when zero is the agreed default
```
Because the key is missing, `map.get()` returns `null` (an absent `Integer` object). The compiler sees an assignment from `Integer` to `int`, so it inserts `storedCount.intValue()`. Calling that method on `null` causes an immediate `NullPointerException`.

Do not silently choose `0` for every missing value. Use a default only when the contract defines one; otherwise validate and reject the missing input before unboxing.
