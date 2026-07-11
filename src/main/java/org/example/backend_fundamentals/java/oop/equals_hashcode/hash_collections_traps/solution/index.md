---
order: 20
search: false
---

# Hash Collection Traps Solutions

## Solution: equals-only-breaks-hash-collections - Override equals Only

```java
final class Student {
    private final String id;

    Student(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Student student && Objects.equals(id, student.id);
    }
}

Set<Student> students = new HashSet<>();
students.add(new Student("101"));
System.out.println(students.contains(new Student("101"))); // often false
```

The two objects are equal by `equals()`, but still have different identity-based hashes. Hash collections search the wrong bucket, so `equals()` may never run.

## Solution: hashcode-only-breaks-equality - Override hashCode Only

```java
final class Student {
    private final String id;

    Student(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

Set<Student> students = new HashSet<>();
students.add(new Student("101"));
students.add(new Student("101"));
System.out.println(students.size()); // 2
```

Both objects land in the same bucket, but default `equals()` still uses identity. Same bucket is not enough; `equals()` must also say they match.

## Solution: correct-equals-hashcode - Correct equals and hashCode

```java
final class Student {
    private final String id;
    private final String name;
    private final int age;

    Student(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Student student)) return false;
        return Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

Student s1 = new Student("101", "A", 20);
Student s2 = new Student("101", "B", 30);

System.out.println(s1.equals(s2)); // true

Set<Student> set = new HashSet<>();
set.add(s1);
set.add(s2);
System.out.println(set.size()); // 1

Map<Student, String> map = new HashMap<>();
map.put(s1, "present");
System.out.println(map.get(s2)); // present
```

Only `id` defines business identity, so only `id` belongs in both methods.

## Solution: mutable-key-breaks-lookup - Mutable Key

```java
final class StudentKey {
    private String id;

    StudentKey(String id) {
        this.id = id;
    }

    void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentKey key && Objects.equals(id, key.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

StudentKey key = new StudentKey("101");
Map<StudentKey, String> map = new HashMap<>();
map.put(key, "Asha");

key.setId("202");

System.out.println(map.get(key));         // usually null
System.out.println(map.containsKey(key)); // usually false
```

The map stored the entry in the bucket for `"101"`. After mutation, lookup computes the bucket for `"202"`. The entry still exists, but normal lookup searches the wrong place.
