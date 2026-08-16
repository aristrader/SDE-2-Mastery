---
name: lld-study-docs
description: Process low-level object-design material into the TestingTesting repository. Use when importing LLD transcripts or notes, creating or improving LLD case studies, adding Java playgrounds or class/sequence diagrams, or updating LLD study-plan rows. Preserve substantive learner reasoning, model responsibilities and invariants, use patterns only when requirements demand them, and validate navigation and syllabus links.
---

# LLD Study Docs

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied source to EOF. Handle long single-line transcripts correctly.
3. Inspect existing pattern, foundation, and case-study modules before editing. Improve or link existing material instead of creating a parallel solution.
4. Convert the source into: scoped requirements, invariants, domain model, ownership/responsibilities, key interactions, state changes, extensions, and interview traps.
5. Use a class, sequence, or state diagram only when it clarifies ownership, interaction, or lifecycle.

## Required LLD depth

- Establish invariants before classes: uniqueness, lifecycle, allowed transitions, and concurrent-resource rules.
- Give each class one clear responsibility. Add an interface or pattern only when behavior actually varies.
- Place concurrency and transaction decisions at the shared-resource boundary.
- Include clarifying questions, a concise delivery order, and likely follow-up extensions.
- Add runnable Java only when it gives real practice value; otherwise keep the module theory/design focused.
- Keep to SDE2 Java-backend interview depth. Do not turn a case study into a framework or a full product.

## Writing and visuals

- Build a readable learning path: problem and invariants first, then model, interactions, extensions, and interview traps.
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
