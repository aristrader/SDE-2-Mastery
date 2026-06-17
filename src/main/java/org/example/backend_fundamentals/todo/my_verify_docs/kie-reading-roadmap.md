# KIE Reading Roadmap — Drools, jBPM, DMN & Kogito

**Source:** https://kie.apache.org/documentation/documentation_10.1.0/
**Goal:** Build mental model bottom-up — concepts → engines (Drools, DMN, jBPM) → cloud-native runtime (Kogito).

---

## Mental Model (read this first, refer back often)

| Layer | Component | What it does |
|---|---|---|
| Rules engine | **Drools** | Evaluates business rules over facts |
| Decision engine | **DMN engine** | Executes DMN models with FEEL expressions |
| Process engine | **jBPM** | Orchestrates BPMN 2.0 workflows |
| Cloud-native runtime | **Kogito** | Wraps all three for Quarkus / Spring Boot / Kubernetes |

**Rule of thumb:** Kogito is *not* a replacement for Drools/jBPM/DMN — it's a modern packaging of them. Learn the engines first, Kogito clicks faster.

---

## Phase 1 — Lay of the Land (½ day)

- [ ] KIE ecosystem overview — what each component is and how they fit
- [ ] Difference between **rules**, **decisions**, and **processes** (don't skip — this trips people up later)
- [ ] Authoring vs runtime — KIE projects, KJARs, KieBase, KieSession concepts at a high level

---

## Phase 2 — Drools (Rules Engine) (2–3 days)

The foundation. Kogito's rules support sits on top of this.

- [ ] Rule engine fundamentals — facts, working memory, agenda, Rete (high-level only)
- [ ] **DRL (Drools Rule Language)** syntax
  - [ ] `when` / `then` structure
  - [ ] Patterns, constraints, bindings
  - [ ] `accumulate`, `collect`, `exists`, `not`
  - [ ] Salience, agenda groups, rule attributes
- [ ] **Runtime model** — KieBase, KieSession (stateful vs stateless), KieContainer
- [ ] Authoring options
  - [ ] DRL files
  - [ ] Decision tables (spreadsheet-based)
  - [ ] Guided rules / guided decision tables
- [ ] Testing — scenario simulation, unit tests
- [ ] *(Optional)* Complex Event Processing (CEP) — only if your use case needs temporal reasoning over event streams

---

## Phase 3 — DMN (Decisions) (1 day)

The cleaner, standardized way to model decisions. Often preferable to raw DRL for business-readable logic.

- [ ] DMN spec basics — Decision Requirements Diagram (DRD)
- [ ] **FEEL** expression language — syntax, built-in functions
- [ ] DMN decision tables (vs Drools decision tables — when to use which)
- [ ] Boxed expressions — context, invocation, list, relation
- [ ] Testing DMN models

---

## Phase 4 — jBPM (Processes) (2 days)

The process engine. Kogito's process support is the cloud-native evolution of this.

- [ ] **BPMN 2.0 fundamentals** — tasks, gateways, events, lanes, pools
- [ ] jBPM engine basics — process instances, runtime manager, KieSession for processes
- [ ] Process variables and data flow between tasks
- [ ] **Human tasks** — task lifecycle, assignment, task service
- [ ] Sub-processes, signals, timers, boundary events, error handling
- [ ] Persistence — process state, history logs, audit
- [ ] Integration points — calling Drools rules and DMN decisions *from* a process

> **Skip-if-going-straight-to-Kogito:** Business Central / KIE Workbench UI tooling, jBPM-specific REST kie-server APIs, and older form modeler — Kogito replaces these. Keep your focus on **engine concepts**, not classic jBPM tooling.

---

## Phase 5 — Kogito (Cloud-Native Runtime) (3+ days)

Now you have the building blocks. Kogito is *how you ship them.*

- [ ] Kogito architecture overview
- [ ] Quarkus integration (primary) — also Spring Boot integration
- [ ] Build a first Kogito service combining a rule + a decision + a process
- [ ] **Serverless Workflow** (CNCF spec) — Kogito's modern, lighter-weight process flavor (alternative to BPMN for many cases)
- [ ] Supporting services
  - [ ] Persistence (Infinispan, MongoDB, PostgreSQL options)
  - [ ] Jobs Service (timers, scheduled work)
  - [ ] Data Index (querying process/decision data)
  - [ ] Trusty Service / Explainability (if relevant)
- [ ] Deployment — container images, Kubernetes Operator, OpenShift
- [ ] Observability — metrics, tracing, logs
- [ ] Code generation model — how Kogito turns BPMN/DMN/DRL into Java at build time

---

## Capstone — Build Something End-to-End

After Phase 5, build a small project to cement everything. Suggested example:

> **Loan approval service:** Drools rules check applicant eligibility → DMN decides interest rate → BPMN process orchestrates the whole flow including a human task for manual review on edge cases → deploy as a Kogito service on Quarkus.

This forces you to wire all four pieces together and surfaces the gaps in your understanding.

---

## Reading Tips

- Don't memorize syntax in Phase 2 — understand the *shape* of DRL. Real fluency comes when you build in Phase 5.
- If your use case is rules/decisions only, you can defer Phase 4 (jBPM/BPMN) and come back later.
- When the docs use the term "KIE Server" — that's the classic deployment model; Kogito services replace it for cloud-native deployments.
- Keep this checklist open in a side pane. Tick items as you go — visible progress is motivating across a multi-day read.

---

## Quick Glossary

- **KIE** — Knowledge Is Everything; umbrella project for Drools/jBPM/etc.
- **KJAR** — Kie JAR; deployable artifact containing rules/processes/decisions.
- **DRL** — Drools Rule Language.
- **DMN** — Decision Model and Notation (OMG standard).
- **BPMN** — Business Process Model and Notation (OMG standard).
- **FEEL** — Friendly Enough Expression Language (DMN's expression language).
- **CEP** — Complex Event Processing.
- **CNCF Serverless Workflow** — open spec for declarative workflows; supported by Kogito alongside BPMN.
