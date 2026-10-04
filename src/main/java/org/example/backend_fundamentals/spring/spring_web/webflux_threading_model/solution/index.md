---
order: 20
search: false
---

# Solution

## Solution: event-loop-starvation - Diagnosing and Isolating Blocking Calls in WebFlux

### 1. Root Cause Analysis
*   **Netty Thread Sizing:** In Netty, worker threads default to `availableProcessors() * 2`. On an 8-core CPU pod, exactly 16 `reactor-http-nio-*` threads are instantiated to handle all incoming socket I/O and reactive pipeline steps.
*   **Event Loop Starvation:** When 16 concurrent requests enter the `flatMap` operator and invoke `fraudService.verify()`, each thread blocks for 250ms waiting for the external HTTP response.
*   **Socket Accept Stalling:** Because all 16 event loop threads are frozen, the Netty selector loop cannot service new I/O events, register new incoming connections, or read request bodies from the OS network buffers.
*   **Health Check Timeout:** The `/actuator/health` endpoint shares the same Netty `EventLoopGroup`. When Kubernetes sends an HTTP GET to `/actuator/health`, the TCP handshake sits unacknowledged in the kernel's accept queue. The probe times out, and Kubernetes restarts the container even though CPU usage is near 0%.

### 2. Refactored Implementation

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final LegacyFraudService fraudService;
    private final OrderRepository orderRepository;

    public OrderController(LegacyFraudService fraudService, OrderRepository orderRepository) {
        this.fraudService = fraudService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public Mono<Order> submitOrder(@RequestBody OrderRequest request) {
        return orderRepository.save(new Order(request))
                .flatMap(order -> verifyFraudIsolated(order.getUserId(), order.getAmount())
                        .flatMap(isFraudulent -> {
                            if (isFraudulent) {
                                return Mono.error(new FraudulentOrderException("Order flagged as fraud"));
                            }
                            return Mono.just(order);
                        })
                );
    }

    // Isolated blocking call offloaded to boundedElastic scheduler
    private Mono<Boolean> verifyFraudIsolated(String userId, BigDecimal amount) {
        return Mono.fromCallable(() -> fraudService.verify(userId, amount))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
```

### 3. Scheduler Selection Justification
*   **Why `Schedulers.boundedElastic()` is correct:**
    *   Designed specifically for blocking I/O (legacy JDBC, third-party blocking HTTP clients, file operations).
    *   It dynamically allocates worker threads up to a bounded cap (default: `10 * availableProcessors()`, or 80 threads on an 8-core machine).
    *   It enqueues up to 100,000 tasks if threads are busy, and evicts idle threads after a 60-second TTL.
    *   The 16 Netty event loop threads remain completely non-blocking and free to process health checks and high-volume traffic.
*   **Why `Schedulers.parallel()` is dangerous:**
    *   `Schedulers.parallel()` maintains a fixed pool sized strictly to the number of CPU cores (`availableProcessors()`, 8 threads on an 8-core machine).
    *   It is intended strictly for non-blocking, CPU-bound computations (e.g., encryption, compression, JSON parsing).
    *   Blocking threads in `Schedulers.parallel()` starves all internal Reactor parallel tasks across the entire JVM.

### 4. Automated Prevention via BlockHound
Integrate **BlockHound** into your project to prevent event loop blocking regressions:

```xml
<!-- Maven Dependency -->
<dependency>
    <groupId>io.projectreactor.tools</groupId>
    <artifactId>blockhound</artifactId>
    <version>1.0.9.RELEASE</version>
    <scope>test</scope>
</dependency>
```

In your unit/integration test bootstrap:

```java
@BeforeAll
static void setUp() {
    BlockHound.install();
}
```

BlockHound uses byte-code instrumentation to detect blocking calls (like `SocketInputStream.read()`, `Thread.sleep()`, or `ReentrantLock.lock()`) executing on threads that implement `reactor.core.scheduler.NonBlocking` (such as Netty's `reactor-http-nio-*`). If detected, it immediately terminates the execution and throws a descriptive `BlockingOperationError` during test runs.

---

## Solution: subscribe-on-vs-publish-on - Thread Hopping Pipeline Design

### 1. Pipeline Implementation

```java
@Service
public class DocumentIngestionService {

    private final WebClient verificationWebClient;
    private final LegacyAuditDao legacyAuditDao;

    public DocumentIngestionService(WebClient.Builder webClientBuilder, LegacyAuditDao legacyAuditDao) {
        this.verificationWebClient = webClientBuilder.baseUrl("https://verifier.internal").build();
        this.legacyAuditDao = legacyAuditDao;
    }

    public Mono<DocumentReceipt> processDocument(DocumentRequest request) {
        return Mono.just(request)
                // Stage 1: Validate payload (runs on Netty event loop)
                .map(this::validatePayload)
                // Stage 2: Offload heavy CPU encryption & hashing to parallel scheduler
                .publishOn(Schedulers.parallel())
                .map(this::computeEncryptionAndHashes)
                // Stage 3: Non-blocking remote WebClient call
                .flatMap(encryptedDoc -> verificationWebClient.post()
                        .uri("/verify")
                        .bodyValue(encryptedDoc)
                        .retrieve()
                        .bodyToMono(VerificationResult.class)
                        .map(res -> new VerifiedDocument(encryptedDoc, res))
                )
                // Stage 4: Offload blocking JDBC audit write to boundedElastic scheduler
                .publishOn(Schedulers.boundedElastic())
                .map(verifiedDoc -> {
                    legacyAuditDao.saveAuditRecord(verifiedDoc.id(), verifiedDoc.verificationStatus());
                    return verifiedDoc;
                })
                // Stage 5: Transform to receipt (emitted to Netty response pipeline)
                .map(verifiedDoc -> new DocumentReceipt(verifiedDoc.id(), "ACCEPTED"));
    }

    private DocumentRequest validatePayload(DocumentRequest req) {
        if (req.payload() == null || req.payload().length == 0) {
            throw new IllegalArgumentException("Payload cannot be empty");
        }
        return req;
    }

    private EncryptedDocument computeEncryptionAndHashes(DocumentRequest req) {
        // Heavy SHA-512 and AES-256 computation (~80ms CPU)
        byte[] encryptedBytes = AesUtil.encrypt(req.payload());
        String sha512 = HashUtil.sha512(req.payload());
        return new EncryptedDocument(req.id(), encryptedBytes, sha512);
    }
}
```

### 2. Thread Allocation Trace

| Stage | Operation | Executing Thread Pool | Justification |
| :--- | :--- | :--- | :--- |
| **Stage 1** | Ingest & Validate | `reactor-http-nio-*` | Fast, in-memory validation; stays on caller Netty thread without overhead of context switch. |
| **Stage 2** | CPU Hashing & AES | `parallel-*` | Switched via `publishOn(Schedulers.parallel())`; prevents 80ms CPU crunch from freezing Netty I/O channels. |
| **Stage 3** | Remote WebClient Call | `reactor-http-nio-*` | `WebClient` registers non-blocking socket channel; response resume signal emits on Netty client event loop. |
| **Stage 4** | Legacy JDBC Audit | `boundedElastic-*` | Switched via `publishOn(Schedulers.boundedElastic())`; isolates 30ms blocking JDBC call from both Netty and CPU pools. |
| **Stage 5** | Response Creation | `boundedElastic-*` → `reactor-http-nio-*` | Transformation executes on current thread, and Netty writes final bytes to HTTP channel. |

### 3. Operator Distinction: Why `subscribeOn` Alone Fails
*   `subscribeOn` dictates the thread that initiates the subscription and runs the data source emission all the way upstream.
*   If we placed `.subscribeOn(Schedulers.boundedElastic())` at the bottom:
    *   The entire upstream pipeline (including the 80ms CPU encryption task) would run on a `boundedElastic` thread.
    *   `boundedElastic` threads are meant for sleeping/waiting I/O, not intensive CPU bound loops.
    *   More critically, `subscribeOn` cannot perform intermediate thread switching. It cannot switch from `parallel` for computation to `boundedElastic` for database I/O. Intermediate switches require `publishOn`.

---

## Solution: context-propagation - Passing Correlation ID Without ThreadLocal

### 1. Failure Mechanism of `ThreadLocal` in WebFlux
*   **Thread Hopping:** In WebFlux, a single request does not occupy a dedicated thread. A request enters on `reactor-http-nio-1`, switches to `parallel-2` for computation, waits for remote I/O, and completes on `reactor-http-nio-3`.
*   **Thread Reuse & Leakage:** Because Netty worker threads are continuously reused across interleaved requests, a `ThreadLocal` value set during Request 1 will either:
    1. Be absent (`null`) when Request 1 resumes on a different thread.
    2. Be erroneously read by Request 2 when Request 2 executes on the same thread before Request 1 clears it.

### 2. Reactive `WebFilter` Implementation

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add header to response for client tracing
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        final String finalCorrelationId = correlationId;
        return chain.filter(exchange)
                // Attach correlationId to the Reactor Context (flows upstream)
                .contextWrite(Context.of(CORRELATION_ID_KEY, finalCorrelationId));
    }
}
```

### 3. Accessing Context in Downstream Logic

```java
@Service
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    public Mono<PaymentResult> processPayment(PaymentRequest request) {
        return Mono.deferContextual(contextView -> {
            String correlationId = contextView.getOrDefault("correlationId", "UNKNOWN");
            log.info("[CorrelationId: {}] Processing payment for amount: {}", correlationId, request.amount());

            return executePaymentGateway(request)
                    .doOnSuccess(res -> log.info("[CorrelationId: {}] Payment successful: {}", correlationId, res.txId()));
        });
    }

    private Mono<PaymentResult> executePaymentGateway(PaymentRequest req) {
        return Mono.just(new PaymentResult(UUID.randomUUID().toString(), "SUCCESS"));
    }
}
```

### 4. Spring Boot 3 & Micrometer Context Propagation
To eliminate manual `deferContextual` boilerplates and allow standard SLF4J MDC (`log.info(...)` with `%X{correlationId}`) to work seamlessly:

1. **Add Dependency:**
   ```xml
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>context-propagation</artifactId>
       <version>1.1.1</version>
   </dependency>
   ```

2. **Register ThreadLocal Accessor:**
   Configure Micrometer's `ContextRegistry` to map between MDC and Reactor Context:
   ```java
   @Configuration
   public class ContextPropagationConfig {

       @PostConstruct
       public void init() {
           // Enables automatic snapshotting and restoration across thread boundaries
           Hooks.enableAutomaticContextPropagation();
       }
   }
   ```

3. **How It Works Under the Hood:**
   When `Hooks.enableAutomaticContextPropagation()` is enabled, Project Reactor wraps operator scheduling points. When a thread hop occurs (e.g., via `publishOn` or `flatMap`), Reactor snapshots the `ThreadLocalAccessor` entries from the current thread into the Reactor `Context`, and upon landing on the new thread, restores the `ThreadLocal` values before user code executes, clearing them once the operator completes.
