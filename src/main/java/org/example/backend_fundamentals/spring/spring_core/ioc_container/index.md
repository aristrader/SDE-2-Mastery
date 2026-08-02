---
order: 20
---

# IoC Container

---

## Interview mental model

**IoC** means Spring controls object creation and wiring. Your class declares what it needs; Spring creates the objects and injects dependencies.

**DI** is how IoC happens in code: constructor, setter, or field injection. Prefer constructor injection.

```java
@Service
public class OrderService {
    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

`OrderService` does not create `PaymentService`. Spring does.

Why this matters in interviews: IoC is the big idea, DI is the coding technique, and the container is the runtime object that makes it happen.

---

## Container object

Spring's container object is called `ApplicationContext`. It is responsible for finding bean definitions, creating bean instances, wiring dependencies, applying framework features like proxies, and managing lifecycle.

In Spring Boot, you normally do not declare or inject it. This line creates it:

```java
SpringApplication.run(App.class, args);
```

`BeanFactory` is the lower-level parent abstraction behind this. Know the name for interviews, but real Spring Boot apps use `ApplicationContext`.

---

## How beans get registered

Spring can only inject objects it knows as beans.

Use stereotype annotations for your app classes:

```java
@Service
public class PaymentService {
}

@Repository
public class OrderRepository {
}
```

Use `@Bean` when the object is created by configuration code, often for third-party classes:

```java
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(String provider, int timeoutMs) {
}

@Configuration
public class PaymentConfig {
    @Bean
    public PaymentClient paymentClient(PaymentProperties properties) {
        return new PaymentClient(properties.provider(), properties.timeoutMs());
    }
}
```

`PaymentProperties` is supplied from external config:

```properties
payment.provider=stripe
payment.timeout-ms=3000
```

Spring binds those values into `PaymentProperties`, then injects that object into the `@Bean` method.

For Spring to find standalone `@ConfigurationProperties` classes, add scanning on the main app:

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class App {
}
```

Interview distinction:

| Use | When |
| --- | --- |
| `@Component` / `@Service` / `@Repository` | You own the class and want component scanning to find it |
| `@Bean` | You need custom construction logic or the class comes from a library |

---

## What happens at startup

You do not need deep internals for interviews. Know this flow:

1. Spring Boot creates the container.
2. Component scanning finds annotated classes.
3. Configuration classes contribute `@Bean` methods.
4. Spring creates singleton beans by default.
5. Spring resolves constructor dependencies and injects them.
6. Bean post-processors apply features such as AOP/proxies.
7. The app starts handling requests.

Use case: if `OrderService` needs `PaymentService`, Spring must know both as beans before it can wire them.

---

## Bean scopes

| Scope | Meaning | Interview note |
| --- | --- | --- |
| `singleton` | One bean instance per `ApplicationContext` | Default. Keep services stateless. |
| `prototype` | New instance whenever requested from Spring | Spring creates it but does not manage full destruction lifecycle. |
| `request` | One instance per HTTP request | Web apps only. |
| `session` | One instance per HTTP session | Web apps only. |
| `application` | One instance per servlet context | Web apps only. |

Most backend services are singleton beans:

```java
@Service
public class OrderService {
    private int processedCount; // avoid this shared mutable state
}
```

That field is shared by all requests because there is one `OrderService` instance. Prefer local variables, method parameters, database state, or properly synchronized state.

---

## Scope mismatch

If a singleton directly injects a prototype, the prototype is created once during singleton creation and then reused.

```java
@Component
@Scope("prototype")
public class ReportGenerator {
}

@Service
public class OrderProcessor {
    private final ReportGenerator generator;

    public OrderProcessor(ReportGenerator generator) {
        this.generator = generator;
    }

    public void process(Order order) {
        generator.generate(order); // same instance every time
    }
}
```

Fix: inject `ObjectProvider<ReportGenerator>` and ask for a fresh object when needed.

```java
@Service
public class OrderProcessor {
    private final ObjectProvider<ReportGenerator> generators;

    public OrderProcessor(ObjectProvider<ReportGenerator> generators) {
        this.generators = generators;
    }

    public void process(Order order) {
        ReportGenerator generator = generators.getObject();
        generator.generate(order);
    }
}
```

For request/session beans injected into singletons, use a scoped proxy:

```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
}
```

Spring injects a proxy into the singleton. The proxy resolves the real request-scoped object for the current HTTP request.

---

## Component scanning

Spring finds beans through component scanning:

```java
@Component
public class PaymentService {
}
```

With Spring Boot:

```java
@SpringBootApplication  // placed in com.example.myapp
public class App {
}
```

Spring scans `com.example.myapp` and all subpackages. Put the main class at the root package.

```java
com.example.myapp.App
com.example.myapp.service.PaymentService
com.example.myapp.repository.OrderRepository
```

If the main class is in the wrong package, Spring may not find your beans.

Fix by moving `App` to the root package or by setting an explicit scan base package:

```java
@SpringBootApplication(scanBasePackages = "com.example.myapp")
public class App {
}
```

---

## Properties and profiles

Use `@Value` for a single property:

```java
@Value("${payment.timeout-ms}")
private int paymentTimeoutMs;
```

Use `@ConfigurationProperties` for grouped config:

```java
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(String provider, int timeoutMs) {
}
```

This maps config like:

```properties
payment.provider=stripe
payment.timeout-ms=3000
```

Use `@Profile` to load beans only in certain environments:

```java
@Bean
@Profile("prod")
public PaymentClient realPaymentClient() { ... }
```

Activate a profile with:

```properties
spring.profiles.active=prod
```

Spring's underlying abstraction for this is `Environment`, but direct `Environment` injection is not the default app-code pattern.

---

## Eager vs lazy initialization

Singleton beans are eager by default:

- startup is slower
- wiring errors fail fast
- first request does not pay bean creation cost

`@Lazy` creates a bean only when first needed:

```java
@Service
@Lazy
public class HeavyReportService {
}
```

Use `@Lazy` sparingly. It can hide startup errors until runtime.

---

## Quick recall

**Q. What creates the Spring container in Boot?**
A. `SpringApplication.run(...)`.

**Q. What is `ApplicationContext`?**
A. Spring's main container object. It creates beans and wires dependencies.

**Q. What should most services use instead of `ApplicationContext.getBean()`?**
A. Constructor injection.

**Q. `@Component` / `@Service` vs `@Bean`?**
A. Use stereotypes for classes you own; use `@Bean` for custom construction or third-party classes.

**Q. Which scope is default?**
A. `singleton`.

**Q. Why keep singleton services stateless?**
A. One instance is shared by many threads.

**Q. How do you get a fresh prototype from a singleton?**
A. Inject `ObjectProvider<PrototypeBean>` and call `getObject()`.

**Q. `@Value` vs `@ConfigurationProperties`?**
A. `@Value` for one property; `@ConfigurationProperties` for grouped config.

**Q. What registers standalone `@ConfigurationProperties` classes?**
A. `@ConfigurationPropertiesScan` on the main app, or `@EnableConfigurationProperties(SomeProperties.class)` for explicit registration.

**Q. Why is eager singleton initialization useful?**
A. It catches wiring/config errors at startup instead of during the first request.
