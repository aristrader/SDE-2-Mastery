---
name: lld-study-docs
description: Process low-level object-design material into the TestingTesting repository. Use when importing LLD transcripts or notes, creating or improving LLD case studies, adding Java playgrounds or class/sequence diagrams, or updating LLD study-plan rows. Preserve substantive learner reasoning, model responsibilities and invariants, use patterns only when requirements demand them, and validate navigation and syllabus links.
---

# LLD Study Docs

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
3. Inspect existing pattern, foundation, and case-study modules before editing. Improve or link existing material instead of creating a parallel solution.
4. Convert the source into: scoped requirements, invariants, one key interaction, the derived domain
   model and responsibilities, state changes, extensions, and interview traps.
5. Use a class, sequence, or state diagram only when it clarifies ownership, interaction, or lifecycle.

## Source grounding and quality comparison

For a substantial rewrite, start with the supplied exercise, existing repository documentation, runnable
code, learner notes, and any available local books or PDFs. They are the source of truth for the current
exercise. Verify Java language or library contracts against official online documentation, then use a
reputable LLD reference such as Hello Interview to compare coverage, explanation quality, and common
interview follow-ups. Do not retrofit its solution into this exercise.

Paraphrase every external idea in a new flow derived from the repository's requirements. Never copy prose,
class diagrams, sequence diagrams, examples, or heading structure. The quality check is whether a reader
can derive the model and its invariants from the stated problem, not whether the page resembles a reference
answer. Link a consulted source only when it is genuinely useful further reading.

## Required LLD depth

- Establish invariants before classes: uniqueness, lifecycle, allowed transitions, and concurrent-resource rules.
- Give each class one clear responsibility. Add an interface or pattern only when behavior actually varies.
- Place concurrency and transaction decisions at the shared-resource boundary.
- Include clarifying questions, a concise delivery order, and likely follow-up extensions.
- Structure LLD exercises as a vague interviewer prompt, a sample candidate clarification discussion, and
  a final agreed exercise. The final exercise owns the complete concrete requirements.
- When creating or copying an entity-identification/class-diagram worksheet under `playground/`, begin it
  with the complete final problem statement before any candidate notes or diagrams.
- Add runnable Java only when it gives real practice value; otherwise keep the module theory/design focused.
- Keep to SDE2 Java-backend interview depth. Do not turn a case study into a framework or a full product.

## Writing and visuals

- Build a readable learning path: problem and invariants first, then model, interactions, extensions, and interview traps.
- Use prose to connect the requirement to the model. Tables and diagrams support that reasoning; they
  must not replace it.
- Preserve useful learner questions, misconceptions, and analogies. Explain the correction in place rather than deleting valuable context.
- Use complete sentences and concrete examples. Avoid thin class lists with no responsibility reasoning and avoid source-transcript filler.
- Prefer an original class, sequence, or state diagram when it materially clarifies ownership or a lifecycle. Never copy book, course, PDF, or web screenshots. Put repo-owned image assets in the module `assets/` directory only when its schema permits them; otherwise use Mermaid.
- End study pages with short `## Quick recall` prompts that test design choices.

## Study-plan handling

- Preserve source discussion, misconceptions, and learner questions when reorganizing.
- Link covered Part rows to the destination docs. Use `Partial` only when a specific gap remains; mark `Done` only after substantial coverage.
- Update `TopicIndex.md` when Part topics are added, renamed, moved, split, or substantially reworded.
- Clear processed temp files to their placeholder comments using absolute paths; do not delete them.

## Validation

1. Run `node scripts/generate-homepage.js` after site-page changes.
2. Run relevant Java verification when runnable code changes, then run `git diff --check`.
3. Preserve unrelated worktree changes and do not commit unless asked.
4. Report docs updated, Part rows touched, partial gaps, and source position.
