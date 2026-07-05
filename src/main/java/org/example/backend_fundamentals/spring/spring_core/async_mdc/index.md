---
order: 10
---

# @Async + Thread Context Propagation

---

## @Async basics

`@Async` on a method tells Spring to execute it on a different thread from the configured async executor. The calling thread returns immediately; the method runs asynchronously.

```java
@Service
public class NotificationService {

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendEmail(String to, String body) {
        // runs on notificationExecutor thread pool
        emailClient.send(to, body);
        return CompletableFuture.completedFuture(null);
    }
}
```

Requirements:
- `@EnableAsync` on a `@Configuration` class (or your `@SpringBootApplication` class).
- The method must be on a Spring-managed bean (not called from within the same class — self-invocation bypasses the AOP proxy).
- Return type: `void`, `Future<T>`, or `CompletableFuture<T>`. Avoid `void` unless you genuinely don't care about the result or exceptions.

Self-invocation gotcha: calling `this.sendEmail(...)` from within `NotificationService` bypasses the proxy, so `@Async` has no effect. Inject the bean into itself or extract to a separate bean.

---

## The propagation problem

`MDC`, `SecurityContextHolder`, and any custom `ThreadLocal` are **per-thread**. When Spring hands your task to a thread pool thread, that thread has a blank context:

| Context | Storage | Default async behavior |
|---|---|---|
| SLF4J MDC (correlation ID, trace ID) | `ThreadLocal` | Lost — async logs have no correlation ID |
| `SecurityContextHolder` | `ThreadLocal` (default mode) | Lost — `@PreAuthorize` in async methods fails |
| Custom tenant context | `ThreadLocal` | Lost — multi-tenant queries use wrong tenant |

This is a silent failure: the code compiles and runs, but your logs are untracked and your security checks are broken on async paths.

---

## TaskDecorator — the canonical fix

`ThreadPoolTaskExecutor` exposes a `TaskDecorator` hook: a `Runnable` wrapper that runs on the **submitting thread** (where context is available), capturing state and injecting it into the **executor thread** before the task runs.

```java
@Bean("notificationExecutor")
public ThreadPoolTaskExecutor notificationExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("notify-");
    executor.setTaskDecorator(new ContextCopyingDecorator());
    executor.initialize();
    return executor;
}
```

```java
public class ContextCopyingDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Captured on the CALLER'S thread — context is still valid here
        Map<String, String> mdcCopy = MDC.getCopyOfContextMap();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String tenantId = TenantContext.getCurrentTenant();

        return () -> {
            try {
                MDC.setContextMap(mdcCopy != null ? mdcCopy : Collections.emptyMap());
                SecurityContextHolder.getContext().setAuthentication(auth);
                TenantContext.setCurrentTenant(tenantId);
                runnable.run();
            } finally {
                // MANDATORY — executor threads are pooled and reused
                MDC.clear();
                SecurityContextHolder.clearContext();
                TenantContext.clear();
            }
        };
    }
}
```

The `finally` block is not optional. Without it, the executor thread carries the previous request's context into the next task it picks up from the queue — a cross-request context leak that is extremely hard to reproduce and diagnose.

---

## SecurityContextHolder propagation modes

`SecurityContextHolder` has three modes for how it stores the `SecurityContext`:

| Mode | Storage | Async behavior |
|---|---|---|
| `MODE_THREADLOCAL` (default) | `ThreadLocal` | Not inherited — async thread is blank |
| `MODE_INHERITABLETHREADLOCAL` | `InheritableThreadLocal` | Inherited by child threads (spawned via `new Thread()`) |
| `MODE_GLOBAL` | Static field | Shared across all threads — dangerous in multi-user apps |

`MODE_INHERITABLETHREADLOCAL` sounds like a fix but does NOT work with thread pools. Pool threads are created once and reused — they are not child threads of each submitter. `InheritableThreadLocal` copies only at thread creation, so pool threads never see the caller's context.

For thread-pool-based async, the correct solution is always `TaskDecorator`.

---

## Always define your own executor

Never rely on Spring's default `SimpleAsyncTaskExecutor` — it creates **a new thread per invocation**, no pooling, no queue, unbounded. Under any load this exhausts OS thread limits.

```java
// BAD — Spring default if no executor is configured
// SimpleAsyncTaskExecutor: 1 new thread per @Async call

// GOOD — explicit bean with sensible bounds
@Bean("defaultAsync")
public ThreadPoolTaskExecutor defaultAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
    executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("async-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setTaskDecorator(new ContextCopyingDecorator());
    executor.initialize();
    return executor;
}
```

Name your `@Async("beanName")` explicitly when you have multiple executors so each use case has appropriate sizing (e.g., I/O-heavy notification emails vs CPU-bound report generation).

---

## Error handling for @Async void methods

`@Async void` methods swallow all exceptions — the caller thread never sees them and they are silently dropped unless you add an `AsyncUncaughtExceptionHandler`:

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("Async method {} threw uncaught exception", method.getName(), ex);
            // alert, metrics, dead-letter, etc.
        };
    }
}
```

This handler does not apply to `CompletableFuture<T>` return types — those propagate the exception inside the future and the caller handles it with `.exceptionally(...)`. Prefer `CompletableFuture<T>` over `void` whenever you need observability.

---

## Interview gotchas

- Self-invocation bypasses the AOP proxy — `@Async` is silently ignored.
- `MODE_INHERITABLETHREADLOCAL` does not work with thread pools — a common wrong answer.
- `SimpleAsyncTaskExecutor` is not a thread pool — it threads-per-call.
- Not clearing ThreadLocal in `finally` = context leaks across pooled threads.
- `@Async` on a `private` method — Spring cannot proxy it; annotation is silently ignored.

---

## Quick recall

**Q. What context is lost when a method runs via @Async?**
A. All ThreadLocal state: MDC correlation IDs, SecurityContextHolder, custom tenant context — the executor thread starts blank.

**Q. What is a TaskDecorator and where does it run?**
A. A Runnable wrapper in `ThreadPoolTaskExecutor`; the wrapping code runs on the caller's thread (capturing context), and the wrapped runnable runs on the executor thread (injecting it).

**Q. Why is the `finally` block mandatory in a TaskDecorator?**
A. Executor threads are reused — without clearing, the previous request's context leaks into the next task on the same thread.

**Q. Does `MODE_INHERITABLETHREADLOCAL` fix the async propagation problem?**
A. No — it only copies context at thread creation time. Thread pool threads are created once and reused, not spawned per-submitter.

**Q. What happens to exceptions thrown by an `@Async void` method?**
A. They are silently swallowed unless you register an `AsyncUncaughtExceptionHandler`. Prefer `CompletableFuture<T>` for observability.

**Q. Why is `SimpleAsyncTaskExecutor` dangerous?**
A. It creates a new OS thread per invocation — no pooling, unbounded under load, exhausts thread limits quickly.

**Q. `@Async` on a method called from within the same class — what happens?**
A. Self-invocation bypasses the Spring AOP proxy; the method runs synchronously on the caller's thread as if `@Async` wasn't there.


<ExerciseNav />
