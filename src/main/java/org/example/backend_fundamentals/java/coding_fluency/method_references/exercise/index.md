---
order: 10
search: false
---

# Method References Practice

Work through these prompts in the practice workspace. Keep notes on compiler errors, runtime output, and the specific rule each exercise is proving.

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

Use the seed list from the stream collectors practice page, or create a small local `orders` list with the same shape.

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
