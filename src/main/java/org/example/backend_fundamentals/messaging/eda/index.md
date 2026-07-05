---
order: 10
---

# Event-Driven Architecture (EDA)

Event-Driven Architecture (EDA) is an architectural style built around the production, detection, and consumption of events. 

## The Core Problem It Solves

In traditional REST architectures (e.g., an `Order Service` calling `Inventory`, `Payment`, `Email`, and `Analytics` services directly), systems suffer from:
- Tight coupling (the `Order Service` knows everyone).
- Adding new consumers requires modifying the producer's code.
- Slow requests and cascading timeouts.

**The EDA Solution:** Instead of direct calls, the `Order Service` publishes an `OrderCreated` event and forgets about it. Consumers react independently. The producer doesn't know the consumers, and consumers don't know the producer.

## Commands vs Events

This is a common interview trap:
- **Commands:** Mean "Do something" (e.g., `CreateOrder`, `ProcessPayment`).
- **Events:** Mean "Something already happened in the past" (e.g., `OrderCreated`, `PaymentProcessed`).

## Components of EDA

1. **Event Producer:** Creates and publishes the event (e.g., `Order Service` publishing `OrderCreated`).
2. **Event Router / Broker:** Stores, routes, and distributes events (e.g., Kafka, RabbitMQ, Pulsar).
3. **Event Consumer:** Subscribes to events and reacts (e.g., `Email Service` consuming `OrderCreated` to send an email).

## Real Example: Video Upload

When a user uploads a video, a `VideoUploaded` event is emitted.
Consumers react independently:
- **Thumbnail Service** $\rightarrow$ Generates thumbnail.
- **Transcoding Service** $\rightarrow$ Generates 480p, 720p, 1080p.
- **Notification Service** $\rightarrow$ Notifies subscribers.
- **Analytics Service** $\rightarrow$ Tracks upload.

One event triggers many independent actions. This is classic EDA. Everything happens asynchronously instead of waiting for a chain of REST calls, avoiding timeouts and cascading failures.

## Advantages

- **Loose Coupling:** The producer doesn't know the consumers, and consumers don't know the producer.
- **Easy Feature Addition:** To add a new feature (e.g., Fraud Detection), simply create a new consumer that subscribes to the event. Zero changes to existing code.
- **Better Scalability:** Consumers scale independently. You might need 100 pods for Analytics but only 5 for Email.

## Comparisons: EDA vs Pub/Sub vs Message Queues

- **Message Queue:** Focuses on work distribution. Typically one producer $\rightarrow$ one consumer.
- **Pub/Sub:** A messaging pattern. One producer $\rightarrow$ many consumers. Answers "How do services communicate?"
- **EDA:** An architectural style. Answers "How should the entire system be designed?" EDA uses Pub/Sub as a building block.
- **Saga:** A distributed transaction pattern. Answers "How do multiple services complete one business transaction safely?" EDA often uses Pub/Sub, and Saga is often implemented on top of EDA.

### Mental Model

- **Message Queue:** One producer $\rightarrow$ One consumer.
- **Publish Subscribe:** One producer $\rightarrow$ Many consumers.
- **Event Driven Architecture:** The entire system is designed around events. Service A emits events, Service B and C react, Service D emits new events. Events become the backbone of the system.

## Gotchas / Pitfalls / Trick questions

- **Debugging:** Tracing a flow like `A -> Event1 -> B -> Event2 -> C` is significantly harder than tracing synchronous calls.
- **Eventual Consistency:** `OrderCreated` does not guarantee the inventory has been updated yet; updates happen eventually.
- **Duplicate Events:** Consumers may receive the same event twice. Consumers MUST be idempotent.
- **Event Ordering:** Events might arrive out of order (e.g., `PaymentCompleted` arriving before `PaymentStarted`).
- **Schema Evolution:** Changing an event schema (e.g., adding a field) can break older consumers. Strict versioning is required.

## Quick recall

**Q: What defines an event in EDA?**
A: A notification that something has already happened in the past (e.g., `OrderCreated`).

**Q: How does EDA reduce tight coupling?**
A: Producers don't know who consumes their events, and consumers don't know who produced them.

**Q: What are the three main components of EDA?**
A: Event Producer, Event Router/Broker, and Event Consumer.

**Q: What is the primary difference between a Message Queue and EDA?**
A: Message Queues typically route one message to one consumer; EDA often distributes an event to multiple independent consumers via Pub/Sub.

**Q: What are the biggest operational challenges in EDA?**
A: Debugging distributed workflows, managing eventual consistency, ensuring consumer idempotency, and handling schema evolution.


<ExerciseNav />
