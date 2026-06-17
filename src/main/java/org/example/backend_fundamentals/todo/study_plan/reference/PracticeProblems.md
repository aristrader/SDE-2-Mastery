# Practice Problems & Interview Questions

Curated practice bank for the 12-week Sprint and standing rehearsal throughout. Problems are *intentionally not answered here* — that's your practice work. Pick a problem, set a timer, build/explain solo, then verify against the listed patterns/concepts.

## How to use this bank

- **Pick problems matching your current weak Part.** If Concurrency is weak, do LRU/LFU + BookMyShow.
- **Time-box ruthlessly.** Machine-coding rounds in real interviews are 45–90 min. Use the listed time.
- **Verbalize, don't read.** Explain the design out loud while building it. That's the interview signal.
- **Repeat the hard ones.** Hard problems should be done 2–3 times until fluent.

---

## Section 1: Machine Coding / LLD problems (25)

Bread and butter for SDE2 → senior interviews. Most companies do one in the loop. Each row gives the problem, difficulty, the GoF + supporting patterns it teaches, and a realistic time-box.

| # | Problem | Difficulty | Patterns it teaches | Time-box |
| --- | --- | --- | --- | --- |
| 1 | **Parking Lot** | Med | Strategy (pricing), Factory (vehicle), Singleton (registry) | 60–90 min |
| 2 | **Splitwise** | Med | Strategy (split algos), Observer (notify), Graph (settlement) | 60–90 min |
| 3 | **Snake & Ladder** | Easy | State, Factory, Random/Strategy for dice | 45–60 min |
| 4 | **Chess** | Hard | State, Strategy (piece moves), Command (undo), Memento | 90–120 min |
| 5 | **Tic-Tac-Toe** | Easy | State, Strategy (win-check) | 30–45 min |
| 6 | **Elevator System** | Med | State, Strategy (scheduling), Observer (button presses), Singleton | 60–90 min |
| 7 | **LRU Cache** | Med | Data structure: HashMap + Doubly Linked List | 45–60 min |
| 8 | **LFU Cache** | Hard | HashMap-of-HashMaps + DLL per frequency | 60–90 min |
| 9 | **Rate Limiter (Token Bucket / Sliding Window)** | Med | Strategy (algo choice), Singleton | 45–60 min |
| 10 | **Logger with log levels** | Easy | Chain of Responsibility, Singleton | 45 min |
| 11 | **In-memory KV store with TTL** | Med | Background eviction, expiration heap | 60–90 min |
| 12 | **ATM** | Med | State, Strategy (withdrawal algo), Chain of Responsibility | 60–90 min |
| 13 | **Vending Machine** | Easy | State, Strategy | 45–60 min |
| 14 | **Library Management** | Med | Entity modeling, search, member ops | 60–90 min |
| 15 | **BookMyShow / Movie Ticket Booking** | Hard | Singleton, concurrency (seat lock), optimistic locking, State | 90–120 min |
| 16 | **Notification Dispatch (multi-channel)** | Med | Strategy (per channel), Observer, Chain of Responsibility | 60–90 min |
| 17 | **Job Scheduler / Cron** | Med | Strategy (schedule type), Priority Queue, Observer | 60–90 min |
| 18 | **Food Ordering (Swiggy/Zomato core)** | Hard | State, Strategy, Observer, Factory | 90–120 min |
| 19 | **Ride-sharing core (Uber/Ola)** | Hard | State, Strategy (matching), Observer, geo-hashing | 90–120 min |
| 20 | **URL Shortener (LLD scope)** | Easy | Encoding strategy, in-memory map | 45–60 min |
| 21 | **Bowling Game scorer** | Easy | State, rules engine | 45 min |
| 22 | **Connection pool implementation** | Med | Object Pool, Singleton | 45–60 min |
| 23 | **In-memory Pub-Sub system** | Med | Observer, Strategy (delivery semantics) | 60–90 min |
| 24 | **KYC verification state machine** 🔐 | Med | State, Strategy, Chain of Responsibility — *directly your domain* | 60–90 min |
| 25 | **Document upload pipeline with retry + idempotency** 🔐 | Med | Idempotency Key, Retry pattern, Strategy | 60 min |

🔐 = directly relevant to your KYC platform — high leverage for promotion narratives.

**Recommended Sprint plan for LLD practice:**

- **Saturday Week 4 onwards:** 1 LLD problem per Saturday (Easy → Medium ramp).
- **From Saturday Week 6:** alternate weekends with HLD (LLD one Saturday, HLD the next).
- **Week 12 (mock-loop week):** 1 LLD problem back-to-back with 1 HLD + 1 behavioral, simulating an interview loop.

**Coverage goal:** ≥4 LLD problems walked end-to-end by Week 12. Don't try to do all 25 — diminishing returns after the first handful once you've internalized the patterns.

---

## Section 2: HLD / System Design problems (20)

Heaviest interview block at senior+. Each row gives the problem, difficulty, and the key concepts you must surface in the design discussion.

| # | Problem | Difficulty | Key concepts to surface | Time-box |
| --- | --- | --- | --- | --- |
| 1 | **URL shortener (full scale)** | Easy | Hashing, DB sharding, caching, read-heavy ratio | 30–45 min |
| 2 | **Pastebin** | Easy | Object storage, expiration, access control | 30–45 min |
| 3 | **Twitter / News feed** | Hard | Fan-out-on-write vs read, timeline generation, ranking, celebrity problem | 60 min |
| 4 | **Instagram (image-heavy feed)** | Hard | CDN, object storage, image variants, feed generation | 60 min |
| 5 | **WhatsApp / Chat** | Hard | Long-poll/WebSockets, message queue, presence, e2e crypto basics | 60 min |
| 6 | **YouTube / Video streaming** | Hard | CDN, transcoding pipeline, adaptive bitrate, recommendations | 60 min |
| 7 | **Uber / Ride-sharing** | Hard | Geo (H3/S2), matching, dispatch, surge pricing | 60–90 min |
| 8 | **Dropbox / File sync** | Hard | Chunking, dedup, sync protocol, conflict resolution | 60 min |
| 9 | **Google Drive / Collaborative editing** | Hard | OT / CRDT, real-time sync, presence | 60 min |
| 10 | **Notification service** | Med | Fan-out, retries, channels, idempotency, ordering | 45–60 min |
| 11 | **Distributed cache (Redis-like)** | Hard | Consistent hashing, replication, eviction, persistence | 60 min |
| 12 | **Distributed message queue (Kafka-like)** | Hard | Partitioning, replication, offsets, exactly-once semantics | 60–90 min |
| 13 | **Rate limiter (distributed)** | Med | Algorithms, Redis-backed counters, sliding windows | 45–60 min |
| 14 | **Web crawler** | Med | URL frontier, dedup, politeness, distributed coordination | 45–60 min |
| 15 | **Search autocomplete / Typeahead** | Med | Trie, ranking, latency targets, hot-prefix caching | 45–60 min |
| 16 | **KYC / identity verification platform** 🔐 | Hard | Vendor orchestration, state machine, multi-tenant, region pinning, webhooks — *directly your domain* | 60–90 min |
| 17 | **Payment system** | Hard | Idempotency, ledger (double-entry), saga, reconciliation | 60–90 min |
| 18 | **Ad-click counter at scale** | Med | Counters, Lambda arch (or pure stream), dedup, fraud signals | 45–60 min |
| 19 | **Distributed locks** | Med | Redis Redlock, ZooKeeper, fencing tokens, failure modes | 45 min |
| 20 | **Logging / Analytics system (ELK-style)** | Med | Ingest pipeline, indexing, querying, retention tiers | 45–60 min |

🔐 = directly your domain — practice this one *especially well*. It's your portfolio piece.

**Recommended Sprint plan for HLD practice:**

- **Saturday Week 6 onwards:** 1 HLD problem per Saturday, alternating with LLD weekends. Rotate focus (1 storage-heavy, 1 compute-heavy, 1 KYC-domain, repeat).
- **Week 12 (mock-loop week):** 1 HLD problem back-to-back with 1 LLD + 1 behavioral.
- **Always finish #16 (KYC platform)** before any interview where senior is on the line — do it **twice** during the Sprint.

**Coverage goal:** ≥4 HLD problems walked end-to-end by Week 12 + your KYC platform polished (done twice).

---

## Section 3: Design pattern interview questions (20)

Asked in both coding rounds and design discussions. Each question lists what the interviewer is actually testing — answer the *underlying concept*, not the surface question.

| # | Question | What's actually being tested |
| --- | --- | --- |
| 1 | Walk through the 5 ways to implement Singleton in Java, and why Bill Pugh is preferred. | Class loading, JMM (happens-before), DCL pitfalls, thread safety |
| 2 | When would you use a Builder over a constructor with many optional params? | Immutability, readability, validation, telescoping-constructor anti-pattern |
| 3 | Hand-written Builder vs Lombok `@Builder` — what's lost with the annotation? | Required-field enforcement, validation in `build()`, custom logic |
| 4 | Factory Method vs Abstract Factory — concrete difference? | Product hierarchy depth, when one factory becomes many |
| 5 | Strategy vs State pattern — sounds similar; how do you tell them apart? | Behavior swap externally vs internal state transitions; who decides the change |
| 6 | Strategy vs Template Method — when each? | Composition (Strategy) vs inheritance (Template); extension points |
| 7 | Observer push vs pull — tradeoffs? | Decoupling vs efficiency; what subscriber needs to know |
| 8 | Decorator vs Subclassing — when prefer each? | Open/Closed principle, runtime composition vs compile-time hierarchy |
| 9 | Adapter vs Facade — both wrap something; what's the actual difference? | Interface incompatibility (Adapter) vs interface complexity simplification (Facade) |
| 10 | Why is Singleton sometimes called an anti-pattern? | Testability, hidden coupling, global state, parallel test runs |
| 11 | Chain of Responsibility — design choices (default handler? short-circuit? logging at each step?). | Order sensitivity, fall-through semantics, side effects |
| 12 | Proxy types — virtual, remote, protection — give one real example each. | Lazy init (virtual), network/RPC (remote), ACL/auth (protection) |
| 13 | Visitor pattern — why is it controversial? When does it actually fit? | Stable class hierarchy + growing operations, double dispatch, OO-vs-procedural |
| 14 | Composite vs Decorator — both recursive; what's the actual difference? | Tree structure (Composite) vs behavior wrapping (Decorator) |
| 15 | Identify the design pattern from this snippet: `Spring's JdbcTemplate.execute(...)`. | Template Method recognition |
| 16 | Saga: orchestration vs choreography — which when? | Central control vs event-driven; complexity vs coupling |
| 17 | Why Repository pattern? What does it abstract away? | DDD, persistence ignorance, testability via in-memory impl |
| 18 | DI vs DIP — clarify the distinction. | Principle (DIP — depend on abstractions) vs implementation technique (DI — inject the abstraction) |
| 19 | When does the Outbox pattern earn its complexity? | Reliable event publishing without 2PC, transactional consistency between DB write + message emit |
| 20 | Name 5 design patterns Spring uses internally, with concrete examples. | Factory (BeanFactory), Proxy (AOP), Template Method (JdbcTemplate), Strategy (TaskScheduler implementations), Observer (ApplicationEventPublisher) |

**Recommended Sprint plan for Design Pattern Qs:**

- **Early Sprint (Weeks 1–6):** Read each question and self-rate. ✅/🟡/🔴 confidence per question.
- **Mid-to-late Sprint (Weeks 7–12):** Rehearse 🔴 / weak-🟡 questions out loud against a timer (2-min answers).
- **The week before any interview:** Run through all 20 in one session as a vocabulary refresh.

---

## Section 4: Other practice

Cross-cutting prep that doesn't fit the above buckets.

### 4.1 Behavioral / STAR stories

Prepare **5–8 stories** covering the following themes. Each story = ~2-minute STAR narrative (Situation, Task, Action, Result). Same story can serve multiple themes.

| # | Theme | Why interviewers ask |
| --- | --- | --- |
| 1 | A technically ambitious project you owned | Tests scope, design judgment, ownership |
| 2 | A production incident you handled | Tests calm under pressure, debugging, communication |
| 3 | A disagreement with a peer / manager you resolved | Tests collaboration, influence without authority |
| 4 | A time you mentored a junior engineer | Tests senior signal (you grow others, not just yourself) |
| 5 | A failure / mistake and what you learned | Tests growth mindset, accountability |
| 6 | A cross-team / cross-functional project | Tests influence and stakeholder management |
| 7 | A time you pushed back on requirements | Tests engineering judgment, principled disagreement |
| 8 | A time you simplified something over-engineered | Tests YAGNI, taste, restraint |

**Where to draft:** maintain a `study_plan/STARStories.md` (create when Sprint Week 8 starts, not earlier — drafts will be stale by then).

### 4.2 Domain cross-questions (already in per-Part docs)

These are interview rehearsal material, not new content:

| Source | Count | Use |
| --- | --- | --- |
| `parts/Part_29_Domain_Patterns_KYC.md` § Cross-questions | 55 | KYC orchestration, scaling, reliability, compliance, security, domain depth, system-design — practice walking through each out loud, 2 min max |
| `parts/Part_31_Platform_Deep_Cuts.md` § Cross-questions | 26 | Architecture & flow, data & state, scale & performance, security & SDK, compliance & ops, vendor — your platform-specific war-story material |
| `parts/Part_07_HLD_Distributed.md` § Trick questions | ~5 | Distributed systems gotchas |
| `parts/Part_06_Databases.md` § Trick questions | ~5 | Isolation level / MVCC traps |
| `parts/Part_01_Java_JVM.md` § Trick questions | ~5 | HashMap / String / Collections traps |
| `parts/Part_02_Concurrency.md` § Trick questions | ~5 | JMM, volatile, deadlock gotchas |

> **Note:** Part 29 § Cross-questions section 6 ("System design cross-questions in your domain") covers 7 HLD problems that overlap with Section 2 of this doc (notably #16 KYC platform, #10 Notification service, #13 Distributed rate limiter). Don't double-rehearse — pick whichever framing helps that day.

### 4.3 Compensation & promotion prep

- Read `levels.fyi` for your level + region 2 weeks before any interview.
- Skim *The Pragmatic Engineer* compensation reviews quarterly.
- Maintain a brag doc (weekly notes) — your promotion currency. See `parts/Part_30_Interview_Career_Prep.md` § Quick recall.

---

## Quick recall

**Q. How many LLD problems should I aim to solve in the Sprint?**
A. 8–10 with quality > 25 with rush. Pick across difficulties: 3 Easy (warm-up), 4 Medium (core), 2-3 Hard (stretch).

**Q. Which HLD problem is non-negotiable for me?**
A. #16 (KYC / identity verification platform). It's your domain — interviewers will *expect* a polished walk-through. Practice it every other week.

**Q. How to use Section 3 (design pattern Qs)?**
A. Survey first (✅/🟡/🔴), then rehearse only the 🔴 + weak-🟡. Don't waste rehearsal time on ones already cold.

**Q. When do I draft STAR stories?**
A. Mid-Sprint onwards (around Week 7–8). Earlier drafts get stale and you'll rewrite them anyway. Run them live in the Week 12 mock-loop.

**Q. Where do the existing cross-questions live?**
A. `parts/Part_29_Domain_Patterns_KYC.md` § Cross-questions (55 items) + `parts/Part_31_Platform_Deep_Cuts.md` § Cross-questions (26 items). Don't duplicate them here — practice from those docs directly.
