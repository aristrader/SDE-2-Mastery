---
order: 20
search: false
---

# Solutions: Spring IoC Container

## Solution: singleton-prototype-scope-leak - Fix Singleton to Prototype Injection Leak

### 1. Root Cause
`CheckoutService` is a singleton bean instantiated once during container startup. Spring resolves constructor dependencies at creation time, injecting a single instance of `TokenGenerator`. Subsequent calls to `checkout(...)` reuse that exact instance because dependency injection happens once at initialization, not on each method invocation.

### 2. Refactored Implementation
```java
@Service
public class CheckoutService {
    private final ObjectProvider<TokenGenerator> tokenGeneratorProvider;

    public CheckoutService(ObjectProvider<TokenGenerator> tokenGeneratorProvider) {
        this.tokenGeneratorProvider = tokenGeneratorProvider;
    }

    public String checkout(String orderId) {
        TokenGenerator tokenGenerator = tokenGeneratorProvider.getObject();
        return tokenGenerator.generateToken(orderId);
    }
}
```

By injecting `ObjectProvider<TokenGenerator>`, `CheckoutService` defers bean resolution until `getObject()` is called, prompting the container to construct a brand new prototype instance with a fresh `salt` for each checkout.

---

## Solution: configuration-properties-binding - Third-Party Client Bean Registration

### 1. Configuration Properties Record
```java
@ConfigurationProperties(prefix = "square.gateway")
public record SquareProperties(String apiKey, int timeoutSeconds) {
}
```

### 2. Configuration Class with `@Bean`
```java
@Configuration
public class SquareConfig {

    @Bean
    public SquareClient squareClient(SquareProperties properties) {
        return new SquareClient(properties.apiKey(), properties.timeoutSeconds());
    }
}
```

### 3. Explanation
- **Why `@Component` cannot be used:** `SquareClient` lives in a compiled external SDK JAR; source annotations cannot be added directly to third-party bytecode. `@Bean` factory methods allow programmatic instantiation and wiring.
- **Discovery:** `SquareProperties` is discovered either via `@ConfigurationPropertiesScan` on the root application class or by adding `@EnableConfigurationProperties(SquareProperties.class)` to `SquareConfig`.

---

## Solution: component-scan-package-boundary - Diagnose Missing Bean Registration

### 1. Root Cause
`@SpringBootApplication` enables component scanning implicitly starting from the package of the declaring class (`com.company.app`) and its subpackages (`com.company.app.*`). Because `AuditLogService` is located in `com.company.audit`, it lies outside `com.company.app` and is excluded during the scan.

### 2. Solutions

**Option A (Recommended: Align package hierarchy):**
Move `Application` up to the common root package `com.company.Application`, or move `AuditLogService` to `com.company.app.audit.AuditLogService`.

**Option B (Explicit scan base packages):**
Configure `@SpringBootApplication` to scan the common parent package:
```java
@SpringBootApplication(scanBasePackages = "com.company")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
