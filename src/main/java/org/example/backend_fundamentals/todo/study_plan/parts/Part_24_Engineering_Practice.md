# Part 24 — Engineering Practice & Communication

> **Sprint allocation:** Light touch — soft skills; practice ongoing, no dedicated week. **Budget: ~1-2 hrs (overflow slot).**

## 24 Engineering Practice & Communication — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 1 | Writing design docs — context, goals, non-goals, alternatives, tradeoffs | 🔴 💼 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | ~1.0 hr | 📖 `engineering_practice/DesignDocsAndRFCs.md` |
| 2 | RFC process, design reviews | 🔴 💼 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | ~0.5 hr | 📖 `engineering_practice/DesignDocsAndRFCs.md` |
| 3 | Code review etiquette — what's worth commenting on | 🔴 💼 | M | 45 min | [x] | [ ] | [ ] | [ ] | ~0.75 hr | 📖 `engineering_practice/CodeReviews.md` |
| 4 | Technical writing — clarity over cleverness | 🔴 💼 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | ~0.75 hr | 📖 `engineering_practice/TechnicalWriting.md` |
| 5 | Mentoring juniors | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 6 | Estimation, scoping, breakdown into stories | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 7 | Sprint hygiene, async communication | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 8 | Influence without authority (cross-team work) | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 9 | Tech talks, brown bags | 🟡 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~5.25 hrs | ~0.5 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~9.0 hrs | ~0.85 wk | |
| Full Part (all items including 🟡) | ~10.5 hrs | ~0.95 wk | ~3.0 hrs so far |

> This Part is soft-skills-heavy. Time estimates assume *reading + reflecting + producing artifacts* (a design doc, a brag log, a code review checklist). The real learning is over months of practice, not study hours.

## Frequently asked

1. **Q:** Walk through the structure of a design doc you'd write for a substantial feature.
   - **Why asked:** Senior signaling. Sections: (1) Context — why this matters now. (2) Goals + non-goals — explicit scope boundary. (3) Proposed design — what we'll build, with key decisions called out. (4) Alternatives considered — what we rejected and why. (5) Tradeoffs — what we're giving up. (6) Risks + mitigations. (7) Rollout plan. (8) Open questions. Always one page of TL;DR up top.
2. **Q:** What's worth commenting on in a code review?
   - **Why asked:** Senior-judgment signal. Worth: correctness bugs, missing edge cases, security issues, performance traps, unclear naming, missing tests for non-trivial logic, violations of project conventions. Not worth: style preferences (use linter), micro-optimizations, "I'd write it differently" (without reason). Bias toward asking questions over making demands.
3. **Q:** How do you estimate a substantial project (3+ months)?
   - **Why asked:** Senior planning. Break into known/unknown. For known parts: bottom-up estimate from comparable past work. For unknown: spike + estimate after. Add 50% buffer for integration, debugging, doc, surprises. Communicate range, not point. Re-estimate at milestones.
4. **Q:** Walk through influencing a cross-team decision without authority.
   - **Why asked:** Staff-engineer signal. (1) Understand their constraints first. (2) Frame in terms of their goals, not yours. (3) Provide data, not opinions. (4) Propose, don't demand. (5) Offer to help with the work. (6) Identify the actual decision-maker. (7) Persist patiently. (8) Accept "no" gracefully when reasonable.
5. **Q:** Mentoring juniors — what's your model?
   - **Why asked:** Promotion signal. Show vs. tell: pair on real work, give them context for *why* you're doing X, let them try and fail safely. Code reviews as teaching moments. Document the "this is why we don't do that" knowledge that's tribal. Don't take over their work; make them stronger.
6. **Q:** How do you write a brag doc / weekly notes?
   - **Why asked:** Promotion infrastructure. Brag doc: live document, append weekly. For each item: situation, action, outcome (STAR-style), with explicit impact. Notes: meeting decisions, problems solved, mentoring instances. Build over 6-12 months — your promotion case writes itself.
7. **Q:** Async communication — what's the senior pattern?
   - **Why asked:** Modern remote-friendly skill. Write more, meet less. Write decisions in writing-first form (so they're searchable). Document context in shared docs, not Slack threads (lost forever). For PRs / RFCs, expect reviewers to think → comment, not respond instantly. Batch your communication windows.

## Trick questions / gotchas

1. **Q:** Your design doc gets minimal feedback in review. You take that as approval. Two weeks later, a senior says "I had concerns I never raised." What went wrong?
   - **Gotcha:** Silent review ≠ approval. Best practice: explicitly ask for concerns ("what's the worst case I'm missing?"). Cold-call specific reviewers for input. Set a deadline by which silence counts as approval. Document who reviewed.
2. **Q:** You wrote a clever solution in 20 lines. A junior asks "why?" and you can't explain it concisely. What's the problem?
   - **Gotcha:** Cleverness is a tax. If you can't explain, others can't maintain. Senior signal: prefer obvious code over clever code, even when slightly longer. Or — if cleverness is necessary — write a multi-line comment explaining the *why* (not the what).
3. **Q:** You estimated 3 weeks. You're 4 weeks in. You think 2 more weeks. What do you do?
   - **Gotcha:** Tell the team NOW, not at week 6. Slipping by 50%+ and only telling at the deadline kills trust. The honest pattern: (1) at slip-detect, communicate ASAP, (2) explain the surprise, (3) offer revised estimate, (4) ask "do we still want this, scope down, or kill?" Re-decide with new info.
4. **Q:** Cross-team work — you delivered, they didn't. The combined feature didn't ship. Where's the failure?
   - **Gotcha:** Senior failure mode: you delivered "your part" but didn't ensure the integrated outcome. Senior pattern: take ownership of the *outcome*, not just your slice. Track the other team's progress, escalate blockers early, offer help. "I did my part" doesn't ship features.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **Write a real design doc** (~3 hrs) — for an actual upcoming KYC platform decision. Run through the full structure. Submit for review. Iterate based on feedback. The artifact AND the review experience are both the value.
- **Code review style guide** (~2 hrs) — write down your team's (or your own) code-review expectations. What to comment on, what to skip, tone, decision rights. Useful for onboarding juniors.
- **Brag doc + weekly notes habit** (~ongoing) — start a markdown file. Append weekly with impact-shaped entries. Six months in, you'll have promotion ammunition.

## Quick recall

**Q. Design doc structure?**
A. Context → Goals + non-goals → Proposed design → Alternatives considered → Tradeoffs → Risks + mitigations → Rollout plan → Open questions. One-page TL;DR up top.

**Q. Code review — what's worth commenting on?**
A. Correctness, security, performance traps, unclear naming, missing tests for non-trivial logic, project-convention violations. NOT style preferences (linter), or "I'd write it differently" without reason.

**Q. Estimation — range vs point?**
A. Always range. Point estimates create false confidence. "3-5 weeks, depending on how the vendor integration goes" is better than "4 weeks." Re-estimate at milestones.

**Q. Brag doc structure per entry?**
A. STAR: Situation, Task, Action, Result. Plus explicit impact statement (saved $X / improved Y by Z%). Quarterly review to clean up weak entries.

**Q. Influence without authority — first step?**
A. Understand their constraints. Frame your ask in terms of their goals. Provide data, not opinions. Propose, don't demand.

**Q. Async communication senior pattern?**
A. Write decisions in long-form (searchable). Document context in shared docs (not Slack threads). PRs and RFCs operate on respond-when-thoughtful cadence, not real-time. Batch communication windows.
