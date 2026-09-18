---
name: hld-study-docs
description: Process high-level system-design material into the TestingTesting repository. Use when importing HLD transcripts or notes, creating or improving HLD case studies, adding system-design diagrams, or updating HLD study-plan rows. Preserve substantive learner reasoning, select interview-relevant deep dives, cover scaling and failure trade-offs, and validate navigation and syllabus links.
---

# HLD Study Docs

## Reader-first quality bar

For a substantial page or rewrite, choose one representative user action before writing. Use it to
carry the reader from the simple design through the pressure that breaks it and the mechanism that
solves it. A component list is not an explanation.

Use this order unless the supplied material genuinely requires another one:

1. State the problem, the useful scope, and what is deliberately out of scope.
2. Walk through the smallest viable request path.
3. Name the concrete pressure that breaks that path.
4. Introduce only the component or protocol that addresses that pressure, including ownership and
   a request/data-flow consequence.
5. For each deep dive, explain the trade-off, failure mode, and recovery policy.

Delete generic claims such as "scalable" or "reliable" unless the page names the workload, failure,
or correctness property that earns the claim. A first-time reader should be able to answer: what
happens on the normal path, why the simpler option fails, and why the chosen design is worth its cost.

Before finalizing, give the design as a one-minute spoken explanation in heading order. If that
explanation needs a component whose reason or flow was never introduced, add the missing reasoning or
cut the component.

For an interview-oriented page, make the assumption that unlocks the design explicit, state the simpler
baseline first, and name the measurable or correctness trigger that justifies a more complex component.
Do not turn a case study into an operational runbook or a catalog of cloud services.

## Workflow

For pages under `system_design/case_studies/`, use the repository `hld-case-study-docs` skill as the active
workflow; this skill remains the shared workflow for HLD patterns, components, and concepts.

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied source to EOF. Handle long single-line transcripts correctly.
3. Inspect the destination case study plus related concept and case-study docs. Improve existing material rather than duplicating it.
4. Convert the source into: narrow requirements, a simple functional architecture, one concrete
   request flow, and two or three deep dives selected from actual non-functional constraints.
5. After drafting a substantial page, use the repository `study-visuals` skill to decide whether an existing
   visual is sufficient or whether a new Mermaid, source-controlled architecture view, or supplementary
   illustration is warranted. Use original SVG, Draw.io, or Mermaid diagrams only when they clarify a
   request path, state transition, or distributed coordination.
6. Before calling a rewrite complete, trace the main request, the pressure that breaks it, the chosen
   trade-off, and the failure-recovery path. Each must be either shown by the current design or labeled as
   a deliberate follow-up; never imply a resilience property that the design does not provide.

## Source grounding and quality comparison

For a substantial rewrite, prepare a small source packet before drafting: user-supplied notes, relevant
existing repository content, and any available local book or PDF material such as Alex Xu. Use those to
preserve context and check scope, estimates, and plausible design choices; do not claim a book is available
if it cannot be located. Then use authoritative online sources for factual technology claims and a reputable
HLD reference such as Hello Interview to audit coverage, flow, and interview depth.

Use the internet and books as evidence and a quality bar, not as a template. Write an original explanation
for this repository's learner and preserve the page's own scope. Never copy prose, heading order, diagrams,
figures, examples, or proprietary exercises. Match causal clarity, coverage, and interview usefulness—not
page length.

Before any external research, reduce the question to a generic technical pattern. Never send company names,
client names, internal service or project names, unpublished metrics, architecture details, schemas, source
code, credentials, screenshots, or repository content to an external site or tool. Use neutral examples in
the resulting documentation unless the user explicitly authorizes identifiable context.

Before finalizing, check that the rewritten page can answer the same useful questions as the source packet:
what is being designed, the normal path, pressure points, chosen trade-offs, and failure recovery. Add a
compact further-reading link only when it is useful to the learner and was actually consulted.

For a case study, use the `hld-case-study-docs` reference-evidence gate rather than treating the presence of a
diagram, headings, tables, or citations as quality evidence. The review must establish that a first-time reader can
state the baseline, the pressure that breaks it, the selected mechanism, its cost, and its failure boundary.

## Source-preservation gate

Before a structural rewrite, inventory each distinct source claim, example, estimate, misconception, diagram,
failure mode, and follow-up. Map each item to the revised page, a clearly linked destination module, or an explicitly
named interview-scope cut with a reason. Recheck the map after drafting. A shorter page, clearer flow, or Git history
does not prove that reusable study material survived.

## Required HLD depth

- Tie every deep dive to a concrete pressure: hot read/write path, correctness conflict, queue backlog, locality query, dependency failure, or cost.
- Explain the data/control flow, decision, trade-off, and failure recovery. Do not add named components without a reason.
- Separate short-lived coordination, such as a lease, from durable business state, such as a conditional update or state machine.
- Cover idempotency and recovery for retries, queues, timeouts, and external effects.
- Explain why a queue is appropriate. Do not queue freshness-sensitive state merely to reduce write QPS.
- Distinguish data partitioning, workload ownership, replication, and regional deployment.
- Keep to SDE2 interview depth unless the user asks for staff-level depth.

## Writing and visuals

- Make the page readable as a learning artifact: introduce the problem and mental model before the component detail, use complete sentences, and keep closely related decisions together.
- A paragraph should make one claim, explain why it matters, and show its concrete effect. Do not
  substitute tables or bullet lists for causal prose.
- Preserve useful learner questions, misconceptions, and analogies. Resolve them directly instead of deleting the struggle that made the explanation useful.
- Avoid both extremes: do not reduce a design to unexplained bullets, and do not retain source-video filler or implementation detail that does not affect an interview answer.
- Prefer one original architecture, state, or flow diagram for a complex design. Never copy book, course,
  PDF, or web screenshots. Put repo-owned image assets in the module `assets/` directory only when its
  schema permits them; otherwise use a concise Mermaid diagram.
- For component and concept pages, audit the core mechanism separately from the page as a whole. If a
  first-time reader must mentally simulate a cache write policy, edge-cache miss/invalidation, load-balancer
  health transition, limiter decision, replication/quorum transition, or similar multi-step mechanism, show
  that mechanism in an original diagram or a compact state/decision table. A page-level architecture diagram
  does not substitute for the mechanism diagram. Do not add diagrams for simple definitions that prose or a
  comparison table explains more clearly.
- An ASCII arrow sketch may orient the reader, but it is not a substitute when the core mechanism has
  branches, state changes, or a failure/recovery path that a rendered diagram needs to make unambiguous.
- When one page presents several alternatives, give the reader a selection rule and make the normal path,
  write/update path, and failure boundary visible for the alternatives that have materially different
  correctness or latency behavior. Do not leave an important option as an isolated definition.
- When an audit finds a narrowly missing quality dimension, close that gap with the smallest useful
  artifact: a state/sequence diagram for an obscured lifecycle, a decision table for a dense survey, or a
  concrete incident/recovery path for an otherwise catalog-like page. Do not inflate a page merely to raise
  an aggregate score; the result must improve what a first-time reader can decide or explain.
- Treat a high rubric score as evidence of coverage, not a reason to hide a weak dimension. A page is not
  publish-ready if its normal path, failure/recovery boundary, selection rule, or diagram purpose is absent,
  even when its average score is high.
- Give every diagram one job: an architecture diagram shows ownership and request/data paths, a sequence
  diagram shows an ordinary or failure interaction, and a state diagram shows a lifecycle. Do not add a
  visual that merely restates adjacent prose.
- Audit diagrams against the stated design: arrows must have the correct caller, receiver, direction, and
  outcome; normal and failure paths must be visibly distinct; and an unimplemented component or recovery
  policy must be labeled as a follow-up. Split a crowded diagram instead of relying on crossing arrows or
  vague labels.
- When diagrams change, run the docs build. If a local preview is available, verify every Mermaid block
  becomes one SVG without browser-console errors or horizontal overflow at a normal desktop viewport.
- End study pages with short `## Quick recall` prompts that test decisions, not vocabulary.

## Scored completion

After a substantial rewrite and rendered review, score scope/mental model, causal mechanism, trade-offs and
failure recovery, and communication/visual clarity out of 20. A page is complete only when every applicable
dimension is 20/20. For every lower score, name the precise missing evidence, improve the page, and add the
narrow preventive rule to this skill or `study-visuals` before continuing to another page. A successful build,
complete-looking table, or visible diagram is never sufficient evidence for 20/20. Report the final scorecard,
reader-verifiable evidence, and changed rule in the handoff.

## Study-plan handling

- Preserve source discussion, misconceptions, and learner questions when reorganizing.
- Link covered Part rows to the destination docs. Use `Partial` only when a specific gap remains; mark `Done` only after substantial coverage.
- Update `TopicIndex.md` when Part topics are added, renamed, moved, split, or substantially reworded.
- Clear processed temp files to their placeholder comments using absolute paths; do not delete them.

## Validation

1. Run `node scripts/generate-homepage.js` after site-page changes.
2. Run `git diff --check`.
3. Preserve unrelated worktree changes and do not commit unless asked.
4. Report docs updated, Part rows touched, partial gaps, and source position.
