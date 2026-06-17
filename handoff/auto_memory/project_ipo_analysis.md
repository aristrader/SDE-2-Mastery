---
name: project-ipo-analysis
description: "IPO pattern analysis project — location, status, and how to resume"
metadata: 
  node_type: memory
  type: project
  originSessionId: 67a62914-016e-4845-ab59-569addf1841a
---

User has a separate Python project for Indian IPO pattern analysis at `/Users/swapnilagarwal/Visual_Studio_Projects/ipo-analysis/`.

**Why:** Personal research to find repeatable patterns in Indian IPOs (Mainboard + SME, 2020–present) for investment decisions.

**Stack:** Python (requests, cloudscraper, BeautifulSoup, pandas). Primary data source: Sharescart (2023–2025). No Java.

**Key docs in that project:**
- `docs/discussion.md` — full thought process, all decisions and reasoning, source discovery results, all patterns/queries considered
- `docs/design.md` — complete 127-column schema, architecture, methodology cautions
- `TODO.md` — done/active/deferred checklist. Next step: build `scrapers/sharescart.py`

**How to resume:** Open the `ipo-analysis/` folder in a new Claude session and say:
> "Read docs/discussion.md, docs/design.md, and TODO.md in order. Confirm understanding then continue from Active section in TODO.md."

**Why:** Context cannot carry over from the TestingTesting project session to the ipo-analysis session — the docs were built specifically to serve as the handoff.
