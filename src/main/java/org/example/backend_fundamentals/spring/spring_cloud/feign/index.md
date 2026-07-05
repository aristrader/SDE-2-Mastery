---
order: 10
---

# Feign / OpenFeign

---

## What Feign is

Feign is a **declarative HTTP client**: you annotate a Java interface, and Spring generates a JDK dynamic proxy at startup. No boilerplate HTTP code — just the contract.

**Internals:** Spring uses reflection to read the interface annotations at startup. It builds a `MethodHandler` per method that encodes the annotation metadata (path, HTTP verb, parameter types) into a `RequestTemplate`. At call time the proxy intercepts the invocation, populates the template with the actual arguments, applies any `RequestInterceptor` beans, then delegates to a `Client` (default: `java.net.HttpURLConnection`, or OkHttp/Apache HttpClient if on classpath) to execute the HTTP call.

```java
@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserClient {
    @GetMapping("/users/{id}")
    UserDto getUser(@PathVariable("id") Long id);

    @PostMapping("/users")
    UserDto createUser(@RequestBody CreateUserRequest request);
}
```

The proxy handles URL construction, serialization, response mapping, and error handling.

---

## @FeignClient attributes

| Attribute | Purpose | Notes |
|---|---|---|
| `name` / `value` | Logical service name; used for service discovery (Eureka/Consul) | Required |
| `url` | Hard-code a base URL; bypasses service discovery | Good for external APIs or local dev |
| `configuration` | Points to a `@Configuration` class with custom beans | Interceptors, decoders, retryers per client |
| `fallback` | Bean class called when circuit is open or call fails | Requires Resilience4j + `feign.circuitbreaker.enabled=true` |
| `fallbackFactory` | Like fallback but receives the exception | Lets you log or respond based on error type |

---

## Request mapping annotations

Feign supports Spring MVC annotations on interface methods:

```java
@FeignClient(name = "kyc-service")
public interface KycClient {
    @GetMapping("/kyc/{userId}/status")
    KycStatus getStatus(@PathVariable("userId") String userId);

    @PostMapping("/kyc/verify")
    VerificationResult verify(
        @RequestHeader("X-Tenant-Id") String tenantId,
        @RequestBody VerifyRequest request
    );

    @GetMapping("/kyc/search")
    List<KycRecord> search(@RequestParam("status") String status,
                           @RequestParam("page") int page);
}
```

All standard MVC parameter annotations work: `@PathVariable`, `@RequestParam`, `@RequestHeader`, `@RequestBody`.

---

## Feign vs WebClient vs RestTemplate

| | RestTemplate | WebClient | Feign |
|---|---|---|---|
| Style | Imperative | Reactive (Mono/Flux) | Declarative |
| Blocking | Yes | No | Yes |
| Verbosity | Medium | High | Low |
| Status | Deprecated for new code | Preferred for reactive | Preferred for simple sync calls |
| Reactive support | No | Yes | No (native) |
| Error handling | Manual | Manual | ErrorDecoder hook |
| Best for | Legacy code | Non-blocking I/O, streaming | Service-to-service REST |

**Rule of thumb:** use Feign when the call is synchronous and the interface is stable. Use WebClient when you need non-blocking or streaming.

---

## Logging levels

Feign has four logging levels controlled per client. Set the level in config and the logger to DEBUG:

| Level | What is logged |
|---|---|
| `NONE` | Nothing (default) |
| `BASIC` | Method, URL, response status code, execution time |
| `HEADERS` | Everything in BASIC + request and response headers |
| `FULL` | Everything in HEADERS + request and response body |

```java
// Per-client configuration class
public class FeignLoggingConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;  // FULL in dev only — bodies are verbose
    }
}
```

```yaml
# Logger must be set to DEBUG for any Feign logging to appear
logging:
  level:
    com.example.clients.UserClient: DEBUG
```

**FULL in production is dangerous** — it logs request/response bodies, which may contain PII or tokens. Use `BASIC` in production.

---

## Load balancing with Spring Cloud LoadBalancer

When `name` in `@FeignClient` is a registered service name (via Eureka, Consul, or a static list), Spring Cloud LoadBalancer resolves it to an actual host:port before each call.

```yaml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false   # Ribbon is deprecated; Spring Cloud LoadBalancer is the replacement
```

```java
// No url attribute → uses service discovery + load balancing
@FeignClient(name = "user-service")
public interface UserClient { ... }
```

Spring Cloud LoadBalancer uses a `ReactiveLoadBalancer` (round-robin by default) to choose an instance. Each Feign call resolves the service name fresh — if an instance goes down, the next call gets a healthy one (assuming the registry is up-to-date).

**url + name together:** supplying `url` bypasses service discovery entirely. The `name` is still required (used as the bean qualifier) but load balancing doesn't apply.

---

## RequestInterceptor

Runs before **every request** on the client. Use for cross-cutting concerns: auth headers, correlation IDs, tenant context.

```java
@Bean
public RequestInterceptor authInterceptor() {
    return requestTemplate -> {
        String token = SecurityContextHolder.getContext()
            .getAuthentication().getCredentials().toString();
        requestTemplate.header("Authorization", "Bearer " + token);
    };
}

// Tenant-ID from MDC (common in multi-tenant KYC platforms)
@Bean
public RequestInterceptor tenantInterceptor() {
    return requestTemplate ->
        requestTemplate.header("X-Tenant-Id",
            MDC.get("tenantId"));
}
```

Register per-client via `@FeignClient(configuration = MyConfig.class)` or globally as a `@Bean` in a `@Configuration` class.

---

## ErrorDecoder

Maps non-2xx HTTP responses to domain exceptions. The default throws `FeignException` with the raw status and body — not useful for business logic.

```java
public class KycServiceErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new UserNotFoundException("User not found");
            case 409 -> new DuplicateKycRequestException("KYC already submitted");
            case 422 -> new KycValidationException(extractBody(response));
            case 503 -> new ServiceUnavailableException("KYC service down");
            default  -> defaultDecoder.decode(methodKey, response);
        };
    }

    private String extractBody(Response response) {
        try (var body = response.body().asInputStream()) {
            return new String(body.readAllBytes());
        } catch (IOException e) {
            return "unknown";
        }
    }
}
```

Register via the client's `configuration` class as a `@Bean`.

**Key point:** ErrorDecoder runs for all non-2xx. 4xx errors should generally **not** be retried — they're deterministic failures. Only network/5xx errors warrant retry.

---

## Retryer

Controls retry behaviour on `IOException` (connection refused, timeout, network blip). The `Retryer.Default` already handles this.

```java
@Bean
public Retryer retryer() {
    // 100ms initial interval, max 1s, 3 attempts
    return new Retryer.Default(100, 1000, 3);
}
```

**Do NOT retry on business errors (4xx).** If your ErrorDecoder throws a domain exception, Feign won't retry it (Retryer only catches `RetryableException`). Throw `RetryableException` only for transient failures.

For advanced retry (exponential backoff, jitter), integrate Resilience4j's `Retry` policy instead.

---

## Timeout configuration

Default Feign timeouts are **10s connect / 60s read** — far too generous for synchronous service-to-service calls. Always override:

```yaml
# application.yml — per-client timeout
spring:
  cloud:
    openfeign:
      client:
        config:
          user-service:
            connectTimeout: 1000   # 1s
            readTimeout: 5000      # 5s
          default:
            connectTimeout: 2000
            readTimeout: 10000
```

Or programmatically:

```java
@Bean
public Request.Options options() {
    return new Request.Options(
        1, TimeUnit.SECONDS,
        5, TimeUnit.SECONDS,
        true  // follow redirects
    );
}
```

**Why it matters:** a slow downstream holds your thread pool. With sync Feign, every blocked thread = one fewer request your service can handle. Set aggressive timeouts and let the circuit breaker open when the downstream is degraded.

---

## Circuit breaker integration (Resilience4j)

```yaml
feign:
  circuitbreaker:
    enabled: true

resilience4j:
  circuitbreaker:
    instances:
      user-service:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
```

```java
@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    @GetMapping("/users/{id}")
    UserDto getUser(@PathVariable("id") Long id);
}

@Component
public class UserClientFallback implements UserClient {
    @Override
    public UserDto getUser(Long id) {
        return UserDto.unknown(id);  // graceful degradation
    }
}
```

For fallback with exception context, use `fallbackFactory`:

```java
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        log.error("UserClient fallback triggered", cause);
        return id -> UserDto.unknown(id);
    }
}
```

---

## Quick recall

**Q. What does @FeignClient generate at startup?**
A. A JDK proxy that translates interface method calls into HTTP requests using the annotations as the contract.

**Q. Feign vs WebClient — when to pick which?**
A. Feign for simple synchronous service-to-service calls; WebClient when you need non-blocking or streaming responses.

**Q. What is a RequestInterceptor used for?**
A. Runs before every request on the client; add Authorization headers, correlation IDs, tenant context — cross-cutting concerns.

**Q. Why is a custom ErrorDecoder important?**
A. The default throws generic FeignException; a custom decoder maps status codes to domain exceptions, letting callers catch meaningful types.

**Q. Should you retry 4xx errors with Feign's Retryer?**
A. No — 4xx are deterministic client errors; retrying won't fix them and wastes resources. Only retry IOExceptions / transient 5xx.

**Q. What are the four Feign logging levels and which is safe for production?**
A. NONE, BASIC, HEADERS, FULL. Use BASIC in production — FULL logs request/response bodies which may contain PII or tokens.

**Q. How does Feign perform load balancing and when is it bypassed?**
A. When only `name` is set, Spring Cloud LoadBalancer resolves the service name to an instance via the registry. Supplying `url` bypasses discovery and load balancing entirely.
