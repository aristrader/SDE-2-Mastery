---
order: 60
---

# System Design Interview Framework

## How it works

A system design interview is not a trivia round. The interviewer is watching how you handle an ambiguous problem with another engineer in the room.

Strong signal:

- You ask clarifying questions.
- You state assumptions.
- You start broad, then deepen.
- You explain tradeoffs.
- You adapt when the interviewer redirects.
- You leave time for bottlenecks, failures, and operations.

Weak signal:

- You jump into a memorized architecture.
- You over-engineer before understanding scale.
- You go deep on one component too early.
- You silently think for long stretches.
- You defend the first design as if it is perfect.

## Delivery flow for a 45-minute round

![System design interview flow](./assets/hld-interview-flow.svg)

| Phase | Time | Output |
| --- | --- | --- |
| Requirements | 5 min | 3-5 functional and 3-5 quantified non-functional requirements |
| Core entities and interface | 5 min | Key nouns, essential API/events, and an optional data flow |
| High-level design | 10-15 min | Main components plus one concrete read/write flow |
| Critical deep dives | 10-15 min | Bottleneck, tradeoff, failure behavior, and scale path |
| Wrap-up | 3-5 min | Risks, observability, and next design step |

Follow the interviewer if they redirect, but keep returning to the next unfinished phase. A complete simple system scores better than an unfinished ambitious one.

### 1. Clarify requirements

Spend the first few minutes narrowing the problem:

- Functional: What are the three most important user actions? What is explicitly out of scope?
- Non-functional: What scale, read/write shape, latency, durability, consistency, availability, security, or compliance requirement changes the design?
- Constraints: Single region or global? Existing managed services? Third-party dependencies? Burst traffic?

Write down only the requirements that will drive a decision. "Low latency" is vague; "feed p99 below 200 ms" is useful. Do not invent a large feature list.

Example for news feed:

```text
Candidate: Is feed chronological or ranked?
Interviewer: Chronological is fine.

Candidate: Text only or media too?
Interviewer: Images and videos.

Candidate: Scale?
Interviewer: 10M DAU, max 5000 friends per user.
```

Those answers decide whether you talk about media storage, CDN, fanout, ranking, and celebrity users.

### 2. Name core entities and the interface

Before drawing infrastructure, name the core resources and actors. For a feed: `User`, `Post`, `Follow`, and `Timeline`. This gives the API and data model stable language without prematurely writing every table column.

For a narrow service, sketch the few API operations or events now. For a broad product, keep the contract light until the main flow is clear.

```text
Broad product: flows and components first
Narrow service: API + core data model early
```

Use REST by default for an external CRUD-style interface; introduce gRPC/RPC or a real-time channel only when it solves a stated need. Derive the actor from authentication rather than trusting a user ID supplied in the request body.

If the system is a pipeline, write a short data flow before the diagram, for example: `upload -> validate -> persist -> enqueue processing -> notify`. Skip this for simple request/response systems.

### 3. Propose high-level design

Draw the first usable architecture:

```text
Clients
  ↓
API / Gateway
  ↓
Services
  ↓
Database / Cache / Queue / Object Storage
```

Then ask for buy-in:

```text
"At this level, does this match the scope you want me to solve?"
```

This is important because the interviewer may want you to focus on one branch: storage, caching, consistency, feed generation, rate limiting, failure handling, or capacity.

Use concrete flows, not just boxes. For a feed system:

- Publishing flow: user writes post → store post → fan out or enqueue feed update.
- Retrieval flow: user opens feed → read timeline/cache → hydrate post/user/media details.

Start with the smallest architecture that serves the functional requirements. Walk through one important request end-to-end and narrate which state changes at each step. Add cache, queue, replication, sharding, or CDN only when a non-functional requirement creates a reason.

At this point, ask for buy-in: "This covers the main flows. Would you like me to deepen the write path, read path, or storage strategy?"

### 4. Deep dive on the critical parts

Pick the components that carry the design risk. Senior interviews usually care about bottlenecks and tradeoffs, not every table column.

Good deep dives:

- URL shortener: ID generation, collision handling, read-heavy cache, DB sharding.
- Chat: connection management, online/offline state, message delivery, ordering.
- News feed: fanout-on-write vs fanout-on-read, celebrity users, cache strategy.
- Rate limiter: algorithm choice, Redis atomicity, distributed counters, 429 behavior.
- KYC platform: vendor orchestration, idempotency, state machine, retries, data residency.

Bad deep dives:

- Spending 15 minutes on exact CSS/media rendering.
- Designing a perfect ranking algorithm when the interview is about scalable feed delivery.
- Writing every API field before agreeing on the architecture.

Use estimates when the result changes a choice: cache size, partition count, queue throughput, object-storage cost, or whether one node is enough. Do not calculate numbers merely to prove that traffic is large.

### 5. Wrap up

End by showing critical thinking:

- Recap the design in 30-60 seconds.
- Name bottlenecks.
- Name failure modes.
- Mention monitoring and rollout.
- Explain the next scale step.
- Mention what you would refine with more time.

Good closing:

```text
"The current design supports the required scale by caching reads, using a queue for async fanout, and storing media in object storage behind CDN. The risks are hot users, queue lag, and cache invalidation. I would monitor feed publish latency, queue depth, cache hit rate, DB QPS, and p99 feed read latency."
```

## Interview checklist

- Requirements: core actions, exclusions, scale, latency, and consistency/durability.
- Contract: entities, APIs/events, and a data flow only when it clarifies the design.
- Architecture: one complete flow through the main boxes and relevant stored state.
- Depth: the highest-risk bottleneck, a rejected alternative, and failure behavior.
- Close: metrics, alerts, rollout, and what breaks at the next order of magnitude.

## Gotchas / Trick questions

1. **"There is one correct design."** No. There are designs that fit or do not fit stated requirements.
2. **"More components means senior design."** No. Senior design is choosing the smallest architecture that meets scale, reliability, and operational needs.
3. **"I should answer immediately to show confidence."** No. Clarifying questions show judgment.
4. **"API schema always comes first."** Not for broad HLD. Start with flows and components; add API/schema only if the problem needs it.
5. **"Once I draw the design, I am done."** No. The wrap-up is where you show bottleneck awareness and production thinking.

## Quick recall

**Q. First step in an HLD interview?**  
A. Clarify scope, features, scale, and assumptions before proposing architecture.

**Q. What is the high-level design phase for?**  
A. Get interviewer buy-in on the main boxes and flows before deep-diving.

**Q. How do you choose what to deep dive?**  
A. Pick the highest-risk or most scale-sensitive parts of the design.

**Q. What should the wrap-up include?**  
A. Recap, bottlenecks, failure modes, monitoring, rollout, and next scale step.

**Q. Biggest red flag?**  
A. Jumping to a memorized solution without understanding requirements.

**Q. Senior signal?**  
A. Tradeoff reasoning: why this component now, what it costs, and what breaks next.
