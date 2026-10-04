---
order: 10
search: false
---

# Exercise

## Exercise: design-doc-section-mapping - Design Document Structure and Section Mapping

### Problem Statement
An SDE2 is proposing a new asynchronous Audit Logging & Compliance Export Service to satisfy regulatory requirements (GDPR/SOC2). The system must ingest 25,000 immutable audit events/sec, retain data for 7 years in tiered cold storage, and provide asynchronous CSV export downloads via pre-signed S3 URLs.

Review the following proposed items and map each item to its most appropriate design doc section:

1. **Item A:** *"We will not build an interactive web-based query and visualization dashboard in this milestone; users requiring custom ad-hoc visual analytics must query data through the existing data warehouse Athena connector."*
2. **Item B:** *"We evaluated DynamoDB with TTL vs. PostgreSQL with TimescaleDB vs. Apache Kafka + S3 Glacier deep archive. Kafka + S3 Glacier was chosen because audit logs are append-only and long-term cost per TB is 90% lower, despite lack of sub-second random key point lookups."*
3. **Item C:** *"The compliance export worker pool could be overwhelmed if multiple enterprise tenants request full 7-year multi-gigabyte data dumps concurrently, potentially starving real-time audit ingestion queues."*
4. **Item D:** *"Expose an asynchronous POST `/v1/audit-exports` triggering an export job and returning `exportId` with HTTP 202 Accepted; poll or webhook via GET `/v1/audit-exports/{exportId}` returning pre-signed S3 download URL when status is `COMPLETED`."*
5. **Item E:** *"Canary release to 5% of internal tenants, verify zero dropped log events via dead-letter queue (DLQ) alerts, followed by progressive 25% -> 50% -> 100% rollout over 48 hours with instant rollback capability via feature flag `audit.v2.ingestion.enabled`."*

### Requirements & Tasks
1. Map each item (A through E) to the canonical design document section:
   - `Non-Goals`
   - `Proposed Design (APIs & Architecture)`
   - `Alternatives Considered & Tradeoffs`
   - `Risks & Mitigations`
   - `Rollout Plan & Rollback Strategy`
2. For each mapping, provide a 1–2 sentence explanation of why placing this item in that section is essential for cross-functional engineering and stakeholder review.

---

## Exercise: rfc-critique-and-tradeoff-analysis - RFC Technical Review and Tradeoff Analysis

### Problem Statement
You are reviewing an RFC proposing a change from synchronous REST communication to an event-driven messaging architecture for an e-commerce checkout and order fulfillment pipeline.

The author proposes:
> *"Whenever an order is placed, `OrderService` will publish an `OrderPlaced` event to Kafka topic `orders.events` with 3 partitions and retention of 7 days. `InventoryService`, `PaymentService`, and `NotificationService` will consume this event independently. If `PaymentService` fails due to insufficient customer funds, it will publish `PaymentFailed`, and `OrderService` will listen to cancel the order."*

During the RFC review meeting, three engineers raise questions:
- **Reviewer 1 (SRE):** *"How do we guarantee that `OrderPlaced` is never published to Kafka if the database transaction inserting the order in `OrderService` rolls back due to a constraint violation?"*
- **Reviewer 2 (Architect):** *"How do we handle out-of-order event delivery if partition scaling or consumer restarts occur?"*
- **Reviewer 3 (Security/Compliance):** *"The RFC proposal does not mention how sensitive payment tokens and customer PII are handled in event payloads."*

### Requirements & Tasks
1. Identify the missing architectural patterns and gaps in the proposed RFC design for all three reviewer concerns.
2. Formulate constructive, high-leverage RFC review feedback for each reviewer comment, proposing concrete production-grade solutions (e.g., Transactional Outbox pattern, partitioning/idempotency keys, payload sanitization/encryption).
