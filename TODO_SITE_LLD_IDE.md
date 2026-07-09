# TODO: Site LLD / Multi-File IDE Mode

Goal: make the site usable for Low-Level Design practice, where one exercise may need many Java files/classes and IDE-like editing.

## Feature: Multi-file workspace

- Allow one practice module to define multiple editable files, not just one snippet.
- Show a file tree for the exercise workspace.
- Support create, rename, delete, and reset file actions.
- Preserve starter files, user-edited files, and hidden/reference solution files separately.
- Keep module-owned files scoped to that practice route.

## Feature: Whole-module compile/run

- Compile and run the full workspace as one Java module/package.
- Support multiple classes, interfaces, enums, records, and tests/demos.
- Pick one configured entry point, such as `Main`, `*Run`, or exercise metadata.
- Show compile errors with file, line, and column.
- Keep execution sandboxed with timeouts, output caps, and process cleanup.

## Feature: IDE-like editor support

- Add autocomplete for Java keywords, classes in the workspace, and standard library types.
- Add diagnostics while typing where feasible.
- Add go-to-file and basic symbol search.
- Add formatting support.
- Add import assistance or quick fixes later.

## Feature: LLD exercise schema

- Add a schema for LLD modules, likely:
  - `index.md` for theory/problem framing
  - `exercise/index.md` for task and constraints
  - `solution/index.md` for design/code explanation
  - `playground/` for starter multi-file Java workspace
  - optional `tests/` or hidden checks later
- Support ordering and navigation like existing structured practice pages.
- Ensure generator validation rejects loose Java files outside allowed workspace folders.

## Feature: Persistence and reset

- Save user work locally per module.
- Provide reset-to-starter and reset-current-file actions.
- Avoid accidentally overwriting user code when docs update.

## Later checks

- Decide whether frontend uses Monaco, CodeMirror, or another editor.
- Decide whether compile/run stays local-only or uses hardened sidecar service.
- Add E2E coverage for multi-file create/edit/run flow.
- Add mobile fallback view, but primary target can remain desktop.
