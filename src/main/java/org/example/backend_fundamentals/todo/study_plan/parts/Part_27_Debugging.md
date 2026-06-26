# Part 27 — Debugging & Production Troubleshooting

> **Sprint allocation:** Light touch — fold into spare slots. **Budget: ~1-2 hrs (overflow slot).**

## 27 Debugging & Production Troubleshooting — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 1 | Thread dumps — jstack, `kill -3`, capture under load | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: `jstack <pid>` on a running Spring Boot app, identify RUNNABLE / WAITING / BLOCKED states (20 min) |
| 2 | Reading thread dumps — find BLOCKED, identify lock contention, deadlocks | 🔴 💼 | D | 3 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 3 | Heap dumps — jmap, `-XX:+HeapDumpOnOutOfMemoryError` | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 4 | Heap analysis — Eclipse MAT (dominator tree, retained heap, leak suspects) | 🔴 💼 | D | 3 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: open a sample heap dump (or generate via `jmap -dump`) in Eclipse MAT, navigate to Leak Suspects report (30 min) |
| 6 | tcpdump basics, Wireshark for deep dives | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 7 | Slow query logs — MySQL slow_log, Postgres pg_stat_statements | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: enable `log_min_duration_statement = 1000` in Postgres, capture a slow query, read the log line (15 min) |
| 11 | 5 whys, fishbone (Ishikawa) analysis | 🔴 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 12 | Reproducing in staging / lower envs | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 13 | GC log analysis — gceasy.io, GCViewer | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 15 | JFR — continuous low-overhead profiling in prod | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 16 | jstat, jcmd, jinfo, jps — command-line forensics | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 17 | mitmproxy / Charles for HTTP intercept | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 18 | dig, nslookup — DNS forensics | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 19 | `ss` / `netstat` — open connections, TIME_WAIT, port exhaustion | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 20 | Lock waits, deadlock graph | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 21 | Replication lag investigation | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 22 | Auto-vacuum / bloat (Postgres) | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 23 | Bisecting bad commits (`git bisect`) | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 24 | Postmortem writing — blameless, action items with owners | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 26 | Knowing when to escalate vs continue investigating | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~23.58 hrs | ~2.15 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~43.58 hrs | ~3.95 wk | |
| Full Part (all items including 🟡) | ~45.08 hrs | ~4.1 wk | |

## Frequently asked

1. **Q:** Walk me through diagnosing a Java service that's "hung" in production.
   - **Why asked:** Daily senior triage skill. (1) `kill -3 <pid>` or `jstack <pid>` for thread dump. (2) Look for BLOCKED threads — what are they waiting on? (3) Look for cycles in lock contention. (4) Check thread pool exhaustion (all worker threads waiting on something). (5) Cross-check with `top` (CPU pinned vs idle), GC logs (full GC pause?), DB connection pool. (6) Get a heap dump if memory looks suspicious.
2. **Q:** Read this Postgres EXPLAIN. Estimated rows 100, actual rows 100,000. What's wrong?
   - **Why asked:** Diagnostic depth. Stale planner statistics. Fix: `ANALYZE <table>` to update. Cause: heavy write traffic since last autovacuum run, or skewed data distribution. May need `default_statistics_target` increase for that column.
3. **Q:** How do you narrow down a failure across 5 microservices?
   - **Why asked:** Distributed-systems debugging. (1) Use correlation ID from the failed request to trace through Datadog / OpenTelemetry. (2) Identify which span errored or had high latency. (3) Drill into that service's logs filtered by same correlationId. (4) Cross-check with that service's metrics during the same window. (5) Reproduce in staging if possible.
4. **Q:** Your service shows OOM in production every 2 weeks. How do you find the leak?
   - **Why asked:** Memory-leak diagnosis. (1) Enable `-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/heap.hprof`. (2) When OOM hits, copy the dump. (3) Open in Eclipse MAT. (4) Run "Leak Suspects" report — usually points at the culprit. (5) Drill via Dominator Tree to find what's retaining the suspect. Common: `ThreadLocal` not cleared, static `List` growing unbounded, cache without size limit.
5. **Q:** A spike of 5xx errors started 10 min ago. Walk me through your first 5 minutes.
   - **Why asked:** Incident response. (0) Acknowledge in incident channel. (1) Check obvious: recent deploys, recent config changes, recent infrastructure events (AWS console). (2) Check distributed tracing for which service is erroring. (3) Check that service's logs + metrics. (4) If unclear, get incident commander + comms separation in place. (5) Within 5 min, you should have a hypothesis or "need more time" decision.
6. **Q:** Your TCP connection pool to a downstream is full. How do you confirm + fix?
   - **Why asked:** Operational depth. Confirm: `ss -tn state established '( sport = :{port} )'` to count connections. Check HikariCP active vs idle metrics. Logs may show "connection acquisition timeout." Fix: depends on root cause. (1) Downstream slowed (mitigate: timeout + circuit breaker). (2) Connection leak (find unclosed Statement / Result). (3) Pool too small (increase based on Little's Law).
7. **Q:** Postgres shows replication lag > 30 seconds. What's likely happening?
   - **Why asked:** Replication operational issue. Causes: (1) Heavy write traffic on primary > replica can apply. (2) Long-running query on replica blocking WAL replay. (3) Network bandwidth between primary + replica. (4) Replica I/O saturation. Check `pg_stat_replication` on primary + `pg_stat_wal_receiver` on replica. Decision: route reads back to primary temporarily; investigate root cause.

## Trick questions / gotchas

1. **Q:** Your thread dump shows 200 threads all WAITING on the same object. The lock holder is not in the dump. Why?
   - **Gotcha:** Lock holder may be in a non-Java thread (native code, GC thread), or the dump was captured between the lock-release and another thread acquiring. Take 3-5 dumps 10 seconds apart — pattern over time reveals the actual contention.
2. **Q:** You ran `jmap -dump` on a production process. The process became unresponsive for 30 seconds. Why?
   - **Gotcha:** `jmap -dump:live` triggers a full GC + suspends the JVM for the dump duration. For 8GB heap, can be tens of seconds. In production: use `jcmd <pid> GC.heap_dump <file>` (newer API, less impactful) or pre-configure `-XX:+HeapDumpOnOutOfMemoryError` so dump only happens on OOM (when the process is dying anyway).
3. **Q:** A spike in latency. CPU is at 5%. RAM has lots of free. DB has no slow queries. Where's the latency?
   - **Gotcha:** Likely network (DNS resolution slow), downstream service (external API), thread pool exhaustion (all worker threads waiting), or GC pause (paused JVM doesn't show high CPU during pause). Look at trace spans, not raw resource metrics.
4. **Q:** Your service was sluggish. You restarted it. Performance returned. Did you fix the bug?
   - **Gotcha:** No — you cleared symptom, not root cause. Get a thread dump + heap dump BEFORE restarting (or even after — the restart loses evidence). "Reboot and shrug" is the SDE2 reflex; "capture then restart" is the senior move.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **Thread dump analysis fluency** (~3.5 hrs combined rows 1+2) — most senior daily skill. Practice with several real dumps. Understand BLOCKED, WAITING, deadlock detection output, lock chain reconstruction.
- **Heap dump leak hunting** (~3.5 hrs combined rows 3+4) — most production-critical OOM skill. Eclipse MAT navigation: Dominator Tree, retained heap, leak suspects, OQL queries.
- **Postmortem writing** (~2 hrs row 24) — STAR-style postmortem with blameless tone, action items + owners. Becomes promotion material when done well.
- **Incident response playbook for KYC verification failures** (~2.5 hrs combined) — runbook entries for top 5 likely failure modes. Test in a game day.

## Hands-on exercises (Practice + Advanced)

Warm-up debugging exercises are listed inline in the topic-table Resources column (counted in main Time summary). Longer exercises below are tracked separately.

### Practice — mid-level (~30-60 min each)

1. **Thread dump analysis on a deadlocked process** (~60 min) — write a small Java app that deadlocks two threads on two locks acquired in opposite order. Run, capture `jstack`, find "deadlock detected" report. Reconstruct the cycle. Fix via consistent lock ordering.
2. **Heap dump → leak hunt in Eclipse MAT** (~60 min) — write a small app that leaks (e.g., static List<byte[]> appending indefinitely). Generate heap dump. Open in MAT. Find leak suspect via Dominator Tree.
3. **Slow query log + EXPLAIN ANALYZE fix** (~45 min) — Postgres with `log_min_duration_statement = 100`. Run a query that does a sequential scan on 1M rows. Capture log. Add index. Verify EXPLAIN shows index scan. Confirm log no longer shows the slow query.

### Advanced — senior-grade depth (~60+ min each)

4. **Production-style debugging exercise** (~90 min) — set up a Spring Boot app with deliberate bugs (a memory leak + a thread deadlock + a slow query). Use the full toolbox: jstack, jmap, EXPLAIN, distributed tracing. Document each diagnosis step. Practice the workflow.
5. **Blameless postmortem template + walkthrough** (~75 min) — write a postmortem template (Summary, Timeline, Root cause, Detection, Mitigation, Action items + owners). Apply to a real (or simulated) incident. Get peer review on the tone and rigor.

### Hands-on time summary (Practice + Advanced only)

| Tier | Total time | Weeks @ 10–12 hrs/wk | Actual time |
|------|------------|----------------------|-------------|
| Practice (mid-level) | ~2.75 hrs | ~0.25 wk | |
| Advanced (senior-grade) | ~2.75 hrs | ~0.25 wk | |
| **Combined hands-on (Practice + Advanced)** | **~5.5 hrs** | **~0.5 wk** | |

> Warm-up exercises (counted in main Time summary above) total ~65 min for Part 27 across 3 in-table warm-ups.

## Quick recall

**Q. Thread dump diagnostic — first sweep?**
A. Count threads by state (RUNNABLE, WAITING, BLOCKED). Many BLOCKED on same monitor → lock contention. "Deadlock detected" line → JVM found a cycle. All worker threads WAITING on the same downstream → pool exhaustion or downstream slow.

**Q. Heap dump leak hunt — first tool?**
A. Eclipse MAT "Leak Suspects" report. Usually points directly at the leak. Drill via Dominator Tree to find what's retaining the suspect (often a static field or ThreadLocal).

**Q. EXPLAIN — what does estimated vs actual row mismatch mean?**
A. Planner statistics are stale or skewed. Run `ANALYZE` to update. If persistent, may need `default_statistics_target` increase or query rewrite.

**Q. Incident — first 60 seconds?**
A. (1) Acknowledge in channel. (2) Check recent deploys + config changes + infra events. (3) Form initial hypothesis or admit "investigating." Don't restart anything before capturing evidence.

**Q. Reboot vs root cause — senior reflex?**
A. Capture evidence (thread dump, heap dump) BEFORE restarting. Restart clears symptom but loses diagnostic info. Senior move: capture then restart, investigate root cause out-of-band.

**Q. 5 whys + fishbone — when each?**
A. 5 whys: linear root-cause drill from symptom backward. Fast, good for clear failures. Fishbone: multi-category cause exploration (people, process, technology, environment). Better for complex / multi-factor failures.
