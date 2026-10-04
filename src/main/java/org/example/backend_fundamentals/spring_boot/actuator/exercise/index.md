---
order: 10
search: false
---

# Exercise

## Exercise: actuator-k8s-probes - Kubernetes Probes and Custom Health Indicator Design

### Problem Statement
You are architecting a Spring Boot microservice deployed to Kubernetes.
1. Explain how you configure the service so that `/actuator/health/liveness` and `/actuator/health/readiness` endpoints are active.
2. Suppose your service relies on an external recommendation engine microservice. If that external service is intermittently timing out, should your custom health check fail the **Liveness** probe, the **Readiness** probe, or neither? What catastrophic failure happens if you bind this external dependency to the wrong probe?
3. Provide a Spring Boot custom `HealthIndicator` implementation that checks a critical local cache and returns `Status.DOWN` with a failure reason if the cache is uninitialized.

---

## Exercise: actuator-security-and-metrics - Port Isolation and Custom Micrometer Metrics

### Problem Statement
A high-throughput payment processing service needs operational observability while preventing credential leaks:
1. Configure `application.yml` to ensure Actuator endpoints listen exclusively on internal management port `8081` with only `health`, `info`, and `metrics` exposed over HTTP.
2. Write a Spring Service method `processPayment(String currency, double amount)` that instruments payment transaction processing using Micrometer's `MeterRegistry`. You must record both a `Counter` for total payments (tagged by `currency` and `status`) and a `Timer` for transaction duration.
3. How do you verify the newly created metric via the HTTP Actuator API?
