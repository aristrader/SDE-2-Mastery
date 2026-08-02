---
order: 30
---

# Bean Lifecycle

---

## Mental model

Spring does more than call constructors. For a normal singleton bean, the useful lifecycle is:

```text
create object -> inject dependencies -> run init callbacks -> apply proxies -> bean is used -> run destroy callbacks
```

Interviewers usually care because lifecycle explains:

- why constructor code may be too early for some initialization
- when `@PostConstruct` and `@PreDestroy` run
- why `@Transactional` works through proxies
- why prototype beans are not destroyed by Spring

---

## Constructor vs `@PostConstruct`

Constructor: use it to receive required dependencies and set fields.

`@PostConstruct`: use it when initialization needs already-injected dependencies.

```java
@Component
public class CacheWarmer {
    private final ProductRepository repository;

    public CacheWarmer(ProductRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void warmUp() {
        repository.findTopProducts().forEach(cache::put);
    }
}
```

With constructor injection, dependencies are available in the constructor. `@PostConstruct` is still useful for startup work that should run after Spring finishes wiring the bean.

Keep heavy startup work small. A slow `@PostConstruct` slows application startup.

---

## `@PreDestroy`

Use `@PreDestroy` to release resources held by a Spring-managed singleton bean when the application shuts down.

```java
@Component
public class FileImportWorker {
    private ExecutorService executor;

    @PostConstruct
    public void start() {
        executor = Executors.newSingleThreadExecutor();
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }
}
```

`@PreDestroy` is tied to **bean instances**, not just classes. Spring calls it only for objects it created and manages as beans. If you create an object yourself with `new`, Spring will not call its `@PreDestroy` method.

Main interview use cases:

- Stop background threads or executors.
- Close network clients, connection pools, file handles, or messaging clients.
- Flush buffered logs, metrics, audit events, or queued messages.
- Release distributed locks or mark a worker instance offline.
- Stop accepting work and cleanly finish/cancel in-flight work.

The OS eventually reclaims process memory and file descriptors, but `@PreDestroy` is about clean application shutdown before the process disappears.

Prototype gotcha: Spring creates prototype beans, but does not track and destroy them later. If a prototype owns a resource, the caller must close it.

---

## Proxies and lifecycle

Spring features like `@Transactional`, `@Async`, and `@Cacheable` usually work by wrapping your bean in a proxy.

```java
@Service
public class PaymentService {
    @Transactional
    public void capturePayment(Order order) {
        // transaction starts before this method and commits/rolls back after it
    }
}
```

Interview point: the object you inject may be a Spring proxy around your real class. That is why calls coming from another bean can trigger `@Transactional`, but self-invocation usually does not:

```java
public void outer() {
    inner(); // same object call, bypasses proxy
}

@Transactional
public void inner() {
}
```

The lifecycle detail worth knowing: proxies are created after the bean itself is initialized, before other beans use it.

---

## BeanPostProcessor in one paragraph

`BeanPostProcessor` is the extension point Spring uses to intercept bean creation before and after init callbacks. Application code rarely writes one, but knowing it exists explains many Spring features.

| Feature | Why lifecycle matters |
| --- | --- |
| `@Autowired` | Spring processes injection metadata while building beans |
| `@PostConstruct` / `@PreDestroy` | Spring detects and calls lifecycle annotations |
| `@Transactional` / `@Async` / `@Cacheable` | Spring can replace the bean with a proxy |

Interview line: you usually do not implement `BeanPostProcessor`, but Spring uses it heavily under the hood.

---

## `@Bean(initMethod/destroyMethod)`

For third-party classes, you cannot add `@PostConstruct` or `@PreDestroy` to the class. Use `@Bean` lifecycle attributes.

```java
@Configuration
public class ClientConfig {
    @Bean(initMethod = "connect", destroyMethod = "close")
    public ExternalClient externalClient() {
        return new ExternalClient();
    }
}
```

Use annotations when you own the class. Use `initMethod` / `destroyMethod` when configuration creates a library object.

---

## `@DependsOn`

Spring already creates dependencies first when they are injected through constructors.

Use `@DependsOn` only when one bean depends on another bean's side effect, not its object reference.

```java
@Bean
@DependsOn("flyway")
public ReportRepository reportRepository() {
    return new ReportRepository();
}
```

Use case: a migration/setup bean must run before another bean starts. If you need `@DependsOn` often, prefer making the dependency explicit through constructor injection.

---

## What to avoid

- Do not put business logic in lifecycle callbacks.
- Do not use lifecycle callbacks to hide missing dependencies.
- Do not rely on prototype `@PreDestroy`.
- Do not implement Spring lifecycle interfaces in app code unless there is a real framework-level reason.
- Do not use self-invocation and expect proxy annotations like `@Transactional` to fire.

---

## Quick recall

**Q. Basic singleton lifecycle?**
A. Create object, inject dependencies, run init callbacks, apply proxies, use bean, run destroy callbacks on shutdown.

**Q. When use `@PostConstruct`?**
A. Startup initialization that needs dependencies already wired.

**Q. When use `@PreDestroy`?**
A. Cleanup for singleton beans when the context shuts down: stop executors, flush buffers, close clients/pools, release locks.

**Q. Is `@PreDestroy` tied to a class or a bean?**
A. A Spring-managed bean instance. Spring only calls it for objects in the application context.

**Q. Are prototype beans destroyed by Spring?**
A. No. Spring creates them and hands them off; caller owns cleanup.

**Q. Why does `@Transactional` sometimes fail on self-invocation?**
A. The call bypasses the Spring proxy.

**Q. What is `BeanPostProcessor` useful for knowing?**
A. It explains how Spring applies lifecycle annotations and creates proxies for features like `@Transactional`.

**Q. `@PostConstruct` vs `@Bean(initMethod)`?**
A. Use `@PostConstruct` when you own the class; use `initMethod` for third-party objects built in config.

**Q. When use `@DependsOn`?**
A. Rarely, when a bean depends on another bean's startup side effect.
