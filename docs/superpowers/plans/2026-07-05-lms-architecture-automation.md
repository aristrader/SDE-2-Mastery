# LMS Architecture Automation Plan

## Status
Implemented and verified on `chore/schema-migration`. This file is now a compact handoff, not a live task dump.

Use new dated plans for future additions. Do not append large new checklists here.

## Implemented
- Recursive schema scanner and navigation generator: `scripts/generate-homepage.js`.
- Generated map: `docs/.vitepress/navigation_map.json`.
- VitePress integration: `docs/.vitepress/config.mjs`.
- Schema-aware page metadata for tabs/actions.
- Generated homepage/domain/topic grids from navigation metadata.
- Strict validation for:
  - missing `index.md`
  - missing/invalid `order`
  - duplicate sibling order
  - invalid folder names
  - exact exercise/solution/design child frontmatter
  - Java files outside `playground/`
  - non-Java files inside `playground/`
  - invalid system-design assets
  - manual generated UI embeds in markdown
  - manual local topic lists in hub pages
  - worksheet-style topic `index.md`
  - loose curriculum markdown outside `todo`
- Local Java runner UX and lifecycle hardening.
- Draw.io fixture path and ArchitectureBoard lifecycle checks.
- Content-role cleanup for Java/Spring coding-fluency and loose docs.
- Progress dashboard routed as `/progress/`.

## Verification Commands
Run after structural/site changes:

```bash
node scripts/generate-homepage.js
npm test
node scripts/site-route-audit.js
PLAYWRIGHT_USE_EXISTING_SERVER=1 node playwright_e2e.js
```

Mobile checks are future scope. Do not run or maintain them unless explicitly requested. If resumed later, the entry points are:

```bash
SITE_AUDIT_VIEWPORT=390x844 node scripts/site-route-audit.js
PLAYWRIGHT_RUN_MOBILE=1 PLAYWRIGHT_USE_EXISTING_SERVER=1 node playwright_e2e.js
```

Current verification passed:

- Generator validation.
- `npm test` including docs build.
- Desktop route audit.
- Desktop Playwright E2E.

## Remaining Product Decisions
- Hosted/public code execution: local dev runner works; deployed static site still needs a sandbox decision.
- Sidebar UX: decide whether generated VitePress sidebar is enough or replace with custom accordion.
- Structured multi-question practice: define stable question IDs, starter files/scratch areas, expected checks, and matching solution sections.
- Curriculum content quality: fill or remove placeholder exercise/solution/design pages using `docs/superpowers/plans/2026-07-05-curriculum-stub-backlog.md`.

## Rules For Future Plans
- Keep implementation plans scoped to one slice.
- Use Antigravity/cheap subagents for broad scans, then verify locally.
- Preserve content before deleting/moving files.
- Add validation before relying on convention.
- Run generator/build/route/E2E checks before commit.
