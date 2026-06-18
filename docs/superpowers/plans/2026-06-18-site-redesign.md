# SDE-2 Mastery Site Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the local-only VitePress study site with clean hub-page navigation, a folder-aware code Playground (edit + run, multi-main aware), and a per-page Claude Code agent.

**Architecture:** A VitePress frontend talks over `/api/*` (Vite proxy) to a small Node sidecar (`server/`) that owns all filesystem and child-process work: `mvn` runs, file saves, and streamed `claude` CLI sessions. A custom theme Layout adds a per-page `Read ↔ Playground` tab; the Playground discovers a folder's `.java` files via a global `import.meta.glob` filtered by the current page path.

**Tech Stack:** VitePress, Vue 3, Node (`node:http`, `node:test`), CodeMirror 6, `vitepress-plugin-mermaid`, `concurrently`, Maven/JDK, `claude` CLI.

**Reference spec:** `docs/superpowers/specs/2026-06-18-site-redesign-design.md`

---

## File Structure

**Backend sidecar (`server/`)** — the only code touching fs / child processes:
- `server/index.js` — `node:http` router; dispatches `/api/run`, `/api/save`, `/api/agent`, `/api/health`.
- `server/run.js` — `runJava(fqcn)`: `mvn compile && exec:java`, returns `{stdout, stderr, error}`.
- `server/save.js` — `saveJava(relativePath, content)`: path-validate inside the java tree, write file.
- `server/paths.js` — shared roots (`REPO_ROOT`, `JAVA_ROOT`) + `resolveInsideJavaTree(relativePath)`.
- `server/agent.js` — `streamAgent({message, context, sessionId}, onEvent)`: spawn `claude`, parse stream-json.

**Frontend theme (`docs/.vitepress/theme/`)**:
- `theme/index.js` — register components + global layout + css (modify existing).
- `theme/Layout.vue` — wraps `DefaultTheme.Layout`, adds Read/Playground tab + AI drawer.
- `theme/components/Playground.vue` — folder file list + editor + run console + docked AI.
- `theme/components/CodeEditor.vue` — CodeMirror 6 wrapper (client-only).
- `theme/components/AgentChat.vue` — chat UI + `/api/agent` fetch-stream client (used by drawer & dock).
- `theme/lib/fileDiscovery.js` — pure `analyzeJavaFiles(filesMap)` + `filesForPage(all, relativePath)`.

**Config / docs**:
- `docs/.vitepress/config.mjs` — Mermaid wrap, Vite proxy, nav (modify).
- `package.json` — deps + `dev` script (modify).
- Hub pages: `src/main/java/org/example/backend_fundamentals/index.md` (homepage) + per-domain `index.md` hubs.

**Tests (`server/*.test.js`)** — `node:test`, run with `node --test`.

Old code removed: the `javaRunnerPlugin` middleware and `RunCode.vue` / the old `CodePlayground.vue` are superseded.

---

## Task 1: Dependencies, gitignore, and dev script

**Files:**
- Modify: `package.json`
- Modify: `.gitignore`

- [ ] **Step 1: Add ignores**

Append to `.gitignore`:
```
### Project ###
node_modules/
.superpowers/
docs/.vitepress/cache/
docs/.vitepress/dist/
.vitepress/
```

- [ ] **Step 2: Install dependencies**

Run:
```bash
npm install -D concurrently mermaid vitepress-plugin-mermaid
npm install codemirror @codemirror/lang-java @codemirror/state @codemirror/view @codemirror/theme-one-dark
```
Expected: packages added, no peer-dep errors that block install.

- [ ] **Step 3: Replace the scripts block in `package.json`**

```json
  "scripts": {
    "server": "node server/index.js",
    "docs:dev": "vitepress dev docs",
    "dev": "concurrently -n server,site -c green,cyan \"npm run server\" \"npm run docs:dev\"",
    "docs:build": "vitepress build docs",
    "docs:preview": "vitepress preview docs",
    "test": "node --test server/"
  },
```

- [ ] **Step 4: Commit**

```bash
git add package.json package-lock.json .gitignore
git commit -m "chore: add site deps, dev script, and ignores"
```

---

## Task 2: Backend shared paths

**Files:**
- Create: `server/paths.js`
- Test: `server/paths.test.js`

- [ ] **Step 1: Write the failing test**

```js
// server/paths.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { resolveInsideJavaTree, JAVA_ROOT } = require('./paths')

test('resolves a valid in-tree path to an absolute path', () => {
  const abs = resolveInsideJavaTree('design_patterns/creational/singleton/BillPughSingleton.java')
  assert.ok(abs.startsWith(JAVA_ROOT))
  assert.ok(abs.endsWith('BillPughSingleton.java'))
})

test('rejects path traversal', () => {
  assert.throws(() => resolveInsideJavaTree('../../../../../etc/passwd'))
})

test('rejects absolute paths outside the tree', () => {
  assert.throws(() => resolveInsideJavaTree('/etc/passwd'))
})
```

- [ ] **Step 2: Run it to verify it fails**

Run: `node --test server/paths.test.js`
Expected: FAIL — cannot find module `./paths`.

- [ ] **Step 3: Implement `server/paths.js`**

```js
// server/paths.js
const path = require('node:path')

// server/ lives at repo root, so repo root is one level up.
const REPO_ROOT = path.resolve(__dirname, '..')
const JAVA_ROOT = path.join(REPO_ROOT, 'src', 'main', 'java', 'org', 'example', 'backend_fundamentals')

/**
 * Resolve a repo-relative-ish path (relative to JAVA_ROOT) to an absolute path,
 * guaranteeing the result stays inside JAVA_ROOT. Throws otherwise.
 */
function resolveInsideJavaTree(relativePath) {
  const abs = path.resolve(JAVA_ROOT, relativePath)
  if (abs !== JAVA_ROOT && !abs.startsWith(JAVA_ROOT + path.sep)) {
    throw new Error(`Path escapes java tree: ${relativePath}`)
  }
  return abs
}

module.exports = { REPO_ROOT, JAVA_ROOT, resolveInsideJavaTree }
```

- [ ] **Step 4: Run it to verify it passes**

Run: `node --test server/paths.test.js`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add server/paths.js server/paths.test.js
git commit -m "feat(server): path resolution scoped to java tree"
```

---

## Task 3: Backend — run Java

**Files:**
- Create: `server/run.js`
- Test: `server/run.test.js`

- [ ] **Step 1: Write the failing test** (real compile+run of a known main; slow, so a generous timeout)

```js
// server/run.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { runJava } = require('./run')

test('runs a known main and returns output', { timeout: 120000 }, async () => {
  const fqcn = 'org.example.backend_fundamentals.design_patterns.creational.singleton.BillPughSingleton'
  const res = await runJava(fqcn)
  assert.strictEqual(typeof res.stdout, 'string')
  assert.strictEqual(typeof res.stderr, 'string')
  // A successful exec has no thrown process error.
  assert.strictEqual(res.error, null)
})

test('rejects a missing fqcn', async () => {
  await assert.rejects(() => runJava(''))
})
```

- [ ] **Step 2: Run it to verify it fails**

Run: `node --test server/run.test.js`
Expected: FAIL — cannot find module `./run`.

- [ ] **Step 3: Implement `server/run.js`**

```js
// server/run.js
const { exec } = require('node:child_process')
const { REPO_ROOT } = require('./paths')

/**
 * Compile the module and run a main class via Maven exec.
 * Resolves with { stdout, stderr, error } — error is the process error message or null.
 */
function runJava(fqcn) {
  return new Promise((resolve, reject) => {
    if (!fqcn || typeof fqcn !== 'string') {
      return reject(new Error('fqcn is required'))
    }
    if (!/^[\w.]+$/.test(fqcn)) {
      return reject(new Error('invalid fqcn'))
    }
    const cmd = `mvn -q compile && mvn -q exec:java -Dexec.mainClass="${fqcn}"`
    exec(cmd, { cwd: REPO_ROOT, maxBuffer: 10 * 1024 * 1024 }, (error, stdout, stderr) => {
      resolve({
        stdout: stdout || '',
        stderr: stderr || '',
        error: error ? error.message : null,
      })
    })
  })
}

module.exports = { runJava }
```

- [ ] **Step 4: Run it to verify it passes**

Run: `node --test server/run.test.js`
Expected: PASS (compile may take ~10-60s on first run).

- [ ] **Step 5: Commit**

```bash
git add server/run.js server/run.test.js
git commit -m "feat(server): run java via maven exec"
```

---

## Task 4: Backend — save Java

**Files:**
- Create: `server/save.js`
- Test: `server/save.test.js`

- [ ] **Step 1: Write the failing test** (round-trip without corrupting the real file)

```js
// server/save.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const fs = require('node:fs')
const { saveJava } = require('./save')
const { resolveInsideJavaTree } = require('./paths')

const REL = 'design_patterns/creational/singleton/BillPughSingleton.java'

test('round-trips file content', async () => {
  const abs = resolveInsideJavaTree(REL)
  const original = fs.readFileSync(abs, 'utf8')
  try {
    const modified = original + '\n// scratch-test-comment\n'
    await saveJava(REL, modified)
    assert.strictEqual(fs.readFileSync(abs, 'utf8'), modified)
  } finally {
    fs.writeFileSync(abs, original, 'utf8') // restore
  }
})

test('rejects out-of-tree paths', async () => {
  await assert.rejects(() => saveJava('../../../../etc/passwd', 'x'))
})
```

- [ ] **Step 2: Run it to verify it fails**

Run: `node --test server/save.test.js`
Expected: FAIL — cannot find module `./save`.

- [ ] **Step 3: Implement `server/save.js`**

```js
// server/save.js
const fs = require('node:fs/promises')
const { resolveInsideJavaTree } = require('./paths')

/**
 * Write content to a .java file inside the java tree. Throws on out-of-tree paths
 * or non-.java targets.
 */
async function saveJava(relativePath, content) {
  if (!relativePath || typeof content !== 'string') {
    throw new Error('relativePath and content are required')
  }
  if (!relativePath.endsWith('.java')) {
    throw new Error('only .java files may be saved')
  }
  const abs = resolveInsideJavaTree(relativePath)
  await fs.writeFile(abs, content, 'utf8')
  return { ok: true, path: relativePath }
}

module.exports = { saveJava }
```

- [ ] **Step 4: Run it to verify it passes**

Run: `node --test server/save.test.js`
Expected: PASS (2 tests); original file restored.

- [ ] **Step 5: Commit**

```bash
git add server/save.js server/save.test.js
git commit -m "feat(server): save java files with path guard"
```

---

## Task 5: Backend — agent streaming

**Files:**
- Create: `server/agent.js`
- Test: `server/agent.test.js`

- [ ] **Step 1: Write the failing test** (parsing logic is pure and unit-testable; the spawn is integration)

```js
// server/agent.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { buildArgs, parseStreamLine } = require('./agent')

test('buildArgs includes core flags and add-dir', () => {
  const args = buildArgs({ message: 'hi', sessionId: null })
  assert.ok(args.includes('-p'))
  assert.ok(args.includes('--output-format'))
  assert.ok(args.includes('stream-json'))
  assert.ok(args.includes('--verbose'))
  assert.ok(args.includes('--add-dir'))
})

test('buildArgs adds --resume when sessionId present', () => {
  const args = buildArgs({ message: 'hi', sessionId: 'abc-123' })
  assert.ok(args.includes('--resume'))
  assert.ok(args.includes('abc-123'))
})

test('parseStreamLine returns null for non-JSON', () => {
  assert.strictEqual(parseStreamLine('not json'), null)
})

test('parseStreamLine parses a JSON event', () => {
  const evt = parseStreamLine('{"type":"assistant","session_id":"s1"}')
  assert.strictEqual(evt.type, 'assistant')
  assert.strictEqual(evt.session_id, 's1')
})
```

- [ ] **Step 2: Run it to verify it fails**

Run: `node --test server/agent.test.js`
Expected: FAIL — cannot find module `./agent`.

- [ ] **Step 3: Implement `server/agent.js`**

```js
// server/agent.js
const { spawn } = require('node:child_process')
const { REPO_ROOT } = require('./paths')

/** Build the claude CLI argv for one turn. */
function buildArgs({ message, sessionId, context }) {
  const args = [
    '-p', message,
    '--output-format', 'stream-json',
    '--verbose',
    '--add-dir', REPO_ROOT,
    '--permission-mode', 'acceptEdits',
  ]
  if (context) {
    args.push('--append-system-prompt', context)
  }
  if (sessionId) {
    args.push('--resume', sessionId)
  }
  return args
}

/** Parse one NDJSON line from stream-json output; null if not JSON. */
function parseStreamLine(line) {
  const trimmed = line.trim()
  if (!trimmed) return null
  try {
    return JSON.parse(trimmed)
  } catch {
    return null
  }
}

/**
 * Spawn claude for one turn. Calls onEvent(evt) for each parsed stream-json event.
 * Resolves when the process exits.
 */
function streamAgent({ message, sessionId, context }, onEvent) {
  return new Promise((resolve) => {
    const child = spawn('claude', buildArgs({ message, sessionId, context }), {
      cwd: REPO_ROOT,
      env: process.env,
    })
    let buffer = ''
    child.stdout.on('data', (chunk) => {
      buffer += chunk.toString()
      const lines = buffer.split('\n')
      buffer = lines.pop() // keep partial line
      for (const line of lines) {
        const evt = parseStreamLine(line)
        if (evt) onEvent(evt)
      }
    })
    child.stderr.on('data', (chunk) => {
      onEvent({ type: 'stderr', text: chunk.toString() })
    })
    child.on('error', (err) => {
      onEvent({ type: 'error', text: err.message })
    })
    child.on('close', (code) => {
      const evt = parseStreamLine(buffer)
      if (evt) onEvent(evt)
      onEvent({ type: 'exit', code })
      resolve()
    })
  })
}

module.exports = { buildArgs, parseStreamLine, streamAgent }
```

- [ ] **Step 4: Run it to verify it passes**

Run: `node --test server/agent.test.js`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add server/agent.js server/agent.test.js
git commit -m "feat(server): claude agent stream + arg building"
```

---

## Task 6: Backend — HTTP router

**Files:**
- Create: `server/index.js`
- Test: `server/index.test.js`

- [ ] **Step 1: Write the failing test** (health + save through the real HTTP server)

```js
// server/index.test.js
const { test, after } = require('node:test')
const assert = require('node:assert')
const { createServer } = require('./index')

const server = createServer()
const base = new Promise((resolve) => {
  server.listen(0, () => resolve(`http://127.0.0.1:${server.address().port}`))
})
after(() => server.close())

test('GET /api/health returns ok', async () => {
  const url = await base
  const res = await fetch(`${url}/api/health`)
  const json = await res.json()
  assert.strictEqual(res.status, 200)
  assert.strictEqual(json.ok, true)
})

test('POST /api/save rejects out-of-tree path with 400', async () => {
  const url = await base
  const res = await fetch(`${url}/api/save`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ relativePath: '../../etc/passwd', content: 'x' }),
  })
  assert.strictEqual(res.status, 400)
})
```

- [ ] **Step 2: Run it to verify it fails**

Run: `node --test server/index.test.js`
Expected: FAIL — cannot find module `./index` (or no `createServer` export).

- [ ] **Step 3: Implement `server/index.js`**

```js
// server/index.js
const http = require('node:http')
const { runJava } = require('./run')
const { saveJava } = require('./save')
const { streamAgent } = require('./agent')

const PORT = process.env.SITE_BACKEND_PORT || 5174

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = ''
    req.on('data', (c) => { data += c })
    req.on('end', () => {
      try { resolve(data ? JSON.parse(data) : {}) }
      catch (e) { reject(e) }
    })
    req.on('error', reject)
  })
}

function sendJson(res, status, obj) {
  res.writeHead(status, { 'content-type': 'application/json' })
  res.end(JSON.stringify(obj))
}

function createServer() {
  return http.createServer(async (req, res) => {
    const { url, method } = req
    try {
      if (method === 'GET' && url === '/api/health') {
        return sendJson(res, 200, { ok: true })
      }
      if (method === 'POST' && url === '/api/run') {
        const { fqcn } = await readBody(req)
        const result = await runJava(fqcn)
        return sendJson(res, 200, result)
      }
      if (method === 'POST' && url === '/api/save') {
        const { relativePath, content } = await readBody(req)
        const result = await saveJava(relativePath, content)
        return sendJson(res, 200, result)
      }
      if (method === 'POST' && url === '/api/agent') {
        const { message, sessionId, context } = await readBody(req)
        res.writeHead(200, {
          'content-type': 'text/event-stream',
          'cache-control': 'no-cache',
          connection: 'keep-alive',
        })
        await streamAgent({ message, sessionId, context }, (evt) => {
          res.write(`data: ${JSON.stringify(evt)}\n\n`)
        })
        return res.end()
      }
      sendJson(res, 404, { error: 'not found' })
    } catch (err) {
      sendJson(res, 400, { error: err.message })
    }
  })
}

if (require.main === module) {
  createServer().listen(PORT, () => {
    console.log(`[site-backend] listening on http://localhost:${PORT}`)
  })
}

module.exports = { createServer }
```

- [ ] **Step 4: Run it to verify it passes**

Run: `node --test server/index.test.js`
Expected: PASS (2 tests).

- [ ] **Step 5: Run the whole backend suite**

Run: `npm test`
Expected: all server tests PASS.

- [ ] **Step 6: Commit**

```bash
git add server/index.js server/index.test.js
git commit -m "feat(server): http router for run/save/agent/health"
```

---

## Task 7: VitePress config — Mermaid, proxy, nav, remove old plugin

**Files:**
- Modify: `docs/.vitepress/config.mjs`
- Delete: `docs/.vitepress/theme/components/RunCode.vue`, `docs/.vitepress/theme/components/CodePlayground.vue`

- [ ] **Step 1: Replace `docs/.vitepress/config.mjs`**

```js
import { withMermaid } from 'vitepress-plugin-mermaid'
import { generateSidebar } from 'vitepress-sidebar'

const commonSidebarConfig = {
  documentRootPath: 'src/main/java/org/example/backend_fundamentals',
  useTitleFromFileHeading: true,
  useTitleFromFrontmatter: true,
  collapseDepth: 2,
  capitalizeFirst: true,
  sortMenusByFrontmatterOrder: true,
}

export default withMermaid({
  title: 'SDE-2 Mastery',
  description: 'Study Plan & Backend Fundamentals',
  srcDir: '../src/main/java/org/example/backend_fundamentals',
  vite: {
    server: {
      proxy: { '/api': 'http://localhost:5174' },
    },
  },
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Study Plan', link: '/todo/study_plan/README' },
      { text: 'Java & JVM', link: '/java/' },
      { text: 'Spring', link: '/spring/' },
      { text: 'System Design', link: '/system_design/' },
      { text: 'Design Patterns', link: '/design_patterns/' },
      { text: 'Networking', link: '/networking/' },
      { text: 'Databases', link: '/databases/' },
    ],
    search: { provider: 'local' },
    sidebar: generateSidebar([
      { ...commonSidebarConfig, scanStartPath: 'todo', resolvePath: '/todo/' },
      { ...commonSidebarConfig, scanStartPath: 'java', resolvePath: '/java/' },
      { ...commonSidebarConfig, scanStartPath: 'spring', resolvePath: '/spring/' },
      { ...commonSidebarConfig, scanStartPath: 'spring_boot', resolvePath: '/spring_boot/' },
      { ...commonSidebarConfig, scanStartPath: 'system_design', resolvePath: '/system_design/' },
      { ...commonSidebarConfig, scanStartPath: 'design_patterns', resolvePath: '/design_patterns/' },
      { ...commonSidebarConfig, scanStartPath: 'networking', resolvePath: '/networking/' },
      { ...commonSidebarConfig, scanStartPath: 'databases', resolvePath: '/databases/' },
    ]),
  },
})
```

- [ ] **Step 2: Delete the superseded components**

```bash
git rm docs/.vitepress/theme/components/RunCode.vue docs/.vitepress/theme/components/CodePlayground.vue
```

- [ ] **Step 3: Verify the build still parses config** (will rebuild fully in Task 13)

Run: `node -e "import('./docs/.vitepress/config.mjs').then(()=>console.log('config OK'))"`
Expected: prints `config OK` (no import/syntax error).

- [ ] **Step 4: Commit**

```bash
git add docs/.vitepress/config.mjs
git commit -m "feat(site): mermaid, api proxy, domain nav; drop old run components"
```

---

## Task 8: File discovery (pure core)

**Files:**
- Create: `docs/.vitepress/theme/lib/fileDiscovery.js`
- Test: `docs/.vitepress/theme/lib/fileDiscovery.test.js`

- [ ] **Step 1: Write the failing test**

```js
// docs/.vitepress/theme/lib/fileDiscovery.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { analyzeJavaFiles, filesForPage } = require('./fileDiscovery')

const FILES = {
  '/src/.../singleton/BillPughSingleton.java':
    'package org.example.singleton;\npublic class BillPughSingleton { public static void main(String[] a){} }',
  '/src/.../singleton/NoSingleton.java':
    'package org.example.singleton;\npublic class NoSingleton {}',
  '/src/.../maps/basics/HashMapBasicsRun.java':
    'package org.example.maps;\npublic class HashMapBasicsRun { public static void main(String[] a){} }',
}

test('groups files by directory', () => {
  const grouped = analyzeJavaFiles(FILES)
  assert.ok(grouped['/src/.../singleton'])
  assert.strictEqual(grouped['/src/.../singleton'].length, 2)
})

test('detects main classes by content and builds fqcn', () => {
  const grouped = analyzeJavaFiles(FILES)
  const billpugh = grouped['/src/.../singleton'].find((f) => f.name === 'BillPughSingleton.java')
  const nosingleton = grouped['/src/.../singleton'].find((f) => f.name === 'NoSingleton.java')
  assert.strictEqual(billpugh.runnable, true)
  assert.strictEqual(billpugh.fqcn, 'org.example.singleton.BillPughSingleton')
  assert.strictEqual(nosingleton.runnable, false)
})

test('filesForPage matches the markdown folder', () => {
  const grouped = analyzeJavaFiles(FILES)
  const matched = filesForPage(grouped, 'maps/basics/MapsDoc.md', (dir) => dir.endsWith('/maps/basics'))
  assert.strictEqual(matched.length, 1)
  assert.strictEqual(matched[0].name, 'HashMapBasicsRun.java')
})
```

- [ ] **Step 2: Run it to verify it fails**

Run: `node --test docs/.vitepress/theme/lib/fileDiscovery.test.js`
Expected: FAIL — cannot find module `./fileDiscovery`.

- [ ] **Step 3: Implement `docs/.vitepress/theme/lib/fileDiscovery.js`** (CommonJS-compatible so `node --test` can require it; Vite also imports it via ESM interop — use `module.exports` and a matching `export` shim)

```js
// docs/.vitepress/theme/lib/fileDiscovery.js
function dirname(p) {
  const i = p.lastIndexOf('/')
  return i === -1 ? '' : p.slice(0, i)
}
function basename(p) {
  const i = p.lastIndexOf('/')
  return i === -1 ? p : p.slice(i + 1)
}

/** Turn a { path: content } map into { dir: [ {name, path, content, runnable, fqcn} ] }. */
function analyzeJavaFiles(filesMap) {
  const grouped = {}
  for (const path of Object.keys(filesMap)) {
    const content = filesMap[path]
    const dir = dirname(path)
    const name = basename(path)
    const className = name.replace(/\.java$/, '')
    const pkgMatch = content.match(/package\s+([\w.]+)\s*;/)
    const pkg = pkgMatch ? pkgMatch[1] : ''
    const runnable = /public\s+static\s+void\s+main\s*\(/.test(content)
    const fqcn = pkg ? `${pkg}.${className}` : className
    if (!grouped[dir]) grouped[dir] = []
    grouped[dir].push({ name, path, content, runnable, fqcn: runnable ? fqcn : null })
  }
  for (const dir of Object.keys(grouped)) {
    grouped[dir].sort((a, b) => a.name.localeCompare(b.name))
  }
  return grouped
}

/**
 * Given grouped files and the current markdown relativePath, return the files whose
 * directory matches the markdown's directory. `matcher(dir)` decides the match so the
 * caller can adapt to glob key shape.
 */
function filesForPage(grouped, relativePath, matcher) {
  for (const dir of Object.keys(grouped)) {
    if (matcher(dir)) return grouped[dir]
  }
  return []
}

module.exports = { analyzeJavaFiles, filesForPage }
```

- [ ] **Step 4: Run it to verify it passes**

Run: `node --test docs/.vitepress/theme/lib/fileDiscovery.test.js`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add docs/.vitepress/theme/lib/fileDiscovery.js docs/.vitepress/theme/lib/fileDiscovery.test.js
git commit -m "feat(site): folder-aware java discovery with main detection"
```

---

## Task 9: CodeEditor component (CodeMirror 6, client-only)

**Files:**
- Create: `docs/.vitepress/theme/components/CodeEditor.vue`

- [ ] **Step 1: Write the component**

```vue
<template>
  <div ref="host" class="code-editor"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { EditorView, basicSetup } from 'codemirror'
import { EditorState } from '@codemirror/state'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readonly: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue'])

const host = ref(null)
let view = null

function makeState(doc) {
  return EditorState.create({
    doc,
    extensions: [
      basicSetup,
      java(),
      oneDark,
      EditorView.editable.of(!props.readonly),
      EditorView.updateListener.of((v) => {
        if (v.docChanged) emit('update:modelValue', v.state.doc.toString())
      }),
    ],
  })
}

onMounted(() => {
  view = new EditorView({ state: makeState(props.modelValue), parent: host.value })
})

// Reset the document when the selected file changes from the parent.
watch(() => props.modelValue, (val) => {
  if (view && val !== view.state.doc.toString()) {
    view.setState(makeState(val))
  }
})

onBeforeUnmount(() => view && view.destroy())
</script>

<style scoped>
.code-editor { border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; }
.code-editor :deep(.cm-editor) { max-height: 480px; }
</style>
```

- [ ] **Step 2: Commit** (rendering is verified in Task 13)

```bash
git add docs/.vitepress/theme/components/CodeEditor.vue
git commit -m "feat(site): CodeMirror java editor component"
```

---

## Task 10: AgentChat component (fetch-stream client)

**Files:**
- Create: `docs/.vitepress/theme/components/AgentChat.vue`

- [ ] **Step 1: Write the component**

```vue
<template>
  <div class="agent-chat">
    <div class="agent-log" ref="logEl">
      <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
        <div class="role">{{ m.role === 'user' ? 'You' : 'Agent' }}</div>
        <pre class="text">{{ m.text }}</pre>
      </div>
      <div v-if="busy" class="msg agent"><div class="role">Agent</div><pre class="text">{{ streaming || '…' }}</pre></div>
    </div>
    <form class="agent-input" @submit.prevent="send">
      <input v-model="draft" :disabled="busy" placeholder="Ask about this page, or tell the agent what to do…" />
      <button type="submit" :disabled="busy || !draft.trim()">Send</button>
      <button type="button" class="reset" @click="reset" :disabled="busy">New chat</button>
    </form>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  context: { type: String, default: '' }, // page context string injected as system prompt
})

const messages = ref([])
const draft = ref('')
const busy = ref(false)
const streaming = ref('')
const sessionId = ref(null)
const logEl = ref(null)

async function scrollDown() {
  await nextTick()
  if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight
}

function reset() {
  messages.value = []
  sessionId.value = null
  streaming.value = ''
}

async function send() {
  const text = draft.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', text })
  draft.value = ''
  busy.value = true
  streaming.value = ''
  await scrollDown()

  try {
    const res = await fetch('/api/agent', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ message: text, sessionId: sessionId.value, context: props.context }),
    })
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buf = ''
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buf += decoder.decode(value, { stream: true })
      const parts = buf.split('\n\n')
      buf = parts.pop()
      for (const part of parts) {
        const line = part.replace(/^data: /, '')
        if (!line) continue
        let evt
        try { evt = JSON.parse(line) } catch { continue }
        handleEvent(evt)
        await scrollDown()
      }
    }
  } catch (e) {
    messages.value.push({ role: 'agent', text: 'Connection error: ' + e.message + '\nIs the backend running? `npm run dev`' })
  } finally {
    if (streaming.value) messages.value.push({ role: 'agent', text: streaming.value })
    streaming.value = ''
    busy.value = false
    await scrollDown()
  }
}

function handleEvent(evt) {
  if (evt.session_id && !sessionId.value) sessionId.value = evt.session_id
  // stream-json: assistant messages carry content blocks; result carries final text.
  if (evt.type === 'assistant' && evt.message && Array.isArray(evt.message.content)) {
    for (const block of evt.message.content) {
      if (block.type === 'text') streaming.value += block.text
    }
  } else if (evt.type === 'result' && typeof evt.result === 'string') {
    if (!streaming.value) streaming.value = evt.result
  } else if (evt.type === 'error' || evt.type === 'stderr') {
    streaming.value += (evt.text || '')
  }
}
</script>

<style scoped>
.agent-chat { display: flex; flex-direction: column; height: 100%; min-height: 320px; border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; }
.agent-log { flex: 1; overflow-y: auto; padding: 12px; background: var(--vp-c-bg-soft); }
.msg { margin-bottom: 12px; }
.msg .role { font-size: 11px; font-weight: 700; color: var(--vp-c-brand-1); text-transform: uppercase; }
.msg .text { margin: 2px 0 0; white-space: pre-wrap; word-break: break-word; font-family: var(--vp-font-family-base); font-size: 14px; }
.msg.user .text { color: var(--vp-c-text-1); }
.agent-input { display: flex; gap: 6px; padding: 8px; border-top: 1px solid var(--vp-c-divider); }
.agent-input input { flex: 1; padding: 6px 10px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg); color: var(--vp-c-text-1); }
.agent-input button { padding: 6px 12px; border: none; border-radius: 6px; background: var(--vp-c-brand-1); color: #fff; cursor: pointer; }
.agent-input button:disabled { opacity: 0.5; cursor: not-allowed; }
.agent-input .reset { background: var(--vp-c-bg-mute); color: var(--vp-c-text-2); }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add docs/.vitepress/theme/components/AgentChat.vue
git commit -m "feat(site): streaming agent chat component"
```

---

## Task 11: Playground component

**Files:**
- Create: `docs/.vitepress/theme/components/Playground.vue`

- [ ] **Step 1: Write the component**

```vue
<template>
  <div class="playground" v-if="files.length">
    <aside class="files">
      <div class="files-head">Files</div>
      <div
        v-for="f in files" :key="f.path"
        :class="['file', selected && selected.path === f.path ? 'active' : '']"
        @click="select(f)"
      >
        <span class="fname">{{ f.name }}</span>
        <button v-if="f.runnable" class="run" :disabled="running" @click.stop="run(f)">▶</button>
      </div>
    </aside>

    <section class="main">
      <div class="editor-head">
        <span>{{ selected ? selected.name : '' }}</span>
        <span class="actions">
          <button :disabled="!dirty || saving" @click="save">{{ saving ? 'Saving…' : 'Save' }}</button>
          <button v-if="selected && selected.runnable" :disabled="running" @click="run(selected)">
            {{ running ? 'Compiling…' : 'Run' }}
          </button>
        </span>
      </div>

      <ClientOnly>
        <CodeEditor v-if="selected" v-model="buffer" />
      </ClientOnly>

      <div v-if="output || runError" class="console">
        <div class="console-head">Output</div>
        <pre v-if="output" class="stdout">{{ output }}</pre>
        <pre v-if="runError" class="stderr">{{ runError }}</pre>
      </div>
    </section>

    <aside class="agent">
      <ClientOnly>
        <AgentChat :context="agentContext" />
      </ClientOnly>
    </aside>
  </div>
  <div v-else class="playground-empty">No Java files in this folder.</div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useData } from 'vitepress'
import CodeEditor from './CodeEditor.vue'
import AgentChat from './AgentChat.vue'
import { analyzeJavaFiles, filesForPage } from '../lib/fileDiscovery.js'

// Eager glob of the whole java tree (raw text). Keys are absolute-ish vite paths.
const raw = import.meta.glob('/src/main/java/**/*.java', { query: '?raw', import: 'default', eager: true })
const grouped = analyzeJavaFiles(raw)

const { page } = useData()

function pageDir(relativePath) {
  const i = relativePath.lastIndexOf('/')
  return i === -1 ? '' : relativePath.slice(0, i) // e.g. design_patterns/creational/singleton
}

const files = computed(() => {
  const dir = pageDir(page.value.relativePath)
  if (!dir) return []
  return filesForPage(grouped, page.value.relativePath, (d) => d.endsWith('/' + dir) || d.endsWith(dir))
})

const selected = ref(null)
const buffer = ref('')
const original = ref('')
const saving = ref(false)
const running = ref(false)
const output = ref('')
const runError = ref('')

const dirty = computed(() => selected.value && buffer.value !== original.value)

const agentContext = computed(() => {
  const dir = pageDir(page.value.relativePath)
  const names = files.value.map((f) => f.name).join(', ')
  return `The user is studying the page ${page.value.relativePath}. ` +
    `Java files in this folder (${dir}): ${names}. ` +
    (selected.value ? `Currently open: ${selected.value.name}.` : '')
})

function select(f) {
  selected.value = f
  buffer.value = f.content
  original.value = f.content
  output.value = ''
  runError.value = ''
}

// auto-select first file when the folder changes
watch(files, (list) => {
  if (list.length) select(list[0])
  else selected.value = null
}, { immediate: true })

async function save() {
  if (!selected.value) return
  saving.value = true
  try {
    const rel = relFromFqcnPath(selected.value)
    const res = await fetch('/api/save', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ relativePath: rel, content: buffer.value }),
    })
    if (!res.ok) throw new Error((await res.json()).error || 'save failed')
    original.value = buffer.value
  } catch (e) {
    runError.value = 'Save error: ' + e.message
  } finally {
    saving.value = false
  }
}

async function run(f) {
  running.value = true
  output.value = ''
  runError.value = ''
  try {
    const res = await fetch('/api/run', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ fqcn: f.fqcn }),
    })
    const data = await res.json()
    output.value = data.stdout || (data.error ? '' : 'Process finished (no output).')
    if (data.stderr) runError.value += data.stderr
    if (data.error) runError.value += '\n' + data.error
  } catch (e) {
    runError.value = 'Run error: ' + e.message + '\nIs the backend running? `npm run dev`'
  } finally {
    running.value = false
  }
}

// Derive the JAVA_ROOT-relative path the backend expects from the glob key.
function relFromFqcnPath(f) {
  const marker = 'backend_fundamentals/'
  const idx = f.path.indexOf(marker)
  return idx === -1 ? f.name : f.path.slice(idx + marker.length)
}
</script>

<style scoped>
.playground { display: grid; grid-template-columns: 200px 1fr 320px; gap: 12px; min-height: 540px; }
.files, .agent { min-width: 0; }
.files { border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; }
.files-head, .console-head { padding: 8px 12px; font-weight: 700; border-bottom: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft); }
.file { display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; cursor: pointer; font-family: var(--vp-font-family-mono); font-size: 13px; }
.file:hover { background: var(--vp-c-bg-soft); }
.file.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); }
.file .run { border: none; background: transparent; color: var(--vp-c-brand-1); cursor: pointer; font-size: 12px; }
.main { display: flex; flex-direction: column; min-width: 0; }
.editor-head { display: flex; justify-content: space-between; align-items: center; padding: 6px 10px; background: var(--vp-c-bg-soft); border: 1px solid var(--vp-c-divider); border-bottom: none; border-radius: 8px 8px 0 0; font-family: var(--vp-font-family-mono); }
.editor-head button { margin-left: 8px; padding: 4px 10px; border: none; border-radius: 6px; background: var(--vp-c-brand-1); color: #fff; cursor: pointer; }
.editor-head button:disabled { opacity: 0.5; cursor: not-allowed; }
.console { margin-top: 10px; border-radius: 8px; overflow: hidden; background: #000; }
.stdout { color: #a7f3d0; padding: 10px; margin: 0; overflow-x: auto; font-family: var(--vp-font-family-mono); font-size: 13px; }
.stderr { color: #fca5a5; padding: 10px; margin: 0; overflow-x: auto; font-family: var(--vp-font-family-mono); font-size: 13px; }
.playground-empty { padding: 24px; color: var(--vp-c-text-2); }
@media (max-width: 960px) { .playground { grid-template-columns: 1fr; } }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add docs/.vitepress/theme/components/Playground.vue
git commit -m "feat(site): folder-aware playground with multi-run + save + agent"
```

---

## Task 12: Custom Layout — Read/Playground tab + AI drawer

**Files:**
- Create: `docs/.vitepress/theme/Layout.vue`
- Modify: `docs/.vitepress/theme/index.js`

- [ ] **Step 1: Write `docs/.vitepress/theme/Layout.vue`**

```vue
<template>
  <div>
    <div v-if="hasCode" class="mode-tabs">
      <button :class="{ active: mode === 'read' }" @click="setMode('read')">Read</button>
      <button :class="{ active: mode === 'play' }" @click="setMode('play')">⚙ Playground</button>
      <button class="ai-toggle" @click="drawer = !drawer">✦ Ask AI</button>
    </div>

    <DefaultTheme.Layout v-if="mode === 'read'" />
    <div v-else class="play-wrap">
      <ClientOnly><Playground /></ClientOnly>
    </div>

    <div v-if="drawer" class="ai-drawer">
      <div class="ai-drawer-head">
        <span>✦ Agent — {{ shortPath }}</span>
        <button @click="drawer = false">✕</button>
      </div>
      <ClientOnly><AgentChat :context="drawerContext" /></ClientOnly>
    </div>
  </div>
</template>

<script setup>
import DefaultTheme from 'vitepress/theme'
import { ref, computed, watch } from 'vue'
import { useData, useRoute } from 'vitepress'
import Playground from './components/Playground.vue'
import AgentChat from './components/AgentChat.vue'
import { analyzeJavaFiles } from './lib/fileDiscovery.js'

const raw = import.meta.glob('/src/main/java/**/*.java', { query: '?raw', import: 'default', eager: true })
const grouped = analyzeJavaFiles(raw)

const { page } = useData()
const route = useRoute()
const mode = ref('read')
const drawer = ref(false)

function pageDir(rel) {
  const i = rel.lastIndexOf('/')
  return i === -1 ? '' : rel.slice(0, i)
}

const hasCode = computed(() => {
  const dir = pageDir(page.value.relativePath)
  if (!dir) return false
  return Object.keys(grouped).some((d) => d.endsWith('/' + dir) || d.endsWith(dir))
})

const shortPath = computed(() => page.value.relativePath.split('/').pop())
const drawerContext = computed(() => `The user is reading ${page.value.relativePath}. Discuss or act on this page's content.`)

function setMode(m) {
  mode.value = m
  if (typeof location !== 'undefined') {
    location.hash = m === 'play' ? 'playground' : ''
  }
}

function syncFromHash() {
  if (typeof location === 'undefined') return
  mode.value = location.hash === '#playground' && hasCode.value ? 'play' : 'read'
}

watch(() => route.path, () => { syncFromHash() })
syncFromHash()
</script>

<style scoped>
.mode-tabs { display: flex; gap: 8px; padding: 8px 24px; border-bottom: 1px solid var(--vp-c-divider); position: sticky; top: var(--vp-nav-height); background: var(--vp-c-bg); z-index: 10; }
.mode-tabs button { padding: 4px 12px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg-soft); color: var(--vp-c-text-1); cursor: pointer; }
.mode-tabs button.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); border-color: var(--vp-c-brand-1); }
.mode-tabs .ai-toggle { margin-left: auto; }
.play-wrap { padding: 16px 24px; }
.ai-drawer { position: fixed; top: 0; right: 0; width: 380px; max-width: 90vw; height: 100vh; background: var(--vp-c-bg); border-left: 1px solid var(--vp-c-divider); box-shadow: -4px 0 16px rgba(0,0,0,0.15); z-index: 100; display: flex; flex-direction: column; }
.ai-drawer-head { display: flex; justify-content: space-between; align-items: center; padding: 12px; border-bottom: 1px solid var(--vp-c-divider); font-weight: 700; }
.ai-drawer-head button { border: none; background: transparent; cursor: pointer; font-size: 16px; color: var(--vp-c-text-2); }
.ai-drawer :deep(.agent-chat) { flex: 1; border: none; border-radius: 0; }
</style>
```

- [ ] **Step 2: Replace `docs/.vitepress/theme/index.js`**

```js
import DefaultTheme from 'vitepress/theme'
import Layout from './Layout.vue'
import './custom.css'

export default {
  ...DefaultTheme,
  Layout,
}
```

- [ ] **Step 3: Commit**

```bash
git add docs/.vitepress/theme/Layout.vue docs/.vitepress/theme/index.js
git commit -m "feat(site): read/playground tab layout + AI drawer"
```

---

## Task 13: Homepage + domain hub pages

**Files:**
- Modify: `src/main/java/org/example/backend_fundamentals/index.md`
- Create: `src/main/java/org/example/backend_fundamentals/java/index.md`
- Create: `src/main/java/org/example/backend_fundamentals/design_patterns/index.md`
- Create: `src/main/java/org/example/backend_fundamentals/system_design/index.md`
- Create: `src/main/java/org/example/backend_fundamentals/spring/index.md`
- Create: `src/main/java/org/example/backend_fundamentals/networking/index.md`
- Create: `src/main/java/org/example/backend_fundamentals/databases/index.md`

- [ ] **Step 1: Replace the homepage `index.md`**

```markdown
---
layout: home
hero:
  name: "SDE-2 Mastery"
  text: "Backend Fundamentals, System Design & Patterns"
  tagline: "A local, runnable study workspace — read the theory, run the code, ask the agent."
  actions:
    - theme: brand
      text: "Open Study Plan"
      link: "/todo/study_plan/README"
    - theme: alt
      text: "Design Patterns"
      link: "/design_patterns/"
features:
  - title: "Study Plan"
    details: "The 32-Part execution map and weekly schedule."
    link: "/todo/study_plan/README"
  - title: "Java & JVM"
    details: "Generics, collections, concurrency, memory, exceptions."
    link: "/java/"
  - title: "Design Patterns"
    details: "Creational, structural, behavioral — with runnable demos."
    link: "/design_patterns/"
  - title: "System Design"
    details: "Clustering, caching, load balancing, fault tolerance."
    link: "/system_design/"
  - title: "Spring"
    details: "IoC, DI, bean lifecycle, transactions."
    link: "/spring/"
  - title: "Networking & Databases"
    details: "IP/NAT/DHCP, SQL vs NoSQL, graph databases."
    link: "/networking/"
---
```

- [ ] **Step 2: Create each domain hub** (`sidebar: false`, links into the domain). Example — `design_patterns/index.md`:

```markdown
---
sidebar: false
---

# Design Patterns

Runnable pattern demos with theory alongside. Open a pattern, switch to **Playground** to run its variants, or **Ask AI** to be quizzed.

- [Creational Patterns Roadmap](/design_patterns/creational/CreationalPatternsRoadmap)
- [Singleton](/design_patterns/creational/singleton/Singleton)
- [OOP Pillars (foundations)](/design_patterns/foundations/oop_pillars/)
```

Create the other five hub pages the same way, each starting with `---\nsidebar: false\n---`, a `# <Domain>` heading, one intro line, and a bullet list of that domain's key entry docs (use the existing files visible under each top-level folder). Keep each hub to the real docs that exist in that folder.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/example/backend_fundamentals/index.md src/main/java/org/example/backend_fundamentals/*/index.md
git commit -m "feat(site): real homepage + domain hub pages"
```

---

## Task 14: Full build + integration verification

**Files:** none (verification only)

- [ ] **Step 1: Backend unit suite**

Run: `npm test`
Expected: all `server/` tests PASS.

- [ ] **Step 2: Production build passes**

Run: `npm run docs:build`
Expected: build completes with no errors (Mermaid pages render, components compile).

- [ ] **Step 3: Boot both processes**

Run (in one terminal): `npm run dev`
Expected: `[site-backend] listening on http://localhost:5174` and VitePress on `http://localhost:5173`.

- [ ] **Step 4: Smoke-test with Playwright** — create `playwright_smoke.js` at repo root:

```js
const { chromium } = require('playwright')
;(async () => {
  const browser = await chromium.launch()
  const page = await browser.newPage()
  const fails = []

  // Health: backend reachable through the vite proxy
  const health = await page.request.get('http://localhost:5173/api/health')
  if (health.status() !== 200) fails.push('health not 200')

  // Singleton page shows the Playground tab and exactly the singleton files
  await page.goto('http://localhost:5173/design_patterns/creational/singleton/Singleton.html')
  await page.waitForLoadState('networkidle')
  const hasTab = await page.getByText('Playground').count()
  if (!hasTab) fails.push('no Playground tab on singleton page')
  await page.click('text=Playground')
  await page.waitForTimeout(500)
  const fileCount = await page.locator('.file').count()
  if (fileCount < 4) fails.push('singleton folder should list >= 4 java files, got ' + fileCount)

  // A multi-main folder exposes multiple run buttons
  await page.goto('http://localhost:5173/java/foundations/collections/maps/basics/#playground')
  await page.waitForTimeout(800)
  const runButtons = await page.locator('.file .run').count()
  if (runButtons < 3) fails.push('maps/basics should expose >= 3 run targets, got ' + runButtons)

  await browser.close()
  if (fails.length) { console.error('SMOKE FAILED:\n' + fails.join('\n')); process.exit(1) }
  console.log('SMOKE PASSED')
})()
```

Run: `node playwright_smoke.js`
Expected: `SMOKE PASSED`. (If the maps/basics path differs, adjust to the actual doc route; the assertion is "≥3 run targets".)

- [ ] **Step 5: Manual checks (record results in the commit message)**

- Edit a file in the Playground, click **Save**, click **Run** → output reflects the edit.
- Open **Ask AI**, send "Explain this file in two sentences" → tokens stream back.
- Ask the agent "add a one-line comment at the top of this file" → editor reloads showing the change.

- [ ] **Step 6: Commit**

```bash
git add playwright_smoke.js
git commit -m "test(site): playwright smoke for tabs, multi-run, proxy"
```

---

## Self-Review Notes (spec coverage)

- Spec §2 architecture → Tasks 2–6 (sidecar) + Task 7 (proxy).
- §3 navigation/reading → Task 7 (nav/mermaid) + Task 13 (hubs/homepage).
- §4 layout C → Task 12.
- §5 folder-aware discovery → Task 8 + Task 11 glob.
- §6 multi-run → Task 8 (main detection) + Task 11 (per-file run buttons) + Task 3 (mvn).
- §7 edit/save/run → Task 9 (editor) + Task 4 (save) + Task 3 (run) + Task 11 (wiring).
- §8 agent → Task 5 (backend) + Task 10 (chat) + Task 12 (drawer).
- §9 module boundaries → matches file structure above.
- §10 error handling → covered in run/save/agent handlers and component catch blocks.
- §12 testing → Task 14.
- §11 out-of-scope items are intentionally absent (no diff view, no new-file creation, no session persistence, no deploy).
