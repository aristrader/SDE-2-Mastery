---
order: 20
search: false
---

# Solutions

## Solution: basic-deque - Basic Deque

```java
Deque<Integer> deque = new ArrayDeque<>();
deque.offerFirst(10); // [10]
deque.offerLast(20);  // [10, 20]
deque.offerFirst(5);  // [5, 10, 20]

System.out.println(deque.pollFirst()); // 5
System.out.println(deque.pollLast());  // 20
System.out.println(deque);             // [10]
```

## Solution: reverse-order - Reverse Order with Deque

```java
Deque<Integer> lifo = new ArrayDeque<>();
for (int i = 1; i <= 5; i++) {
    lifo.offerFirst(i);
}
while (!lifo.isEmpty()) {
    System.out.println(lifo.pollFirst()); // 5, 4, 3, 2, 1
}

Deque<Integer> fifo = new ArrayDeque<>();
for (int i = 1; i <= 5; i++) {
    fifo.offerLast(i);
}
while (!fifo.isEmpty()) {
    System.out.println(fifo.pollFirst()); // 1, 2, 3, 4, 5
}
```

## Solution: stack-using-deque - Stack using Deque

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(10);
stack.push(20);
stack.push(30);

System.out.println(stack.peek()); // 30
System.out.println(stack.pop());  // 30
```

Prefer `ArrayDeque` over legacy `Stack`; `Stack` extends old synchronized `Vector`.

## Solution: palindrome-check - Palindrome Check

```java
public boolean isPalindrome(String s) {
    Deque<Character> deque = new ArrayDeque<>();
    for (char c : s.toCharArray()) {
        deque.offerLast(c);
    }

    while (deque.size() > 1) {
        if (!deque.pollFirst().equals(deque.pollLast())) {
            return false;
        }
    }
    return true;
}
```
