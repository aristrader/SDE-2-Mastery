# Curriculum Schema Migration Plan

## Status
Completed on `chore/schema-migration`. Keep this as migration history and future restructuring guidance only.

## Durable Migration Rules
- Folder shape is navigation shape.
- Use the lightest schema that fits the learning value.
- Do not create fake exercise/solution/design pages just to satisfy an old pattern.
- `index.md` is theory, not an exercise worksheet.
- `playground/` is Java source only.
- `exercise/index.md` owns prompts.
- `solution/index.md` and `design/index.md` own answers/rubrics.
- Loose topical markdown under curriculum is not allowed; route it, merge it, or archive it under `todo`.
- Folder names, H1 titles, and navigation labels should agree.
- After moving/renaming/deleting curriculum content, run `node scripts/generate-homepage.js`.

## Completed Phases
- Root/domain hub pages created.
- Legacy folders normalized.
- Existing modules migrated into schema families.
- Child frontmatter normalized with `search: false`.
- Generated navigation map wired into VitePress.
- Pre-build/pre-commit validation added.
- Java/Spring content-role cleanup completed for the first slice.
- Loose curriculum markdown routed or archived.

## Future Restructures
Create a fresh dated plan for the specific restructure. Include:

- source paths
- destination paths
- no-data-loss verification
- schema validation changes if needed
- route/build/E2E checks

Do not extend this historical migration plan with new active tasks.
