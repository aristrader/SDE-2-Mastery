---
order: 10
search: false
---

# Exercises: Jackson Customization

## Exercise: polymorphic-event-deserialization - Polymorphic Deserialization for Webhooks

### Problem
Your service consumes payment gateway webhook events delivered to a single ingestion endpoint `POST /api/v1/webhooks`. The incoming JSON payload contains a `type` field indicating the event subtype:

- `CHARGE_SUCCEEDED`: contains `eventId`, `timestamp`, `chargeId`, `amountCents`, and `currency`.
- `REFUND_PROCESSED`: contains `eventId`, `timestamp`, `refundId`, `originalChargeId`, and `reason`.

Currently, a junior developer declared the controller parameter as generic `Map<String, Object>` and manually parses keys with nested `if-else` blocks and unsafe casts.

### Requirements
1. Design an abstract class `WebhookEvent` and concrete subclasses `ChargeSucceededEvent` and `RefundProcessedEvent`.
2. Configure Jackson polymorphic deserialization using `@JsonTypeInfo` and `@JsonSubTypes` so Spring MVC deserializes directly into the correct concrete class based on the `type` property.
3. Configure fallback behavior using `defaultImpl` so unknown or newly introduced gateway event types deserialize into an `UnknownWebhookEvent` instead of throwing an unhandled `InvalidTypeIdException`.
4. Provide the controller method signature accepting `WebhookEvent`.

---

## Exercise: pii-masking-serializer - Custom Serializer for Sensitive PII

### Problem
A fintech REST API returns bank account summaries via `GET /api/v1/accounts/{id}`. The response object `AccountResponse` contains an `accountNumber` field (12 to 16 digits). Under regulatory compliance and security policies, the full account number must never be returned in cleartext over the API; it must be masked so only the last 4 digits are visible, with preceding digits replaced by asterisks (e.g. `****-****-****-4321` or `****4321`).

### Requirements
1. Implement a custom Jackson serializer `MaskedAccountNumberSerializer` extending `StdSerializer<String>`.
2. Handle edge cases safely: `null` values and strings shorter than 4 characters.
3. Apply the serializer to the `accountNumber` field in an `AccountResponse` DTO using `@JsonSerialize`.
4. Explain why implementing masking at the Jackson serialization boundary is safer and more maintainable than modifying the domain model or string values in business service methods.

---

## Exercise: third-party-sdk-mixin - Customizing Unmodifiable SDK Classes via Mixins

### Problem
Your application uses an external anti-fraud SDK that returns an unmodifiable compiled class `FraudEvaluationResult`:

```java
// Compiled in thirdparty-fraud-sdk.jar - source code cannot be edited
public final class FraudEvaluationResult {
    private final String evaluationId;
    private final double score;
    private final String rawRiskVector;
    private final String vendorInternalTraceId;

    public FraudEvaluationResult(String evaluationId, double score,
                                 String rawRiskVector, String vendorInternalTraceId) {
        this.evaluationId = evaluationId;
        this.score = score;
        this.rawRiskVector = rawRiskVector;
        this.vendorInternalTraceId = vendorInternalTraceId;
    }

    public String getEvaluationId() { return evaluationId; }
    public double getScore() { return score; }
    public String getRawRiskVector() { return rawRiskVector; }
    public String getVendorInternalTraceId() { return vendorInternalTraceId; }
}
```

When returning this object from an internal audit API, you must:
1. Rename `score` to `risk_score` in the JSON output.
2. Completely suppress `rawRiskVector` and `vendorInternalTraceId` from JSON serialization.

Because the class resides in a third-party dependency JAR, you cannot add `@JsonProperty` or `@JsonIgnore` directly to its source code.

### Requirements
1. Define a Jackson Mixin class or interface `FraudEvaluationResultMixin` declaring the necessary annotations.
2. Register the Mixin with Spring Boot using a `Jackson2ObjectMapperBuilderCustomizer` bean.
3. Explain why declaring `@Bean public ObjectMapper objectMapper() { return new ObjectMapper(); }` to register mixins is an anti-pattern in Spring Boot applications.

---

## Exercise: bidirectional-recursion-dto - Resolve Infinite Recursion in Parent-Child Hierarchy

### Problem
An internal HR management service models `Department` and `Employee` as bidirectional JPA entities:

```java
@Entity
public class Department {
    @Id
    private Long id;
    private String name;

    @OneToMany(mappedBy = "department")
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
    private Department department;
    // getters and setters
}
```

A controller exposes `GET /departments/{id}`:
```java
@GetMapping("/{id}")
public Department getDepartment(@PathVariable Long id) {
    return departmentRepository.findById(id).orElseThrow();
}
```

When invoked, the request crashes with:
`HttpMessageNotWritableException: Could not write JSON: Document nesting exceeds maximum allowed (1000)` caused by a `StackOverflowError`.

### Requirements
1. Identify the exact mechanism causing Jackson to trigger infinite recursion.
2. Provide the annotation-based fix using `@JsonManagedReference` and `@JsonBackReference`.
3. Provide the production-recommended architectural solution using response DTOs and explain why returning persistent JPA entities directly from Spring `@RestController` endpoints violates clean architecture principles.
