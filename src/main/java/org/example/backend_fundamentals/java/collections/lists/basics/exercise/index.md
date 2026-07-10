---
order: 10
search: false
---

# Practice

## Exercise: basic-arraylist-operations - Basic ArrayList Operations

### Objective
Practice basic `ArrayList` creation and operations.

### Problem
Create a method that takes a list of integers and performs basic list operations.

### Requirements
- Create an `ArrayList<Integer>`.
- Add at least 5 integers.
- Return first element, last element, size, and whether the list contains a target.

### Constraints
- Do not use streams.
- Use `List<Integer>` as the reference type.
- Use `ArrayList<Integer>` as the implementation.

## Exercise: insert-update-and-remove - Insert, Update, and Remove

### Objective
Practice modifying a list.

### Problem
Given a list of strings, perform these operations in order:

1. Add one element at the end.
2. Add one element at index `1`.
3. Replace the element at index `2`.
4. Remove one element by index.
5. Remove one element by value.
6. Return the final list.

### Constraints
- Mutate a working list.
- Preserve operation order exactly.
- Handle lists with at least 3 elements.

## Exercise: integer-remove-gotcha - Integer Remove Gotcha

### Objective
Understand `remove(index)` vs `remove(value)` for `List<Integer>`.

### Problem
Given a list of integers and a value, remove the first occurrence of that value.

### Constraints
- Remove by value, not by index.
- Must work correctly when the value is also a valid index.
- Do not use streams.

## Exercise: sort-and-reverse - Sort and Reverse

### Objective
Practice sorting and reversing lists.

### Problem
Given a list of integers:

1. Create an ascending version.
2. Create a descending version.
3. Return both lists.

### Constraints
- Use `Collections.sort` and `Collections.reverse`.
- Do not mutate the original input list.
- Do not use streams.

## Exercise: sublist-practice - subList Practice

### Objective
Understand how `subList()` works.

### Problem
Given a list of strings and two indices `from` and `to`, return a sublist from `from` inclusive to `to` exclusive.

### Constraints
- Use `subList(from, to)`.
- Return a new independent list copied from the sublist.
- Assume indices are valid.

## Exercise: merge-two-lists - Merge Two Lists

### Objective
Practice combining lists.

### Problem
Given two lists of integers, return a new list containing all elements of the first list followed by all elements of the second list.

### Constraints
- Do not modify the input lists.
- Preserve order.
- Do not sort.

## Exercise: remove-duplicates-while-preserving-order - Remove Duplicates While Preserving Order

### Objective
Practice list traversal and duplicate handling.

### Problem
Given a list of integers, return a new list with duplicates removed while preserving first occurrence order.

### Constraints
- Keep only the first occurrence of each value.
- Do not sort.
- Do not use streams.

## Exercise: rotate-list-right - Rotate List Right

### Objective
Practice index-based list manipulation.

### Problem
Given a list of integers and an integer `k`, rotate the list to the right by `k` positions.

```text
list = [1, 2, 3, 4, 5]
k = 2
result = [4, 5, 1, 2, 3]
```

### Constraints
- Handle `k > list.size()`.
- Return a new list.
- Do not mutate the original list.

## Exercise: mini-problem-student-names - Mini Problem: Student Names

### Objective
Use list operations in a realistic small problem.

### Problem
Given a list of student names:

1. Remove blank names.
2. Trim extra spaces.
3. Sort names alphabetically.
4. Return the cleaned list.

### Constraints
- Do not mutate the original input list.
- Blank means empty string after trimming.
- Do not use streams.
