---
order: 10
search: false
---

# Exercises

## Exercise: deployment-strategy-tradeoff-selection - Deployment Strategy Selection & Capacity Planning

### Context & Goal
Modern production architectures must balance blast radius, infrastructure cost, rollback speed, and state compatibility when selecting deployment strategies. As a backend or platform engineer, you are tasked with selecting and configuring the deployment strategy for three different critical production workloads.

### Scenarios
1. **Workload A (Payment Authorization Gateway):**
   - High-concurrency, low-latency ($p99 < 50\text{ms}$).
   - Zero tolerance for transaction failure or dropped connections during deployment.
   - Requires instant rollback ($< 5\text{ seconds}$) in case of critical defects.
   - Dedicated cloud budget is available for $2\times$ baseline infrastructure during rollouts.
   - Stateless backend with strictly backward-compatible API contracts.

2. **Workload B (Data Ingestion & Report Aggregator):**
   - Runs 60 memory-intensive pods consuming substantial CPU and memory (high infrastructure footprint).
   - Organization operates under strict cloud cost caps (cannot double hardware capacity during deployment).
   - Workload tolerates gradual rollout over 15–20 minutes.
   - Pods require 45 seconds to initialize JVM caches and DB connection pools before accepting traffic.

3. **Workload C (High-Traffic E-Commerce Checkout API):**
   - High-throughput customer checkout service where complex business logic bugs (e.g., pricing discount calculation or inventory reservation edge cases) might evade integration tests.
   - Need to verify live production metrics (error rates, checkout conversion, latency) against real user traffic before routing full fleet traffic.
   - Multi-tenant architecture running on Kubernetes.

### Tasks
1. For each workload (A, B, and C), select the most appropriate deployment strategy among **Rolling Update**, **Blue-Green Deployment**, and **Canary Release**.
2. For Workload B, define the exact Kubernetes Deployment rollout parameters:
   - What values should be set for `maxSurge` and `maxUnavailable` to ensure that active processing capacity never drops below 100% of target replicas while keeping extra compute surge below 25%?
   - How should `minReadySeconds` and `readinessProbe` be configured to prevent traffic routing to unready JVM pods?
3. For Workload A, explain why Blue-Green provides faster rollback than a standard Rolling deployment and identify the critical network switch mechanism (e.g., DNS vs Load Balancer Target Group vs Ingress rule) that enables instant traffic redirection.

---

## Exercise: automated-canary-abort-decision-engine - Automated Canary Telemetry Analysis & Abort Decision Logic

### Context & Goal
A progressive delivery operator (such as Argo Rollouts or Flagger) is managing a canary release of an `Order Processing Service` (v2.0 vs stable baseline v1.0). The canary receives 5% of ingress traffic. The automated analysis controller samples metrics every 2 minutes.

### Metric Thresholds & Guardrails
- **HTTP 5xx Error Rate Threshold:** Canary error rate must not exceed baseline error rate by more than $+0.5\%$, and absolute canary 5xx rate must be $< 1.0\%$.
- **Latency Guardrail:** Canary $p99$ response latency must not exceed $200\text{ms}$ or degrade by $> 15\%$ compared to the baseline.
- **Runtime Stability:** Pod restart count must equal 0 (`CrashLoopBackOff` = 0).
- **Failure Tolerance (`failureLimit`):** Rollout must abort immediately if metric evaluation breaches guardrails for **3 consecutive evaluation cycles**.

### Telemetry Evaluation Table

| Metric Timestamp | Baseline 5xx (%) | Canary 5xx (%) | Baseline p99 (ms) | Canary p99 (ms) | Canary Restarts | Order Success Rate Baseline (%) | Order Success Rate Canary (%) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **$t_1$ (2 min)** | 0.05% | 0.08% | 110ms | 118ms | 0 | 99.8% | 99.7% |
| **$t_2$ (4 min)** | 0.06% | 1.45% | 112ms | 225ms | 0 | 99.8% | 96.2% |
| **$t_3$ (6 min)** | 0.04% | 1.80% | 108ms | 240ms | 0 | 99.9% | 95.1% |
| **$t_4$ (8 min)** | 0.05% | 1.65% | 110ms | 230ms | 0 | 99.8% | 95.5% |
| **$t_5$ (10 min)**| 0.05% | 0.90% | 111ms | 180ms | 0 | 99.8% | 98.9% |

### Tasks
1. Evaluate each time interval ($t_1$ through $t_5$). For each interval, state whether the evaluation **PASSED** or **FAILED**, specifying the exact breached metric criteria.
2. Identify at which timestamp ($t_1$ to $t_5$) the automated abort must be triggered by the rollout controller based on `failureLimit: 3`.
3. Explain the exact sequence of technical actions the controller and ingress proxy execute upon triggering an automated abort.
4. Explain why metric analysis should compare Canary metrics against the concurrent Baseline rather than static absolute hardcoded thresholds alone.

---

## Exercise: expand-contract-database-migration - Zero-Downtime Expand-Contract Schema & App Rollout Staging

### Context & Goal
You need to refactor the `orders` table in a high-traffic production PostgreSQL database. The legacy schema stores addresses as a plain text string `legacy_shipping_address VARCHAR(255)`. The new business requirement demands structured JSON address storage `shipping_address_json JSONB` containing `{ "street": "...", "city": "...", "zip": "..." }`.

The application cannot endure any read/write downtime, and multiple microservice instances running different versions coexist during rollouts.

### Tasks
1. Construct the step-by-step 5-phase Expand-Contract deployment lifecycle. For each phase, specify:
   - Database migration action (DDL/DML).
   - Application version released and its exact Read and Write behavior.
2. Write the DDL for the initial **Expand** phase:
   - Why must `shipping_address_json` be added as `NULL` (or with a default) rather than `NOT NULL` without default?
3. Describe how the **Phase 3 Data Backfill** job must be engineered to prevent table locking, database replication lag, and high CPU spikes on the production database.
4. Construct a **Rollback Decision Matrix** covering failures discovered at:
   - **Scenario A:** A critical bug is found in App v1.1 during Phase 2 (Dual Write active).
   - **Scenario B:** The backfill script in Phase 3 encounters malformed address records and halts.
   - **Scenario C:** A query performance regression is detected in App v1.2 during Phase 4 (Reads switched).
   - For each scenario, state the exact rollback action and whether a database schema rollback is required.
