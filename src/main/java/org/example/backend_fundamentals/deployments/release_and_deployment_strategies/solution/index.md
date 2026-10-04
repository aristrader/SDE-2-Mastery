---
order: 20
search: false
---

# Solutions

## Solution: deployment-strategy-tradeoff-selection - Deployment Strategy Selection & Capacity Planning

### 1. Strategy Selection by Workload

| Workload | Recommended Strategy | Justification |
| :--- | :--- | :--- |
| **Workload A (Payment Gateway)** | **Blue-Green Deployment** | Requires instant rollback ($< 5\text{s}$) and near-zero tolerance for error. Dedicated budget allows provisioning an isolated Green environment ($2\times$ capacity). Pre-warming and sanity verification happen before shifting 100% traffic atomically at the load balancer level. |
| **Workload B (Data Aggregator)** | **Rolling Update** | Constrained by hardware budget (cannot double 60 heavy pods). Rolling update gradually replaces old pods with new ones, keeping hardware overhead bounded by `maxSurge` (e.g., $+20\%$). |
| **Workload C (Checkout API)** | **Canary Release** | High business risk where discount/pricing bugs may only show under real user traffic diversity. Route 5% live traffic to canary pods to analyze real-world error rates, p99 latency, and checkout completion metrics before broad promotion. |

### 2. Kubernetes Rolling Update Configuration for Workload B
To satisfy the constraints (target capacity never drops below 100% of desired 60 replicas, compute surge stays below 25%, and slow JVM initialization is accommodated):

```yaml
spec:
  replicas: 60
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 15          # 25% of 60 pods = 15 extra pods max during rollout
      maxUnavailable: 0     # Guarantees active ready capacity never falls below 60 pods
  minReadySeconds: 45       # Wait buffer where pod must stay Ready without crashing before next pod batch
  template:
    spec:
      containers:
        - name: aggregator
          image: aggregator:v2.0
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 5
            failureThreshold: 3
          lifecycle:
            preStop:
              exec:
                command: ["sh", "-c", "sleep 15"] # Connection draining buffer
```

- **`maxUnavailable: 0`**: Ensures the deployment controller scales up new v2 pods *before* terminating any old v1 pods.
- **`maxSurge: 15` (or `25%`)**: Caps additional memory/CPU resource allocation during rollout to $\le 25\%$.
- **`readinessProbe` + `minReadySeconds: 45`**: Pods will not receive production traffic until Spring context, JVM caches, and DB pools are warmed up. `minReadySeconds: 45` forces the rollout controller to monitor stability for 45s before proceeding to the next batch.

### 3. Blue-Green Rollback Speed & Network Mechanics
- **Why Blue-Green is faster to roll back than Rolling Update:**
  - In a **Rolling Update**, rolling back requires redeploying the previous image pod-by-pod across the fleet, pulling container images, starting JVMs, and waiting for readiness probes, taking several minutes for 60 pods.
  - In **Blue-Green**, the old (Blue) environment remains fully scaled, warm, and idle. Rollback requires only flipping the network router/load balancer pointer back to the Blue target group, completing in $< 1\text{ second}$.
- **Network Switch Mechanism:**
  - Avoid raw DNS switching for instant rollbacks because DNS caching across client resolvers and ISPs introduces unpredictable propagation lag (TTL violations).
  - Use **Load Balancer Target Group / Ingress Weighted Routing** (e.g., AWS ALB Target Group flip, NGINX Ingress upstream switch, or Envoy virtual host routing). The switch executes atomically in the data plane.

---

## Solution: automated-canary-abort-decision-engine - Automated Canary Telemetry Analysis & Abort Decision Logic

### 1. Interval Evaluation & Metric Breach Analysis

- **$t_1$ (2 min): [PASSED]**
  - Canary 5xx ($0.08\%$) vs Baseline ($0.05\%$): delta $+0.03\% \le +0.5\%$, and $< 1.0\%$.
  - Canary p99 ($118\text{ms}$) vs Baseline ($110\text{ms}$): degradation is $7.2\% \le 15\%$, and $< 200\text{ms}$.
  - Restarts: 0. Order success rate: $99.7\%$ (normal).
  - Consecutive Failure Count: `0`.

- **$t_2$ (4 min): [FAILED]**
  - Canary 5xx ($1.45\%$) breaches absolute limit ($> 1.0\%$) and delta limit ($+1.39\% > +0.5\%$).
  - Canary p99 ($225\text{ms}$) breaches absolute limit ($> 200\text{ms}$) and degradation limit ($+100.8\% > 15\%$).
  - Order success rate dropped to $96.2\%$.
  - Consecutive Failure Count: `1`.

- **$t_3$ (6 min): [FAILED]**
  - Canary 5xx ($1.80\%$) breaches both 5xx limits.
  - Canary p99 ($240\text{ms}$) breaches both latency limits.
  - Order success rate dropped to $95.1\%$.
  - Consecutive Failure Count: `2`.

- **$t_4$ (8 min): [FAILED]**
  - Canary 5xx ($1.65\%$) breaches both 5xx limits.
  - Canary p99 ($230\text{ms}$) breaches both latency limits.
  - Order success rate: $95.5\%$.
  - Consecutive Failure Count: `3` (equals `failureLimit: 3`).

### 2. Abort Trigger Timestamp
The automated abort triggers immediately at **$t_4$ (8 minutes)** when the consecutive evaluation failure count reaches `3` (`failureLimit: 3`). Note that $t_5$ will never execute because the rollout is halted and aborted at $t_4$.

### 3. Controller & Ingress Abort Execution Sequence
1. **Zero Canary Traffic Ingress Weight:** The rollout controller immediately updates the Ingress / Service Mesh virtual service configuration, setting canary traffic weight from $5\% \to 0\%$ and baseline weight to $100\%$.
2. **Connection Draining (`preStop`):** Existing in-flight requests on canary pods are allowed to finish within the termination grace period.
3. **Lock Rollout Phase:** Controller marks the rollout status as `Failed (ProgressDeadlineExceeded / MetricThresholdBreached)` and halts any further promotion steps.
4. **Scale Down & Alerting:** Canary ReplicaSet is scaled down to 0 replicas (or kept at 1 isolated replica in a debug/no-traffic state for crash dump inspection), and high-priority PagerDuty/Slack notifications are emitted with metric breach telemetry.

### 4. Why Comparative Baseline Analysis is Necessary
- **Eliminates False Positives from Upstream/Global Outages:** If an upstream dependency (e.g., payment vendor or database) slows down or fails, baseline 5xx and p99 latency will spike concurrently. A comparative evaluation ($\Delta \text{Canary} - \text{Baseline}$) avoids rolling back healthy code due to external infrastructure incidents.
- **Adapts to Natural Traffic Waves:** Diurnal traffic spikes (e.g., peak lunch hours) naturally increase latency across all instances. Comparing against real-time baseline ensures canary performance is evaluated relative to actual runtime conditions.

---

## Solution: expand-contract-database-migration - Zero-Downtime Expand-Contract Schema & App Rollout Staging

### 1. 5-Phase Deployment Sequence

```
Phase 1: DB Expand        --> Phase 2: App v1.1 Deploy --> Phase 3: Async Backfill
(Add nullable JSONB col)      (Dual-write both formats)     (Populate legacy rows)
                                                                     |
Phase 5: DB Contract      <-- Phase 4: App v1.2 Deploy <-------------+
(Drop legacy VARCHAR col)     (Switch reads to JSONB)
```

| Phase | Database Action | App Version Released | Read Behavior | Write Behavior |
| :--- | :--- | :--- | :--- | :--- |
| **Phase 1: DB Expand** | `ALTER TABLE orders ADD COLUMN shipping_address_json JSONB NULL;` | App v1.0 (unmodified) | Reads `legacy_shipping_address` | Writes `legacy_shipping_address` |
| **Phase 2: App Deploy (Dual Write)** | None (DB has both columns) | Release App v1.1 | Reads `legacy_shipping_address` | **Dual-writes** to both `legacy_shipping_address` and `shipping_address_json` |
| **Phase 3: Async Backfill** | Batch update script populates legacy rows where `shipping_address_json IS NULL` | App v1.1 (still active) | Reads `legacy_shipping_address` | Dual-writes both |
| **Phase 4: App Deploy (Switch Reads)** | None | Release App v1.2 | **Reads `shipping_address_json`** | Writes `shipping_address_json` (and optionally legacy column if rollback safety needed) |
| **Phase 5: DB Contract** | `ALTER TABLE orders DROP COLUMN legacy_shipping_address;` | App v1.2 (active fleet) | Reads `shipping_address_json` | Writes `shipping_address_json` |

### 2. Initial Expand Phase DDL & `NULL` Constraint
```sql
-- Phase 1 Migration Script (Flyway / Liquibase)
ALTER TABLE orders
ADD COLUMN shipping_address_json JSONB NULL;
```
- **Why it must be `NULL` (or have a default):**
  - If a column is added as `NOT NULL` without a default value, existing rows in the table cause the DDL statement to fail immediately.
  - Furthermore, running instances of App v1.0 know nothing about `shipping_address_json` and do not include it in `INSERT` statements. If the column were `NOT NULL`, all subsequent writes from App v1.0 would fail with a constraint violation.

### 3. Non-Blocking Async Backfill Engineering
To backfill millions of legacy records safely on production:
- **Cursor-Based Chunking:** Update records in discrete primary key batches (e.g., 500–1,000 rows per batch) rather than a single massive `UPDATE` statement:
  ```sql
  UPDATE orders
  SET shipping_address_json = json_build_object('street', legacy_shipping_address, 'city', 'Unknown', 'zip', '00000')
  WHERE id BETWEEN :startId AND :endId
    AND shipping_address_json IS NULL;
  ```
- **Sleep / Throttle Buffer:** Add a pause (e.g., 50ms) between batches to allow the DB engine to flush WAL, prevent table lock starvation, and avoid replication lag on read replicas.
- **Idempotency:** Only target records where `shipping_address_json IS NULL` to avoid overwriting newer rows already dual-written by App v1.1.

### 4. Rollback Decision Matrix

| Failure Point | Manifested Issue | Recovery Action | DB Schema Rollback Required? |
| :--- | :--- | :--- | :--- |
| **Scenario A (Phase 2 App v1.1 failure)** | Memory leak / crash in dual-write logic | Roll back application from v1.1 to v1.0. | **No.** App v1.0 ignores the new `shipping_address_json` column; no DDL change needed. |
| **Scenario B (Phase 3 Backfill failure)** | Data parsing error / corrupted legacy strings | Pause/stop the backfill worker job. Debug and fix script, then resume chunking. | **No.** Running apps (v1.1) continue reading from `legacy_shipping_address` and dual-writing. |
| **Scenario C (Phase 4 App v1.2 failure)** | JSON deserialization bug or slow JSON query plan | Roll back application from v1.2 to v1.1. | **No.** Because App v1.1 dual-wrote both columns and backfill completed, `legacy_shipping_address` is completely up-to-date and consistent. |
| **Post Phase 5 (Contract executed)** | Dropping column breaks unexpected third-party reporting query | Must restore column from schema backup or point-in-time recovery. | **Yes.** Once a column is dropped (`DROP COLUMN`), schema rollback is irreversible without data restoration. |
