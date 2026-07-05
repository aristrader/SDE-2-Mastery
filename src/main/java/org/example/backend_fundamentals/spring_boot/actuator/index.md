---
order: 10
---

# Actuator

---

## What Actuator is

Spring Boot Actuator adds **production-ready operational endpoints** — no code required. Add the dependency, get immediate visibility into health, metrics, configuration, and runtime state.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Under the hood: Actuator registers `@Endpoint` beans; the web layer (MVC or WebFlux) maps them to HTTP at `/actuator/{id}`. Telemetry flows through **Micrometer**, a facade over monitoring backends (Prometheus, Datadog, CloudWatch, etc.).

---

## Key endpoints

| Endpoint | Method | What it returns |
|---|---|---|
| `/actuator/health` | GET | Aggregate health status (UP/DOWN/OUT_OF_SERVICE) + component breakdown |
| `/actuator/info` | GET | Arbitrary app info (version, build, custom) |
| `/actuator/metrics` | GET | List of all metric names; `/actuator/metrics/{name}` for detail |
| `/actuator/env` | GET | All `Environment` properties (property sources, values, origins) |
| `/actuator/beans` | GET | All beans in the Spring context, with their type, scope, and dependencies |
| `/actuator/mappings` | GET | All `@RequestMapping` routes and their handler methods |
| `/actuator/threaddump` | GET | Current JVM thread dump (all threads + stack traces) |
| `/actuator/heapdump` | GET | Full heap dump as a binary file (download it, open in VisualVM / MAT) |
| `/actuator/loggers` | GET/POST | Read current log levels; POST to change level at runtime without restart |
| `/actuator/conditions` | GET | Same as `--debug` ConditionEvaluationReport, over HTTP |
| `/actuator/scheduledtasks` | GET | All `@Scheduled` tasks and their configuration |
| `/actuator/httptrace` | GET | Last N HTTP request/response traces (Boot 2.x; renamed `httpexchanges` in Boot 3) |
| `/actuator/shutdown` | POST | Graceful shutdown (disabled by default — enable deliberately) |

---

## Exposure configuration

By default, only `/actuator/health` and `/actuator/info` are exposed over HTTP (security). Everything is exposed over JMX.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers,env   # explicit allowlist
        # include: "*"  -- exposes everything -- NEVER do this in prod without auth
        exclude: heapdump,shutdown                 # always exclude dangerous ones
```

Exposing over HTTP with `include: "*"` in production without Spring Security is a critical security vulnerability — anyone can read all env variables (including secrets), change log levels, or trigger a heap dump.

---

## Health endpoint in depth

### Composite health

Actuator aggregates health checks from multiple `HealthIndicator` beans into a single response. Spring Boot auto-configures indicators for: DataSource, Redis, MongoDB, Cassandra, Kafka, RabbitMQ, Elasticsearch, Solr, and more.

```
GET /actuator/health
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "MySQL", "validationQuery": "isValid()" } },
    "redis": { "status": "UP", "details": { "version": "7.2.3" } },
    "diskSpace": { "status": "UP", "details": { "total": 499963174912, "free": 120000000000 } }
  }
}
```

**Health status values** (in severity order, worst wins):

| Status | Meaning |
|---|---|
| `UP` | Component is healthy |
| `OUT_OF_SERVICE` | Component deliberately taken out (e.g., maintenance mode) |
| `DOWN` | Component is unhealthy / unreachable |
| `UNKNOWN` | Cannot determine status |

`INFO` is a valid `Status` constant (`Status.INFO`) but isn't part of the default severity ordering — used by `ReadinessStateHealthIndicator` to signal "app started but not yet ready" during startup.

Aggregate status is the worst of all components using the default order `DOWN > OUT_OF_SERVICE > UP > UNKNOWN`. Any `DOWN` makes the root `DOWN`; `OUT_OF_SERVICE` beats `UP` but loses to `DOWN`.

### Show details

```yaml
management:
  endpoint:
    health:
      show-details: always     # always | never | when-authorized
      show-components: always
```

`never` (default) hides component breakdown from unauthenticated callers. `when-authorized` shows details only to authenticated users with a specific role.

### Custom `HealthIndicator`

```java
@Component
public class ExternalKycApiHealthIndicator implements HealthIndicator {

    private final KycApiClient client;

    @Override
    public Health health() {
        try {
            boolean reachable = client.ping();
            return reachable
                ? Health.up().withDetail("kyc-api", "reachable").build()
                : Health.down().withDetail("kyc-api", "timeout").build();
        } catch (Exception e) {
            return Health.down(e).withDetail("kyc-api", "exception: " + e.getMessage()).build();
        }
    }
}
```

Spring Boot detects `HealthIndicator` beans automatically — no registration needed. Component name in the health response is derived from the bean name (`externalKycApiHealthIndicator` → `externalKycApi`).

### Readiness vs Liveness (Spring Boot 2.3+)

Kubernetes distinguishes liveness (is the app running?) from readiness (can it serve traffic?):

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
# Exposes:
# /actuator/health/liveness  — LivenessStateHealthIndicator
# /actuator/health/readiness — ReadinessStateHealthIndicator
```

Auto-enabled when running in Kubernetes (detected via `KUBERNETES_SERVICE_HOST` env var), or force-enabled with `management.endpoint.health.probes.enabled=true`.

---

## Custom endpoints

Define your own beyond the built-ins:

```java
@Component
@Endpoint(id = "featureFlags")         // maps to /actuator/featureFlags
public class FeatureFlagEndpoint {

    private final FeatureFlagService flagService;

    @ReadOperation                     // HTTP GET
    public Map<String, Boolean> flags() {
        return flagService.getAllFlags();
    }

    @WriteOperation                    // HTTP POST with body
    public void setFlag(@Selector String flagName, boolean enabled) {
        flagService.setFlag(flagName, enabled);
    }

    @DeleteOperation                   // HTTP DELETE
    public void removeFlag(@Selector String flagName) {
        flagService.remove(flagName);
    }
}
```

`@Selector` on a parameter maps to a path segment: `POST /actuator/featureFlags/dark-mode` with body `{"enabled": true}`.

For web-only endpoints (when you need full HTTP control): use `@WebEndpoint` instead of `@Endpoint`. For JMX-only: `@JmxEndpoint`.

---

## Metrics — Micrometer

Micrometer is the metrics facade under Actuator — the same role SLF4J plays for logging. Your code records against the Micrometer API; a registry adapter ships data to the backend (Prometheus, Datadog, New Relic, CloudWatch, etc.).

### Auto-instrumented metrics (zero config)

- **JVM**: GC pause time, heap/non-heap usage, thread counts, class loading
- **Tomcat**: active connections, request count, error count, max threads
- **Spring MVC**: request duration per endpoint (`http.server.requests`)
- **HikariCP**: pool size, active connections, pending threads, connection acquisition time
- **Executor services**: queue size, completed tasks, active threads

### Custom metrics

```java
@Service
public class KycVerificationService {

    private final Counter verificationCounter;
    private final Timer verificationTimer;
    private final AtomicInteger pendingVerifications;

    public KycVerificationService(MeterRegistry registry) {
        // Counter: monotonically increasing
        this.verificationCounter = Counter.builder("kyc.verifications.total")
            .description("Total KYC verifications attempted")
            .tag("service", "kyc")
            .register(registry);

        // Timer: records latency + count + total time
        this.verificationTimer = Timer.builder("kyc.verification.duration")
            .description("KYC verification duration")
            .register(registry);

        // Gauge: current snapshot of a value
        this.pendingVerifications = new AtomicInteger(0);
        Gauge.builder("kyc.verifications.pending", pendingVerifications, AtomicInteger::get)
            .description("Pending KYC verifications")
            .register(registry);
    }

    public VerificationResult verify(String userId) {
        pendingVerifications.incrementAndGet();
        verificationCounter.increment();
        return verificationTimer.record(() -> {
            try {
                return doVerify(userId);
            } finally {
                pendingVerifications.decrementAndGet();
            }
        });
    }
}
```

### Prometheus integration

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Once on the classpath, Spring Boot auto-configures a `PrometheusMeterRegistry`. Expose `/actuator/prometheus` and point your Prometheus scrape config at it.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

---

## Security

### Separate management port

The most common production pattern — isolate management endpoints from app traffic:

```yaml
management:
  server:
    port: 8081    # management endpoints on a different port from server.port (8080)
```

The ops network (k8s cluster network, VPN, internal load balancer) reaches port 8081; public traffic only reaches 8080. No Spring Security config needed — the management port is network-isolated.

### Spring Security on management endpoints

If you can't use a separate port:

```java
@Configuration
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                .anyRequest().hasRole("ACTUATOR_ADMIN")
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
```

`EndpointRequest` is an Actuator-aware `RequestMatcher` helper — it knows which paths map to which endpoint types.

---

## Info endpoint

By default the info endpoint returns `{}`. Populate it via properties:

```yaml
management:
  info:
    env:
      enabled: true    # expose info.* properties
    build:
      enabled: true    # expose META-INF/build-info.properties (from spring-boot-maven-plugin)
    git:
      enabled: true    # expose git.properties (from git-commit-id-plugin)
      mode: full       # simple | full

info:
  app:
    name: kyc-service
    version: '@project.version@'   # Maven resource filtering substitutes this at build time
    environment: ${ENVIRONMENT:local}
```

For dynamic info, implement `InfoContributor`:

```java
@Component
public class FeatureFlagInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("feature-flags", Map.of(
            "new-kyc-flow", true,
            "dark-mode", false
        ));
    }
}
```

---

## Interview gotchas

**"Actuator endpoints are exposed by default — is that a security risk?"**
Only `health` and `info` are exposed over HTTP by default. JMX exposes all, but JMX is rarely accessible remotely. The real risk is misconfigured `include: "*"` without auth — the env endpoint leaks secrets.

**"How does the health aggregate work when one component is DOWN?"**
Root status becomes DOWN. Spring Boot uses `HealthAggregator` with default order `DOWN > OUT_OF_SERVICE > UP > UNKNOWN`. Register a custom `StatusAggregator` bean to change the order. `OUT_OF_SERVICE` indicates deliberate maintenance; beats `UP`, loses to `DOWN`.

**"Can you change a log level without restarting?"**
Yes — `POST /actuator/loggers/com.mycompany.service` with `{"configuredLevel": "DEBUG"}`. Effective immediately, survives until restart (or another POST). Useful for live production debugging.

**"What's Micrometer and why does it matter?"**
The metrics facade — your code uses the Micrometer API; the registry adapter (Prometheus, Datadog, etc.) handles the backend. Swap backends by swapping dependencies, not code. Same principle as SLF4J for logging.

**"Difference between `@Endpoint`, `@WebEndpoint`, and `@JmxEndpoint`?"**
`@Endpoint` is technology-agnostic (works over HTTP and JMX). `@WebEndpoint` is HTTP-only, gives you full `@RequestMapping` control. `@JmxEndpoint` is JMX-only.

---

## Quick recall

**Q. What does Actuator add to a Spring Boot app?**
A. Production-ready operational HTTP endpoints — health, metrics, env, beans, thread dumps — with zero boilerplate.

**Q. Which endpoints are exposed over HTTP by default?**
A. Only `/actuator/health` and `/actuator/info`. All others must be explicitly included via `management.endpoints.web.exposure.include`.

**Q. How do you implement a custom health check?**
A. Implement `HealthIndicator`, annotate with `@Component`. Spring auto-detects it and includes it in the composite health response.

**Q. What is Micrometer?**
A. The metrics facade under Actuator — provides `Counter`, `Timer`, `Gauge` etc.; registry adapters (Prometheus, Datadog) send data to monitoring backends without changing instrumentation code.

**Q. How do you secure Actuator in production?**
A. Preferred: separate management port (`management.server.port`) so management traffic is network-isolated. Alternative: Spring Security using `EndpointRequest` matcher to restrict access by role.

**Q. How do you change a log level at runtime?**
A. `POST /actuator/loggers/{logger-name}` with body `{"configuredLevel": "DEBUG"}`. No restart needed; effective immediately.

**Q. Liveness vs readiness probes — what's the difference?**
A. Liveness = is the process alive and not deadlocked (restart if fails). Readiness = is the app ready to handle traffic (remove from load balancer if fails, don't restart). Exposed at `/actuator/health/liveness` and `/actuator/health/readiness`.


<ExerciseNav />
