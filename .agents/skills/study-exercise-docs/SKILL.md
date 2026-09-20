---
name: study-exercise-docs
description: Process already-defined interview-study exercises and solutions into the TestingTesting repository. Use when importing exercise.md or theory.md/exercise.md pairs, creating exercise and solution pages, or adding runnable practice scaffolding. Preserve the supplied task intent and constraints, derive matching solutions, retain a clear learning flow, and validate module schema and exercise IDs.
---

# Study Exercise Docs

For substantial public study-content work, follow the shared
[efficient execution policy](../study-visuals/references/efficient_execution.md).

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

## Learning quality

- The topic page must teach the needed mental model first. Keep task, constraints, and acceptance criteria concrete;
  add code, visuals, or extra examples only when they remove a real practice ambiguity.

## Module and study-plan handling

- Follow the module schema and the `AGENTS.md` study-plan, temp-import, validation, and commit rules.
