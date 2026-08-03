# TODO: Content Layout Discussion

Future content-layout questions that are not part of the concrete LLD/IDE implementation plan.

## Inline vs Separate Practice

Current schema separates:

- `index.md`
- `exercise/index.md`
- `solution/index.md`

This may not be ideal for every topic. For concept-heavy topics like Lists, tiny drills such as add/remove/get/iterate may be better inline near the exact lesson section, while larger/heavier exercises stay on separate practice pages.

Later evaluate:

- Inline micro-exercises inside `index.md` for immediate reinforcement.
- Separate `exercise/index.md` for larger tasks, LLD-style work, interview drills, and multi-file practice.
- Separate `solution/index.md` only when answers are long enough to hide from first read.
- Whether navigation should show both "inline checks" and "Practice" without duplication.
- Whether ChatGPT import workflow should classify exercises as micro inline vs full practice page.

Do not change schema now. Track as future content-layout decision.

## Multi-Language Solutions

Later evaluate whether selected practice pages should support solutions in languages beyond Java.

Track:

- Which topics benefit from non-Java solutions, instead of forcing every page to support every language.
- How to show Java as the primary path while still allowing alternate-language answers.
- Whether exercises should share one prompt with multiple solution tabs/files.
- How playground/run support changes for languages that are not currently supported on-site.
- Whether study-plan rows need a language coverage marker.

Do not implement now. Decide after Java content and practice structure are stable.
