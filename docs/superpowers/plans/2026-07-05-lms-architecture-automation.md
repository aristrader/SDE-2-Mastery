# LMS Architecture Automation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. 
> **CRITICAL RULE:** DO NOT commit anything to Git until the entire plan is completed, the automated QA passes, Review Agents have checked the code, and the human gives explicit permission to commit.

**Goal:** Automate the VitePress navigation, restore secure Java execution (Piston API), enforce build-time validation, build the Draw.io embedder, and test all edge cases.

---

### Task 1: Auto-generate Homepage & Enforce Schema Validation

**Files:**
- Create: `scripts/generate-homepage.js`
- Modify: `package.json`

- [ ] **Step 1: Write the generation and validation script**
The script must scan `src/main/java/org/example/backend_fundamentals` and enforce the schemas.
*Edge Cases & Rules:*
- **File System Chaos & Hierarchical Ordering:** The scanner must be recursive. It MUST assert that *every single directory* at every level (e.g., `java/`, `java/oop/`, `java/oop/encapsulation/`) contains an `index.md` with an `order: X` frontmatter. If missing, crash the build. Explicitly check `fs.statSync().isDirectory()`. Assert case-sensitive `index.md`.
- **Node.js OOM Prevention:** Use `readline` module to stream `index.md` line-by-line to parse frontmatter, terminating the stream at the closing `---`. Do not use `fs.readFileSync` or hard byte limits.
- **Cross-Platform Compatibility:** The script MUST explicitly normalize paths using `pathString.split(path.sep).join('/')` so that Windows backslashes do not corrupt VitePress's POSIX-based route resolution, which would cause CI to fail.
- **Security & SEO:** Run regex `/^[a-zA-Z0-9_-]+$/` on folder names.
- **Pedagogy (Strict):** Assert that `exercise/` and `solution/` folders exist and contain their own `index.md` files. Throw a fatal error if missing.
- **Sidebar UX Limits:** The dynamic script MUST explicitly exclude `exercise/` and `solution/` directories from the generated VitePress sidebar arrays to prevent massive 5-level deep UI nesting. Navigation to these will be handled via in-page links.
- **Chicken-and-Egg Rule:** DO NOT hook this validation script into `package.json` `prebuild` or `predev` until the ENTIRE `schema-migration-plan.md` has been successfully executed, otherwise it will instantly crash on legacy unmigrated folders.
- **DX Build Modes:** Check `process.env.NODE_ENV`. `predev` throws a yellow `WARNING`. `prebuild` throws a fatal `Error`.
- **Global Navigation State:** Because `exercise/` and `solution/` are excluded from the sidebar, `generate-homepage.js` MUST output a `docs/.vitepress/navigation_map.json` file containing a flat array of all topics in `order`. `ExerciseNav.vue` will import this JSON to resolve Next/Prev buttons reliably on direct page refresh.

- [ ] **Step 2: Modify: `docs/package.json` & Git Hooks**
- In `scripts`, prepend `node scripts/generate-homepage.js &&` to the `dev` and `build` commands (e.g., `"prebuild": "node scripts/generate-homepage.js"`).
- Set `NODE_OPTIONS=--max_old_space_size=3072` (Limit to 3GB, not 4GB, to prevent the Linux OOM killer from terminating the 7GB GitHub Actions runner if Playwright is running concurrently).
- **Husky Pre-commit (CRITICAL):** Add a Husky `pre-commit` hook that runs `generate-homepage.js`. This guarantees developers cannot commit malformed folders. *However, do NOT install this hook until the schema migration is 100% complete, or it will block iterative migration commits.*

- [ ] **Step 3: Test Negative Validation Cases**
1. Create a dummy folder `backend_fundamentals/test_fail_no_index` without an `index.md`. Run `npm run docs:build`. **Assert it crashes.**

---

### Task 1.5: AI Curriculum Auto-Generation (Forcing Function Resolution)

**Goal:** Resolve the build failures caused by the strict schema validation by auto-generating missing exercises.

- [ ] **Step 1: Create the Generator Script/Agent**
Create a Node.js script that listens for the specific exit code (1) from `generate-homepage.js`. 
- **Local Dev Only:** This AI script MUST only run locally on the developer's machine. If it detects it is running in a CI/CD environment (e.g., `process.env.CI`), it MUST immediately exit so developers don't lose AI-generated files on ephemeral runners.
- When `generate-homepage.js` fails due to a missing `exercise/index.md` or `solution/index.md`, the wrapper script invokes the Antigravity CLI or Python SDK.
- [ ] **Step 2: Generate Coding Exercises**
For standard coding topics, generate a practical problem statement and a solution. If purely theoretical, output: *"No coding exercise required,"* but you MUST still generate the physical `.md` files with valid `order` and `search: false` YAML frontmatter, otherwise the build validation will enter an infinite loop.
- **Crucial Frontmatter (Spoilers):** The generated `exercise/index.md` and `solution/index.md` MUST include `search: false` in their YAML frontmatter so they are excluded from VitePress local search, preventing accidental solution spoilers or orphaned navigation.
- [ ] **Step 3: Generate System Design Exercises**
For architecture topics, generate a real-world scaling scenario, embed a blank `<ArchitectureBoard src="blank.drawio" />`, and generate a Grading Rubric in the solution file. If purely introductory, output: *"No architecture exercise required."*
- [ ] **Step 4: Circuit Breaker**
Implement `MAX_RETRIES = 3` to prevent infinite AI generation loops and zombie token billing if the generator repeatedly fails schema validation.

---

### Task 2: Dynamic Navigation in VitePress

**Files:**
- Modify: `docs/.vitepress/config.mjs`

- [ ] **Step 1: Replace hardcoded nav and sidebar**
- [ ] **Step 2: Update `docs/.vitepress/config.mjs`**
- Import and execute `generate-homepage.js` output.
- Pass the dynamic values to `themeConfig.sidebar`.
- **CRITICAL VitePress Route Resolution:** You MUST configure `srcDir: '../../src/main/java/org/example/backend_fundamentals'` so VitePress knows where the markdown files live (otherwise it defaults to `docs/` and 404s).
- **CRITICAL VitePress Memory Leak:** You MUST configure `srcExclude: ['**/*.java', '**/target/**']` so VitePress doesn't crash trying to parse source code as static assets.
- **HMR Warning:** Be aware that adding new folders or changing `order: X` will require restarting the `npm run docs:dev` server, as VitePress `config.mjs` does not natively HMR synchronous `fs` calls.**
Use `fs.readdirSync` to dynamically generate `dynamicSidebar` and `dynamicNavItems`. Explicitly set `build.sourcemap = false` to save CI memory.

---

### Task 3: Interactive Components (Piston API & Draw.io Embed)

**Files:**
- Modify: `docs/.vitepress/theme/components/Playground.vue`
- Modify: `docs/.vitepress/theme/components/CodeEditor.vue`
- Create: `docs/.vitepress/theme/components/ArchitectureBoard.vue`

- [ ] **Step 1: CodeEditor.vue & Playground.vue**
Create a Vue component (`<Playground>`) that uses `@guolao/vue-monaco-editor`.
- **SSG Crash Prevention (CRITICAL):** Monaco Editor requires browser `window` APIs. You MUST wrap the component usage in `<ClientOnly>` inside the markdown, or dynamically import the component only on the client, otherwise the VitePress Node.js build will instantly crash.
- **Web Worker Vite Config:** The `docs/.vitepress/config.mjs` MUST be updated with Vite configuration (e.g., using `vite-plugin-monaco-editor` or explicit worker loader mapping) to properly bundle Monaco's `editor.worker.js`. Otherwise, syntax highlighting will silently fail with 404s.
- Implements a UI wrapper simulating a standard IDE (Run button, Console Output).
- On click, issues an HTTP `POST` fetch to the public Piston API.
- The component receives the raw file path via a Vue prop. **Vite Build Fix:** You MUST use `import.meta.glob('.../*.java', { query: '?raw' })` to dynamically load the file contents. Do NOT use standard dynamic `import(prop)` as Vite cannot statically analyze it.
- **Vite Bundle Bloat (CRITICAL):** Remove `eager: true` from the `import.meta.glob` call. Use dynamic async imports to prevent SSG heap crashes and massive client JS payloads.
- **Dark Mode Sync:** Import `useData` from `vitepress`. Watch `useData().isDark` and dynamically bind the Monaco editor theme (`vs-dark` vs `vs`) so the editor doesn't blind the user when the site is in dark mode.
- **Component Reuse Trap:** When `$route` changes, DO NOT mount a new editor instance. Call `.setValue(newCode)` on the existing editor. 
- **SPA Memory Leaks & Race Conditions:** Explicitly destroy the editor (`view.destroy()`) in `onBeforeUnmount`. Because `onBeforeUnmount` is bypassed on component reuse, the `$route` watcher MUST also `.abort()` any pending network requests and clear previous state. Parse `Retry-After` on HTTP 429 and disable the Run button.
- **Security (XSS Prevention):** The execution output returned from the Piston API MUST be rendered strictly using Vue text interpolation (`{{ output }}`) or `v-text`. Under no circumstances should `v-html` be used, as it would expose the site to Self-XSS if a user `System.out.println`s a malicious script tag.

- [ ] **Step 2: ArchitectureBoard.vue (Memory Leak Patches)**
Create a Vue component that uses an `<iframe>` to embed a Draw.io SVG. 
- **Dark Mode Sync:** Import `useData().isDark`. Dynamically append `&ui=dark` or `&ui=kennedy` to the Draw.io iframe URL so the diagram background matches the site theme.
- Store the event listener function reference and call `window.removeEventListener('message', handlerReference)` in `onBeforeUnmount`.
- Perform the "about:blank Flush" (`iframeRef.value.src = 'about:blank'`) to forcefully garbage collect the iframe's internal JS context.
- Because of component reuse, implement a `$route` watcher to trigger the about:blank flush and reset listeners when navigating between architecture topics. **CRITICAL:** The watcher MUST remove the *previous* event listener before attaching the new one, or global event listeners will exponentially stack.

- [ ] **Step 2.5: ExerciseNav.vue (a11y & Navigation)**
Create a Vue component to fix the "Trapdoor Effect" for pages excluded from the sidebar.
- Since `exercise/` and `solution/` are not in the global VitePress sidebar, VitePress will NOT generate Next/Prev footer links for them.
- This component MUST dynamically read the current route and render accessible `<nav aria-label="Pagination">` links (e.g., Back to Theory, View Solution, Next Topic).
- The AI Curriculum Auto-Generation agent MUST inject this `<ExerciseNav />` component into every generated `exercise/index.md` and `solution/index.md` file.

### Task 4: AI Governance Rules & Cleanup

- [ ] **Step 1: Write AGENTS.md**
Add rules to `.agents/AGENTS.md` forbidding manual nav edits, defining schemas, and explicitly forbidding the creation of empty placeholder folders/files.

- [ ] **Step 2: Cleanup Obsolete Files**
Delete `scrape.js`, `check-site.js`, etc.

---

### Task 5: Playwright E2E Suite (Resilience & Memory QA)

Create `playwright_e2e.js` at the repository root.
- **CI/CD Prerequisites:** Ensure your CI pipeline runs `npx playwright install --with-deps` before executing this script, or the tests will crash due to missing browser binaries.
- **Wait-On Boot:** The script MUST use `wait-on` (or Playwright's `webServer` config) to block test execution until the dev server is fully listening, preventing `Connection Refused` race conditions.
- Target URL: `http://localhost:5173/java/oop/encapsulation/playground`
- [ ] **Step 1: Write the Playwright Test Suite**
The script must boot the dev server and test:
1. **Dynamic Nav:** Click Next/Prev sequentially.
2. **Architecture Board:** Verify iframe renders successfully.
3. **Java Positive:** Inject code, intercept request with `page.route()`, return mock success payload.
4. **Java Negative (Syntax Fail):** Return mock compiler error payload, assert UI handles it.
5. **Java Negative (Rate Limit):** Return HTTP 429, assert UI shows rate limit warning.
6. **Resilience (Rapid Click):** Click Run 5 times rapidly. Intercept requests and assert the first 4 were `.abort()`ed.
7. **Resilience (Timeout):** Mock Piston API with a 30s delay. Assert UI handles timeout gracefully instead of hanging.
8. **Resilience (OOM Truncation):** Mock a 50k character Piston response. Assert UI truncates it to 10k.
8. **Resilience (OOM Truncation):** Mock a 50k character Piston response. Assert UI truncates it to 10k.

- [ ] **Step 2: Run QA Loop until Green**
The agent MUST loop on `node playwright_e2e.js` until all UI tests pass.

- [ ] **Step 3: CLI Validation Testing (Task 6)**
Create a separate Node script (e.g. `test-validation.js`) that uses `child_process.execSync` to run `generate-homepage.js` against temporary mocked directories.
1. **Curriculum E2E Test:** Mock a missing `exercise/` folder and ensure the script exits with status 1.
2. **Structural Negative Tests (Dangling & Malformed):** 
    - *Missing Index:* Mock a folder without an `index.md`. Assert exit 1.
    - *Missing Order:* Mock an `index.md` missing the YAML `order:` tag. Assert exit 1.
    - *Invalid Order:* Mock an `index.md` with a non-numeric order (e.g., `order: abc`). Assert exit 1.
    - *Duplicate Order:* Mock two sibling folders that share the exact same `order: 10`. Assert exit 1.
    - *Invalid Folder Name:* Mock a folder with spaces or special characters (e.g., `invalid folder!`). Assert exit 1.
    - *Empty File:* Mock an `index.md` that is completely 0 bytes. Assert exit 1.

---

### Task 6: Final Peer Review & Human Handoff

- [ ] **Step 1: Spawn Review Agents**
Spawn a subagent (Code Reviewer) to inspect the newly written code.
- [ ] **Step 2: Request Human Permission to Commit**
Do not run `git commit` until human permission is granted.
