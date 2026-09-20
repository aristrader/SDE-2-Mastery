---
name: theory-study-docs
description: Process non-HLD and non-LLD interview-study notes into the TestingTesting repository. Use when importing theory transcripts, book notes, temp.md content, or concept explanations for Java, backend, databases, networking, security, or distributed systems. Preserve substantive learner reasoning, create readable topic documentation, use original visuals only when helpful, and update linked study-plan rows.
---

# Theory Study Docs

For substantial public study-content work, follow the shared
[efficient execution policy](../study-visuals/references/efficient_execution.md).

## Reader-first quality bar

Open a substantial topic with the engineering question it answers and one small backend example.
Then explain the mechanism, the observable consequence, and the boundary where the rule stops
applying. Do not turn the page into a glossary of APIs, definitions, or disconnected best practices.

A first-time reader should be able to state what problem the concept solves, trace one relevant path,
and identify the most likely interview trap. Keep claims precise: replace broad praise such as
"efficient" or "safe" with the cost, guarantee, or failure being discussed.

Separate an API contract from a common implementation detail. Name the Java version or implementation
only when the distinction changes a correct answer; otherwise teach the stable behavior first.

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied source to EOF. Handle long single-line transcripts correctly.
3. Find the closest existing topic document and related Part row. Merge into it when the concepts belong together; create a new theory module only for a genuinely distinct topic.
4. Preserve substantive learner questions, misconceptions, analogies, and flow reasoning. Remove only source filler, repeated statements, and details outside interview scope.
5. Before calling a rewrite complete, trace the central engineering question through the mechanism, its
   observable consequence, and its boundary or exception. A rule without one of those steps is either a
   glossary entry or an unsupported claim.
6. After drafting a substantial page, use the repository `study-visuals` skill to decide whether an existing
   visual is sufficient or whether a new mechanism, decision, or conceptual visual is warranted.

## Source grounding and quality comparison

For a substantial rewrite, start with user-supplied material, relevant existing repository pages, and any
available local books or PDFs. Verify technical claims using the closest primary online source: Java/JDK
documentation for language and library behavior, framework documentation for Spring, or the owning
vendor/specification for protocols and infrastructure. Use one independent reputable learning reference to
check coverage and a coherent first-time-reader flow.

Do not copy reference prose, illustrations, heading order, examples, or exercises. Build the explanation
around this repository's backend use case, learner questions, and interview scope. Quality means a clear
problem-to-mechanism-to-consequence narrative with appropriate coverage, not a longer page. Add
further-reading links only for sources actually consulted and useful after the page is understood.

For API design, prefer the relevant HTTP specification, Google AIPs, or Microsoft API guidance; for Spring,
prefer Spring's own reference; for Java behavior, prefer the JDK documentation. A learning site may set the
interview-quality bar, but it must not be the source of a technical claim.

Before external research, turn the question into a generic technical query. Never send company or client
names, internal project or service names, unpublished metrics, architecture, schemas, source code,
credentials, screenshots, or repository content to an external site or tool. Use neutral examples in study
content unless the user explicitly authorizes identifiable context.

Keep the result interview-specific. Cut operational runbook steps, framework option catalogs, and general
background unless they change an answer a senior Java-backend interviewer would evaluate.

## Source-preservation gate

Before a structural rewrite, inventory each distinct source claim, example, learner question, misconception,
analogy, diagram, boundary, and follow-up. Map each item to the revised page, a clearly linked destination module,
or an explicitly named interview-scope cut with a reason. Recheck the map after drafting. A shorter page, clearer
flow, or Git history does not prove that reusable study material survived.

## Writing standard

- Start from the problem the concept solves, then establish the mental model before mechanisms, APIs, or edge cases.
- Use complete sentences, short sections, concrete backend examples, and precise comparisons. Do not produce a dense glossary or an over-compressed cheat sheet.
- Give each paragraph a job: claim, reason, and concrete consequence. Use bullets or tables only when
  comparison is clearer than prose.
- Keep SDE2 interview depth: explain what it is, why it exists, how the relevant path works, important trade-offs, and the most likely traps.
- Add an original visual only when a flow, state transition, data layout, or decision is genuinely harder
  to understand in text. Give it one job and introduce it with the question it answers; never copy
  screenshots from books, courses, PDFs, or web pages. Respect the module schema before adding an
  `assets/` directory.
- Audit a diagram's arrows, labels, and branches against the explained mechanism. When diagrams change,
  run the docs build; with a local preview, verify Mermaid renders to SVG without console errors or
  horizontal overflow at a normal desktop viewport.
- Do not make compatible choices look mutually exclusive in a decision visual. When a request or design can
  combine decisions, use converging branches or state the composition explicitly.
- End study pages with `## Quick recall` containing short interview-focused Q&A.

## Scored completion

After a substantial rewrite and rendered review, score problem and mental model, mechanism and concrete
consequence, traps/trade-offs/boundaries, and communication/visual clarity out of 20. A page is complete
only when every applicable dimension is 20/20. For every lower score, name the precise missing evidence,
improve the page, and add the narrow preventive rule to this skill or `study-visuals` before continuing.
Report the final scorecard and changed rule in the handoff.

## Study-plan handling

- Link covered Part rows to the destination docs. Use `Partial` only when a concrete gap remains; mark `Done` only after substantial coverage.
- Update `TopicIndex.md` when Part topics are added, renamed, moved, split, or substantially reworded.
- Clear processed temp files to their placeholder comments using absolute paths; do not delete them.

## Validation

1. Run `node scripts/generate-homepage.js` after page or navigation changes.
2. Run relevant Java verification only when runnable code changed, then run `git diff --check`.
3. Preserve unrelated worktree changes and do not commit unless asked.
4. Report docs updated, Part rows touched, partial gaps, and source position.
