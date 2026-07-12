---
order: 90
---

# Lombok

Lombok generates Java source during compilation: constructors, getters, setters, builders, loggers, and equality methods. It is useful, but every annotation should be treated as code you are choosing to generate.

## Common annotations

| Annotation | Generates | Main caution |
| --- | --- | --- |
| `@Getter` / `@Setter` | accessors | setters make objects mutable |
| `@Data` | getters, setters, `equals`, `hashCode`, `toString`, required constructor | too broad for entities and domain objects |
| `@Value` | immutable class shape | all fields become private final |
| `@Builder` | builder API | Jackson needs `@Jacksonized` or explicit config |
| `@RequiredArgsConstructor` | constructor for `final` and `@NonNull` fields | non-final fields are excluded |
| `@Slf4j` | static logger | use `{}` placeholders, not string concatenation |

## `@Data` vs `@Value`

`@Data` creates a mutable object by default.

```java
@Data
class UserDto {
    private String id;
    private String email;
}
```

`@Value` creates an immutable class shape.

```java
@Value
class UserDto {
    String id;
    String email;
}
```

For DTOs that should not change after construction, prefer `@Value`, a record, or an explicit immutable class.

## Constructors and Spring injection

`@RequiredArgsConstructor` includes `final` fields and fields annotated `@NonNull`.

```java
@Service
@RequiredArgsConstructor
class BillingService {
    private final InvoiceRepository repository;
    private final Clock clock;
}
```

Spring 4.3+ autowires a single constructor automatically, so `@Autowired` is usually unnecessary.

If a dependency is not `final`, Lombok will not include it in the generated constructor.

## Builders and Jackson

`@Builder` is convenient for tests and wide DTOs, but it changes construction shape.

```java
@Value
@Builder
@Jacksonized
class VerificationResult {
    String userId;
    String status;
    String reason;
}
```

Use `@Jacksonized` when Jackson should deserialize through the Lombok builder. Without it, Jackson may look for a no-arg constructor or a creator constructor and fail.

## Equality traps

Do not put broad `@Data` on JPA entities. Generated `equals`, `hashCode`, and `toString` can include lazy associations and trigger extra queries or `LazyInitializationException`.

For entities, write equality deliberately or use:

```java
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class UserEntity {
    @EqualsAndHashCode.Include
    private String businessKey;
}
```

## Logging

`@Slf4j` injects:

```java
private static final Logger log = LoggerFactory.getLogger(CurrentClass.class);
```

Prefer parameterized logging:

```java
log.info("Verification completed for userId={}", userId);
```

Do not build log strings eagerly with `+` when the level may be disabled.

## Delombok

When unsure what Lombok generated, inspect it:

```bash
mvn lombok:delombok
```

Generated source is easier to reason about in code reviews than guessing from annotations.

## Quick recall

- **`@Data` vs `@Value`?** `@Data` is mutable; `@Value` is immutable-style.
- **Constructor fields for `@RequiredArgsConstructor`?** `final` and `@NonNull`.
- **Builder + Jackson fix?** Usually `@Jacksonized`.
- **Why avoid `@Data` on JPA entities?** Equality/toString can touch lazy fields.
- **What does `@Slf4j` add?** A static SLF4J logger for the class.
- **How to inspect generated code?** Delombok.
