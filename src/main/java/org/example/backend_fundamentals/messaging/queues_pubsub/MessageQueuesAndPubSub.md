# Message Queues & Publish-Subscribe

The two core messaging shapes. A **queue** distributes *work* to one consumer; **pub/sub** distributes *events* to many. Everything below — ACKs, delivery semantics, DLQ, backpressure, fan-out — hangs off that distinction.

## Why queues exist

Video upload done synchronously makes the user wait 30–60s for thumbnail + transcode + notify + metadata. With a queue:

```text
User → Upload API → Queue   ("upload accepted" returned immediately)
                      ↓
                   Workers process later
```

A queue is **temporary storage for work**: send email, generate invoice, process payment, resize image. Consumer loop: take job → process → **ACK** → delete → repeat.

**Competing consumers:** attaching several consumers to one queue doesn't duplicate work — each message still goes to exactly *one* consumer; the consumers *compete* for messages, which is how a queue load-balances work across workers. (Contrast pub/sub, where every subscriber gets a copy.)

## ACK and delivery semantics

The ACK is what makes delivery reliable: worker processes, then ACKs, then the queue deletes. No ACK (worker crashed) → the queue **retries**. The order of process/ACK/delete defines the guarantee:

- **At-most-once** — receive → delete → process. A crash loses the message. Delivered 0 or 1 times. (Fire-and-forget.)
- **At-least-once** — receive → process → ACK → delete. A missing ACK triggers retry. Delivered 1+ times (duplicates possible). **The common default.**
- **Exactly-once** — **misconception:** "duplicates never happen." **Correction:** true exactly-once is hard and expensive; in practice you get it via **at-least-once + idempotency + deduplication** (e.g. dedup on payment id 123 → ignore the duplicate). It *appears* exactly-once because duplicates are made harmless.

## FIFO and ordering

A single worker drains a queue in order (1,2,3,4,5). **Multiple workers break ordering** — they may finish 2,1,3,5,4. FIFO queues preserve order but are slower and more expensive (they limit parallelism). Only pay for FIFO when ordering genuinely matters (e.g. per-account event streams).

## Dead-letter queue (DLQ) and poison pills

A message that keeps failing (invalid card, corrupt data, unknown customer) shouldn't retry forever and block the queue. After N failed attempts it's moved to a **DLQ** for inspection:

```text
Main Queue → fail N times → DLQ → alert / human investigation
```

**Poison pill** has two meanings: (1) a malformed message that always fails (the DLQ case), or (2) a sentinel `STOP` message that tells a consumer to shut down gracefully.

## Push vs pull, and long polling

- **Pull** — consumer repeatedly asks "any message?" (wasteful polling).
- **Push** — broker pushes immediately when a message arrives.
- **Long polling** — consumer asks, broker *holds the request open* and returns the instant a message arrives (or on timeout). Best of both; **SQS** uses this.

## Backpressure

If producers outrun consumers (10k/s in, 1k/s out → +9k/s backlog), the queue grows until memory/disk pressure and latency blow up. **Backpressure** slows producers down — via `HTTP 429`/`503`, rate limiting, or bounded buffers. Restaurant analogy: a kitchen doing 100 meals/hr can't accept 500 orders/hr forever — at some point it stops taking orders.

## Task queue vs message queue

- **Task queue** — work-oriented ("generate PDF", "resize image").
- **Message queue** — communication/event-oriented ("OrderCreated", "UserRegistered").

Often used interchangeably; the mechanics are the same.

## Publish-subscribe

Pub/sub flips the cardinality: **one producer → many consumers**. A publisher writes to a **topic**; every subscriber gets its own copy (**fan-out**):

```text
Order Service → Topic "OrderCreated" → {Inventory, Analytics, Notification, Fraud}
```

Contrast: a *queue* hands each job to exactly one consumer; a *topic* delivers a copy to every subscriber.

**Why it's powerful — extensibility.** Without pub/sub, Order Service must call Payment, Inventory, Analytics, Notification directly and depends on all of them. With pub/sub it publishes one event; new consumers (Recommendation Engine, Fraud) just **subscribe** — Order Service never changes.

**Event vs task** (the key mental model):
- Queue message = a **command**: "do this."
- Pub/sub message = an **event**: "this happened" (a fact).

This events-vs-commands distinction is the foundation of event-driven architecture.

**Other pub/sub properties:**
- **Eliminates polling** — subscribers are pushed events instead of asking "any updates?" repeatedly.
- **Filtering** — subscribers select event types they care about (Analytics wants OrderCreated/OrderShipped; Notifications want OrderCreated/OrderReturned); the broker filters. (This is the SNS message-filtering model.)
- **Durability** — a durable topic stores events for a down subscriber and redelivers when it recovers; without durability, missed events are lost. Durable delivery is typically **at-least-once**, so subscribers must be idempotent.
- **Independent scaling** — if Analytics is overloaded, scale it ×20 without touching Order Service or other subscribers (publishers/subscribers are decoupled).
- **Multiple delivery protocols** — one topic can deliver to heterogeneous endpoints: a **queue**, an **HTTP endpoint**, a **Lambda/serverless function**, etc. The publisher doesn't know the destinations; the topic handles routing.

Managed/embedded implementations: **Amazon SNS**, **Google Pub/Sub**, **Kafka** (consumer groups), **Redis Pub/Sub** (fire-and-forget, no durability).

## Queue + pub/sub together

The common production pattern: pub/sub for distribution, queues for processing.

```text
Order Service → Topic → {Payment Queue, Inventory Queue, Analytics Queue} → workers
```

The topic fans the event out; each subscriber's queue handles retries, DLQ, and worker scaling independently.

**Kafka does both at once:** a topic with multiple **consumer groups** gives pub/sub (each group receives all events); within a group, multiple workers share the partitions, giving queue-style work distribution.

## The deciding intuition

The cleanest way to choose between them:

- **Queue → "who will do this work?"** (one worker should handle it.)
- **Pub/sub → "who wants to know this happened?"** (many systems may react.)

## Quick recall

**Q. Queue vs pub/sub in one line?**
A. Queue = one producer → one consumer (work distribution, a command "do this", competing consumers). Pub/sub = one producer → many consumers (event distribution, a fact "this happened", fan-out).

**Q. What's the deciding question for each?**
A. Queue: "who will do this work?" Pub/sub: "who wants to know this happened?"

**Q. What does "competing consumers" mean?**
A. Multiple consumers on one queue still get one message each — they compete, which load-balances work; it does not duplicate the message (that's pub/sub).

**Q. At-most-once vs at-least-once vs exactly-once?**
A. At-most-once: delete-then-process, may lose. At-least-once: process-ACK-delete, may duplicate (the default). Exactly-once: usually at-least-once + idempotency/dedup so duplicates are harmless.

**Q. What is a DLQ for, and what's a poison pill?**
A. DLQ holds messages that failed N retries so they don't block the queue. Poison pill = a perpetually-failing (malformed) message, or a sentinel STOP message.

**Q. What is backpressure and how is it applied?**
A. Slowing producers when consumers can't keep up (queue backlog grows) — via HTTP 429/503, rate limiting, or bounded buffers.

**Q. Why is pub/sub more extensible than direct calls?**
A. New consumers just subscribe to the topic; the producer publishes one event and never changes when reactors are added.

**Q. How does Kafka give both queue and pub/sub behavior?**
A. Multiple consumer groups on a topic = pub/sub (each group sees all events); multiple workers within a group share partitions = queue-style work distribution.
