# Efficient Study-Content Execution

Use this policy for substantial public study-content work. It reduces repeated context and validation cost; it
does not remove a quality gate.

## Model routing

Use `gpt-5.6-terra` at low or medium reasoning for repository discovery, the complete initial source read,
source inventory, first-draft prose, mechanical edits, and routine validation-result collection. Escalate to
`gpt-5.6-sol` only for final factual/correctness review, an independent unfamiliar-reader critic pass, a genuine
technical contradiction, visual-audit findings, and final scope/commit review. Keep writer and critic passes
independent.

## One evidence ledger

Before editing, create one compact private evidence record for the module. It must contain the source-preservation
map, consulted references and exact facts verified, reader path, visual plan, validation results, and unresolved
findings. Reuse this record throughout the run. Read the complete source packet once; after that, inspect only
changed files and targeted line ranges unless new evidence makes another source read necessary.

## Output and delegation budget

Do not stream a whole large document, full generated file, full diff, or raw build log into the working context when
a targeted range or compact result proves the point. Have `gpt-5.6-terra` return a bounded source packet—candidate,
paths, preserved concepts, and gaps—rather than copied page text. Capture validation as command, pass/fail, and the
first relevant error location; inspect more only to diagnose that error. Do not poll an agent repeatedly: wait once
for a useful interval while doing local work, then continue from its compact result. Keep screenshots and evidence
private and report their outcome, not their raw payload.

## One-pass quality loop

After the complete draft, request one independent critic pass. If it finds material defects, make one focused patch
and request one re-audit. Do not spend another critic cycle on cosmetic preferences; a further cycle is justified
only by a material acceptance failure.

Validate in this order: cheap static checks (including `git diff --check` and any relevant schema check), homepage
generation, docs build, then one browser audit after all visual edits. Do not rerun an expensive build or browser
audit unless relevant page content or assets changed after its last success. Keep commentary to completed gates,
concrete blockers, and the final handoff.

When asked to process the next item, complete evidence, research, preservation, drafting, writer and critic review,
visual validation, required validation, backlog update, and requested scoped commit/push before stopping. A failed
command is a finding to diagnose and work around; stop only for a real authority or external-state blocker.
