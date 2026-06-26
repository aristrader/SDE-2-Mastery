# Observability & Operations (SDE-2 Backend Interview Guide)

This guide covers essential observability and operations concepts targeted for Senior Backend Engineer (SDE-2) interviews, focusing on production scenarios and high-ROI topics rather than deep implementation details.

---

## 1. Logging & Context

Logs are records of discrete events, primarily used for debugging production issues, incident investigation, and auditing.

### Log Levels
- **DEBUG:** Detailed developer information (usually disabled in production).
- **INFO:** Normal business/application events.
- **WARN:** Unexpected condition, but the application continues running.
- **ERROR:** Request or operation failed.

### Structured Logging
Instead of plain text logs, use JSON format.
**Advantages:** Searchable, filterable, machine-readable, and easily ingested by tools like Datadog, ELK, or Splunk.

### Correlation ID / Request ID
In a microservices architecture, a single request may traverse multiple services (e.g., Gateway → User Service → Order Service). By attaching a `Correlation-ID` to every log entry for that request, you can easily search and trace the entire request flow across all services.

### Best Practices
- **What to log:** Request start/end, important business events, external API failures, exceptions, and retry attempts.
- **What NOT to log:** Passwords, tokens, sensitive customer data, and huge payloads (unless debugging).

### MDC vs. Trace Context
- **MDC (Mapped Diagnostic Context):** Used for *logging*. Automatically attaches contextual values (like Request ID, User ID) to every log statement in the current thread.
- **Trace Context:** Used for *distributed tracing*. Tracks request flow across services.
- **Relationship:** Tracing frameworks typically copy the Trace ID into the MDC. This allows you to easily correlate your distributed traces with your structured logs.

---

## 2. Metrics & Dashboards

**Logs vs. Metrics:** Logs tell you *what* happened for a specific event (detailed, expensive to store). Metrics tell you *how often* something is happening (aggregated numbers, cheap to store, ideal for dashboards/alerts).

### Metric Types
- **Counter:** Only increases (e.g., total requests, total errors).
- **Gauge:** Can increase or decrease (e.g., CPU usage, active users, queue size).
- **Histogram:** Measures distributions (e.g., API latency, request size). Useful for calculating percentiles like P95 or P99.
- **Summary:** Also measures distributions, but Histograms are generally preferred because they can be aggregated across multiple instances.

### Monitoring Methods
- **RED Method (User-facing services):** **R**ate, **E**rrors, **D**uration.
- **USE Method (Infrastructure):** **U**tilization, **S**aturation, **E**rrors.

### Tooling
- **Prometheus:** Pulls (scrapes) metrics from applications (`/metrics` endpoint) and stores them as time-series data.
- **Grafana:** Provides dashboards, visualization, and alert integration (often consuming data from Prometheus).
- **Dashboards:** Central place to monitor service health. Typical contents include requests/sec, error rates, latency (P95/P99), CPU/Memory, JVM heap, DB latency, and queue lag.

---

## 3. Distributed Tracing

Distributed tracing answers the question: *"Which service caused the delay for this specific request?"*

### Core Concepts
- **Trace:** The complete lifecycle of one incoming request. One request = One Trace = One Trace ID.
- **Span:** A single unit of work within a trace (e.g., a database query, an external API call, a specific business function). Each span has its own Span ID.
- **Parent-Child Relationship:** Spans form a tree. For example, an HTTP request span might be the parent of a database query span.

### Context Propagation
The most important tracing concept. When a service makes an outgoing call to another service, the tracing framework automatically attaches standard HTTP headers (like W3C's `traceparent`). The receiving service reads these headers and continues the same trace instead of starting a new one.

### Automatic vs. Manual Spans
- **Automatic Instrumentation:** Frameworks automatically create spans for infrastructure operations like HTTP calls, SQL queries, Redis commands, and Kafka message publishing.
- **Manual Spans:** Used to measure specific business operations (e.g., "Verify OTP", "Calculate Discount"). Developers manually wrap these operations in a span (e.g., using `tracer.withSpanInScope(span)`) to group sub-operations logically and measure the duration of business logic.

*Note: Not every function automatically becomes a span; a span should represent a meaningful unit of work.*

---

## 4. OpenTelemetry, Datadog & APM

### Tooling Responsibilities
- **OpenTelemetry:** Responsible for *instrumentation*. It creates traces/spans, propagates context, and exports telemetry (metrics, logs, traces). It is vendor-neutral and *does not store data*.
- **Datadog:** A full observability platform (Backend + UI) that ingests, stores, and visualizes logs, metrics, and traces.
- **Prometheus:** A backend that *only* stores metrics. It does not understand traces, trace IDs, or spans.

*Why use OpenTelemetry?* It prevents vendor lock-in. You instrument your code once with OpenTelemetry and can export the telemetry to any backend (Datadog, Jaeger, Prometheus, etc.) without changing your application code.

### APM (Application Performance Monitoring)
APM uses distributed tracing underneath to provide insights into application health. It easily identifies which API, service, database query, or external call is slow or failing.

---

## 5. Alerting & Incident Management

### Alerting Best Practices
- **Good Alerts:** Actionable, customer-impacting, low noise, and linked to a runbook (e.g., "Checkout error rate > 5%", "P99 latency > 2 sec").
- **Bad Alerts:** Prone to temporary spikes and false alarms (e.g., "CPU > 90%").
- **Symptom vs. Cause:** Prefer *symptom-based* alerts (e.g., checkout failing) over *cause-based* alerts (e.g., high CPU), as they directly reflect customer impact.
- **Runbooks:** Documentation detailing the meaning of an alert, verification steps, dashboards to check, immediate mitigation steps, and escalation contacts to reduce panic during an incident.

### Incident Lifecycle
1. **Alert & Acknowledge:** Monitoring detects the issue; on-call engineer accepts ownership.
2. **Investigate & Mitigate:** Assess impact and restore service quickly (e.g., rollback, restart, feature flag). *Rule of thumb: Mitigate/Rollback first, investigate/debug later.*
3. **Recover & RCA:** Verify system health and perform Root Cause Analysis.
4. **Postmortem:** A document outlining the timeline, root cause, and action items. Must be **blameless**—focusing on process failures, not individual blame. Use the "Five Whys" technique to find the underlying process issue.

---

## 6. Service Level Terminology

- **SLI (Service Level Indicator):** What we measure (e.g., Availability, Error rate, P99 latency).
- **SLO (Service Level Objective):** The internal target for an SLI (e.g., 99.9% availability, P99 latency < 300ms).
- **SLA (Service Level Agreement):** The business promise to customers (e.g., 99.9% uptime, with financial compensation if violated).

*Memory trick:* SLI = What we measure. SLO = What we aim for. SLA = What we promise.
