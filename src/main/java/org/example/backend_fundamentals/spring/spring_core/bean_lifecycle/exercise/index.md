---
order: 10
search: false
---

# Exercise

---

## Exercise: self-invocation-proxy-lifecycle - Self-Invocation and Lifecycle Proxy Bypass

In the following Spring Boot service, an engineer reports two critical bugs:
1. When `processOrder()` calls `chargeCard()`, no database transaction is opened or rolled back on failure.
2. When `processOrder()` calls `sendConfirmationEmail()`, the email is executed synchronously on the request thread instead of asynchronously in the background pool.

```java
@Service
public class OrderProcessingService {

    public void processOrder(Order order) {
        validateOrder(order);
        chargeCard(order);               // Bug 1: @Transactional does not start
        sendConfirmationEmail(order);    // Bug 2: @Async runs synchronously
    }

    private void validateOrder(Order order) {
        // validation logic
    }

    @Transactional
    public void chargeCard(Order order) {
        // debit customer account
    }

    @Async
    public void sendConfirmationEmail(Order order) {
        // external SMTP delivery
    }
}
```

### Tasks
1. Explain in terms of the Spring bean lifecycle (`BeanPostProcessor` dynamic proxying) why calling `chargeCard()` and `sendConfirmationEmail()` directly from `processOrder()` fails to trigger transaction management and asynchronous execution.
2. Provide the recommended architectural fix (refactoring into collaborating beans).
3. Provide an acceptable single-bean alternative (such as self-injection with `@Lazy` or `ObjectProvider`).

---

## Exercise: prototype-lifecycle-resource-leak - Prototype Bean Resource Cleanup and Destruction

A high-throughput ingestion service requests a new instance of `CsvDataParser` per file chunk using Spring's prototype scope:

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CsvDataParser {

    private final ExecutorService parsingPool = Executors.newFixedThreadPool(4);

    public void parseChunk(byte[] chunkData) {
        // multithreaded parsing
    }

    @PreDestroy
    public void close() {
        parsingPool.shutdown();
    }
}
```

The singleton consumer looks like:

```java
@Service
public class ChunkIngestionService {

    private final ObjectProvider<CsvDataParser> parserProvider;

    public ChunkIngestionService(ObjectProvider<CsvDataParser> parserProvider) {
        this.parserProvider = parserProvider;
    }

    public void processFile(List<byte[]> chunks) {
        for (byte[] chunk : chunks) {
            CsvDataParser parser = parserProvider.getObject();
            parser.parseChunk(chunk);
            // Developer assumes Spring will call @PreDestroy when parser is done
        }
    }
}
```

### Tasks
1. Explain why this design causes an out-of-memory error (OOM) and thread pool leak, detailing Spring's contract regarding prototype bean destruction.
2. Refactor `CsvDataParser` and `ChunkIngestionService` to guarantee deterministic resource cleanup without leaking thread pools.

---

## Exercise: postconstruct-proxy-ordering - Initialization Callbacks vs Proxy and Event Timing

A developer attempts to warm up a cache asynchronously during application startup using `@PostConstruct`:

```java
@Component
public class CurrencyRateManager {

    private final Map<String, BigDecimal> exchangeRates = new ConcurrentHashMap<>();
    private final RateExchangeClient client;

    public CurrencyRateManager(RateExchangeClient client) {
        this.client = client;
    }

    @PostConstruct
    public void init() {
        fetchRatesAsync(); // Developer expects this to run on background thread
    }

    @Async
    public CompletableFuture<Void> fetchRatesAsync() {
        Map<String, BigDecimal> rates = client.fetchCurrentRates();
        exchangeRates.putAll(rates);
        return CompletableFuture.completedFuture(null);
    }
}
```

### Tasks
1. Explain why calling `fetchRatesAsync()` inside `@PostConstruct` runs synchronously on the main startup thread instead of asynchronously in the task executor, referencing the exact timing of `BeanPostProcessor.postProcessAfterInitialization`.
2. Rewrite the initialization trigger to ensure `fetchRatesAsync()` executes properly through its Spring proxy once the application context is fully started.
