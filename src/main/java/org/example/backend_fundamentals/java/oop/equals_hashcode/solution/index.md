---
order: 20
search: false
---

# Solutions

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
