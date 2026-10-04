---
order: 20
search: false
---

# Solution

## Solution: design-doc-section-mapping - Design Document Structure and Section Mapping

### Section Mapping and Rationale

1. **Item A (No interactive visual analytics dashboard):**
   - **Section:** `Non-Goals`
   - **Rationale:** Explicitly stating out-of-scope capabilities prevents stakeholder scope creep and clarifies that engineering effort is strictly focused on data ingestion, retention, and raw compliance export rather than frontend tooling.

2. **Item B (Evaluation of DynamoDB vs. TimescaleDB vs. Kafka + S3 Glacier):**
   - **Section:** `Alternatives Considered & Tradeoffs`
   - **Rationale:** Demonstrates sound engineering judgment by documenting evaluated storage alternatives, justifying the chosen architecture based on append-only characteristics and long-term cost per TB while acknowledging lookup latency tradeoffs.

3. **Item C (Concurrent export worker starvation of real-time ingestion):**
   - **Section:** `Risks & Mitigations`
   - **Rationale:** Identifies non-happy-path failure modes (noisy neighbors / resource starvation) and prompts the design of mitigations (e.g., separate thread pools, rate limits, or decoupled Kafka consumer groups) before implementation.

4. **Item D (Asynchronous POST `/v1/audit-exports` and polling S3 pre-signed URL):**
   - **Section:** `Proposed Design (APIs & Architecture)`
   - **Rationale:** Defines the concrete interface contract, asynchronous HTTP status semantics (HTTP 202 Accepted), and data flow so downstream consumers and frontend clients can review and integrate against clear specifications.

5. **Item E (Canary deployment to 5%, metric verification, and feature flag rollback):**
   - **Section:** `Rollout Plan & Rollback Strategy`
   - **Rationale:** Establishes safe, phased production rollout gates with measurable observability health checks and zero-downtime rollback switches, minimizing blast radius during production deployment.

---

## Solution: rfc-critique-and-tradeoff-analysis - RFC Technical Review and Tradeoff Analysis

### Architectural Gaps and Constructive RFC Feedback

1. **Reviewer 1 (Dual-write / atomic consistency gap):**
   - **Architectural Gap:** Dual-write anomaly where database write and Kafka publish are not atomic. If the DB commits but Kafka publish fails (or vice versa), the system enters an inconsistent state.
   - **Constructive RFC Feedback:**
     > **blocking (consistency / reliability):** Publishing directly to Kafka within the checkout service without distributed transaction guarantees risks dual-write inconsistency (e.g., database transaction rolls back after the event is sent, or the event fails to publish after commit). Let's adopt the **Transactional Outbox Pattern** (persisting the event in an `outbox` table within the same DB transaction and relaying via Debezium/Kafka Connect or a polling publisher) to ensure at-least-once message delivery.

2. **Reviewer 2 (Ordering and partitioning gap):**
   - **Architectural Gap:** 3 Kafka partitions without an explicit partition key or idempotency strategy can lead to out-of-order event consumption across consumer threads (e.g., processing `PaymentFailed` or `OrderCancelled` before `OrderPlaced`).
   - **Constructive RFC Feedback:**
     > **suggestion (scalability / ordering):** To ensure per-order sequential delivery across partition rebalances and consumer scaling, please explicitly specify `orderId` as the Kafka message partition key. Additionally, consumers (`InventoryService`, `PaymentService`) must implement idempotency checks (e.g., storing processed `eventId` in a deduplication table) to handle at-least-once redelivery safely.

3. **Reviewer 3 (Security and PII compliance gap):**
   - **Architectural Gap:** Broadcasting unencrypted sensitive data (PII or payment credentials) across shared Kafka topics violates compliance standards (GDPR/PCI-DSS) and expands audit attack surfaces.
   - **Constructive RFC Feedback:**
     > **blocking (security / compliance):** Emitting raw customer PII or payment tokens onto shared Kafka topics violates data-minimization and compliance boundaries. The RFC should specify a schema that carries only opaque entity identifiers (e.g., `orderId`, `customerId`) with consumers fetching authorized details via secured APIs ("Claim Check Pattern"), or require envelope field-level encryption with dedicated KMS keys for sensitive fields.
