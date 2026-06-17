# Method References — Coding Exercises

## Why this matters
Method references are the idiomatic replacement for single-method lambdas — less noise, more explicit intent.
Interviewers ask you to rewrite a lambda on the spot; fumbling bound vs unbound signals you learned the syntax but not the semantics.
Constructor references come up in stream-to-collection pipelines and DTO mapping — daily patterns in service code.

## Domain model

Same `Order` and `LineItem` from prior exercises, plus a utility class and a DTO:

```java
public record Order(String id, OrderStatus status, String customerId,
                    List<LineItem> lineItems, BigDecimal total) {}

public record LineItem(String productId, int quantity, BigDecimal unitPrice) {}

/** DTO produced when an order is approved for processing. */
public record OrderDto(String orderId, String customerId, BigDecimal total) {
    /** One constructor: called via constructor reference in Exercise 4. */
    public OrderDto(Order order) {
        this(order.id(), order.customerId(), order.total());
    }
}

/** Utility class with static helpers — used in Exercise 1. */
public class OrderUtils {
    public static boolean isHighValue(Order order) {
        return order.total().compareTo(new BigDecimal("300.00")) > 0;
    }
    public static String summarise(Order order) {
        return order.id() + ":" + order.status();
    }
}
```

Same seed list from `StreamsCore.md` — copy it into your runner.

---

## Exercise 1: Static method reference (`ClassName::staticMethod`) (~5 min)
**Goal:** Replace a lambda that delegates entirely to a static method.

**Task:** You have these two lambdas in a stream pipeline:
```java
orders.stream().filter(o -> OrderUtils.isHighValue(o))   // identify high-value orders
orders.stream().map(o -> OrderUtils.summarise(o))         // summarise each order
```
Rewrite both using static method references. Then add a third: parse a `List<String>` of numeric strings into `List<Integer>` using `Integer::parseInt`.

**Gotcha:** A static reference works only when the lambda does nothing except call the static method with its argument(s) directly — no extra logic, no wrapping. `o -> OrderUtils.isHighValue(o) && o.total() != null` cannot become a method reference.

---

## Exercise 2: Bound instance method reference (`instance::method`) (~5 min)
**Goal:** Capture a specific object instance and reference one of its methods.

**Task:**
1. Print every order summary to stdout by rewriting `o -> System.out.println(o)` as a bound instance reference in a `.forEach()`.
2. You have a specific order: `Order target = orders.get(1)`. Filter the full list to find all orders sharing the same status as `target`, by rewriting the predicate `o -> target.status().equals(o.status())` using a bound reference on `target.status()` — think about which object is "bound" here.

**Gotcha:** `System.out::println` is bound because `System.out` is a specific `PrintStream` captured at the reference site. `PrintStream::println` (unbound) would require passing the `PrintStream` as the first argument, which doesn't fit `Consumer<Order>`. For part 2, the bound reference is `target.status()::equals`, and `o.status()` is what gets passed to `equals`.

---

## Exercise 3: Unbound instance method reference (`ClassName::instanceMethod`) (~5 min)
**Goal:** Reference an instance method without tying it to a specific object — the stream element itself becomes the receiver.

**Task:**
1. Map a `Stream<Order>` to a `Stream<String>` of statuses by rewriting `o -> o.status().name()` — think about how many hops this is and whether a single reference covers it.
2. Map a `Stream<Order>` to customer IDs using `Order::customerId` directly.
3. Given a `List<String>` of mixed-case status names (e.g., `["paid", "pending"]`), convert each to uppercase using `String::toUpperCase`.

**Gotcha:** `o -> o.status().name()` is a two-hop lambda — it cannot collapse into a single method reference. Use it as-is or chain two `.map()` calls; don't force a reference where the lambda does two things. Part 2 is the clean case: `Order::customerId` works because `customerId()` is called on the stream element directly.

---

## Exercise 4: Comparator.comparing with method reference (~5 min)
**Goal:** Build a `Comparator` using `Comparator.comparing` with an unbound method reference — the most common real-world usage of method references outside of streams.

**Task:**
1. Sort `orders` by `customerId` ascending using `Comparator.comparing(Order::customerId)`.
2. Sort by `total` descending: `Comparator.comparing(Order::total).reversed()`.
3. Sort by `status` ascending, then by `total` descending as tiebreaker: chain with `.thenComparing(...)`.

**Gotcha:** `Comparator.comparing` takes a `Function<T, U>` key extractor — an unbound instance reference fits perfectly. `Order::customerId` means "call `customerId()` on whichever `Order` is passed." Don't confuse it with `someOrder::customerId`, which captures a specific order and always returns the same value.

---

## Exercise 5: Constructor reference (`ClassName::new`) (~5 min)
**Goal:** Use a constructor as a function to transform or collect into a mutable collection.

**Task:**
1. Map the `Stream<Order>` to a `Stream<OrderDto>` by rewriting `o -> new OrderDto(o)` as a constructor reference.
2. Collect the resulting DTOs into an `ArrayList` (not the default unmodifiable list) using `toCollection(ArrayList::new)`.

**Gotcha:** `OrderDto::new` resolves to the constructor matching the functional interface's signature. The stream element is `Order`, so Java looks for `OrderDto(Order)` — defined above. Missing constructor is a compile error, not runtime. For `toCollection(ArrayList::new)`, the supplier is `Supplier<ArrayList<OrderDto>>`, matching the no-arg `ArrayList` constructor.

---

## Quick recall

**Q.** What is the difference between a bound and an unbound instance method reference?
**A.** Bound: a specific object instance is captured (`instance::method`) — the reference acts as a zero-arg (or n-arg) function where the receiver is fixed. Unbound: the receiver is the first argument supplied at invocation (`ClassName::method`) — used in `.map()` where the stream element is the receiver.

**Q.** When can you NOT replace a lambda with a method reference?
**A.** When the lambda does more than delegate to a single method call — e.g., two chained calls (`o.status().name()`), arithmetic, conditional logic, or multiple arguments beyond what the method expects.

**Q.** What functional interface does `ClassName::new` satisfy?
**A.** Whichever interface matches the constructor's parameter list. A no-arg constructor satisfies `Supplier<T>`; a single-arg constructor satisfies `Function<A, T>` or `UnaryOperator<T>`; a two-arg constructor satisfies `BiFunction<A, B, T>`.

**Q.** Why is `System.out::println` a bound reference and not a static one?
**A.** `println` is an instance method on `PrintStream`; `System.out` is the specific `PrintStream` instance that gets captured. Static references point to methods that don't require an instance at all (e.g., `Integer::parseInt`).

**Q.** What happens if you write `OrderDto::new` but `OrderDto` has no constructor matching the stream element type?
**A.** Compile error — the constructor reference cannot be resolved to a matching functional interface. The error points to the reference site, not inside `OrderDto`.

**Q.** How does `Comparator.comparing(Order::total)` work and which method reference form is it?
**A.** It uses an unbound instance reference: `Order::total` is the key extractor — Java calls `total()` on whichever `Order` it receives. Chain `.reversed()` for descending, `.thenComparing(...)` for secondary sort keys.
