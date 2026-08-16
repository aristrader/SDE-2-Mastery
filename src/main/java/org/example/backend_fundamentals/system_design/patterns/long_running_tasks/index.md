---
order: 40
---

# Long-Running Tasks

Use this pattern when a request cannot produce a final answer within the user-facing latency budget: video transcoding, report generation, large imports, email campaigns, or bulk reprocessing. Do not queue work that completes quickly only to make the diagram look more scalable.

## Core flow

```text
client -> API validates request -> persist Job(PENDING) + outbox -> durable queue
                                                             -> worker -> Job(SUCCEEDED | FAILED)
client <- 202 Accepted + jobId                         client polls, receives webhook, or gets push update
```

Persist the job state before publishing work, usually with a transactional outbox. Return `202 Accepted` and a job ID only after the request is durable. Workers can scale independently from request-serving instances.

## Required decisions

| Concern | Interview-ready answer |
|---|---|
| Duplicate submission or redelivery | Idempotency key and a worker that makes the effect replay-safe |
| Temporary dependency failure | Bounded retry with exponential backoff and jitter |
| Poison job | Retry limit, dead-letter queue, alert, and a repair/replay path |
| Queue backlog | Monitor age/lag, autoscale workers, enforce a deadline, and shed/defer lower-priority work |
| User visibility | Status endpoint first; webhook or push only when justified |
| Long job crash | Resume from durable checkpoints or restart idempotently |

Kafka is useful when replay and ordered event streams matter. A task queue such as SQS is often simpler when the problem is independent jobs with retry and dead-letter handling. The interview decision is based on access and recovery needs, not vendor names.

## When not to use it

Keep short, correctness-sensitive actions synchronous when the user needs the final result immediately. A queue adds delay, duplicate delivery, observability work, and a second state machine.

## Quick recall

**Q. Why is a queue not enough by itself?**
A. You still need durable job status, idempotent workers, retry limits, a deadline, and a user-visible outcome.

**Q. What does `202 Accepted` mean?**
A. The system accepted a durable request for later processing; it does not mean the work succeeded.
