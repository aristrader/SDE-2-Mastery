# HANDOFF — start here (onboarding for a fresh session / new account)

You are picking up **TestingTesting** — a **personal SDE-interview prep / learning workspace** (Java backend
fundamentals + design patterns + system design), paired with a Notion knowledge base. It is NOT a production
app and **NOT a git repo** (no version history) — so the folder itself + this handoff are the only record.
This pack exists so a NEW account/session can fully re-orient, and to capture the few things that don't travel
on a fresh login.

> **The repo IS the brain.** The detailed `CLAUDE.md` + the co-located study docs travel with the folder. This
> handoff routes you to them and captures what would otherwise be lost (auto-memory + the gitignored config).

## 1. READ THESE IN ORDER
1. **`CLAUDE.md`** (repo root) — the full project brain: what this repo is, the study-plan structure, the
   per-unit WORKFLOW + conventions, the end-of-pattern duties, the Notion-sync rules, the doc-quality standard.
   **Read it fully — it's comprehensive.** ⚠ One staleness note: CLAUDE.md references a `todo/study_plan/` tree
   at the root, but the actual study material currently lives co-located with code under
   `src/main/java/org/example/backend_fundamentals/` (see §2). Trust the filesystem over that path.
2. **`src/main/java/org/example/backend_fundamentals/todo/NotionLayout.md`** — the Notion publish tree + the
   source-map table + the `✅`/`📋`/`🔄` sync-status of every doc. This is the live index of what's been written
   and what needs syncing to Notion.
3. The study docs themselves — `.md` files co-located with the Java under `backend_fundamentals/` (e.g.
   `spring/`, `databases/`, `networking/`, `system_design/`, `design_patterns/`, `java/`). Each design-pattern
   package follows the shape: **Java variants + a `...Run.java` demo runner + a sibling `.md`** — preserve that.

## 2. WHAT THIS REPO IS (one paragraph)
A Spring Boot Maven module (Java 8 target, Boot 3.1.8 parent) used purely as a **study scaffold** — the Spring
machinery is ambient (no app/controllers/beans), so code can freely use Lombok/Spring idioms when a pattern
calls for it. The unit of work is **a concept (design pattern, Spring topic, system-design topic) + its
explanatory `.md`**, not a running service. Content is organized under `src/main/java/org/example/
backend_fundamentals/` (design_patterns, spring, databases, networking, system_design, java) with the study
markdown sitting next to the demonstrating code. The markdown is mirrored into **Notion** (the publish target).

## 3. THE WORKFLOW + KEY CONVENTIONS (from CLAUDE.md — the durable rules)
- **Context clears after every unit:** the user runs `/clear` after each design pattern / topic. Persistent
  state lives in `CLAUDE.md` + the docs + the auto-memory. So `CLAUDE.md` must always be current for the *next*
  session — treat the end-of-unit update like a commit.
- **End-of-unit duties (MANDATORY before /clear):** (0) fill actual time in the Part row + Time summary;
  (1) run the demo (`mvn -q compile && mvn -q exec:java -Dexec.mainClass="..."`) and confirm it works;
  (2) tick the pattern done in its roadmap; (3) update living/foundations docs with generic insights;
  (4) update `CLAUDE.md` if conventions/preferences changed; (5) update `NotionLayout.md` (tree + source map);
  (7) the **`🔄` Notion-sync rule** — editing an already-`✅` doc flips it to `🔄` until the user re-pastes and
  explicitly says "synced"; (8) end with a one-line "Notion update" summary.
- **Doc-quality bar (`DocCreationStandard.md` if present):** the single test — "would a senior Java backend
  interviewer actually ask this?" If no, don't write it. Prefer interview-prep density over textbook bloat.
- **Tier/tag/time symbols** (🔴/🟠/🟡/🟢 priority; 💼/🔐/🎯 tags; L/M/MP/D/VD time) — defined in CLAUDE.md.
- **Pure GitHub-flavored Markdown** (no HTML) so it pastes cleanly into Notion.

## 4. RUNNING IT
`mvn -q compile` to build; run a demo with `mvn -q exec:java -Dexec.mainClass="org.example.backend_fundamentals.<path>.<RunnerClass>"`.
Java 8 target, Maven (`pom.xml` at root). No tests of note; this is a study repo, not a service.

## 5. WHAT DOESN'T TRAVEL WITH A FRESH LOGIN (captured here)
1. **Auto-memory** (`handoff/auto_memory/`) — copied from `~/.claude/projects/.../memory/` (lives outside the
   repo). ⚠ It's trivial: an index + an **OBSOLETE** pointer to the separate `ipo-analysis` Python project
   (that project is fully self-contained now — ignore the pointer). Nothing load-bearing is in the memory; the
   brain is `CLAUDE.md`.
2. **Gitignored config** (`handoff/ENVIRONMENT.md`) — `.claude/settings.local.json` (the Maven/exec + WebFetch
   permission allowlist) is local and won't travel; recreate from that doc.
3. **No git** — there is NO version history; the folder copy is the entire record. Consider `git init` on the
   new setup if you want history going forward.

## 6. OWNER PROFILE (how to work WITH them)
Solo learner prepping for senior Java backend / SDE interviews. Works in tight units, clears context between
them, and mirrors everything to Notion. Wants interview-grade density (not textbook surveys), the
Java-variants + runner + `.md`-sibling shape preserved, and `CLAUDE.md` + `NotionLayout.md` kept current as the
session-to-session memory. Privacy/security-conscious (company laptop).
