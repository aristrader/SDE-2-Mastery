---
order: 30
---

# Bean Lifecycle

---

## Full lifecycle sequence

```
1.  Instantiation          — constructor called; object exists but has no dependencies yet
2.  Dependency injection   — @Autowired fields and setters populated
3.  Aware callbacks        — BeanNameAware, BeanFactoryAware, ApplicationContextAware (in that order)
4.  BPP before-init        — BeanPostProcessor.postProcessBeforeInitialization() on EVERY bean
5.  Init                   — @PostConstruct  →  InitializingBean.afterPropertiesSet()  →  @Bean(initMethod)
6.  BPP after-init         — BeanPostProcessor.postProcessAfterInitialization() on EVERY bean
7.  Bean ready for use
8.  Destruction            — @PreDestroy  →  DisposableBean.destroy()  →  @Bean(destroyMethod)
                             (singleton only — prototype beans never reach step 8)
```

Steps 4 and 6 apply to **every bean in the context**, not just the bean being initialized. `BeanPostProcessor` implementations run as interceptors across the entire container.

---

## @PostConstruct — why not the constructor?

```java
@Component
public class CacheWarmer {

    @Autowired
    private ProductRepository repository;  // injected AFTER constructor

    @PostConstruct
    public void warmUp() {
        // repository is available here — injection is done
        repository.findTopProducts().forEach(cache::put);
    }
}
```

The constructor fires at step 1, before injection. `@Autowired` fields are null inside the constructor. `@PostConstruct` fires at step 5, after all dependencies are set — safe to use them.

Other init-method equivalents, in Spring's processing order within step 5:
1. `@PostConstruct`
2. `InitializingBean.afterPropertiesSet()`
3. `@Bean(initMethod = "...")`

If you declare all three on one bean, they all run, in that order.

---

## @PreDestroy — singleton only

```java
@Component
public class ConnectionPool {

    private Pool pool;

    @PostConstruct
    public void init() {
        pool = Pool.create();
    }

    @PreDestroy
    public void shutdown() {
        pool.close();  // called when ApplicationContext is closed
    }
}
```

`@PreDestroy` fires when `ApplicationContext.close()` is invoked (or when the JVM shutdown hook fires in a Spring Boot app). It is **only called for singleton-scoped beans**.

Prototype beans never receive `@PreDestroy`. Spring creates them and hands them off — lifecycle management after creation is the caller's responsibility. If a prototype bean holds resources, the caller must close them.

---

## BeanFactoryPostProcessor — before beans exist

`BeanFactoryPostProcessor` runs **before any bean is instantiated**. It receives the `ConfigurableListableBeanFactory` and can read or modify `BeanDefinition` metadata — but must not trigger early bean instantiation.

```
Context refresh starts
  → BeanFactoryPostProcessor.postProcessBeanFactory()   ← runs HERE, on BeanDefinitions
  → Bean instantiation begins
  → BeanPostProcessor.postProcessBefore/AfterInitialization()  ← runs per bean, during init
```

```java
@Component
public class CustomBFP implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition bd = beanFactory.getBeanDefinition("myService");
        bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);  // change scope before instantiation
    }
}
```

Spring uses `BeanFactoryPostProcessor` internally for:
- `PropertySourcesPlaceholderConfigurer` — resolves `${...}` placeholders in `BeanDefinition` values
- `ConfigurationClassPostProcessor` — processes `@Configuration`, `@ComponentScan`, `@Import`

**Key distinction:**

| | `BeanFactoryPostProcessor` | `BeanPostProcessor` |
|---|---|---|
| Runs | Before any bean instantiation | Around each bean's init callbacks |
| Operates on | `BeanDefinition` metadata | Bean instances |
| Can modify | Scope, class, property values in definitions | The bean object itself (can replace with proxy) |
| Spring uses it for | `@Configuration` processing, property placeholders | AOP proxies, `@Autowired`, `@PostConstruct` |

---

## BeanPostProcessor

A `BeanPostProcessor` intercepts every bean in the context at two points: just before init callbacks run, and just after. Spring uses this internally for nearly all its advanced features.

```java
@Component
public class AuditBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(AuditBeanPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        log.debug("Before init: {}", beanName);
        return bean;  // must return the bean (or a replacement)
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        log.debug("After init: {}", beanName);
        return bean;
    }
}
```

**You can return a different object.** The return value replaces the bean in the context. This is how AOP works: `postProcessAfterInitialization` returns a CGLIB proxy wrapping the original bean. Every `@Transactional`, `@Cacheable`, and `@Async` bean you use is a proxy created here.

### What Spring builds on BeanPostProcessor

| Feature | BPP implementation |
|---|---|
| AOP proxies (`@Transactional`, `@Cacheable`) | `AbstractAutoProxyCreator` |
| `@Autowired` processing | `AutowiredAnnotationBeanPostProcessor` |
| `@PostConstruct` / `@PreDestroy` | `CommonAnnotationBeanPostProcessor` |
| `@Scheduled` | `ScheduledAnnotationBeanPostProcessor` |
| `@Async` | `AsyncAnnotationBeanPostProcessor` |

`BeanPostProcessor` beans are themselves special: they are instantiated before other beans and are not eligible for `@Autowired` from regular beans (Spring warns if you try).

---

## InitializingBean / DisposableBean vs annotations

| | `InitializingBean` / `DisposableBean` | `@PostConstruct` / `@PreDestroy` |
|---|---|---|
| Coupling | Coupled to Spring (`org.springframework.beans.factory`) | JSR-250 (`javax.annotation`) — framework-agnostic |
| Testability | Must mock or wire a Spring context to trigger | Plain POJO call — test frameworks call the annotated method directly |
| Preference | Legacy code or framework internals | Always prefer in application code |

Spring processes both if present. Order within step 5: `@PostConstruct` runs before `InitializingBean.afterPropertiesSet()`.

The interface approach is occasionally useful in framework or library code that controls the lifecycle explicitly. In application code, there's no reason to couple to Spring interfaces.

---

## The prototype @PreDestroy gap — explicit gotcha

```java
@Component
@Scope("prototype")
public class StreamProcessor implements AutoCloseable {

    private InputStream stream;

    @PostConstruct
    public void open() { stream = openStream(); }

    @PreDestroy  // NEVER CALLED by Spring for prototype beans
    public void close() { stream.close(); }
}
```

If this bean holds a stream, connection, or any other resource, it leaks. Spring creates the bean (runs `@PostConstruct`) and then forgets about it; `@PreDestroy` is silently skipped.

**Patterns to handle this:**

1. **Implement `AutoCloseable`** and have the caller close it in a try-with-resources block.
2. **`@Bean` with explicit `destroyMethod`** — only works if the bean is singleton.
3. **Don't put stateful resources in prototype beans** — prefer a factory that manages the resource lifecycle explicitly.

---

## Aware callbacks

Three commonly used `Aware` interfaces, called at step 3 in order:

| Interface | What it injects | Typical use |
|---|---|---|
| `BeanNameAware` | The bean's name in the context | Logging, debugging |
| `BeanFactoryAware` | The `BeanFactory` that created this bean | Dynamic `getBean()` calls |
| `ApplicationContextAware` | The full `ApplicationContext` | Publishing events, reaching other beans at runtime |

`ApplicationContextAware` is the most common. Prefer constructor injection when the dependency is known at compile time; use `ApplicationContextAware` only when you need dynamic resolution at runtime (e.g., the scope-mismatch fix).

---

## @DependsOn — explicit initialization ordering

Spring infers bean ordering from injection relationships — if `BeanA` takes `BeanB` as a constructor argument, `BeanB` is created first. But sometimes a bean depends on a side effect of another bean (e.g., a database schema migration, a static registry initialization) without holding a direct reference to it.

`@DependsOn` makes the ordering explicit:

```java
@Component
@DependsOn("flywayMigration")   // flywayMigration bean is guaranteed to init first
public class UserRepository {
    // safe to query — schema is ready
}
```

```java
@Bean
@DependsOn({"kafkaAdminSetup", "schemaRegistry"})  // multiple dependencies
public KafkaConsumer kafkaConsumer() { ... }
```

**`@DependsOn` also affects destroy order** — the named beans are destroyed after the bean that depends on them (reverse of init order).

Use `@DependsOn` sparingly. If you need it often, it usually signals a missing explicit dependency that should be injected instead.

---

## Quick recall

**Q. At which lifecycle step are AOP proxies created?**
A. Step 6 — `BeanPostProcessor.postProcessAfterInitialization()`. `AbstractAutoProxyCreator` wraps the bean in a CGLIB proxy here.

**Q. Why use `@PostConstruct` instead of the constructor for init logic?**
A. The constructor fires before dependency injection — `@Autowired` fields are null. `@PostConstruct` fires after injection, so all dependencies are available.

**Q. Why is `@PreDestroy` never called on prototype beans?**
A. Spring does not track prototype beans after creation. It hands them off and has no hook to call destroy on them. The caller owns the lifecycle.

**Q. What is `BeanPostProcessor` and what does Spring use it for internally?**
A. An interceptor that runs before and after init callbacks on every bean. Spring uses it to create AOP proxies, process `@Autowired`, `@PostConstruct`, `@Scheduled`, and `@Async`.

**Q. `InitializingBean` vs `@PostConstruct` — which and why?**
A. Prefer `@PostConstruct`. It's JSR-250, not Spring-specific, so the class stays portable. `InitializingBean` couples the class to Spring's API.

**Q. What happens if you inject a regular bean into a `BeanPostProcessor`?**
A. Spring warns and may fail — `BeanPostProcessor` beans are created early, before the regular bean instantiation cycle, so regular beans are not yet available for injection into them.

**Q. `BeanFactoryPostProcessor` vs `BeanPostProcessor` — key difference?**
A. BFP runs before any bean is instantiated and operates on `BeanDefinition` metadata. BPP wraps each individual bean's init callbacks and operates on bean instances (can return a proxy).

**Q. When do you use `@DependsOn`?**
A. When a bean relies on a side effect of another bean (e.g., a migration runner) but holds no direct reference to it. Without `@DependsOn`, Spring has no way to infer the ordering.

