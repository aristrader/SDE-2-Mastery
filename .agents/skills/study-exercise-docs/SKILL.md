---
name: study-exercise-docs
description: Process already-defined interview-study exercises and solutions into the TestingTesting repository. Use when importing exercise.md or theory.md/exercise.md pairs, creating exercise and solution pages, or adding runnable practice scaffolding. Preserve the supplied task intent and constraints, derive matching solutions, retain a clear learning flow, and validate module schema and exercise IDs.
---

# Study Exercise Docs

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied theory and exercise source to EOF before editing.
3. Keep each supplied exercise's intent, order, constraints, and concepts tested. Do not broaden it into a new curriculum.
4. Place the exercise in the closest valid module. Create exactly one matching solution using the required shared kebab-case ID.

## Learning quality

- Ensure the topic doc introduces the mental model and traps before the exercise. Do not make readers discover missing theory while attempting the task.
- Make exercise statements concrete: inputs, required behavior, constraints, and acceptance criteria. Keep the solution readable enough to teach the decision, not merely reveal an answer.
- Preserve useful learner misconceptions or hints from the source when they help a future reader avoid a common wrong turn.
- Add runnable Java playground code only when execution materially helps practice. Keep Java focused on the exercise, with no tutorial prose embedded in JavaDoc.
- Use original diagrams only when they clarify an interaction or state change. Never copy screenshots from books, courses, PDFs, or web pages.

## Module and study-plan handling

- Follow the repository's module schema, ordering, `search: false` requirements, and `## Exercise: id - Title` / `## Solution: id - Title` headings exactly.
- Link covered Part rows to destination docs. Use `Partial` only when a concrete coverage gap remains; mark `Done` only after substantial coverage.
- Update `TopicIndex.md` when Part topics are added, renamed, moved, split, or substantially reworded.
- Clear processed `theory.md` and `exercise.md` files to their placeholder comments using absolute paths; do not delete them.

## Validation

1. Run `node scripts/generate-homepage.js` after module changes.
2. Run relevant Java verification when runnable code changes, then run `git diff --check`.
3. Preserve unrelated worktree changes and do not commit unless asked.
4. Report exercises, solutions, playground files, Part rows, and partial gaps.
