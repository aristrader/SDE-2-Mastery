---
name: lld-study-docs
description: >-
  Process low-level object-design material into the TestingTesting repository. Use for LLD case studies,
  Java playgrounds, diagrams, study-plan links, and imported LLD notes.
---

# LLD Study Docs

Use the shared [efficient execution policy](../study-visuals/references/efficient_execution.md) for QG-4,
evidence, research, writer/critic separation, visual review, validation, and closure. Do not duplicate it here.

## Outcome

Teach one representative interaction from requirement and invariant to responsibilities, state changes, and the
boundary that protects correctness. A first-time reader must be able to explain why each non-obvious class exists,
where the invariant is enforced, and what changes for a follow-up.

- Treat runnable playground code as the truth for current behavior. Label unimplemented interview extensions.
- Existing `case_studies/**/playground/` Java and worksheets are user-owned: review and document them, but edit an
  exact file only with explicit permission.
- Keep scope at SDE2 Java-backend depth; do not build a framework or a full product.

## Workflow

1. Read active source material once and make the shared preservation map. Reuse or link nearby modules instead of
   creating parallel explanations.
2. Establish scoped requirements and invariants before classes. Walk one normal interaction, then derive state,
   responsibilities, collaborations, and the relevant concurrency/persistence boundary.
3. For a case study, retain the vague interviewer prompt, a conversational clarification discussion, and a final
   agreed exercise. The final exercise owns exact requirements and acceptance scenarios; all three stay
   solution-neutral.
4. Trace every agreed requirement and test scenario to current behavior, an explicitly documented gap, or a labeled
   follow-up. Never imply that a proposed design is already implemented.
5. After drafting, use `study-visuals` only if a class, sequence, or state visual answers an ownership, interaction,
   or lifecycle question better than prose.

## Design and study-plan rules

- One class has one clear responsibility. Add an interface or pattern only for an independently varying behavior;
  put locks and transactions at the shared-resource boundary.
- Preserve useful learner questions, misconceptions, analogies, examples, and follow-ups. Keep detailed lessons in
  Markdown, not JavaDoc. End study pages with short `## Quick recall` prompts.
- A copied entity/class-diagram worksheet begins with the complete final problem before candidate notes or diagrams.
- Link covered study-plan rows; use `Partial` only for a named gap, update `TopicIndex.md` for substantial topic
  changes, and clear processed temp files to their placeholders without deleting them.
