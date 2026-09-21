# Public Study Quality and Efficient Execution

Use this compact contract for substantial public study-content work. It saves context; it never removes a quality gate.

## Intake and research

- Use `gpt-5.6-terra` at low/medium reasoning for discovery, one complete source read, source inventory, drafting, mechanical edits, and routine validation. Use `gpt-5.6-sol` only for factual contradictions, unfamiliar-reader criticism, visual findings, and final scope/commit review. For a compact, related batch, default to one Terra writer and one Sol critic over the whole batch with one ledger; split only when one bounded packet cannot cover the modules. Parallelism reduces elapsed time, not token use.
- Before editing, create one private evidence ledger: source-preservation map, exact facts/references, reader path, visual plan, validation results, and unresolved findings. Reuse it; after intake, read only changed files or exact line ranges unless new evidence requires otherwise.
- Treat a coherent cluster of up to five small modules as one run when it shares a reader path. Build one fact packet (authoritative mechanisms plus two independent coverage references) and map every reused fact to each current claim; split only when that map or critic review no longer fits a bounded packet. Triage existing pages first: preserve a sound page with a targeted repair rather than rewriting it merely for uniformity.
- For a compact theory cluster, keep the ledger to the source map, claim-to-fact map, reader path, visual decision, and findings—normally under 900 words. Give a delegated critic only that ledger and changed paths with no conversation fork; it verifies the rendered prose, not a second discovery pass. This cap does not apply to a complex HLD/LLD case study.
- Preserve every distinct source claim, example, misconception, diagram, estimate, failure mode, requirement, and follow-up in the edited page, a linked destination, or an explicit scope cut with a reason.
- Research with generic external queries only. Use primary documentation for factual mechanisms and independent, accessible learning/interview references for coverage. Reuse a verified fact packet for closely related modules only when the current ledger maps each fact to its current claim. Never copy prose, examples, diagrams, code, or page shape.

## Teaching and review

- Start with the engineering problem, then mental model, mechanism, concrete consequence, and boundary. Define a term before relying on it. Explain a deep dive as problem → naive failure → mechanism → trade-off → recovery.
- Use only an original visual that answers one stated reader question; retain the equivalent explanation in prose. Check its arrows, labels, reading-column fit, browser errors, and overflow after it renders.
- Run a writer pass against the source map, then one independent unfamiliar-reader critic pass. The critic reuses the ledger's fact packet and reports only scored dimensions plus material deviations. Every applicable dimension must be 20/20; fix material defects in one focused patch and request one re-audit. Do not repeat a critic pass for cosmetic preferences.

## Efficient execution and closure

- Do not stream whole documents, generated files, full diffs, or raw logs when a targeted range or command result suffices. Ask Terra for a bounded packet—paths, preserved concepts, gaps—not copied prose. Do not poll agents; do local work and wait once for a useful result. Keep screenshots private and report their outcome only.
- In a parallel batch, agents own distinct modules and do not run homepage generation, docs dev, docs build, or browser checks. After every draft and critic repair is stable, one coordinator runs static/schema checks, homepage generation, one production build, then one route audit per changed page. Repeat an expensive check only when relevant content or assets changed after its success.
- Close, validate, and commit a stable cluster—not each folder—unless the user explicitly requests a smaller checkpoint. A clean critic may attest to every module in the cluster in one scorecard; it must not manufacture separate review prose for each page.
- Keep research collection bounded: one focused search packet per cluster, then open only the authoritative source and the two independent sources selected for the ledger. Stop when every current claim has evidence; more search results are not more validation.
- When asked to process the next item, finish evidence, research, preservation, writing, reviews, visuals, validation, backlog record, and requested scoped commit/push. A failed command is a finding to diagnose; stop only for an authority or external-state blocker. Keep commentary to completed gates, concrete blockers, and handoff.
