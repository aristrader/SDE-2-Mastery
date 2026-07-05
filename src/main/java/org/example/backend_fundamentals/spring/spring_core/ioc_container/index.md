---
order: 50
---

# IoC Container

---

## What IoC actually is

**Inversion of Control** flips who is in charge. In traditional code, your class creates its dependencies. With IoC, the framework creates and wires everything — your code just declares what it needs.

> You don't call the framework. The framework calls you.

**DI is the mechanism that implements IoC.** You declare dependencies (via constructor, field, or setter); the container resolves and injects them. The container also manages the full lifecycle — creation, wiring, init callbacks, and destruction.

---

## ApplicationContext vs BeanFactory

| | `BeanFactory` | `ApplicationContext` |
|---|---|---|
| Bean creation | Lazy — on first `getBean()` | Eager — all singletons at startup |
| Events | No | Yes (`ApplicationEvent`, `@EventListener`) |
| i18n | No | Yes (`MessageSource`) |
| AOP support | Limited | Full (proxy creation via `BeanPostProcessor`) |
| Environment / properties | No | Yes (`@Value`, `Environment`) |
| Use in practice | Never directly | Always |

`ApplicationContext` extends `BeanFactory`. In production code, always use `ApplicationContext` (or let Spring Boot wire it). `BeanFactory` is an internal interface — knowing it exists matters for interviews; using it directly does not.

**Eager init at startup is a feature, not a problem.** A misconfigured bean blows up at startup, not on the first production request at 3 AM. This is the fail-fast principle.

Common `ApplicationContext` implementations:

- `AnnotationConfigApplicationContext` — standalone apps, annotation-based config
- `AnnotationConfigServletWebServerApplicationContext` — Spring Boot web apps (created internally)
- `ClassPathXmlApplicationContext` — legacy XML-based config (rarely seen post-2015)

---

## Bean scopes

### singleton (default)

One instance per `ApplicationContext`. Every injection point and every `getBean()` call returns the same object.

```java
@Component  // singleton by default
public class OrderService {
    // same instance injected everywhere in this context
}
```

**Shared state risk:** if you put mutable instance fields on a singleton bean, every thread hits the same object. Design singletons to be stateless or protect state with synchronization.

### prototype

A new instance is created every time the bean is requested — via injection or `getBean()`.

```java
@Component
@Scope("prototype")
public class ReportGenerator {
    // fresh instance per caller
}
```

**Critical gotcha:** Spring creates prototype beans on demand but **does not manage their lifecycle after creation**. `@PreDestroy` is never called on prototype beans. If the bean holds resources (connections, file handles), the caller is responsible for cleanup.

### request / session / application

Web-only scopes. One bean per:

- `request` — one per HTTP request; destroyed when the request completes
- `session` — one per HTTP session; destroyed when the session expires
- `application` — one per `ServletContext` (effectively a singleton across the web application)

Declare with `@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)` — the `proxyMode` is required when injecting a short-lived scoped bean into a longer-lived one (e.g., request-scoped into a singleton).

---

## Scope mismatch — the silent bug

Injecting a `prototype` (or `request`-scoped) bean into a `singleton` bean:

```java
@Component  // singleton
public class OrderProcessor {

    @Autowired
    private ReportGenerator generator;  // prototype — but you get the SAME one forever

    public void process(Order order) {
        generator.generate(order);  // always the same ReportGenerator instance
    }
}
```

The singleton is created once at startup, and Spring injects one `ReportGenerator` at that moment. Every subsequent call to `process()` uses **that same instance** — prototype semantics are lost.

**Fix 1 — inject `ApplicationContext`, call `getBean()` each time:**

```java
@Component
public class OrderProcessor {

    @Autowired
    private ApplicationContext ctx;

    public void process(Order order) {
        ReportGenerator generator = ctx.getBean(ReportGenerator.class);  // fresh each time
        generator.generate(order);
    }
}
```

**Fix 2 — `@Lookup` method injection (cleaner, Spring-managed):**

```java
@Component
public abstract class OrderProcessor {

    public void process(Order order) {
        ReportGenerator generator = createGenerator();
        generator.generate(order);
    }

    @Lookup
    protected abstract ReportGenerator createGenerator();  // Spring overrides this at runtime
}
```

Spring subclasses `OrderProcessor` at runtime and overrides `createGenerator()` to call `getBean()` internally. The class must be non-final; the method must be non-final and non-private.

---

## BeanDefinitionRegistry and component scanning

Before the `ApplicationContext` can create any beans, it builds a registry of `BeanDefinition` objects — one per bean. A `BeanDefinition` describes how to create the bean: class name, scope, constructor arguments, property values, init/destroy method names, and lazy-init flag. No instantiation happens yet — this is pure metadata.

`BeanDefinitionRegistry` is the interface through which `BeanDefinition`s are registered; `DefaultListableBeanFactory` implements it. You rarely interact with it directly, but it underlies `@ComponentScan`, XML config, and `@Bean` methods — they all register `BeanDefinition`s into the registry.

### `@ComponentScan` — base package resolution

```java
@SpringBootApplication  // placed in com.example.myapp
public class MyApp { }
```

`@ComponentScan` with no explicit `basePackages` scans **the package of the annotated class and all its sub-packages**. This is why Spring Boot apps place their main class at the top-level package (`com.example.myapp`) — everything underneath gets scanned automatically.

If the main class sits in `com.example.myapp.config` without `basePackages`, components in `com.example.myapp.service` are missed. Set `basePackages` explicitly when the main class is not at the root package.

```java
@ComponentScan(basePackages = "com.example.myapp")   // explicit, safe
```

---

## Environment abstraction — profiles and properties

The `Environment` abstraction (`org.springframework.core.env.Environment`) provides unified access to:

- **Properties** — from `application.properties`, `application.yml`, system properties, environment variables, and custom `PropertySource` implementations. Injected via `@Value("${some.property}")` or `environment.getProperty("some.property")`.
- **Profiles** — named sets of beans/config. Activate with `spring.profiles.active=prod`. Beans annotated `@Profile("prod")` are only registered when that profile is active.

```java
@Component
public class DataSourceConfig {

    @Autowired
    private Environment env;

    public String getDbUrl() {
        return env.getProperty("spring.datasource.url");  // resolves across all PropertySources
    }
}

@Bean
@Profile("prod")                    // registered only when prod profile is active
public DataSource prodDataSource() { ... }

@Bean
@Profile("!prod")                   // registered in every non-prod profile
public DataSource devDataSource() { ... }
```

`ApplicationContext` extends `EnvironmentCapable`, so it exposes `getEnvironment()`. `BeanFactory` does not — one of the concrete advantages of `ApplicationContext` over the raw `BeanFactory`.

---

## Eager vs lazy initialization

**Eager (default for singletons):** all singleton beans are instantiated when the `ApplicationContext` starts.

- Pro: misconfigured beans fail at startup, not in production under load
- Pro: no first-request latency spike
- Con: slower startup (matters for serverless / CLI tools)

**Lazy (`@Lazy`):** bean is created on first use.

```java
@Component
@Lazy
public class HeavyReportingService {
    // instantiated only when first injected or requested
}
```

- Pro: faster startup, saves memory if the bean is rarely used
- Con: config errors surface at runtime, not startup; first caller pays the init cost

`@Lazy` on a `@Configuration` class makes all `@Bean` methods in that class lazy. `@Lazy` at an injection point defers resolution even if the target bean is normally eager.

---

## Quick recall

**Q. What is IoC and how does DI implement it?**
A. IoC: framework controls object creation and wiring, not your code. DI: you declare dependencies; the container injects them — that's how IoC is achieved.

**Q. ApplicationContext vs BeanFactory — which do you use and why?**
A. Always `ApplicationContext`. It adds eager singleton init (fail-fast), events, AOP, and property resolution on top of `BeanFactory`'s lazy baseline.

**Q. What is the scope mismatch problem?**
A. Injecting a prototype (or request-scoped) bean into a singleton — the singleton captures one instance at startup; prototype semantics are lost. Fix with `ApplicationContext.getBean()` or `@Lookup`.

**Q. What does Spring NOT do for prototype beans that it does for singletons?**
A. `@PreDestroy` is never called on prototype beans — Spring hands them off and forgets them. Caller is responsible for cleanup.

**Q. Why is eager singleton init called "fail-fast"?**
A. Misconfigured beans blow up at startup before any traffic hits, rather than failing on the first production request.

**Q. When would you use `@Lazy`?**
A. Slow-starting beans that are rarely needed (CLI tools, seldom-used services), or to break certain circular dependency situations — but prefer redesigning over using `@Lazy` as a band-aid.

**Q. What does `@ComponentScan` scan when no `basePackages` is specified?**
A. The package of the annotated class and all sub-packages. Place the main class at the root package so everything underneath is covered.

**Q. What is a `BeanDefinition` and when is it created?**
A. Metadata describing how to create a bean (class, scope, init method, etc.). Created during context refresh before any bean is instantiated — the registry is built first, then instantiation follows.

**Q. What does the `Environment` abstraction unify?**
A. Properties (from files, system env, CLI args) and profiles. `@Value`, `@Profile`, and `environment.getProperty()` all go through it. Available on `ApplicationContext` but not on raw `BeanFactory`.


<ExerciseNav />
