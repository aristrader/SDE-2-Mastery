---
order: 20
search: false
---

# Solutions: Jackson Customization

## Solution: polymorphic-event-deserialization - Polymorphic Deserialization for Webhooks

### 1. Root Cause and Mechanism
By default, Jackson cannot deserialize an abstract class or interface because it cannot instantiate uninstantiable types. `@JsonTypeInfo` instructs Jackson to read an identifying discriminator (here, the `type` property) from the JSON payload. `@JsonSubTypes` maps each discriminator string value (`CHARGE_SUCCEEDED`, `REFUND_PROCESSED`) to its corresponding concrete Java class.

Setting `defaultImpl = UnknownWebhookEvent.class` ensures forward compatibility: if the external payment provider introduces a new event type before your code is updated, Jackson falls back gracefully to `UnknownWebhookEvent` instead of throwing an `InvalidTypeIdException`.

### 2. Implementation

```java
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = UnknownWebhookEvent.class
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChargeSucceededEvent.class, name = "CHARGE_SUCCEEDED"),
    @JsonSubTypes.Type(value = RefundProcessedEvent.class, name = "REFUND_PROCESSED")
})
public abstract class WebhookEvent {
    private String eventId;
    private Instant timestamp;
    private String type;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

public class ChargeSucceededEvent extends WebhookEvent {
    private String chargeId;
    private long amountCents;
    private String currency;

    public String getChargeId() { return chargeId; }
    public void setChargeId(String chargeId) { this.chargeId = chargeId; }

    public long getAmountCents() { return amountCents; }
    public void setAmountCents(long amountCents) { this.amountCents = amountCents; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}

public class RefundProcessedEvent extends WebhookEvent {
    private String refundId;
    private String originalChargeId;
    private String reason;

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public String getOriginalChargeId() { return originalChargeId; }
    public void setOriginalChargeId(String originalChargeId) { this.originalChargeId = originalChargeId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

public class UnknownWebhookEvent extends WebhookEvent {
    // Captures unmapped event types safely without crashing
}
```

### 3. Controller Method

```java
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookEventProcessor eventProcessor;

    public WebhookController(WebhookEventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEvent event) {
        eventProcessor.process(event);
        return ResponseEntity.ok().build();
    }
}
```

---

## Solution: pii-masking-serializer - Custom Serializer for Sensitive PII

### 1. Serializer Implementation

```java
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class MaskedAccountNumberSerializer extends StdSerializer<String> {

    public MaskedAccountNumberSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if (value.length() <= 4) {
            gen.writeString("****");
            return;
        }

        String lastFour = value.substring(value.length() - 4);
        gen.writeString("****-****-****-" + lastFour);
    }
}
```

### 2. DTO Application

```java
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record AccountResponse(
    Long accountId,
    String accountHolder,
    @JsonSerialize(using = MaskedAccountNumberSerializer.class)
    String accountNumber,
    String status
) {}
```

### 3. Architectural Rationale
- **Single Source of Truth:** Placing masking at the Jackson serialization boundary guarantees that every API response returning this DTO adheres to masking rules, even if new controller endpoints or service workflows are added in the future.
- **Data Integrity in Memory:** Service and database layers frequently need the unmasked account number for internal operations (routing payment orders, verification against the core ledger, idempotency keys). Mutating or masking strings directly in service logic or database entities risks corrupting downstream systems or writing masked values back to storage.

---

## Solution: third-party-sdk-mixin - Customizing Unmodifiable SDK Classes via Mixins

### 1. Mixin Definition

```java
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class FraudEvaluationResultMixin {

    @JsonProperty("risk_score")
    abstract double getScore();

    @JsonIgnore
    abstract String getRawRiskVector();

    @JsonIgnore
    abstract String getVendorInternalTraceId();
}
```

### 2. Registration via `Jackson2ObjectMapperBuilderCustomizer`

```java
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonCustomizerConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer fraudSdkMixinCustomizer() {
        return builder -> builder.mixIn(
            FraudEvaluationResult.class,
            FraudEvaluationResultMixin.class
        );
    }
}
```

### 3. Why Manual `ObjectMapper` Bean Creation Is an Anti-Pattern
When you declare `@Bean public ObjectMapper objectMapper() { return new ObjectMapper(); }`:
1. **Destroys Auto-Configuration:** Spring Boot's `JacksonAutoConfiguration` backs off when a user-defined `ObjectMapper` bean is present.
2. **Loses Built-in Modules:** Modules like `JavaTimeModule` (for `Instant`, `LocalDate`), `Jdk8Module` (for `Optional`), and `ParameterNamesModule` (for constructor/record binding) are no longer registered automatically.
3. **Ignores `application.yml` Properties:** Standard Spring Boot properties like `spring.jackson.date-format`, `spring.jackson.default-property-inclusion`, and `spring.jackson.deserialization.fail-on-unknown-properties` are ignored.
4. **Best Practice:** Always use `Jackson2ObjectMapperBuilderCustomizer` to append custom behavior (mixins, serializers, modules) to Spring Boot's shared builder pipeline.

---

## Solution: bidirectional-recursion-dto - Resolve Infinite Recursion in Parent-Child Hierarchy

### 1. Root Cause
In a bidirectional relationship, `Department` holds a reference to a `List<Employee>`, and each `Employee` holds a reference to `Department`. During serialization:
1. Jackson serializes `Department`.
2. It encounters `employees` and serializes each `Employee`.
3. Inside `Employee`, it encounters `department` and attempts to serialize the `Department`.
4. This loops infinitely until Java exhausts stack space or Jackson's depth guard triggers `HttpMessageNotWritableException` / `StackOverflowError`.

### 2. Annotation Fix (`@JsonManagedReference` / `@JsonBackReference`)

```java
@Entity
public class Department {
    @Id
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department")
    @JsonManagedReference
    private List<Employee> employees;
    // getters and setters
}

@Entity
public class Employee {
    @Id
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id")
    @JsonBackReference
    private Department department;
    // getters and setters
}
```
- `@JsonManagedReference` is the forward side: it is serialized normally.
- `@JsonBackReference` is the back side: it is skipped during serialization, breaking the cycle. During deserialization, Jackson automatically sets the back-reference pointer.

### 3. Production Standard: DTO Projection Pattern

```java
public record DepartmentResponse(
    Long id,
    String name,
    List<EmployeeSummaryResponse> employees
) {}

public record EmployeeSummaryResponse(
    Long id,
    String name
) {}

// Service mapper:
public DepartmentResponse toResponse(Department dept) {
    List<EmployeeSummaryResponse> empSummaries = dept.getEmployees().stream()
        .map(e -> new EmployeeSummaryResponse(e.getId(), e.getName()))
        .toList();
    return new DepartmentResponse(dept.getId(), dept.getName(), empSummaries);
}
```

### Why Exposing JPA Entities in REST Controllers Is an Anti-Pattern:
1. **LazyInitializationException:** Serializing uninitialized lazy-loaded associations outside a transaction boundary throws `LazyInitializationException`.
2. **Leaking Internal Schema:** Any database schema change (adding internal columns, refactoring foreign keys) directly breaks external API clients.
3. **Mass Assignment / Security Vulnerabilities:** Exposing entity setters on incoming `@RequestBody` payloads enables over-posting attacks where clients tamper with protected fields (e.g. `isAdmin`, `tenantId`, `balance`).
4. **Tight Presentation-Persistence Coupling:** Persistent entities model storage and transactional boundaries; DTOs model API contracts and client use cases. Mixing them causes cascading refactoring debt.
