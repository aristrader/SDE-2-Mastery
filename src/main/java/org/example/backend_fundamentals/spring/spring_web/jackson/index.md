---
order: 30
---

# Jackson Customization

Jackson converts Java objects to JSON and JSON to Java objects. In Spring Boot REST APIs, this happens automatically.

Focus on common annotations, unknown fields, date formatting, and when you would use `ObjectMapper` directly.

---

## Common annotations

### @JsonIgnore

Use it to hide a field from JSON.

```java
public class UserResponse {
    private Long id;
    private String email;

    @JsonIgnore
    private String passwordHash;
}
```

Do not expose secrets like passwords, tokens, or internal risk scores.

### @JsonIgnoreProperties

Use it to ignore multiple fields or unknown input fields.

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentProviderResponse {
    private String paymentId;
    private String status;
}
```

This is useful when consuming external APIs because providers may add fields later.

Spring Boot usually disables `FAIL_ON_UNKNOWN_PROPERTIES` globally, so unknown JSON fields are ignored by default in Boot apps. Still, `ignoreUnknown = true` is explicit and useful on external DTOs.

### @JsonProperty

Use it when the JSON field name and Java field name differ.

```java
public record CreateUserRequest(
        @JsonProperty("user_id") String userId,
        @JsonProperty("full_name") String fullName) {
}
```

It works for both serialization and deserialization.

Use `@JsonAlias` when you only want extra accepted input names during deserialization.

### @JsonView

Use `@JsonView` to serialize different subsets of fields from the same DTO depending on the endpoint or caller role (e.g. public summary vs internal/admin detail).

1. **Define view marker interfaces** (inheritance models progressive field disclosure):
```java
public class UserViews {
    public interface Summary {}
    public interface InternalDetail extends Summary {}
}
```

2. **Annotate model fields**:
```java
public class UserProfileResponse {
    @JsonView(UserViews.Summary.class)
    private Long id;

    @JsonView(UserViews.Summary.class)
    private String username;

    @JsonView(UserViews.InternalDetail.class)
    private String email;

    @JsonView(UserViews.InternalDetail.class)
    private String internalRiskScore;
}
```

3. **Activate the view on controller handler methods**:
```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}/summary")
    @JsonView(UserViews.Summary.class)
    public UserProfileResponse getSummary(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping("/{id}/detail")
    @JsonView(UserViews.InternalDetail.class)
    public UserProfileResponse getDetail(@PathVariable Long id) {
        return userService.getUser(id);
    }
}
```

> **Trap:** Jackson's default is `MapperFeature.DEFAULT_VIEW_INCLUSION = true`, meaning properties without `@JsonView` are serialized in every view. In Spring Boot, disable this globally to prevent leaking unannotated fields:
> ```yaml
> spring:
>   jackson:
>     mapper:
>       default-view-inclusion: false
> ```

---

## Custom serializers and deserializers

When annotations cannot handle custom data masking, composite schemas, or external formats, extend `StdSerializer<T>` or `StdDeserializer<T>`.

### Custom serializer (e.g. PII masking)

```java
public class MaskedCardSerializer extends StdSerializer<String> {

    public MaskedCardSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value == null || value.length() < 4) {
            gen.writeString("****");
            return;
        }
        String lastFour = value.substring(value.length() - 4);
        gen.writeString("****-****-****-" + lastFour);
    }
}
```

Apply directly to a DTO field:
```java
public record CardSummaryResponse(
        String cardHolder,
        @JsonSerialize(using = MaskedCardSerializer.class) String cardNumber) {
}
```

### Custom deserializer (e.g. parsing compound strings)

```java
public class MoneyDeserializer extends StdDeserializer<Money> {

    public MoneyDeserializer() {
        super(Money.class);
    }

    @Override
    public Money deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String raw = node.asText(); // e.g. "USD 49.99"
        String[] parts = raw.split(" ");
        return new Money(parts[0], new BigDecimal(parts[1]));
    }
}
```

Apply via `@JsonDeserialize(using = MoneyDeserializer.class)` on the target field or register globally in a `SimpleModule`.

---

## Mixins for third-party classes

You cannot modify bytecode or add annotations to classes in external SDK JARs. Jackson **Mixins** apply annotations to third-party types without touching their source code.

1. **Define an abstract mixin class or interface** mirroring the target class properties:
```java
// Target class from external SDK:
// public class SdkPaymentToken { private String token; private String signature; }

public abstract class SdkPaymentTokenMixin {
    @JsonProperty("auth_token")
    private String token;

    @JsonIgnore
    private String signature;
}
```

2. **Register the mixin with Spring Boot's builder**:
```java
@Configuration
public class JacksonCustomizerConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer mixinCustomizer() {
        return builder -> builder.mixIn(SdkPaymentToken.class, SdkPaymentTokenMixin.class);
    }
}
```

---

## Polymorphic deserialization

When an API accepts or returns an interface or abstract base class (such as webhook events or heterogeneous commands), Jackson needs a discriminator to determine which concrete subclass to instantiate.

Use `@JsonTypeInfo` and `@JsonSubTypes`:

```java
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CardPaymentEvent.class, name = "CARD"),
    @JsonSubTypes.Type(value = BankTransferEvent.class, name = "BANK_TRANSFER")
})
public abstract class PaymentWebhookEvent {
    private String eventId;
    private String type;
    // getters and setters
}

public class CardPaymentEvent extends PaymentWebhookEvent {
    private String cardLastFour;
}

public class BankTransferEvent extends PaymentWebhookEvent {
    private String iban;
}
```

When receiving `{"type": "CARD", "eventId": "evt_101", "cardLastFour": "4242"}`, Jackson deserializes directly into `CardPaymentEvent`.

> **Deduction alternative:** In Jackson 2.12+, `@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)` infers the concrete class by matching field signatures without requiring an explicit discriminator property in the JSON.

---

## Dates and JavaTimeModule

Java 8 time types such as `LocalDate`, `Instant`, and `LocalDateTime` need Jackson's Java time module.

In Spring Boot with `spring-boot-starter-web`, this is normally already configured.

For readable ISO dates, disable timestamp/array output:

```yaml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
```

Example output:

```json
{
  "createdAt": "2026-07-26T10:15:30Z"
}
```

---

## ObjectMapper and Spring MVC pipeline

`ObjectMapper` is Jackson's central engine for converting between JSON and Java objects.

### How Spring MVC uses Jackson: MappingJackson2HttpMessageConverter

In Spring Boot REST controllers, you do not invoke `ObjectMapper` manually. When a request hits a handler method:
1. `DispatcherServlet` delegating to `RequestMappingHandlerAdapter` checks incoming `Content-Type: application/json` and the `@RequestBody` parameter.
2. Spring iterates through registered `HttpMessageConverter` beans and selects `MappingJackson2HttpMessageConverter`.
3. The converter reads the input stream and delegates to its internal `ObjectMapper` instance to deserialize the payload.
4. For outgoing `@ResponseBody` or `@RestController` return values, `MappingJackson2HttpMessageConverter` serializes the returned Java object to JSON matching the request's `Accept: application/json` header.

### Customizing ObjectMapper: Customizer vs manual bean

In Spring Boot, **never declare `@Bean public ObjectMapper objectMapper() { return new ObjectMapper(); }`**. Doing so wipes out Spring Boot's auto-configured features (`JavaTimeModule`, parameter names module, JDK8 module, naming strategies, and configured property rules).

Instead, customize the builder via `Jackson2ObjectMapperBuilderCustomizer`:

```java
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customJackson() {
        return builder -> builder
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnUnknownProperties(false);
    }
}
```

### Thread safety and performance

- **Thread safety:** `ObjectMapper` is thread-safe **only after configuration is complete**. Reading and writing JSON concurrently across multiple threads is completely safe. Mutating its configuration (e.g. calling `configure()` or `registerModule()`) while requests are in flight is NOT thread-safe and causes race conditions.
- **Allocation overhead:** Creating `new ObjectMapper()` per request is a severe performance anti-pattern. Building an `ObjectMapper` constructs serializer/deserializer provider caches and scans class reflection metadata. Always inject the pre-configured singleton `ObjectMapper` bean.

Use it directly when you need to parse or create JSON inside your own classes, for example with a message payload, audit log, or external API response:

```java
@Service
public class AuditPayloadService {
    private final ObjectMapper objectMapper;

    public AuditPayloadService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(AuditEvent event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    public AuditEvent fromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, AuditEvent.class);
    }
}
```

---

## Gotchas and pitfalls

### 1. Bidirectional relationships and infinite recursion

Directly serializing bidirectional relationships (such as JPA `@OneToMany` / `@ManyToOne`) causes Jackson to navigate parent -> children -> parent in an infinite loop, throwing `HttpMessageNotWritableException: Could not write JSON: Document nesting exceeds maximum allowed (1000)` or `StackOverflowError`.

**Remedies:**
- **`@JsonManagedReference` & `@JsonBackReference`:**
  ```java
  public class Department {
      @JsonManagedReference
      private List<Employee> employees;
  }

  public class Employee {
      @JsonBackReference
      private Department department;
  }
  ```
  `@JsonManagedReference` is serialized forward; `@JsonBackReference` is skipped during serialization, breaking the loop.
- **DTO projection (production standard):** Never expose JPA entities directly in HTTP responses. Map entities to decoupled response DTOs with flat IDs (e.g., `departmentId`).

### 2. Mutating ObjectMapper at runtime

Calling `objectMapper.setSerializationInclusion(...)` or registering modules inside a service or controller method mutates shared global state across all worker threads. If a specific endpoint needs unique serialization, use `objectMapper.writer(filters)` or `@JsonView`, not runtime mutation of the singleton mapper.

### 3. Record deserialization without parameter names

Java records deserialize cleanly out of the box in Spring Boot 3 / Jackson 2.12+ because `jackson-module-parameter-names` is registered automatically and `-parameters` compiler flag is standard. In legacy setups without the module or flag, Jackson fails unless an explicit `@JsonCreator` is added to the canonical constructor.

---

## Quick recall

**Q. What does `@JsonIgnore` do?**
A. Excludes a field/property from JSON serialization and deserialization.

**Q. Why use `@JsonIgnoreProperties(ignoreUnknown = true)`?**
A. To avoid breaking when input JSON contains fields your DTO does not define.

**Q. What does `@JsonProperty` solve?**
A. It maps different JSON and Java names, such as `user_id` to `userId`.

**Q. What is `ObjectMapper` used for?**
A. Manual JSON conversion, such as `writeValueAsString` and `readValue`.

**Q. Should you create a new `ObjectMapper` manually in Spring Boot?**
A. No. Inject the auto-configured singleton `ObjectMapper` bean to preserve registered modules and avoid costly per-request cache allocation.

**Q. What is the role of `MappingJackson2HttpMessageConverter`?**
A. It is the Spring MVC converter that bridges HTTP `application/json` request/response bodies and Java objects via Jackson.

**Q. How do you safely customize Jackson in Spring Boot without losing auto-configuration?**
A. Expose a `Jackson2ObjectMapperBuilderCustomizer` bean rather than declaring a raw `ObjectMapper` bean.

**Q. How do you prevent infinite recursion when serializing bidirectional relationships?**
A. Annotate the forward reference with `@JsonManagedReference` and back reference with `@JsonBackReference`, or ideally map entities to flat DTOs.

**Q. How does `@JsonView` filter fields conditionally?**
A. By marking DTO fields with marker interfaces and tagging controller methods with `@JsonView(ViewClass.class)`.

**Q. What problem do Jackson Mixins solve?**
A. They attach Jackson annotations to third-party or compiled library classes without modifying their source code.

**Q. How does Jackson achieve polymorphic deserialization?**
A. Using `@JsonTypeInfo` to specify the discriminator property and `@JsonSubTypes` to map discriminator values to concrete subclasses.
