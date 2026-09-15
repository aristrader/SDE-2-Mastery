---
name: theory-study-docs
description: Process non-HLD and non-LLD interview-study notes into the TestingTesting repository. Use when importing theory transcripts, book notes, temp.md content, or concept explanations for Java, backend, databases, networking, security, or distributed systems. Preserve substantive learner reasoning, create readable topic documentation, use original visuals only when helpful, and update linked study-plan rows.
---

# Theory Study Docs

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

## Writing standard

- Start from the problem the concept solves, then establish the mental model before mechanisms, APIs, or edge cases.
- Use complete sentences, short sections, concrete backend examples, and precise comparisons. Do not produce a dense glossary or an over-compressed cheat sheet.
- Give each paragraph a job: claim, reason, and concrete consequence. Use bullets or tables only when
  comparison is clearer than prose.
- Keep SDE2 interview depth: explain what it is, why it exists, how the relevant path works, important trade-offs, and the most likely traps.
- Add original visuals only when a flow, state transition, data layout, or comparison is difficult to understand in text. Never copy screenshots from books, courses, PDFs, or web pages. Respect the module schema before adding an `assets/` directory.
- End study pages with `## Quick recall` containing short interview-focused Q&A.

## Study-plan handling

- Link covered Part rows to the destination docs. Use `Partial` only when a concrete gap remains; mark `Done` only after substantial coverage.
- Update `TopicIndex.md` when Part topics are added, renamed, moved, split, or substantially reworded.
- Clear processed temp files to their placeholder comments using absolute paths; do not delete them.

## Validation

1. Run `node scripts/generate-homepage.js` after page or navigation changes.
2. Run relevant Java verification only when runnable code changed, then run `git diff --check`.
3. Preserve unrelated worktree changes and do not commit unless asked.
4. Report docs updated, Part rows touched, partial gaps, and source position.
