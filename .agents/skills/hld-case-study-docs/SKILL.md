---
name: hld-case-study-docs
description: Create or improve TestingTesting high-level system-design case studies. Use for system_design/case_studies modules that need interview scope, a complete design path, focused deep dives, recovery reasoning, and paired design/exercise pages.
---

# HLD Case Study Docs

For substantial public case-study work, follow the shared
[efficient execution policy](../study-visuals/references/efficient_execution.md) in addition to QG-4.

## Outcome

Help an SDE2 give a coherent 35–40 minute interview answer to one product/system prompt. The page must
show problem navigation, a complete viable design, and only the deep dives that prove the important choices.
Do not turn a case study into a catalogue of components or a production implementation plan.

For a substantial rewrite, read [the QG-4 acceptance contract](references/quality_contract.md) before inspecting the
page. Its evidence record, writer pass, critic pass, and rendered audit are mandatory completion gates.

## Completion discipline

The shared contract requires full-item completion; QG-4 defines the evidence and acceptance gates.

## Source packet and boundaries

Read the existing case-study page, paired `design/` and `exercise/` pages, local source material, and the
closest shared pattern/component pages. Use authoritative technical sources for factual claims. Use Hello
Interview as a quality benchmark for prioritised requirements, delivery flow, and interview depth—not as a
template, source of copied prose, diagram, example, or solution structure.

Before external research, reduce the request to a generic technical question. Never send repository/company
information, unpublished data, source code, or screenshots externally. Keep examples neutral.

## Case-study flow

Use this order unless the problem genuinely needs another sequence:

1. State the deliberately narrow scope, user-visible operations, and explicit exclusions.
2. Show the candidate's clarification questions and the answers that materially change the design.
3. Prioritise a small set of functional requirements and concrete non-functional promises. Estimate only a
   number that changes a design decision.
4. Walk one main request or event from client to durable outcome before naming every component.
5. Introduce components only as a response to that path's pressure; state data ownership and the contract
   between synchronous and asynchronous boundaries.
6. Deep-dive into two or three cruxes selected from the requirements: ordering, fanout, sharding, connection
   ownership, durability, consistency, bottlenecks, cost, or recovery.
7. Trace retries, timeouts, partial failure, reconnection, reconciliation, and observable client outcome.
8. End with a concise interview delivery order and Quick recall.

Quick recall questions and answers must render as distinct, scannable blocks. Use normal Markdown paragraph
spacing rather than trailing-space line-break tricks that can fail validation or collapse in the site renderer.

The paired `exercise/` page is attemptable and concise. The paired `design/` page contains the agreed
requirements, key API/data shapes, chosen path, and deep-dive decisions; neither tab should duplicate a
second long-form lesson.

If an inherited case-study page also contains substantial reusable theory—for example spatial indexing, routing, or
storage mechanics—do not silently compress that theory away to make the case study shorter. Keep the HLD walkthrough
focused and move the complete interview-relevant explanation, misconceptions, examples, and trade-offs into a linked
theory module. The case page should point to it explicitly.

## Source-preservation gate

Use the QG-4 preservation map; do not compress reusable theory without moving it to a linked destination.

## Reference-evidence gate

Read the QG-4 contract before drafting. Its source map, reference comparison, reader milestones, writer/critic pass,
rendered audit, scoring, and backlog record are mandatory and are not duplicated here.

## Depth and decision rules

- Requirements should reveal the hard part. Mark extras out of scope instead of solving every product feature.
- A capacity estimate earns space only when it changes connection ownership, storage partitioning, fanout,
  queueing, shard count, or a latency/cost decision.
- State the precise ordering, delivery, freshness, or consistency guarantee. Do not claim exactly-once,
  instant failover, or global order without the mechanism and cost.
- When a flow needs both ordering and deduplication, name their separate scopes: an identity may deduplicate a
  record globally while a partition-local sequence defines order. A cursor must name the stream or partition it
  advances through; do not present one generic cursor as if it orders unrelated streams.
- A durable record decides an accepted business/event outcome. Push notifications, caches, and best-effort
  streams may optimise delivery but must not become the correctness path accidentally.
- Keep protocol semantics separate from application policy. For example, a redirect status expresses whether a
  target is permanent or temporary; caching, analytics, and later routing control require their own stated
  policy rather than an implied guarantee from the status code alone.
- Explain a simple baseline before the pressure-driven evolution. At least one deep dive must describe what
  fails, the recovery rule, and what the caller observes.
- Keep to SDE2 depth: two or three well-reasoned deep dives beat exhaustive staff-level infrastructure.

## Visuals and quality check

Use `study-visuals` after drafting. A case normally needs an ownership/context view plus a distinct normal/recovery
flow or strategy comparison; QG-4 owns the scoring evidence.

## Validation

Confirm the system-design schema. Record `Validated (QG-4)` only after every contract gate passes; otherwise use
`Draft` or `Needs quality review` in the backlog.
