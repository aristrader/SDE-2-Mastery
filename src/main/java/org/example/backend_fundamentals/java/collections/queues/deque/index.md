---
order: 20
---

# Deque

`Deque` means double-ended queue. It supports insertion and removal at both ends.

```java
Deque<Integer> deque = new ArrayDeque<>();
deque.offerFirst(10);
deque.offerLast(20);
deque.pollFirst();
deque.pollLast();
```

## Stack replacement

Use `ArrayDeque` instead of legacy `Stack`:

```java
Deque<String> stack = new ArrayDeque<>();
stack.push("A");
stack.push("B");

System.out.println(stack.pop()); // B
```

`Stack` extends old synchronized `Vector`; `Deque` is the modern API.

## Common deque patterns

- reverse processing order;
- stack-like behavior;
- checking from both ends, such as palindrome validation.

## Quick recall

- **Add front?** `offerFirst`.
- **Add back?** `offerLast`.
- **Remove front?** `pollFirst`.
- **Remove back?** `pollLast`.
- **Modern stack type?** `Deque` with `ArrayDeque`.
