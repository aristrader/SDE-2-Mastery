# TODO: Java/JVM Interview Study Plan Review

Goal: turn the Java/JVM section into a useful senior-backend interview track.

This repo is a personal backend-fundamentals learning repo, not a product clone. Borrow only the useful learning patterns:

- clear study paths
- realistic prompts
- strong answer shape
- traps and follow-ups
- theory -> practice -> solution flow where practice is real

Do not copy another site's content, brand, pricing, community model, or UI.

## Review Verdict

The current direction is useful, but the old plan mixed too many concerns. The valuable part is the learning architecture. The weak part is product polish and visual/UI work before the content path is strong.

Keep:

- Java/JVM landing page as a minimal generated-navigation hub.
- Better theory pages for high-value Java topics.
- Real exercises only where writing or debugging code proves understanding.
- Topic-local interview recap pages for Java interview prompts.
- Separate treatment for theory, code practice, design, HLD, and LLD.

Cut:

- Long source-review inventory. It does not help implement the repo.
- Community, pricing, login, AI tutor, progress persistence, comments.
- Broad UI makeover.
- Decorative cards, badges, and visuals as first-order work.
- Visuals for every topic.
- Exercises for topics where practice would be artificial.
- Academic surveys that do not help a Java backend interview answer.
- Large frontmatter/schema changes before Markdown labels prove useful.

Defer:

- Site-wide UX changes.
- Persistent progress tracking.
- AI feedback.
- Heavy interactive diagrams.
- Global curriculum schema changes.

## Core Content Test

Every item must pass this:

> Would a senior Java backend interviewer plausibly ask about this, or would knowing it help answer something they ask?

If no, delete it.

Good content:

- Java-specific internals and contracts.
- Gotchas that create bugs in backend code.
- Performance characteristics of Java's actual tools.
- Correct vs incorrect Java examples.
- Interview answer shape and likely follow-ups.
- Short recall questions.

Bad content:

- History without current relevance.
- Comparisons to non-Java implementations.
- Pattern catalogs with no design pressure.
- API walkthroughs the user already knows from daily coding.
- Completeness for its own sake.

## Content Types

Use different requirements by topic type. Do not force one template everywhere.

### 1. Theory-Centric Topics

Use for topics where the value is explanation, recognition, and interview articulation.

Examples:

- JDK vs JRE vs JVM
- bytecode execution overview
- platform independence
- access modifiers
- nested classes, unless tied to a real design case
- Java string pool and immutability

Requirements:

- Explain the mechanism.
- Name the interview trap.
- Include a 60-second answer.
- Include 3-6 focused follow-up questions.
- End with `## Quick recall`.
- Add code only when it clarifies a trap.

Do not add:

- full exercise/solution folders unless there is a real task
- artificial quizzes
- long historical sections
- broad language comparisons

Recommended page shape:

```text
# Topic

## How it works
## Java-specific traps
## Interview answer
## Follow-ups
## Quick recall
```

### 2. Theory + Code Practice Topics

Use where the concept is not understood until the user writes, predicts, or debugs code.

Examples:

- equals/hashCode
- HashMap behavior
- Comparator and ordering
- generics wildcards and erasure
- Optional misuse
- streams laziness
- immutability
- synchronized, volatile, locks
- ExecutorService
- CompletableFuture

Requirements:

- Theory page explains the mechanism and traps.
- `exercise/index.md` contains focused tasks.
- `solution/index.md` has matching solution IDs.
- Use runnable Java under `playground/` only when running code proves the point.
- Keep exercises small: predict output, fix bug, implement one class, explain trade-off.

Do not add:

- 10+ exercise dumps on one page
- broad coding katas unrelated to the topic
- starter projects or frameworks
- fake "practice" that is just rephrased theory

Exercise shape:

```text
## Exercise: kebab-id - Title

### Goal
### Task
### Constraints
### Checks
```

Solution shape:

```text
## Solution: kebab-id - Title

### Approach
### Code
### Why this works
### Follow-up
```

### 3. Design-Centric Topics

Use for design thinking, trade-offs, API shape, state modeling, boundaries, and failure modes where code is secondary.

Examples:

- API design
- design docs and RFCs
- idempotency
- domain modeling
- dependency boundaries
- choosing composition over inheritance
- pattern selection

Requirements:

- Start from a concrete scenario.
- Define constraints and non-goals.
- Show trade-offs.
- Name failure modes.
- Show the decision path, not just the final answer.
- Include "bad design" vs "better design" when useful.

Do not add:

- generic pattern definitions with no scenario
- template-heavy architecture documents
- UML unless it clarifies a specific model
- code-first exercises unless implementation choices matter

Recommended page shape:

```text
# Topic

## Scenario
## Requirements
## Constraints
## Design choices
## Trade-offs
## Failure modes
## Interview answer
## Quick recall
```

### 4. Design + Practice Topics

Use when the user should actually design something and compare alternatives.

Examples:

- design an immutable value object
- design an LRU cache API
- model a parking lot
- model an elevator
- model rate limiting objects
- design command/strategy usage for a real backend case

Requirements:

- Provide a prompt with clarified scope.
- Ask for entities, responsibilities, invariants, and extension points.
- Require at least one trade-off.
- Provide a reference solution, not the only possible solution.
- Include "what interviewer pushes on next".

Do not add:

- production-grade implementation requirements
- database, networking, or deployment details unless the prompt is HLD
- over-patterned solutions
- diagrams that replace reasoning

Recommended exercise shape:

```text
## Exercise: kebab-id - Title

### Prompt
### Requirements
### Out of scope
### Design tasks
### Review checks
```

### 5. HLD Topics

Use for system design: scale, data flow, storage, consistency, availability, queues, caching, APIs, observability, failure handling.

Examples:

- URL shortener
- feed/timeline
- chat
- file upload
- notification system
- rate limiter
- distributed cache
- search autocomplete

Requirements:

- Start with functional and non-functional requirements.
- Define capacity assumptions only when needed for a design decision.
- Draw/read a component flow.
- Discuss APIs, storage model, cache strategy, async flow, failure modes, and trade-offs.
- Include bottlenecks and how the design evolves.
- Keep Java-specific details out unless the prompt asks for implementation.

Do not add:

- class diagrams as the main artifact
- Java collection details
- low-level object modeling
- exhaustive cloud-provider feature lists
- fake precision capacity math

Recommended HLD shape:

```text
# System

## Prompt
## Requirements
## Scale assumptions
## High-level design
## APIs
## Data model
## Core flows
## Bottlenecks
## Failure modes
## Trade-offs
## Follow-ups
```

### 6. LLD Topics

Use for object modeling, class responsibilities, invariants, extensibility, concurrency correctness inside one service or process.

Examples:

- parking lot
- elevator
- splitwise
- chess
- vending machine
- ride matching core model
- rate limiter class design
- in-memory cache

Requirements:

- Clarify scope first.
- Identify core entities and relationships.
- State invariants.
- Define public APIs.
- Show class responsibilities.
- Discuss extensibility and correctness.
- Add code only for the core model or a tricky behavior.

Do not add:

- distributed systems concerns unless explicitly part of prompt
- database schema as the centerpiece
- Spring controllers/repositories
- pattern name-dropping
- fully productionized code

Recommended LLD shape:

```text
# Problem

## Prompt
## Scope
## Entities
## Invariants
## APIs
## Class design
## Tricky cases
## Reference implementation
## Follow-ups
```

## Java/JVM Track Strategy

First make Java/JVM coherent. Do not start with UI polish.

1. Keep the Java landing page minimal and generated-navigation driven.
2. Strengthen high-value theory pages.
3. Add code practice only for topics that need it.
4. Add topic-local interview recap pages only where they help interview preparation.
5. Add visuals only where they reduce real confusion.
6. Add UI/status polish last, if still useful.

## Java/JVM Priority Map

Must know:

- Collections: List, Set, Map, Queue, hashing, sorting.
- OOP contracts: equality, immutability, dispatch, access, static/final.
- Generics: invariance, bounds, wildcards, erasure.
- Coding fluency: lambdas, Optional, streams, records, immutable objects.
- Concurrency: race conditions, synchronized, volatile, JMM, executors, locks.
- JVM: compilation pipeline, bytecode basics, GC basics.

Common interview:

- HashMap internals.
- equals/hashCode contract.
- Comparable vs Comparator.
- mutable map key bug.
- raw types and heap pollution.
- stream laziness and short-circuiting.
- Optional misuse.
- visibility vs atomicity.
- deadlock basics.
- ExecutorService sizing and shutdown.

Deep dive:

- CompletableFuture composition.
- concurrent collections internals.
- advanced GC trade-offs.
- class loading.
- lock fairness and conditions.
- wait/notify correctness.

Rare / advanced:

- nested class edge cases.
- bytecode instruction detail.
- uncommon collection implementation details.
- historical JVM changes without current interview value.

Skip for now:

- full bytecode instruction catalog.
- Java agent/instrumentation.
- custom classloader implementation.
- deep GC tuning flags.
- JIT compiler internals beyond enough to explain warmup and optimization.
- collection algorithms Java does not use.

## First Java/JVM Rollout

Keep the first rollout small.

### Phase 0: Audit

Deliver:

- inventory of Java pages
- which pages are strong
- which pages need theory cleanup
- which pages need practice cleanup
- which pages need runnable examples
- which pages should stay theory-only
- exact Phase 1 touch list

Do not edit content during the audit unless fixing obvious broken links in the same file.

### Phase 1: Hub

Touch:

- `src/main/java/org/example/backend_fundamentals/java/index.md`

Do:

- keep the page as a minimal hub
- rely on generated navigation for section/topic cards

Do not:

- change generator schema
- add global CSS
- rewrite module pages
- hardcode study routes or topic tables

### Phase 2: High-Value Page Cleanup

Order:

1. `java/oop/equals_hashcode/`
2. `java/collections/maps/` and `java/collections/hashing/`
3. `java/generics/basics/`, `bounds/`, `wildcards/`, `erasure/`
4. `java/concurrency/race_conditions/`, `synchronized_keyword/`, `volatile_keyword/`, `jmm/`
5. `java/concurrency/executor_service/`, `locks/`, `completable_future/`
6. `java/jvm/jdk_jre_jvm/`, `compilation_pipeline/`, `bytecode_execution/`, `gc/`

For each touched page:

- cut low-value breadth
- add Java-specific mechanism
- add traps
- add interview answer
- keep or add focused code only where helpful
- end with `## Quick recall`

### Phase 3: Code Practice

Add only these first:

1. equals/hashCode mutable key bug
2. HashMap lookup/collision/resize reasoning
3. volatile/JMM visibility bug
4. generics wildcard API design
5. stream laziness and short-circuiting

Do not add more until these are useful and paired with solutions.

### Phase 4: Topic-Local Interview Recaps

Add interview recap folders inside the owning topic/module, not under a top-level Java drill hub.

Suggested structure:

- `interview_recap/index.md`
- `interview_recap/quick_revision/index.md`
- `interview_recap/questions/index.md`

Use this first for:

1. Generics
2. JVM
3. Concurrency
4. Collections
5. OOP
6. Streams

Rules:

- quick revision contains method/API names or high-level topic names with one-line definitions
- questions contains 2-3 strong questions only
- questions may reuse or link existing exercises
- do not move or delete the full exercises from their original topic folders
- do not create `java/interview_drills/`

### Phase 5: Visuals

Add visuals only after the related text is stable.

Good first visuals:

- HashMap lookup/collision/resize.
- stream laziness and short-circuiting.
- JMM visibility and happens-before.

Do not start with:

- GC region diagrams
- class loading diagrams
- bytecode pipelines

Reason: those are easy to overbuild and rarely beat concise text for this repo.

## Design Track Strategy

Design topics should not be stuffed into Java/JVM docs unless the topic is Java-specific.

Use existing locations:

- generic design thinking: `src/main/java/org/example/backend_fundamentals/todo/study_plan/deep_dives/DesignThinkingProcess.md`
- pattern selection: `src/main/java/org/example/backend_fundamentals/todo/study_plan/deep_dives/PatternSelectionExercise.md`
- design patterns: `src/main/java/org/example/backend_fundamentals/design_patterns/`
- API design: `src/main/java/org/example/backend_fundamentals/networking/api_design/`
- system design: `src/main/java/org/example/backend_fundamentals/system_design/`

First useful design work:

1. Add a short design-answer checklist.
2. Add bad-vs-better examples for pattern selection.
3. Add practice prompts where there is a concrete scenario.
4. Keep pattern docs tied to pressures: changing algorithm, object creation, notification, state transitions, composition.

Cut from design docs:

- pattern worship
- "use Factory because factories are good"
- giant UML-first examples
- framework-heavy implementations
- broad taxonomy pages that do not improve answers

## HLD Track Strategy

HLD needs separate artifacts from Java/JVM.

First useful HLD structure:

- HLD hub with problem categories.
- Reusable design checklist.
- 3-5 canonical problems with complete breakdowns.
- One failure-mode drill bank.

Initial HLD problems:

1. URL shortener
2. rate limiter
3. notification system
4. file upload / media processing
5. chat or real-time updates

Each HLD problem must cover:

- requirements
- scale assumptions
- APIs
- data model
- high-level architecture
- bottlenecks
- failure modes
- trade-offs
- follow-ups

Do not mix in:

- Java syntax
- class-level object models
- Spring Boot scaffolding
- excessive cloud service catalogs

## LLD Track Strategy

LLD should be its own track or clearly separated under design patterns / practice.

First useful LLD structure:

- LLD hub with modeling checklist.
- 3-5 canonical problems.
- Reference solutions that show invariants and extensibility.
- Short code only where it proves the model.

Initial LLD problems:

1. Parking lot
2. Elevator
3. Vending machine
4. Splitwise / expense sharing
5. In-memory LRU cache

Each LLD problem must cover:

- scope
- entities
- relationships
- invariants
- APIs
- class responsibilities
- extension points
- tricky cases
- reference implementation

Do not mix in:

- distributed scaling
- storage partitioning
- message queues
- cloud deployment
- production-grade Spring layers

## UX Rules

UX improvements are useful only after content is coherent.

Allowed later:

- Java/JVM landing page sections
- plain Markdown priority labels
- "practice available" links
- "next best page" links
- small diagrams where they clarify a mechanism

Cut for now:

- global theme redesign
- progress persistence
- login
- comments
- AI tutor
- decorative hero sections
- heavy diagram libraries
- schema changes for badges

## Validation

After adding, moving, deleting, or renaming content:

```bash
node scripts/generate-homepage.js
```

After Java playground or runnable Java changes:

```bash
mvn -q compile
```

After UX/layout changes:

- run docs dev server
- smoke test desktop and mobile
- check `/java/` and touched routes
- check no text overlap at 390px width

Before commit:

- review `git diff --stat`
- review `git diff --name-status`
- confirm generated navigation was not manually edited
- confirm unrelated user files are not staged

## Non-Goals

- Do not clone another interview site's UI.
- Do not build community/pricing/login features.
- Do not add AI tutor or feedback loops.
- Do not add progress persistence.
- Do not turn every page into guided practice.
- Do not add empty exercise folders.
- Do not rewrite Collections unless a concrete gap is found.
- Do not change global schema/frontmatter in the first pass.
- Do not add large dependencies.

## Success Criteria

- Java/JVM root reads like a study track, not a folder listing.
- A 1-day user has a clear route.
- A 1-week user has a clear route.
- Theory-only topics stay concise.
- Theory + code topics have real exercises and paired solutions.
- Design, HLD, and LLD are separated by the kind of thinking they require.
- High-value Java topics have traps, follow-ups, and quick recall.
- Low-value breadth is cut instead of preserved.
- The site remains fast, readable, and mobile-safe.
