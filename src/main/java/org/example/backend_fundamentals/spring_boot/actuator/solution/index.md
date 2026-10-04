---
order: 20
search: false
---

# Solution

## Solution: actuator-k8s-probes - Kubernetes Probes and Custom Health Indicator Design

### 1. Enabling Liveness & Readiness Probes
In Spring Boot, probes are enabled automatically when running in a Kubernetes environment. To enable them explicitly in configuration:

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,info
```

This exposes `/actuator/health/liveness` and `/actuator/health/readiness`.

### 2. Dependency Probe Assignment & Cascading Failure Pitfall
- **Correct Probe**: It should be attached to neither or only considered for selective degradation / circuit breaker metrics. If an external service outage must stop traffic to this pod, it should only affect **Readiness**, never Liveness.
- **Catastrophic Failure**: If bound to the **Liveness** probe, when the external service goes down, Kubernetes will kill and restart every pod of your service simultaneously. This causes a **cascading reboot loop** (stampede) across your entire fleet without fixing the downstream outage, multiplying startup load on your databases and crashing the cluster.

### 3. Custom HealthIndicator Implementation

```java
package org.example.backend_fundamentals.spring_boot.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("cacheHealthIndicator")
public class CacheHealthIndicator implements HealthIndicator {

    private final LocalCacheService cacheService;

    public CacheHealthIndicator(LocalCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Health health() {
        if (cacheService.isInitialized()) {
            return Health.up()
                .withDetail("cacheSize", cacheService.size())
                .build();
        }
        return Health.down()
            .withDetail("error", "Local cache not initialized or warming up")
            .build();
    }
}
```

---

## Solution: actuator-security-and-metrics - Port Isolation and Custom Micrometer Metrics

### 1. `application.yml` Port Isolation & Endpoint Exposure

```yaml
server:
  port: 8080

management:
  server:
    port: 8081
    address: 127.0.0.1
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

Binding `management.server.address` to `127.0.0.1` or routing port `8081` through private internal VPC networks guarantees external clients cannot hit management endpoints.

### 2. Custom Micrometer Counter & Timer Instrumentation

```java
package org.example.backend_fundamentals.spring_boot.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessingService {

    private final MeterRegistry meterRegistry;

    public PaymentProcessingService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void processPayment(String currency, double amount) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String status = "SUCCESS";
        try {
            // Execute business payment logic
            executeTransaction(currency, amount);
        } catch (Exception ex) {
            status = "FAILURE";
            throw ex;
        } finally {
            meterRegistry.counter("payment.transactions.total", "currency", currency, "status", status)
                .increment();
            sample.stop(meterRegistry.timer("payment.transactions.duration", "currency", currency, "status", status));
        }
    }

    private void executeTransaction(String currency, double amount) {
        // Business logic simulation
    }
}
```

### 3. Verification via HTTP Actuator API
Query the metrics endpoint on the management port:
1. List available metric names: `GET http://localhost:8081/actuator/metrics`
2. Inspect payment counter with dimensional tag filtering:
   `GET http://localhost:8081/actuator/metrics/payment.transactions.total?tag=currency:USD&tag=status:SUCCESS`
3. Inspect payment timer duration:
   `GET http://localhost:8081/actuator/metrics/payment.transactions.duration`
