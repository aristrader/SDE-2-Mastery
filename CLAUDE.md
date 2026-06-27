# CLAUDE.md

## Study plan — `todo/study_plan/` (the SDE-mastery execution map)

The user has built a structured 9-month study plan covering 32 Parts (~10-12 hrs/week). The per-Part docs in `todo/study_plan/parts/` are the authoritative source for the syllabus. All planning infrastructure lives in `todo/study_plan/`:

- **`README.md`** — start here. Navigation hub: folder layout, what-to-open-when table, tier symbols, tag legend, current phase + week pointer.
- **`MasterSchedule.md`** — Sprint Weeks 1-12: flat priority-Part schedule (no sub-phases), weekly rhythm (Mon-Thu study, Fri 30-min recall, Sat practice problem from Week 4, Sun slack), Friday recall rotation table, end-of-Sprint criteria.
- **`parts/Part_NN_*.md`** — 32 per-Part planning docs (the authoritative syllabus). Each has: 11-col topic table (#, Topic, Tags, Tier, Time, Done, Partial, Grilling, Visit Again, Notes, Resources) with priority-sorted rows + warm-ups inline in Resources; Time summary (3 scopes — 🔴 only / 🔴+🟠 / Full); Key diagrams (Mermaid, where useful); Frequently asked; Trick questions; Mastery candidates; Hands-on (Practice + Advanced) with own time summary; Quick recall.
- **`deep_dives/*.md`** — long-form study material that doesn't atomize into Part rows (DesignThinkingProcess, PatternSelectionExercise, PatternSelectionScenarios).
- **`reference/TopicIndex.md`** — alphabetical concept → Part + row pointer (~260 entries).
- **`reference/PracticeProblems.md`** — 25 LLD + 20 HLD + 20 design-pattern Qs + behavioral STAR themes.
- **`reference/Resources.md`** — books / courses / blogs / tools.
- **`reference/ConsolidationOutline.md`** — Months 4-9 high-level plan (post-Sprint).
- **`reference/DocCreationStandard.md`** — the bar for writing any new study-plan doc. Read FIRST whenever creating or substantially editing any `.md` referenced from a Part row (anything under `java/`, `spring/`, etc. that's linked from `parts/Part_NN_*.md`). The single test: "Would a senior Java backend interviewer actually ask about this?" If no — don't write it.
- **`reference/DocCleanupPrompt.md`** — subagent-ready prompt for sweeping existing docs to remove textbook bloat (academic alternatives, comparative surveys, "Done when" checklists, etc.). Use when an old doc reads like a textbook instead of interview prep.

**Conventions inside `study_plan/`:**
- Pure GitHub-flavored Markdown (no HTML) — pastes cleanly into Notion.
- Tier symbols: 🔴 MUST, 🟠 HIGH, 🟡 MEDIUM, 🟢 LOW.
- Tag symbols: 💼 work-relevant, 🔐 security/KYC, 🎯 interview-frequency.
- Time tiers: L = light (≤30 min), M = medium (~45-60 min), MP = medium-plus (~1.5-2 hrs), D = deep (~2-3 hrs), VD = very deep (≥3 hrs).
- Warm-ups (inline in Resources column, `💻 Warm-up: ...`) are counted in the main Time summary; Practice + Advanced exercises in the Hands-on section get their own mini-summary.
- The `Done`, `Grilling`, and `Visit Again` columns are checkboxes the user manually ticks; `Notes` is free-text. Don't fill these for the user (exception: the ChatGPT doc-import workflow ticks `Done`/`Partial` — see that section). `Done` = can give a 2-min explanation; `Partial` = row partially covered (Notes says covered-vs-pending; untick when it graduates to Done — these are the cheap wins to sweep later); `Grilling` = held up under hard follow-up questions from Claude; `Visit Again` = shaky or caught by a trick question.

**Maintenance rule — auto-sync TopicIndex.md when Part docs change:**
- Whenever you add, rename, or significantly reword a topic-table row in any `parts/Part_NN_*.md`, also update `reference/TopicIndex.md` so the index stays accurate.
- Add a corresponding alphabetical entry in `reference/TopicIndex.md` using the format `- **Topic name** → Part NN (row #) — secondary cross-refs.` (row # is helpful but not required if the index would get noisy).
- When you rename a row, update the matching index entry's label.
- When you delete a row, remove the matching index entry.
- When you move/split rows that already had index entries (e.g., the GuardDuty bundle split into 4 rows), update the index entries to match.
- Don't expand `reference/TopicIndex.md` beyond ~250 entries — be selective. A row added to a Part doesn't automatically deserve an index entry; add only if someone might search for it by that name.
- Do this in the same session as the Part edit, not as a follow-up. It's a cheap step that decays quickly when deferred.

**When the user references the plan, read `study_plan/README.md` first** — it's the entry point and tells you where everything else lives.

## What this repo actually is

This is a **personal design-patterns learning / prep workspace**, not a production application. Although the project is set up as a Spring Boot Maven module (Java 8 target, Spring Boot 3.1.8 parent, with webflux, validation, Guava, and Lombok on the classpath), the Spring machinery is largely unused — no `@SpringBootApplication`, no controllers, no beans wired up. The Spring dependencies are ambient, so code can freely reach for Lombok (`@Slf4j`, `@UtilityClass`) and Spring idioms when a pattern discussion calls for them.

The unit of work is a **design pattern + its explanatory `.md`**, not a running service. Each pattern package has Java classes demonstrating one or more variants, a `...Run.java` main class (the demo runner), and a sibling `.md` capturing the explanation, trade-offs, and decisions. Preserve that shape when adding code: **Java variants + runner + `.md` sibling**.

## Workflow — context clears between patterns

The user runs `/clear` after every design pattern is finished. Persistent state lives in this file and in the auto-memory system (`~/.claude/projects/.../memory/`).

### End-of-pattern duties — MANDATORY before context is cleared

When a pattern is finished, before context is cleared:

0. **Fill actual time in the Part row's `Notes` column AND update the Time summary** — update the single cumulative figure in the `Full Part` row only (same lightweight rule as the import workflow below; 🔴-only / 🔴+🟠 actual cells stay blank, no row-lists). Estimate active time from the session (elapsed working time, e.g. from `.claude/events.jsonl` timestamps); an approximate figure is fine.

1. **Run the demo** (`mvn -q compile && mvn -q exec:java -Dexec.mainClass="..."`) and confirm it still works.
2. **Tick the pattern done in `design_patterns/creational/CreationalPatternsRoadmap.md`**.
3. **Update the living docs** if any generic insights surfaced (`todo/study_plan/deep_dives/DesignThinkingProcess.md`, `java/foundations/access_modifiers/AccessModifiersDeepDive.md`, or the matching foundations doc under `design_patterns/foundations/`). Pattern-specific stuff belongs in that pattern's own doc.
4. **Update this `CLAUDE.md`** if new working-style preferences, conventions, anti-patterns, or repo-shape changes came out of the discussion. Treat the end-of-pattern update like a unit-of-work commit — the *next* session reads only this file.
Do not skip these. If skipped, the next session will start cold and lose context the user expected to carry over.

## ChatGPT doc-import workflow — `temp.md` (ACTIVE, survives /clear)

The user studies topics in ChatGPT threads, has ChatGPT generate a study page per thread (via the saved prompt in `todo/study_plan/reference/ChatGptDocImportPrompt.md`, which follows `reference/DocCreationStandard.md`), and imports those pages here one at a time. The routine — follow it whenever the user says they've pasted a doc into `temp.md` (repo root) or asks to "process" it:

1. **Read the temp file(s) completely** — `temp.md`, or `temp1`–`temp4` when several dumps are staged at once. **CRITICAL:** The internal reading tool has an 800-line limit per view. If a file is longer than 800 lines, you MUST page through it in chunks (e.g., lines 1-700, 701-1400, etc.) until you reach the end before processing. Never assume the top of the file represents the entire content. Then read `todo/study_plan/reference/DocCreationStandard.md` before processing.
2. **Standards pass** — restructure to the repo doc shape (direct start, dense sections, `## Quick recall` ending, pure GFM/no HTML, ```java blocks, no emojis). **Never drop a discussion point (especially pain points or struggles)** — cleanup means reorganize/rephrase/dedupe, not cut. *(Note: This rule explicitly overrides the "ruthless cutting" mandate in DocCreationStandard.md for temp.md imports).* **Preserve the conversational narrative & analogies.** When the dump contains "misconceptions", "analogies", or "step-by-step flowchart logic", preserve that rich narrative format. Do NOT compress a deep explanatory dive into a dry reference sheet. Non-interview tangents go to a `## Good to know` section near the end instead of being removed. Avoid shortening the user's content; only true redundancy may be collapsed.
3. **Extend where thin — but only topics actually discussed.** If a discussed topic is under-explained against the interview bar (missing internals, the "why", a classic gotcha), deepen it per DocCreationStandard. Do NOT add undiscussed subtopics just to close a Part-row gap (user explicitly declined this — e.g. don't bolt on IPv6/Anycast sections to fully earn a row the chat only partially covered). Extension deepens; shortening is not allowed.
4. **Group, Merge, or Split — decide yourself.** If one paste spans multiple distinct topics, split them. Conversely, if multiple uploaded temp files (or chunks of a long file) relate to the same topic or link to an existing document, **merge them together and improve the existing page** rather than creating fragmented separate files. The user has said: **don't ask where to put files — decide the destination/filename autonomously** and just state the choice. New top-level folders under `backend_fundamentals/` are fine (e.g. `networking/`, `messaging/` were created this way). **A new top-level folder MUST also be registered in `docs/.vitepress/config.mjs`** — add it to both the `nav` array and the `sidebar` `generateSidebar([...])` array, and give the folder an `index.md` hub (frontmatter `sidebar: false`, `pageClass: hub-page`) — otherwise it won't appear on the site. Subfolders of an already-registered folder (e.g. anything new under `databases/`) are auto-discovered and need no config change.
5. **Write the .md(s)** at the chosen location(s) under `backend_fundamentals/`, then **clear the processed temp file(s)** back to their two comment lines. **Never completely delete the files (do NOT use `rm`)** — always ensure `temp.md` and `temp1.md` through `temp4.md` remain physically on disk so the user doesn't have to recreate them manually. **Use absolute paths for every Bash file op** (clear/ls/find) — the shell CWD is *not* guaranteed to be the repo root, and a relative `> temp.md` once created stray empty files inside `backend_fundamentals/`. Never rely on `cd` persisting between calls.
6. **Update the Part doc(s)** in `todo/study_plan/parts/`:
   - If solid doc content matches **no existing Part row**, add a new row for it (priority-sorted position, sensible tier/time; e.g. row 31 "DHCP + MAC addresses" was added to Part 11 this way) and sync TopicIndex.
   - Tick `Done` (`[x]`) only on rows the doc **substantially** covers (explicit user instruction — overrides the "don't tick for the user" default; `Grilling` / `Visit Again` stay untouched). Partially covered rows stay unticked in `Done` but get **`Partial` ticked (`[x]`)**, a `Partial: <covered> ; <pending>` note in `Notes`, the 📖 link, and a call-out in the import report. When a later paste completes a partial row: tick `Done`, untick `Partial`.
   - Actual time: **~1.5 hr per paste, total** (regardless of how many files it splits into), split across covered rows in the `Notes` column. Approximate is fine — a single per-row figure, no arithmetic agonising.
   - Add a `📖 <path/to/Doc.md>` link in the Resources column of **every covered row**.
   - **Time summary — lightweight rule (token-saving, applies to ALL parts).** Maintain **one** running cumulative figure in the **`Full Part` row's "Actual time" cell** only (e.g. `~5.0 hrs so far`). Leave the 🔴-only and 🔴+🟠 "Actual time" cells **blank**. **Never** append a done/partial **row-list** to any cell — the table's `Done`/`Partial` checkboxes are the single source of truth (the old per-scope row-lists were pure duplication and have been removed). Every studied Part now follows this one format — keep it that way.
7. **Bookkeeping (batch at end of run).** Sync `reference/TopicIndex.md` **only when a row is added / renamed / deleted** — a plain `Done`/`Partial` tick needs no index change. Also add new docs to their folder `index.md` hub. When one run processes **multiple pastes**, do all index + hub updates as a single final sweep, not per paste.
8. **Import report** — end every run with a compact block (short table/list, no prose):
   - **Files created:** paths (note splits, e.g. "1 paste → 2 files").
   - **Part rows covered:** Part + row # + topic, marking Done vs Partial.
   - **Partial coverage:** for each partial row, covered vs pending (the user relies on this instead of gap-filling extensions).
   - **Timeline position:** one line — current Sprint week vs where the covered Part sits in the schedule (ahead / on track / behind). **Sprint anchor: Monday, May 18, 2026 = Week 1 start** (in `study_plan/README.md` "Current phase + week" — bump the Week if a new one started).
   - Keep figures **approximate** — do **not** burn tokens computing exact 🔴-done/total ratios or planned-vs-actual sums unless the user explicitly asks for the full standing.

## Pattern progress

See `design_patterns/creational/CreationalPatternsRoadmap.md` for the full completion checklist.

Pending side quest: DI orchestration mini-project (Strategy / Registry / Spring DI) — see `todo/study_plan/deep_dives/PatternSelectionExercise.md`.

## User's working style

- **Code-first, then guide.** For most patterns, the user writes the code themselves. Default to *guidance, review, and explanation* — describe shapes and trade-offs in chat without touching files. Wait for explicit "please write this" before generating code.
- **Hands-off mode is sticky.** Once the user signals it (e.g., "let me write the code, just guide me"), stay hands-off until they explicitly ask for a file write. Hands-off applies to `.java` *and* `.md` files in the active package.
- **Reviews are direct.** When asked "is this right?", give a yes/no with the exact issue and the exact fix. No hedging.
- **One pattern at a time.** Do not muddy explanations by mixing in adjacent patterns (Strategy creeping into a Factory Method discussion was a real confusion point). If multiple patterns genuinely apply to the same code, name that explicitly and offer to focus on one.
- **JavaDoc is teaching material.** Write rich JavaDocs on new code; cross-reference siblings with `{@link}`; explain *why* of design choices in class-level docs.
- **Living docs are deliverables, not chores.** When a discussion surfaces a generic insight, land it in the right `todo/` doc *during the same session*, not as a follow-up.
- **Suggest before persisting.** When the user clearly wants something noted but the location is ambiguous, propose where it should go and let them confirm before writing.
- **Exercise scaffolding is always two files.** The moment exercises are given for a topic, create both in the correct folder — no exceptions, no waiting for the user to ask:
  1. `*Practice.java` — skeleton only: package declaration, class shell, empty `main`. No implementation.
  2. `Exercises.md` — full exercise descriptions, one section per topic, with clear instructions and any "answer in a comment" questions.

## Commands

```bash
mvn -q compile                                              # fast sanity check
mvn -q clean compile                                        # full rebuild
mvn -q exec:java -Dexec.mainClass="<FQCN>"                 # run any demo runner
# FQCN = org.example.backend_fundamentals.<package>.<RunnerClass>
# No tests yet (src/test/java is empty) — don't invent tests unless asked.
```

When asked to verify a change works, compile + run the relevant `...Run` main rather than writing a unit test.

**Bash hygiene:** use **absolute paths** for file operations. The shell CWD is not guaranteed to be the repo root and does not reliably persist across calls — a relative `> temp.md` once wrote stray files into `backend_fundamentals/`. Don't depend on a prior `cd`.

## Code layout and the `.md`-alongside-code convention

> The `web/` folder (mirroring WEB RELATED CONCEPTS) does not currently exist on disk — its only doc was folded into `study_plan/parts/Part_11_Networking.md`, Part 13 (AWS) row 33, and Part 22 (DevOps) rows 9 + 18. Recreate `web/` if/when web-related demo code lands.

**Folder rules:** Maintain a structured tree under `backend_fundamentals/`. No files at `org/example/` root. No `package-info.java` placeholders. `scratch/` is outside `backend_fundamentals/`.

Each pattern package is **self-contained** — products, creators, services, and the runner all live together, even when names overlap with a sibling package (e.g., `simple_factory/`, `factory_method_basic/`, and `factory_method/` each have their own `Employee.java`, `AndroidDeveloper.java`, etc., and the four builder packages each carry their own copy of `JobOffer.java`). Do not consolidate; the duplication is intentional so each package reads as a standalone lesson and as a contrast against its siblings.

The root-of-package `.md` files are the primary deliverables alongside code. When code changes affect a pattern's explanation, update the matching `.md` in the same session.

## The `todo/` folder — living prep docs

`src/main/java/org/example/backend_fundamentals/todo/` contains living documents that grow with the user's learning, not implementation. Two categories of doc live here:

**Always lives in `todo/` (meta / process docs):**

- Deep-dive docs live in `todo/study_plan/deep_dives/` (not `todo/`): `DesignThinkingProcess.md`, `PatternSelectionExercise.md`, `PatternSelectionScenarios.md`.

**Topic docs in `todo/` while in progress, move out when done.** No active topic docs currently in `todo/`.

### Topic-doc lifecycle — the rule

A topic doc starts in `todo/` and only moves when the user signals completion. Don't move proactively.

**On completion:** (1) Review doc for stale refs, session deposits, selective demo cross-refs — integration, not authoring. (2) `mkdir` destination (no `package-info.java`), move the `.md`.

`AccessModifiersDeepDive.md` is the established example.

## Doc strategy and Website publishing

### Where each kind of insight goes

| Insight type | Lands in |
| --- | --- |
| Pattern-specific mechanics, code walkthroughs | That pattern's own `.md` (e.g., `Singleton.md`, `FactoryMethodProd.md`) |
| Generic design-thinking heuristics, traps, decision processes | `todo/study_plan/deep_dives/DesignThinkingProcess.md` |
| OOP pillars, SOLID, supporting principles, etc. | The matching foundations doc under `design_patterns/foundations/` or `java/foundations/` |
| Specific access-modifier / `final` / `abstract` reasoning | `java/foundations/access_modifiers/AccessModifiersDeepDive.md` |
| Pattern-selection / orchestration trade-offs | `todo/study_plan/deep_dives/PatternSelectionExercise.md` |

If unsure, lean toward the pattern's own doc; promote to a `todo/` doc only when the insight clearly generalises beyond the current pattern.

## Conventions to honour when editing

- **`.md` formatting:** pure Markdown only. Avoid HTML tags (`<p>`, `<b>`, `<i>`, `<ul>`, etc.) as they may render poorly. Tables, fenced ```java code blocks, `- [ ]` task lists, and standard headings all work cleanly.
- **Crisp by default; long only where it earns it.** Topic docs are reference material — quick overview now, deeper learning later. Keep sections short and direct. Expand only where the topic is genuinely confusing, where the user got stuck during the session, or where the short version would mislead. Twice as long isn't twice as useful.
- **End with a `## Quick recall` Q&A, not a "Done when" checklist.** Each answer is 1-2 lines max — a self-check, not a lesson.
- **JavaDoc only where it adds value, and keep it tight.** Don't javadoc the obvious — skip getters whose name says it all, trivial setters, self-documenting static factories like `Temperature.celsius(double)`. Do javadoc the *non-obvious*: a class's purpose and key design decision, contracts (`@throws`, format expectations), cross-references via `{@link}`, anything a careful reader couldn't infer from the name and signature. When you do write JavaDoc, keep it tight: class-level 3–8 lines, method-level 1–2 lines plus `@param` / `@return` / `@throws` only as needed. **Long-form lessons live in the matching `.md`, not in JavaDoc** — no `<h2>` sections, no multi-paragraph backstory, no takeaway sections, no feature tables inside JavaDoc.
- **Role-based class names.** `DeveloperHiringProcess`, `BillPughSingleton` — not `FactoryImpl` or `SingletonHelper`. The user has called this out explicitly.
- **Demo runners print to `System.out` and log via Lombok `@Slf4j`.** Keep this consistent when adding new runners.
- **Git Repository.** This project is tracked via Git. Changes are version-controlled, but still confirm before large deletions.

