---
order: 20
search: false
---

# Generic Basics Solutions

## Solution: generic-box - Generic Box

```java
static final class Box<T> {
    private T value;

    void set(T value) {
        this.value = value;
    }

    T get() {
        return value;
    }
}
```

`Box<String>` accepts only strings, `Box<Integer>` accepts only integers, and `Box<Student>` accepts only students.

## Solution: generic-pair - Generic Pair

```java
static final class Pair<K, V> {
    private final K key;
    private final V value;

    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    K key() {
        return key;
    }

    V value() {
        return value;
    }
}
```

Example:

```java
Pair<Student, Marks> studentMarks = new Pair<>(new Student("Asha"), new Marks(92));
Pair<Employee, Department> employeeDepartment = new Pair<>(new Employee("Dev"), new Department("Payments"));
Pair<Integer, String> idName = new Pair<>(1, "one");
```

## Solution: generic-pair-stack - Generic containers

```java
static final class Pair<A, B> {
    private final A first;
    private final B second;

    Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    A getFirst() {
        return first;
    }

    B getSecond() {
        return second;
    }

    Pair<B, A> swap() {
        return new Pair<>(second, first);
    }
}

static final class SimpleStack<T> {
    private final List<T> values = new ArrayList<>();

    void push(T value) {
        values.add(value);
    }

    T pop() {
        return values.remove(values.size() - 1);
    }

    T peek() {
        return values.get(values.size() - 1);
    }

    boolean isEmpty() {
        return values.isEmpty();
    }
}
```

Use `List<String> names = new ArrayList<>();`, not raw `List names`. Then `names.add(42)` fails at compile time instead of failing later with a cast problem.

## Solution: generic-print-method - Generic Method

```java
public static <T> void print(T value) {
    System.out.println(value);
}
```

The class does not need to be generic just because one method is generic.
