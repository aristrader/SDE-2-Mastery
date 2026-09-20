# Public Study Quality and Efficient Execution

Use this compact contract for substantial public study-content work. It saves context; it never removes a quality gate.

## Intake and research

- Use `gpt-5.6-terra` at low/medium reasoning for discovery, one complete source read, source inventory, drafting, mechanical edits, and routine validation. Use `gpt-5.6-sol` only for factual contradictions, unfamiliar-reader criticism, visual findings, and final scope/commit review.
- Before editing, create one private evidence ledger: source-preservation map, exact facts/references, reader path, visual plan, validation results, and unresolved findings. Reuse it; after intake, read only changed files or exact line ranges unless new evidence requires otherwise.
- Preserve every distinct source claim, example, misconception, diagram, estimate, failure mode, requirement, and follow-up in the edited page, a linked destination, or an explicit scope cut with a reason.
- Research with generic external queries only. Use primary documentation for factual mechanisms and independent, accessible learning/interview references for coverage. Never copy prose, examples, diagrams, code, or page shape.

## Teaching and review

- Start with the engineering problem, then mental model, mechanism, concrete consequence, and boundary. Define a term before relying on it. Explain a deep dive as problem → naive failure → mechanism → trade-off → recovery.
- Use only an original visual that answers one stated reader question; retain the equivalent explanation in prose. Check its arrows, labels, reading-column fit, browser errors, and overflow after it renders.
- Run a writer pass against the source map, then one independent unfamiliar-reader critic pass. Score applicable dimensions out of 20; every dimension must be 20/20. Fix material defects in one focused patch and request one re-audit. Do not repeat a critic pass for cosmetic preferences.

## Efficient execution and closure

- Do not stream whole documents, generated files, full diffs, or raw logs when a targeted range or command result suffices. Ask Terra for a bounded packet—paths, preserved concepts, gaps—not copied prose. Do not poll agents; do local work and wait once for a useful result. Keep screenshots private and report their outcome only.
- Validate once, in order: static/schema checks, homepage generation, docs build, then one browser audit after all visual edits. Repeat an expensive check only when relevant content or assets changed after its success.
- When asked to process the next item, finish evidence, research, preservation, writing, reviews, visuals, validation, backlog record, and requested scoped commit/push. A failed command is a finding to diagnose; stop only for an authority or external-state blocker. Keep commentary to completed gates, concrete blockers, and handoff.
