---
name: hld-case-study-docs
description: Create or improve TestingTesting high-level system-design case studies. Use for system_design/case_studies modules that need interview scope, a complete design path, focused deep dives, recovery reasoning, and paired design/exercise pages.
---

# HLD Case Study Docs

## Outcome

Help an SDE2 give a coherent 35–40 minute interview answer to one product/system prompt. The page must
show problem navigation, a complete viable design, and only the deep dives that prove the important choices.
Do not turn a case study into a catalogue of components or a production implementation plan.

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

Use the repository `study-visuals` skill after drafting. A case study normally needs one architecture
ownership diagram and one representative request/sequence or recovery diagram. Reuse existing valid SVGs;
replace only visuals that no longer match the written design. Use AI raster imagery only for supplementary
intuition, never for the technical architecture or flow.

Before calling a page complete, assess it against four interview dimensions used by Hello Interview. Inspect
the actual rendered reading-column width: a diagram that is technically correct but needs zoom is incomplete.

| Dimension | Evidence in the page |
| --- | --- |
| Problem navigation | Scoped requirements, explicit assumptions, and chosen cruxes. |
| Solution design | A complete main path before optional components. |
| Technical excellence | Concrete ownership, contracts, trade-offs, and failure recovery. |
| Communication | Heading order supports a short spoken walkthrough; diagrams have one job. |

Score every applicable dimension out of 20 after the rendered review. Do not mark the case study complete
until each dimension is 20/20. When a score is lower, identify the exact missing evidence, improve the page,
and update this skill or `study-visuals` with the narrow rule that would have prevented that gap before moving
to the next case study. Report the final scorecard and the preventive rule changed in the handoff.

## Validation

1. Confirm `index.md`, `design/index.md`, and `exercise/index.md` remain a valid system-design module.
2. Run `node scripts/generate-homepage.js` and `git diff --check`.
3. Run the docs build after page or visual changes.
4. In a local preview, inspect diagrams for rendering, readable labels, console errors, and overflow.
