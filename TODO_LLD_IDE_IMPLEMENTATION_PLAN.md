# TODO: LLD / Java IDE Implementation Plan

Goal: support LLD and multi-class Java practice well enough for study work without turning the site into an IntelliJ clone.

This plan tracks remaining work only. Keep each phase separately reviewable and committable.

## Working Assumptions

- Today this is a localhost-only personal study setup.
- Later the dev server may need to be reachable from another machine, so avoid choices that make LAN hardening impossible.
- The user may already be studying on `http://127.0.0.1:5173/`; do not disturb that server.
- All development servers, Playwright probes, route audits, and manual browser checks for this plan must use a separate port such as `5176` or `5177`.
- Structured exercise pages should stay single-file scratch work by default.
- Multi-file LLD should use topic playgrounds or larger case-study modules.
- Parking Lot counts as LLD, but it belongs under system-design case studies, not Java collection drills.

## Port Isolation Rules

When implementing this plan, assume port `5173` belongs to the user's active study session.

Do:

- Prefer `5176` for LSP work and `5177` as the fallback if `5176` is busy.
- Start isolated docs servers directly with `docs:dev`, not the shared PID helper:
  `VITE_ENABLE_JAVA_RUNNER=1 VITE_ENABLE_JAVA_LSP=1 npm run docs:dev -- --host 127.0.0.1 --port 5176`
- Point probes and e2e scripts at the isolated server:
  `PLAYWRIGHT_BASE_URL=http://127.0.0.1:5176 node scripts/probe-monaco-completions.js`
- For `playwright_e2e.js`, always use an already-running isolated server:
  `PLAYWRIGHT_USE_EXISTING_SERVER=1 PLAYWRIGHT_BASE_URL=http://127.0.0.1:5176 node playwright_e2e.js`
- For route audits, pass the base URL explicitly:
  `SITE_AUDIT_BASE_URL=http://127.0.0.1:5176 node scripts/site-route-audit.js`

Avoid while the user is studying:

- `npm run start`, `npm run start:light`, `npm run start:java`, or `npm run start:lsp` unless you first confirm the shared `.tmp/docs-dev.pid` will not collide with the user's session.
- `npm run end`, because it uses the shared pid file and also kills `lms-java-lsp-` sessions.
- `npm run test:e2e` without `PLAYWRIGHT_USE_EXISTING_SERVER=1` and an explicit `PLAYWRIGHT_BASE_URL`, because the script defaults to `5173`.
- Any command that opens, reloads, kills, or assumes ownership of `5173`.

## Current State

- The site has a Monaco-based Java editor through `docs/.vitepress/theme/components/CodeEditor.vue`.
- Topic playgrounds support multi-file workspaces through `docs/.vitepress/theme/components/Playground.vue`.
- Structured exercise pages support one scratch editor plus question/solution panels through `docs/.vitepress/theme/components/ExerciseWorkspace.vue`.
- Local Java run support is optional and disabled in light mode.
- Java LSP support is optional and enabled by `VITE_ENABLE_JAVA_LSP=1`; `npm run start:lsp` is only one helper, and isolated `docs:dev -- --port 5176` is preferred while the user is studying.
- The LSP bridge is local-dev only and lives in `docs/.vitepress/dev/javaLspBridge.mjs`.
- The bridge creates temporary JDT LS workspaces under OS temp folders, not inside the repo.
- Workspace limits already exist: max sessions, max files, max file size, idle timeout, stale-client timeout, and temp cleanup.
- The main playground path already wires LSP completion, completion resolve, hover, go-to-definition, formatting, code actions, and diagnostics.
- The structured exercise path currently wires LSP completion, completion resolve, and diagnostics only.
- `LSP starting` / `LSP ready` / `LSP failed` state labels already exist in both playground and structured exercise surfaces.
- Common Java type completions are ranked so study types like `List`, `ArrayList`, `HashMap`, `TreeSet`, and `Integer` appear above noisy internal/JDK suggestions.
- A Lists LLD pilot exists at `src/main/java/org/example/backend_fundamentals/java/collections/lists/playground/lld/`.
- The existing completion probe prints type suggestions and accepting `List`, but does not assert ranking, import choice, or receiver/member completions yet.

## Current Risks / Gaps

1. Member completion after receivers such as `list.` still needs final verification.
2. Browser-side LSP requests still use synthetic URIs such as `file:///TaskInboxRun.java` or `file:///PracticeScratch.java`.
3. The bridge remaps synthetic URIs to temp-file URIs by Java filename. That is acceptable for unique filenames, but fragile if a session contains duplicate names from different groups/packages.
4. LSP warm-up can show incomplete suggestions before JDT LS settles; UX labels exist, but behavior still needs manual verification.
5. There is no failing automated probe dedicated to `receiver.` member completions.
6. LLD rollout has only one Java-topic pilot. More LLD pages should be added only where multi-file practice clearly helps.
7. Future LAN use will need runner/LSP hardening beyond today's localhost assumptions.
8. The default dev/e2e helper scripts can target or manage `5173`; future implementation must override ports explicitly.
9. The local Java runner is trusted-local only today. It has temp dirs, validation, caps, and timeouts, but no Host/Origin checks and no process-tree kill for child processes spawned by Java code.

## Out Of Scope For Now

- A full IDE clone.
- Arbitrary local file browsing.
- Browser-selected shell commands.
- Persistent JDT workspaces.
- Debugger support.
- Project import support for Maven/Gradle.
- Global rollout of multi-file workspaces to every Java page.
- LSP in production docs builds.
- Manual Java API completion lists for methods like `add`, `size`, `get`, or `remove`.
- LAN-safe auth/origin hardening before the dev server is intentionally exposed off localhost.

## Execution Order

Run phases in order:

1. Phase 0: protect `5173`, choose an isolated port, and note unrelated worktree changes.
2. Phase 1: make LSP completion probes assertive and prove/fix `list.` member completion.
3. Phase 1b: verify multi-file local runner behavior.
4. Phase 2: verify LSP warm-up UX.
5. Phase 3: lock or correct the LLD content pattern.
6. Phase 4: complete Parking Lot MVP.
7. Phase 5: cleanup and guardrail review.

Stop before Phase 4 if Phase 1, Phase 1b, or Phase 2 fails. Do not add more LLD content on top of unreliable editor/run behavior.

## Phase 0: Preflight And Isolation

Purpose: protect the user's active study session and preserve unrelated work before implementation starts.

Scope:

- Check `git status --short` and identify unrelated user changes.
- Do not edit or revert unrelated Parking Lot, study notes, or generated files unless the current phase explicitly owns them.
- Check whether `5176` is available; if not, use `5177`.
- Start any dev server for this plan on the isolated port only.
- Record the isolated base URL in the working notes for the phase.

Acceptance checks:

- No process on `5173` was killed, reloaded, or reused.
- The isolated server responds on the chosen port.
- Future commands use the isolated base URL.

Validation:

- `git status --short`
- `node -e "fetch('http://127.0.0.1:5176/').then(r=>console.log(r.status)).catch(e=>{console.error(e.cause?.code||e.message);process.exit(1)})"` after the isolated server starts.

Commit boundary:

- No commit needed unless helper scripts are changed.

## Phase 1: Prove And Fix LSP Member Completion

Purpose: make existing LSP functionality trustworthy before adding more LLD content.

Scope:

- Reproduce completion for a small snippet with `List<String> list = new ArrayList<>(); list.`
- Add or extend a Playwright-based probe that fails unless `list.` shows real JDK members.
- Convert the completion probe from logging-only to assertion-based before relying on it.
- Keep completion data from real JDT LS responses.
- Keep local keyword/snippet fallback disabled after `.` so member completion is not polluted.
- Resolve the synthetic-URI risk before broad LLD rollout:
  - preferred: pass bridge-owned document URIs or stable document ids back to the client;
  - acceptable short-term guard: reject duplicate Java basenames in one LSP session until real document identity exists.

Acceptance checks:

- `list.` shows real JDK methods such as `add`, `get`, `size`, `remove`, and `stream`.
- The receiver-completion probe exits non-zero when those methods are missing.
- The completion probe exits non-zero if type ranking or `List` import insertion regresses.
- Type completion still ranks `List`, `ArrayList`, `HashMap`, `TreeSet`, and `Integer` correctly.
- Accepting `List` imports `java.util.List`, not `java.awt.List`.
- Duplicate Java basenames cannot silently route LSP requests to the wrong document.
- Route changes do not duplicate diagnostics or completion providers.
- Explicit stop, route change, server shutdown, and stale-client timeout after reload/page close do not leak `jdtls` processes or `lms-java-lsp-` temp sessions.

Validation:

- Start isolated LSP server on non-`5173` port:
  `VITE_ENABLE_JAVA_RUNNER=1 VITE_ENABLE_JAVA_LSP=1 npm run docs:dev -- --host 127.0.0.1 --port 5176`
- `PLAYWRIGHT_BASE_URL=http://127.0.0.1:5176 node scripts/probe-monaco-completions.js`
- Receiver-completion probe added or integrated in this phase.
- Manual process check for stale `jdtls` / `lms-java-lsp-` sessions.
- `npm test`

Commit boundary:

- Commit only LSP/editor/probe changes.
- Do not mix curriculum content changes into this phase.

## Phase 1b: Verify Multi-File Runner Smoke

Purpose: ensure multi-file LLD examples still compile and run locally before adding more content.

Scope:

- Add or reuse the smallest smoke path for a package, support class, and runnable class.
- Confirm compile diagnostics map to the correct file.
- Confirm timeout and output caps still work.
- Do not add a full test suite unless the existing e2e shape makes that cheap.

Acceptance checks:

- A multi-file workspace run includes support classes and executes the selected runner.
- A compile error in a support file points at that support file.
- Timeout and output truncation behavior remain bounded.
- No runner endpoint is exposed beyond trusted-local assumptions.

Validation:

- Use an isolated server with Java runner enabled:
  `VITE_ENABLE_JAVA_RUNNER=1 VITE_ENABLE_JAVA_LSP=0 npm run docs:dev -- --host 127.0.0.1 --port 5176`
- Optional e2e against the isolated server:
  `PLAYWRIGHT_USE_EXISTING_SERVER=1 PLAYWRIGHT_BASE_URL=http://127.0.0.1:5176 node playwright_e2e.js`
- `npm test`

Commit boundary:

- Commit only runner/e2e smoke changes.
- Do not mix curriculum content changes into this phase.

## Phase 2: Verify LSP Warm-Up UX

Purpose: confirm the existing status/sorting behavior is understandable without adding heavy prewarm behavior.

Scope:

- Verify `LSP starting` / `LSP ready` / `LSP failed` state transitions in playground and structured exercise surfaces.
- Confirm that before ready, users can still type and use local snippets.
- Confirm that after ready, semantic suggestions replace noisy fallback behavior naturally.
- Suppress or de-prioritize noisy semantic results only if real study suggestions are being buried.
- Avoid automatic page-wide prewarm unless Phase 1 proves startup time is the main issue.
- Keep light docs mode free of LSP process startup.

Acceptance checks:

- Isolated light-mode server shows no LSP status and starts no LSP process.
- Isolated LSP-mode server shows clear status transitions.
- LSP failure leaves the editor usable with local snippets and compile/run feedback.
- No new UX surface suggests that the browser can browse arbitrary files or import full projects.

Validation:

- Start light mode on a non-`5173` port:
  `VITE_ENABLE_JAVA_RUNNER=0 VITE_ENABLE_JAVA_LSP=0 npm run docs:dev -- --host 127.0.0.1 --port 5176`
- Start LSP mode on a non-`5173` port:
  `VITE_ENABLE_JAVA_RUNNER=1 VITE_ENABLE_JAVA_LSP=1 npm run docs:dev -- --host 127.0.0.1 --port 5176`
- `npm test`

Commit boundary:

- Commit only UX/status/sorting changes.

## Phase 3: Lock The LLD Workspace Pattern

Purpose: define the smallest repeatable content pattern before creating more LLD modules.

Preferred Java-topic pattern:

- Theory page explains the concept.
- `exercise/index.md` describes the task and checks.
- `solution/index.md` explains the reference approach.
- `playground/<topic>/` contains runnable Java files.
- Use multi-file playground only when the exercise naturally needs multiple classes.

Preferred case-study pattern:

- `index.md` introduces the problem and scope.
- `design/index.md` captures the model, flows, constraints, tradeoffs, and reference design/answer.
- `exercise/index.md` gives the implementation/design task.
- Do not add `solution/index.md` beside `design/index.md` for system-design case studies unless the navigation schema is intentionally changed first.
- `playground/` contains runnable Java files only when code adds practice value.
- Assets such as diagrams live under `assets/` when needed.
- `system_design/case_studies/` hosts design case studies, including LLD machine-coding case studies; Parking Lot is Part 4 LLD practice even though it lives under system design.

Use multi-file LLD for:

- Small object-modeling problems.
- System-design case studies such as Parking Lot.
- Design pattern practice where collaborators are the lesson.
- Collection-backed mini-components.
- Exception and validation flows with meaningful supporting classes.

Avoid multi-file LLD for:

- Theory-only pages.
- Single-method syntax drills.
- Stream transformation drills.
- Tiny exercises where one scratch file is clearer.

Acceptance checks:

- Lists LLD pilot still feels like the Java-topic template.
- Parking Lot, if continued, follows the case-study template.
- Case-study modules do not contain both `design/` and `solution/`.
- No new empty folders.
- No `Main.java` beside module docs.
- Java files stay under `playground/`.

Validation:

- `node scripts/generate-homepage.js`
- Manual route check for Lists Code and Practice tabs on the isolated port.
- Manual route check for Parking Lot on the isolated port if touched.

Commit boundary:

- Commit only pattern/doc/template adjustments.

## Phase 4: Complete The First LLD Practice Item

Purpose: finish one high-value LLD item before starting another.

Default first item:

- Complete Parking Lot MVP first, because Parking Lot work is already active in the repo.
- Do not add Maps/OOP/custom-exception LLD drills until Parking Lot compiles and has a readable Read -> Practice -> Design -> Code flow.

Parking Lot MVP means:

- `index.md` states the problem, scope, and what is intentionally simplified.
- `exercise/index.md` has scope, constraints, expected operations, and acceptance criteria.
- `design/index.md` has non-empty Requirements, Core entities, Main flows, Tradeoffs, and Quick recall.
- `playground/` has runnable Java that demonstrates the core workflow.
- `ParkingLotRun` demonstrates park, reject/full or unavailable case, exit/release, and availability/payment only if those concepts are included in scope.

Rules:

- Add one LLD item after Phase 1, Phase 1b, and Phase 2.
- Each module must have a real multi-class reason.
- Each exercise must be solvable in 30-60 minutes.
- Each Java-topic solution or case-study design must stay compact and interview-oriented.
- Do not duplicate existing drills under a new LLD label.
- After Parking Lot, the next Java-topic candidate is a small Maps inventory/cache drill.
- Do not add Factory/Builder LLD unless existing playgrounds are unclear; those sections already have multi-file examples.

Acceptance checks:

- Every Java-topic exercise has a matching solution; every case-study exercise has a matching design page.
- Every runnable example has a clear runner.
- The page flow is Read -> Practice -> Solution -> Code where applicable.
- For case studies, the page flow is Read -> Practice -> Design -> Code where applicable.
- Existing theory pages are not bloated with implementation instructions.
- `node scripts/generate-homepage.js` passes with no `solution/` beside `design/` for Parking Lot.

Validation:

- `node scripts/generate-homepage.js`
- `mvn -q compile`
- Run Parking Lot exactly if touched:
  `mvn -q exec:java -Dexec.mainClass="org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.ParkingLotRun"`
- Run each other new `...Run` class if the module has runnable Java.
- Manual browser route check for every new/changed module on the isolated port.
- `npm test`

Commit boundary:

- Commit one topic/module at a time.
- Do not combine unrelated LLD topics in one commit.

## Phase 5: Final Cleanup And Guardrail Review

Purpose: make sure the IDE work remains local, bounded, and maintainable.

Checklist:

- No JDT binaries, caches, indexes, or temp workspaces are committed.
- No generated navigation was manually edited.
- No broad LSP startup happens in normal docs mode.
- No arbitrary filesystem path, command, or project import surface was added.
- Process cleanup still handles page close, route change, idle timeout, client disconnect, and dev-server shutdown.
- `docs/.vitepress/navigation_map.json` reflects only intended curriculum routes.
- Temporary or planning files are removed only when the user explicitly asks.
- Localhost-only assumption is still documented for Java runner/LSP.
- No cleanup command killed or replaced a pre-existing `5173` docs server.
- Any isolated test server started on `5176`/`5177` was stopped by its own process id or a port-scoped cleanup path.

Validation:

- `git status --short`
- `git diff --check`
- `node scripts/generate-homepage.js`
- `npm test`
- Manual LSP start/stop/process cleanup check on the isolated port.
- Optional e2e only against the isolated server:
  `PLAYWRIGHT_USE_EXISTING_SERVER=1 PLAYWRIGHT_BASE_URL=http://127.0.0.1:5176 node playwright_e2e.js`

Commit boundary:

- Commit cleanup separately from feature/content phases.

## Future LAN Hardening Backlog

Do this only when the dev server will intentionally be reachable from another machine:

- Bind intentionally and document the allowed host/port.
- Add Host and Origin checks for local runner and LSP endpoints.
- Keep strict FQCN/path validation.
- Enforce process-tree kill on timeout/disconnect.
- Keep output caps and request-size caps.
- Consider an explicit local-only token or allowlist before exposing runner endpoints.
- Add tests around rejected host/origin/path cases.
- Replace or extend shared PID handling before relying on `scripts/start-dev.js` for multiple simultaneous dev servers.
- Before LAN exposure, either complete this hardening backlog or block Java runner/LSP endpoints off-loopback.

## Done Criteria

This TODO can be closed when:

- `list.` member completion works from real JDT LS data.
- Receiver completion is covered by a failing automated probe or test.
- Type imports still choose Java study defaults correctly.
- LSP remains optional, local-only, and leak-free in manual checks.
- Parking Lot MVP, or one later LLD item, has been completed only if it clearly improves practice coverage.
- The Lists pilot and any new Java-topic LLD modules have readable Read/Practice/Solution/Code flows.
- Parking Lot and any other case-study LLD modules have readable Read/Practice/Design/Code flows.
- All manual browser/probe/e2e validation for this plan was run on a non-`5173` port.
- `node scripts/generate-homepage.js` and `npm test` pass.
