---
order: 20
search: false
---

# Stream Basics Solutions

## Solution: list-mutability - List Collector Mutability

```java
List<String> unspecified = orderIds.stream().collect(Collectors.toList());

List<String> mutable = orderIds.stream()
    .collect(Collectors.toCollection(ArrayList::new));

List<String> unmodifiable = orderIds.stream().toList();

List<String> unmodifiableJava10 = orderIds.stream().collect(Collectors.toUnmodifiableList());
```

`mutable.add("O7")` works because the concrete collection factory requested `ArrayList`. The two unmodifiable lists throw `UnsupportedOperationException`. Do not test mutation on `unspecified` as a contract: `Collectors.toList()` deliberately makes no mutability or implementation promise. `Stream.toList()` permits null elements; `Collectors.toUnmodifiableList()` rejects them.

## Solution: set-deduplication - Set Collector Deduplication

```java
Set<String> uniqueCustomerIds = customerIds.stream().collect(Collectors.toSet());
```

The set removes duplicates. Do not assume a specific order from `Collectors.toSet()`.
