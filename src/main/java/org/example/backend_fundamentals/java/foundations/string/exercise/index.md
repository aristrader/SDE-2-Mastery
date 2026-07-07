---
order: 10
search: false
---

# Practice

## Exercise: string-pool-equality - String Pool vs Heap

### Goal
Understand the difference between string literals (String Pool) and `new String()` (Heap) using `==` and `.equals()`.

### Task
In `main`, create three Strings:
- `String a = "hello";`
- `String b = "hello";`
- `String c = new String("hello");`

Print the boolean results of:
1. `a == b`
2. `a == c`
3. `a.equals(c)`

### Checks
- Why is `a == c` false?

## Exercise: stringbuilder-loop - StringBuilder in Loops

### Goal
Avoid intermediate garbage creation when building Strings in a loop.

### Task
Write a `for` loop from 0 to 999.
Inside the loop, use a `StringBuilder` to append the numbers together into one long string.
(Do not use the `+` operator to append).

### Checks
- Why is this more efficient than `String s = ""; for(...) s += i;`?
