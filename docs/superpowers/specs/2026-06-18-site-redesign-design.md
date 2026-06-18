# SDE-2 Mastery Site — Redesign Design

> Supersedes `2026-06-17-documentation-site-design.md`. That spec described the
> Gemini-built version (multi-sidebar + a single `<CodePlayground>`). This redesign
> reworks navigation, the code experience, and adds a per-page AI agent.

## 1. Context & Goals

The site renders the repo's `.md` study material (160 docs) alongside its `.java`
implementations (132 files) using VitePress. It is **local-only** — run via
`npm run dev` on the author's machine; GitHub is backup, never a deployment target.
That removes all hosting/security constraints and lets the site shell out to the
JDK and the `claude` CLI directly.

Three goals:

1. **Readable, extensible navigation.** Today the sidebar is a 100%-auto-generated
   dump over a deep tree, and the homepage is placeholder copy. Reading should be
   clean and the structure should absorb new topics without manual wiring.
2. **Java files as runnable examples / fillable exercises.** View, edit, and run any
   `.java` in a folder, with correct handling of folders that have **multiple**
   interconnected main classes.
3. **Per-page AI agent.** On any page, talk to a Claude Code agent scoped to this
   repo that can explain, quiz, discuss, and edit files / run code on request.

### Problems in the current (Gemini) implementation — explicitly fixed here

- `CodePlayground.vue` globs the **entire** Java tree and never filters by page, so
  every page shows all 132 files. → Fixed via folder-aware filtering.
- It dumps raw code into a `<pre>`, bypassing syntax highlighting. → Fixed with a real editor.
- It assumes one runnable file per folder. → Fixed with content-based main detection + a run-target selector.
- `/api/run-java` is a dev-server middleware buried in `config.mjs`. → Moved to a dedicated backend.
- 23 docs use Mermaid, which VitePress does not render without a plugin. → Plugin added.
- Homepage is placeholder text. → Replaced with a real hub.

## 2. Architecture

Two processes, one command.

```
npm run dev  (concurrently)
├── vitepress dev docs        # frontend reader, port 5173
│     └── proxies /api/* ─────────────┐
└── node server/index.js      # backend sidecar, port 5174
      ├── POST /api/run        # mvn compile + exec:java
      ├── POST /api/save       # write edited .java back to disk
      └── POST /api/agent      # streamed (text/event-stream) claude CLI output
```

- **Frontend**: VitePress + a custom theme layer. Owns rendering, navigation, the
  Read/Playground tabs, the editor, and the chat UI.
- **Backend sidecar** (`server/`, plain Node `http`, no framework needed): the only
  component that touches the filesystem and spawns child processes. Long-lived and
  stateful (holds agent sessions), which is why it is separate from the docs build
  config. Vite's `server.proxy` forwards `/api/*` to it, so the browser sees one origin.

Rationale for the split: the agent endpoint streams and is stateful; run/save are
filesystem mutations. Keeping all of that in one small, testable server — instead of
inside `config.mjs` — gives each piece one clear job.

## 3. Navigation & Reading

- **Top nav = domains** (Study Plan, Java & JVM, Spring, System Design, Design
  Patterns, Networking, Databases).
- **Hub landing pages**: each domain's entry route is a hub page with `sidebar: false`
  — a card grid linking into topics, ordered by learning path. Replaces sidebar bloat
  with an intentional index.
- **Contextual sidebar**: once inside a domain, a sidebar scoped to that domain
  appears (auto-generated via `vitepress-sidebar`, tuned with `collapseDepth: 2`,
  `capitalizeFirst`, and frontmatter `order` for curation — not raw alphabetical noise).
- **Mermaid**: add `vitepress-plugin-mermaid` (+ `mermaid`) so the 23 diagram-bearing
  docs render.
- **Homepage**: real hub — what the site is, the domain cards, current Sprint pointer.
- **Reading polish**: keep the existing Inter font / brand-color `custom.css`; ensure
  inline code uses VitePress Shiki highlighting (already default).

## 4. Per-Page Layout (Layout C)

A custom VitePress `Layout` wraps `DefaultTheme.Layout` and adds a `Read ↔ Playground`
tab bar. The tab bar renders **only when the current page's folder contains ≥1 `.java`**;
pure doc pages render the default theme unchanged. Active tab is stored in the URL hash
(`#playground`) so it is linkable and survives reload.

- **Read tab** — default theme: contextual sidebar, TOC, prose, inline highlighted
  code, rendered Mermaid. The AI is reachable here as a **slide-out drawer** from the right.
- **Playground tab** — full-width work area:
  - left: file list of all `.java` in the folder, each main-bearing file showing a Run button;
  - center: CodeMirror editor for the selected file + an output console below it;
  - right: the AI panel (same chat component as the drawer, docked).

All interactive pieces (editor, chat, run output) are wrapped in `<ClientOnly>` to
avoid SSR errors during VitePress's static prerender.

## 5. Folder-Aware File Discovery

One eager glob of the whole tree, grouped by directory at build time:

```js
const raw = import.meta.glob('/src/main/java/**/*.java', { query: '?raw', import: 'default', eager: true })
// → group keys by dirname into { [dir]: [{name, content, path}] }
```

At runtime the Playground reads `useData().page.value.relativePath`, takes its
`dirname`, and selects only that folder's files. (Loading all ~132 files as raw
strings into the bundle is fine for a local tool.)

## 6. Multi-Run Handling

A folder may contain 0, 1, or many runnable classes, and they are interconnected
(a runner depends on sibling product/service classes).

- **Load the whole folder**, not one file — that is the unit that compiles together.
- **Detect run targets by content**: a file is runnable iff it contains
  `public static void main`. Its FQCN is built from the parsed `package` + class name.
  (Filename is unreliable — `BillPughSingleton`, `*Practice`, etc. have mains and are
  not named `*Run`.)
- **Run-target selector**: each main-bearing file in the list gets its own Run button.
  Non-main files are view/edit only.
- **Interconnection is automatic**: the backend runs `mvn -q compile` over the whole
  module before `exec:java`, so any launched main links against all siblings — exactly
  how `mvn exec:java` already behaves.

## 7. Edit · Save · Run

- **Editor**: CodeMirror 6 + `@codemirror/lang-java`. Chosen over Monaco for lighter
  weight and smoother Vite/SSR integration. Client-only.
- **Save**: `POST /api/save { relativePath, content }`. The sidecar resolves the path,
  rejects anything outside `src/main/java/.../backend_fundamentals`, and writes the file.
- **Run**: `POST /api/run { fqcn }`. The sidecar executes
  `mvn -q compile && mvn -q exec:java -Dexec.mainClass="<fqcn>"` from repo root and
  returns `{ stdout, stderr, error }`. The UI shows a "Compiling…" state during the call.

## 8. AI Agent

- **Endpoint**: `POST /api/agent` returning a streamed `text/event-stream` response.
  The body carries the user message, page context, and optional `sessionId`. The
  client consumes it via `fetch` + a `ReadableStream` reader (not native `EventSource`,
  which is GET-only and cannot carry the context body).
- **Spawn**:
  `claude -p "<message>" --output-format stream-json --add-dir <repoRoot> --permission-mode acceptEdits [--resume <sessionId>]`,
  with `--append-system-prompt` injecting page context: the folder path, its `.java`
  and sibling `.md`, and the currently selected file.
- **Streaming**: the CLI emits NDJSON `stream-json` events; the sidecar forwards each as
  a `text/event-stream` chunk; the chat UI (reading the `fetch` stream) renders
  tokens/tool-activity live.
- **Multi-turn**: capture `session_id` from the first response; subsequent turns pass
  `--resume <sessionId>`. One active session per page; a "New chat" button resets it;
  navigating away ends it (a page→session map can persist this later).
- **File edits reflect back**: when the agent edits the open file, the editor reloads
  that file's content from disk after the turn completes.
- **Auth & permissions**: uses the existing Claude Code auth — no API key. Default
  `acceptEdits` (auto-accepts file edits). A UI toggle escalates to `bypassPermissions`
  (also auto-runs bash/commands) since it is the author's own local repo. The agent is
  scoped to the repo via `--add-dir` and every action is visible in the transcript.

## 9. Component / Module Boundaries

| Unit | Responsibility | Depends on |
| --- | --- | --- |
| `server/index.js` | HTTP router for the sidecar | run / save / agent handlers |
| `server/run.js` | spawn mvn compile+exec, capture output | `child_process`, repo root |
| `server/save.js` | path-validate + write file | `fs`, java-tree root |
| `server/agent.js` | spawn claude CLI, manage sessions, stream SSE | `child_process`, session map |
| `theme/Layout.vue` | tab bar + Read/Playground switch | VitePress `useData`, `useRoute` |
| `theme/Playground.vue` | folder files + editor + run console + AI | file-discovery util, `/api/run`, `/api/save` |
| `theme/CodeEditor.vue` | CodeMirror wrapper (client-only) | CodeMirror 6 |
| `theme/AgentChat.vue` | chat UI, stream client, used by drawer & dock | `/api/agent` (fetch stream) |
| `theme/fileDiscovery.js` | global glob → folder→files map; main detection | `import.meta.glob` |

Each is independently understandable and testable; the frontend never touches the
filesystem, and the backend never renders UI.

## 10. Error Handling

- **Run**: compile/runtime errors surface in the console pane (stderr in red, exit
  status shown). Backend unreachable → "Is the backend running? `npm run dev`".
- **Save**: path-validation failure → 400 with a clear message; nothing written.
- **Agent**: CLI spawn failure or non-zero exit → an error bubble in the chat; SSE
  connection drop → "reconnect" affordance. Malformed `stream-json` lines are skipped, not fatal.
- **SSR**: interactive components are `<ClientOnly>`; a missing folder match renders a
  plain "no code in this folder" state rather than throwing.

## 11. Out of Scope (YAGNI)

- Public deployment, hosted backend, or online code execution.
- Compile-incremental optimization (accept mvn cold-start latency in v1; note for later).
- A visual diff view for agent edits (v1 reloads the file; diff can come later).
- Persisting agent sessions across navigation or restarts (v1 keeps one live per page).
- In-browser creation of new files (edit/run existing files only).

## 12. Testing & Verification

- **Backend**: run `/api/run` against a known runner (e.g. a singleton main) and assert
  expected stdout; `/api/save` round-trips content and rejects out-of-tree paths;
  `/api/agent` streams at least one event from a trivial prompt.
- **Frontend**: build passes (`npm run docs:build`); a folder with multiple mains
  (`collections/maps/basics`) shows three run targets; a doc-only page shows no tab;
  the playground on `singleton/` lists only singleton files.
- **Manual**: edit → save → run reflects the change; agent edit reloads the editor.
```
