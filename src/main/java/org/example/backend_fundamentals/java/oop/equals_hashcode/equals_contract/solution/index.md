---
order: 20
search: false
---

# equals Contract Solutions

## Solution: equals-override - Override equals

```java
final class Person {
    private final String name;
    private final int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Person person)) return false;
        return age == person.age && Objects.equals(name, person.name);
    }
}
```

The same fields used for logical equality must later be used by `hashCode()`.
