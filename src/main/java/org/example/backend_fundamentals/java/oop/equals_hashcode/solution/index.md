---
order: 20
search: false
---

# Solutions

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

Map<Student, String> map = new HashMap<>();
map.put(new Student("101"), "Asha");
System.out.println(map.get(new Student("101"))); // often null
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

The map stored the entry in the bucket for `"101"`. After mutation, lookup computes the bucket for `"202"`. The entry still exists, but lookup searches the wrong place.

## Solution: comparison-apis - Comparison APIs

```java
String literalA = "java";
String literalB = "java";
String heapA = new String("java");
String heapB = new String("java");
String missing = null;

System.out.println(literalA == literalB);          // true, same interned object
System.out.println(heapA == heapB);                // false, different objects
System.out.println(heapA.equals(heapB));           // true, same content
System.out.println(Objects.equals(missing, heapA));// false, null-safe
System.out.println(Objects.equals(missing, null)); // true
```

Use `==` for primitives or intentional identity checks. Use `.equals()` for non-null object value equality. Use `Objects.equals()` when either side may be null.

## Solution: equals-override - Override equals

```java
public class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // 1. identity
        if (!(o instanceof Person)) return false; // 2. type check + null safe
        Person person = (Person) o; // 3. cast
        // 4. field comparisons
        return age == person.age && Objects.equals(name, person.name);
    }
}
```

## Solution: hashcode-override - Override hashCode

```java
    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
```

## Solution: comparable-sort - Comparable sorting

```java
public class Person implements Comparable<Person> {
    // ... fields and constructors ...

    @Override
    public int compareTo(Person other) {
        // Correct: prevents underflow vs (this.age - other.age)
        return Integer.compare(this.age, other.age); 
    }
}
```

## Solution: comparator-sort - Comparator sorting

```java
public class Main {
    public static void main(String[] args) {
        List<Person> people = new ArrayList<>(List.of(
            new Person("Charlie", 30),
            new Person("Alice", 25),
            new Person("Bob", 30)
        ));

        // 1. Sort by name
        people.sort(Comparator.comparing(Person::getName));
        
        // 2. Sort by age descending
        people.sort(Comparator.comparingInt(Person::getAge).reversed());
        
        // 3. Sort by age ascending, then name
        people.sort(
            Comparator.comparingInt(Person::getAge)
                      .thenComparing(Person::getName)
        );
    }
}
```
