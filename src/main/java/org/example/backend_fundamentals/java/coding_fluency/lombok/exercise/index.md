---
order: 10
search: false
---

# Lombok Practice

Work through these prompts in the practice workspace. Keep notes on compiler errors, runtime output, and the specific rule each exercise is proving.

## Domain model

```java
// You will create Lombok-annotated versions of these two types in the exercises
// (do not import Spring annotations unless explicitly asked)

// A verification result returned from the KYC engine
class KycVerificationResult {
    String userId;
    String status;       // e.g. "APPROVED", "REJECTED", "PENDING"
    String reason;
    java.time.Instant verifiedAt;
}

// A service that depends on two collaborators
class KycVerificationService {
    KycRepository repository;      // final field
    NotificationService notifier;  // final field
}
```

## Exercise 1: @Value — immutable DTO (~10 min)

**Goal:** Use `@Value` to make `KycVerificationResult` fully immutable without writing accessors,
constructors, or equals/hashCode by hand.

**What `@Value` generates:** Shorthand for `@Getter` + `@FieldDefaults(level=PRIVATE, makeFinal=true)` + `@RequiredArgsConstructor` + `@EqualsAndHashCode` + `@ToString`. No setters.

**Task:**
Annotate `KycVerificationResult` with `@Value`. Then:
1. Try to add a setter — what happens?
2. Try to set a field directly (`result.status = "X"`) — what happens?
3. Print the object — what does `toString()` look like without you writing it?
4. Create two instances with the same data and confirm `equals()` returns `true`.

**Gotcha:** `@Value` makes every field `private final` automatically — you do not write `final`
yourself. Adding a non-final field manually compiles, but that field gets a setter generated,
defeating immutability.

---

## Exercise 2: @Builder + @Value — build-then-lock (~15 min)

**Goal:** Combine `@Builder` with `@Value` to get a fluent builder producing immutable objects; use
`toBuilder()` to derive modified copies.

**Task:**
1. Add `@Builder` alongside `@Value` on `KycVerificationResult`. Build an instance using the generated
   builder.
2. Attempt to call a setter on the built object — confirm it doesn't exist.
3. Enable `toBuilder = true` on `@Builder`. Create a base result, then derive a new result with only
   `status` changed using `result.toBuilder().status("REJECTED").build()`. Confirm the original is
   unchanged.

**Gotcha:** `@Value` already synthesises an all-args constructor (via `@RequiredArgsConstructor` on all-final fields). `@Builder` alongside `@Value` works because Lombok coordinates the two, but adding a manual `@AllArgsConstructor` causes a "duplicate constructor" compile error — leave constructor generation to Lombok.
`toBuilder()` is not generated unless you set `toBuilder = true` explicitly.

---

## Exercise 3: @Data vs @Value — mutation boundary (~10 min)

**Goal:** Understand exactly which methods each annotation generates and why `@Data` is unsuitable for
immutable DTOs.

**Task:**
Create two versions of the same tiny class (e.g., `UserSummary` with `id` and `email`): one with `@Data`,
one with `@Value`. For each, list:

| Method          | @Data | @Value |
|-----------------|-------|--------|
| Getters         |       |        |
| Setters         |       |        |
| Constructor     |       |        |
| equals/hashCode |       |        |
| toString        |       |        |
| Field modifier  |       |        |

Call a setter on the `@Data` version to confirm mutation is possible.

**Gotcha:** `@Data` on a JPA entity is dangerous — Lombok's `equals`/`hashCode` includes all fields.
If a lazy-loaded collection is touched during equals, Hibernate fires an extra query. Use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on entities instead.

---

## Exercise 4: @Slf4j — structured logging (~10 min)

**Goal:** Wire up a logger without `Logger` declaration boilerplate; verify the logger name is the
class name.

**Task:**
Add `@Slf4j` to `KycVerificationService`. Then write a `verify(String userId)` method that:
1. Logs at `DEBUG` level: `"Starting KYC verification for userId={}"`
2. Logs at `INFO` level: `"Verification complete for userId={}, status={}"`
3. Logs at `ERROR` level with an exception: `"Verification failed for userId={}"` with a caught
   `RuntimeException`.

Print `log.getClass().getName()` and `log.getName()` to confirm what Lombok wired.

**Gotcha:** SLF4J's `{}` placeholder is not `String.format` — don't use `+` concatenation or
`String.format()` in log calls. The placeholder defers string construction until the log level is
enabled, which matters at high throughput.

---

## Exercise 5: @RequiredArgsConstructor — constructor injection (~10 min)

**Goal:** Replace a hand-written constructor for final fields with `@RequiredArgsConstructor`; understand
how Spring uses it.

**Task:**
1. Write `KycVerificationService` manually with a constructor that takes `KycRepository` and
   `NotificationService` and assigns them to `final` fields.
2. Delete the constructor. Add `@RequiredArgsConstructor`. Confirm the class still compiles and the
   fields are assigned.
3. Add `@Component` (or `@Service`) to the class. Explain why Spring's constructor injection works here
   without `@Autowired` on the constructor (Spring 4.3+ injects automatically when there is exactly one
   constructor).

**Gotcha:** `@RequiredArgsConstructor` generates a constructor only for `final` fields and fields
annotated `@NonNull`. A non-final field without `@NonNull` is silently excluded — forget `final` and
the dependency is not injected and remains null at runtime.

---
