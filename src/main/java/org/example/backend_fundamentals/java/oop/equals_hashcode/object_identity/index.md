---
order: 10
---

# Object Identity

Every Java class ultimately extends `Object`, directly or indirectly. That gives every object default implementations of methods such as `equals`, `hashCode`, and `toString`.

## Reference identity

`==` checks whether two references point to the same object.

```java
Student a = new Student("101");
Student b = new Student("101");
Student same = a;

System.out.println(a == b);    // false
System.out.println(a == same); // true
```

Default `Object.equals()` also checks reference identity:

```java
public boolean equals(Object obj) {
    return this == obj;
}
```

That default is correct when object identity is the meaning. It is wrong for value-like objects where two different instances can represent the same domain value.

## `==` vs `equals`

| Check | Meaning |
| --- | --- |
| `a == b` | Same object reference |
| `a.equals(b)` | Logical equality as implemented by the class |
| `Objects.equals(a, b)` | Null-safe logical equality |

Use `==` for primitives, enum constants, singleton checks, and reference identity checks. Use `equals` for normal object value comparisons.

## Quick recall

- **Does every class explicitly write `extends Object`?** No, the compiler gives it implicitly when no other superclass is declared.
- **Does `==` call `equals()`?** No.
- **Is default `equals()` value-based?** No, it is reference-based.
