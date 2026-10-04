---
order: 10
search: false
---

# Exercises: @Async and Thread Context Propagation

Practice designing, implementing, and debugging thread context propagation, `TaskDecorator` mechanisms, thread pool lifecycle traps, and async exception handling in Spring applications.

---

## Exercise: context-copying-task-decorator - Implement Context-Propagating TaskDecorator with Safe Cleanup

### Problem Statement
In a multi-tenant order processing service, incoming HTTP requests pass through an authentication and tracing filter that sets:
1. `MDC` with `traceId` and `spanId`.
2. A custom `TenantContext` storing the authenticated customer's `tenantId` in a `ThreadLocal<String>`.

Order fulfillment runs asynchronously via `@Async("fulfillmentExecutor") public CompletableFuture<Void> fulfillOrder(Order order)`. During load testing, two major defects are detected:
- Async logs from `fulfillOrder` have no `traceId` or `spanId`, making distributed tracing impossible.
- Worker threads reused by the pool occasionally process tasks for Tenant B while `TenantContext` still holds Tenant A's identifier from a prior completed job, leading to cross-tenant data corruption.

```java
public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) { CURRENT_TENANT.set(tenantId); }
    public static String getTenantId() { return CURRENT_TENANT.get(); }
    public static void clear() { CURRENT_TENANT.remove(); }
}
```

### Requirements
1. Implement a Spring `TaskDecorator` class named `MdcTenantTaskDecorator`.
2. Capture the caller thread's MDC context map (handling the possibility of `null`) and current tenant ID inside `decorate(Runnable runnable)`.
3. Wrap the runnable such that the captured context is applied to the worker thread before `runnable.run()` is called.
4. Ensure all thread-local state is cleared in a `finally` block to prevent cross-request contamination in pooled threads.
5. Provide the `@Bean` definition for `ThreadPoolTaskExecutor` configuring core size 4, max size 16, queue capacity 200, thread name prefix `"order-async-"`, `CallerRunsPolicy`, and the custom decorator.

---

## Exercise: security-context-async-propagation - Resolve SecurityContext Loss and Thread-Pool Mode Pitfall

### Problem Statement
A banking application exports quarterly financial reports asynchronously:

```java
@Service
public class StatementService {

    @Async("statementExecutor")
    public CompletableFuture<byte[]> generateStatement(String accountId) {
        // Calls auditService which checks @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
        return CompletableFuture.completedFuture(statementGenerator.buildPdf(accountId));
    }
}
```

When invoked, the async method throws `org.springframework.security.authorization.AuthorizationDeniedException: Access Denied` because `SecurityContextHolder.getContext().getAuthentication()` returns `null` on the worker thread.

A junior developer proposes fixing this by adding:
```java
SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
```

### Requirements
1. Explain why `MODE_INHERITABLETHREADLOCAL` appears to work in low-traffic local tests but causes silent privilege escalation or authorization failures under production thread-pool load.
2. Implement a `SecurityContextTaskDecorator` implementing `TaskDecorator` that safely propagates the caller's `Authentication`.
3. Explain why `SecurityContextHolder.createEmptyContext()` must be used on the executor thread rather than calling `SecurityContextHolder.getContext().setAuthentication(...)` directly.
4. Guarantee that `SecurityContextHolder.clearContext()` executes in a `finally` block on the worker thread.

---

## Exercise: async-uncaught-exception-handler - Configure Custom Executor and Uncaught Exception Handler

### Problem Statement
A notification engine sends background webhook notifications to external web endpoints using an `@Async` method returning `void`:

```java
@Service
public class WebhookDeliveryService {

    @Async
    public void deliverWebhook(String endpoint, WebhookPayload payload) {
        restClient.post()
                .uri(endpoint)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
```

When an external endpoint is unreachable or times out, the `RestClientException` is silently dropped. The caller receives no error, no log message appears in the error logs, and monitoring dashboards report zero failed deliveries.

### Requirements
1. Explain why `@Async void` methods silently swallow exceptions by default while `@Async CompletableFuture<T>` methods do not.
2. Implement an application configuration class `AsyncConfiguration` implementing `AsyncConfigurer`.
3. Configure `getAsyncExecutor()` to return a bounded `ThreadPoolTaskExecutor`.
4. Implement `getAsyncUncaughtExceptionHandler()` to log the method name, parameter values, and exception details when an uncaught exception is thrown.
5. Contrast the exception observability model of `void` vs `CompletableFuture<T>` and describe how callers handle exceptions when returning `CompletableFuture<T>`.
