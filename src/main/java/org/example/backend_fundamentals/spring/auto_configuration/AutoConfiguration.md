# Auto-Configuration

---

## What `@SpringBootApplication` actually is

`@SpringBootApplication` is a composed annotation — a shortcut for three annotations on one class:

```java
@SpringBootApplication
// is exactly equivalent to:
@Configuration         // this class is a source of bean definitions
@EnableAutoConfiguration  // trigger auto-config scanning
@ComponentScan         // scan this package and all sub-packages for @Component, @Service, etc.
public class MyApp { ... }
```

`@EnableAutoConfiguration` is the key one. It tells Spring Boot to scan for auto-configuration classes and conditionally apply them.

---

## How auto-config classes are discovered

Spring Boot maintains a registry of auto-configuration classes. At startup, `@EnableAutoConfiguration` reads this registry and loads each class, subject to their `@Conditional` gates.

### Spring Boot 2.x — `spring.factories`

```
META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,\
  ...
```

This file lives inside every `spring-boot-autoconfigure-*.jar`. Spring Boot reads all `spring.factories` files on the classpath at startup (including from starters) and instantiates each listed class conditionally.

### Spring Boot 3.x — `AutoConfiguration.imports` only

Spring Boot 3 dropped `spring.factories` support for auto-configuration entries entirely. Only the dedicated file is read:

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

One fully-qualified auto-config class per line. Same semantics — read at startup, each class evaluated conditionally. The split improves startup performance (no need to parse the catch-all `spring.factories` file) and separates auto-config registration from other extension points.

> Migration note: if you maintain a custom starter and target Boot 3+, move your auto-config class registrations from `spring.factories` to `AutoConfiguration.imports`. The `@AutoConfiguration` annotation (introduced in 2.7) should replace `@Configuration` on auto-config classes — it marks the class as excluded from regular `@ComponentScan` pickup.

---

## `@Conditional` — the gates

Every auto-config class is decorated with one or more `@Conditional` annotations. If any condition fails, the entire configuration class is skipped. These are the main ones:

| Annotation | Applies when... |
|---|---|
| `@ConditionalOnClass` | The specified class is present on the classpath |
| `@ConditionalOnMissingClass` | The specified class is NOT on the classpath |
| `@ConditionalOnBean` | A bean of the specified type/name already exists |
| `@ConditionalOnMissingBean` | No bean of the specified type/name exists yet |
| `@ConditionalOnProperty` | A property is set (and optionally has a specific value) |
| `@ConditionalOnWebApplication` | Running in a web context (servlet or reactive) |
| `@ConditionalOnNotWebApplication` | NOT running in a web context |
| `@ConditionalOnResource` | A specific resource exists on the classpath |
| `@ConditionalOnExpression` | A SpEL expression evaluates to true |
| `@ConditionalOnJava` | Running on a specific JVM version range |

### Real example — `DataSourceAutoConfiguration`

```java
@Configuration
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })   // only if JDBC is on classpath
@ConditionalOnMissingBean(type = "io.r2dbc.spi.ConnectionFactory")      // not if R2DBC is present
@AutoConfigureBefore(JdbcTemplateAutoConfiguration.class)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean                        // skip if user defined their own DataSource
    @ConditionalOnProperty(name = "spring.datasource.url")
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
```

---

## The override pattern — `@ConditionalOnMissingBean`

`@ConditionalOnMissingBean` is the primary mechanism for user overrides. User beans are registered before auto-configs run, so if you define your own bean of the same type, the auto-config's `@ConditionalOnMissingBean` check fails and the auto-config bean is skipped.

```java
// Spring Boot's auto-config (inside spring-boot-autoconfigure.jar):
@Bean
@ConditionalOnMissingBean
public ObjectMapper objectMapper() { ... }   // will be skipped if you define your own

// Your app (your @Configuration):
@Bean
public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
}
// Spring Boot's ObjectMapper bean is now skipped.
```

This is why you never need to "turn off" most auto-configs explicitly — just define the thing yourself.

---

## Disabling auto-config explicitly

When `@ConditionalOnMissingBean` isn't sufficient (e.g., you want to prevent Spring from even trying):

```java
// On the main class:
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class MyApp { }

// Or in application.properties:
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

Use this when your app has the JDBC classes on the classpath (e.g., as a transitive dependency) but isn't using a database — without excluding, Spring Boot tries to auto-configure a `DataSource` and fails if no URL is configured.

---

## Auto-configuration ordering

Auto-config classes sometimes depend on each other — `JdbcTemplateAutoConfiguration` needs `DataSource` to exist first. Ordering annotations control the sequence:

| Annotation | Meaning |
|---|---|
| `@AutoConfigureBefore(X.class)` | This auto-config must run before X |
| `@AutoConfigureAfter(X.class)` | This auto-config must run after X |
| `@AutoConfigureOrder(n)` | Numeric ordering within the auto-config set |

These only affect the order among auto-configuration classes, not relative to your own `@Configuration` beans (yours are always processed first).

---

## Debugging auto-config — `ConditionEvaluationReport`

Run your app with `--debug` or set `logging.level.org.springframework.boot.autoconfigure=DEBUG`:

```bash
java -jar myapp.jar --debug
```

This prints the **ConditionEvaluationReport** at startup — three sections:

```
============================
CONDITIONS EVALUATION REPORT
============================

Positive matches (auto-configs that loaded):
-----------------------------------------
DataSourceAutoConfiguration matched:
   - @ConditionalOnClass found required class 'javax.sql.DataSource' (OnClassCondition)

Negative matches (auto-configs that were skipped):
--------------------------------------------------
RabbitAutoConfiguration:
   Did not match: @ConditionalOnClass did not find required class 'com.rabbitmq.client.Channel'

Exclusions:
-----------
None

Unconditional classes:
----------------------
org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration
```

This is your primary diagnostic tool when a bean isn't being created and you can't figure out why.

**Actuator endpoint — runtime introspection:**

If Spring Boot Actuator is on the classpath, the same report is available over HTTP at runtime without restarting:

```
GET /actuator/conditions
```

Returns JSON with `positiveMatches`, `negativeMatches`, `exclusions`, and `unconditionalClasses`. Useful in deployed environments without access to startup logs. Previously `/autoconfig` (pre-Boot 2.x) — renamed to `/conditions` in Boot 2.0.

---

## `spring-boot-autoconfigure` JAR

All built-in auto-configurations live in:

```
org.springframework.boot:spring-boot-autoconfigure
```

This JAR is transitively included by virtually every Spring Boot starter. You can browse it on GitHub at:
`spring-projects/spring-boot` → `spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/`

Common sub-packages: `jdbc/`, `orm/jpa/`, `web/`, `data/redis/`, `security/`, `kafka/`, `amqp/` — each has one or more `*AutoConfiguration.java` classes worth reading when debugging integration issues.

---

## Interview gotchas

**"How does Spring Boot know which auto-configs to load?"**
Not classpath scanning — it reads the static registry file (`spring.factories` or `AutoConfiguration.imports`). Classpath scanning would be too slow and non-deterministic.

**"What's the difference between `@ConditionalOnClass` and `@ConditionalOnBean`?"**
`@ConditionalOnClass` checks the classpath (is the .class file present?). `@ConditionalOnBean` checks the Spring context (has a bean been registered?). Common mistake: using `@ConditionalOnBean` to check for a library class — wrong tool.

**"If I define my own `DataSource` bean, does Spring Boot still try to configure one?"**
No — auto-config uses `@ConditionalOnMissingBean`. Spring Boot processes user `@Configuration` beans before auto-configs, so yours is registered first, and the auto-config's condition fails silently.

**"Can auto-configs load in any order?"**
No — `@AutoConfigureBefore`/`@AutoConfigureAfter` enforce ordering within the auto-config set. But all auto-configs run after user `@Configuration` classes, which is why `@ConditionalOnMissingBean` works.

---

## Quick recall

**Q. What three annotations does `@SpringBootApplication` compose?**
A. `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.

**Q. How does Spring Boot discover auto-config classes?**
A. Reads `META-INF/spring.factories` (Boot 2.x) or `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Boot 3.x) — a static registry, not classpath scanning.

**Q. What does `@ConditionalOnMissingBean` do and why does it matter?**
A. Skips the auto-config bean if you've already defined one of the same type — this is the override mechanism. Define your own bean and Spring Boot's auto-config backs off.

**Q. How do you disable an auto-config entirely?**
A. `@SpringBootApplication(exclude = SomeAutoConfiguration.class)` or `spring.autoconfigure.exclude` property.

**Q. How do you debug which auto-configs loaded and why?**
A. Run with `--debug` flag; Spring prints the `ConditionEvaluationReport` showing positive matches, negative matches (skipped), and exclusions.

**Q. `@ConditionalOnClass` vs `@ConditionalOnBean` — difference?**
A. `OnClass` checks the classpath (is the .class present?); `OnBean` checks the Spring context (is a bean registered?). Do not confuse them.

**Q. Why do auto-configs run after user `@Configuration` classes?**
A. By design — so `@ConditionalOnMissingBean` can detect user-defined beans before deciding whether to create auto-config beans.

**Q. When did `spring.factories` get replaced and what replaced it?**
A. Boot 2.7 introduced `AutoConfiguration.imports` alongside `spring.factories`. Boot 3.0 dropped `spring.factories` for auto-config entirely — only `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` is read.

**Q. How do you inspect which auto-configs loaded in a running app without restarting?**
A. `GET /actuator/conditions` (requires Actuator). Returns positive matches, negative matches, and exclusions as JSON. Formerly `/autoconfig` in Boot 1.x.
