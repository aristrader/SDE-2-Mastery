---
order: 20
search: false
---

# Solutions: @Async and Thread Context Propagation

## Solution: context-copying-task-decorator - Implement Context-Propagating TaskDecorator with Safe Cleanup

### 1. Root Cause
- **Context Loss:** `MDC` and `TenantContext` rely on `ThreadLocal` storage. When Spring dispatches execution to a `ThreadPoolTaskExecutor`, a separate worker thread executes the task. Because thread-local state is bound to the submitting thread, the worker thread begins with an empty context.
- **Tenant Leakage:** Thread pools reuse threads across multiple tasks. If a task sets a `ThreadLocal` on a pooled worker thread and fails to clear it before completion, that thread retains the previous tenant ID. When the thread is subsequently assigned a task from another request, it executes with stale tenant context.

### 2. Implementation: `MdcTenantTaskDecorator`

```java
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import java.util.Map;

public class MdcTenantTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // 1. Snapshot caller thread context before leaving submitting thread
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        String tenantId = TenantContext.getTenantId();

        return () -> {
            try {
                // 2. Install captured context on worker thread
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                } else {
                    MDC.clear();
                }

                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                } else {
                    TenantContext.clear();
                }

                // 3. Execute target async task
                runnable.run();
            } finally {
                // 4. MANDATORY: Clean up worker thread to prevent cross-request leakage
                MDC.clear();
                TenantContext.clear();
            }
        };
    }
}
```

### 3. ThreadPoolTaskExecutor Configuration

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncExecutorConfig {

    @Bean("fulfillmentExecutor")
    public ThreadPoolTaskExecutor fulfillmentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("order-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new MdcTenantTaskDecorator());
        executor.initialize();
        return executor;
    }
}
```

---

## Solution: security-context-async-propagation - Resolve SecurityContext Loss and Thread-Pool Mode Pitfall

### 1. Why `MODE_INHERITABLETHREADLOCAL` Fails with Thread Pools
`InheritableThreadLocal` copies variables from parent to child thread **only when a new thread is initialized** (`new Thread()`).
In a managed `ThreadPoolTaskExecutor`:
- Threads are instantiated early (up to `corePoolSize`) and reused to process tasks from an internal queue.
- When an HTTP request submits a task to an existing worker thread, no new thread is created; hence `InheritableThreadLocal` does not copy the caller's `SecurityContext`.
- If an idle worker thread happened to be created while User A was executing, it will retain User A's `SecurityContext` indefinitely. If User B's request subsequently reuses that pooled thread, User B will run with User A's privileges (privilege escalation).
- Therefore, `MODE_INHERITABLETHREADLOCAL` works only when each task spawns a new thread (`new Thread().start()`), which is strictly forbidden in pooled production architectures.

### 2. Implementation: `SecurityContextTaskDecorator`

```java
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Snapshot authentication on caller thread
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return () -> {
            try {
                if (authentication != null) {
                    // Create fresh context instead of mutating existing reference
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                }
                runnable.run();
            } finally {
                // Ensure worker thread is wiped clean
                SecurityContextHolder.clearContext();
            }
        };
    }
}
```

### 3. Why `SecurityContextHolder.createEmptyContext()` is Required
Calling `SecurityContextHolder.getContext().setAuthentication(...)` mutates whatever `SecurityContext` object is currently bound to the thread. If the caller thread and worker thread share the same `SecurityContext` instance reference, concurrent modifications or async context clearing can mutate or nullify the caller thread's authentication while the HTTP request is still in-flight.
`SecurityContextHolder.createEmptyContext()` ensures that the worker thread receives an isolated, independent `SecurityContext` container.

---

## Solution: async-uncaught-exception-handler - Configure Custom Executor and Uncaught Exception Handler

### 1. Error Propagation: `void` vs `CompletableFuture<T>`
- **`@Async void`**: The caller fire-and-forgets the task without retaining a reference to the execution handle. Because there is no `Future` object to capture exceptions, any unchecked exception thrown inside the method escapes to the executor. Without an `AsyncUncaughtExceptionHandler`, Spring simply logs the unhandled exception at `ERROR` level (or drops it depending on logger configuration) without alerting callers.
- **`@Async CompletableFuture<T>`**: The method returns a handle immediately. Any exception thrown within the async execution is captured by the future and stored as the completion failure. The caller or downstream consumer handles the failure reactively using `.exceptionally(...)`, `.handle(...)`, or synchronously when resolving `.join()` / `.get()`.

### 2. Configuration: `AsyncConfiguration` with `AsyncConfigurer`

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
            log.error("Uncaught async exception in method: {} with parameters: {}",
                    method.getName(),
                    Arrays.toString(params),
                    throwable);
            // Optional: Publish to alert topic, Prometheus error counter, or Dead Letter Queue
        }
    }
}
```

### 3. Observability Comparison
| Feature | `@Async void` | `@Async CompletableFuture<T>` |
|---|---|---|
| **Exception Destination** | `AsyncUncaughtExceptionHandler` | Encapsulated in `CompletableFuture` |
| **Caller Visibility** | None (fire-and-forget) | Direct via `.exceptionally()` or `.get()` |
| **Retry / Recovery** | Requires custom logic inside handler or task | Direct chaining via reactive pipeline |
| **Use Case Recommendation** | Non-critical best-effort fire-and-forget (e.g. metrics) | Business operations requiring outcome verification |
