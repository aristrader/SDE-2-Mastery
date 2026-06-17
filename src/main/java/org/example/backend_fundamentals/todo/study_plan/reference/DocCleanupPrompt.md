# Doc Cleanup Prompt

Self-contained prompt for sweeping existing study-plan docs to remove textbook bloat. Paste into a subagent verbatim, then append the list of docs that subagent should review.

---

**Context:** Study plan docs for a senior Java backend SDE interview. The user is an SDE2 who writes Java/Spring Boot daily. These are reference material — not a textbook. Every line should earn its place by being something an interviewer could ask or a gotcha that bites in production.

**Your job:** Read each doc in your assigned list. For every section or paragraph apply this single test:

> "Would a senior Java backend interviewer actually ask about this, or would knowing it help answer a question they would ask?"

If no — cut it. No exceptions.

**Cut without hesitation:**

- Comparative analysis of alternatives Java doesn't use (open addressing, Python dict internals, .NET Dictionary, etc.)
- "When you'd pick X vs Y" for decisions a Java application developer never makes
- Specialized algorithms irrelevant to Java interviews (cuckoo hashing, Robin Hood hashing, hopscotch hashing, etc.)
- Historical context that doesn't directly explain a current Java gotcha or a question an interviewer would ask — "it used to work differently before Java X" with no bearing on what you do today gets cut
- Academic depth on JDK design choices where knowing the outcome is sufficient for interviews
- Any section the user must read entirely before realising it's skippable

**Calibration example — this is exactly the kind of content cut from `Hashing.md`:**

```
### Collision strategies in context — Java picked one of several

- Separate chaining — each bucket is a pointer to a list/tree of colliding entries.
  Used by Java HashMap, LinkedHashMap, Hashtable, and ConcurrentHashMap.
- Open addressing — one slot per bucket; if occupied, probe to the next slot
  (linear probing, quadratic probing, double hashing)...

### Open addressing — how the alternative actually works
[~80 lines: linear probing visual, tombstones, load factor comparison table
Java vs Python dict, when to pick open addressing...]
```

Why it was cut: Java uses separate chaining. The developer never picks a collision strategy. The entire open addressing section is content the user must read to decide to skip. The only thing salvaged was one phrase — "Java HashMap uses separate chaining" — folded as a one-liner into the existing collision section. Everything else gone.

Use this as your calibration benchmark. If a section feels like that — survey of alternatives, implementation choices the developer doesn't make, detail that exists for completeness not interviews — cut it.

**Keep without question:**

- How Java's actual implementation works — bucket model, treeification, CAS, happens-before, etc.
- Gotchas and pitfalls that bite in real Java code
- Comparisons between Java's own types (HashMap vs LinkedHashMap vs TreeMap, etc.)
- Performance characteristics of Java's actual collections and concurrency primitives
- Code examples showing correct vs incorrect patterns
- Interview questions with answers
- Quick recall Q&As

**Note on exercise-guide docs (Part 1b Coding Fluency):** These are exercise guides, not theory docs. The keep/cut test for these is: "does this help practice the skill or warn about a coding gotcha?" Academic theory beyond what's needed to complete the exercise gets cut.

**Format rules:**

- Pure GitHub-flavored Markdown only. No HTML.
- If you remove content that had a corresponding `## Quick recall` Q&A, remove that Q&A too.
- If a doc has a `## Done when` section (checklist style), remove it entirely OR convert any good questions in it into `## Quick recall` Q&A pairs — whichever keeps the doc cleaner. Never leave a `## Done when` section in place.
- Do not add new content. Only cut and clean up references to what was cut.

**Report:** For each doc, list what you cut and one-line why. If a doc is clean, say so. At the very end, after all docs, give a flat bullet list of every topic or section title cut across all your docs — just the names, no explanations. This is the tracking list.

---

**Append below this line in actual use:** the list of docs assigned to this subagent, with absolute paths.
