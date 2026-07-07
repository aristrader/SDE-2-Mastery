# LMS Architecture Design Specification

## Status
The schema/navigation automation and first content-role cleanup are implemented on `chore/schema-migration`.

Keep this spec as the durable product contract. Do not keep completed task checklists here; new future initiatives should get new small specs/plans.

## Product Goal
The documentation site should feel like a curriculum, not a file dump:

- New visitors see major domains first: Java, Spring, System Design, Databases, Networking, Security, and related areas.
- Domain hubs and sidebars progressively reveal categories and final topics.
- Folder depth is a UX decision: avoid both huge flat sibling lists and deep chains of low-value folders.
- Final topic pages expose predictable study actions: Read, Code, Practice, Solution, Scenario, Design, or Diagram, depending on schema.

## Content Roles
- `index.md`: theory, mental model, trade-offs, examples, gotchas, Quick recall. It must not be an exercise worksheet.
- `playground/`: Java source only. Code mode reads from here.
- `exercise/index.md`: prompts, task context, starter instructions, acceptance criteria.
- `solution/index.md`: answers/explanations for practice modules.
- `design/index.md`: system-design answer/rubric/diagram page.
- `assets/`: images and `.drawio` files only.
- Loose topical markdown under curriculum is not allowed; route it as `index.md`, merge it, or move it under `todo/archive`.

## Schema Families
- Theory-only: `index.md`.
- Interactive/code: `index.md`, `playground/`, `exercise/index.md`, `solution/index.md`.
- Practice: `index.md`, `exercise/index.md`, `solution/index.md`.
- System design: `index.md`, `exercise/index.md`, `design/index.md`, optional `assets/`.

All module `index.md` files need `order: X`. Child pages use:

- `exercise/index.md`: `order: 10`, `search: false`
- `solution/index.md`: `order: 20`, `search: false`
- `design/index.md`: `order: 20`, `search: false`

## Navigation Contract
- `scripts/generate-homepage.js` is the validation and navigation source of truth.
- It emits `docs/.vitepress/navigation_map.json`.
- VitePress config, homepage/domain grids, sidebars, and topic tabs/actions consume generated metadata.
- Hub pages must not contain hand-written local topic lists or manual generated UI components.
- Build/dev/pre-commit must fail on schema violations.

## Execution Contract
- Local Java execution is developer-only through `/api/run-java` in `docs:dev`.
- Static/public deployment needs a hosted or self-hosted sandbox before Run is exposed to visitors.
- Runner hardening requirements remain: loopback-only local mode, strict main-class/file validation, temp dirs, output caps, timeouts, process cleanup, no save endpoint.

## Memory And Lifecycle Contract
- Do not eagerly load large code/assets just to render navigation.
- Monaco/editor instances must dispose editor/model resources.
- Route changes must abort or ignore stale runs, diagram loads, timers, iframe work, and network requests.
- Output/logs must be capped.
- Diagram iframes and `medium-zoom` listeners must clean up on route changes.

## Completed Cleanup Summary
- Exercise-heavy Java/Spring `index.md` pages were split into practice pages.
- Misplaced Spring practice bucket was moved into canonical Spring modules.
- Method references, stream collectors, compilation-pipeline, loose Java/Spring/Networking/Security docs, resources, and progress pages were routed into schema-compliant folders.
- Duplicate nested-class content was merged into the canonical nested-classes topic.
- Generator now rejects worksheet topic pages and loose curriculum markdown.

## Future Work
- Improve placeholder exercise/solution/design content using the stub backlog.
- Define structured multi-question practice with per-question solution panes.
- Decide whether VitePress default sidebar should be replaced with a custom accordion sidebar.
- Decide hosted/public code execution.
- Add new focused docs for any future UX or curriculum redesign instead of extending this spec.
