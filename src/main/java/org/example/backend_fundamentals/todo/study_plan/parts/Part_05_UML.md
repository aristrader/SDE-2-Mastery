# Part 5 — UML & Design Documentation

> **Sprint allocation:** Week 3 (light pass alongside Part 4). **Budget: ~2 hrs.**

## 5 UML & Design Documentation — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|----------|-------------|-------|-----------|
| 1 | Class diagram — relationships (association, aggregation, composition, inheritance, dependency) | 🔴 💼 🎯 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | 📖 Refactoring Guru — "UML class diagram" quick ref (~20 min) |
| 2 | Sequence diagram — for any flow with > 2 services | 🔴 💼 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 3 | State diagram — perfect for KYC status, order state, etc. | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 4 | Activity diagram | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 5 | Component diagram | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 6 | Deployment diagram — infra-level | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 7 | C4 model — Context, Container, Component, Code (modern alternative) | 🟠 💼 🆕 | M | 1 hr | [ ] | [ ] | [ ] | [ ] | | 📖 c4model.com — official intro (~30 min) |
| 8 | Tooling — PlantUML, Mermaid (Markdown-native), draw.io, Excalidraw | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 🎓 Mermaid live editor — try sequence/state/class examples |
| 9 | Use case diagram | 🟡 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 10 | ER diagram | 🟡 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 11 | Object diagram, package diagram | 🟢 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~3.75 hrs | ~0.34 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~8.5 hrs | ~0.77 wk | |
| Full Part (all items including 🟡 + 🟢) | ~10.75 hrs | ~0.98 wk | |

> Short Part. Practical use matters more than theoretical fluency — draw 2-3 diagrams of your real KYC platform as the deliverable.

## Frequently asked

1. **Q:** Association vs Aggregation vs Composition — distinguish with concrete examples.
   - **Why asked:** Most-confused trio in UML. Association = generic "uses" relationship (Employee uses Computer). Aggregation = whole-part with independent lifetimes (Department has Employees, but Employees outlive Department). Composition = whole-part with bound lifetimes (Order owns OrderItems, OrderItems die with Order). Arrowheads differ: line / open diamond / filled diamond.
2. **Q:** When do you reach for sequence diagram vs state diagram vs activity diagram?
   - **Why asked:** Tests diagram judgment. Sequence = inter-service interactions over time, lifelines + messages. State = single entity's lifecycle, states + transitions. Activity = workflow / business process, swim lanes + decisions. Pick by what question you're answering.
3. **Q:** What's the C4 model and how does it differ from traditional UML?
   - **Why asked:** Modern alternative gaining traction. C4 = 4 nested zoom levels: Context (system in its env), Container (apps/databases), Component (modules inside containers), Code (classes — usually skipped). Strengths over UML: simpler vocabulary, opinionated about levels, less notation overhead, prescribes audience per level.
4. **Q:** Walk through how you'd diagram the KYC verification flow for a new joiner. Which diagram types, in what order?
   - **Why asked:** Tests practical communication skill. Likely answer: (1) C4 Container diagram showing SDK → orchestrator → vendors; (2) Sequence diagram showing one verification end-to-end; (3) State diagram of the KYC status machine. Top-down zoom-in pattern.

## Trick questions / gotchas

1. **Q:** Open diamond vs filled diamond — what's the actual semantic difference in code?
   - **Gotcha:** Open (aggregation) = "has-a" with shared lifecycle. The parts can exist without the whole. Filled (composition) = "owns" — when the whole is destroyed, parts must be destroyed too. In Java/Spring terms: composition often implies the parent is responsible for creating + destroying the child. Most "uses" relationships are actually association (plain line) — UML over-uses diamonds.
2. **Q:** What does a dashed line arrow in a class diagram mean?
   - **Gotcha:** Dependency — class A *uses* class B (e.g., as a method parameter or local variable) but doesn't store B as a field. Solid line = stronger relationship (association: stores reference). Easy to confuse.
3. **Q:** In a state diagram, what's the difference between a guard `[x > 5]` and an action `/ doSomething()`?
   - **Gotcha:** Guard appears in square brackets on the transition, evaluated to decide IF the transition fires. Action appears after a slash, executed WHEN the transition fires. Many state diagrams omit guards entirely — a common error.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **State diagram of the KYC status machine** (~2 hrs) — directly your platform. Diagram IN_PROGRESS / VERIFIED / REVIEW / ERROR / FAILED + the transitions, guards, and entry/exit actions. Becomes a portable artifact for design discussions + STAR stories.
- **C4 Container + Component diagrams of the KYC orchestrator** (~2.5 hrs) — your platform documented in modern style. Useful for senior interviews when asked "design a KYC platform."
- **Sequence diagram of a verification flow end-to-end** (~1.5 hrs) — SDK launch → backend → 3 vendor calls → status callback → partner notification. Practice rendering complex multi-service flows.

## Quick recall

**Q. Association arrowhead types — quick ref.**
A. Plain line = association. Open arrow = directed association. Open diamond = aggregation (loose has-a). Filled diamond = composition (owns, dies-with). Dashed arrow = dependency (uses, doesn't store). Triangle arrow (hollow) = generalization / inheritance.

**Q. Aggregation vs composition — one-line distinction.**
A. Aggregation: parts outlive the whole. Composition: parts die with the whole. Composition implies stronger ownership in code (creation + destruction responsibility).

**Q. State diagram: `event [guard] / action` — what's each piece?**
A. Event triggers the transition. Guard is a boolean precondition (transition only fires if true). Action is executed when transition fires. Inside a state, you can also have entry/exit actions and do-activities.

**Q. C4 model levels in order from outside-in.**
A. Context (system + external actors) → Container (apps + databases inside the system) → Component (modules inside a container) → Code (classes inside a component — usually skipped because UML class diagrams cover it).
