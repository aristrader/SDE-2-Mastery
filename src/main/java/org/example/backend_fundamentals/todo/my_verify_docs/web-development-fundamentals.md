# Web Development Fundamentals: From Browser to Database

A reference covering how a web request flows from the frontend through the backend to the database, and all the layers in between.

---

## The Big Picture

```
Browser/Mobile App
    ↓ HTTPS
Load Balancer (AWS ALB / Nginx / Istio)
    ↓ HTTP
Web Server (Undertow / Tomcat / Netty)
    ↓ Servlet / Reactive pipeline
Framework (Spring Boot)
    ↓ Method calls
Application Code (Controllers → Services → Repositories)
    ↓ JDBC
Connection Pool (HikariCP)
    ↓ TCP/MySQL protocol
Database (MySQL / PostgreSQL)
```

---

## 1. Frontend → Backend Communication

### SDK vs API

- **SDK (Software Development Kit):** A library that wraps API calls. The mobile SDK in this project captures liveness photos, ID card images, and sends them to the backend. The SDK handles retries, image compression, session management.
- **API (Application Programming Interface):** The raw HTTP endpoints. `POST /api/v1/verify` is the API. The SDK calls this API internally.

### HTTP Request Lifecycle

```
Client builds request (method, URL, headers, body)
    ↓
DNS resolves hostname → IP address
    ↓
TCP handshake (SYN → SYN-ACK → ACK)
    ↓
TLS handshake (if HTTPS — certificate exchange, key negotiation)
    ↓
HTTP request sent over the encrypted connection
    ↓
Server processes and returns HTTP response
    ↓
Connection kept alive (HTTP/1.1 keep-alive) or closed
```

### Key Concepts

| Term | What It Is |
|------|-----------|
| **REST** | Architectural style — stateless, resource-based URLs, HTTP methods (GET/POST/PUT/DELETE) |
| **JSON** | Data format for request/response bodies |
| **Headers** | Metadata — `Authorization: Bearer <JWT>`, `Content-Type: application/json` |
| **Status Codes** | 200 OK, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 500 Internal Server Error |
| **JWT** | JSON Web Token — signed token containing user claims, verified by the backend without calling the auth server |
| **OAuth2** | Authorization framework — client credentials, authorization code flow. This project uses client credentials for service-to-service (Feign) and JWT for user auth (KeyCloak) |

---

## 2. Web Servers

The web server accepts incoming HTTP connections and dispatches them to the application.

### Undertow (used in this project)

- Embedded in Spring Boot (alternative to Tomcat)
- Non-blocking I/O using XNIO library
- Thread model:
  - **I/O threads** — accept connections, read/write bytes (small pool, ~cores x 2)
  - **Worker threads** — execute the actual request handling (larger pool, configurable)
- The thread names you see in logs like `XNIO-1 task-2` are Undertow worker threads

### Tomcat (Spring Boot default)

- Blocking I/O by default (one thread per request)
- `server.tomcat.threads.max=200` — max concurrent requests
- Simpler model but uses more threads

### Netty (for reactive / WebFlux)

- Fully non-blocking, event-loop based
- Handles thousands of concurrent connections with few threads
- Used when you need `Mono<T>` / `Flux<T>` reactive types

### Which to Choose

| Use Case | Server |
|----------|--------|
| Traditional Spring MVC (this project) | Undertow or Tomcat |
| High concurrency, streaming | Netty with WebFlux |
| Simple REST APIs | Tomcat (default, least config) |

---

## 3. Spring Boot Request Processing

### The Request Pipeline

```
HTTP Request arrives at Undertow
    ↓
Servlet Filter Chain
  → RequestContextFilter (sets up request attributes)
  → SecurityFilterChain (JWT validation, authorization)
  → MyServiceContext filter (sets up thread-local context)
    ↓
DispatcherServlet
    ↓
HandlerMapping (finds the right @Controller method)
    ↓
HandlerInterceptors (pre-handle)
    ↓
@Controller method executes
    ↓
HandlerInterceptors (post-handle)
    ↓
HTTP Response sent back
```

### Key Spring Annotations

| Annotation | Purpose |
|------------|---------|
| `@RestController` | Combines `@Controller` + `@ResponseBody` — methods return JSON directly |
| `@RequestMapping` / `@PostMapping` | Maps URLs to methods |
| `@RequestBody` | Deserializes JSON request body to Java object |
| `@ResponseBody` | Serializes Java object to JSON response |
| `@Autowired` / `@RequiredArgsConstructor` | Dependency injection |
| `@Service` | Business logic layer bean |
| `@Repository` | Data access layer bean |
| `@Component` | Generic Spring-managed bean |
| `@Configuration` | Defines beans via `@Bean` methods |
| `@Transactional` | Wraps method in a database transaction |
| `@Async` | Runs method on a separate thread |
| `@Scheduled` | Runs method on a cron schedule |
| `@Value` | Injects property values |

---

## 4. Dependency Injection & Spring Context

### What It Solves

Without DI:
```java
// Every class creates its own dependencies — tightly coupled
public class VerifyApiService {
    private KycStorageFacade facade = new KycStorageFacade(
        new KycOperationRepository(...),
        new KycStatusRepository(...)
    );
}
```

With DI:
```java
// Spring creates and wires everything — loosely coupled
@RequiredArgsConstructor
public class VerifyApiService {
    private final KycStorageFacade facade; // Spring injects this
}
```

### How It Works

1. On startup, Spring scans for `@Component`, `@Service`, `@Repository`, `@Configuration` classes
2. Creates instances (beans) and puts them in the **Application Context** (a registry)
3. Resolves dependencies — if `VerifyApiService` needs `KycStorageFacade`, Spring looks it up in the context and injects it
4. Beans are **singletons** by default — one instance shared across the entire app

---

## 5. Database Access Layer

### The Stack

```
Application Code (@Service)
    ↓ calls
Spring Data JPA Repository (@Repository)
    ↓ generates
Hibernate (JPA Implementation / ORM)
    ↓ generates
SQL Queries
    ↓ sent via
HikariCP Connection Pool
    ↓ sends over
JDBC Driver (mysql-connector-java)
    ↓ TCP
MySQL Server
```

### ORM (Object-Relational Mapping)

Hibernate maps Java objects to database tables:

```java
@Entity
@Table(name = "kyc_status")
public class KycStatusEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id")
    private String groupId;
}
```

- `@Entity` → this class maps to a database table
- `@Table` → the table name
- `@Id` → primary key
- `@GeneratedValue(IDENTITY)` → auto-increment
- `@Column` → column name (optional if field name matches)

### Spring Data JPA

Generates SQL from method names:

```java
// Spring generates: SELECT * FROM kyc_status WHERE group_id = ?
Optional<KycStatusEntity> findByGroupId(String groupId);

// Spring generates: SELECT COUNT(*) > 0 FROM kyc_operation_attempts WHERE group_id = ? AND ...
boolean existsByGroupIdAndTransactionIdAndOperation(String groupId, String transactionId, String operation);
```

For complex queries, use `@Query`:
```java
@Query("UPDATE KycStatusEntity k SET k.overallStatus = :status WHERE k.groupId = :groupId")
void updateOverallStatus(@Param("groupId") String groupId, @Param("status") OverallStatus status);
```

### Connection Pool (HikariCP)

Opening a DB connection is expensive (~5-10ms — TCP handshake, MySQL authentication, SSL). HikariCP keeps a pool of open connections and lends them out:

```
Thread needs DB  →  borrow connection from pool  →  run query  →  return connection to pool
```

Default config:
- 10 connections in the pool
- Threads wait up to 30s if all connections are busy
- Idle connections kept alive for 10 min

---

## 6. Transactions

### What a Transaction Is

A transaction groups multiple SQL statements into one atomic unit:

```sql
BEGIN;
INSERT INTO kyc_operation_attempts (...) VALUES (...);
UPDATE kyc_status SET overall_status = 'VERIFIED' WHERE group_id = 'abc';
COMMIT;  -- both succeed, or...
ROLLBACK;  -- both are undone
```

### ACID Properties

| Property | Meaning |
|----------|---------|
| **Atomicity** | All or nothing — either all statements commit or all roll back |
| **Consistency** | DB moves from one valid state to another (constraints enforced) |
| **Isolation** | Concurrent transactions don't interfere with each other |
| **Durability** | Once committed, data survives crashes |

### Isolation Levels (MySQL InnoDB)

| Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|-------|-----------|-------------------|-------------|
| READ UNCOMMITTED | Yes | Yes | Yes |
| READ COMMITTED | No | Yes | Yes |
| **REPEATABLE READ** (MySQL default) | No | No | Possible (but InnoDB prevents most) |
| SERIALIZABLE | No | No | No |

### Spring `@Transactional`

```java
@Transactional
public void recomputeOverallStatus(String groupId) {
    // Everything in this method runs in ONE transaction
    KycStatusEntity status = repo.findByGroupId(groupId);  // SELECT
    List<KycOperationProjection> attempts = repo.findAll(groupId);  // SELECT
    OverallStatus computed = compute(attempts);
    repo.updateOverallStatus(groupId, computed);  // UPDATE
    // COMMIT happens here (when method returns)
}
```

If an exception is thrown, Spring rolls back automatically.

### Propagation Types

| Type | Behavior |
|------|----------|
| `REQUIRED` (default) | Join existing transaction, or create new one |
| `REQUIRES_NEW` | Always create a new transaction (suspends existing) |
| `NESTED` | Create a savepoint within the existing transaction |
| `SUPPORTS` | Join if exists, otherwise run without transaction |

---

## 7. Concurrency in Web Applications

### Thread-Per-Request Model

```
Request 1 → Thread A → [process] → response
Request 2 → Thread B → [process] → response
Request 3 → Thread C → [process] → response
```

Each request gets its own thread. Threads are reused from the web server's thread pool.

### Thread Safety Concerns

- **Singletons are shared:** Spring beans are singletons — all threads share the same instance. Don't store request-specific state in fields.
- **ThreadLocal:** `MyServiceContext` uses ThreadLocal to store per-request data (transaction ID, group ID). Each thread has its own copy. Must be cleared after the request (memory leak otherwise).
- **Shared mutable state:** If two threads modify the same object without synchronization → race condition. This is why `VerificationAggregateResultInternal.addStageErrorsAndWarnings()` has the comment about synchronization trade-offs.

### Async Patterns

| Pattern | Mechanism | Use Case |
|---------|-----------|----------|
| `@Async` | Spring submits to TaskExecutor | Fire-and-forget (audit logging) |
| `CompletableFuture` | Java concurrent framework | Parallel stage execution with timeout |
| `@Scheduled` | Spring cron/fixed-rate | Background cleanup jobs |
| Message Queue (Kinesis/Kafka) | External broker | Cross-service async communication |

---

## 8. External Service Communication

### Feign Clients (used in this project)

Declarative HTTP client — define an interface, Spring generates the implementation:

```java
@FeignClient(name = "liveness-service", url = "${liveness.url}")
public interface LivenessClient {
    @PostMapping("/api/v1/liveness")
    LivenessResponse checkLiveness(@RequestBody LivenessRequest request);
}
```

Spring generates HTTP client code that:
1. Serializes `LivenessRequest` to JSON
2. Sends POST to the configured URL
3. Deserializes response to `LivenessResponse`
4. Handles OAuth2 client credentials (via `FeignSecurityConfig`)

### WebClient (also used in this project)

Non-blocking HTTP client for reactive calls:

```java
webClient.post()
    .uri("/api/v1/ocr")
    .bodyValue(request)
    .retrieve()
    .bodyToMono(OcrResponse.class)
```

### Circuit Breaker Pattern

If an external service is down, don't keep hammering it. After N failures, "open" the circuit — return error immediately without calling the service. After a timeout, "half-open" — try one request. If it succeeds, close the circuit.

Libraries: Resilience4j (in this project's pom.xml), Hystrix (deprecated).

---

## 9. Caching

### Why Cache

External service calls (OSS config, product config) are slow and return the same data for many requests. Cache the response.

### Redis (used in this project)

In-memory key-value store used as a distributed cache:

```
Request → check Redis → HIT → return cached value (1ms)
                       → MISS → call OSS service (50ms) → store in Redis → return
```

Configured via Spring's `@Cacheable`:
```java
@Cacheable("productConfigCache")
public OssProductConfigDTO getProductConfig(String productExternalId) {
    return ossFeign.getConfig(productExternalId);  // only called on cache miss
}
```

Cache entries have TTL (time-to-live) — after expiry, next request triggers a fresh call.

---

## 10. Security

### Authentication vs Authorization

- **Authentication (AuthN):** Who are you? → JWT token validation (KeyCloak)
- **Authorization (AuthZ):** What can you do? → Check user permissions for requested operations

### JWT Flow in This Project

```
Client → KeyCloak: "Here are my credentials"
KeyCloak → Client: "Here's your JWT token (signed)"
Client → API: "POST /verify" + "Authorization: Bearer <JWT>"
API → Spring Security: Validate JWT signature using KeyCloak's public key
Spring Security → Controller: "User is authenticated, claims = {...}"
```

The API never calls KeyCloak to validate the token — it verifies the signature locally using the JWK (JSON Web Key) set.

---

## 11. Observability

### The Three Pillars

| Pillar | What | Tool |
|--------|------|------|
| **Logs** | Text records of what happened | Logback → Grafana Loki |
| **Metrics** | Numeric measurements over time | Micrometer → Prometheus → Grafana |
| **Traces** | Request flow across services | OpenTelemetry → Grafana Tempo |

### Structured Logging (this project)

```json
{"status":"INFO", "message":"Transitioned 5 stale KYC cases", "transactionId":"abc-123", "groupId":"def-456"}
```

Not just text — structured JSON with fields. Allows filtering in Grafana: "show me all logs where groupId = X".

### Metrics with `@Timed`

```java
@Timed(value = "vida.verify.my-service.auditing_time_to_verification_response", histogram = true)
public void verificationResponse(...) { ... }
```

Micrometer records: how long this method took, how many times it was called, percentile distribution (p50, p95, p99).

### Distributed Tracing

A single user request may call 5 services. A **trace** ties them all together:

```
Trace ID: abc-123
  └─ Span 1: my-verify (POST /verify)     [0ms - 1500ms]
      └─ Span 2: liveness-service          [100ms - 800ms]
      └─ Span 3: ocr-service              [100ms - 600ms]
      └─ Span 4: fraud-shield-service     [200ms - 400ms]
      └─ Span 5: S3 upload                [1200ms - 1400ms]
```

The `traceId` and `spanId` in the log entries are part of this system.

---

## Topics for Further Reading

### JDK / JVM
- JVM memory model — heap, stack, metaspace, garbage collection (G1GC, ZGC)
- Java 21 features — virtual threads, pattern matching, sealed classes, records
- ClassLoader hierarchy — how Spring Boot fat JARs work
- JIT compilation — how the JVM optimizes hot code paths

### Spring Internals
- Spring AOP (Aspect-Oriented Programming) — how `@Transactional`, `@Async`, `@Timed` work under the hood (proxy-based interception)
- Bean lifecycle — `@PostConstruct`, `@PreDestroy`, `InitializingBean`, `DisposableBean`
- Spring profiles — how `application-{profile}.properties` override base properties
- Spring auto-configuration — how `spring-boot-starter-*` dependencies auto-wire beans

### Database
- MySQL InnoDB internals — B+ tree indexes, clustered vs secondary indexes, MVCC
- Query execution plans — `EXPLAIN ANALYZE` to understand index usage
- Database migration tools — Flyway, Liquibase (versioned schema changes)
- Read replicas — scaling reads by routing to replicas

### Distributed Systems
- CAP theorem — consistency, availability, partition tolerance (pick two)
- Eventual consistency — what it means for microservices
- Idempotency — designing APIs that are safe to retry
- Saga pattern — distributed transactions across microservices
- Event sourcing — storing events instead of current state

### DevOps / Infrastructure
- Docker — containerization, Dockerfile, layers, multi-stage builds
- Kubernetes — pods, deployments, services, ingress, health probes, resource limits
- Helm charts — templated K8s manifests
- CI/CD — Bitbucket Pipelines (this project), GitHub Actions, Jenkins
- Infrastructure as Code — Terraform, Pulumi

### Performance
- Load testing — JMeter, Gatling, k6
- Profiling — async-profiler, JFR (Java Flight Recorder)
- Database query optimization — slow query log, index hints
- Connection pool tuning — HikariCP sizing formula
- JVM tuning — heap size, GC algorithm selection, GC log analysis
