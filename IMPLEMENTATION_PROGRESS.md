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
