---
order: 20
---

# Auto-Configuration

---

## Mental model

Spring Boot auto-configuration means:

> If a library is on the classpath and you have not defined your own bean, Boot creates sensible default beans for you.

Example: adding JDBC/JPA dependencies lets Boot attempt `DataSource`, transaction, and JPA setup. Adding web dependencies lets Boot configure Spring MVC, Jackson, and an embedded server.

---

## `@SpringBootApplication`

`@SpringBootApplication` combines three ideas:

```java
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class App {
}
```

| Part | Meaning |
| --- | --- |
| `@Configuration` | This class can define beans |
| `@ComponentScan` | Find your `@Component`, `@Service`, `@Repository`, `@Controller` classes |
| `@EnableAutoConfiguration` | Apply Boot's conditional default configuration |

The auto-configuration part is what makes Boot feel automatic.

---

## Conditional defaults

Auto-configuration is conditional. Boot creates defaults based on libraries, properties, and beans already present.

You do not need to memorize condition annotations. Just remember the common idea:

| Condition | Meaning |
| --- | --- |
| Library exists | Boot can configure support for it |
| Your bean already exists | Boot usually backs off |
| Required property is missing | Boot may skip config or fail startup |

Tiny example:

```java
@ConditionalOnClass(PaymentClient.class)
@ConditionalOnMissingBean
PaymentClient paymentClient(...) { ... }
```

This says: if `PaymentClient` exists and the app has not defined its own `PaymentClient`, create a default one.

---

## Customizing auto-configuration

Most interview/use-case questions are really about this: what do you do when Boot's default is not what you want?

**Option 1 — define your own bean.**

Boot usually backs off if you provide the bean yourself.

```java
@Configuration
public class JsonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }
}
```

Use this when you still want the feature, but with your own configuration.

**Option 2 — configure the feature with properties.**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/app
spring.datasource.username=app
spring.datasource.password=secret
```

Use this when Boot needs values to create the default bean.

**Option 3 — exclude the auto-configuration.**

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class App {
}
```

Use this when a dependency is on the classpath but the app intentionally does not use that feature.

Example: `spring-boot-starter-data-jpa` is present, so Boot tries to create a `DataSource`. If the app has no database, exclude `DataSourceAutoConfiguration`. If the app does use a database, configure `spring.datasource.*`.

Interview line:

```text
Auto-config is opinionated, but user configuration wins.
```

---

## Quick recall

**Q. What is auto-configuration?**
A. Boot creates default beans when matching libraries/properties are present and you have not defined your own bean.

**Q. What enables auto-configuration?**
A. `@EnableAutoConfiguration`, included inside `@SpringBootApplication`.

**Q. Why does defining your own bean override Boot's default?**
A. Boot usually backs off when a user-defined bean already exists.

**Q. When exclude an auto-configuration?**
A. When a dependency is on the classpath but you intentionally do not want Boot to configure it.
