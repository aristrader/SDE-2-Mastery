# AGENTS.md

Main Codex instructions for this repository. Keep this file short; add only durable rules that prevent repeated mistakes.
This is the authoritative agent instruction file for the repo.

## Repository Shape

- This is a personal backend-fundamentals and interview-prep learning repo, not a production Spring service.
- Primary content lives under `src/main/java/org/example/backend_fundamentals/`.
- The study plan lives under `src/main/java/org/example/backend_fundamentals/todo/study_plan/`.
- Do not put files directly under `src/main/java/org/example/`.
- Do not add `package-info.java` placeholders.
- Preserve unrelated user changes.

## Local Project Workspace

- Keep reusable external projects outside this repository at `/Users/swapnilagarwal/workspace/projects/`.
- Use `empty-project/` for the reusable starter and `working-repos/` for active or completed project clones.
- Keep these names neutral for clean screen sharing; do not create interview-named top-level folders.

## Default Working Style

- Use ponytail by default for all repo work: smallest correct change, reuse existing code/patterns first, avoid speculative abstractions, and keep explanations short unless the user asks for depth.
- Versioned reusable agent workflows live in `.agents/skills/`. Keep each skill narrowly scoped; use the matching HLD, LLD, theory, or exercise skill rather than combining workflows.
- The user often wants to write code themselves. If they say to guide/review only, do not edit active `.java` or `.md` files until asked.
- When asked "is this right?", answer directly: yes/no, exact issue, exact fix.
- Keep explanations focused on the current topic or pattern. If adjacent patterns apply, name that explicitly instead of mixing concepts.
- Confirm before large deletions.
- Do not ask before running `git add`; stage requested files directly when committing.

## Large Context Offload

- For broad repo scans or many-file context gathering, use Antigravity CLI as a read-only summarizer instead of loading huge file sets directly:
  `"/Users/swapnilagarwal/.local/bin/agy" --add-dir /Users/swapnilagarwal/IdeaProjects/TestingTesting -p "<focused read-only prompt>"`
- Ask for compact, source-grounded summaries with file paths. Treat the result as advisory; verify specific claims against local files before editing.
- Do not use Antigravity for small targeted reads where `rg`, `sed`, or direct file inspection is cheaper and clearer.

## Commands

```bash
mvn -q compile
mvn -q clean compile
mvn -q exec:java -Dexec.mainClass="<FQCN>"
node scripts/generate-homepage.js
node scripts/normalize-study-plan-rows.js
codegraph status .
codegraph sync .
```

- Use `mvn -q compile` for a quick Java sanity check.
- When verifying examples, run the relevant `...Run` main with Maven instead of inventing tests.
- `src/test/java` is currently empty; do not add tests unless the user asks or the change clearly needs them.
- Run `node scripts/generate-homepage.js` after adding, moving, deleting, or renaming site pages/sections.

## Code Navigation

- CodeGraph is initialized for this repo via `.codegraph/` and `.mcp.json`; the database and logs are local artifacts and should not be committed.
- Use CodeGraph for relationship questions: callers/callees, impact, symbol bodies inside large files, and bounded exploration of unfamiliar areas.
- Prefer `rg`/direct file reads for plain text searches, constants, small files, and simple path discovery.
- If CodeGraph results look stale, run `codegraph sync .`; keep `codegraph explore` bounded because it can return large source chunks.

## LMS / Website Structure

Use schema families under `backend_fundamentals`; choose the lightest schema that fits the learning mode.

- Interactive/code module: `index.md`, `playground/` Java-only files, `exercise/index.md`, `solution/index.md`.
- Practice module: `index.md`, `exercise/index.md`, `solution/index.md` for non-code practice topics.
- System design module: `index.md`, `design/index.md`, `exercise/index.md`, optional `assets/`.
- Theory-only module: `index.md` only, for topics where exercises would be artificial. Add exercise/solution later only when there is real practice value.
- If study material needs diagrams/images, create original repo-owned visuals rather than copying screenshots from books, courses, PDFs, or copyrighted web pages. Prefer simple SVG or Draw.io diagrams in the module's `assets/` directory and embed with normal Markdown image syntax.
- Do not add `assets/` to a theory-only module unless you also convert it to a valid module shape (`exercise/` plus `design/` or `solution/`), because navigation validation treats `assets/` as module content.
- Module `index.md` files need valid YAML frontmatter and `order: X`.
- `exercise/index.md` uses `search: false` and `order: 10`.
- `solution/index.md` uses `search: false` and `order: 20`.
- `design/index.md` uses `search: false` and `order: 20`.
- Never create empty folders; they can break navigation generation.
- Do not manually edit generated navigation data. `scripts/generate-homepage.js` writes `docs/.vitepress/navigation_map.json`, and `docs/.vitepress/config.mjs` consumes it.
- Java playground code belongs in `playground/`; do not place `Main.java` beside module `index.md`.
- Validation must be project-wide: new, moved, or renamed curriculum content should fail `node scripts/generate-homepage.js`, docs dev, docs build, and pre-commit if it violates its schema family.
- UX changes should be judged by navigation clarity: domain discovery, readable sidebars, clear topic-level tabs/actions, and predictable theory/practice/answer/code flows.
- Do not reintroduce local Java run/save endpoints without the documented sidecar hardening: loopback-only, Host/Origin checks, strict FQCN/path validation, process-tree kill on timeout/disconnect, output caps, and tests.

## Markdown Rules

- Use pure GitHub-flavored Markdown; avoid HTML tags.
- Prefer concise interview-prep writing over textbook coverage.
- Use fenced `java` blocks for Java examples.
- Topic docs should end with `## Quick recall` when they are study material.
- Keep Quick recall answers short.
- Preserve useful user analogies, misconceptions, and struggle points when converting notes into docs.

## Curriculum Cleanup Rules

- Organize topic folders by learning flow, not source-dump shape. Split pages that are too broad; merge tiny drills when separate pages add navigation noise.
- Put basic theory before practice on each topic. Do not start a page with exercises before syntax, mental model, traps, and interview relevance are clear.
- Keep exercises close to the topic they practice. Use combined drills only when the point is cross-topic integration or interview-style review.
- When moving/splitting/deleting docs, verify useful content was preserved or intentionally cut; report any intentional cuts.

## Java Rules

- Use role-based class names such as `DeveloperHiringProcess` or `BillPughSingleton`; avoid vague names such as `FactoryImpl`.
- Demo runners should print to `System.out` and may use Lombok `@Slf4j` if consistent with nearby code.
- Add JavaDoc only when it explains non-obvious purpose, contracts, design decisions, or useful cross-references.
- Do not put long lessons, feature tables, or multi-section explanations in JavaDoc; put them in Markdown docs.

## Study Plan Rules

- When the user references the study plan, read `src/main/java/org/example/backend_fundamentals/todo/study_plan/README.md` first.
- Per-Part files in `src/main/java/org/example/backend_fundamentals/todo/study_plan/parts/` are the authoritative syllabus.
- Keep each Part inventory numbered sequentially in displayed priority order. After reordering rows, run `node scripts/normalize-study-plan-rows.js --write`; the normal homepage generation validates this.
- Cross-document study-plan references should use the Part and topic name, not row numbers, because priority changes can reorder rows.
- Before creating or substantially editing study material linked from a Part row, read `src/main/java/org/example/backend_fundamentals/todo/study_plan/reference/DocCreationStandard.md`.
- New study material should pass this bar: would a senior Java backend interviewer plausibly ask about it?
- Do not tick `Done`, `Grilling`, or `Visit Again` for the user except during the temp-doc import workflow.
- If adding, renaming, deleting, moving, splitting, or significantly rewording a Part topic row, update `src/main/java/org/example/backend_fundamentals/todo/study_plan/reference/TopicIndex.md` in the same session.
- A plain `Done` or `Partial` tick does not require a TopicIndex update.

## Temp Doc Import Workflow

Use this when the user asks to process temp import files such as `temp.md` or `temp1.md` through `temp4.md`.

- Read every staged temp file completely before processing.
- If a staged temp file is long, page through it in chunks until EOF; never assume the first view is the whole file.
- Read `src/main/java/org/example/backend_fundamentals/todo/study_plan/reference/DocCreationStandard.md`.
- Decide destination paths yourself unless truly blocked.
- Merge related dumps into existing docs; split one dump if it clearly covers multiple topics.
- Do not drop substantive discussion points, struggle points, misconceptions, analogies, or flowchart-style reasoning. Reorganize, dedupe, and deepen only topics actually discussed.
- Do not add undiscussed subtopics just to complete a Part-row gap. If a row is only partly covered, mark it `Partial` and report the gap.
- Write docs under `src/main/java/org/example/backend_fundamentals/`.
- Clear processed temp files back to their placeholder comments; do not delete the temp files.
- For covered Part rows, add the doc link in Resources.
- Tick `Done` only when substantially covered. For partial coverage, tick `Partial`, leave `Done` unticked, and note covered vs pending.
- Track actual time only in the `Full Part` actual-time cell, as a single cumulative estimate.
- Use absolute paths for temp-file clearing or similar file operations; do not rely on shell CWD.
- End with a compact report: files created, rows covered, partial gaps, and timeline position. Sprint anchor: Monday, May 18, 2026 = Week 1 start.

## ChatGPT Study Import Workflow

Use this when the user says they pasted study output into `theory.md` and `exercise.md`.

- Read both files completely before processing; page through long files until EOF.
- Treat `theory.md` like the temp-doc import workflow: preserve substantive notes, struggle points, misconceptions, analogies, and flowchart-style reasoning; merge or split into the right topic docs under `backend_fundamentals`.
- Treat `exercise.md` as a source list of already-defined tasks owned by the study AI. Do not redesign, broaden, or replace them; split them into the right module `exercise/index.md` files and preserve intent, order, constraints, requirements, and concepts tested.
- Create matching `solution/index.md` files for every exercise by deriving answers from `theory.md`, existing repo material, and standard Java/Spring knowledge. Do not skip solution files unless explicitly blocked; report any uncertainty as a partial gap.
- Prefer interactive/code modules when exercises should run on-site: `index.md`, Java-only files under `playground/`, `exercise/index.md`, and `solution/index.md`.
- For every structured exercise, use headings exactly as `## Exercise: kebab-id - Title` and `## Solution: same-kebab-id - Title`; each exercise ID must have exactly one matching solution ID.
- Preserve ordering: module `index.md` has `order: X`; `exercise/index.md` has `order: 10` and `search: false`; `solution/index.md` has `order: 20` and `search: false`.
- Keep exercise files task-focused. Put lessons and long explanations in topic `index.md`, not inside exercises unless needed to attempt the task.
- Do not invent broad new exercises unrelated to pasted content. Small clarifying acceptance criteria, starter code, and runnable playground scaffolding are allowed when they preserve the intended task.
- After processing, clear `theory.md` and `exercise.md` back to placeholder comments; do not delete them.
- Run `node scripts/generate-homepage.js`; run relevant docs/build or Java sanity commands when structure or runnable code changed.
- End with a compact report: docs updated, exercises created, solutions created, playground files created, study-plan rows touched, partial gaps.

## Durable Insight Routing

- Pattern-specific mechanics go in that module/topic doc.
- Generic design-thinking notes go in `src/main/java/org/example/backend_fundamentals/todo/study_plan/deep_dives/DesignThinkingProcess.md`.
- Pattern-selection and orchestration notes go in `src/main/java/org/example/backend_fundamentals/todo/study_plan/deep_dives/PatternSelectionExercise.md`.
- OOP/SOLID/supporting-principle notes go in the closest relevant foundations doc.
- If unsure, keep the insight local to the current topic rather than creating broad documentation.
