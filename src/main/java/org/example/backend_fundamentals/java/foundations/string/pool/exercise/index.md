---
order: 10
search: false
---

# String Pool Practice

## Exercise: string-pool-equality - String Pool vs Heap

### Goal
Understand literals, heap strings, `==`, and `.equals()`.

### Task
In `main`, create:

- `String a = "hello";`
- `String b = "hello";`
- `String c = new String("hello");`

Print:

1. `a == b`
2. `a == c`
3. `a.equals(c)`

### Checks
- Explain why `a == b` is true.
- Explain why `a == c` is false.
- Explain why `a.equals(c)` is true.
- Add `String d = "he" + "llo";` and predict `a == d` before running it.
- Add `String prefix = "he"; String e = prefix + "llo";` and explain why `.equals()` is the only comparison to keep.
