---
order: 10
search: false
---

# Exercises: Auto-Configuration

---

## Exercise: custom-starter-backoff - Designing a Reusable Starter with Conditional Back-Off

### Problem Statement

You are tasked with building a shared internal telemetry starter library (`company-telemetry-starter`) for Spring Boot 3 microservices.

### Requirements

1. Write a configuration class `TelemetryAutoConfiguration` that:
   - Activates only if the class `io.company.telemetry.TelemetryClient` is present on the classpath.
   - Can be explicitly disabled by setting `management.telemetry.enabled=false`, but defaults to enabled (`true`) if the property is omitted.
   - Registers a default `TelemetryClient` bean (`DefaultTelemetryClient`) only if the consuming microservice has not registered its own custom `TelemetryClient` bean.
2. Specify the exact file path and declaration format required by Spring Boot 3 to register `TelemetryAutoConfiguration` as an auto-configuration class.

---

## Exercise: transitive-datasource-exclusion - Handling Unwanted Transitive Auto-Configuration

### Problem Statement

A high-throughput background worker microservice only consumes events from Apache Kafka and writes to Redis. It depends on an internal shared library (`company-auth-common`) which transitively pulls in `spring-boot-starter-data-jpa`.

During startup, the application crashes with:
`Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.`

### Requirements

1. Explain the sequence of condition evaluations that causes Boot to attempt `DataSource` configuration and fail.
2. Provide two independent ways to exclude `DataSourceAutoConfiguration` and `HibernateJpaAutoConfiguration` without modifying the upstream dependency:
   - Approach A: Programmatic via `@SpringBootApplication`.
   - Approach B: Configuration property via `application.properties` / `application.yml`.
3. Explain which approach is preferred when you need environment-specific or profile-driven exclusion (e.g., in CI or unit test profiles).

---

## Exercise: condition-evaluation-debugging - Diagnosing Failed Auto-Configuration Activation

### Problem Statement

A microservice imports `spring-boot-starter-data-redis`, but injecting `RedisTemplate<String, Object>` fails at startup with `NoSuchBeanDefinitionException`.

### Requirements

1. How do you instruct Spring Boot to output the Condition Evaluation Report on startup, and which specific section of the report explains why a bean was skipped?
2. Identify two distinct common root causes that will appear in the Negative Matches section for an auto-configuration class.
3. What architectural issue occurs if an auto-configuration class is placed inside the base package scanned by `@ComponentScan` instead of being discovered via the auto-configuration imports file?
