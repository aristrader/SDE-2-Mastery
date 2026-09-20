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

Treat an instruction to process the next case study as authorization to complete its entire workflow, not merely to
start intake or drafting. Keep a visible private checklist for source preservation, reference comparison, drafting,
writer pass, unfamiliar-reader critic pass, rendered audit, generation, build, backlog record, and requested commit/
push. Do not describe the module as complete, update it to `Validated`, commit it, or stop for a status handoff while
any applicable gate remains unfinished. A status question is a request for an update, not a request to pause. If a
command fails, report its exact result and continue with safe independent work; stop only for a real authority or
external-state blocker.

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

Before a substantial rewrite, inventory each distinct source claim, example, estimate, misconception, diagram, failure
mode, and follow-up. Map every item to the revised case-study section, a clearly linked theory/design/exercise page, or
an explicitly named interview-scope cut with a reason. After drafting, recheck the map against the source. Do not use a
smaller word count, cleaner narrative, or Git history as evidence that material was preserved.

## Reference-evidence gate

For a public, first-time-reader case study, perform a real quality comparison before drafting and again after the
rendered review. A reputable interview resource such as Hello Interview is a benchmark for learning flow; an
authoritative engineering source is evidence for a factual mechanism. Neither is a prose, heading, diagram, or
solution template.

Make a private working map with these reader milestones. For each one, record the existing-source concept, the
benchmark expectation, and the exact final heading, visual, or linked destination that proves it survived:

1. the prompt, assumptions, and explicit scope;
2. a plain-language mental model and a simple baseline;
3. the pressure that breaks the baseline;
4. requirements and one estimate that change a decision;
5. API/data contracts and ownership;
6. a complete normal write or event path;
7. a complete normal read path;
8. the chosen scaling strategy and rejected alternative;
9. a concrete correctness/failure/recovery boundary; and
10. an interview delivery order and decision-focused recall.

Do not score a milestone as covered because a heading, table, citation, or rendered diagram exists. It is covered
only when a reader can explain the causal chain without relying on unstated background knowledge. Define a term on
first use when it carries the design: for example, distinguish a post, a feed entry, fanout, candidate selection,
and hydration instead of treating them as interchangeable labels.

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
until each dimension is 20/20. A 20 requires reader-verifiable evidence from the reference-evidence map, not a
successful build, a plausible component list, or a diagram that merely renders. When a score is lower, identify the
exact missing explanation or visual, improve the page, and update this skill or `study-visuals` with the narrow rule
that would have prevented that gap before moving to the next case study. Report the final scorecard, evidence, and
preventive rule changed in the handoff.

## Validation

1. Confirm `index.md`, `design/index.md`, and `exercise/index.md` remain a valid system-design module.
2. Run `node scripts/generate-homepage.js` and `git diff --check`.
3. Run the docs build after page or visual changes.
4. In a local preview, inspect diagrams for rendering, readable labels, console errors, and overflow.
5. Record `Validated (QG-4)` only after every contract gate passes; otherwise leave the module in `Draft` or
   `Needs quality review` in the quality backlog.
