# Doc Creation Standard

The bar for writing any new study-plan doc — both the top-level `parts/Part_NN_*.md` files and the content docs they link to (anything in `java/`, `spring/`, etc. referenced from a Part row's Resources column).

Apply this BEFORE writing, not after. If a draft fails this standard, cut rather than rewrite.

---

## The single test

Every section, paragraph, and example must pass this:

> "Would a senior Java backend interviewer actually ask about this, or would knowing it help answer a question they would ask?"

If no — don't write it. No exceptions. Length is never a virtue; depth in the right places is.

---

## Reader profile

The reader is an SDE2 who writes Java/Spring Boot daily, preparing for a senior Java backend interview. They:

- Already know how to use most Java/Spring APIs day-to-day. Skip basic-usage walkthroughs for topics they cold-know (e.g., basic HashMap usage, basic `@Transactional`, basic REST controllers). When unsure, default to skipping — the user will explicitly ask for a basic walkthrough if they need one for a less-familiar area (security/crypto, AWS, networking, etc.).
- Need to know **Java's specific choices deeply** — bucket model, treeification, CAS, happens-before, propagation rules, proxy mechanics — not comparative surveys of how other languages do it.
- Have limited brain context. Every line they have to read but then decide to skip costs more than it gives.

---

## What earns its place

- **How Java's actual implementation works** — internals, mechanisms, thresholds, the actual algorithm.
- **Gotchas and pitfalls** that bite in real Java code.
- **Comparisons between Java's own types** (HashMap vs LinkedHashMap vs TreeMap; `@Transactional` propagation modes; JDK vs CGLIB proxies).
- **Performance characteristics** of Java's actual collections and concurrency primitives.
- **Code examples** showing correct vs incorrect patterns — concrete, not abstract.
- **Interview-style questions with answers** — what gets asked, how to answer.
- **Quick recall Q&As** — 5-7 pairs, each answer 1-2 lines.

## What gets cut (or never written)

- Comparative analyses of alternatives Java doesn't use (open addressing, Python dict internals, .NET Dictionary, Ruby Hash internals).
- "When you'd pick X vs Y" for decisions a Java application developer never makes (you don't choose your hashmap's collision strategy).
- Specialised algorithms irrelevant to Java interviews (cuckoo hashing, Robin Hood, hopscotch, etc.).
- Historical context that doesn't explain a current gotcha or a question an interviewer would ask. Cut "it used to work differently before Java X" with no current bearing.
- Academic depth on JDK design choices where knowing the outcome is enough.
- Any section the reader must read entirely before realising it's skippable.

**Calibration example — content cut from `Hashing.md`:** a ~100-line walkthrough of open addressing (separate chaining alternative) including linear probing, tombstones, load factor tables comparing Java vs Python dict. Java uses separate chaining; the developer never picks. The only salvage was one sentence — "Java HashMap uses separate chaining" — folded into the existing collision section. Everything else gone.

If you're tempted to write something that feels like that — survey of alternatives, implementation choices the developer doesn't make, detail that exists for completeness — don't.

---

## Doc structure

### Theory docs (most foundation/concurrency/spring content docs)

Standard shape:

```
# Title

## How it works  (or domain-specific section names — internals, mechanism, contract)
## Gotchas / Pitfalls / Trick questions
## Comparisons within Java  (where applicable: types, modes, alternatives Java itself provides)
## Performance characteristics  (where applicable)
## Quick recall
```

- Start direct. No intro fluff about "what hashing is" if the reader is already past that.
- Each section short and dense. Two short paragraphs beats one long one.
- Code blocks fenced `\`\`\`java` — concrete patterns, not abstract examples.
- End with `## Quick recall` — 5-7 Q&A pairs, each answer 1-2 lines max.

### Exercise guides (Part 1b Coding Fluency style)

Standard shape:

```
# Title

## Domain model  (the types/setup the exercise builds on)
## Exercise 1 — <task name>
   Task → Gotchas → Acceptance criteria
## Exercise 2 — ...
## Trick questions / gotchas  (cross-exercise)
## Quick recall
```

The keep/cut test for exercise docs becomes: "does this help practice the skill or warn about a coding gotcha?" Academic theory beyond what's needed to complete the exercise gets cut.

---

## Format rules

- **Pure GitHub-flavored Markdown.** No HTML tags — they don't survive Notion paste.
- **H1 once** (title). H2 for main sections, H3 for subsections only when needed.
- **Tables** with `|` pipes for multi-column reference content.
- **Code blocks** fenced with ```java (or other language).
- **Lists** with `-` for bullets, `1.` for ordered.
- **Never `## Done when` (checklist style).** Use `## Quick recall` (Q&A style) instead. Checklists are textbook-style; Q&As are interview-style.
- **No emojis** except the tier/tag symbols used in Part-row tables (`🔴 🟠 🟡 🟢 💼 🔐 🎯 📖 💻`).

---

## When applying to an existing draft

If a section already exists but feels like the cut list — academic survey, alternative explanation, historical trivia, comparative content the reader will skip — cut it before publishing. Don't rationalise: if you have to argue for keeping it, it doesn't belong.

*(Exception: If you are processing a `temp.md` ChatGPT dump, follow the `CLAUDE.md` workflow rules instead: do NOT cut these if they were actively discussed. Move them to a `## Good to know` section to preserve the user's learning context.)*

For sweeping older bloated docs, use the sibling `DocCleanupPrompt.md` — that's the subagent-ready version of the cut criteria.
