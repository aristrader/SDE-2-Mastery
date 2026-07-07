---
order: 20
search: false
---

# Solutions

## Solution: invalid-overload - The Return Type Trap

```java
public void process(int data) {}
// public boolean process(int data) {} // ERROR: process(int) is already defined
```
The compiler rejects this because the method signature is only `process(int)`. The return type is ignored for signature uniqueness. If a caller simply writes `process(5);` without assigning the result to a variable, the compiler has no idea whether it should call the `void` version or the `boolean` version.
