---
order: 20
---

# Message Brokers & Event Streaming

A message broker is middleware between producers and consumers that **stores, routes, and delivers** messages asynchronously. The senior-interview payload is *why* you'd put a broker between two services, and the broker-vs-event-streaming distinction.

## The problem brokers solve

Direct synchronous call `Order → HTTP → Payment` couples the two: if Payment is down the Order fails; if Payment is slow the Order waits. Insert a broker:

```text
Order Service → Broker → Payment Service
```

Now Order saves, publishes "process payment", and returns immediately. Payment consumes later — even if it was down for 10 minutes. Communication becomes **asynchronous** and **decoupled**.

## What you get

- **Decoupling** — add an Analytics consumer later without touching Order Service.
- **Reliability** — the broker persists messages; if a consumer crashes, messages remain.
- **Scalability** — the broker fans work across many workers (`Queue → W1, W2, W3`).
- **Producers don't need to know if consumers are online.**

A broker also does **routing** (one event → Payment + Inventory + Analytics), **retries** of failed consumers, and sometimes **transformation / protocol translation** (HTTP→AMQP, JSON→XML).

## Broker vs queue

A *queue* is one pattern (Producer → Queue → Consumer). A *broker* is the surrounding system that offers **many** patterns: queues, topics, routing, retries, persistence. So "broker" ⊃ "queue".

**Two core messaging models:**
- **Point-to-point** — one message → exactly one consumer (e.g. "send email"). Backed by a queue.
- **Publish-subscribe** — one message → many consumers (e.g. "OrderCreated" → Inventory + Analytics + Notification). Backed by a topic.

## Message broker vs event streaming

The key conceptual split:

| Traditional broker (RabbitMQ, ActiveMQ) | Event streaming (Kafka, Pulsar) |
|------------------------------------------|----------------------------------|
| Mental model: **post office** — deliver then discard | Mental model: **CCTV recording** — append and keep |
| Message = *work to be done* | Message = *immutable event in a log* |
| Consumer reads → ACK → **message deleted** | Consumer reads → **message stays**; consumers track offsets |
| Optimized for reliable delivery, routing, retries, DLQ | Optimized for sequential-disk throughput, partitioning, retention, **replay** |

**The replay difference is the headline.** Kafka is a **distributed commit log**: append, store, replay. Messages persist after consumption, and each consumer tracks its own offset:

```text
Consumer A → offset 1000
Consumer B → offset 750     (each reads independently; messages remain)
```

So if you've processed 100M events through Kafka → Analytics and *later* want a Fraud-Detection service, you read from offset 0 and reprocess history. With RabbitMQ those events are already gone.

**Kafka can also behave like a plain broker:** `Order → Kafka topic → Payment` where Payment consumes once. So Kafka serves both queue-style and stream-style use cases; RabbitMQ is queue/routing-focused.

**Where each fits:**
- **RabbitMQ** — user signup → email queue → email worker; message removed after processing.
- **Kafka** — ride lifecycle events (requested/assigned/started/completed/paid) consumed by billing, analytics, ML, fraud, notifications, monitoring; historical retention is valuable.

## ESB — why it died

The old Enterprise Service Bus put routing + transformation + business logic + validation *in the bus* (`Service → ESB → Service`). Everything came to depend on it → bottleneck, hard to scale, hard to maintain. Modern systems push logic into the **services** and keep the broker dumb (`Microservices → Message Broker`), each service owning its own logic.

## Quick recall

**Q. One-line definition of a message broker?**
A. Middleware between producers and consumers that stores, routes, and delivers messages asynchronously — decoupling sender from receiver.

**Q. Broker vs queue?**
A. A queue is one pattern (producer→queue→consumer); a broker is the system providing many patterns (queues, topics, routing, retries, persistence).

**Q. Traditional broker vs event streaming?**
A. Broker (RabbitMQ) = deliver-and-discard work items. Streaming (Kafka) = immutable append-only log you can replay; consumers track offsets and messages persist.

**Q. Why does replay matter?**
A. New consumers can reprocess all historical events (e.g. add fraud detection after the fact) — impossible once a traditional broker has deleted delivered messages.

**Q. Why did the ESB model fall out of favor?**
A. Centralizing routing/transformation/logic in the bus made it a bottleneck and single point of contention; modern systems keep logic in services and the broker simple.
