---
name: agy-study-drafts
description: Draft or repair one public TestingTesting study page with Antigravity while minimizing Codex context and Gemini turn cost. Use for public Markdown study prose; not for user-owned Java/playground or private content.
---

# Agy Study Drafts

Use Agy as the writer and keep Codex to one final local QG-4 gate check.

1. Launch from the repository root (the permission file is not inherited from a module directory), give Agy one public module directory and exact target path, and use a fresh sandboxed PTY session with `--model gemini-3.8-flash-low --disable-slash-commands --effort low --output-format json`. Poll that same session instead of resuming conversations. It may edit only that target. The repository `.gemini/settings.json` grants only routine read-only inspection commands.
2. Ask for one compact JSON report: preservation result, authoritative URLs and verified facts, two independent learning/interview URLs, unresolved findings, and commands. Never send private material, user-owned code, or broad repository context.
3. Default to micro-patches: preserve every existing line and add one bounded paragraph, table row, or recall item from a supplied verified fact packet. Do not do open-ended web research or a structural rewrite in an editing turn. A structural rewrite needs an explicit source map and a reason; do not impose a universal template.
4. Use a fresh Agy Flash diff critic only for material changes. Give it the writer's checklist and changed diff; it reports material preservation/correctness issues, not style preferences. A focused repair gets one re-audit.
5. Codex checks only the critic result, changed diff, and local gates. Record Agy `duration_seconds` and `usage.total_tokens`. A micro-patch should stay near 30k tokens and 30 seconds; otherwise shrink it further. Do not retry a broad packet.
6. Finish with the normal static, navigation, render, backlog, and scoped commit/push gates. Agy does not replace those local validations.
