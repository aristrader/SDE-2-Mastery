---
order: 10
search: false
---

# Technical Writing Exercises

## Exercise: pr-description-migration - Refactor a High-Risk Pull Request Description

Review this pull request submitted by a teammate touching payment processing and database storage:

> **PR #418: Updates to ledger service**
>
> **Description:**
> Fixed billing concurrency issue and updated ledger table. Added database index and changed column schema. Tested locally on my machine.

The pull request actually introduces the following changes:
1. Alters column `account_balance` from `INTEGER` to `NUMERIC(18, 4)` on a PostgreSQL table with 45 million rows.
2. Changes transaction isolation from `READ COMMITTED` to `SERIALIZABLE` on the payment settlement path.
3. Publishes a new `PAYMENT_SETTLED` event to Kafka inside the database transaction.

### Tasks
1. Identify four critical operational hazards omitted by this PR description.
2. Explain the database lock and deployment failure risk of the direct column type change.
3. Rewrite this PR description following the production 5-point template (Context & Problem, Solution & Changes, Migration & Database Safety, Rollback Plan, Verification).

---

## Exercise: incident-status-cadence - Structure Real-Time Outage Communication

At 14:10 UTC, your backend monitoring triggers a P1 alert: Order Service HTTP 504 Gateway Timeouts have surged from 0.01% to 18.4%. Checkout success rate drops from 99.9% to 72%. At 14:20 UTC, the on-call engineer posts this in the shared cross-team `#incidents` Slack channel:

> "Looking into 504s. DB seems slow. Might restart the pods soon."

### Tasks
1. Identify three severe communication failures in this message and explain how each harms incident triage.
2. Draft a professional **T+15 Initial Incident Update** suitable for cross-functional stakeholders (engineering leads, customer support, product managers).
3. At 14:35 UTC, investigation reveals that a newly deployed background report query saturated the database connection pool. The team decides to kill the slow query, scale read replicas, and temporarily disable the background report feature flag. Draft the **T+30 Progress and Mitigation Update**, including clear next-step timing and rollback criteria.

---

## Exercise: rfc-goals-and-alternatives - Eliminate Buzzwords and Define Boundaries

You are reviewing a proposal to decouple synchronous REST calls between `OrderService` and `InventoryService`. The draft RFC contains this overview:

> "We will leverage a hyper-scalable, next-generation event-driven paradigm using enterprise Kafka streaming to holistically decouple downstream microservice dependencies and achieve infinite horizontal scalability."

### Tasks
1. Rewrite this problem statement in clear, objective backend engineering terms without buzzwords. Quantify the underlying engineering problem (coupling, latency cascading, failure domains).
2. Write three explicit **Goals** and three explicit **Non-Goals** for the RFC to define strict architectural boundaries.
3. Compare the proposed approach (Transactional Outbox + Kafka) against one viable alternative (Idempotent REST with Resilience4j Circuit Breaker and Retry). Present a structured trade-off comparison covering consistency, operational complexity, failure modes, and recovery.

---

## Exercise: code-comment-audit - Distinguish Intent and Invariants from Noise

Review this Java snippet managing a cache and token-bucket rate limiter:

```java
// Check if user is null
if (userId == null || userId.isBlank()) {
    throw new IllegalArgumentException("User ID must not be blank");
}

// Increment counter by one
long currentRequests = requestCounter.incrementAndGet();

// Check if request count exceeds limit
if (currentRequests > MAX_REQUESTS_PER_MINUTE) {
    // Return false because rate limit is exceeded
    return false;
}

// Workaround for Redis driver race condition in netty-transport 4.1.92:
// connection pool can close the socket prematurely during thread interrupt,
// so we execute the sync pipeline in a guarded block rather than propagating interrupt.
return executeWithGuardedSocket(userId);
```

### Tasks
1. Audit each of the five comments: classify each as **Redundant Noise** (delete), **Unclear Code Symptom** (refactor code and delete), or **Valid Intent/Invariant** (retain).
2. State the guiding rule for when a code comment is technically required versus when it indicates poor code quality.

---

## Trick questions / gotchas

**Q. Does a detailed PR description replace self-documenting code?**
A. No. Code explains *how* the implementation works mechanically. The PR description explains *why* the change happened now, architectural context, deployment safety, and rollback strategy that cannot be inferred from a git diff.

**Q. Should incident updates provide root causes immediately?**
A. No. Speculating on root causes during an active incident causes confirmation bias and misleads responders. Incident updates report observable symptoms, blast radius, current investigative hypothesis, and mitigation actions.

**Q. Why are Non-Goals often more important than Goals in an RFC?**
A. Non-Goals prevent scope creep and align stakeholders on what the system will *not* solve, preventing premature over-engineering and unaligned expectations.

---

## Quick recall

**Q. What are the five key sections of a production PR description?**
A. Context/Problem, Changes Made, Migration & Deployment Safety, Rollback Plan, and Verification/Testing.

**Q. What is the core rule for real-time incident status updates?**
A. State symptoms, measurable blast radius, immediate action, and next update time—never speculate without data.

**Q. What earns a code comment its place?**
A. Non-obvious *why* (architectural decisions, vendor bug workarounds, concurrency/memory ordering invariants), not *what* the syntax obviously does.
