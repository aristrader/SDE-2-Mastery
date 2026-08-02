---
order: 30
---

# Actuator

---

## Mental model

Spring Boot Actuator adds operational endpoints to a running app.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Use it to answer production questions:

- Is the app healthy?
- Is it ready to receive traffic?
- What metrics is it producing?

---

## Common endpoints

| Endpoint | Use |
| --- | --- |
| `/actuator/health` | Health status for app/dependencies |
| `/actuator/info` | App version/build/info |
| `/actuator/metrics` | Available metric names and values |
| `/actuator/loggers` | View/change log levels at runtime; protect it |
| `/actuator/env` | Environment/config values; sensitive; do not expose publicly |

Default HTTP exposure is conservative: usually only `health` and `info`.

---

## Exposure

Expose only what you need.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

Avoid this in production unless endpoints are strongly protected:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

Why: endpoints such as `env`, `heapdump`, `beans`, and `loggers` can expose internals or change runtime behavior.

---

## Health

`/actuator/health` reports whether the app and important dependencies are healthy.

Typical response:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

If one important component is `DOWN`, the overall status becomes `DOWN`.

For interviews, know that Spring can include dependency health such as database/Redis, and custom checks are possible with `HealthIndicator`. You do not need to memorize custom health-check code.

Liveness and readiness are special health endpoints:

| Probe | Meaning | Failure action |
| --- | --- | --- |
| Liveness | Is the process alive? | Restart the container |
| Readiness | Can the app receive traffic? | Remove from load balancer |

Endpoints:

```text
/actuator/health/liveness
/actuator/health/readiness
```

Example: during startup, liveness can be `UP` while readiness is `DOWN`. That means "do not restart the app, but do not send traffic yet."

---

## Metrics and Micrometer

Short note only: Actuator exposes metrics at `/actuator/metrics`. Micrometer is the metrics facade underneath, similar to how SLF4J is a facade for logging.

---

## Security

The security rule is simple:

```text
Expose health/info publicly if needed. Protect everything else.
```

Common production pattern: put management endpoints on a separate port and restrict that port at the network level.

```yaml
management:
  server:
    port: 8081
```

If endpoints share the app port, protect them with Spring Security.

Never expose `env`, `heapdump`, `threaddump`, `beans`, or `loggers` publicly.

---

## Quick recall

**Q. What does Actuator add?**
A. Operational endpoints for health, info, metrics, and runtime visibility.

**Q. Which endpoints are usually exposed by default over HTTP?**
A. `health` and `info`.

**Q. What is Micrometer?**
A. The metrics facade used by Actuator.

**Q. Liveness vs readiness?**
A. Both are health endpoints. Liveness means restart if broken; readiness means stop sending traffic.

**Q. Why is `include: "*"` risky?**
A. It can expose sensitive internals or dangerous operations unless protected.

**Q. Should `env`, `heapdump`, `beans`, or `loggers` be public?**
A. No. They expose internals or can affect runtime behavior.
