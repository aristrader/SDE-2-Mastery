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

## Blue-Green Deployment
Atomic traffic switch between two identical environments.
- **How it works:** Blue environment (v1) serves 100% traffic. Green environment (v2) is deployed and tested. Traffic switches 100% instantly to Green.
- **Advantages:** Very fast deployment. Very fast rollback (point traffic back to Blue). Operational simplicity.
- **Disadvantages:** High infrastructure cost (duplicate environment required). Higher exposure if a bug exists.

## Canary Release
Gradual traffic shift using percentages to limit blast radius.
- **How it works:** 99% Blue, 1% Green → 95% Blue, 5% Green → ... → 100% Green.
- **Advantages:** Lowest user impact. Detects hidden production issues. Observe metrics before full rollout.
- **Disadvantages:** Medium infrastructure cost. Traffic splitting required. More monitoring and operational complexity.
- **Note:** "Canary" usually refers to server-side traffic splitting. Client-side staged rollout (e.g., Play Store 1% rollout) is similar in philosophy but usually called phased adoption.

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
Never make a schema change that breaks currently running application versions. The golden rule for zero downtime is the Expand-Contract pattern (e.g., renaming `first_name` to `full_name`).
1. **Expand:** Add new column `full_name`. Old column remains.
2. **Dual Write:** Whenever data changes, write to both `first_name` and `full_name`.
3. **Backfill:** Run migration job to populate all existing rows for the new column.
4. **Switch Reads:** Read from `full_name` instead of `first_name`.
5. **Contract:** Stop dual writing and delete `first_name`.

Adding a `NOT NULL` column follows a similar pattern: add as nullable, backfill, then apply `NOT NULL` constraint.

## Rollback Strategies
- **Code Rollback:** Usually straightforward. Redeploy previous version (or switch back to Blue in Blue-Green).
- **Database Rollback:** Much harder and potentially irreversible if data is deleted. Design migrations (like Expand-Contract) to avoid DB rollback. If deployment fails, roll back the application but avoid rolling back the database unless absolutely necessary.

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


