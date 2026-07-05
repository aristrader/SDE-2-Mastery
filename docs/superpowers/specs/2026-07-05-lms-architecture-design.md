# LMS Architecture Design Specification

## Overview
This document specifies the architecture for transforming the static documentation site into a fully automated, interactive Learning Management System (LMS). It eliminates manual routing updates, restores secure interactive code execution, defines strict module schemas, implements build-time validation, and mandates strict automated QA and code review workflows.

## 1. Zero-Touch Automation & Navigation
To eliminate manual routing updates and ensure no topics are missed silently:
- **Dynamic VitePress Configuration**: `docs/.vitepress/config.mjs` will utilize `fs.readdirSync` to dynamically scan `src/main/java/org/example/backend_fundamentals`. Top-level directories will automatically populate the Nav dropdowns and Sidebar arrays.
  - *Fallback Rule:* To prevent erratic routing, any `order: X` collisions will fallback to a secondary alphabetical sort.
- **Dynamic Homepage Features**: A Node.js script (`scripts/generate-homepage.js`) will parse the directories to generate the feature grid YAML.
- **NPM Hooks**: The generation script will be bound to `predev` and `prebuild` in `package.json`.

## 2. Specialized Interactive Components
- **Secure Code Execution (`<Playground>`)**: The existing `Playground.vue` component will be unlocked from read-only mode. Execution will be offloaded securely to the public **Piston Execution API**.
  - *Resilience & Vue Reactivity Rules:* Must actively `.abort()` previous requests on rapid consecutive clicks. MUST prevent submission if the code editor is empty. 
  - *Component Reuse Trap:* When Vue reuses the component on route changes, `onBeforeUnmount` is bypassed. Therefore, the `watch` on `$route` must explicitly `.abort()` any pending network requests, clear previous output state, and call `.setValue(newCode)` on the reused editor instance.
  - *Rate Limiting Rule:* Parse the `Retry-After` header on 429 responses and physically disable the Run button until the window expires.
  - *OOM Protection (Build & Client-Side):* The `import.meta.glob` call for Java files MUST NOT use `eager: true`. Eager evaluation inlines the entire Java repository into the JS bundle, exhausting the Vite build heap and crippling client RAM. It must use dynamic async imports or runtime HTTP fetches.
  - *OOM Protection (Execution):* Truncate the Piston output string to 10,000 characters.
  - *SPA Memory Leak Rule (Code Editor):* The Code Editor instance MUST be explicitly destroyed (`view.destroy()` for CodeMirror, or `editor.dispose()` and `model.dispose()` for Monaco) in `onBeforeUnmount`. 
  - *Compliance & Legal Rule:* Must display a persistent UI warning adjacent to the Run button explicitly forbidding the submission of proprietary code, API keys, or PII.
- **System Design Viewer (`<ArchitectureBoard>`)**: A new Vue component will embed interactive Draw.io files. 
  - *SPA Memory Leak Rule (Iframes):* Must explicitly `window.removeEventListener('message', handlerReference)` in `onBeforeUnmount`. You cannot just pass the string `'message'`; you must store the exact function reference.
  - *Component Reuse Trap:* Similar to the Code Editor, Vue will reuse this component. You MUST bind a `watch` on `$route` (or `props.src`) to perform the "about:blank Flush" (`iframeRef.value.src = 'about:blank'`) and reset the event listener before loading the new iframe source.
- **Global SPA Memory Rules:** Any use of `medium-zoom` must explicitly call `.detach()` or `.destroy()` on route changes to prevent DOM detachement memory leaks.

## 3. Strict Module Schemas
Every topic must be scaffolded as a self-contained module directory. 
- *Security & SEO Rule:* Folder names must strictly be alphanumeric, underscores, or hyphens (`^[a-zA-Z0-9_-]+$`). Using hyphens (`-`) is strongly preferred for SEO URL parsing (e.g. `system-design` instead of `system_design`).
- *Pedagogical Rule:* All modules MUST contain an `exercise/index.md` and `solution/index.md` file. The build MUST fail if they are missing. This serves as a strict forcing function to prevent incomplete curriculum.
- *AI Curriculum Generation Rule:* When the build fails due to missing exercises, an AI agent must be dispatched to read the theory `index.md` and auto-generate the missing content. 
  - **Governance Requirement:** AI generation must be completely decoupled from the CI pipeline to prevent unreviewed hallucinations entering production. It must be executed locally by a developer (e.g., via a script) and pass human peer review before commit.
  - **Circuit Breaker:** The AI must have a hard limit of 3 retries (`MAX_RETRIES = 3`) to prevent infinite generation loops if it repeatedly hallucinates malformed markdown.
  - **Coding:** Generate a problem and solution, or explicitly write "No coding exercise required."
  - **System Design:** Generate a real-world Scenario, a blank `<ArchitectureBoard>`, and a Grading Rubric, or explicitly write "No architecture exercise required."

**Schema A: Executable Code (e.g., Java)**
```text
[topic_name]/
├── index.md           # Theory & Concepts (Requires 'order: X' frontmatter)
├── playground/        # Standalone Java files loaded into <JavaPlayground>
├── exercise/          
│   └── index.md       # Coding problem statement & hints
└── solution/          
    └── index.md       # Explanation and takeaways
```

**Schema B: System Design**
```text
[system_design_topic]/
├── index.md                # Problem Statement & Capacity Estimation (Requires 'order: X')
├── design/                 # Reference architecture (api.md, data.md)
├── exercise/               
│   └── index.md            # Structured empty markdown template for learner
└── assets/                 # Raw .drawio files and .png exports
```

## 4. Progressive Curriculum Sequencing
- Sequencing is driven purely by YAML frontmatter `order: X` placed in the `index.md` of every folder. Increments of 10 are mandatory.

## 5. Build-Time Schema Validation & AI Governance
- **Environment-Aware Validation**: The `generate-homepage.js` script will act as a strict schema validator. 
  - *Dev Mode (`predev`):* Throws a yellow WARNING in the console.
  - *Prod Mode (`prebuild`):* Throws a fatal Error (`process.exit(1)`).
- **Node.js Memory Leak Prevention**: The generation script MUST use Node's `readline` module to stream `index.md` line-by-line, stopping the stream immediately after hitting the closing `---` to extract YAML frontmatter. Reading entire files via `fs.readFileSync(..., 'utf8')` will exhaust the V8 heap as the curriculum scales. Using a hard byte limit (e.g., 500 bytes) risks truncating long frontmatter and crashing the parser.
- **File System Resilience & Hierarchical Ordering**: The scanner MUST be recursive. For *every single directory* at every level (domain, category, module), the script MUST assert the existence of an `index.md` file containing the `order: X` YAML frontmatter. If any directory in the curriculum lacks an `index.md` or an `order` tag, the build MUST crash. This guarantees flawless navigation sorting. It must also explicitly check `fs.statSync().isDirectory()` to avoid `ENOTDIR` crashes from stray root files (like `diagram.png`), and must use case-sensitive assertions for `index.md` to prevent Linux CI/CD failures.

## 6. Legacy Cleanup
All obsolete scripts, deprecated layout components, and hardcoded routing mappings will be permanently deleted.

## 7. Mandatory Automated QA & Negative Testing (Playwright)
Before any human review is requested, a Playwright E2E script must verify the holistic health of the application. 
- **API Mocking Rule:** E2E scripts must use Playwright Network Interception (`page.route()`) to mock Piston API responses.

## 8. Final Peer Review & Git Protocol
- **No Unapproved Commits**: Agents are strictly forbidden from executing `git commit` at any stage without explicit human authorization.
