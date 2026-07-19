---
order: 20
search: false
---

# Stream Basics Solutions

## Solution: list-mutability - List Collector Mutability

```java
List<String> mutable = orderIds.stream().collect(Collectors.toList());

List<String> unmodifiable = orderIds.stream().toList();

List<String> unmodifiableJava10 = orderIds.stream().collect(Collectors.toUnmodifiableList());
```

`mutable.add("O7")` works. The two unmodifiable lists throw `UnsupportedOperationException`.

## Solution: set-deduplication - Set Collector Deduplication

```java
Set<String> uniqueCustomerIds = customerIds.stream().collect(Collectors.toSet());
```

The set removes duplicates. Do not assume a specific order from `Collectors.toSet()`.
