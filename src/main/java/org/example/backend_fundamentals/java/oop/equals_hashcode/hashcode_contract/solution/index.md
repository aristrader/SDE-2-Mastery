---
order: 20
search: false
---

# hashCode Contract Solutions

## Solution: hashcode-override - Override hashCode

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

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```

Equal objects must have equal hash codes. Different objects may still collide.
