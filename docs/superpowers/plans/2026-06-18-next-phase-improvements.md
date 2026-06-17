# SDE-2 Mastery Site — Next-Phase Improvement Roadmap

Status as of 2026-06-18 (branch `feat/site-redesign`). The core redesign is done and
verified: backend sidecar (run/save/agent, 24 tests), Read/Playground tabs, folder-aware
+ subfolder-aware Playground, multi-run, agy/gemini/claude providers, hub navigation,
mermaid, readability theme, loopback security. This doc prioritizes what's next.

## Tier 1 — high value, low/medium effort

1. **Agent latency UX.** `agy` has ~15–18s agent startup per turn. Add a clear "Agent is
   working… (Ns)" elapsed indicator + a cancel button, and stream partial output as it
   arrives. Optionally default `AGY_MODEL` to the fastest Gemini Flash tier for snappier Q&A.
   *(Files: AgentChat.vue, .env.example.)*

2. **Exercise vs example distinction.** `*Practice.java` + `Exercises.md` are exercises;
   everything else is an example. Badge exercise files in the Playground file list, and
   surface the folder's `Exercises.md` as a panel/link in Playground mode so practice and
   reference are visible together. *(fileDiscovery.mjs: tag `kind: 'exercise'|'example'`;
   Playground.vue: badge + exercises link.)*

3. **Run UX.** Show a spinner + elapsed timer during `mvn` (first compile is slow);
   stream stdout incrementally instead of one buffered dump; add a "Stop" for long runs;
   remember the last-run file per page. *(run.js could switch to spawn+stream; Playground.vue.)*

4. **Mobile / narrow-viewport audit.** Verify tabs, the Playground grid (collapses at
   960px), the AI drawer (90vw), and tables scroll cleanly on phones; fix any overflow.

## Tier 2 — medium value

5. **Agent can act, not just chat (agy).** Since `agy` is agentic, add explicit affordances:
   quick-action buttons in the chat ("Quiz me on this page", "Explain the selected file",
   "Add a comment explaining X") and reload the open editor file after the agent edits it
   (detect file changes post-turn and refresh `buffer`).

6. **Per-page agent session persistence.** Sessions are in-memory and reset on restart.
   Optionally persist the page→sessionId map (and agy `--conversation` ids) so a chat
   survives a dev-server restart.

7. **Search scope polish.** Local search indexes everything incl. the 32 Part planning
   docs; consider grouping results by domain or boosting concept docs over plan docs.

8. **Doc-page width control.** Prose is ~784px with the TOC shown. Add an optional
   "focus/full-width" toggle per page (hide TOC) for users who want the wider measure.

## Tier 3 — nice to have

9. **Diff view for agent edits.** When the agent edits a file, show a before/after diff in
   the editor with accept/discard, instead of silently reloading.

10. **In-browser file creation.** Let the Playground create a new `*Practice.java` from a
    template (pairs with the exercise workflow).

11. **Output history / multiple run panes.** Keep a short history of run outputs per page.

12. **Centralize the Java glob.** Layout.vue and Playground.vue each eagerly glob the whole
    Java tree (bundle ships it twice). Centralize into one shared module if bundle size matters.

13. **Tests for the Vue layer.** Add component tests (or more Playwright integration specs)
    for the Playground/Layout so regressions like the 7px run-button or the 688px margin are
    caught automatically.

## Known constraints (by design, not bugs)
- Local-only; the sidecar binds 127.0.0.1 and runs an agent with skip-permissions —
  never expose it (no `0.0.0.0`, no tunnel).
- `/api/save` + `/api/run` together are arbitrary local code execution by design.
- Gemini provider is conversational only (no file edits); agy/claude are agentic.

## Suggested order
Tier 1 (1→4) next session — they most directly affect daily study UX — then Tier 2 as time allows.
