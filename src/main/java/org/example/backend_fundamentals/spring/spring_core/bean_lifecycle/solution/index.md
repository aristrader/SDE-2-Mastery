---
order: 20
search: false
---

# Solution

---

## Solution: self-invocation-proxy-lifecycle - Self-Invocation and Lifecycle Proxy Bypass

### 1. Root Cause Analysis
During Spring bean initialization, `BeanPostProcessor` implementations (such as `AsyncAnnotationBeanPostProcessor` and `InfrastructureAdvisorAutoProxyCreator`) wrap bean instances in dynamic proxies during `postProcessAfterInitialization`.

When external components invoke methods on `OrderProcessingService`, they call methods on the outer Spring proxy, which intercepts the call and applies transaction boundaries and async thread delegation. However, when `processOrder()` calls `chargeCard()` or `sendConfirmationEmail()` directly, Java evaluates the call as `this.chargeCard(...)`. This self-invocation targets the unproxied raw instance directly, completely bypassing the Spring proxy interceptor chain.

### 2. Recommended Architectural Fix (Collaborating Beans)
Separate distinct business and infrastructure responsibilities into dedicated collaborating Spring beans:

```java
@Service
public class PaymentService {
    @Transactional
    public void chargeCard(Order order) {
        // executes within transaction proxy
    }
}

@Service
public class NotificationService {
    @Async
    public void sendConfirmationEmail(Order order) {
        // executes asynchronously via task executor proxy
    }
}

@Service
public class OrderProcessingService {
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public OrderProcessingService(PaymentService paymentService, NotificationService notificationService) {
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    public void processOrder(Order order) {
        paymentService.chargeCard(order);
        notificationService.sendConfirmationEmail(order);
    }
}
```

### 3. Alternative Single-Bean Fix (Self-Injection via ObjectProvider / @Lazy)
If keeping logic in a single class is necessary, inject the proxied bean instance into itself:

```java
@Service
public class OrderProcessingService {

    private final ObjectProvider<OrderProcessingService> selfProvider;

    public OrderProcessingService(ObjectProvider<OrderProcessingService> selfProvider) {
        this.selfProvider = selfProvider;
    }

    public void processOrder(Order order) {
        OrderProcessingService proxiedSelf = selfProvider.getObject();
        proxiedSelf.chargeCard(order);            // routes through @Transactional proxy
        proxiedSelf.sendConfirmationEmail(order); // routes through @Async proxy
    }

    @Transactional
    public void chargeCard(Order order) { ... }

    @Async
    public void sendConfirmationEmail(Order order) { ... }
}
```

---

## Solution: prototype-lifecycle-resource-leak - Prototype Bean Resource Cleanup and Destruction

### 1. Root Cause Analysis
According to the Spring Framework specification:
- Spring manages the complete lifecycle of **singleton** beans (creation, wiring, initialization, and destruction).
- For **prototype** beans, Spring instantiates, configures, and hands the instance to the caller, but **does not track or hold references to prototype instances**.
- Consequently, Spring **never invokes `@PreDestroy` or `DisposableBean.destroy()` on prototype beans**.

In `ChunkIngestionService`, each file chunk creates a new `CsvDataParser` containing a 4-thread `ExecutorService`. Because `@PreDestroy` is never called, each iteration leaves active unclosed worker threads running. These uncollected threads and their thread-stack allocations rapidly cause thread exhaustion and JVM OutOfMemoryError (`java.lang.OutOfMemoryError: unable to create new native thread`).

### 2. Refactored Solution (Explicit Lifecycle / AutoCloseable)
Implement `AutoCloseable` on the prototype bean and manage its lifecycle with a standard Java `try-with-resources` block:

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CsvDataParser implements AutoCloseable {

    private final ExecutorService parsingPool = Executors.newFixedThreadPool(4);

    public void parseChunk(byte[] chunkData) {
        // multithreaded parsing
    }

    @Override
    public void close() {
        parsingPool.shutdown();
    }
}
```

In the consumer service:

```java
@Service
public class ChunkIngestionService {

    private final ObjectProvider<CsvDataParser> parserProvider;

    public ChunkIngestionService(ObjectProvider<CsvDataParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    public void processFile(List<byte[]> chunks) {
        for (byte[] chunk : chunks) {
            try (CsvDataParser parser = parserProvider.getObject()) {
                parser.parseChunk(chunk);
            } // guarantees parser.close() is executed deterministically
        }
    }
}
```

*(Note: Alternatively, you can use `ConfigurableBeanFactory#destroyBean(parser)` if framework-level destruction post-processors are required).*

---

## Solution: postconstruct-proxy-ordering - Initialization Callbacks vs Proxy and Event Timing

### 1. Root Cause Analysis
In the Spring bean lifecycle:
1. `postProcessBeforeInitialization` executes.
2. Initialization callbacks execute (`@PostConstruct`, `InitializingBean.afterPropertiesSet`).
3. `postProcessAfterInitialization` executes (where AOP proxies for `@Async`, `@Transactional`, and `@Validated` are created).

When `@PostConstruct` executes:
- The bean instance is still raw and unproxied; the proxy wrapping occurs only *after* `@PostConstruct` returns.
- Inside `@PostConstruct`, `this.fetchRatesAsync()` is a direct method call on the unproxied instance, bypassing the asynchronous execution interceptor.

### 2. Robust Solution via `ApplicationReadyEvent`
Listen for `ApplicationReadyEvent` or `ContextRefreshedEvent`. At this phase, all beans are fully initialized, proxied, and ready in the `ApplicationContext`:

```java
@Component
public class CurrencyRateManager {

    private final Map<String, BigDecimal> exchangeRates = new ConcurrentHashMap<>();
    private final RateExchangeClient client;

    public CurrencyRateManager(RateExchangeClient client) {
        this.client = client;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // Triggered after context initialization; calls pass through the @Async proxy
        fetchRatesAsync();
    }

    @Async
    public CompletableFuture<Void> fetchRatesAsync() {
        Map<String, BigDecimal> rates = client.fetchCurrentRates();
        exchangeRates.putAll(rates);
        return CompletableFuture.completedFuture(null);
    }
}
```
