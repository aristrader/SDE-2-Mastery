---
order: 20
search: false
---

# Solutions

## Solution: integer-cache-trap - The Cache Trap

```java
Integer a = 50;
Integer b = 50;
System.out.println(a == b); // Prints true (Cached)

Integer x = 500;
Integer y = 500;
System.out.println(x == y); // Implementation detail outside the guaranteed cache range

System.out.println(x.equals(y)); // Prints true (Safe content comparison)
```
Java guarantees identity when boxing qualifying constant-expression `int` values from -128 to 127. Outside that range, identity is not specified for value comparison, even if one JDK happens to reuse an object. `==` checks identity, not content; always use `.equals()` for wrapper values.

## Solution: unboxing-npe - Hidden NullPointerException

```java
Map<String, Integer> map = new HashMap<>();

// int count = map.get("missing_key"); // Throws NullPointerException
```
Because the key is missing, `map.get()` returns `null` (an absent `Integer` object). 
The compiler sees you assigning an `Integer` to an `int`, so it unboxes by quietly inserting a call to `.intValue()`.
At runtime, this becomes `null.intValue()`, causing an immediate `NullPointerException`. Never assign a potentially null wrapper directly to a primitive without checking!
