---
order: 20
search: false
---

# Records Solutions

## Solution: basic-dto - Basic record DTO + Jackson round-trip
```java
TransactionDto dto1 = new TransactionDto("1", new BigDecimal("100"), "USD");
TransactionDto dto2 = new TransactionDto("1", new BigDecimal("100"), "USD");
System.out.println(dto1.equals(dto2)); // true
System.out.println(dto1); // TransactionDto[id=1, amount=100, currency=USD]
// dto1.amount = new BigDecimal("200"); // Compiler error: cannot assign a value to final variable amount
```

## Solution: compact-constructor - Compact constructor validation
```java
record TransactionDto(String id, BigDecimal amount, String currency) {
    public TransactionDto {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency must be 3 uppercase letters");
        }
    }
}
```

## Solution: records-vs-value - Records vs Lombok @Value

| Concern | Record | Lombok `@Value` |
| --- | --- | --- |
| Generated members | Constructor, accessors, `equals`, `hashCode`, `toString` are language-defined. | Lombok generates final fields, getters, constructor, equality, and `toString` at compile time. |
| Mutability | Shallowly immutable: a `List` component can still be mutable. | Also shallowly immutable for the same reason. |
| Inheritance | Cannot extend a class; can implement interfaces. | Final by default, so it is normally not extended; design can be adjusted with Lombok options when needed. |
| Serialization/framework binding | Needs a compatible Jackson version/configuration. | Also needs compatible Lombok/Jackson/framework configuration. |
| Copy-with change | Write a `withX` method or constructor call. | `toBuilder` requires explicit `@Builder(toBuilder = true)`, not `@Value` alone. |

## Solution: simulating-tobuilder - Simulating toBuilder() on records
```java
public TransactionDto withAmount(BigDecimal newAmount) {
    return new TransactionDto(id, newAmount, currency);
}
public TransactionDto withCurrency(String newCurrency) {
    return new TransactionDto(id, amount, newCurrency);
}
```
