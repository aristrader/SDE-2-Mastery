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
