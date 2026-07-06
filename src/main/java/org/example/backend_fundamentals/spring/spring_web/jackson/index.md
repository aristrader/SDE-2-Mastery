---
order: 20
---

# Jackson Customization

---

## @JsonIgnore and @JsonIgnoreProperties

`@JsonIgnore` on a field excludes it from both serialization (Java → JSON) and deserialization (JSON → Java). On a getter it excludes only serialization; on a setter, only deserialization — but this asymmetry is fragile. Prefer field-level placement.

`@JsonIgnoreProperties` at class level covers multiple fields in one annotation:

```java
@JsonIgnoreProperties({"passwordHash", "internalScore"})
public class UserDto { ... }
```

`ignoreUnknown = true` is the most important variant for APIs: it tells Jackson to silently skip any JSON keys that don't map to a field. Without it, adding a new field to a downstream response breaks all callers that haven't updated their DTO yet.

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalPaymentResponse { ... }
```

Interview gotcha: Spring Boot's default `ObjectMapper` sets `FAIL_ON_UNKNOWN_PROPERTIES = false` globally, so you get `ignoreUnknown` behavior for free in a Spring app. Outside Spring you must configure it explicitly.

---

## @JsonProperty — field renaming

Rename the JSON key without changing the Java field name:

```java
public class KycRequest {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("full_name")
    private String fullName;
}
```

`@JsonProperty` controls both directions: the serialized key is `user_id` and deserialization reads `user_id` into `userId`. For asymmetric naming (different key for read vs write) use `@JsonAlias` for extra read-aliases alongside `@JsonProperty`.

---

## @JsonView — per-caller field visibility

Define marker interfaces that represent "views":

```java
public class Views {
    public interface Public {}
    public interface Internal extends Public {}  // Internal includes all Public fields
}
```

Annotate fields with which views expose them:

```java
public class UserDto {
    @JsonView(Views.Public.class)
    private String name;

    @JsonView(Views.Internal.class)
    private String taxId;         // only Internal callers see this
}
```

In a Spring MVC controller, select the active view:

```java
@GetMapping("/users/{id}")
@JsonView(Views.Public.class)
public UserDto getUser(@PathVariable Long id) { ... }
```

The `Internal` view can be used in admin endpoints or internal service calls. One DTO, two JSON shapes — no separate class.

`MapperFeature.DEFAULT_VIEW_INCLUSION` controls fields with **no** `@JsonView` annotation. Default `true` — un-annotated fields appear in every view. Set `false` to make un-annotated fields invisible unless explicitly declared.

---

## Custom serializer

Extend `JsonSerializer<T>` when Jackson's default output isn't what you want — formatting a `Money` type, encrypting a field, or printing a `LocalDate` in a non-ISO pattern:

```java
public class MoneySerializer extends JsonSerializer<Money> {
    @Override
    public void serialize(Money value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("amount", value.getAmount());
        gen.writeStringField("currency", value.getCurrency().getCurrencyCode());
        gen.writeEndObject();
    }
}
```

Register on the field:

```java
@JsonSerialize(using = MoneySerializer.class)
private Money price;
```

Or register globally via a `SimpleModule` — preferred when the type appears across many DTOs:

```java
SimpleModule module = new SimpleModule();
module.addSerializer(Money.class, new MoneySerializer());
objectMapper.registerModule(module);
```

---

## Custom deserializer

Extend `JsonDeserializer<T>` for non-standard input formats:

```java
public class MoneyDeserializer extends JsonDeserializer<Money> {
    @Override
    public Money deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        BigDecimal amount = node.get("amount").decimalValue();
        Currency currency = Currency.getInstance(node.get("currency").asText());
        return Money.of(amount, currency);
    }
}
```

Register on the field or globally the same way as the serializer.

---

## @JsonCreator — deserialization constructor

By default Jackson calls the no-arg constructor and sets fields. When the class has no no-arg constructor (immutable value objects, records, constructors with validation), annotate the constructor Jackson should use:

```java
public class Money {
    private final BigDecimal amount;
    private final String currency;

    @JsonCreator
    public Money(
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency) {
        this.amount = amount;
        this.currency = currency;
    }
}
```

`@JsonCreator` tells Jackson to use this constructor. `@JsonProperty` on each parameter maps the JSON key to the argument — without it, Jackson cannot match positional constructor parameters to JSON fields by name.

Also works on a static factory method:

```java
@JsonCreator
public static Money of(
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("currency") String currency) {
    return new Money(amount, currency);
}
```

Interview relevance: records and Lombok `@Value` classes break Jackson deserialization without this pattern (or a matching `@JsonDeserialize` configuration).

---

## Jackson Mixin

A Mixin lets you attach Jackson annotations to a class you don't own — a third-party library class or a domain object that you can't modify:

```java
// You cannot touch ThirdPartyResponse — it lives in an external jar
public abstract class ThirdPartyResponseMixin {
    @JsonIgnore
    abstract String getInternalDebugInfo();

    @JsonProperty("customer_id")
    abstract String getCustomerId();
}

// Register it
objectMapper.addMixIn(ThirdPartyResponse.class, ThirdPartyResponseMixin.class);
```

Jackson merges the mixin's annotations onto `ThirdPartyResponse` at runtime. No subclassing, no bytecode modification.

---

## JavaTimeModule — java.time support

Jackson core has no built-in support for `java.time` types (`LocalDate`, `LocalDateTime`, `ZonedDateTime`, etc.). Without `JavaTimeModule`, Jackson serializes them as verbose JSON objects (the internal fields of the Java class) rather than ISO strings, and deserialization fails entirely.

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // "2024-01-15", not [2024,1,15]
```

Spring Boot auto-registers `JavaTimeModule` when `jackson-datatype-jsr310` is on the classpath (it is by default via `spring-boot-starter-web`). You still need to disable `WRITE_DATES_AS_TIMESTAMPS` for ISO strings — Spring Boot does NOT disable it by default (the auto-configuration sets sensible defaults via `Jackson2ObjectMapperBuilderCustomizer`).

---

## ObjectMapper configuration — key flags

| Feature | Default | What it does |
|---|---|---|
| `FAIL_ON_UNKNOWN_PROPERTIES` | `true` (plain Jackson) / `false` (Spring Boot) | Throw on unknown JSON keys |
| `WRITE_DATES_AS_TIMESTAMPS` | `true` | Write `LocalDate` as `[2024,1,15]` vs `"2024-01-15"` — set `false` for ISO strings |
| `DEFAULT_VIEW_INCLUSION` | `true` | Include un-annotated fields in every `@JsonView` |
| `FAIL_ON_EMPTY_BEANS` | `true` | Throw when serializing an object with no serializable fields |

---

## Customizing Spring Boot's ObjectMapper

**Option 1 — `Jackson2ObjectMapperBuilderCustomizer` bean (preferred).** Adds to Spring's auto-configured defaults without replacing them:

```java
@Bean
public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
    return builder -> builder
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .modules(new JavaTimeModule());
}
```

**Option 2 — declare your own `ObjectMapper` bean.** Replaces Spring Boot's auto-configured mapper entirely. You lose all of Spring's defaults (`JavaTimeModule` registration, Spring MVC integration). Only do this when you need total control and are prepared to replicate what you need.

The customizer is the senior-level answer in interviews because it composes rather than replaces.

---

## MappingJackson2HttpMessageConverter

Spring MVC uses `HttpMessageConverter` instances to convert between HTTP bodies and Java objects. `MappingJackson2HttpMessageConverter` is the Jackson-backed converter — handles `application/json` (and `application/*+json`).

Spring Boot auto-configures it using the same `ObjectMapper` bean. You rarely touch it directly. When you do (second converter for a different media type, custom charset), use `WebMvcConfigurer`:

```java
@Override
public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.stream()
        .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
        .map(c -> (MappingJackson2HttpMessageConverter) c)
        .findFirst()
        .ifPresent(c -> c.setDefaultCharset(StandardCharsets.UTF_8));
}
```

`extendMessageConverters` modifies the existing list. `configureMessageConverters` replaces it entirely — same trap as the `ObjectMapper` bean override.

---

## Quick recall

**Q. `@JsonIgnoreProperties(ignoreUnknown = true)` — when is it critical?**
A. When consuming external APIs: prevents deserialization failure if the upstream adds new fields your DTO doesn't know about yet.

**Q. Does Spring Boot's ObjectMapper fail on unknown properties by default?**
A. No — Spring Boot sets `FAIL_ON_UNKNOWN_PROPERTIES = false` globally. Plain Jackson defaults to `true`.

**Q. When do you use a Mixin vs a custom serializer?**
A. Mixin: adding annotations to a class you don't own. Custom serializer: controlling the JSON output format of a type.

**Q. `Jackson2ObjectMapperBuilderCustomizer` vs overriding the `ObjectMapper` bean — which is safer?**
A. Customizer — it adds to Spring's defaults. Overriding the bean replaces all defaults and breaks Spring MVC integration if you miss anything.

**Q. Why does Jackson fail to deserialize `LocalDate`/`LocalDateTime` without extra setup?**
A. Jackson core has no `java.time` support. `JavaTimeModule` adds it; also disable `WRITE_DATES_AS_TIMESTAMPS` to get ISO strings instead of arrays.

**Q. `@JsonCreator` — when do you need it?**
A. When your class has no no-arg constructor (immutable classes, records, Lombok `@Value`). Annotate the constructor Jackson should use, and `@JsonProperty` each parameter so Jackson maps JSON keys to arguments.

**Q. Where do you register a `SimpleModule` (custom ser/deser) in Spring Boot?**
A. Declare it as a `@Bean` — Spring Boot auto-discovers and registers `Module` beans into the `ObjectMapper` automatically.


