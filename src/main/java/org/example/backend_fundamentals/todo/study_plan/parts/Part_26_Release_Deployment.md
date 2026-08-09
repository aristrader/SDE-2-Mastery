# Part 26 — Release & Deployment Strategies

> **Sprint allocation:** Light touch — pass through during overflow days. **Budget: ~1-2 hrs (overflow slot).**

## 26 Release & Deployment Strategies — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 1 | Blue-green deployment — atomic traffic switch (Basic concept only) | 🔴 💼 🎯 | MP | 15 min | [x] | [ ] | [ ] | [ ] | ~25 min | (Cross-ref Part 22 deployments)<br>📖 `deployments/index.md` |
| 2 | Canary release — small % first, gradual rollout (Basic concept only) | 🔴 💼 🎯 | MP | 15 min | [x] | [ ] | [ ] | [ ] | ~25 min | 📖 `deployments/index.md` |
| 3 | Rolling deployment — replace instances gradually (Basic concept only) | 🔴 💼 🎯 | MP | 15 min | [x] | [ ] | [ ] | [ ] | ~25 min | 📖 `deployments/index.md` |
| 4 | Feature flags / toggles — release ≠ launch | 🔴 💼 🎯 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | ~25 min | 💻 Warm-up: simple feature flag via Spring `@ConfigurationProperties` + conditional bean; flip flag, reload app, observe behavior change (15 min)<br>📖 `deployments/index.md` |
| 5 | Backward-compatible API changes — add fields, never remove or rename | 🔴 💼 🎯 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | ~25 min | 📖 `deployments/index.md` |
| 6 | Backward-compatible DB changes — expand / contract (add → migrate → switch → remove) | 🔴 💼 🎯 | D | 2 hrs 30 min | [x] | [ ] | [ ] | [ ] | ~30 min | 💻 Warm-up: walk through renaming `username → user_name` via expand-contract (add new col, dual-write, backfill, switch reads, drop old) on paper (30 min)<br>📖 `deployments/index.md` |
| 7 | Rollback strategy for every change (incl. data migrations — hardest) | 🔴 💼 🎯 | D | 2 hrs | [x] | [ ] | [ ] | [ ] | ~25 min | 📖 `deployments/index.md` |
| 8 | Dark launches — code deployed but disabled (Basic concept only) | 🟠 💼 🎯 | M | 15 min | [ ] | [ ] | [ ] | [ ] | | |
| 9 | Shadow traffic — mirror prod to new service silently to compare (Basic concept only) | 🟠 💼 🎯 | MP | 15 min | [ ] | [ ] | [ ] | [ ] | | |
| 10 | Trunk-based development vs GitFlow | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 11 | Semantic versioning | 🟠 💼 | L | 30 min | [ ] | [ ] | [ ] | [ ] | | |
| 12 | Database migration tools — Flyway, Liquibase (idempotent migrations) | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: Flyway migration `V1__init.sql` + `V2__add_email_col.sql` running on Spring Boot startup (15 min) |
| 14 | Multi-version compatibility (rolling deploy implies N and N+1 coexist) | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 19 | Hot-fix workflow | 🟢 | M | 30 min | [ ] | [ ] | [ ] | [ ] | | |



## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~8.25 hrs | ~0.75 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~13 hrs | ~1.18 wk | |
| Full Part (all items including 🟡 + 🟢) | ~13.5 hrs | ~1.23 wk | ~3.0 hrs |

## Frequently asked

1. **Q:** Walk through expand-contract for renaming a DB column.
   - **Why asked:** Senior-canonical safe migration. Steps: (1) Add new column `user_name`. (2) Deploy code that writes to BOTH old and new (dual-write). (3) Backfill new from old. (4) Switch reads to new. (5) Deploy code that only writes new. (6) Drop old column. Each step is reversible. Total: 4-5 deploys. Slow but safe.
2. **Q:** Feature flags — when do they earn complexity?
   - **Why asked:** Modern release-launch decoupling. Worth it when: (1) you want to deploy ahead of launch (testing in prod with safety), (2) you need per-user / per-tenant rollout (KYC partners), (3) you want kill switches for fast rollback without redeploy, (4) you do A/B testing on real traffic.
3. **Q:** Canary release — what gates promotion to next %?
   - **Why asked:** Operational depth. Metric gates: error rate, latency p99, business KPIs (verification success rate). Watch a window (10-30 min) at each stage. Auto-rollback if regression. Manual promotion + auto-rollback is more common than fully-auto promotion. Document the metrics + thresholds.
4. **Q:** Rolling deploy of 10 instances. Instance 1 is on new code, instance 2 on old code. They share a Redis cache. What can go wrong?
   - **Why asked:** Multi-version compatibility. New code may write Redis values in incompatible format (different JSON schema). Old code reads, errors. Fix: backward-compatible serialization for at least one version overlap. Or: use separate cache keys per version.
5. **Q:** Your team has 1 deploy/quarter. DORA metrics — what's wrong?
   - **Why asked:** SRE maturity. Low deploy frequency correlates with high blast radius per deploy (big bang releases). Aim for high deploy frequency + small batch sizes + automated rollback. DORA elite: multiple deploys per day, MTTR < 1 hour.
6. **Q:** Walk through the rollback of a data migration that's halfway done.
   - **Why asked:** Hardest case. Data migrations often have no clean rollback — once you've transformed 5M rows, you can't un-transform without re-applying inverse logic. Mitigations: (1) expand-contract makes rollback = "undo the latest step", (2) shadow tables (keep old data, switch over), (3) accept-forward (re-run forward migration once issue is fixed), (4) point-in-time DB restore (drastic, last resort).
7. **Q:** Trunk-based development vs GitFlow — when each?
   - **Why asked:** Modern team practice. Trunk-based: short-lived feature branches (or commit directly to main), CI gates, feature flags for incomplete work. Right for high-velocity teams + automated testing. GitFlow: long-lived develop/release branches, complex merge ceremony. Older pattern, slower throughput. Most modern teams pick trunk.

## Trick questions / gotchas

1. **Q:** Your feature flag platform has 99.9% uptime. Your service depends on it for every request. What's your fallback?
   - **Gotcha:** Single-point-of-failure on flag platform. Each request blocking on flag eval = LF availability impacts your service. Mitigations: (1) SDK with local cache + last-known-good values, (2) bounded blocking time + default values on timeout, (3) periodic flag refresh, async from request path.
2. **Q:** You did blue-green with a stateful service (long-lived DB connections). Switched green→blue. Half the users got errors. Why?
   - **Gotcha:** In-flight requests. The atomic switch doesn't drain existing connections gracefully. Fix: (1) tell LB to drain green (stop accepting new, keep existing), (2) wait for in-flight to complete (configurable grace, typically 30-60s), (3) THEN cut new traffic to blue, (4) terminate green. Modern LBs (ALB, K8s Services) handle this with connection draining settings.
3. **Q:** You added a non-null column to a 10M-row table during deploy. The deploy took 20 minutes and the app was unavailable. What's wrong?
   - **Gotcha:** Adding NOT NULL column with default value requires updating every existing row → table lock or long migration. Right: (1) add nullable column first, (2) backfill in batches, (3) only then add NOT NULL constraint after all rows have value. Or use online schema change tools (gh-ost) that do it without locking.
4. **Q:** Your kill switch is in code: `if (KILL_SWITCH_ENABLED) return; ...`. The bug is in the code before the kill switch. The switch doesn't save you. Why?
   - **Gotcha:** Kill switch placement. If the bug is in code BEFORE the switch check, you've already failed. Kill switches need to be EARLY in the request path — ideally first-line in the controller / at the API gateway. Defense in depth: also have a circuit breaker that can disable a downstream call.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **Expand-contract DB migration end-to-end** (~3 hrs row 6) — the senior-canonical safe migration. Walk through it for a real KYC schema change.
- **Rollback strategy for data migrations** (~2.5 hrs row 7) — hardest case. Document patterns: shadow tables, accept-forward, PITR.
- **Feature flag architecture for KYC platform** (~2.5 hrs row 4) — kill switches per partner, gradual rollouts for new verification logic, A/B testing on UX.

## Hands-on exercises (Practice + Advanced)

Warm-up release exercises are listed inline in the topic-table Resources column (counted in main Time summary). Longer exercises below are tracked separately.

### Practice — mid-level (~30-60 min each)

1. **Flyway migration with rollback** (~45 min) — write `V1__create_users.sql`, `V2__add_email.sql`. Then write `U2__remove_email.sql` rollback. Apply, then rollback. Verify state.
2. **Feature flag with local-cache SDK** (~60 min) — Spring Boot + a flag client (or a simple in-memory one). Flag values served from local cache + refreshed every 30s. Service still works if flag platform is down (uses last-known-good).
3. **Expand-contract DB column rename walkthrough** (~60 min) — write each migration step as a SQL script + corresponding code change. Document the 5-step process for a real example.

### Advanced — senior-grade depth (~60+ min each)

4. **Multi-version compatibility test** (~90 min) — deploy old + new versions side-by-side. Send requests to both. Use a feature flag to route 50/50. Verify both versions handle each other's data correctly (e.g., new version reads old serialized cache, old version reads new).

### Hands-on time summary (Practice + Advanced only)

| Tier | Total time | Weeks @ 10–12 hrs/wk | Actual time |
|------|------------|----------------------|-------------|
| Practice (mid-level) | ~2.75 hrs | ~0.25 wk | |
| Advanced (senior-grade) | ~2.75 hrs | ~0.25 wk | |
| **Combined hands-on (Practice + Advanced)** | **~5.5 hrs** | **~0.5 wk** | |

> Warm-up exercises (counted in main Time summary above) total ~60 min for Part 26 across 3 in-table warm-ups.

## Quick recall

**Q. Expand-contract — 5 steps?**
A. (1) Add new column. (2) Dual-write old + new. (3) Backfill new from old. (4) Switch reads to new. (5) Stop writing old + drop. Each step is reversible.

**Q. Blue-green vs canary — one-line distinction.**
A. Blue-green: atomic switch between two identical envs. Canary: gradual traffic shift (1% → ... → 100%) with metric gates. Blue-green is fast rollback; canary is risk reduction.

**Q. DORA metrics?**
A. Deploy frequency (how often deploys happen), Lead time (commit-to-deploy time), MTTR (time to recover from incident), Change failure rate (% of deploys causing prod issues). Elite teams: multiple deploys/day, <1 hr MTTR.

**Q. Feature flag earns complexity when?**
A. Decouple deploy from launch, per-user/tenant rollout, kill-switch capability, A/B testing on real traffic.

**Q. Rolling deploy gotcha — multi-version?**
A. Old + new instances coexist during rollout. Both must handle each other's data (cache format, message schema, DB schema). Plan for N + N+1 compatibility.

**Q. Rollback of data migration — hardest case why?**
A. Data transformation is often not invertible. Mitigations: expand-contract (rollback per step), shadow tables, accept-forward (fix the bug + roll forward), point-in-time restore (last resort).
