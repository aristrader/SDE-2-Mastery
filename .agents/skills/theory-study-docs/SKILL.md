---
name: theory-study-docs
description: Process non-HLD and non-LLD interview-study notes into the TestingTesting repository. Use when importing theory transcripts, book notes, temp.md content, or concept explanations for Java, backend, databases, networking, security, or distributed systems. Preserve substantive learner reasoning, create readable topic documentation, use original visuals only when helpful, and update linked study-plan rows.
---

# Theory Study Docs

For substantial public study-content work, follow the shared
[efficient execution policy](../study-visuals/references/efficient_execution.md).

## Workflow

1. Read the repository `AGENTS.md`, the study-plan `README.md`, and `reference/DocCreationStandard.md`.
2. Read every supplied source to EOF. Handle long single-line transcripts correctly.
3. Find the closest existing topic document and related Part row. Merge into it when the concepts belong together; create a new theory module only for a genuinely distinct topic.
4. Preserve substantive learner questions, misconceptions, analogies, and flow reasoning. Remove only source filler, repeated statements, and details outside interview scope.
5. Before calling a rewrite complete, trace the central engineering question through the mechanism, its
   observable consequence, and its boundary or exception. A rule without one of those steps is either a
   glossary entry or an unsupported claim.
6. After drafting a substantial page, use the repository `study-visuals` skill to decide whether an existing
   visual is sufficient or whether a new mechanism, decision, or conceptual visual is warranted.

## Writing standard

- Apply the shared contract; end study pages with short interview-focused `## Quick recall` Q&A.

## Study-plan handling

- Link covered Part rows to the destination docs. Use `Partial` only when a concrete gap remains; mark `Done` only after substantial coverage.
- Update `TopicIndex.md` when Part topics are added, renamed, moved, split, or substantially reworded.
- Clear processed temp files to their placeholder comments using absolute paths; do not delete them.
