---
name: theory-study-docs
description: Process non-HLD and non-LLD interview-study notes into the TestingTesting repository. Use when importing theory transcripts, book notes, temp.md content, or concept explanations for Java, backend, databases, networking, security, or distributed systems. Preserve substantive learner reasoning, create readable topic documentation, use original visuals only when helpful, and update linked study-plan rows.
---

# Theory Study Docs

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied source to EOF. Handle long single-line transcripts correctly.
3. Find the closest existing topic document and related Part row. Merge into it when the concepts belong together; create a new theory module only for a genuinely distinct topic.
4. Preserve substantive learner questions, misconceptions, analogies, and flow reasoning. Remove only source filler, repeated statements, and details outside interview scope.

## Writing standard

- Start from the problem the concept solves, then establish the mental model before mechanisms, APIs, or edge cases.
- Use complete sentences, short sections, concrete backend examples, and precise comparisons. Do not produce a dense glossary or an over-compressed cheat sheet.
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
