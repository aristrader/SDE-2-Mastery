---
order: 50
---

# System.identityHashCode

`System.identityHashCode(obj)` returns an identity-based hash code, ignoring any overridden `hashCode()` method.

```java
Employee a = new Employee("101");
Employee b = new Employee("101");

System.out.println(a.hashCode());                  // value-based if overridden
System.out.println(System.identityHashCode(a));    // identity-based
System.out.println(System.identityHashCode(b));    // usually different
```

Use it rarely, mostly for debugging identity problems.

## IdentityHashMap

`IdentityHashMap` uses `==` instead of `equals`. It is not a normal replacement for `HashMap`.

Use cases are rare: object graph traversal, proxy tracking, serialization internals, or identity-sensitive caches.

## Quick recall

- **Does `identityHashCode` call overridden `hashCode()`?** No.
- **Does identity hash prove memory address?** No, treat it as an identity-style hash only.
- **Should normal domain maps use `IdentityHashMap`?** No.
