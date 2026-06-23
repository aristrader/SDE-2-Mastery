# SLA, SLO, and SLI: Technical Overview

These concepts are critical for defining, tracking, and maintaining service quality and reliability. They are widely used in Business, Site Reliability Engineering (SRE), and Operations.

## Core Definitions

The relationship between the three forms a continuous chain:
**SLA (Business Promise) → SLO (Target to Meet Promise) → SLI (Measurement to Verify Target)**

### 1. SLA (Service Level Agreement)
An SLA is a formal agreement between a company and its customers. It contains external promises regarding service quality, typically defined by business, legal, and sales teams.
* **Examples:** Guarantees of 99.9% monthly uptime, defined response times, or overall availability.
* **Violations:** Failing to meet an SLA often results in customer compensation, service credits, or contractual penalties.

### 2. SLO (Service Level Objective)
An SLO translates the business promises of the SLA into specific, measurable engineering targets.
* **Examples:**
  * Monthly availability ≥ 99.9%
  * 95% of requests complete within 200ms
  * 99% of incidents acknowledged within 5 minutes

### 3. SLI (Service Level Indicator)
An SLI is the actual quantitative measurement of the service's performance. It is the observed metric used to determine whether the SLO is being achieved.
* **Examples:**
  * Actual measured availability of 99.95%
  * Actual measurement of 96.2% of requests completing within 200ms
* **Common Metrics Used for SLIs:**
  * **Availability:** (e.g., 99.95%)
  * **Latency:** (e.g., 150ms)
  * **Error Rate:** (e.g., 0.02%)
  * **Throughput:** (e.g., 5000 requests/sec)
  * **Incident Response Time:** (e.g., 3 minutes)

---

## Real-World Scenarios

### Scenario 1: Availability
* **SLA:** "We promise 99.9% monthly availability."
* **SLO:** Availability ≥ 99.9%.
* **SLI:** Actual Availability = 99.95%.
* **Result:** 99.95% > 99.9%, so the SLO is achieved and the SLA is satisfied.

**Availability Calculation Example (SDE2 Interview Context):**
For an SLO of 99.9% uptime over a standard 30-day month:
* **Total minutes:** 30 days × 24 hours × 60 mins = 43,200 minutes.
* **Allowed downtime:** 0.1% of 43,200 = 43.2 minutes.
* *Takeaway:* To meet the 99.9% SLO, the service can only experience ~43 minutes of downtime per month.

### Scenario 2: Response Time
* **SLA:** "Users should receive fast responses."
* **SLO:** 95% of requests complete within 200ms.
* **SLI:** 92% of requests completed within 200ms.
* **Result:** 92% < 95%, meaning the SLO is violated, leading to a potential SLA violation.

---

## Common Misconceptions & Clarifications

1. **Misconception:** SLI and SLO are interchangeable, or SLO is the metric itself.
   * **Correction:** An SLI is the *measured metric value* (e.g., actual uptime = 99.95%). An SLO is the *target threshold* for that metric (e.g., target uptime = 99.9%).
2. **Misconception:** SLA, SLO, and SLI are independent, unrelated concepts.
   * **Correction:** They depend on one another. The SLA dictates the business promise, which defines the target SLO, which is monitored using measured SLIs.
