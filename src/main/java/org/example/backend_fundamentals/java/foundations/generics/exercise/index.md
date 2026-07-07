---
order: 10
search: false
---

# Generics Practice

Exercises are grouped by topic. Write code in the matching `*Practice.java` file in this folder. After discussion, the code gets cleaned up into the final demo file.

---

## Topic 1 — Generics basics

1. **`Pair<A, B>`** — holds two values of potentially *different* types. Methods: `getFirst()`, `getSecond()`, `swap()` returns a `Pair<B, A>`.
2. **`Stack<T>`** — backed by `ArrayList`. Methods: `push(T)`, `pop()` (throws if empty), `peek()`, `isEmpty()`.
3. **Spot the bug** — rewrite this using generics:
   ```java
   List names = new ArrayList();
   names.add("Alice");
   names.add("Bob");
   for (Object name : names) {
       String upper = ((String) name).toUpperCase();
   }
   ```

---

## Topic 2 — Bounds (`extends`)

1. **`findMax(List<T> list)`** — returns the largest element. Bound `T` appropriately so you can compare elements.
2. **`sumList(List<T> list)`** — returns the sum as a `double`. Bound `T` so you can call `.doubleValue()` on each element.
3. **Spot the issue** — what's wrong here and how do you fix it?
   ```java
   public <T> T findMax(List<T> list) {
       T max = list.get(0);
       for (T item : list) {
           if (item > max) { // problem?
               max = item;
           }
       }
       return max;
   }
   ```

---

## Topic 3 — Wildcards

### 3a — Unbounded wildcard

1. **`printAll` — bounds vs wildcard comparison.** Write the same method two ways and call both:
   ```java
   // Version A — bounded type parameter (you name the type)
   <T> void printAll(List<T> list)

   // Version B — wildcard (you don't name the type)
   void printAll(List<?> list)
   ```
   Call each with `List<String>`, `List<Integer>`, `List<Double>`. Observe: does the call site look different? Can you pass `List<Integer>` to a `List<Number>` parameter? Try it with both versions and see what compiles.

### 3b — Upper-bounded wildcard (`? extends`)

2. **`sumList(List<? extends Number> list)`** — returns sum as `double`. The `? extends Number` means: accept any list whose elements are Numbers — `List<Integer>`, `List<Double>`, `List<BigDecimal>` all work. Compare with your `BoundsPractice.sumStream` — what's the difference in the method signature?

3. **`printNumbers(List<? extends Number> list)`** — prints each element after calling `.doubleValue()` on it. Try calling it with `List<Integer>` and `List<Double>`. Then try adding an element inside the method — observe the compile error and explain why it happens.

### 3c — Lower-bounded wildcard (`? super`)

4. **`addNumbers(List<? super Integer> list)`** — adds integers 1 to 5 into the list. Call it with a `List<Integer>`, `List<Number>`, and `List<Object>` — all three should work. Try calling it with a `List<String>` and see the compile error. Inside the method, try reading an element back and assigning it to an `Integer` variable — observe and explain.

5. **`fill(List<? super Integer> list, int value, int count)`** — adds `value` to the list `count` times. Call it with `List<Number>` and confirm the list contains the right elements after.

---

## Topic 4 — PECS

1. **`copy(src, dst)`** — write a generic copy method. Decide which wildcard goes on `src` and which on `dst` before writing. Test copying `List<Integer>` into `List<Number>`.
2. **`addDefaults(list, value, count)`** — adds `value` into the list `count` times. Which role does the list play?
3. **`findMax(List<? extends T> list)`** — returns the largest element using PECS. Bound `T` so you can compare.

---

## Topic 5 — Type Erasure

1. **`instanceof` trap** — try `list instanceof List<Integer>`. Observe the compile error. Write the correct runtime check using the raw type.
2. **Same erasure overload** — write `process(List<Integer>)` and `process(List<String>)` as two methods. Observe the compile error. Fix using different method names.
3. **`.getClass()` demo** — create `List<Integer>` and `List<String>`, compare `.getClass()` with `==`. Explain the result.
4. **Unchecked cast** — write a method that takes `Object`, casts it to `List<String>`, and returns it. Explain why the compiler warns but doesn't error, and when the runtime crash actually happens.
