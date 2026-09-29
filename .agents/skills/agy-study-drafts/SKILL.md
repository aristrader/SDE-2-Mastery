---
name: agy-study-drafts
description: Draft or repair one public TestingTesting study page with Antigravity while minimizing Codex context and Gemini turn cost. Use for public Markdown study prose; not for user-owned Java/playground or private content.
---

# Agy Study Drafts

Use Agy as the writer and keep Codex to one final local QG-4 gate check.

1. Snapshot `git diff HEAD --name-only` first. Preflight the exact target from its module directory with a read-only H1 probe; if Agy cannot prove that identity, use an output-only draft and apply only an exact-path patch locally. Otherwise give Agy one public module directory and exact absolute target path in a fresh sandboxed PTY session with `--model gemini-3.7-flash-low --disable-slash-commands --output-format json`. Poll that same session instead of resuming conversations. The repository `.gemini/settings.json` grants only routine read-only inspection commands. Compare the post-edit path list to the snapshot; inspect and stop on a new unexpected path, or revert it only when its diff is unambiguously Agy-created.
2. Ask for one compact JSON report: preservation result, authoritative URLs and verified facts, two independent learning/interview URLs, unresolved findings, and commands. Never send private material, user-owned code, or broad repository context.
3. Use micro-patches only after a source audit says the page is already sound. For a structural gap, build a source-preservation map and have Agy draft one bounded section at a time from a verified fact packet; never trade away examples, recall, or reader flow for speed.
4. Use a fresh Agy Flash critic after material changes. Give it the preservation map and changed diff; it reports material preservation/correctness issues, not style preferences. A score below 20 must name the exact defect; otherwise require a corrected score before any edit. A focused repair gets one re-audit.
5. Codex verifies the critic evidence, source map, changed sections, and local gates before awarding QG-4. Record Agy `duration_seconds` and `usage.total_tokens`. A micro-patch should stay near 30k tokens and 30 seconds; otherwise shrink it further. Do not retry a broad packet.
6. Finish with the normal static, navigation, render, backlog, and scoped commit/push gates. Agy does not replace those local validations.
