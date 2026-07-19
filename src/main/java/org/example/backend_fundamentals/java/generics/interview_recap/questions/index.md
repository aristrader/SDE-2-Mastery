---
title: Interview Questions
order: 20
search: false
---

# Generics Interview Questions

## Question 1: Why does this not compile?

```java
List<Integer> integers = new ArrayList<>();
List<Number> numbers = integers;
```

Answer shape: Java blocks this because generics are invariant. If it were allowed, `numbers.add(3.14)` could insert a `Double` into a `List<Integer>`.

## Question 2: Design a method that sums numbers from `List<Integer>`, `List<Long>`, and `List<Double>`.

Answer shape: use `List<? extends Number>` and read each value as `Number`. Do not add to that list.

Related full practice: [wildcards exercise](../../wildcards/exercise/).

## Question 3: Why can `List<String>` and `List<Integer>` look the same at runtime?

Answer shape: type erasure removes most generic type arguments after compilation. The compiler inserts checks/casts; runtime cannot reliably distinguish `List<String>` from `List<Integer>`.
