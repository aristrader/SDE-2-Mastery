---
order: 10
---

# Technical Writing

## Purpose

Technical writing is about communicating technical ideas clearly, not about English proficiency.
**Core rule:** Optimize for the next engineer, not for sounding smart. Prioritize clarity over cleverness.

Every line of documentation, pull request description, or architecture proposal is an asynchronous interface contract. When you write ambiguously, you shift cognitive load, debugging time, and operational risk onto teammates and your future on-call self.

## Common Places for Technical Writing

- PR descriptions
- Design docs (RFCs / Architecture Decision Records)
- Jira tickets and bug reports
- README and developer onboarding guides
- API documentation (OpenAPI, contracts, error catalogs)
- Incident updates and postmortems
- Slack and async team communication

## Good vs Bad Writing

- **Bad:** "Fixed issue." (Unclear which issue, why, or how).
- **Good:** "Fixed duplicate notifications caused by concurrent retries. Added idempotency check before sending notifications and included regression tests." (Communicates the problem, cause, and solution).

| Document Type | Bad (Vague / Jargon-Heavy) | Good (Intentional / Actionable) |
|---|---|---|
| **PR Description** | "Updated billing logic and database." | "Added idempotency key check to `BillingService.charge()`. Prevents duplicate credit card charges on client timeout retries. Added Flyway migration script to index `idempotency_key`." |
| **Incident Update** | "Looking into it." | "SEV-1: High database latency (>450ms p99) observed following release v2.4. Investigating unindexed query on `orders` table. Rollback to v2.3 is primed if latency does not recover by 14:30 UTC." |
| **Architecture RFC** | "Leveraging a highly optimized distributed asynchronous architecture." | "Publishing checkout events to Kafka; background workers consume events to complete order fulfillment asynchronously, isolating checkout from downstream fulfillment latency." |
| **Bug Ticket** | "Login is broken." | "Login returns HTTP 500 when username contains a `+` symbol. Affects mobile clients on v3.2. Reproduction steps and stack trace attached." |

## Comments in Code

Don't explain what the code obviously does (e.g., `// Increment i`). Instead, explain *why* something exists (e.g., `// Retry only for transient failures to avoid duplicate notifications.`).

### When to Delete Comments
If a comment explains *what* the syntax is doing, refactor the code instead:
- **Bad:** `// Check if user is eligible for discount` followed by `if (u.getAge() > 60 && u.getOrders() > 5)`
- **Good:** `if (user.isEligibleForSeniorLoyaltyDiscount())` (self-documenting code without comments).

### When Comments Are Mandatory
Comments earn their place when they capture non-obvious context that the code cannot express:
1. **Concurrency and memory models:** Documenting happens-before relationships, CAS loop retry semantics, or thread safety invariants.
2. **Workarounds for external bugs:** Explaining why an unconventional pattern is required due to a specific vendor library bug or JDK quirk (with issue tracker link).
3. **Non-obvious domain or regulatory constraints:** Explaining a legal requirement or business rule that appears counter-intuitive in code.

```java
// Workaround for AWS S3 SDK v2 client connection leak (Issue #4102):
// The response stream must be explicitly drained and closed before releasing
// back to the HTTP connection pool, even on partial read errors.
drainAndCloseStream(responseStream);
```

## PR Description

A good PR description allows someone to understand the purpose without reading every file. It should answer:
- What changed?
- Why?
- Migration required?
- Rollback concerns?
- How was it tested?

### The 5-Point Production PR Template

```markdown
### 1. Context & Motivation
What problem does this solve? Link to Jira ticket or incident report.

### 2. Changes Made
High-level summary of architectural and code changes.

### 3. Database & Migration Safety
- Are there schema changes? (Yes/No)
- Does the migration acquire exclusive table locks?
- Is expand-contract followed to guarantee backward compatibility with older running replicas?

### 4. Rollback Plan
- Can this PR be safely rolled back?
- Are there feature flags or kill switches configured?
- What are the operational triggers for initiating a rollback?

### 5. Verification & Testing
- Unit and integration tests added?
- Load/stress test results (if performance-sensitive).
- Manual verification steps.
```

## Design Documents and Incident Updates

### Design Documents (RFCs and ADRs)
Avoid unnecessary buzzwords ("Leveraging a highly optimized distributed asynchronous architecture"). Be clear ("Messages are published to Kafka, and workers consume them asynchronously to improve throughput").

Effective design docs focus on decisions and boundaries:
- **Problem Statement:** Quantify the problem with metrics (e.g., "p99 latency increases from 50ms to 1,200ms when order volume exceeds 1,500 TPS").
- **Goals vs. Non-Goals:** Explicitly stating what the project will *not* solve is the single most effective tool against scope creep.
- **Alternatives Considered:** Document rejected solutions and specific technical reasons for rejection (e.g., "Rejected Redis Streams due to strict 7-year event retention compliance requirements").
- **Trade-offs:** Every architectural choice trades one quality attribute for another (e.g., trading immediate consistency for write availability).

### Incident Updates and Postmortems
Provide useful status updates. Instead of "Looking into it", use "High database latency observed after deployment. We're investigating slow queries. Rollback is ready if latency continues to increase."

- **Live Incident Cadence:** Post regular updates at fixed intervals (e.g., every 15-20 minutes during SEV-1). Include: Symptoms & Metrics, Current Investigation Path, Mitigation Action, and Time of Next Update.
- **Blameless Postmortems:** Focus on systemic vulnerabilities rather than human error. Distinguish between the **trigger** (e.g., "developer pushed bad config") and the **root cause** (e.g., "deployment pipeline lacked automated schema validation before rollout").

## Interview Relevance

Direct questions are uncommon, but interviewers evaluate technical writing indirectly through system design explanations, communication clarity, and structured thinking:
- **System Design Interviews:** Ability to frame requirements, distinguish functional vs non-functional goals, define explicit non-goals, and articulate technical trade-offs without buzzwords.
- **Behavioral Interviews:** Describing how you resolve cross-team disagreements via RFCs, communicate outage impact to leadership, or document architectural decisions.

## Boundaries and Anti-Patterns

- **Documentation Debt:** Documentation kept separate from code (e.g., outdated Confluence wikis) rapidly rots. Keep architectural decisions close to the codebase (e.g., Architecture Decision Records in git).
- **Over-Engineering Documentation:** A two-line typo fix does not need a five-section RFC. Calibrate documentation depth to the blast radius of the change.

## Takeaway

Write for someone who has never seen your code before. The goal is to reduce the number of questions future engineers need to ask.

## Sources & Further Reading

- **Google Technical Writing Courses** (Google Developers) — Principles of audience awareness, active voice, clarity, and structural hierarchy.
- **Martin Fowler on Technical Communication & Architecture** — Architecture Decision Records (ADRs) and self-documenting code.
- **IETF RFC 2119** — Key words for use in RFCs to Indicate Requirement Levels (MUST, SHOULD, MAY).
- **AWS Well-Architected Framework: Reliability Pillar** — Best practices for operational incident response and blameless postmortem culture.

## Quick recall

**Q. What is the core rule of technical writing?**
A. Optimize for the next engineer by prioritizing clarity over sounding smart.

**Q. What makes a good PR description?**
A. It answers what changed, why it changed, migration/rollback concerns, and how it was tested.

**Q. How should code comments be written?**
A. Explain *why* a piece of code exists (intent, invariants, workarounds), rather than stating what the code obviously does.

**Q. How should incident updates be communicated?**
A. Provide actionable context on symptoms, blast radius, current investigation focus, and next update time, rather than saying "Looking into it."

**Q. Why are explicit Non-Goals essential in an RFC?**
A. They define rigid scope boundaries, prevent premature over-engineering, and align stakeholders on what the proposal deliberately leaves unsolved.

**Q. What is the difference between an incident trigger and root cause?**
A. The trigger is the immediate catalyst (e.g. invalid config deployed); the root cause is the systemic flaw that allowed the trigger to cause failure (e.g. lack of schema validation in CI).
