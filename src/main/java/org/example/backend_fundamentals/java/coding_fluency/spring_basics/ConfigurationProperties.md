# @ConfigurationProperties — Coding Exercises

## Why this matters
Your KYC service has a cluster of related runtime knobs — retry limits, timeouts, allowed document types, country allowlists — that change per environment. Scattering them across `@Value` annotations couples classes to individual property keys and fails late (at first use, not startup). `@ConfigurationProperties` binds an entire YAML subtree into a typed object, validates it at startup, and gives you a single autocompletable source of truth.

## Domain model

```yaml
# application.yml — reference config for the exercises
kyc:
  max-retries: 3
  timeout-ms: 5000
  allowed-countries:
    - ID
    - SG
    - MY
  document:
    max-size-mb: 10
    allowed-types:
      - PASSPORT
      - NATIONAL_ID
```

---

## Exercise 1: Basic binding (~10 min)

**Goal:** Map a flat YAML block to a typed Java class.

**Task:** Create `KycProperties` annotated with `@ConfigurationProperties(prefix = "kyc")`. Declare fields:
- `int maxRetries`
- `long timeoutMs`
- `List<String> allowedCountries`

Use camelCase in Java — Spring's relaxed binding converts `max-retries` → `maxRetries` automatically. All three forms below bind to the same `maxRetries` field:

| Form | Example |
|------|---------|
| kebab-case (preferred) | `kyc.max-retries=3` |
| SCREAMING_SNAKE (env var) | `KYC_MAX_RETRIES=3` |
| camelCase | `kyc.maxRetries=3` |

Register the class so Spring picks it up — three options:
- `@Component` on the class itself (simplest)
- `@EnableConfigurationProperties(KycProperties.class)` on a `@Configuration` class (explicit)
- `@ConfigurationPropertiesScan` on the application class (scans a package for all such classes — Spring Boot 2.2+)

**Gotcha:** `@ConfigurationProperties` alone does not register the bean — pick one of the three. Forget, and injection fails with `NoSuchBeanDefinitionException`.

---

## Exercise 2: Nested binding (~10 min)

**Goal:** Bind a nested YAML subtree to a nested Java type.

**Task:** Add a `DocumentConfig` type with:
- `int maxSizeMb`
- `List<String> allowedTypes`

Nest it inside `KycProperties` as a field named `document`. Try both forms and understand the difference:
- Form A: a plain inner class (needs no-arg constructor + setters, or Lombok `@Data`)
- Form B: a `record` (all-args constructor only — works from Spring Boot 2.6+ / 3.x)

Pick one and bind it to the `kyc.document.*` YAML block.

**Gotcha:** An inner class needs either a no-arg constructor + setters or `@ConstructorBinding`. `private static` works; a non-static inner class doesn't — it requires an enclosing instance.

---

## Exercise 3: Startup validation with @Validated (~10 min)

**Goal:** Catch misconfigured environments at startup, not when a request hits bad config.

**Task:** Add `@Validated` to `KycProperties`. Then add Bean Validation constraints:
- `maxRetries`: `@Min(1) @Max(10)`
- `timeoutMs`: `@Positive`
- `allowedCountries`: `@NotEmpty`

Verify: temporarily set `max-retries: 0` and confirm the context fails to start with `BindValidationException` (or equivalent), not a later runtime error.

**Gotcha:** `@Validated` on a `@ConfigurationProperties` class requires `spring-boot-starter-validation` on the classpath. Without it, constraints are silently ignored — no error, no warning, just unchecked bad values. Check your `pom.xml`.

---

## Exercise 4: Immutable record config (~5 min)

**Goal:** Replace the mutable class with an immutable record.

**Task:** Rewrite `KycProperties` as a `record`:

```java
@ConfigurationProperties("kyc")
@Validated
public record KycProperties(
    @DefaultValue("3") @Min(1) @Max(10) int maxRetries,
    @DefaultValue("5000") @Positive long timeoutMs,
    @NotEmpty List<String> allowedCountries
) {}
```

`@DefaultValue` (from `org.springframework.boot.context.properties.bind`) provides a fallback when the property is absent. Records have an all-args canonical constructor, which Spring Boot 3.x uses directly via `@ConstructorBinding` (implicit on records from Boot 3.x — no annotation needed). Keep `@Validated` and the constraints.

**Gotcha:** Spring Boot 2.x needs an explicit `@ConstructorBinding` on the record constructor; 3.x is automatic. Using the 2.x annotation in a 3.x project triggers a deprecation warning or startup error.

---

## Exercise 5: Injection into a service and @Value comparison (~10 min)

**Goal:** See exactly where `@ConfigurationProperties` wins over scattered `@Value`.

**Task:** Create `KycVerificationService` with a constructor that takes `KycProperties`. Implement `boolean isCountryAllowed(String countryCode)` that checks `allowedCountries`. Then, in a comment block or scratch file, write the `@Value` equivalent:

```java
@Value("${kyc.max-retries}") private int maxRetries;
@Value("${kyc.timeout-ms}") private long timeoutMs;
@Value("${kyc.allowed-countries}") private List<String> allowedCountries;
```

Note how many annotations you need, what happens if a key is renamed, and what you lose in testing (hint: `KycProperties` is a POJO you can `new` in a unit test; `@Value` fields need a Spring context or `ReflectionTestUtils`).

**Gotcha:** `@Value("${kyc.allowed-countries}")` with `List<String>` works in YAML but needs comma-separated values in `.properties` files. YAML list syntax (`- ID`) doesn't work with `@Value` — it produces a single string. `@ConfigurationProperties` handles YAML lists natively.

---

## Quick recall

**Q.** `@ConfigurationProperties` is on the class but it's not in the Spring context. What did you forget?
**A.** Registration: `@Component` on the class, `@EnableConfigurationProperties(MyProps.class)` on a config class, or `@ConfigurationPropertiesScan` on the application class — one of the three is required.

**Q.** Name three relaxed binding forms that all map to a Java field named `maxRetries`.
**A.** `kyc.max-retries` (kebab-case), `KYC_MAX_RETRIES` (env var / SCREAMING_SNAKE), `kyc.maxRetries` (camelCase). Spring's `Binder` normalises all three.

**Q.** Why does `@Validated` on a `@ConfigurationProperties` class give you better failure timing than checking values in a service?
**A.** Validation runs at context startup. Bad config causes immediate startup failure with a clear message, rather than a runtime exception on the first request minutes or hours later in production.

**Q.** What is the difference between how a record and a mutable class are bound by `@ConfigurationProperties`?
**A.** A mutable class uses a no-arg constructor + setters. A record uses its all-args canonical constructor (`@ConstructorBinding`, implicit in Spring Boot 3.x), making the bound config immutable after startup.

**Q.** Why does `@Value("${kyc.allowed-countries}")` fail to bind a YAML list but `@ConfigurationProperties` succeeds?
**A.** `@Value` receives the raw string representation. YAML lists serialize as objects, not comma-separated strings, so the conversion fails. `@ConfigurationProperties` uses Spring's `ConversionService`, which understands YAML sequences natively.

**Q.** How do you unit-test a service that depends on `KycProperties` without starting a Spring context?
**A.** Construct `KycProperties` with `new KycProperties(3, 5000L, List.of("ID", "SG"))` and pass it to the service constructor directly — it's a plain Java object. No Spring context or `ReflectionTestUtils` needed.
