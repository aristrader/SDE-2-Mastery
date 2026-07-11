---
order: 20
search: false
---

# joining Solutions

## Solution: joining-strings - joining

```java
String allIds = orders.stream()
    .map(Order::id)
    .collect(Collectors.joining(","));

String paidIds = orders.stream()
    .filter(o -> o.status() == OrderStatus.PAID)
    .map(Order::id)
    .collect(Collectors.joining(" | ", "[", "]"));
```
