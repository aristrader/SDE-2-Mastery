---
order: 10
search: false
---

# Exercise

## Exercise: event-loop-starvation - Diagnosing and Isolating Blocking Calls in WebFlux

### Context
You are a senior backend engineer maintaining a mission-critical Spring Boot WebFlux service deployed on an 8-core Kubernetes pod (`reactor.netty.ioWorkerCount` defaults to 16 threads). During peak traffic, you notice the `/actuator/health` endpoint intermittently times out, leading Kubernetes to terminate and restart healthy containers. An investigation traces the issue to a newly introduced fraud validation check:

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final LegacyFraudService fraudService; // Synchronous third-party HTTP client (blocks for 250ms)
    private final OrderRepository orderRepository; // Reactive R2DBC repository

    public OrderController(LegacyFraudService fraudService, OrderRepository orderRepository) {
        this.fraudService = fraudService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public Mono<Order> submitOrder(@RequestBody OrderRequest request) {
        return orderRepository.save(new Order(request))
                .flatMap(order -> {
                    // PROBLEM: Blocking call executed directly inside reactive pipeline
                    boolean isFraudulent = fraudService.verify(order.getUserId(), order.getAmount());
                    if (isFraudulent) {
                        return Mono.error(new FraudulentOrderException("Order flagged as fraud"));
                    }
                    return Mono.just(order);
                });
    }
}
```

### Requirements
1. **Root Cause Analysis:** Explain why 16 concurrent requests calling `fraudService.verify()` can completely freeze HTTP ingress for all other endpoints (including `/actuator/health`), even when CPU and RAM utilization are below 10%.
2. **Refactor Implementation:** Refactor `submitOrder` to isolate the blocking `fraudService.verify()` call so it never executes on a `reactor-http-nio-*` thread. Use the standard Project Reactor mechanism and select the appropriate `Scheduler`.
3. **Scheduler Selection Justification:** Explain why `Schedulers.boundedElastic()` is suitable for this operation whereas `Schedulers.parallel()` is dangerous.
4. **Automated Prevention:** Specify what testing library and JVM agent should be integrated into your CI suite to detect accidental blocking calls on event loop threads before code merges to production.

---

## Exercise: subscribe-on-vs-publish-on - Thread Hopping Pipeline Design

### Context
You are designing an asynchronous document ingestion pipeline in Spring WebFlux. The incoming request payload contains raw document bytes and metadata. The pipeline must execute four distinct stages:
1. **Ingest & Validation (Stage 1):** Read the HTTP request body and validate JSON schema headers (fast, non-blocking).
2. **Heavy Encryption & Hashing (Stage 2):** Compute SHA-512 signatures and AES-256 block encryption on the document bytes (CPU-intensive, ~80ms compute time).
3. **Remote Metadata Verification (Stage 3):** Call an external verification microservice using non-blocking Spring `WebClient`.
4. **Audit Log Persistence (Stage 4):** Persist an audit log entry to an internal compliance database using a legacy blocking JDBC driver (~30ms I/O latency).
5. **Response Emission (Stage 5):** Return `201 Created` with the document receipt.

### Requirements
1. **Pipeline Construction:** Write the reactive method `Mono<DocumentReceipt> processDocument(DocumentRequest request)` incorporating the correct placement of `publishOn` and `subscribeOn`.
2. **Thread Allocation Trace:** For each stage (1 through 5), identify the exact thread pool that executes it (`reactor-http-nio-*`, `parallel-*`, `boundedElastic-*`).
3. **Operator Distinction:** Explain why placing a single `subscribeOn(Schedulers.boundedElastic())` at the end of the chain fails to satisfy the architectural requirement of protecting both the event loop and the CPU cores.

---

## Exercise: context-propagation - Passing Correlation ID Without ThreadLocal

### Context
In a Spring MVC microservice architecture, distributed tracing correlation IDs are typically handled via a servlet `Filter` and SLF4J `MDC`:

```java
MDC.put("correlationId", correlationId);
try {
    chain.doFilter(request, response);
} finally {
    MDC.remove("correlationId");
}
```

A team migrated this filter directly to a Spring WebFlux `WebFilter`. In production, logs inside downstream operators either display `correlationId: null` or, worse, log the `correlationId` of an unrelated customer request.

### Requirements
1. **Failure Mechanism:** Explain why `ThreadLocal`-based MDC breaks across reactive stages and causes cross-tenant log corruption in WebFlux.
2. **WebFilter Implementation:** Write a reactive `WebFilter` that extracts `X-Correlation-ID` from the incoming `ServerWebExchange` headers (or generates a random UUID if absent) and attaches it to the Reactor `Context`.
3. **Accessing Context in Downstream Logic:** Show how a downstream reactive service reads this correlation ID using `Mono.deferContextual()` without relying on `ThreadLocal`.
4. **Spring Boot 3 / Micrometer Bridge:** Explain how to configure `io.micrometer:context-propagation` with `Hooks.enableAutomaticContextPropagation()` so standard `log.info("...")` statements automatically resolve the correlation ID in log patterns without manual contextual extraction.
