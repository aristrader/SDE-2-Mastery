---
order: 10
search: false
---

# Practice

## Exercise: set-of-duplicates - Silent Dedupe vs Fail Fast

### Goal
Observe the difference in how `HashSet` and `Set.of()` handle duplicate inputs.

### Task
Create a `HashSet` using a list with duplicates: `new HashSet<>(List.of("A", "A", "B"));`. Print its size.
Create an immutable set using the factory method with duplicates: `Set.of("A", "A", "B");`.

### Checks
- What is the size of the `HashSet`? Did it warn you about the duplicate?
- What exception is thrown by `Set.of`? Why is throwing an exception often better for literal collections?


## Exercise: basic-hashset-operations - Basic HashSet Operations

### Objective

Become comfortable with HashSet.

### Problem

Create a HashSet.

Perform:

* add
* remove
* contains
* size
* isEmpty

---

## Exercise: understanding-add - Understanding add()

### Objective

Understand the boolean returned by `add()`.

### Problem

Insert duplicate values.

Print the return value of every `add()` call.

Explain the output.

---

## Exercise: remove-duplicates - Remove Duplicates

### Objective

Convert a List into unique values.

### Problem

Given a List of integers containing duplicates,

return only unique elements using a HashSet.

---

## Exercise: preserve-order - Preserve Order

### Objective

Learn LinkedHashSet.

### Problem

Remove duplicates while preserving insertion order.

---

## Exercise: linked-hash-set - Preserving Insertion Order

### Goal
See how `LinkedHashSet` solves the unpredictable iteration order of a regular `HashSet`.

### Task
Create a `HashSet<Integer>`. Add the numbers `10`, `1`, `5`, and `20`. Print the set.
Create a `LinkedHashSet<Integer>`. Add the same numbers in the same order. Print the set.

### Checks
- Which set prints the elements exactly in the order you inserted them (`[10, 1, 5, 20]`)?
- Which set prints them in a seemingly random bucket order?

---

## Exercise: sorting - Sorting

### Objective

Learn TreeSet.

### Problem

Insert random integers into a TreeSet.

Print the final order.

---

## Exercise: null-handling - Null Handling

### Objective

Understand null behavior.

### Problem

Experiment with:

* HashSet
* LinkedHashSet
* TreeSet

Observe which accept null and which don't.

Write one-line explanation.

---

## Exercise: union - Union

### Objective

Practice basic Set operations.

### Problem

Given two sets,

return their union.

---

## Exercise: intersection - Intersection

### Objective

Practice common interview operation.

### Problem

Return common elements between two sets.

---

## Exercise: difference - Difference

### Objective

Practice Set subtraction.

### Problem

Return elements present in Set A but not Set B.

---

## Exercise: contains-performance - contains() Performance

### Objective

Build intuition.

### Problem

Compare searching using:

* List
* HashSet

for a large collection.

Record observations.

---

## Exercise: unique-visitors - Unique Visitors

### Objective

Small real-world problem.

### Problem

Given a stream of user IDs,

return the number of unique visitors.

---

## Exercise: mini-challenge - Mini Challenge

### Objective

Combine everything.

### Problem

Given a List of words:

* remove duplicates
* preserve insertion order
* print alphabetically
* print total unique count

Use the most appropriate Set implementations.
