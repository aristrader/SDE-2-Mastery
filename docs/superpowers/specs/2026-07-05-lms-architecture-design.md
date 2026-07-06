# LMS Architecture Design Specification

## Overview
This document specifies the architecture for transforming the static documentation site into a fully automated, interactive Learning Management System (LMS). It eliminates manual routing updates, restores secure interactive code execution, defines strict module schemas, implements build-time validation, and mandates strict automated QA and code review workflows.

**Current status:** This spec is historical design context for the `chore/schema-migration` branch. When this document conflicts with the live repository, prefer root `AGENTS.md`, `scripts/generate-homepage.js`, `docs/.vitepress/config.mjs`, and `package.json`. `.agents/AGENTS.md` is older architecture-governance context; root `AGENTS.md` is the primary agent instruction file.

## Product Goal
The site should feel like a readable curriculum, not a file dump. A first-time visitor should quickly understand the major areas available, such as Java, Spring, System Design, Databases, Messaging, Security, Deployments, Performance, and Engineering Practice. From there, the visitor should be able to move through a deliberate hierarchy of domains, categories, subcategories, and final topic pages without guessing where content lives.

The folder hierarchy is therefore part of the product surface. Too little nesting flattens unrelated topics into a long list; too much nesting turns navigation into a maze. Each directory level should represent a meaningful learning decision: domain, category, subcategory, or topic. A topic should be a self-contained learning unit with theory, optional runnable/editable Java examples, a practice/exercise page, and a solution or design page.

Navigation and schema automation exist to protect that experience. The goal is not simply to enforce folders; it is to make the curriculum discoverable, keep page roles predictable, and prevent missing or orphaned learning material.

## 1. Curriculum Information Architecture
- **Top-level domains:** The first navigation layer should expose the broad curriculum map: Java, Spring, System Design, Databases, Messaging, and similar major areas.
- **Intermediate levels:** Categories and subcategories should exist only when they reduce scanning cost for the learner. They should group related topics into a clear mental model, not mirror arbitrary filesystem history.
- **Topic modules:** Final topic directories should be the normal place where study happens. They own their theory page and any supporting exercise, solution/design, assets, and Java playground files.
- **Depth guideline:** Prefer the shallowest hierarchy that keeps sibling lists readable. If one page contains too many unrelated siblings, introduce a category. If a category only contains one meaningful child and adds no clarity, consider collapsing it during future restructures.
- **Navigation quality bar:** Generated navigation is not enough by itself. The browser experience must make the domain map visible, keep sidebar groups scannable, and make the next meaningful learner action obvious.

## 2. Zero-Touch Automation & Navigation
To eliminate manual routing updates and ensure no topics are missed silently:
- **Generated VitePress Configuration Data**: `scripts/generate-homepage.js` recursively scans `src/main/java/org/example/backend_fundamentals` and writes `docs/.vitepress/navigation_map.json`. `docs/.vitepress/config.mjs` imports that JSON to populate the Curriculum dropdown and sidebars.
  - *Ordering Rule:* Any sibling `order: X` collision is a validation error. Do not fall back to alphabetical sorting, because that hides curriculum-order mistakes.
- **Generated Navigation Map**: The Node.js script emits `sidebar`, `nav`, and flattened `navMap` data. `ExerciseNav.vue` uses the flattened map for direct-refresh-safe exercise and solution navigation.
- **NPM/Husky Hooks**: The root `package.json` scripts run `node scripts/generate-homepage.js` before docs dev/build commands, and `.husky/pre-commit` runs the same validator before commits.

## 3. Topic Learning Experience
Each final topic should use the learning journey that fits the topic type:
- **Theory first:** `index.md` explains the concept, common interview traps, and when the topic matters.
- **Practice when useful:** `exercise/index.md` gives a focused problem, scenario, or prompt when practice is meaningful. It is intentionally excluded from search to avoid cluttering global discovery.
- **Answer path when useful:** `solution/index.md` or `design/index.md` contains the detailed answer, trade-offs, and rubric. It is also excluded from search so solutions do not pollute discovery.
- **Runnable/editable examples:** Java and other code-oriented topics may include playground files that can be explored from the topic context. Future UX may allow running or editing examples from theory, exercise, or solution contexts when that improves learning.
- **Theory-only topics:** Some domains, such as networking foundations or conceptual security notes, may be best served by a concise theory page only. Do not force empty exercises where they add no learning value.

## 4. Layout And Topic Navigation UX
- **Homepage and domain hubs:** The homepage should quickly answer "what can I study here?" Domain hubs should show the next level of major categories without requiring sidebar spelunking.
- **Sidebar behavior:** Sidebar depth should reflect the curriculum hierarchy but avoid hiding the active topic path. Long sibling lists should be grouped or split; one-child waypoints should be collapsed unless the label adds real orientation.
- **Topic-level actions:** A learner inside a topic should see clear actions for the schema family: Theory, Practice, Solution, Design, Code, or Lab. Do not show tabs or buttons that lead to empty or artificial pages.
- **Tab model:** Tabs should be schema-aware, not hardcoded to only `Read` and `Code`. Possible tabs/actions:
  - Theory-only: `Read`.
  - Interactive/code: `Read`, `Code`, `Practice`, `Solution`.
  - System design: `Read`, `Scenario`, `Design`, optional `Diagram`.
  - Future labs: `Read`, `Lab`, `Answer`, or domain-specific names.
- **State and deep links:** Tab state should be URL-addressable and robust on refresh. Links from exercise/solution/design pages should return to the correct topic context.
- **Mobile and dense-content layout:** On smaller screens, tabs, sidebars, code panes, tables, diagrams, and navigation buttons must not overlap or force horizontal page-level scrolling except inside code/table containers.

## 5. Specialized Interactive Components
- **Secure Code Execution (`<Playground>`)**: `Playground.vue` provides editable Java examples and offloads execution to the public **Piston Execution API**.
  - *Operational Update (2026-07-06):* The public `https://emkc.org/api/v2/piston/execute` endpoint currently responds that the public API is whitelist-only as of 2026-02-15. Hosted execution therefore requires either an approved/self-hosted Piston-compatible endpoint configured through `VITE_PISTON_EXECUTE_URL`, or a separate hardened local sidecar for local study mode.
  - *Resilience & Vue Reactivity Rules:* Must actively `.abort()` previous requests on rapid consecutive clicks. MUST prevent submission if the code editor is empty. 
  - *Component Reuse Trap:* When Vue reuses the component on route changes, `onBeforeUnmount` is bypassed. Therefore, the `watch` on `$route` must explicitly `.abort()` any pending network requests, clear previous output state, and call `.setValue(newCode)` on the reused editor instance.
  - *Rate Limiting Rule:* Parse the `Retry-After` header on 429 responses and physically disable the Run button until the window expires.
  - *OOM Protection (Build & Client-Side):* The `import.meta.glob` call for Java files MUST NOT use `eager: true`. Eager evaluation inlines the entire Java repository into the JS bundle, exhausting the Vite build heap and crippling client RAM. It must use dynamic async imports or runtime HTTP fetches.
  - *OOM Protection (Execution):* Truncate the Piston output string to 10,000 characters.
  - *SPA Memory Leak Rule (Code Editor):* The Code Editor instance MUST be explicitly destroyed (`view.destroy()` for CodeMirror, or `editor.dispose()` and `model.dispose()` for Monaco) in `onBeforeUnmount`. 
  - *Compliance & Legal Rule:* Must display a persistent UI warning adjacent to the Run button explicitly forbidding the submission of proprietary code, API keys, or PII.
  - *Run UX Rule:* The run area should clearly show file selection, runnable status, execution state, stdout/stderr/compile errors, rate-limit/timeout states, and output truncation. Non-runnable support files should still be browsable without pretending they can execute alone.
  - *Context Rule:* Code examples should be available from theory, exercise, and solution contexts when useful, but code mode must preserve the learner's place and not hide essential navigation.
- **Java Execution Strategy**:
  - *Hosted/default mode:* Prefer remote sandbox execution through Piston or an equivalent sandbox for public/static-site usage. This avoids exposing the visitor's machine or the site host to arbitrary Maven/JVM execution. The endpoint must be configurable; do not assume the public Piston endpoint is available.
  - *Local study mode:* A local-only runner may be reintroduced for richer repo demos, but it must be treated as a powerful developer tool, not a public endpoint. Historical commits `6a1be06` and `79bc29c` contain the previous sidecar approach: `POST /api/run` accepted an FQCN and ran `mvn -q compile && mvn -q exec:java -Dexec.mainClass="<FQCN>"` from the repo root.
  - *Do not revive the earliest middleware design:* The older `/api/run-java` Vite middleware used `child_process.exec` directly inside Vite config. If local execution returns, use a separate local sidecar with explicit security and lifecycle controls.
  - *Local runner hardening requirements:* Use `spawn` instead of shell-only `exec` where possible; validate FQCNs with a strict Java-name regex; bind only to `127.0.0.1`; reject non-local `Host` and `Origin` headers; cap request body size; serialize or tightly limit Maven runs; kill the whole Maven/JVM process group on timeout, stop, route change, or client disconnect; cap captured output; return separated `stdout`, `stderr`, and `error`.
  - *Save/edit hardening requirements:* If file saving is supported, only write `.java` files under the allowed Java tree, reject `../` traversal, verify real paths stay inside the tree, refuse symlink targets, and never expose save endpoints in hosted mode.
- **System Design Viewer (`<ArchitectureBoard>`)**: `ArchitectureBoard.vue` embeds interactive Draw.io files.
  - *SPA Memory Leak Rule (Iframes):* Must explicitly `window.removeEventListener('message', handlerReference)` in `onBeforeUnmount`. You cannot just pass the string `'message'`; you must store the exact function reference.
  - *Component Reuse Trap:* Similar to the Code Editor, Vue will reuse this component. You MUST bind a `watch` on `$route` (or `props.src`) to perform the "about:blank Flush" (`iframeRef.value.src = 'about:blank'`) and reset the event listener before loading the new iframe source.
- **Global SPA Memory Rules:** Any use of `medium-zoom` must explicitly call `.detach()` or `.destroy()` on route changes to prevent DOM detachment memory leaks.

## 6. SPA Lifecycle And Memory Safety
- **Lazy asset loading:** Do not eagerly glob the full Java tree or all large assets into the client bundle. Use schema metadata and lazy raw-file imports so navigation can render without loading every playground file.
- **Editor cleanup:** Monaco/CodeMirror instances must dispose editor and model resources on unmount. Route reuse must update the existing editor value without creating leaked editor instances.
- **Network cleanup:** Any pending run, save, Piston, sidecar, diagram, or agent request must be abortable. Route changes, tab changes, unmounts, and explicit Stop actions must cancel or ignore stale work.
- **Timer cleanup:** Rate-limit countdowns, elapsed timers, timeout timers, polling loops, and delayed iframe reloads must be cleared on route change and unmount.
- **Iframe cleanup:** Draw.io iframes must remove the exact `message` listener reference and perform an `about:blank` flush before loading a new diagram or unmounting.
- **Zoom cleanup:** `medium-zoom` must detach/destroy on route changes and unmounts; retargeting should not accumulate stale DOM references.
- **Stale result protection:** A completed request must not write output into a newer route, a different selected file, or a newer run. Track run IDs or abort signals and ignore stale completions.
- **Output bounds:** Execution output and logs must be capped both server-side and client-side to avoid large-string memory pressure.

## 7. Schema Families
Every topic must be scaffolded as a self-contained module directory, but not every topic needs the same child pages. Choose the schema based on learning mode, then let automation enforce the chosen shape.

- *Security & SEO Rule:* Folder names must strictly be alphanumeric, underscores, or hyphens (`^[a-zA-Z0-9_-]+$`). Using hyphens (`-`) is strongly preferred for SEO URL parsing (e.g. `system-design` instead of `system_design`).
- *Schema Selection Rule:* The schema should follow the learner task: code practice, system design practice, theory-only reading, or future domain-specific practice. Do not create child pages merely to satisfy a generic shape.
- *Pedagogical Rule:* If a topic declares or implies a practice path, it MUST include the matching answer path. Code/interview-practice modules need `exercise/index.md` plus `solution/index.md`; system design modules need `exercise/index.md` plus `design/index.md`. Theory-only modules may contain only `index.md`.
- *AI Curriculum Generation Rule:* Missing exercise/solution/design pages may be generated through a reviewed local AI workflow, but generation must stay decoupled from CI and must not commit unreviewed curriculum.
  - **Governance Requirement:** AI generation must be completely decoupled from the CI pipeline to prevent unreviewed hallucinations entering production. It must be executed locally by a developer (e.g., via a script) and pass human peer review before commit.
  - **Circuit Breaker:** The AI must have a hard limit of 3 retries (`MAX_RETRIES = 3`) to prevent infinite generation loops if it repeatedly hallucinates malformed markdown.
  - **Coding:** Generate a problem and solution only when the topic is intentionally using the interactive/code schema. If real practice is not useful, keep the topic theory-only.
  - **System Design:** Generate a real-world scenario, design rubric, and optional diagram assets only when the topic is intentionally using the system-design schema. If the topic is introductory or conceptual, keep it theory-only.

**Schema A: Interactive Code Module (Java, Spring examples, coding fluency)**
```text
[topic_name]/
├── index.md           # Theory and concepts. Requires 'order: X' frontmatter.
├── playground/        # Java-only source directory when examples are runnable/editable.
├── exercise/          
│   └── index.md       # Practice. Requires order: 10 and search: false.
└── solution/          
    └── index.md       # Solution. Requires order: 20 and search: false.
```

**Schema B: System Design Module**
```text
[system_design_topic]/
├── index.md                # Theory/problem statement. Requires 'order: X'.
├── exercise/               
│   └── index.md            # Scenario. Requires order: 10 and search: false.
├── design/
│   └── index.md            # Reference design/rubric. Requires order: 20 and search: false.
└── assets/                 # Optional images and .drawio files only.
```

**Schema C: Theory-Only Module (networking basics, conceptual notes, reference topics)**
```text
[topic_name]/
└── index.md                # Concise theory. Requires 'order: X'.
```

Use Schema C when an exercise would be placeholder content. If the topic later gains real practice value, migrate it to Schema A or a domain-specific schema in the same session as adding the practice material.

**Schema D: Practice Module (non-code interview/practice topics)**
```text
[topic_name]/
├── index.md                # Theory and concepts. Requires 'order: X'.
├── exercise/
│   └── index.md            # Practice prompt. Requires order: 10 and search: false.
└── solution/
    └── index.md            # Detailed answer. Requires order: 20 and search: false.
```

Use Schema D when a topic has useful practice and an answer path, but no runnable/editable playground code. If code execution becomes useful later, migrate the topic to Schema A in the same session as adding real playground files.

**Future Schema Families**
Additional schemas may be introduced for domain-specific learning modes, such as:
- **Networking lab module:** theory plus packet-flow exercises, troubleshooting prompts, or diagrams.
- **Database/query module:** theory plus SQL/query practice and solution.
- **Security threat-model module:** theory plus attack/defense scenario and rubric.

New schema families must be added deliberately: document the shape, update `AGENTS.md`, update `scripts/generate-homepage.js` validation, and add negative tests before relying on them broadly.

## 8. Progressive Curriculum Sequencing
- Sequencing is driven purely by YAML frontmatter `order: X` placed in the `index.md` of every folder. Increments of 10 are mandatory.
- Ordering should reflect learning progression and scanning ergonomics, not insertion history. Leave gaps between order values so future topics can be inserted without renumbering whole sections.

## 9. Build-Time Schema Validation & AI Governance
- **Project-Wide Validation Contract**: All curriculum content under `src/main/java/org/example/backend_fundamentals/` must be validated whenever content is added, moved, renamed, or structurally changed. The validator is the gatekeeper for schema-family rules, navigation ordering, and generated navigation data.
- **Strict Validation Mode**: `generate-homepage.js` is the normal schema validator for local development, docs dev, docs build, and Husky pre-commit. Structural validation errors must be fatal in these workflows.
  - *Normal workflows:* Invalid curriculum structure must fail `node scripts/generate-homepage.js`, docs dev/build commands, and Husky pre-commit.
  - *Audit/report workflows:* Non-fatal navigation-smell reporting is allowed only as a separate audit mode for issues such as excessive depth or long sibling lists. It must not downgrade schema violations to warnings.
- **Failure Rule:** Once schema-family validation is implemented, invalid curriculum structure must never reach the served site as warnings-only behavior in normal workflows.
- **Node.js Memory Protection**: The generation script MUST use Node's `readline` module to stream frontmatter from `index.md` line-by-line, stopping after the closing `---` for order extraction. Avoid full-file reads for validation-only metadata as the curriculum scales.
- **File System Resilience & Hierarchical Ordering**: The scanner MUST be recursive. For *every single directory* at every level (domain, category, module), the script MUST assert the existence of an `index.md` file containing the `order: X` YAML frontmatter. If any directory in the curriculum lacks an `index.md` or an `order` tag, the build MUST crash. This guarantees flawless navigation sorting. It must also explicitly check `fs.statSync().isDirectory()` to avoid `ENOTDIR` crashes from stray root files (like `diagram.png`), and must use case-sensitive assertions for `index.md` to prevent Linux CI/CD failures.

## 10. Known Hardening Follow-Ups
- Implement the project-wide validation contract in `scripts/generate-homepage.js`; this is intentionally documented before implementation so the next code change can be reviewed against a stable rule set.
- Make schema detection explicit instead of accidental. A valid leaf may be theory-only, but any directory with `playground/`, `exercise/`, `solution/`, `design/`, or `assets/` should be validated against a named schema family.
- Extend `scripts/generate-homepage.js` to enforce exact child frontmatter when child pages exist: `exercise/index.md` uses `order: 10` and `search: false`; `solution/index.md` and `design/index.md` use `order: 20` and `search: false`.
- Validate directory contents by schema: `playground/` should contain Java-only source files, and `assets/` should contain only images or `.drawio` files.
- Replace eager Java globs in both `Layout.vue` and `Playground.vue` with lazy loading before significantly expanding playground coverage.
- Fix Monaco worker build output so `npm run docs:build` does not emit generated worker files under `src/main/java/org/example/backend_fundamentals/Users/...`. Build artifacts belong under ignored VitePress build/cache/temp locations, never inside curriculum content.
- Replace hardcoded Read/Code behavior with schema-aware tabs/actions. `ExerciseNav.vue` must stop assuming every topic has an exercise.
- Decide whether Java execution is hosted Piston-only, local sidecar-only, or dual-mode. If local sidecar returns, implement the hardening requirements above before exposing run/save controls.
- Add navigation UX checks for hierarchy depth: top-level domains should be easy to scan, intermediate sidebars should not become long undifferentiated lists, and final topic pages should expose clear paths to theory, practice, and answer pages.

## 11. Legacy Cleanup
All obsolete scripts, deprecated layout components, and hardcoded routing mappings will be permanently deleted.

## 12. Mandatory Automated QA & Negative Testing (Playwright)
Before any human review is requested, a Playwright E2E script must verify the holistic health of the application. 
- **API Mocking Rule:** E2E scripts must use Playwright Network Interception (`page.route()`) to mock Piston API responses.
- **Navigation QA Rule:** E2E tests must cover homepage domain discovery, domain hub drilldown, sidebar active state, topic action visibility, direct refresh on exercise/solution/design pages, and mobile layout.
- **Playground QA Rule:** E2E tests must cover runnable and non-runnable files, success, compile error, timeout, rate limit, rapid clicks/abort, output truncation, and route changes during a pending run.
- **Memory/Lifecycle QA Rule:** Tests must verify repeated route changes, tab switches, diagram loads, and run cancellations do not accumulate listeners, timers, stale output, or duplicate editor instances.
- **Local Runner QA Rule:** If the local sidecar returns, tests must cover invalid FQCN rejection, timeout process-tree kill, client-disconnect abort, concurrency limiting, Host/Origin rejection, request-size caps, out-of-tree save rejection, non-Java save rejection, and symlink escape rejection.

## 13. Final Peer Review & Git Protocol
- **No Unapproved Commits**: Agents are strictly forbidden from executing `git commit` at any stage without explicit human authorization.
