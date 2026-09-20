---
name: lld-study-docs
description: >-
  Process low-level object-design material into the TestingTesting repository. Use when importing LLD
  transcripts or notes, creating or improving LLD case studies, adding Java playgrounds or class/sequence
  diagrams, or updating LLD study-plan rows. Preserve substantive learner reasoning, model responsibilities
  and invariants, use patterns only when requirements demand them, and validate navigation and syllabus links.
---

# LLD Study Docs

For substantial public study-content work, follow the shared
[efficient execution policy](../study-visuals/references/efficient_execution.md).

## Reader-first quality bar

Anchor a substantial case study in one representative use case. Lead the reader from the requirement
and its invariant to the interaction that preserves it; do not start with a class list or pattern.

Use this order unless the source genuinely requires another one:

1. Scope the problem and name the important rules that must remain true.
2. Walk through one ordinary interaction.
3. Derive state, responsibilities, and collaborations from that interaction.
4. Explain the boundary where correctness, concurrency, or persistence must be protected.
5. Add extensions only after the core model works, including their trade-off.

Every non-obvious class needs a purpose in the flow. Every pattern needs a varying behavior or
concrete change it isolates. A first-time reader should be able to explain why each class exists and
where the invariant is enforced.

When runnable code already exists, treat it as the source of truth for current behavior. Do not describe
an interview follow-up as implemented; label it as an extension and state what boundary would change.

Treat existing `case_studies/**/playground/` Java code and design worksheets as user-owned active work.
Review, explain, and refer to them, but do not edit them unless the user explicitly authorizes the exact
file to change. Improve surrounding learning documentation without rewriting the user's implementation.

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied source to EOF. Handle long single-line transcripts correctly.
3. Inspect existing pattern, foundation, and case-study modules before editing. Improve or link existing
   material instead of creating a parallel solution.
4. Convert the source into: scoped requirements, invariants, one key interaction, the derived domain
   model and responsibilities, state changes, extensions, and interview traps.
5. After drafting a substantial page, use the repository `study-visuals` skill to decide whether an existing
   visual is sufficient or whether a new class, sequence, or state diagram is warranted. Use a diagram only
   when it clarifies ownership, interaction, or lifecycle.
6. Before calling a case-study rewrite complete, trace every agreed requirement and test scenario to one of:
   implemented behavior, an explicitly documented current gap, or a labeled follow-up. Do not let the
   design prose imply that an extension already exists in the playground.

## Shared quality

The shared contract owns research, preservation, reader verification, scoring, and validation. The runnable model
and agreed final exercise remain the source of truth; never retrofit a reference solution into them.

## Required LLD depth

- Establish invariants before classes: uniqueness, lifecycle, allowed transitions, and concurrent-resource
  rules.
- Clarify scope before drawing classes. When two requirements vary independently, model their composition
  instead of multiplying subclasses; do not introduce a pattern merely because its name sounds relevant.
- Give each class one clear responsibility. Add an interface or pattern only when behavior actually varies.
- Place concurrency and transaction decisions at the shared-resource boundary.
- Include clarifying questions, a concise delivery order, and likely follow-up extensions.
- Make sample clarification discussions read as an interview conversation: question, agreed answer, and the
  design decision that answer changes. A checklist is useful only after the conversational flow is clear.
- Structure LLD exercises as a vague interviewer prompt, a sample candidate clarification discussion, and
  a final agreed exercise. The final exercise owns the complete concrete requirements.
- When creating or copying an entity-identification/class-diagram worksheet under `playground/`, begin it
  with the complete final problem statement before any candidate notes or diagrams.
- Add runnable Java only when it gives real practice value; otherwise keep the module theory/design focused.
- Keep to SDE2 Java-backend interview depth. Do not turn a case study into a framework or a full product.

## Writing and visuals

- Build a readable learning path: problem and invariants first, then model, interactions, extensions, and
  interview traps.
- Use prose to connect the requirement to the model. Tables and diagrams support that reasoning; they
  must not replace it.
- Preserve useful learner questions, misconceptions, and analogies. Explain the correction in place rather
  than deleting valuable context.
- Use complete sentences and concrete examples. Avoid thin class lists with no responsibility reasoning and
  avoid source-transcript filler.
- Prefer an original class, sequence, or state diagram when it materially clarifies ownership or a
  lifecycle. Never copy book, course, PDF, or web screenshots. Put repo-owned image assets in the module
  `assets/` directory only when its schema permits them; otherwise use Mermaid.
- Give each diagram one job: use a sequence diagram for who invokes whom in one path, a class diagram for
  static ownership/variation, and a state diagram for a lifecycle with meaningful transitions. Do not add a
  diagram that merely repeats prose or another diagram.
- Audit every diagram against the implementation or agreed model: each arrow must have the correct caller,
  receiver, direction, and outcome; each class relationship must exist; and every alternate/failure path
  must be either implemented or visibly labeled as a follow-up. Avoid placeholder participants such as
  “delivery” when the actual actor boundary matters to the reasoning.
- Keep normal and failure outcomes visually distinct. Prefer short labels and split a crowded lifecycle
  diagram before relying on crossing arrows or one ambiguous terminal state.
- When diagrams change, validate the built page as well as Markdown. Run the docs build; when a local
  preview is available, verify each Mermaid block becomes one SVG without browser console errors and remains
  readable at a normal desktop viewport.
- End study pages with short `## Quick recall` prompts that test design choices.

## Study-plan handling

- Preserve source discussion, misconceptions, and learner questions when reorganizing.
- Link covered Part rows to the destination docs. Use `Partial` only when a specific gap remains; mark
  `Done` only after substantial coverage.
- Update `TopicIndex.md` when Part topics are added, renamed, moved, split, or substantially reworded.
- Clear processed temp files to their placeholder comments using absolute paths; do not delete them.
