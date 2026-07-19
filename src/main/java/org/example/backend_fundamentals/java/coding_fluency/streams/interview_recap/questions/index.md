---
title: Interview Questions
order: 20
search: false
---

# Streams Interview Questions

## Question 1: Convert employees into department-wise active employee names.

Answer shape: `filter(Employee::active)`, then `groupingBy(Employee::department, mapping(Employee::name, toList()))`.

Related full practice: [stream interview drills](../../interview_drills/exercise/).

## Question 2: Find the bug in `employees.parallelStream().forEach(list::add)`.

Answer shape: it mutates shared non-thread-safe state from multiple threads. Collect into a result instead.

## Question 3: Why does `Collectors.toMap(Employee::id, identity())` fail sometimes?

Answer shape: duplicate keys throw unless a merge function is supplied. Decide whether to keep first, keep last, combine, or reject explicitly.
