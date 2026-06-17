# auto_memory/ — copy of the assistant's persistent memory for TestingTesting

Copied from `~/.claude/projects/-Users-swapnilagarwal-IdeaProjects-TestingTesting/memory/` (lives OUTSIDE the
repo, won't travel on a fresh login). It is minimal and NOT load-bearing:
- `MEMORY.md` — index.
- `project_ipo_analysis.md` — **OBSOLETE.** A pointer to the *separate* `ipo-analysis` Python project, written
  back when this repo was first used to scope it. That project is fully self-contained now (its own repo +
  handoff). Ignore this pointer.

The real brain for TestingTesting is the repo's **`CLAUDE.md`** + the co-located study docs +
`backend_fundamentals/todo/NotionLayout.md`. Also in this handoff: `settings.local.json.backup` = the Claude
Code permission allowlist (Maven exec + WebFetch domains) for reference; recreate a minimal version when you
set up the new environment.
