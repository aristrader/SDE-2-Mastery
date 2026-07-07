---
order: 10
search: false
---

# Lombok Practice

## Domain model

```java
class KycVerificationResult {
    String userId;
    String status;
    String reason;
    java.time.Instant verifiedAt;
}

class KycVerificationService {
    KycRepository repository;
    NotificationService notifier;
}
```

## Exercise: value-immutable-dto - @Value immutable DTO

### Goal
Use `@Value` to make `KycVerificationResult` fully immutable without writing accessors, constructors, or equals/hashCode by hand.

### Task
Annotate `KycVerificationResult` with `@Value`. Then:
1. Try to add a setter — what happens?
2. Try to set a field directly (`result.status = "X"`) — what happens?
3. Print the object — what does `toString()` look like without you writing it?
4. Create two instances with the same data and confirm `equals()` returns `true`.

### Gotcha
`@Value` is shorthand for getters, private final fields, required-args construction, `equals`/`hashCode`, and `toString`. It generates no setters. You do not write `final` yourself; adding a non-final field manually changes the generated API and can make the type accidentally mutable.

## Exercise: builder-value - @Builder + @Value build-then-lock

### Goal
Combine `@Builder` with `@Value` to get a fluent builder producing immutable objects; use `toBuilder()` to derive modified copies.

### Task
1. Add `@Builder` alongside `@Value` on `KycVerificationResult`. Build an instance using the generated builder.
2. Attempt to call a setter on the built object — confirm it doesn't exist.
3. Enable `toBuilder = true` on `@Builder`. Create a base result, then derive a new result with only `status` changed using `result.toBuilder().status("REJECTED").build()`. Confirm the original is unchanged.

### Gotcha
`toBuilder()` is not generated unless you set `toBuilder = true` explicitly.

## Exercise: data-vs-value - @Data vs @Value mutation boundary

### Goal
Understand exactly which methods each annotation generates and why `@Data` is unsuitable for immutable DTOs.

### Task
Create two versions of the same tiny class, for example `UserSummary` with `id` and `email`: one with `@Data`, one with `@Value`. For each, list:
- Getters, Setters, Constructor, equals/hashCode, toString, Field modifier.
Call a setter on the `@Data` version to confirm mutation is possible.

### Gotcha
`@Data` on a JPA entity is dangerous — Lombok's `equals`/`hashCode` includes all fields. Use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on entities instead.

## Exercise: slf4j-logging - @Slf4j structured logging

### Goal
Wire up a logger without `Logger` declaration boilerplate; verify the logger name is the class name.

### Task
Add `@Slf4j` to `KycVerificationService`. Then write a `verify(String userId)` method that:
1. Logs at `DEBUG`: `"Starting KYC verification for userId={}"`
2. Logs at `INFO`: `"Verification complete for userId={}, status={}"`
3. Logs at `ERROR` with a caught exception: `"Verification failed for userId={}"`
4. Prints `log.getClass().getName()` and `log.getName()` to confirm what Lombok wired.

### Gotcha
SLF4J's `{}` placeholder is not `String.format` — don't use `+` concatenation or `String.format()` in log calls.

## Exercise: requiredargsconstructor-injection - @RequiredArgsConstructor constructor injection

### Goal
Replace a hand-written constructor for final fields with `@RequiredArgsConstructor`; understand how Spring uses it.

### Task
1. Write `KycVerificationService` manually with a constructor that takes `KycRepository` and `NotificationService` and assigns them to `final` fields.
2. Delete the constructor. Add `@RequiredArgsConstructor`. Confirm the class still compiles and the fields are assigned.
3. Add `@Component` or `@Service` to the class. Explain why Spring's constructor injection works here without `@Autowired` on the constructor.

### Gotcha
`@RequiredArgsConstructor` generates a constructor only for `final` fields and fields annotated `@NonNull`. A non-final field without `@NonNull` is silently excluded.
