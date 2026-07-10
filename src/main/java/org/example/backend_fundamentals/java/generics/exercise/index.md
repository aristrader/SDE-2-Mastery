---
order: 10
search: false
---

# Generics Practice

Use the question list in the practice workspace. Each exercise has a focused goal, starter code, and matching solution.

## Exercise: generic-pair-stack - Generic containers

### Goal
Build small generic types and see how type parameters remove casts at the call site.

### Task
Implement:

- `Pair<A, B>` with `getFirst()`, `getSecond()`, and `swap()` returning `Pair<B, A>`.
- `SimpleStack<T>` backed by `ArrayList` with `push`, `pop`, `peek`, and `isEmpty`.
- A raw-list bug fix: replace a raw `List` with `List<String>` so bad inserts fail at compile time.

### Starter code

```java
import java.util.ArrayList;
import java.util.List;

public class GenericPairStackPractice {
    static final class Pair<A, B> {
        private final A first;
        private final B second;

        Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        A getFirst() {
            return null;
        }

        B getSecond() {
            return null;
        }

        Pair<B, A> swap() {
            return null;
        }
    }

    static final class SimpleStack<T> {
        private final List<T> values = new ArrayList<>();

        void push(T value) {
        }

        T pop() {
            return null;
        }

        T peek() {
            return null;
        }

        boolean isEmpty() {
            return true;
        }
    }

    public static void main(String[] args) {
        Pair<String, Integer> pair = new Pair<>("age", 30);
        Pair<Integer, String> swapped = pair.swap();
        System.out.println(pair.getFirst() + "=" + pair.getSecond());
        System.out.println(swapped.getFirst() + "=" + swapped.getSecond());

        SimpleStack<String> stack = new SimpleStack<>();
        stack.push("first");
        stack.push("second");
        System.out.println(stack.peek());
        System.out.println(stack.pop());
        System.out.println(stack.pop());

        List<String> names = new ArrayList<>();
        names.add("Alice");
        names.add("Bob");
        // names.add(42); // should be a compile error
        for (String name : names) {
            System.out.println(name.toUpperCase());
        }
    }
}
```

### Checks
- Output starts with `age=30` and `30=age`.
- Stack pops in LIFO order: `second`, then `first`.
- `names.add(42)` fails at compile time when uncommented.

## Exercise: bounded-max-sum - Bounds with extends

### Goal
Use bounds to unlock methods on `T`: `compareTo` for max and `doubleValue` for sums.

### Task
Implement:

- `findMax(List<T>)` where `T` can be compared.
- `sumList(List<T>)` where `T` is a `Number`.
- Fix the common bug where someone writes `item > max` for generic objects.

### Starter code

```java
import java.util.List;

public class BoundedMaxSumPractice {
    static <T extends Comparable<T>> T findMax(List<T> values) {
        return null;
    }

    static <T extends Number> double sumList(List<T> values) {
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(findMax(List.of(3, 10, 2)));
        System.out.println(sumList(List.of(1, 2, 3)));

        // Why is this invalid for generic T?
        // if (item > max) { ... }
    }
}
```

### Checks
- Max prints `10`.
- Sum prints `6.0`.
- Your explanation names both issues: `>` works on primitives, and unbounded `T` has no comparison contract.

## Exercise: wildcard-producer - Upper-bounded wildcards

### Goal
Practice `? extends` for producer inputs: read values safely, but do not add values.

### Task
Implement:

- `printNumbers(List<? extends Number>)`
- `sumList(List<? extends Number>)`

Then try to add an `Integer` inside `printNumbers` and explain the compiler error.

### Starter code

```java
import java.util.List;

public class WildcardProducerPractice {
    static void printNumbers(List<? extends Number> values) {
    }

    static double sumList(List<? extends Number> values) {
        return 0;
    }

    public static void main(String[] args) {
        printNumbers(List.of(1, 2, 3));
        printNumbers(List.of(1.5, 2.5));
        System.out.println(sumList(List.of(1, 2, 3)));
        System.out.println(sumList(List.of(1.5, 2.5)));
    }
}
```

### Checks
- Both `List<Integer>` and `List<Double>` compile.
- The sums print `6.0` and `4.0`.
- You can explain why `values.add(1)` is unsafe for `List<? extends Number>`.

## Exercise: wildcard-consumer - Lower-bounded wildcards

### Goal
Practice `? super` for consumer inputs: add integers safely, but read back only as `Object`.

### Task
Implement:

- `addNumbers(List<? super Integer>)` adding `1` through `5`.
- `fill(List<? super Integer>, int value, int count)`.

Call both with `List<Integer>`, `List<Number>`, and `List<Object>`. Try `List<String>` and confirm it does not compile.

### Starter code

```java
import java.util.ArrayList;
import java.util.List;

public class WildcardConsumerPractice {
    static void addNumbers(List<? super Integer> values) {
    }

    static void fill(List<? super Integer> values, int value, int count) {
    }

    public static void main(String[] args) {
        List<Integer> integers = new ArrayList<>();
        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        addNumbers(integers);
        addNumbers(numbers);
        fill(objects, 9, 3);

        System.out.println(integers);
        System.out.println(numbers);
        System.out.println(objects);

        Object first = objects.get(0);
        System.out.println(first.getClass().getSimpleName());
    }
}
```

### Checks
- `integers` and `numbers` contain `[1, 2, 3, 4, 5]`.
- `objects` contains `[9, 9, 9]`.
- Reading from `List<? super Integer>` is treated as `Object`, not `Integer`.

## Exercise: erasure-traps - Type erasure traps

### Goal
See what generic type information survives at runtime and what gets erased.

### Task
Demonstrate three erasure rules:

- `list instanceof List<Integer>` is illegal; raw `List` is the runtime check.
- `process(List<Integer>)` and `process(List<String>)` cannot be overloaded together.
- `List<Integer>` and `List<String>` have the same runtime class.

### Starter code

```java
import java.util.ArrayList;
import java.util.List;

public class ErasureTrapsPractice {
    static void processIntegers(List<Integer> values) {
        System.out.println("integers=" + values);
    }

    static void processStrings(List<String> values) {
        System.out.println("strings=" + values);
    }

    public static void main(String[] args) {
        Object unknown = List.of(1, 2, 3);

        if (unknown instanceof List) {
            System.out.println("It is a List at runtime");
        }

        List<Integer> ints = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        System.out.println(ints.getClass() == strings.getClass());

        processIntegers(List.of(1, 2));
        processStrings(List.of("a", "b"));
    }
}
```

### Checks
- Runtime class comparison prints `true`.
- You can explain why overloaded `process(List<Integer>)` and `process(List<String>)` have the same erased signature.
