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

Use `@JsonAlias` when you only want extra accepted input names.

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

## ObjectMapper

`ObjectMapper` is Jackson's main class for converting JSON and Java objects manually.

In controllers, you usually do not use it directly. Spring uses it behind the scenes for `@RequestBody` and response bodies.

Use it directly when you need to parse or create JSON inside your own class, for example with a message payload, audit log, or external API response.

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

In Spring Boot, inject the existing `ObjectMapper` bean instead of creating a new one, so your code uses the same JSON behavior as the rest of the app.

---

## Quick recall

**Q. What does `@JsonIgnore` do?**  
A. Excludes a field/property from JSON serialization/deserialization.

**Q. Why use `@JsonIgnoreProperties(ignoreUnknown = true)`?**  
A. To avoid breaking when input JSON contains fields your DTO does not define.

**Q. What does `@JsonProperty` solve?**  
A. It maps different JSON and Java names, such as `user_id` to `userId`.

**Q. What is `ObjectMapper` used for?**  
A. Manual JSON conversion, such as `writeValueAsString` and `readValue`.

**Q. Should you create a new `ObjectMapper` manually in Spring Boot?**  
A. Prefer injecting Boot's existing `ObjectMapper` bean.
