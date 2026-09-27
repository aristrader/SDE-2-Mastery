---
name: agy-study-drafts
description: Draft or repair one public TestingTesting study page with Antigravity while minimizing Codex context and Gemini turn cost. Use for public Markdown study prose; not for user-owned Java/playground or private content.
---

# Agy Study Drafts

Use Agy as the writer and keep Codex to one final local QG-4 check.

1. Give Agy one public module directory and one exact target path. Use a fresh session with `--new-project --disable-slash-commands --sandbox --effort low --mode accept-edits --output-format json`; it may edit only that target.
2. Ask for one compact JSON report: preservation result, authoritative URLs and verified facts, two independent learning/interview URLs, unresolved findings, and commands. Never send private material, user-owned code, or broad repository context.
3. Ask Agy to read the target and directly adjacent practice pages once, research generic questions, and make one complete draft. Do not ask it to echo the draft or a repository-wide summary.
4. Codex inspects only the changed diff and runs one final unfamiliar-reader/factual QG-4 check. Send any material corrections as one fresh, focused Agy repair prompt—not a growing conversation. Cosmetic changes do not get another pass.
5. Record Agy `duration_seconds` and `usage.total_tokens`. If a small page exceeds roughly 25k total tokens or one minute, narrow the next prompt or use local targeted repair; do not retry the same large packet.
6. Finish with the normal static, navigation, render, backlog, and scoped commit/push gates. Agy does not replace those local validations.
