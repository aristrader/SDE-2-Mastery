---
order: 10
search: false
---

# Method References Practice

## Domain model

```java
public record Order(String id, OrderStatus status, String customerId,
                    List<LineItem> lineItems, BigDecimal total) {}

public record LineItem(String productId, int quantity, BigDecimal unitPrice) {}

public record OrderDto(String orderId, String customerId, BigDecimal total) {
    public OrderDto(Order order) {
        this(order.id(), order.customerId(), order.total());
    }
}

public class OrderUtils {
    public static boolean isHighValue(Order order) {
        return order.total().compareTo(new BigDecimal("300.00")) > 0;
    }
    public static String summarise(Order order) {
        return order.id() + ":" + order.status();
    }
}
```

## Exercise: static-method - Static method reference

### Goal
Replace a lambda that delegates entirely to a static method.

### Task
Rewrite both using static method references:
```java
orders.stream().filter(o -> OrderUtils.isHighValue(o))
orders.stream().map(o -> OrderUtils.summarise(o))
```
Then add a third: parse a `List<String>` of numeric strings into `List<Integer>` using `Integer::parseInt`.

## Exercise: bound-instance - Bound instance method reference

### Goal
Capture a specific object instance and reference one of its methods.

### Task
1. Print every order summary to stdout by rewriting `o -> System.out.println(o)` as a bound instance reference in a `.forEach()`.
2. You have a specific order: `Order target = orders.get(1)`. Filter the full list to find all orders sharing the same status as `target`, by rewriting the predicate `o -> target.status().equals(o.status())` using a bound reference on `target.status()`.

## Exercise: unbound-instance - Unbound instance method reference

### Goal
Reference an instance method without tying it to a specific object.

### Task
1. Map a `Stream<Order>` to a `Stream<String>` of statuses by rewriting `o -> o.status().name()` (Note: this might need two `.map()` calls to use method references).
2. Map a `Stream<Order>` to customer IDs using `Order::customerId` directly.
3. Given a `List<String>` of mixed-case status names, convert each to uppercase using `String::toUpperCase`.

## Exercise: comparator-comparing - Comparator.comparing

### Goal
Build a `Comparator` using `Comparator.comparing` with an unbound method reference.

### Task
1. Sort `orders` by `customerId` ascending using `Comparator.comparing(Order::customerId)`.
2. Sort by `total` descending: `Comparator.comparing(Order::total).reversed()`.
3. Sort by `status` ascending, then by `total` descending as tiebreaker: chain with `.thenComparing(...)`.

## Exercise: constructor-ref - Constructor reference

### Goal
Use a constructor as a function to transform or collect into a mutable collection.

### Task
1. Map the `Stream<Order>` to a `Stream<OrderDto>` by rewriting `o -> new OrderDto(o)` as a constructor reference.
2. Collect the resulting DTOs into an `ArrayList` using `toCollection(ArrayList::new)`.
