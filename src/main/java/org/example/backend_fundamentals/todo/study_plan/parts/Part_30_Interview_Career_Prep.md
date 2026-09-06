# Part 30 — Interview & Career Prep

> For SDE2 → SDE3 / Staff and for external moves. This Part is meta — it's about packaging everything from Parts 1–29 and 31 into a story interviewers want to buy.

> **Sprint allocation:** Light touch + intensifies Week 12 (mock-loop week). **Budget: ~1-2 hrs ongoing, peaks Week 12 (~10 hrs mock loop).**

## 30 Interview & Career Prep — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 1 | Coding round — brute force → optimize → code → test → edge cases | 🔴 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 2 | Machine coding / LLD — clarify → classes → interfaces → sequence → edge cases (45–90 min) | 🔴 🎯 | D | 2.5 hrs | [ ] | [x] | [ ] | [ ] | Partial: delivery framework and Parking Lot completed end-to-end; more timed mocks still pending | 📖 [Low-Level Design Interview Framework](/system_design/lld_interview_framework/) · 💻 `low_level_design/case_studies/parking_lot/exercise/index.md` · 📖 `low_level_design/case_studies/parking_lot/design/index.md` |
| 3 | System design / HLD — requirements → estimates → API → HLD → deep-dive → tradeoffs | 🔴 🎯 | D | 3 hrs | [x] | [ ] | [ ] | [ ] | Covered from Alex Xu pages 34-50 — estimation, clarify scope, HLD buy-in, API/schema timing, deep-dive selection, wrap-up, time allocation, dos/don'ts. Timed mock still useful separately | 📖 [System Design Interview Framework](/system_design/interview_framework/) · 📖 [Back-of-the-Envelope Capacity Estimation](/performance/capacity_estimation/) · 💻 Warm-up: 45-min HLD timed mock — pick a problem from `reference/PracticeProblems.md`, sketch requirements→estimates→API→HLD on paper (45 min) |
| 4 | Behavioral / leadership — STAR method, prepared stories (have 5–8 ready) | 🔴 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  | 💻 Warm-up: write down 3 STAR stories from your last year (situation → task → action → result + explicit impact) (45 min) |
| 5 | Hiring manager — motivation, growth, fit (Basic concept only) | 🔴 🎯 | M | 15 min | [ ] | [ ] | [ ] | [ ] |  |  |
| 6 | SDE2 vs SDE3 vs Staff vs Principal — scope of impact, ambiguity tolerance, influence radius (Basic concept only) | 🟠 🎯 | M | 15 min | [ ] | [ ] | [ ] | [ ] |  |  |
| 7 | Peer feedback — solicit early, give precisely (Basic concept only) | 🟠 🎯 | M | 15 min | [ ] | [ ] | [ ] | [ ] |  |  |
| 8 | Visibility compounders — design docs, tech talks, mentoring (Basic concept only) | 🟠 🎯 | M | 15 min | [ ] | [ ] | [ ] | [ ] |  |  |
| 9 | levels.fyi, Glassdoor, Blind — compensation benchmarks (Basic concept only) | 🟠 🎯 | M | 15 min | [ ] | [ ] | [ ] | [ ] |  |  |
| 10 | Negotiation basics — competing offers, signing bonus, RSU cliffs and vesting (Basic concept only) | 🟠 🎯 | MP | 15 min | [ ] | [ ] | [ ] | [ ] |  |  |
| 11 | Promotion criteria at your company — write them down, gap-analyze quarterly (Basic concept only) | 🟡 | M | 15 min | [ ] | [ ] | [ ] | [ ] |  |  |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~8.75 hrs | ~0.8 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~10 hrs | ~0.91 wk | |
| Full Part (all items including 🟡) | ~10.25 hrs | ~0.93 wk | |

> The real learning here is in *practice*, not study hours. Mock interviews + actual interviews compound faster than reading. Treat this Part as a forcing function: use mocks to discover gaps in Parts 1–29, then go deepen there.

## Frequently asked

1. **Q:** Walk me through how you'd approach a system design round.
   - **Why asked:** Most senior signal in a single round. Sequence: (1) Clarify requirements (functional + non-functional, scale, SLAs) — 5 min. (2) Estimate: QPS, storage, bandwidth — 3 min. (3) API design — 5 min. (4) High-level architecture (data flow, components) — 10 min. (5) Deep-dive on 1-2 components the interviewer picks — 15 min. (6) Tradeoffs + alternatives — 5 min. Don't jump to architecture before requirements are pinned.
2. **Q:** STAR method — give me an example from your work.
   - **Why asked:** Behavioral round signal. STAR = Situation (context), Task (your responsibility), Action (what you did, specifically), Result (outcome + measurable impact). Bad: "I improved performance." Good: "Service P99 was 2.5s breaching the 1s SLA; I was tech lead; I profiled with async-profiler, found a JDBC connection pool starvation under spike load, fixed the pool sizing + added circuit breaker, P99 dropped to 600ms, no SLA breach for 6 months."
3. **Q:** SDE3 vs Staff — what's the scope shift?
   - **Why asked:** Promotion-context calibration. SDE3: owns features end-to-end, mentors 1-2 juniors, contributes to design decisions. Staff: owns *systems* (multiple features / multiple teams), influences cross-team architecture, mentors seniors, drives org-wide tech strategy. The shift is from "I can build hard things" to "I make hard things possible for others."
4. **Q:** How would you negotiate a competing offer?
   - **Why asked:** Practical career skill. (1) Don't reveal numbers first if you can avoid it. (2) Have a real competing offer (or strong BATNA) — bluffing is detectable. (3) Negotiate the full package: base, signing, RSU, vesting cliff, equity refresh. (4) Use cliff and vesting curve as levers (4-year cliff is normal; 1-year cliff is normal at start). (5) Ask for the offer in writing; let the recruiter run internal escalation.
5. **Q:** How do you prepare for a coding round?
   - **Why asked:** Practical. Patterns over puzzles: hash maps for "find pair", two pointers for sorted arrays, BFS/DFS for graphs, DP for "count ways"/"min/max". Practice talking through brute force first; explicitly call out optimization moves. Test with small + edge + empty inputs. 30 min of warm-up per day for 2-3 weeks before a target interview beats cramming 3 hrs the day before.
6. **Q:** Bar-raiser round — what are they evaluating?
   - **Why asked:** Amazon-canonical, increasingly common elsewhere. Bar-raiser doesn't work in the team — their job is to enforce the hiring bar regardless of team pressure. They evaluate: leadership principles (or equivalent), depth of judgment, ability to disagree-and-commit, growth trajectory. Less about technical skill, more about decision-making and ownership patterns.
7. **Q:** External presence — does it matter?
   - **Why asked:** Career-strategy question. Matters most at: hiring (visibility shortens funnel), promotion to Staff+ (external rep is part of the case), founder-level moves. Matters least at: SDE2 → SDE3 (internal impact dominates). Lowest-effort high-leverage: thoughtful blog every 2 months, GitHub with real projects, LinkedIn presence with substantive posts.

## Trick questions / gotchas

1. **Q:** You give a great technical answer. Interviewer says "okay but why this and not X?" You say "I haven't really thought about X." What's the missing signal?
   - **Gotcha:** Senior interviews test *tradeoff awareness*. Knowing the answer isn't enough — you must know what you *didn't* choose and why. Even one sentence per alternative ("X would work but adds operational complexity for marginal benefit") is enough. Lack of comparison is a junior tell.
2. **Q:** You answer behavioral with "we" instead of "I." Why is this bad?
   - **Gotcha:** Behavioral rounds evaluate *your* impact, not the team's. "We optimized the cache" → "I led the cache optimization; my role was X." Don't claim sole credit if untrue, but don't bury your contribution. Most candidates underclaim from politeness; the interviewer can't tell what *you* did.
3. **Q:** You're asked to design a payment system. You start drawing immediately. What did you skip?
   - **Gotcha:** Requirements gathering. "How many payments per second? What's the consistency requirement? Cross-border or domestic? What's the SLA?" — these change the design fundamentally. Jumping to architecture without scope = junior signal. Even with experience, force yourself to ask 3-5 clarifying questions before drawing.
4. **Q:** You accept the first offer because you don't want to seem greedy. Six months later, you find a peer made 30% more. Lesson?
   - **Gotcha:** Negotiation isn't greed. Companies *expect* negotiation; they make first offers below their max precisely because they assume candidates will counter. Not negotiating signals you don't value yourself. Polite, specific counters ("based on competing offers / market data, I was hoping for X") are normal and respected.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **HLD interview practice — 5 full mocks** (~10 hrs) — pick 5 problems from `reference/PracticeProblems.md`, do timed 45-min mocks, record yourself, review for gaps. Mocks expose what reading doesn't.
- **LLD interview practice — 5 full mocks** (~10 hrs) — same pattern, focus on class design + sequence diagrams + edge cases.
- **STAR story bank — 8 stories** (~4 hrs) — write 8 behavioral stories covering: technical leadership, conflict resolution, mentoring, failure + recovery, cross-team influence, ambiguous-scope decision, on-call incident, scope cut. Refine over time.
- **Promotion plan to Staff** (~ongoing) — write down your company's Staff criteria. Quarterly gap analysis. Tie each gap to a specific upcoming project. This compounds.

## Hands-on exercises (Practice + Advanced)

Warm-up interview exercises are listed inline in the topic-table Resources column (counted in main Time summary). Longer exercises below are tracked separately.

### Practice — mid-level (~60-90 min each)

1. **LLD mock — Parking Lot or LRU Cache** (~75 min) — pick from `reference/PracticeProblems.md`. Timed: 15 min requirements, 30 min design, 20 min code skeleton, 10 min edge cases. Record yourself. Review for clarity + completeness.
2. **HLD mock — TinyURL or Rate Limiter** (~60 min) — pick from `reference/PracticeProblems.md`. Timed: 5 min requirements, 3 min estimates, 5 min API, 30 min HLD + deep-dive, 10 min tradeoffs. Record yourself.
3. **STAR story drafting session** (~60 min) — pick 3 work events from the last 12 months. Write each as STAR with explicit impact. Practice telling each in under 2 min out loud.

### Advanced — senior-grade depth (~90+ min each)

4. **Full mock interview loop simulation** (~3 hrs) — back-to-back: 1 coding round (45 min), 1 LLD (60 min), 1 HLD (60 min), 1 behavioral (45 min). Same day. This is the realistic fatigue test.
5. **Peer mock interview** (~90 min) — find a peer at similar level. They interview you, you interview them. Honest feedback after each. More signal than self-mocking.

### Hands-on time summary (Practice + Advanced only)

| Tier | Total time | Weeks @ 10–12 hrs/wk | Actual time |
|------|------------|----------------------|-------------|
| Practice (mid-level) | ~3.25 hrs | ~0.3 wk | |
| Advanced (senior-grade) | ~4.5 hrs | ~0.4 wk | |
| **Combined hands-on (Practice + Advanced)** | **~7.75 hrs** | **~0.7 wk** | |

> Warm-up exercises (counted in main Time summary above) total ~2.5 hrs for Part 30 across 3 in-table warm-ups.

## Quick recall

**Q. System design round sequence?**
A. Requirements → estimates → API → HLD → deep-dive → tradeoffs. Don't jump to architecture before scope is pinned. Always cover what you *didn't* choose.

**Q. STAR method?**
A. Situation, Task, Action, Result. Plus explicit impact (number, %, time saved). "I" not "we" — your contribution must be clear.

**Q. SDE3 vs Staff scope shift?**
A. SDE3 owns features; Staff owns systems across teams. Shift from "I can build hard things" to "I make hard things possible for others."

**Q. Negotiation — what to negotiate beyond base?**
A. Signing bonus, RSU grant, vesting curve, cliff, equity refresh, start date, level. Full package matters more than base alone.

**Q. Coding round — sequence?**
A. Brute force first (always) → identify bottleneck → optimize → code → test (small, edge, empty) → discuss complexity. Talk while you think.

**Q. Brag doc — why and what shape?**
A. Live document, append weekly. STAR-shaped entries with impact statements. Six months in, your promotion case writes itself. Currency for both internal promotion and external moves.
