---
order: 20
search: false
---

# Optional Solutions

## Solution: replace-null-check - Replace a null-check chain
```java
String city = findUserById(id)
    .map(User::address)
    .map(Address::city)
    .orElse("Unknown");

String cityUpper = findUserById(id)
    .map(User::address)
    .map(Address::city)
    .map(String::toUpperCase)
    .orElse("UNKNOWN");
```

## Solution: orelse-vs-orelseget - orElse vs orElseGet eager vs lazy
```java
// expensiveDefault() is called regardless of Optional status
User a = findUserById("existing-id").orElse(expensiveDefault());

// expensiveDefault() is skipped if the user is present
User b = findUserById("existing-id").orElseGet(() -> expensiveDefault());
```

## Solution: filter-ifpresent - filter + ifPresent
```java
findUserById(id)
    .filter(User::active)
    .ifPresent(u -> System.out.println("Sending email to: " + u.email()));
```

## Solution: flatmap-chained - flatMap for chained Optionals
```java
String city = findUserById(id)
    .flatMap(this::getShippingAddress)
    .map(Address::city)
    .orElse("No city on file");
```

## Solution: ifpresentorelse - ifPresentOrElse branch without unpacking
```java
findUserById(id).ifPresentOrElse(
    u -> System.out.println("Found: " + u.email()),
    () -> System.out.println("User not found")
);
```

## Solution: orelsethrow-mandatory - orElseThrow for mandatory lookups
```java
User user = findUserById(id)
    .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
```
