# LMS Architecture Automation Implementation Plan

> **Historical note:** This plan was created through `/superpowers`. The original superpowers sub-skill instructions below are implementation history, not a requirement for ordinary future maintenance.
> **Git rule:** Do not commit anything to Git unless the user explicitly asks for a commit.

**Goal:** Automate the VitePress navigation, restore secure Java execution, enforce build-time validation, build the Draw.io viewer, and test all edge cases.

**Current status:** This plan has already been executed on `chore/schema-migration`. Use it as implementation history and follow-up guidance, not as a fresh task list. When this plan conflicts with the live repository, prefer root `AGENTS.md`, `scripts/generate-homepage.js`, `docs/.vitepress/config.mjs`, and `package.json`. `.agents/AGENTS.md` is retained as older architecture-governance context.

## Active Follow-Ups
- Keep using `scripts/generate-homepage.js` as the project-wide validation contract for curriculum structure, generated navigation, exact child frontmatter, Java-only playgrounds, and assets-only system-design assets.
- Keep homepage, domain/category hub cards, top navigation, and sidebars generated from `navigation_map.json`; the generator now rejects hand-written local topic lists and manual `<AutoTopicGrid>` markers on pages with child topics.
- Continue tuning `order` frontmatter where human study sequencing still needs improvement. The generated hub cards and sidebars now share the same source, so order fixes update both.
- Keep `scripts/site-route-audit.js` as the broad navigation/layout smoke test for route status, browser console issues, and page-level overflow.
- Treat `scripts/auto-generate.js` as a scaffold until it invokes a reviewed local generation workflow and produces human-reviewable curriculum.
- Defer curriculum stub cleanup until explicitly requested; use `docs/superpowers/plans/2026-07-05-curriculum-stub-backlog.md` as the inventory.
- Public/static Java execution remains a separate product decision. Local `docs:dev` execution works through `/api/run-java`; a deployed static site still needs an approved hosted sandbox before Run can work for visitors.

## Required Review Gates
These gates are part of the implementation task list. Do not rely on memory or a final broad review to catch these areas.

- [x] **UX/Site Walkthrough Gate:** Playwright E2E, targeted generated-card checks, and desktop/mobile route audits verified homepage/domain/topic flows, system-design design page, Code mode, and mobile layout.
- [x] **Generator Validation Gate:** `npm test` runs focused positive and negative generator tests for theory-only leaves, child frontmatter, duplicate orders, invalid folder names, invalid `playground/`, invalid `assets/`, and Java files outside playgrounds.
- [x] **Java Execution Security Gate:** Current local runner is Vite dev-only, has no save endpoint, validates main-class and Java filenames, writes only to temp dirs, avoids shell command strings, caps body/file/output sizes, times out child processes, and cleans temp dirs. A future Maven sidecar or hosted runner still needs its own hardening pass.
- [x] **Memory/Lifecycle Gate:** E2E covers route-change stale-output protection, diagram iframe lifecycle, mobile Code mode, and Monaco disposal is handled in `CodeEditor.vue`.
- [ ] **Curriculum Stub Cleanup Gate:** Do not start stub cleanup during architecture implementation. When explicitly requested, use Task 7 and the stub backlog as the source inventory, then decide per topic whether to write real material or migrate to a lighter schema.

## Current Site Audit Findings
Audit date: 2026-07-06. Commands run: `npm run docs:dev -- --host 127.0.0.1`, Playwright browser checks, `npm test`, and `npm run docs:build`.

- **Duplicate practice/solution navigation:** `Layout.vue` injects `<ExerciseNav />` globally in `#doc-after`, while 304 curriculum `index.md` files also contain `<ExerciseNav />`. Rendered evidence: `/java/oop/encapsulation/` and `/databases/indexes/` each show two "Practice Exercise" links; `/java/oop/encapsulation/exercise/` shows two "View Solution" links and two "Back to Theory" links.
- **Exercise pages can show duplicate editors:** `/java/oop/encapsulation/exercise/` renders two editor instances because the markdown page contains `<Playground files="java/oop/encapsulation/playground/" />` and the layout/playground system also resolves code for the current topic. The current `Playground.vue` does not define a `files` prop, so the markdown prop is ignored and the component falls back to route-based discovery.
- **Theory-only and schema-aware actions are not implemented:** `ExerciseNav.vue` hardcodes `hasExercise = true`, so pages that should be theory-only still advertise "Practice Exercise" if they are served through the current layout. It also detects system design with `route.path.includes('/system_design/')` instead of generated schema metadata.
- **Code mode is still bolted onto layout state:** `Layout.vue` only exposes `Read` and `Code`; it does not expose schema-aware actions such as Practice, Solution, Scenario, Design, Diagram, or Lab. Topic-level action visibility should come from generated schema metadata.
- **Eager Java loading remains a build/performance risk:** Both `Layout.vue` and `Playground.vue` use `import.meta.glob(..., { eager: true })` over the whole Java tree. `npm run docs:build` passes, but it emits large chunk warnings and logs `PLAYGROUND MOUNTED...` during SSG, showing playground discovery is executing during build/render.
- **Build artifact pollution still exists:** `npm run docs:build` recreates `src/main/java/org/example/backend_fundamentals/Users/.../docs/.vitepress/{dist,.temp}` due the current Monaco/Vite worker output behavior. This generated tree must be fixed before considering the build clean.
- **Broken rendered links were not detected by VitePress:** `ignoreDeadLinks: false` and `npm run docs:build` completed successfully. This does not prove every orphan/deep-link-only document is navigable from the UI.
- **Potential orphan/deep-link-only markdown:** There are 25 non-`index.md` topical markdown files outside `todo/`, such as `java/oop/object_model/Methods.md`, `java/oop/equals_hashcode/EqualsHashCode.md`, `networking/api_design/ApiTechnologies_Summary.md`, and `security/cryptography/TLS_HTTPS_PKI.md`. Decide whether each should become a routed topic, merge into the nearest topic `index.md`, or move under a reference/archive area.
- **Java file outside schema:** `src/main/java/org/example/backend_fundamentals/java/nested_classes/NestedClassDemo.java` sits outside a `playground/` directory and contains lesson-style JavaDoc. It should be migrated into an appropriate topic/playground or converted into markdown plus playground code.
- **Empty files/folders were not found:** No 0-byte `.md`/`.java` files and no empty directories were found under `backend_fundamentals` during this audit.

## Follow-Up Implementation Audit Findings
Audit date: 2026-07-06, after schema-aware navigation and playground hardening.

- **Generated route crawl passed:** `node scripts/site-route-audit.js` loaded all 433 generated routes at desktop viewport. Every route returned status 200, had no browser console errors, and had no page-level horizontal overflow.
- **Mobile route crawl passed:** `SITE_AUDIT_VIEWPORT=390x844 node scripts/site-route-audit.js` loaded the same 433 generated routes at mobile width with no route failures, browser console errors, or page-level horizontal overflow.
- **Core E2E journey passed:** `PLAYWRIGHT_USE_EXISTING_SERVER=1 node playwright_e2e.js` verified representative homepage/domain/topic navigation, Java theory/exercise/solution flow, system-design theory/exercise/design flow, mobile Code mode, mocked Piston success, compile error, rate limit, large-output truncation, whitelist/unavailable response handling, and route-change stale-output protection.
- **Real hosted execution is unavailable by default:** A real browser run against `https://emkc.org/api/v2/piston/execute` returned the public whitelist notice dated 2026-02-15. The UI now labels this as `Execution Unavailable`; real hosted execution requires `VITE_PISTON_EXECUTE_URL` pointing to an approved or self-hosted endpoint, or a hardened local sidecar.
- **ArchitectureBoard has no live curriculum coverage yet:** No `.drawio` files and no markdown `<ArchitectureBoard>` usages currently exist under `backend_fundamentals`. Component lifecycle code is present, but there is no real diagram page to visually validate.
- **System-design design pages are placeholders:** All 13 `system_design/**/design/index.md` files currently contain only frontmatter plus `# Design`. Treat these as curriculum stub backlog items; do not auto-fill them during architecture work.
- **Navigation smell audit:** The generated tree has 12 top-level domains, 5 one-child waypoints (`/databases/distributed_transactions/`, `/networking/api_design/`, `/performance/`, `/spring/spring_cloud/`, `/system_design/case_studies/`), and no oversized sibling groups below the root at the current threshold.

## Product UX Criteria
- A new visitor should immediately see the major curriculum domains, then progressively drill down into categories, subcategories, and final topic modules.
- Directory depth is a UX decision. Add nesting when it improves scanning and topic discovery; collapse nesting when it creates empty-feeling or one-child waypoints.
- Final topic pages should behave like learning units. Some are theory-only; others add runnable/editable examples, exercise/practice, and solution/design pages.
- Schema choice should follow learning value. Do not force exercises onto networking, security, or reference topics when a clean theory page is the better experience.
- Search should favor discovery of theory and topic entry points. Exercise, solution, and design pages are excluded from global search to reduce clutter and avoid solution spoilers.
- Generated navigation is successful only if it is readable in the browser. Passing schema validation is necessary but not sufficient.

---

### Task 0: Product Audit Before More Implementation

**Goal:** Establish what is wrong with the current branch before writing more UI or validation code.

- [ ] **Navigation Inventory**
  - Count top-level domains, max depth, largest sibling groups, and one-child categories from `navigation_map.json`.
  - Identify places where the sidebar becomes a long undifferentiated list or where a learner must drill through low-value folders.
  - Compare homepage/domain hub content against the same tree so the first-click path is obvious.

- [ ] **Current UI Review**
  - Review `docs/.vitepress/theme/Layout.vue`, `custom.css`, `Playground.vue`, `ExerciseNav.vue`, and `ArchitectureBoard.vue`.
  - Capture screenshots for desktop and mobile of: homepage, a domain hub, a deep Java topic, a theory-only topic, a system-design topic, exercise page, solution/design page, and Code mode.
  - Record UX issues before making changes: hidden navigation, tabs that appear/disappear unexpectedly, text overlap, excessive full-bleed behavior, confusing empty states, or actions that imply missing pages exist.

- [ ] **Decision Output**
  - Produce a short implementation checklist grouped by: navigation tree, layout shell, topic tabs/actions, playground/run UX, system-design UX, validation, tests.
  - Do not start implementation until this audit is reviewed.

### Task 1: Auto-generate Homepage & Enforce Schema Validation

**Files:**
- Create: `scripts/generate-homepage.js`
- Modify: `package.json`

**Validation contract:** Every add, move, rename, or structural edit under `src/main/java/org/example/backend_fundamentals/` must pass `node scripts/generate-homepage.js` before it can be served or committed. The docs dev/build scripts and Husky pre-commit should fail on invalid structure.

- [x] **Step 1: Write the generation and validation script**
The script must scan `src/main/java/org/example/backend_fundamentals` and enforce the schemas.
*Edge Cases & Rules:*
- **File System Chaos & Hierarchical Ordering:** The scanner must be recursive. It MUST assert that *every single directory* at every level (e.g., `java/`, `java/oop/`, `java/oop/encapsulation/`) contains an `index.md` with an `order: X` frontmatter. If missing, crash the build. Explicitly check `fs.statSync().isDirectory()`. Assert case-sensitive `index.md`.
- **Node.js OOM Prevention:** Use `readline` module to stream `index.md` line-by-line for validation frontmatter, terminating at the closing `---`. Do not use full-file reads or hard byte limits for metadata validation.
- **Cross-Platform Compatibility:** The script MUST explicitly normalize paths using `pathString.split(path.sep).join('/')` so that Windows backslashes do not corrupt VitePress's POSIX-based route resolution, which would cause CI to fail.
- **Security & SEO:** Run regex `/^[a-zA-Z0-9_-]+$/` on folder names.
- **Pedagogy (Schema-Aware):** Assert required children based on schema family. Interactive code modules require `exercise/` plus `solution/`; system design modules require `exercise/` plus `design/`; theory-only modules may contain only `index.md`.
- **Sidebar UX Limits:** The dynamic script MUST explicitly exclude `exercise/`, `solution/`, and `design/` directories from the generated VitePress sidebar arrays to prevent massive UI nesting. Navigation to these will be handled via in-page links.
- **Chicken-and-Egg Rule:** This hook is now enabled because the schema migration is complete.
- **Strict Build Mode:** Schema violations are fatal for `node scripts/generate-homepage.js`, docs dev/build, and pre-commit. If a non-fatal report is useful, implement it as a separate audit command for navigation smells only; do not let normal site commands serve invalid structure.
- **Global Navigation State:** Because `exercise/` and `solution/` are excluded from the sidebar, `generate-homepage.js` MUST output a `docs/.vitepress/navigation_map.json` file containing a flat array of all topics in `order`. `ExerciseNav.vue` will import this JSON to resolve Next/Prev buttons reliably on direct page refresh.
- **Schema Metadata:** Extend generated navigation data with enough metadata for the UI to know whether a topic is theory-only, interactive/code, system design, or a future schema family. UI components should not infer capabilities from URL string checks alone.
- **Current enforcement gap:** The live generator validates the major structure, recursive `index.md`, numeric `order`, duplicate sibling orders, folder-name regex, and module `exercise` plus `solution/design` presence. It permits plain leaf `index.md` pages, but it does not yet make schema choice explicit or strictly validate exact `exercise order: 10`, exact `solution/design order: 20`, `search: false`, Java-only `playground/`, or assets-only `assets/`.
- **Future UX Checks:** Add warnings or reports for navigation smells that pure schema validation cannot catch: oversized sibling lists, categories with only one child, excessive depth before reaching a topic, or top-level domains that are hidden or unclear.

- [x] **Step 2: Modify: root `package.json` & Git Hooks**
- In `scripts`, prepend `node scripts/generate-homepage.js &&` to the `dev`, `docs:dev`, and `docs:build` commands.
- Set `NODE_OPTIONS=--max_old_space_size=3072` (Limit to 3GB, not 4GB, to prevent the Linux OOM killer from terminating the 7GB GitHub Actions runner if Playwright is running concurrently).
- **Husky Pre-commit (CRITICAL):** A Husky `pre-commit` hook runs `generate-homepage.js`. This guarantees developers cannot commit malformed folders.

- [ ] **Step 3: Test Negative Validation Cases**
1. Create a dummy folder `backend_fundamentals/test_fail_no_index` without an `index.md`. Run `npm run docs:build`. **Assert it crashes.**
2. Add/keep targeted validator tests for schema families: theory-only leaf pages, interactive code modules, system design modules, exact child frontmatter, `search: false`, invalid files in `playground/`, and invalid files in `assets/`.

---

### Task 1.5: AI Curriculum Auto-Generation (Forcing Function Resolution)

**Goal:** Provide a reviewed local assistive workflow for creating real practice material when a topic genuinely needs it. This is not a CI repair loop and must not generate placeholder files just to satisfy validation.

- [x] **Step 1: Create the Generator Script/Agent**
Create a Node.js script that listens for the specific exit code (1) from `generate-homepage.js`. 
- **Local Dev Only:** This AI script MUST only run locally on the developer's machine. If it detects it is running in a CI/CD environment (e.g., `process.env.CI`), it MUST immediately exit so developers don't lose AI-generated files on ephemeral runners.
- When a developer intentionally migrates a topic into an interactive, system-design, or future practice schema and the matching child pages are missing, the wrapper script may invoke Antigravity or another reviewed local AI workflow. The current `scripts/auto-generate.js` is a scaffold/mock and must not be treated as production-quality curriculum generation.
- [ ] **Step 2: Generate Coding Exercises**
For standard coding topics, generate a practical problem statement and a solution only when the topic is intentionally using the interactive/code schema. If the topic is purely theoretical, keep or migrate it to the theory-only schema instead of generating placeholder `exercise/` and `solution/` pages.
- **Crucial Frontmatter (Spoilers):** The generated `exercise/index.md` and `solution/index.md` MUST include `search: false` in their YAML frontmatter so they are excluded from VitePress local search, preventing accidental solution spoilers or orphaned navigation.
- [ ] **Step 3: Generate System Design Exercises**
For architecture topics, generate a real-world scaling scenario, an appropriate `design/index.md` rubric, and optional diagram assets only when the topic is intentionally using the system-design schema. If the topic is introductory or conceptual, keep it theory-only instead of creating fake scenario/design pages.
- [x] **Step 4: Circuit Breaker**
Implement `MAX_RETRIES = 3` to prevent infinite AI generation loops and zombie token billing if the generator repeatedly fails schema validation.

---

### Task 2: Dynamic Navigation in VitePress

**Files:**
- Modify: `docs/.vitepress/config.mjs`

- [x] **Step 1: Replace hardcoded nav and sidebar**
- [x] **Step 2: Update `docs/.vitepress/config.mjs`**
- Import `docs/.vitepress/navigation_map.json`, which is generated by `generate-homepage.js`.
- Pass the dynamic values to `themeConfig.sidebar`.
- **CRITICAL VitePress Route Resolution:** Configure `srcDir: '../src/main/java/org/example/backend_fundamentals'` from `docs/.vitepress/config.mjs` so VitePress knows where the markdown files live.
- **CRITICAL VitePress Memory Leak:** You MUST configure `srcExclude: ['**/*.java', '**/target/**']` so VitePress doesn't crash trying to parse source code as static assets.
- **HMR Warning:** Be aware that adding new folders or changing `order: X` may require restarting the `npm run docs:dev` server because VitePress config imports generated JSON at startup.
Use the generated JSON to build `themeConfig.sidebar` and a single top-level `Curriculum` dropdown. Explicitly set `build.sourcemap = false` if CI memory becomes a problem.

---

### Task 3: Interactive Components (Local Java Runner & Draw.io Viewer)

**Files:**
- Modify: `docs/.vitepress/theme/components/Playground.vue`
- Modify: `docs/.vitepress/theme/components/CodeEditor.vue`
- Create: `docs/.vitepress/theme/components/ArchitectureBoard.vue`

- [x] **Step 1: CodeEditor.vue & Playground.vue**
Create a Vue component (`<Playground>`) that uses `@guolao/vue-monaco-editor`.
- **SSG Crash Prevention (CRITICAL):** Monaco Editor requires browser `window` APIs. You MUST wrap the component usage in `<ClientOnly>` inside the markdown, or dynamically import the component only on the client, otherwise the VitePress Node.js build will instantly crash.
- **Web Worker Vite Config:** The current config uses `vite-plugin-monaco-editor` to bundle Monaco worker files.
- **Current worker-output bug:** The current Monaco plugin/config writes worker bundles into an absolute-path mirror under the VitePress `srcDir` during `npm run docs:build` (`backend_fundamentals/Users/.../docs/.vitepress/...`). Treat this as a build hygiene bug: generated worker files must not be emitted under curriculum content.
- Implements a UI wrapper simulating a standard IDE (Run button, Console Output).
- On click, issues an HTTP `POST` fetch to `/api/run-java` in local dev mode. `VITE_PISTON_EXECUTE_URL` remains optional for a separately approved hosted sandbox.
- The component receives the raw file path via a Vue prop. **Vite Build Fix:** You MUST use `import.meta.glob('.../*.java', { query: '?raw' })` to dynamically load the file contents. Do NOT use standard dynamic `import(prop)` as Vite cannot statically analyze it.
- **Vite Bundle Bloat (CRITICAL):** Remove `eager: true` from the `import.meta.glob` call. Use dynamic async imports to prevent SSG heap crashes and massive client JS payloads.
- **Dark Mode Sync:** Import `useData` from `vitepress`. Watch `useData().isDark` and dynamically bind the editor theme so the editor follows site color mode.
- **Component Reuse Trap:** When `$route` changes, DO NOT mount a new editor instance. Call `.setValue(newCode)` on the existing editor. 
- **SPA Memory Leaks & Race Conditions:** Explicitly destroy the editor (`view.destroy()`) in `onBeforeUnmount`. Because `onBeforeUnmount` is bypassed on component reuse, the `$route` watcher MUST also `.abort()` any pending network requests and clear previous state. Parse `Retry-After` on HTTP 429 and disable the Run button.
- **Security (XSS Prevention):** The execution output returned from the runner MUST be rendered strictly using Vue text interpolation (`{{ output }}`) or `v-text`. Under no circumstances should `v-html` be used, as it would expose the site to Self-XSS if a user `System.out.println`s a malicious script tag.
- **Current implementation risk:** `Playground.vue` currently still uses an eager Java glob. Fix this before expanding the Java playground corpus or treating docs build memory as fully hardened.
- **Layout implementation risk:** `Layout.vue` also eagerly globs Java files to decide whether to show Code mode. The final design should avoid loading the whole Java corpus just to render a page shell.
- **Tab UX requirement:** Replace the current two-mode Read/Code model with schema-aware topic actions. Tabs/buttons should show only valid destinations for the current topic and should be stable on refresh.
- **Run UX requirement:** The run panel should distinguish running, success, compile/runtime error, timeout, rate limit, aborted request, and truncated output. It should avoid setting aborted output after a newer run starts.
- **Compliance warning requirement:** The run panel must display a persistent warning near the Run control. Local mode warns not to run untrusted code; hosted mode warns not to submit proprietary code, API keys, credentials, or PII.
- **Editor disposal requirement:** The implementation must verify that the underlying Monaco editor and model are disposed on unmount or replacement. Aborting network requests alone is not enough to satisfy the memory-safety requirement.

- [x] **Step 1.1: Java Execution Mode Decision**
Current mode is local study execution in Vite dev through `/api/run-java`. It compiles edited Java playground files in a temp directory with `javac` and runs the selected `main` with `java`. It deliberately avoids Maven, shell command strings, and save endpoints. Hosted/static execution remains optional and requires a separately approved sandbox endpoint.

- [x] **Step 1.2: Local Runner Requirements**
- Endpoint shape: `POST /api/run-java { mainClass, files }` runs only in Vite dev.
- Validate main class with a strict Java-name regex.
- Validate Java file names and infer package directories from source content.
- Cap request body size, per-file source size, file count, runtime, and stdout/stderr buffers.
- Use temp directories under the OS temp root and remove them after every run.
- Return structured `{ ok, phase, stdout, stderr, error }`.
- No file-save endpoint is exposed.

- [x] **Step 1.3: Memory And Lifecycle Requirements**
- Dispose Monaco editor and model resources on unmount.
- Abort pending run/save/Piston/sidecar requests on route change and unmount.
- Clear rate-limit countdowns, elapsed timers, timeout timers, and delayed iframe reloads.
- Ignore stale completions using run IDs or abort signals so older output cannot overwrite newer state.
- Cap output both client-side and server-side.
- Detach/destroy `medium-zoom` and iframe message listeners on route changes and unmount.

- [x] **Step 2: ArchitectureBoard.vue (Memory Leak Patches)**
Create a Vue component that uses an `<iframe>` to embed a Draw.io SVG. 
- **Dark Mode Sync:** Import `useData().isDark`. Dynamically append `&ui=dark` or `&ui=kennedy` to the Draw.io iframe URL so the diagram background matches the site theme.
- Store the event listener function reference and call `window.removeEventListener('message', handlerReference)` in `onBeforeUnmount`.
- Perform the "about:blank Flush" (`iframeRef.value.src = 'about:blank'`) to forcefully garbage collect the iframe's internal JS context.
- Because of component reuse, implement a `$route` watcher to trigger the about:blank flush and reset listeners when navigating between architecture topics. **CRITICAL:** The watcher MUST remove the *previous* event listener before attaching the new one, or global event listeners will exponentially stack.

- [x] **Step 2.5: ExerciseNav.vue (a11y & Navigation)**
Create a Vue component to fix the "Trapdoor Effect" for pages excluded from the sidebar.
- Since `exercise/` and `solution/` are not in the global VitePress sidebar, VitePress will NOT generate Next/Prev footer links for them.
- This component MUST dynamically read the current route and render accessible `<nav aria-label="Pagination">` links (e.g., Back to Theory, View Solution, Next Topic).
- Topic navigation is owned by the VitePress layout. AI curriculum generation MUST NOT inject `<ExerciseNav />` into markdown pages; doing so reintroduces duplicate actions and splits navigation ownership.
- **Current implementation risk:** `ExerciseNav.vue` currently assumes every topic has an exercise and uses URL string checks for system design. Replace this with generated schema metadata so theory-only topics do not show invalid actions.

### Task 4: AI Governance Rules & Cleanup

- [x] **Step 1: Write AGENTS.md**
Add durable LMS rules to root `AGENTS.md`: forbid manual nav edits, define schemas, and explicitly forbid empty placeholder folders/files. `.agents/AGENTS.md` also exists as older architecture-governance context.

- [x] **Step 2: Cleanup Obsolete Files**
Delete `scrape.js`, `check-site.js`, etc.

---

### Task 5: Playwright E2E Suite (Resilience & Memory QA)

Create `playwright_e2e.js` at the repository root.
- **CI/CD Prerequisites:** Ensure your CI pipeline runs `npx playwright install --with-deps` before executing this script, or the tests will crash due to missing browser binaries.
- **Wait-On Boot:** The script MUST use `wait-on` (or Playwright's `webServer` config) to block test execution until the dev server is fully listening, preventing `Connection Refused` race conditions.
- Target URL: `http://localhost:5173/java/oop/encapsulation/`
- [ ] **Step 1: Write the Playwright Test Suite**
The script must boot the dev server and test:
1. **Homepage Domain Discovery:** Verify a first-time visitor can see and open major domains from the homepage and Curriculum navigation.
2. **Domain Hub Drilldown:** Verify representative domains expose meaningful category links and do not depend only on the sidebar.
3. **Sidebar Active State:** Verify a deep topic highlights the active branch and does not show exercise/solution/design as noisy nested sidebar entries.
4. **Hierarchy Drilldown:** Verify a representative domain can drill down through categories to a final topic without dead ends or confusing empty pages.
5. **Theory-Only Topic:** Verify a theory-only topic shows no fake Practice/Solution actions.
6. **Interactive Topic Journey:** Verify a code topic exposes Read, Code, Practice, and Solution actions where applicable.
7. **System Design Topic Journey:** Verify a system-design topic exposes Read, Scenario, Design, and optional Diagram actions where applicable.
8. **Direct Refresh:** Open theory, exercise, solution, and design URLs directly; verify the right actions and back-links render.
9. **Mobile Layout:** Test homepage, sidebar/menu, topic tabs, code mode, tables, and diagrams at a mobile viewport with no incoherent overlap.
10. **Architecture Board:** Verify iframe renders successfully and route changes reset listeners/iframe state.
11. **Java Positive:** Inject code, intercept `/api/run-java` with `page.route()`, return mock success payload, and separately run one real browser execution through the local JDK.
12. **Java Negative (Compile/Runtime Fail):** Return mock compiler/runtime error payloads, assert UI distinguishes them from successful stdout.
13. **Java Negative (Rate Limit):** Return HTTP 429, assert UI shows rate-limit warning and disables Run for the parsed retry window.
14. **Resilience (Rapid Click):** Click Run 5 times rapidly. Intercept requests and assert stale requests are aborted or ignored.
15. **Resilience (Timeout):** Mock the runner with a 30s delay. Assert UI handles timeout gracefully instead of hanging.
16. **Resilience (OOM Truncation):** Mock a 50k character runner response. Assert UI truncates it to 10k.
17. **Route Change During Run:** Start a long run, navigate away, and assert no stale output lands on the new page.
18. **Lifecycle Leak Smoke:** Repeatedly switch Read/Code/Practice/Solution tabs and navigate across topics; assert no duplicate console output, duplicate message handling, or visible stale state.
19. **Diagram Lifecycle:** Navigate between two architecture boards and toggle dark mode; assert the iframe reloads correctly and old message listeners do not duplicate loads.
20. **Local Runner Security (if enabled):** Assert invalid FQCN, non-local Origin/Host, oversized body, timeout, disconnect, out-of-tree save, non-Java save, and symlink escape all fail safely.

- [ ] **Step 2: Run QA Loop until Green**
The agent MUST loop on `node playwright_e2e.js` until all UI tests pass.

- [ ] **Step 3: CLI Validation Testing (Task 6)**
Create a separate Node script (e.g. `test-validation.js`) that uses `child_process.execSync` to run `generate-homepage.js` against temporary mocked directories.
1. **Curriculum E2E Test:** Mock missing required children for an interactive module and a system design module, and ensure the script exits with status 1.
2. **Structural Negative Tests (Dangling & Malformed):** 
    - *Missing Index:* Mock a folder without an `index.md`. Assert exit 1.
    - *Missing Order:* Mock an `index.md` missing the YAML `order:` tag. Assert exit 1.
    - *Invalid Order:* Mock an `index.md` with a non-numeric order (e.g., `order: abc`). Assert exit 1.
    - *Duplicate Order:* Mock two sibling folders that share the exact same `order: 10`. Assert exit 1.
    - *Invalid Folder Name:* Mock a folder with spaces or special characters (e.g., `invalid folder!`). Assert exit 1.
    - *Empty File:* Mock an `index.md` that is completely 0 bytes. Assert exit 1.
    - *Theory-Only Module:* Mock a valid leaf directory with only `index.md` and `order: X`. Assert exit 0.
    - *Invalid Child Frontmatter:* Mock `exercise/index.md` without `search: false` or with an order other than `10`, and mock `solution/index.md` or `design/index.md` with an order other than `20`. Assert exit 1 once strict child-frontmatter validation is implemented.
    - *Invalid Schema Contents:* Mock `.md` files under `playground/` and non-image/non-`.drawio` files under `assets/`. Assert exit 1 once strict directory-content validation is implemented.

---

### Task 6: Final Peer Review & Human Handoff

- [ ] **Step 1: Code Review**
Review newly written code for behavioral regressions, schema gaps, and missing validation before asking for a commit.
- [ ] **Step 2: Review Gate Sign-Off**
Confirm the Required Review Gates that apply to the implemented changes have been completed or explicitly marked not applicable with a reason.
- [ ] **Step 3: Request Human Permission to Commit**
Do not run `git commit` until human permission is granted.

---

### Task 7: Deferred Curriculum Stub Cleanup

**Status:** Do not start unless the user explicitly asks for this task.

**Goal:** Review and fix the placeholder curriculum pages created only to satisfy the old strict schema. Use [2026-07-05-curriculum-stub-backlog.md](./2026-07-05-curriculum-stub-backlog.md) as the inventory.

- [ ] Review the 294 candidate stubs found by the read-only Antigravity scan:
  - 134 `exercise/index.md` files.
  - 122 `solution/index.md` files.
  - 13 `design/index.md` files.
  - 25 low-content or placeholder theory/index files.
- [ ] For each topic, decide whether to create real practice/design content or migrate the topic to theory-only / another appropriate schema family.
- [ ] Do not bulk-generate filler. Exercise, solution, and design pages should exist only when they add learning value.
- [ ] After each cleanup batch, run `node scripts/generate-homepage.js` and update schema/navigation tests as needed.
