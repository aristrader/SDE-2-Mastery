---
order: 10
search: false
---

# Practice

## Exercise: basic-deque - Basic Deque

### Objective
Practice inserting and removing from both ends.

### Task
Create a `Deque<Integer>` and use:

- `offerFirst`
- `offerLast`
- `pollFirst`
- `pollLast`

### Checks
- Can you explain which end each method touches?
- What order remains after removing from both ends?

## Exercise: reverse-order - Reverse Order with Deque

### Objective
Use a deque to compare FIFO and LIFO processing.

### Task
Insert numbers `1..5` using `offerFirst`, then remove from the front.
Repeat using `offerLast`.

### Checks
- Which approach behaves like LIFO?
- Which approach behaves like FIFO?

## Exercise: stack-using-deque - Stack using Deque

### Objective
Use `ArrayDeque` as a stack replacement.

### Task
Push `10`, `20`, `30`, then inspect and pop.

### Checks
- Which value is at the top?
- Why avoid legacy `Stack`?

## Exercise: palindrome-check - Palindrome Check

### Objective
Use both ends of a deque.

### Task
Write `boolean isPalindrome(String s)` by adding characters to a deque and comparing front/back characters.

### Checks
- Does it work for `madam`?
- Does it fail for `hello`?
