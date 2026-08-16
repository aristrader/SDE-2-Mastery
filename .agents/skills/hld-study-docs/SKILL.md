---
name: hld-study-docs
description: Process high-level system-design material into the TestingTesting repository. Use when importing HLD transcripts or notes, creating or improving HLD case studies, adding system-design diagrams, or updating HLD study-plan rows. Preserve substantive learner reasoning, select interview-relevant deep dives, cover scaling and failure trade-offs, and validate navigation and syllabus links.
---

# HLD Study Docs

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied source to EOF. Handle long single-line transcripts correctly.
3. Inspect the destination case study plus related concept and case-study docs. Improve existing material rather than duplicating it.
4. Convert the source into: narrow requirements, core entities/APIs, a simple functional architecture, and two or three deep dives selected from the actual non-functional constraints.
5. Use original SVG, Draw.io, or Mermaid diagrams only when they clarify a request path, state transition, or distributed coordination.

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
- Preserve useful learner questions, misconceptions, and analogies. Resolve them directly instead of deleting the struggle that made the explanation useful.
- Avoid both extremes: do not reduce a design to unexplained bullets, and do not retain source-video filler or implementation detail that does not affect an interview answer.
- Prefer one original architecture, state, or flow diagram for a complex design. Never copy book, course, PDF, or web screenshots. Put repo-owned image assets in the module `assets/` directory only when its schema permits them; otherwise use a concise Mermaid diagram.
- End study pages with short `## Quick recall` prompts that test decisions, not vocabulary.

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
