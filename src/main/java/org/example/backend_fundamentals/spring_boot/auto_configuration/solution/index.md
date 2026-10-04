---
order: 20
search: false
---

# Solutions: Auto-Configuration

---

## Solution: custom-starter-backoff - Designing a Reusable Starter with Conditional Back-Off

### 1. Auto-Configuration Class

```java
package io.company.telemetry.autoconfigure;

import io.company.telemetry.DefaultTelemetryClient;
import io.company.telemetry.TelemetryClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(TelemetryClient.class)
@ConditionalOnProperty(
    prefix = "management.telemetry",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TelemetryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TelemetryClient.class)
    public TelemetryClient telemetryClient() {
        return new DefaultTelemetryClient();
    }
}
```

- `@AutoConfiguration`: Designates the class as a top-level auto-configuration class.
- `@ConditionalOnClass(TelemetryClient.class)`: Verifies required classes are on the classpath.
- `matchIfMissing = true`: Ensures telemetry is enabled by default unless explicitly configured as `management.telemetry.enabled=false`.
- `@ConditionalOnMissingBean(TelemetryClient.class)`: Instructs Boot to step aside if the consuming application defines its own `TelemetryClient` bean.

### 2. Registration in Spring Boot 3

Create the file:
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

Content:
```text
io.company.telemetry.autoconfigure.TelemetryAutoConfiguration
```

*(Note: In Spring Boot 2.6 and earlier, registration used `META-INF/spring.factories` under `org.springframework.boot.autoconfigure.EnableAutoConfiguration`.)*

---

## Solution: transitive-datasource-exclusion - Handling Unwanted Transitive Auto-Configuration

### 1. Root Cause of Startup Failure

`DataSourceAutoConfiguration` evaluates `@ConditionalOnClass({DataSource.class, EmbeddedDatabaseType.class})`. When JPA/JDBC starter is present on the classpath, the condition matches. Boot then attempts to create a `HikariDataSource` bean, looking for connection properties (`spring.datasource.url`). Since no embedded database driver is on the classpath and no URL property is supplied, startup fails.

### 2. Exclusion Approaches

#### Approach A: Annotation-based (`@SpringBootApplication` or `@EnableAutoConfiguration`)

```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class EventWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventWorkerApplication.class, args);
    }
}
```

#### Approach B: Configuration Property

In `application.properties`:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

Or in `application.yml`:
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

### 3. Comparison & Profile Usage

- **Property-based exclusion (`spring.autoconfigure.exclude`)** is preferred for environment-specific or profile-driven configuration because properties can be overridden dynamically across profiles (e.g., `application-test.yml` vs `application-prod.yml`), environment variables (`SPRING_AUTOCONFIGURE_EXCLUDE`), or CLI arguments without recompiling code.
- **Annotation-based exclusion** is hardcoded at compile-time and applies globally across all profiles and test runs unless overridden.

---

## Solution: condition-evaluation-debugging - Diagnosing Failed Auto-Configuration Activation

### 1. Enabling Condition Evaluation Report

Run the application with the `--debug` command-line switch or configure in `application.properties`:
```properties
debug=true
```
*(Or set log level: `logging.level.org.springframework.boot.autoconfigure=DEBUG`)*

Check the **Negative matches** section in the console output. This section explicitly lists skipped configurations and the specific condition annotation that failed.

### 2. Common Root Causes in Negative Matches

1. **Missing Class (`@ConditionalOnClass`)**: A required optional transitive dependency (such as `commons-pool2` for pooled Redis connections or a specific driver) is not present on the classpath.
2. **Missing Property / Value Mismatch (`@ConditionalOnProperty`)**: A required property is absent, or set to a disabling value (e.g., `spring.data.redis.enabled=false`).
3. **Generic Type Mismatch (`@ConditionalOnMissingBean`)**: Boot's default `RedisAutoConfiguration` defines `RedisTemplate<Object, Object>`. If code injects `RedisTemplate<String, Object>` without registering a custom bean with that exact generic signature, injection fails.

### 3. Risk of Putting Auto-Configuration in `@ComponentScan` Packages

If an auto-configuration class is located in a package scanned by `@ComponentScan`:
1. It is processed as a standard user `@Configuration` class during the initial component scan phase.
2. `@ConditionalOnMissingBean` checks run prematurely before other user configurations and other auto-configurations have been registered.
3. Ordering controls (`@AutoConfiguration(before = ..., after = ...)`) are ignored, breaking predictable condition evaluation order and bean override behavior.
