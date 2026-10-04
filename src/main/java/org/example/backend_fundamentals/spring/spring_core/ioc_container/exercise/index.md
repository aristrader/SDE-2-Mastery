---
order: 10
search: false
---

# Exercises: Spring IoC Container

## Exercise: singleton-prototype-scope-leak - Fix Singleton to Prototype Injection Leak

### Problem
In a payment processing system, `TokenGenerator` is designed to be stateful and generate single-use security tokens. It is marked as `@Scope("prototype")`. A singleton service `CheckoutService` injects `TokenGenerator` directly via its constructor:

```java
@Component
@Scope("prototype")
public class TokenGenerator {
    private final String salt = UUID.randomUUID().toString();

    public String generateToken(String orderId) {
        return salt + "-" + orderId;
    }
}

@Service
public class CheckoutService {
    private final TokenGenerator tokenGenerator;

    public CheckoutService(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    public String checkout(String orderId) {
        return tokenGenerator.generateToken(orderId);
    }
}
```

### Requirements
1. Explain why `CheckoutService` currently reuses the exact same `TokenGenerator` and salt across all checkout calls despite `@Scope("prototype")`.
2. Refactor `CheckoutService` using Spring's `ObjectProvider<T>` to fetch a fresh `TokenGenerator` on every `checkout(...)` invocation without injecting `ApplicationContext`.

---

## Exercise: configuration-properties-binding - Third-Party Client Bean Registration

### Problem
You need to integrate a third-party payment gateway client `SquareClient` (which is an unmodifiable class from an external SDK) into your Spring Boot application. It requires `apiKey` and `timeoutSeconds`.

```java
// From external SDK - cannot modify or add @Component
public class SquareClient {
    private final String apiKey;
    private final int timeoutSeconds;

    public SquareClient(String apiKey, int timeoutSeconds) {
        this.apiKey = apiKey;
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getApiKey() { return apiKey; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
}
```

### Requirements
1. Create a type-safe `@ConfigurationProperties` record named `SquareProperties` mapped to the prefix `square.gateway`.
2. Write a `@Configuration` class `SquareConfig` that registers a `SquareClient` bean using values from `SquareProperties`.
3. Explain why `@Component` cannot be used directly on `SquareClient` and how Spring Boot discovers `SquareProperties`.

---

## Exercise: component-scan-package-boundary - Diagnose Missing Bean Registration

### Problem
A developer adds a new service `AuditLogService` annotated with `@Service`, but when the application boots, `OrderService` fails startup with:
`NoSuchBeanDefinitionException: No qualifying bean of type 'com.company.audit.AuditLogService' available`.

The package structure is:
- Main Application class: `com.company.app.Application` (annotated with `@SpringBootApplication`)
- `OrderService`: `com.company.app.service.OrderService`
- `AuditLogService`: `com.company.audit.AuditLogService`

### Requirements
1. Identify the root cause of why Spring's component scanning missed `AuditLogService`.
2. Provide two valid solutions to fix the issue: one adjusting package hierarchy, and one configuring `@SpringBootApplication`.
