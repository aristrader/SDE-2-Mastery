# SDE-2 Mastery — local study site

An interactive, **local-only** VitePress site over this repo's study material (`.md`) and its
Java implementations (`.java`): read the theory, run the code, and chat with an AI agent about
any page. Not deployed — runs on your machine; GitHub is backup only.

## Run it

```bash
npm install            # first time
npm run dev            # starts the site (http://localhost:5173) + backend (127.0.0.1:5174)
```

`npm run dev` launches two processes via `concurrently`:
- **VitePress** — the docs frontend (port 5173).
- **Backend sidecar** (`server/`) — the only piece that touches the filesystem / runs processes;
  bound to loopback only. Vite proxies `/api/*` to it.

Other scripts: `npm test` (backend unit tests), `npm run docs:build` (production build),
`node playwright_smoke.js` (integration smoke — needs `npm run dev` running).

## Features

- **Read / Playground tabs** — appear on any page whose folder (or a subfolder) contains `.java`.
  - *Read*: the doc, with rendered Mermaid and styled tables.
  - *Playground*: a file list of the folder's Java (grouped by subfolder; `*Practice.java`
    flagged as **exercise**), a CodeMirror editor, **Save**, and a **Run** button on every
    class with a `main()` (folders with several runnable demos get one Run per file).
- **✦ Ask AI** (floating, every page) — a streaming chat scoped to the current page. Quick-action
  chips (Explain / Quiz / Practice) get you started.

## AI providers

Set in `.env` (copy from `.env.example`). `AI_PROVIDER` selects the backend:

| Provider | Agentic? | Notes |
| --- | --- | --- |
| `agy` (default) | yes | Local `agy` CLI (Gemini-backed); can edit/run files; cheap; uses agy's own auth. Optional `AGY_MODEL`. |
| `gemini` | no | Gemini API (conversational only). Needs `GEMINI_API_KEY`, optional `GEMINI_MODEL`. |
| `claude` | yes | Claude Code CLI agent. |

## Where things live

- `server/` — `index.js` (router), `run.js` (mvn), `save.js`, `paths.js`, `agentAgy.js` /
  `agentGemini.js` / `agentClaude.js` (providers).
- `docs/.vitepress/` — `config.mjs`, `theme/Layout.vue`, `theme/components/{Playground,CodeEditor,AgentChat}.vue`,
  `theme/lib/fileDiscovery.mjs`, `theme/custom.css`.
- Study content — `src/main/java/org/example/backend_fundamentals/` (the site's `srcDir`).
- Design/plans — `docs/superpowers/specs/` and `docs/superpowers/plans/` (incl. the
  next-phase roadmap).

## Security note

The sidecar binds `127.0.0.1` only. `/api/run` + `/api/save` together are arbitrary local
code execution by design, and the agentic providers run with permissions auto-accepted —
keep it local (never `0.0.0.0` or behind a tunnel).
