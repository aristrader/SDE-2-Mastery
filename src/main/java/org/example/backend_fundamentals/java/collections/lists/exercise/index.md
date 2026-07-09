---
order: 10
search: false
---

# Practice

## Exercise: arrays-aslist-trap - The Fixed-Size Trap

### Goal
Understand that `Arrays.asList()` returns a list that is mutable in content but fixed in size.

### Task
Create a list using `List<String> list = Arrays.asList("A", "B", "C");`.
Attempt to modify an existing element: `list.set(0, "X");`.
Attempt to add a new element: `list.add("D");`.

### Checks
- Which operation succeeds?
- Which operation throws an exception, and what is the exception?

## Exercise: list-of-immutability - Fully Immutable Lists

### Goal
Experience the strict immutability provided by the Java 9 `List.of()` factory methods.

### Task
Create a list using `List<String> list = List.of("A", "B", "C");`.
Attempt to modify an existing element: `list.set(0, "X");`.
Attempt to add a `null` element: `List.of("A", null);`.

### Checks
- Do any mutation operations succeed on a `List.of` instance?
- What happens when you try to pass `null` to the factory method?


## Exercise: basic-arraylist-operations - Basic ArrayList Operations

### Objective

Practice basic `ArrayList` creation and operations.

### Problem

Create a method that takes a list of integers and performs basic list operations.

### Requirements

* Create an `ArrayList<Integer>`.
* Add at least 5 integers.
* Return:

    * first element
    * last element
    * size of the list
    * whether the list contains a given target

### Constraints

* Do not use streams.
* Use `List<Integer>` as the reference type.
* Use `ArrayList<Integer>` as the implementation.

### Concepts Tested

* `List`
* `ArrayList`
* `add`
* `get`
* `size`
* `contains`

---

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

### Requirements

* Use `ArrayList<String>`.
* Preserve operation order exactly.
* Handle lists with at least 3 elements.

### Constraints

* Do not create a new list for every operation.
* Mutate the existing list.

### Concepts Tested

* `add(index, value)`
* `set`
* `remove(index)`
* `remove(value)`

---

## Exercise: integer-remove-gotcha - Integer Remove Gotcha

### Objective

Understand `remove(index)` vs `remove(value)` for `List<Integer>`.

### Problem

Given a list of integers and a value, remove the first occurrence of that value.

### Requirements

* Use `List<Integer>`.
* Remove by value, not by index.
* Return the modified list.

### Constraints

* Do not use streams.
* Do not create a new list.
* Must work correctly when the value is also a valid index.

### Concepts Tested

* `remove(Integer.valueOf(x))`
* overloaded methods
* wrapper class behavior

---

## Exercise: iterate-three-ways - Iterate Three Ways

### Objective

Practice different list iteration styles.

### Problem

Given a list of integers, return the sum of all elements using three separate methods:

1. Index-based `for` loop
2. Enhanced `for` loop
3. `Iterator`

### Requirements

* Implement three methods.
* All three methods should return the same sum.

### Constraints

* Do not use streams.
* Do not use recursion.

### Concepts Tested

* index loop
* enhanced for loop
* `Iterator`

---

## Exercise: safe-removal-using-iterator - Safe Removal Using Iterator

### Objective

Practice removing elements safely while iterating.

### Problem

Given a list of integers, remove all even numbers from the list.

### Requirements

* Use `Iterator`.
* Mutate the original list.
* Return the modified list.

### Constraints

* Do not create a new list.
* Do not use `removeIf`.
* Do not use streams.

### Concepts Tested

* `Iterator`
* `hasNext`
* `next`
* `Iterator.remove`
* safe mutation during iteration

---

## Exercise: sort-and-reverse - Sort and Reverse

### Objective

Practice sorting and reversing lists.

### Problem

Given a list of integers:

1. Sort it in ascending order.
2. Create another list in descending order.
3. Return both lists.

### Requirements

* Use `Collections.sort`.
* Use `Collections.reverse`.
* Do not manually sort.

### Constraints

* Do not use streams.
* Do not mutate the original input list directly.
* Return both ascending and descending versions.

### Concepts Tested

* `Collections.sort`
* `Collections.reverse`
* copying lists

---

## Exercise: sublist-practice - subList Practice

### Objective

Understand how `subList()` works.

### Problem

Given a list of strings and two indices `from` and `to`, return a sublist from `from` inclusive to `to` exclusive.

### Requirements

* Use `subList(from, to)`.
* Return a new independent list copied from the sublist.

### Constraints

* Do not return the raw `subList` view directly.
* Assume indices are valid.

### Concepts Tested

* `subList`
* inclusive/exclusive ranges
* defensive copy

---

## Exercise: merge-two-lists - Merge Two Lists

### Objective

Practice combining lists.

### Problem

Given two lists of integers, return a new list containing all elements of the first list followed by all elements of the second list.

### Requirements

* Use `ArrayList`.
* Do not modify the input lists.
* Preserve order.

### Constraints

* Do not use streams.
* Do not sort.

### Concepts Tested

* `addAll`
* copying lists
* preserving order

---

## Exercise: remove-duplicates-while-preserving-order - Remove Duplicates While Preserving Order

### Objective

Practice list traversal and duplicate handling.

### Problem

Given a list of integers, return a new list with duplicates removed while preserving first occurrence order.

### Requirements

* Preserve original order.
* Keep only the first occurrence of each value.

### Constraints

* Do not use streams.
* Do not sort.
* You may use another collection if needed.

### Concepts Tested

* list traversal
* duplicate detection
* order preservation

---

## Exercise: rotate-list-right - Rotate List Right

### Objective

Practice index-based list manipulation.

### Problem

Given a list of integers and an integer `k`, rotate the list to the right by `k` positions.

### Example

Input:

```text
list = [1, 2, 3, 4, 5]
k = 2
```

Output:

```text
[4, 5, 1, 2, 3]
```

### Requirements

* Handle `k > list.size()`.
* Return a new list.
* Do not modify the original list.

### Constraints

* Do not use streams.
* Do not use recursion.

### Concepts Tested

* index calculation
* `subList`
* `addAll`
* list copying

---

## Exercise: arraylist-vs-linkedlist-experiment - ArrayList vs LinkedList Experiment

### Objective

Build intuition for `ArrayList` vs `LinkedList`.

### Problem

Write a small program that performs these operations on both `ArrayList` and `LinkedList`:

1. Add 10,000 integers.
2. Access elements by index.
3. Add elements at the beginning.
4. Remove elements from the beginning.

### Requirements

* Print time taken for each operation.
* Use `System.nanoTime()` or `System.currentTimeMillis()`.
* Compare the result briefly in comments.

### Constraints

* This is an experiment, not a benchmark.
* Do not over-optimize.
* Keep the code simple.

### Concepts Tested

* `ArrayList`
* `LinkedList`
* performance intuition
* practical trade-offs

---

## Exercise: mini-problem-student-names - Mini Problem: Student Names

### Objective

Use list operations in a realistic small problem.

### Problem

Given a list of student names:

1. Remove blank names.
2. Trim extra spaces.
3. Sort names alphabetically.
4. Return the cleaned list.

### Requirements

* Use `ArrayList`.
* Use `Iterator` for removing blank names.
* Use `Collections.sort`.

### Constraints

* Do not use streams.
* Do not mutate the original input list.
* Blank means empty string after trimming.

### Concepts Tested

* copying lists
* trimming strings
* iterator removal
* sorting