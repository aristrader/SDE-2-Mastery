---
name: hld-case-study-docs
description: >-
  Create or improve TestingTesting high-level system-design case studies with interview scope, a complete design
  path, focused deep dives, recovery reasoning, and paired design/exercise pages.
---

# HLD Case Study Docs

Use the shared [efficient execution policy](../study-visuals/references/efficient_execution.md) for the common
QG-4 gates. For a substantial case-study rewrite, read [the QG-4 acceptance contract](references/quality_contract.md)
before drafting; it owns the detailed evidence, writer/critic, render, scorecard, and backlog rules.

## Outcome

Enable an SDE2 to deliver a coherent 35–40 minute answer: a viable scoped design, its pressure-driven evolution,
and only the deep dives that prove its important choices. Do not turn the page into either a component catalogue or
a staff-level production plan.

## Workflow

1. Read the existing case-study, paired `design/` and `exercise/` pages, local sources, and relevant shared concepts
   once. Preserve each reusable claim, example, estimate, failure mode, diagram intent, and follow-up in place, a
   linked destination, or an explicit scope cut.
2. Research only generic technical questions. Use primary sources for mechanisms and two independent accessible
   references for coverage; never send repository or company material externally or copy another source's prose,
   examples, diagrams, code, or page shape.
3. Tell the design in this order: narrow prompt and exclusions → clarification answers that change the design →
   prioritised requirements and decision-changing estimate → naive baseline and pressure → chosen normal path →
   components introduced for that pressure → two or three focused deep dives → failure/recovery and caller outcome
   → interview delivery and Quick recall.
4. Define a component before relying on it. Every deep dive explains problem → naive failure → mechanism →
   trade-off → recovery. State precise ownership and ordering, delivery, freshness, or consistency guarantees.
5. Keep `exercise/` concise and attemptable. Keep `design/` to agreed requirements, key API/data shapes, chosen
   path, and decisions; neither tab duplicates a second long-form lesson.

## Guardrails

- An estimate belongs only when it changes partitioning, connection ownership, storage, fanout, queueing, latency,
  or cost. Mark extras as deferred rather than silently omitting them.
- A durable record owns an accepted business outcome; a cache, notification, or best-effort stream must not silently
  become the correctness path. Distinguish deduplication identity from ordering scope and name the cursor's stream.
- Keep reusable theory in a linked theory page when it would distract from the walkthrough; never discard it merely
  to shorten the case study.
- Use `study-visuals` after drafting. A visual must answer a distinct reader question and be checked after render;
  normal context/ownership and recovery/strategy visuals must not duplicate each other.
- Record `Validated (QG-4)` only after every contract gate passes; otherwise keep the backlog status truthful.
