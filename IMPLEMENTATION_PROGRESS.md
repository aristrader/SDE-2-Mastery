# Site Redesign — Implementation Progress

Branch: `feat/site-redesign`. Plan: `docs/superpowers/plans/2026-06-18-site-redesign.md`.
Each task committed separately, so `git log feat/site-redesign` is the full trace.
Event hook also writes to `.claude/events.jsonl` (active after a `/hooks` reload or next session).

## Status — ALL TASKS COMPLETE ✅
- [x] Branch created off main
- [x] Event-logging hook configured (`.claude/settings.local.json`)
- [x] T1: deps, gitignore, dev script
- [x] T2-6: backend sidecar (paths, run, save, agent, index) + node:test — 17 tests pass
- [x] T7: vitepress config (mermaid, proxy, nav) + removed old run components
- [x] T8: file discovery lib + test (3 tests pass)
- [x] T9-12: frontend (CodeEditor, AgentChat, Playground, Layout) — build passes clean
- [x] T13: homepage + 6 domain hubs
- [x] T14: build + integration verify — `npm test` green, `docs:build` clean, playwright smoke PASSED, `/api/run` end-to-end through proxy returns real output

## How to run
```
npm run dev      # starts backend (5174) + vitepress (5173); open http://localhost:5173
npm test         # backend unit suite (node:test)
npm run docs:build   # production build
node playwright_smoke.js   # integration smoke (needs npm run dev running)
```

## Verified working
- Backend: paths guard, mvn run, file save round-trip, agent arg-building/stream parsing, http router.
- Site builds clean (mermaid renders, no glob deprecation warnings).
- Singleton page shows Read/Playground tab; Playground lists all 5 singleton files with a Run button per main-bearing class (multi-run confirmed).
- Doc-only pages (study plan README) show no tab.
- `/api/run` compiles + executes through the vite proxy and returns stdout.

## Session 2 — autonomous review & improvement pass (2026-06-18)
Verified with evidence (screenshots/measurements/tests), each committed separately.
- **Margins (verified real bug):** earlier "fix" lost a CSS specificity battle (prose stayed
  688px). Real cap was `.content`; fixed with `!important`. Hub 720→1056px; doc ~784 (TOC-bound).
- **AI → `agy` (default):** discovered the local `agy` agent CLI (Gemini-backed, agentic, cheap).
  Added as 3rd provider, made default; fixed an stdin-pipe hang (agy waited on stdin → now closed).
- **Strict code review (subagent):** fixed loopback bind (was 0.0.0.0 + skip-perms = LAN RCE),
  AgentChat non-200 handling, illegal keep-alive header + post-header error guard, bounded
  session/history maps, spawn-error resolve, stale comment. (fqcn regex + path guard confirmed safe.)
- **Site bug-hunt (subagent):** clean except Playground absent on pages whose code is in
  subfolders → fixed with nearest-markdown-ancestor ownership (Prototype now shows simple/+polymorphic/).
- **Theme:** brand was defaulting to green (pre-1.0 var names); fixed to indigo→cyan, then a
  full readability pass (typography, zebra tables, code/blockquote, deep-navy dark).
- **Mobile:** fixed a table-overflow regression I introduced (kept tables scroll-on-narrow).
- **UX:** agent elapsed timer + Stop, exercise badges (`*Practice.java`), AI quick-action chips,
  run elapsed timer.
- **Hygiene:** untracked 521 vitepress cache/dist files; added README + next-phase roadmap.
- **Final state:** 24 backend + 7 lib tests pass, `docs:build` clean, playwright smoke PASSED.

## Notes / deviations from plan (discovered during execution)
- **fileDiscovery is `.mjs` (ESM)**, not `.js` — package.json is `type: commonjs`, so an
  ESM file keeps both Vite and `node --test` happy without interop hacks.
- **Glob is relative, not `/src/...` absolute** — the Vite root here doesn't map `/src` to
  repo root (this was the original Gemini "glob fix" pain). Playground uses `../../../../src/...`,
  Layout uses `../../../src/...` (different depths), with `{ query: '?raw', import: 'default' }`.
- **Tabs moved into the DefaultTheme `#doc-top` slot** instead of a full-width bar above the
  layout. The full-width bar sat *behind* VitePress's fixed sidebar (buttons unclickable on
  desktop). Slot placement puts tabs in the content column; play mode hides `.vp-doc` via an
  `html.pg-play` class in custom.css so the Playground gets the full width.

## Not done here (left for you to try live — uses your Claude auth, costs tokens)
- Actually chatting with the **AI agent** in the browser. The endpoint + UI are wired and the
  arg-building/stream-parsing are unit-tested, but I didn't spawn a live `claude` session to
  avoid nesting a costly agent run. Open any code page → **✦ Ask AI** to try it.
- In-browser **edit → Save → Run** round-trip (save endpoint is unit-tested; try it live).
