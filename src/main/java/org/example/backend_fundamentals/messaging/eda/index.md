---
order: 30
---

# Event-Driven Architecture (EDA)

Event-Driven Architecture (EDA) is an architectural style built around the production, detection, and consumption of events. Instead of services asking each other to perform actions, services broadcast that an action has already occurred.

## The Pressure: The Orchestration Bottleneck

In a traditional synchronous REST architecture, processing a business workflow often relies on central orchestration. When a user places an order, the `Order Service` directly calls the `Inventory Service`, `Payment Service`, `Email Service`, and `Analytics Service` in sequence.

## The Naive Failure: Cascading Timeouts and Tight Coupling

This synchronous orchestration creates three major failure modes:
- **Tight Coupling:** The `Order Service` must know the API contract, location, and existence of every downstream service.
- **Cascading Failures:** If the `Email Service` is slow or down, the `Order Service` threads block, eventually causing the order creation itself to timeout and fail, even though sending an email is not critical to accepting the order.
- **Rigid Extension:** Adding a new feature (e.g., Fraud Detection) requires modifying and redeploying the `Order Service` to make a new API call.

## The Event Path: Reactive Choreography

**The EDA Solution:** Instead of direct commands, the `Order Service` durably records its own state, safely defers event publication (e.g., using a transactional outbox), and immediately returns success to the user. Downstream consumers subscribe to the broker and react independently.

1. **Event Producer:** Creates and publishes the event (e.g., `Order Service` publishing `OrderCreated`). The producer doesn't know the consumers.
2. **Event Router / Broker:** Stores, routes, and distributes events (e.g., Kafka, RabbitMQ, Pulsar).
3. **Event Consumer:** Subscribes to events and reacts (e.g., `Email Service` consuming `OrderCreated` to send an email). Consumers don't know the producer.

### A Real Example: Video Upload
When a user uploads a video, a `VideoUploaded` event is emitted. Consumers react independently:
- **Thumbnail Service** $\rightarrow$ Generates thumbnail.
- **Transcoding Service** $\rightarrow$ Generates 480p, 720p, 1080p.
- **Notification Service** $\rightarrow$ Notifies subscribers.
- **Analytics Service** $\rightarrow$ Tracks upload.

One event triggers many independent actions without waiting for a chain of REST calls, avoiding timeouts and cascading failures. You might need 100 pods for Analytics but only 5 for Email—EDA allows them to scale independently.

## Deep Dive: Delivery, Ordering, and Idempotency

### Delivery Guarantees
- **Problem:** Events must reliably reach consumers despite network partitions or consumer crashes.
- **Naive Failure:** Using in-memory pub/sub where unacknowledged messages are lost if a consumer restarts.
- **Mechanism:** Persistent event brokers durably retain events independently of consumers (e.g., Kafka retains by time or size). Consumers commit offsets to track their progress. Message queues (like RabbitMQ) often remove messages upon explicit consumer acknowledgment.
- **Trade-off:** Retrying failed processing blocks the partition or requires complex Dead Letter Queue (DLQ) management.
- **Recovery:** Consumers implement retry loops with exponential backoff and route unrecoverable events to a DLQ for manual intervention.

### Event Ordering
- **Problem:** Events might arrive out of order (e.g., `PaymentCompleted` arriving before `PaymentStarted`).
- **Naive Failure:** Processing a profile update for a user that doesn't exist yet, resulting in a state failure.
- **Mechanism:** Message brokers guarantee ordering within a specific partition (e.g., routing all events for `UserId=123` to the same partition using a partition key). Alternatively, include a monotonically increasing `sequenceNumber` or `version` in the event payload.
- **Trade-off:** Strict ordering limits concurrent processing per partition.
- **Recovery:** If an out-of-order event is detected via a missing sequence number, the consumer can reject it (sending it to a retry queue) or buffer it until the missing prior events arrive.

### Idempotency
- **Problem:** Network retries mean consumers may receive the exact same event twice (at-least-once delivery).
- **Naive Failure:** Processing the same `OrderCreated` event twice and charging the customer's credit card twice.
- **Mechanism:** Consumers MUST be idempotent. Before processing, the consumer checks if the event's unique `eventId` has already been processed by querying a deduplication table or verifying the target entity's state.
- **Trade-off:** Deduplication checks add database latency to every message processed.
- **Recovery:** For internal state, use local atomic database transactions (e.g., inserting into `processed_events` and `orders` tables together). For external effects like payment charges, supply a stable idempotency key to the external service's API.

## Failure and Recovery: Operational Pitfalls

- **Debugging Complexity:** Tracing a flow like `A -> Event1 -> B -> Event2 -> C` is significantly harder than reading synchronous code. Using a distributed tracing system to pass a correlation ID through all events is highly recommended at scale.
- **Eventual Consistency:** `OrderCreated` does not guarantee the inventory has been updated *yet*. The system is only eventually consistent. UIs must be designed to handle pending states.
- **Schema Evolution:** Changing an event schema (e.g., adding a field) can break older consumers. Schema registries and explicit versioning help enforce backward compatibility and prevent widespread parsing failures.

## The Interview Answer: Terminology Traps

Interviewers often test if you understand the boundaries between related concepts:

- **Commands vs Events:**
  - **Commands:** Mean "Do something" (e.g., `CreateOrder`). Targeted at a specific service.
  - **Events:** Mean "Something already happened in the past" (e.g., `OrderCreated`). Broadcast to whoever cares.
- **Message Queue vs Pub/Sub vs EDA:**
  - **Message Queue:** Focuses on work distribution. Typically one producer $\rightarrow$ one consumer.
  - **Pub/Sub:** A messaging pattern. One producer $\rightarrow$ many consumers. Answers "How do services communicate?"
  - **EDA:** An architectural style. Answers "How should the entire system be designed?" EDA uses Pub/Sub as a building block.

### Mental Model
- **Message Queue:** One producer $\rightarrow$ One consumer.
- **Publish Subscribe:** One producer $\rightarrow$ Many consumers.
- **Event Driven Architecture:** The entire system is designed around events. Service A emits events, Service B and C react, Service D emits new events. Events become the backbone of the system.

## Deferred Scope
- **Transactional Outbox:** How a producer safely saves to its database and publishes an event atomically.
- **Saga Pattern:** How multiple services complete one business transaction safely on top of EDA.

## Quick recall

**Q: What defines an event in EDA?**
A: A notification that something has already happened in the past (e.g., `OrderCreated`).

**Q: How does EDA reduce tight coupling?**
A: Producers don't know who consumes their events, and consumers don't know who produced them.

**Q: What are the three main components of EDA?**
A: Event Producer, Event Router/Broker, and Event Consumer.

**Q: How do consumers handle duplicate events?**
A: By implementing idempotency, typically checking a unique `eventId` against a deduplication table before processing.

**Q: What is the primary difference between a Message Queue and EDA?**
A: Message Queues typically route one message to one consumer; EDA often distributes an event to multiple independent consumers via Pub/Sub.

**Q: What are the biggest operational challenges in EDA?**
A: Debugging distributed workflows, managing eventual consistency, ensuring consumer idempotency, and handling schema evolution.
