---
order: 40
---

# Release & Deployment Strategies

Zero-downtime deployment ensures users continue using the application while deployment happens. Each deployment strategy evolved to address limitations of the previous ones.

## Rolling Deployment
Gradually replacing instances of the old version with the new version.
- **How it works:** Update one server at a time (e.g., in Kubernetes, 19 old, 1 new → 18 old, 2 new).
- **Advantages:** Low infrastructure cost (reuses existing servers).
- **Disadvantages:** Slower rollback (multiple versions may already exist simultaneously). Old and new versions coexist, requiring backward compatibility.
- **Connection Draining:** Before stopping an old instance, tell the Load Balancer to stop sending new requests to it. Existing requests finish, and then the instance is updated.

### Rollout Controller Behavior & Pod Lifecycle Mechanics
In container orchestrators like Kubernetes, rolling updates are managed declaratively via Deployment controllers managing two ReplicaSets (old and new):
- **`maxSurge`:** The maximum number of pods that can be created above the desired replica count during the update (e.g., `25%` or absolute count). Ensures you never drop below target capacity.
- **`maxUnavailable`:** The maximum number of pods that can be unavailable during the update (e.g., `0%` or `25%`). Setting `maxUnavailable: 0` guarantees full capacity throughout the rollout.
- **`minReadySeconds`:** The warm-up buffer period a newly created pod must stay continuously "Ready" without crashing before the controller marks it available and moves to the next pod.
- **`progressDeadlineSeconds`:** The controller watchdog timeout (default 600s). If the rollout cannot make progress within this window (e.g., `CrashLoopBackOff`, `ImagePullBackOff`, failing probes), it flags `ProgressDeadlineExceeded` to halt the rollout.
- **Controller Action Sequence:**
  1. Creates new ReplicaSet v2.
  2. Scales up ReplicaSet v2 by `maxSurge` pods.
  3. Waits for new pods to pass startup/readiness probes and satisfy `minReadySeconds`.
  4. Removes old pods from Service endpoints (starts connection draining / `preStop` hook).
  5. Scales down ReplicaSet v1 pods in batches bounded by `maxUnavailable`.
  6. Repeats until ReplicaSet v2 reaches 100% desired replicas and v1 scales to 0.

## Blue-Green Deployment
Atomic traffic switch between two identical environments.
- **How it works:** Blue environment (v1) serves 100% traffic. Green environment (v2) is deployed and tested. Traffic switches 100% instantly to Green via DNS or load balancer routing rules.
- **Advantages:** Very fast deployment. Very fast rollback (point traffic back to Blue). Operational simplicity.
- **Disadvantages:** High infrastructure cost (duplicate environment required). Higher exposure if a bug exists.

## Canary Release
Gradual traffic shift using percentages to limit blast radius.
- **How it works:** 99% Blue, 1% Green → 95% Blue, 5% Green → ... → 100% Green.
- **Advantages:** Lowest user impact. Detects hidden production issues. Observe metrics before full rollout.
- **Disadvantages:** Medium infrastructure cost. Traffic splitting required. More monitoring and operational complexity.
- **Note:** "Canary" usually refers to server-side traffic splitting. Client-side staged rollout (e.g., Play Store 1% rollout) is similar in philosophy but usually called phased adoption.

### Automated Canary Analysis & Abort/Rollback Metric Criteria
Modern progressive delivery systems (e.g., Argo Rollouts, Flagger, Kayenta) monitor real-time telemetry from Prometheus/Datadog and execute automated aborts when metrics breach defined guardrails:
- **Telemetry & Metric Guardrails:**
  - **Error Rate:** HTTP 5xx / gRPC non-OK status rate (e.g., abort if canary 5xx > 1% or exceeds baseline by +0.5%).
  - **Latency Regression:** p95 / p99 request duration (e.g., abort if p99 latency exceeds 250ms or degrades > 15% vs baseline).
  - **Runtime & Saturation:** Pod restart count > 0 (`CrashLoopBackOff`), Out-of-Memory (`OOMKilled`) events, JVM GC pause spikes, CPU throttling > 20%.
  - **Business Invariants:** Drop in successful checkout/order creation rate, payment processing failure spikes.
- **Analysis Mechanics:**
  - **`failureLimit`:** Number of consecutive metric calculation failures required before triggering an abort (e.g., `failureLimit: 3`). Prevents transient network blips from triggering false rollbacks.
  - **Traffic Abort Execution:** When an abort is triggered, the controller immediately cuts canary ingress weight to 0%, redirects all traffic back to the stable baseline, and marks the rollout failed without manual intervention.

## Health Checks
Deployment completed does not mean the application is ready. Frameworks like Spring Boot take time to start (JVM, Spring Context, DB, Cache, etc.).
- **Readiness Probe:** Can this instance receive production traffic? (e.g., connected to DB, Redis, Kafka). If `NOT READY`, traffic stops, but the process isn't restarted.
- **Liveness Probe:** Should this process continue running? (e.g., application deadlocks, infinite loop). If `NOT ALIVE`, the platform restarts the process.
- **Startup Probe:** Delays liveness probe during startup to prevent premature restarts for slow-starting applications.

## Feature Flags
Separate deployment from release. Deploy code with the feature OFF, then enable it later without redeployment.
- **Uses:** Gradual rollout (employees, beta users), kill switch (immediately disable a crashing feature), A/B testing (50% old UI, 50% new UI).
- **Downsides:** Technical debt, more conditionals, more testing complexity. Stale flags should be cleaned up.

## Backward-Compatible APIs
Because deployments are gradual and old/new clients coexist, API changes must avoid breaking existing consumers.
- **Rule 1:** Add fields. Don't remove existing ones.
- **Rule 2:** Don't rename fields. Return both, deprecate later, remove in a future version.
- **Rule 3:** Don't change meaning or semantics (e.g., price from dollars to cents).
- **Rule 4:** Be careful with required fields. Make new fields optional initially.
- **Rule 5:** Version APIs only when necessary (for breaking changes).

## Backward-Compatible Database Changes (Expand-Contract)
Never make a schema change that breaks currently running application versions. The golden rule for zero downtime is the Expand-Contract pattern (also known as Parallel Change, e.g., renaming `first_name` to `full_name`).
1. **Expand:** Add new column `full_name`. Old column remains.
2. **Dual Write:** Whenever data changes, write to both `first_name` and `full_name`.
3. **Backfill:** Run migration job to populate all existing rows for the new column.
4. **Switch Reads:** Read from `full_name` instead of `first_name`.
5. **Contract:** Stop dual writing and delete `first_name`.

Adding a `NOT NULL` column follows a similar pattern: add as nullable, backfill, then apply `NOT NULL` constraint.

### Safe Compatibility & Deployment Ordering Matrix
Executing database and application changes requires strict multi-step release staging across distinct deployments:

| Phase | Action | Database State | Application Version | Safe Rollback Target |
| :--- | :--- | :--- | :--- | :--- |
| **Phase 1: DB Expand** | Apply migration script | Add nullable column `full_name` / new table | App v1.0 (reads/writes `first_name`) | Drop new column if DB migration fails |
| **Phase 2: App Deploy (Dual Write)** | Release App v1.1 | Columns `first_name` and `full_name` exist | App v1.1 (reads `first_name`, dual-writes both) | Roll back App to v1.0 (v1.0 ignores `full_name`) |
| **Phase 3: Async Data Backfill** | Execute background batch job | All legacy rows populated with `full_name` | App v1.1 (still active) | Re-run or throttle backfill job |
| **Phase 4: App Deploy (Switch Reads)** | Release App v1.2 | Both columns populated and kept in sync | App v1.2 (reads `full_name`, writes both or `full_name`) | Roll back App to v1.1 |
| **Phase 5: DB Contract** | Apply migration script | Drop legacy column `first_name` | App v1.2 (no longer references `first_name`) | Once dropped, schema rollback requires restore |

## Rollback Strategies
- **Code Rollback:** Usually straightforward. Redeploy previous version (or switch back to Blue in Blue-Green).
- **Database Rollback:** Much harder and potentially irreversible if data is deleted. Design migrations (like Expand-Contract) to avoid DB rollback. If deployment fails, roll back the application but avoid rolling back the database unless absolutely necessary.

## Compact Realistic Delivery Walkthrough
Here is a complete, realistic production delivery workflow for an **Order Processing Service** undergoing a schema refactor and progressive canary release:

1. **Step 1: Expand Schema in Database**
   - Execute DB migration tool (Flyway/Liquibase): `ALTER TABLE orders ADD COLUMN shipping_address_json JSONB NULL;`
   - Existing running App v1.0 instances continue operating unaffected.
2. **Step 2: Deploy App v1.1 with Warmup & Probes**
   - CI pipeline builds container image with dual-write logic (writes old string format + new JSON format; reads old string format).
   - Deployment controller launches canary pod with `maxSurge: 25%` and `maxUnavailable: 0`.
   - Startup probe verifies Spring context and database connection pools are initialized.
   - Pod passes readiness probe (`GET /actuator/health/readiness`) and satisfies `minReadySeconds: 30`.
3. **Step 3: Canary Traffic Shift & Automated Telemetry Evaluation**
   - Service mesh / Ingress routes 5% of production traffic to the canary instance.
   - Analysis controller continuously evaluates telemetry against baseline over 10 minutes:
     - Error rate condition: 5xx rate $< 0.1\%$
     - Latency condition: p99 latency $< 150\text{ms}$
     - Pod health: 0 restarts (`CrashLoopBackOff` = 0)
4. **Step 4: Rollout Promotion vs Abort Handling**
   - **Happy Path:** Metrics pass $\rightarrow$ Traffic steps up $5\% \rightarrow 25\% \rightarrow 50\% \rightarrow 100\%$. Old pods drained and terminated.
   - **Abort Path (Simulated Bug):** If canary throws database syntax errors causing 5xx rate to hit 2.5% (exceeding `failureLimit: 3`), the automated analyzer aborts the rollout, resets routing weight to 0% for the canary, and keeps 100% traffic on the stable v1.0 pods.
5. **Step 5: Background Backfill & Contract Schema**
   - Run asynchronous batch backfill across legacy order rows.
   - Deploy App v1.2 switching reads to `shipping_address_json`.
   - Once all services run v1.2, execute final DB migration: `ALTER TABLE orders DROP COLUMN legacy_shipping_address;`.

## CI/CD Pipeline (High Level)
- **Continuous Integration (CI):** Developer → Git Push/Merge → CI → Build → Run Tests → Create Artifact. Every commit should remain deployable.
- **Continuous Delivery / Deployment (CD):** Deploy Artifact → Health Checks → Traffic Shift → Monitoring.

## Quick recall
**Q. Difference between Blue-Green and Canary?**
A. Blue-Green is an atomic switch of 100% traffic to a duplicate environment. Canary is a gradual traffic shift (1% → 5% → ...) to limit blast radius.

**Q. Readiness vs Liveness probe?**
A. Readiness checks if the application can receive traffic (e.g., DB connected). Liveness checks if the process should continue running or be restarted (e.g., deadlocked).

**Q. Why not rename a DB column directly in production?**
A. Old and new application versions coexist during deployment. Renaming directly breaks the older versions that still query the old column name.

**Q. 5 steps of Expand-Contract DB migration?**
A. (1) Expand (add new), (2) Dual Write (write to both), (3) Backfill (populate existing), (4) Switch Reads, (5) Contract (delete old).

**Q. What role do `maxSurge` and `maxUnavailable` play in Kubernetes rolling updates?**
A. `maxSurge` sets how many extra pods can run above target replica count during update; `maxUnavailable` sets how many pods can be taken offline. Setting `maxUnavailable: 0` ensures 100% baseline capacity is maintained.

**Q. What key metric criteria trigger an automated canary abort?**
A. Breaches in error rate thresholds (HTTP 5xx / gRPC error spikes), p95/p99 latency regressions, container crash loops/restarts, or drops in core business metrics (e.g. order completion rate) across consecutive evaluation intervals (`failureLimit`).


