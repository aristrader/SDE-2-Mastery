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
System.out.println(x == y); // Prints false (Not Cached)

System.out.println(x.equals(y)); // Prints true (Safe content comparison)
```
Java optimizes memory by keeping a cache of `Integer` objects from -128 to 127. When you autobox `50`, it grabs the pre-existing object from the cache for both `a` and `b`. For `500`, it creates a `new Integer(500)` every time. `==` checks identity, not content. Always use `.equals()`.

## Solution: unboxing-npe - Hidden NullPointerException

```java
Map<String, Integer> map = new HashMap<>();

// int count = map.get("missing_key"); // Throws NullPointerException
```
Because the key is missing, `map.get()` returns `null` (an absent `Integer` object). 
The compiler sees you assigning an `Integer` to an `int`, so it autoboxes by quietly inserting a call to `.intValue()`.
At runtime, this becomes `null.intValue()`, causing an immediate `NullPointerException`. Never assign a potentially null wrapper directly to a primitive without checking!
