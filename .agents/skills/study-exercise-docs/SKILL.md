---
name: study-exercise-docs
description: Process already-defined interview-study exercises and solutions into the TestingTesting repository. Use when importing exercise.md or theory.md/exercise.md pairs, creating exercise and solution pages, or adding runnable practice scaffolding. Preserve the supplied task intent and constraints, derive matching solutions, retain a clear learning flow, and validate module schema and exercise IDs.
---

# Study Exercise Docs

## Reader-first quality bar

An exercise should be attemptable without guessing hidden rules. State the scenario, input/output or
observable behavior, constraints, and acceptance criteria. Its solution should first name the key
decision, then explain why it satisfies the constraints before presenting code or a final design.

Keep the solution traceable to the stated requirements. Do not smuggle in a new framework, infrastructure
component, or advanced extension just because it would exist in production.

Before calling an exercise complete, trace each acceptance criterion to a code path, a concrete explanation,
or a clearly labeled follow-up. An illustrative outline must say what it intentionally omits so the reader
does not mistake it for a runnable production sample.

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied theory and exercise source to EOF before editing.
3. Keep each supplied exercise's intent, order, constraints, and concepts tested. Do not broaden it into a new curriculum.
4. Place the exercise in the closest valid module. Create exactly one matching solution using the required shared kebab-case ID.
   For an existing interactive LLD module that already uses `design/`, put the explanatory answer in
   that design page and link the exercise to it instead; never create both `design/` and `solution/`.
5. Before finalising a substantial exercise or solution, use the repository `study-visuals` skill to decide
   whether one trace, state, or interaction visual would remove a real ambiguity. Do not add decorative
   visuals to an otherwise attemptable exercise.

## Source grounding and quality comparison

Treat the supplied exercise, relevant existing repository content, and available local books or PDFs as
authoritative for intent and scope. Verify language, framework, or protocol facts using primary online
documentation, then use an independent reputable reference to check ambiguity, expected depth, coverage,
and common traps. Do not import a reference solution or broaden the exercise to match it.

Write original scenarios, traces, diagrams, and explanations. Never copy prose, diagrams, exercises, or
solution structure from books, courses, PDFs, or websites. A quality solution makes its own key decision
traceable to the stated requirements; it need not match a reference's length or outline.

Keep exercises interview-specific. A framework detail belongs only when it explains the contract, failure
mode, or test boundary the candidate would need to reason about; omit setup guides and production options
that do not change the solution.

Before external research, abstract the exercise into a generic technical question. Never send company or
client names, internal project or service names, unpublished metrics, architecture, schemas, source code,
credentials, screenshots, or repository content to an external site or tool. Use neutral examples unless
the user explicitly authorizes identifiable context.

## Learning quality

- Ensure the topic doc introduces the mental model and traps before the exercise. Do not make readers discover missing theory while attempting the task.
- Make exercise statements concrete: inputs, required behavior, constraints, and acceptance criteria. Keep the solution readable enough to teach the decision, not merely reveal an answer.
- Use one short example or trace when it removes ambiguity. Do not add a diagram, abstraction, or
  extra exercise merely to make the module look more complete.
- Preserve useful learner misconceptions or hints from the source when they help a future reader avoid a common wrong turn.
- Add runnable Java playground code only when execution materially helps practice. Keep Java focused on the exercise, with no tutorial prose embedded in JavaDoc.
- Use an original diagram only when it clarifies an interaction or state change that the code alone hides.
  Give it one job, verify arrow direction and alternate outcomes against the exercise, and never copy
  screenshots from books, courses, PDFs, or web pages. When diagrams change, validate the built page;
  with a local preview, check Mermaid SVG rendering, browser errors, and horizontal overflow.

## Scored completion

After a substantial exercise or solution rewrite and rendered review, score attemptability, solution-to-
requirement traceability, interview-relevant reasoning, and communication/visual clarity out of 20. A module
is complete only when every applicable dimension is 20/20. For every lower score, name the precise missing
evidence, improve the exercise or solution, and add the narrow preventive rule to this skill or
`study-visuals` before continuing. Report the final scorecard and changed rule in the handoff.

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
