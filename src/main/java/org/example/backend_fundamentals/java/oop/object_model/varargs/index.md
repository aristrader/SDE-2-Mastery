---
order: 50
---

# Variable Arguments (Varargs)

Varargs (`...`) allow a method to accept zero or multiple arguments of the same type, saving you from writing dozens of overloaded methods.

## Internal Behavior
Under the hood, the compiler treats a varargs parameter as an array.
```java
public void print(int... nums) {
    for (int n : nums) {
        System.out.println(n);
    }
}
```
Inside the method, `nums` is exactly the same as `int[] nums`. If you call `print()`, the compiler passes an empty array `new int[0]`. If you call `print(1, 2)`, the compiler passes `new int[]{1, 2}`.

## The Rules of Varargs
1. **Must be the last parameter:** A method can have other parameters, but the varargs parameter must always be the very last one. 
   - `void foo(String name, int... nums)` -> Valid
   - `void foo(int... nums, String name)` -> Invalid
2. **Only one per method:** Because it must be the last parameter, you can only have a maximum of one varargs parameter per method.

## Real-world usage
- `String.format(String format, Object... args)`
- `System.out.printf(String format, Object... args)`
