<!-- claude --resume cebd43fb-bbe4-4e83-978c-721c100b4dd1 -->

# Study Plan — README

Navigation hub for the study plan. This file is enough to orient day-to-day.

## What this is

A 9-month executable study plan covering 32 Parts of backend / SDE mastery (per-Part docs in `parts/` are the authoritative syllabus). Three-month Sprint (12 weeks, priority-Part order, last 3 weeks shift toward mocks) then six-month Consolidation.

## Folder layout

```
study_plan/
├── README.md             ← you are here (read first)
├── MasterSchedule.md     ← historical Sprint calendar and tracking rules
├── parts/                ← the 32 Part docs (your syllabus)
├── deep_dives/           ← long-form study material
└── reference/            ← look up when you need it
    ├── TopicIndex.md
    ├── PracticeProblems.md
    ├── Resources.md
    └── ConsolidationOutline.md
```

## What to open when

| When you… | Open |
|-----------|------|
| Start the week (Mon morning) | `MasterSchedule.md` |
| Study a topic | `parts/Part_NN_<title>.md` |
| Forget where a concept lives | `reference/TopicIndex.md` |
| Saturday practice (Week 4+ LLD, Week 6+ HLD) | `reference/PracticeProblems.md` |
| Deep-dive a topic that needs more than its Part rows | `deep_dives/` |
| Need a book / video / tool | `reference/Resources.md` |
| Finish the Sprint | `reference/ConsolidationOutline.md` |
| Prepare for an interview now | `parts/Part_30_Interview_Career_Prep.md` plus a timed case-study exercise |

## How to read a Part planning doc

Each doc has:

1. **Topic table** — one row per syllabus bullet, priority-sorted (🔴 first, then 🟠, 🟡). Columns: # · Topic · Tags · Tier · Time · Done · Partial · Grilling · Visit Again · Notes · Resources. Resources is sparse (~20-30% of rows); icons: 📺 video · 🎓 tutorial · 📖 read · 💻 practice.
2. **Time summary** — 3-row table: 🔴 only / 🔴+🟠 / Full Part. Hours, weeks @ 10-12 hrs/wk, and an empty "Actual time" cell.
3. **Frequently asked** — 3-10 senior-level interview questions.
4. **Trick questions / gotchas** — 2-5 non-obvious traps.
5. **Mastery candidates** — top 3-5 from this Part (suggestions, not commitments).
6. **Quick recall** — 4-6 Q&As for self-test.

**Checkbox semantics:** tick **Done** when you can give a 2-min out-loud explanation. Tick **Partial** when a row is only partially covered (the `Notes` cell says what's covered vs pending) — these are cheap wins to wrap up later; untick Partial when the row graduates to Done. Tick **Grilling** when Claude has drilled hard questions on the topic and you've held up. Tick **Visit Again** when shaky or caught by a trick question. Edit by typing `x` between the brackets: `[ ]` → `[x]`. To find all partials: search `| [x] |` in the Partial column or grep `Partial:` in Notes.

## Tier symbols

| Symbol | Time | What it means |
| --- | --- | --- |
| **L** | 15-30 min | Definitions, term recognition |
| **M** | 30-60 min | One diagram or example |
| **MP** | 1-2 hrs | Examples + write a one-pager |
| **D** | 2-4 hrs | Mental model + trick-question handling |
| **VD** | 4-8 hrs | Hands-on demo or multi-page deep doc |

## Tag legend

| Priority | Context |
| --- | --- |
| 🔴 MUST | 💼 daily backend work |
| 🟠 HIGH | 🎯 system design / LLD interview |
| 🟡 MED | 🔐 KYC / identity domain |
| 🟢 LOW | 🆕 modern / emerging |
| ⚪ OPTIONAL | |

## Current phase + week

> **Sprint start:** Monday, May 18, 2026 (Week 1 = May 18–24)
> **Original Sprint end:** Sunday, August 9, 2026
> **Phase:** Post-Sprint interview preparation / application launch
> **Current source of truth:** Per-Part checkboxes and `Part_30_Interview_Career_Prep.md`; do not infer readiness from the historical calendar.

The Sprint calendar is retained as the original plan, not a claim of current progress. Unknown `Spent`, `Done rows`, and status values stay unrecorded rather than implying work was not done.

## Priority cleanup TODO

- [ ] Review the red tier one Part at a time. For each Part, propose both directions: demote red rows that are not normal interview must-haves, and promote lower-priority rows that are. Do not change any priority until the user approves that Part.

## If you're behind schedule

- Defer any unstarted Mastery items to Consolidation.
- Compress Conversant items to read + 5-min verbal self-explain (skip the deep write-up).

## If you're ahead

- Promote one 🟠 HIGH item from Consolidation into the current Sprint week.
- Pull a Part 4 structural pattern forward, OR walk one Part 31.9 cross-question out loud.
