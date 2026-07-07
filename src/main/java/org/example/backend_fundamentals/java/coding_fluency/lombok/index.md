---
order: 50
---

# Lombok

## Why this matters
Lombok eliminates the boilerplate around DTOs and service classes: constructors, getters,
equals/hashCode, builders, and loggers. On a KYC platform these appear in every verification request,
response DTO, and service component. Knowing exactly which methods each annotation generates — and
where the gotchas are — is table stakes for code review at SDE2 level.

## Quick recall

**Q.** What is the key difference between `@Data` and `@Value`?  
**A.** `@Data` generates setters and does not make fields final — objects are mutable. `@Value` makes all fields `private final` and generates no setters — objects are immutable.

**Q.** Why does `@Builder` break Jackson deserialization by default?  
**A.** `@Builder` suppresses the no-arg constructor. Jackson needs a no-arg constructor (or an annotated builder) to deserialize JSON. The cleanest fix is `@Jacksonized` alongside `@Builder`; alternatively, add `@NoArgsConstructor` + `@AllArgsConstructor` and annotate the constructor with `@JsonCreator`.

**Q.** What fields does `@RequiredArgsConstructor` include in the generated constructor?  
**A.** Only `final` fields and fields annotated `@NonNull`. Non-final, non-annotated fields are excluded.

**Q.** Why is `@Data` dangerous on a JPA entity?  
**A.** Lombok's generated `equals`/`hashCode` includes all fields. Accessing a lazy-loaded collection during comparison triggers an extra Hibernate query and can cause `LazyInitializationException` outside a session.

**Q.** What does `@Slf4j` actually inject?  
**A.** `private static final Logger log = LoggerFactory.getLogger(TheClass.class)` — the exact declaration Lombok writes at compile time. No runtime reflection; the logger name is always the fully-qualified class name.

**Q.** How do you inspect what Lombok actually generated?  
**A.** Run `mvn lombok:delombok` — it writes the expanded source under `target/generated-sources/delombok/` so you can see exactly what was synthesised.


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


## Common Gotchas

- `@Value` is shorthand for getters, private final fields, required-args construction, `equals`/`hashCode`, and `toString`. It generates no setters.
- `@Value` makes every field `private final` automatically — you do not write `final` yourself. Adding a non-final field manually changes the generated API and can make the type accidentally mutable.
- `@Value` already synthesises an all-args constructor (via `@RequiredArgsConstructor` on all-final fields). `@Builder` alongside `@Value` works because Lombok coordinates the two, but adding a manual `@AllArgsConstructor` causes a "duplicate constructor" compile error — leave constructor generation to Lombok.
- `@Data` on a JPA entity is dangerous — Lombok's `equals`/`hashCode` includes all fields. If a lazy collection is touched during equality, Hibernate may fire extra queries or throw outside a session. Use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` on entities.
- `@Slf4j` injects a static logger named after the class. You can confirm with `log.getClass().getName()` and `log.getName()` when learning what Lombok generated.
- SLF4J's `{}` placeholder is not `String.format` — don't use `+` concatenation or `String.format()` in log calls. Placeholder arguments avoid string construction when the log level is disabled.
- `@RequiredArgsConstructor` generates a constructor only for `final` fields and fields annotated `@NonNull`. A non-final field without `@NonNull` is silently excluded, so Spring will not inject it through that constructor.
- Spring 4.3+ autowires a single constructor automatically. With `@RequiredArgsConstructor` and `@Component`/`@Service`, you usually do not need `@Autowired` on the generated constructor.
