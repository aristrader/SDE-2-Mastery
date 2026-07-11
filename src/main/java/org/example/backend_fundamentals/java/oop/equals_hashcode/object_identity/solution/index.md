---
order: 20
search: false
---

# Object Identity Solutions

## Solution: default-equality - Default Equality

```java
final class Student {
    private final String id;

    Student(String id) {
        this.id = id;
    }
}

Student s1 = new Student("101");
Student s2 = new Student("101");

System.out.println(s1 == s2);        // false
System.out.println(s1.equals(s2));   // false
System.out.println(s1.hashCode());   // identity-based
System.out.println(s2.hashCode());   // identity-based
```

`s1` and `s2` are different objects. Default `equals()` behaves like `this == other`, so equal-looking state does not matter.

## Solution: comparison-apis - Comparison APIs

```java
String literalA = "java";
String literalB = "java";
String heapA = new String("java");
String heapB = new String("java");
String missing = null;

System.out.println(literalA == literalB);           // true, same interned object
System.out.println(heapA == heapB);                 // false, different objects
System.out.println(heapA.equals(heapB));            // true, same content
System.out.println(Objects.equals(missing, heapA)); // false, null-safe
System.out.println(Objects.equals(missing, null));  // true
```

Use `==` for primitives or intentional identity checks. Use `.equals()` for non-null object value equality. Use `Objects.equals()` when either side may be `null`.
