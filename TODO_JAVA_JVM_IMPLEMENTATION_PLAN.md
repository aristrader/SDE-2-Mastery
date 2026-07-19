# TODO: Java/JVM Implementation Plan

Source review: `TODO_JAVA_JVM_HELLO_INTERVIEW_INSPIRED_PLAN.md`

Goal: implement the Java/JVM interview track in small commits. Each phase must be independently reviewable, testable, and revertable.

Rule: one phase = one commit. If a phase becomes large, split it into the named sub-phases below and commit each sub-phase separately.

## Global Rules

- Preserve unrelated user changes.
- Do not edit files outside the phase's `Touch only` list.
- The `Touch only` list implicitly allows this file only for that phase's `## Progress Log` entry.
- Do not create empty folders.
- Do not manually edit generated navigation data.
- Before substantial study-doc edits, read `src/main/java/org/example/backend_fundamentals/todo/study_plan/reference/DocCreationStandard.md`.
- Before changing topic rows in study-plan Part files, read the relevant Part file and update `src/main/java/org/example/backend_fundamentals/todo/study_plan/reference/TopicIndex.md` in the same phase.
- Do not tick `Done`, `Grilling`, or `Visit Again` for the user.
- Prefer existing focused child modules over creating hub-level exercise pages.
- Run `node scripts/generate-homepage.js` after any content structure change.
- Run `mvn -q compile` only when Java code or runnable playground files change.
- Run `npm test` before the final commit for this workstream, or earlier if generator/schema/theme behavior changes.
- Update this file's `## Progress Log` after each committed phase.
- Before every commit, review:

```bash
git diff --stat
git diff --name-status
git status --short
```

Commit rule:

- Stage only files touched by the current phase plus generated files created by required commands.
- Commit message format: `docs(java): <phase summary>`
- If validation fails, fix inside the same phase before committing.
- If a phase reveals unrelated broken existing content, record it in `## Progress Log`; do not fix it unless it blocks the phase.

Before creating a new page or folder, check:

```bash
find src/main/java/org/example/backend_fundamentals/java -path '*<topic>*' -name index.md | sort
```

Use the existing closest topic page unless the audit proves a new page is needed.

## Phase Boundaries

| Phase | Commit scope | Can be reverted without affecting later phases? |
| --- | --- | --- |
| 0 | Audit plus optional baseline generated navigation | Yes |
| 1 | Java root only | Yes |
| 3A | OOP theory cleanup | Yes |
| 3B | Collections theory cleanup | Yes |
| 3C | Generics theory cleanup | Yes |
| 3D | Concurrency theory cleanup | Yes |
| 3E | JVM theory cleanup | Yes |
| 4A-4E | One practice topic per commit | Yes |
| 5A-5F | One topic-local interview recap bundle per commit | Yes |
| 6A-6C | One visual per commit | Yes |
| 7 | Minimal UX polish | Yes |

If a later phase depends on an earlier link, keep the link degraded gracefully: no empty destinations, no placeholders.

## Definition Of Done For Every Content Page

A touched theory page must have:

- valid YAML frontmatter; new pages need `title` and `order`, existing pages may keep the repo's current valid `order`-only pattern
- Java-specific mechanism, not generic textbook coverage
- traps or trick questions
- interview answer shape
- `## Quick recall`
- no academic survey unrelated to Java backend interviews

A touched exercise page must have:

- frontmatter with `title: Exercises`, `order: 10`, `search: false`
- headings exactly like `## Exercise: kebab-id - Title`
- task, constraints, and checks
- no broad kata unrelated to the topic

A touched solution page must have:

- frontmatter with `title: Solutions`, `order: 20`, `search: false`
- headings exactly like `## Solution: same-kebab-id - Title`
- matching exercise IDs exactly
- approach, code or pseudo-code when useful, explanation, follow-up

A touched runnable Java example must:

- live under the topic's `playground/`
- compile with `mvn -q compile`
- use role-based class names
- print useful output if it has a runner

A touched link must:

- use a route/path that exists after generation
- avoid placeholders
- keep canonical topic links pointing to the deepest useful page, not just a hub

## Phase 0: Audit

Purpose: create the baseline and exact touch list. No content rewrite.

Touch only:

- `TODO_JAVA_JVM_IMPLEMENTATION_PLAN.md`
- `docs/.vitepress/navigation_map.json` only if `node scripts/generate-homepage.js` changes it

Do:

- inventory every `src/main/java/org/example/backend_fundamentals/java/**/index.md`
- classify each page
- count exercise pages, solution pages, and playground directories
- map hub pages to existing focused child pages
- identify broken or missing `## Quick recall`
- identify pages that should stay theory-only
- identify whether planned Phase 4 practice should update existing exercise/solution pages instead of creating new ones
- write the result under `## Audit Result`
- write the concrete next touch list under `## Phase 1 Approved Touch List`

Do not:

- rewrite Java docs
- create shortcut route pages during audit
- create exercise or solution folders
- manually edit generated navigation

Commands:

```bash
find src/main/java/org/example/backend_fundamentals/java -name index.md | sort
find src/main/java/org/example/backend_fundamentals/java -type d \( -name exercise -o -name solution -o -name playground \) | sort
rg -L '^## Quick recall$' -g 'index.md' -g '!**/exercise/**' -g '!**/solution/**' src/main/java/org/example/backend_fundamentals/java
rg -n '^## Exercise:|^## Solution:' src/main/java/org/example/backend_fundamentals/java
node scripts/generate-homepage.js
```

Manual review cases:

- Java root explains a path, not just navigation.
- Strong pages are not scheduled for rewrite unless they block Phase 1.
- Theory-only pages are not assigned fake exercises.
- Existing exercise/solution IDs are paired if present.
- Planned practice targets use existing focused child modules where they already exist.

Exit gate:

- `## Audit Result` is filled in
- `## Phase 1 Approved Touch List` is filled in
- validation passes
- any generated navigation diff is understood as baseline drift from the generator
- commit Phase 0

## Phase 1: Java Root

Purpose: make `/java/` a usable study hub without changing child pages.

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Do:

- add clear track purpose
- keep `/java/` as a minimal hub
- rely on generated navigation for sections/topics
- avoid manually maintained topic tables, study routes, priority maps, practice maps, skip lists, or quick-recall content on the hub

Do not:

- create shortcut route pages
- edit child topic pages
- add CSS
- add schema/frontmatter fields beyond what already exists
- create practice pages
- hardcode topic navigation that can drift from folders/files

Testing direction:

```bash
node scripts/generate-homepage.js
```

Manual review cases:

- The page is a simple hub, not a duplicate study plan.
- Section/topic navigation comes from generated navigation.
- No manually maintained route tables can drift from folders/files.
- No UI-specific work slipped in.

Exit gate:

- validation passes
- diff only contains Java root and generated navigation if any
- commit Phase 1

## Phase 3: High-Value Theory Cleanup

Purpose: improve content quality without creating new practice surfaces.

Shared rules for all Phase 3 sub-phases:

- one cluster = one commit
- do not create exercise folders
- do not add visuals
- do not edit unrelated cluster pages
- keep existing useful examples
- cut low-value breadth before adding new text
- preserve frontmatter

Validation after each sub-phase:

```bash
node scripts/generate-homepage.js
```

Run only if Java files changed:

```bash
mvn -q compile
```

Manual review cases for each touched page:

- Mechanism is Java-specific.
- Common trap is explicit.
- Interview answer is concise.
- Quick recall exists and is short.
- Page did not become a catch-all.

### Phase 3A: OOP Contracts

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/object_identity/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/equals_contract/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/hashcode_contract/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/hash_collections_traps/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/object_model/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/method_dispatch/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must cover:

- equality contract
- mutable key trap
- identity vs equality
- dynamic dispatch
- overloading vs overriding
- how to answer `equals`/`hashCode` in an interview

Exit gate:

- validation passes
- commit Phase 3A

### Phase 3B: Collections Internals

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/collections/maps/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/maps/hashmap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/maps/linked_hashmap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/maps/treemap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/maps/concurrent_hashmap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/hashing/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/sorting/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must cover:

- HashMap lookup path
- bucket, collision, resize, treeification at interview depth
- equality/hashCode connection
- ordering and comparator traps
- when HashMap, LinkedHashMap, TreeMap, ConcurrentHashMap differ

Must cut:

- non-Java hash table algorithm surveys
- deep algorithms Java developers do not choose

Exit gate:

- validation passes
- commit Phase 3B

### Phase 3C: Generics

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/generics/basics/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/bounds/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/wildcards/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/erasure/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must cover:

- invariance
- bounded type parameters
- wildcard reads vs writes
- PECS
- erasure
- raw types
- heap pollution

Exit gate:

- validation passes
- commit Phase 3C

### Phase 3D: Concurrency Core

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/concurrency/race_conditions/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/synchronized_keyword/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/volatile_keyword/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/jmm/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must cover:

- race condition diagnosis
- visibility vs atomicity
- happens-before
- synchronized semantics
- volatile semantics
- when volatile is not enough
- interview explanation of JMM without overdoing formalism

Exit gate:

- validation passes
- commit Phase 3D

### Phase 3E: JVM Basics

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/jvm/jdk_jre_jvm/index.md`
- `src/main/java/org/example/backend_fundamentals/java/jvm/compilation_pipeline/index.md`
- `src/main/java/org/example/backend_fundamentals/java/jvm/bytecode_execution/index.md`
- `src/main/java/org/example/backend_fundamentals/java/jvm/gc/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must cover:

- JDK vs JRE vs JVM
- javac to bytecode to execution
- class loading at high level only
- interpreter/JIT enough for interview answers
- GC roots and generations/regions at practical depth
- pause explanation at practical depth

Must cut:

- bytecode instruction catalogs
- GC tuning flag catalogs
- custom classloader implementation
- JIT internals beyond interview relevance

Exit gate:

- validation passes
- commit Phase 3E

## Phase 4: Code Practice

Purpose: add practice only where writing or debugging code proves the concept.

Shared rules:

- one practice topic = one commit
- each exercise ID has exactly one matching solution ID
- update existing focused exercise/solution pages when they already exist
- add `playground/` only if code should compile or run
- do not broaden tasks beyond the topic
- do not create practice for theory-only pages

Required exercise frontmatter:

```yaml
---
title: Exercises
order: 10
search: false
---
```

Required solution frontmatter:

```yaml
---
title: Solutions
order: 20
search: false
---
```

Testing direction for every Phase 4 sub-phase:

```bash
node scripts/generate-homepage.js
mvn -q compile
```

Manual review cases:

- Exercise can be attempted without reading the solution.
- Solution directly answers the exercise.
- Constraints catch the actual trap.
- Playground code compiles if added.
- No unrelated lessons are embedded in the exercise page.

### Phase 4A: equals/hashCode Mutable Key

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/hash_collections_traps/exercise/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/hash_collections_traps/solution/index.md`
- optional `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/hash_collections_traps/playground/**`
- `docs/.vitepress/navigation_map.json` only if regenerated

Required case:

- mutate a key after insertion into a `HashMap`
- predict lookup behavior
- fix with immutable key or stable equality fields

Exit gate:

- validation passes
- commit Phase 4A

### Phase 4B: HashMap Lookup/Collision/Resize

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/collections/maps/hashmap/exercise/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/maps/hashmap/solution/index.md`
- optional `src/main/java/org/example/backend_fundamentals/java/collections/maps/hashmap/playground/**`
- `docs/.vitepress/navigation_map.json` only if regenerated

Required case:

- reason about lookup path
- explain collision handling
- explain why resize changes bucket positions

Exit gate:

- validation passes
- commit Phase 4B

### Phase 4C: volatile/JMM Visibility Bug

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/concurrency/volatile_keyword/exercise/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/volatile_keyword/solution/index.md`
- optional `src/main/java/org/example/backend_fundamentals/java/concurrency/volatile_keyword/playground/**`
- `docs/.vitepress/navigation_map.json` only if regenerated

Required case:

- visibility bug
- why `volatile` fixes visibility
- why `volatile` does not make compound updates atomic

Exit gate:

- validation passes
- commit Phase 4C

### Phase 4D: Generics Wildcard API Design

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/generics/wildcards/exercise/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/wildcards/solution/index.md`
- optional `src/main/java/org/example/backend_fundamentals/java/generics/wildcards/playground/**`
- `docs/.vitepress/navigation_map.json` only if regenerated

Required case:

- choose `? extends` vs `? super`
- explain reads and writes
- avoid raw types

Exit gate:

- validation passes
- commit Phase 4D

### Phase 4E: Stream Laziness

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/interview_drills/exercise/index.md`
- `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/interview_drills/solution/index.md`
- optional `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/interview_drills/playground/**`
- `docs/.vitepress/navigation_map.json` only if regenerated

Required case:

- predict operation order
- demonstrate terminal operation
- show short-circuiting
- warn against `parallelStream` by default

Exit gate:

- validation passes
- commit Phase 4E

## Phase 5: Topic-Local Interview Recaps

Purpose: keep interview prep close to the topic it tests. Do not create a separate top-level Java interview lane.

Shared rules:

- one high-level module = one commit
- create recap pages only inside the owning module, for example `java/generics/interview_recap/`
- do not create `java/interview_drills/`
- do not move or delete existing full exercises
- questions may link to or repeat existing exercises when that is the best coverage
- keep recap content short; full learning content stays in the normal topic pages

Required recap bundle:

- `interview_recap/index.md`
- `interview_recap/quick_revision/index.md`
- `interview_recap/questions/index.md`

Required recap hub frontmatter:

```yaml
---
title: Interview Recap
order: 90
---
```

Required quick revision frontmatter:

```yaml
---
title: Quick Revision
order: 10
search: false
---
```

Required questions frontmatter:

```yaml
---
title: Interview Questions
order: 20
search: false
---
```

Content rules:

- `quick_revision/` is a compact recall sheet only:
  - method names and API names where useful, for example streams
  - high-level topic names and one-line definitions where useful, for example GC or generics
  - common traps in one line each
- `questions/` contains only 2-3 strong questions for that module
- each question should prove whether the person knows the topic enough to move ahead
- answers should be short or link to the existing canonical exercise/solution
- no broad question banks
- no duplicate top-level navigation hub

Testing direction:

```bash
node scripts/generate-homepage.js
```

Run only if Java files are added:

```bash
mvn -q compile
```

Manual review cases:

- Recap is discoverable inside the topic/module it belongs to.
- Quick revision is fast recall, not a second textbook page.
- Questions are few and high signal.
- Existing exercises remain in their original folders.
- Links point to existing canonical topic/exercise/solution pages.

### Phase 5A: Generics Interview Recap

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/generics/interview_recap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/interview_recap/quick_revision/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/interview_recap/questions/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Exit gate:

- validation passes
- validation passes
- commit Phase 5A

### Phase 5B: JVM Interview Recap

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/jvm/interview_recap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/jvm/interview_recap/quick_revision/index.md`
- `src/main/java/org/example/backend_fundamentals/java/jvm/interview_recap/questions/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Exit gate:

- validation passes
- validation passes
- commit Phase 5B

### Phase 5C: Concurrency Interview Recap

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/concurrency/interview_recap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/interview_recap/quick_revision/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/interview_recap/questions/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Exit gate:

- validation passes
- validation passes
- commit Phase 5C

### Phase 5D: Collections Interview Recap

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/collections/interview_recap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/interview_recap/quick_revision/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/interview_recap/questions/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Exit gate:

- validation passes
- commit Phase 5D

### Phase 5E: OOP Interview Recap

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/oop/interview_recap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/interview_recap/quick_revision/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/interview_recap/questions/index.md`
- `docs/.vitepress/navigation_map.json` only if regenerated

Exit gate:

- validation passes
- commit Phase 5E

### Phase 5F: Streams Interview Recap

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/interview_recap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/interview_recap/quick_revision/index.md`
- `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/interview_recap/questions/index.md`
- existing `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/interview_drills/**` only if adding links, not deleting or moving exercises
- `docs/.vitepress/navigation_map.json` only if regenerated

Exit gate:

- validation passes
- commit Phase 5F

## Phase 6: Visuals

Purpose: add optional visual explanations after text is stable.

Shared rules:

- one visual = one commit
- no heavy dependency
- no decorative diagrams
- text must stand without the visual
- visual must be readable on mobile
- do not start if Phase 3 text for that topic is still weak

Testing direction:

- run `npm run docs:dev:light`
- run `npm run docs:build` if CSS, assets, or custom Markdown layout is added
- smoke test desktop
- smoke test mobile around 390px width
- check no overlap and no unreadable labels

### Phase 6A: HashMap Visual

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/collections/maps/index.md`
- optional local asset under `src/main/java/org/example/backend_fundamentals/java/collections/maps/assets/`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must show:

- bucket lookup
- collision
- resize effect

Exit gate:

- visual helps explain the text
- mobile check passes
- commit Phase 6A

### Phase 6B: Stream Laziness Visual

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/index.md`
- optional local asset under `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/assets/`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must show:

- intermediate operations are lazy
- terminal operation triggers work
- short-circuiting stops work

Exit gate:

- visual helps explain the text
- mobile check passes
- commit Phase 6B

### Phase 6C: JMM Visual

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/concurrency/jmm/index.md`
- optional local asset under `src/main/java/org/example/backend_fundamentals/java/concurrency/jmm/assets/`
- `docs/.vitepress/navigation_map.json` only if regenerated

Must show:

- thread-local observation vs shared memory at a conceptual level
- happens-before edge
- why visibility differs from atomicity

Exit gate:

- visual helps explain the text
- mobile check passes
- commit Phase 6C

## Phase 7: Minimal UX Polish

Purpose: improve navigation clarity only after content is useful.

Touch only:

- `src/main/java/org/example/backend_fundamentals/java/index.md`
- selected Java module hub `index.md` files from the audit-approved list
- `docs/.vitepress/theme/custom.css` only if Markdown cannot express the needed clarity
- `docs/.vitepress/navigation_map.json` only if regenerated

Allowed:

- plain Markdown priority labels
- "practice available" links
- "next best page" links
- restrained Java root sections

Still blocked:

- global theme redesign
- progress persistence
- login
- comments
- AI tutor
- schema-based badge system
- decorative hero sections

Testing direction:

- run `npm run docs:dev:light`
- run `npm run docs:build` if CSS changes
- smoke test `/java/`
- smoke test touched module hubs
- smoke test mobile around 390px width
- check no unreadable cards or text overlap

Exit gate:

- navigation clarity improves
- normal reading pages remain quiet
- commit Phase 7

## Phase 8: Final Review

Purpose: verify the track after all selected phases.

Touch only:

- `TODO_JAVA_JVM_IMPLEMENTATION_PLAN.md`
- fixes found by final validation, committed separately if non-trivial

Run:

```bash
node scripts/generate-homepage.js
npm test
```

Run if any Java was added:

```bash
mvn -q compile
```

Manual review cases:

- `/java/` path is clear.
- New routes appear in generated navigation.
- Major links resolve to existing generated routes.
- Each added exercise has a matching solution.
- Each added interview recap is topic-local and has quick revision plus 2-3 strong questions.
- Touched theory pages end with short `## Quick recall`.
- No artificial exercise pages exist.
- No empty folders exist.
- No global UI or schema work slipped in.
- No study-plan checkboxes were ticked for the user.
- If Part rows were edited, `TopicIndex.md` was updated in the same commit.

Exit gate:

- final review notes added to `## Progress Log`
- commit final plan update if needed

## Audit Result

- Existing Java/JVM structure already has focused topic pages for OOP, collections, generics, streams, concurrency, and JVM.
- Existing practice coverage already exists for the main Phase 4 targets:
  - `oop/equals_hashcode/hash_collections_traps/exercise/` and `solution/`
  - `collections/maps/hashmap/exercise/` and `solution/`
  - `concurrency/volatile_keyword/exercise/` and `solution/`
  - `generics/wildcards/exercise/` and `solution/`
  - `coding_fluency/streams/interview_drills/exercise/` and `solution/`
- No broad duplicate exercise folders were needed.
- Useful visuals were added as plain Markdown/text diagrams inside existing pages, not as assets or CSS.
- Site generation/testing was deferred during editing, then run after user approval.
- Existing dirty generated navigation diff points to `system_design/case_studies/parking_lot/`, not this Java/JVM pass.

## Phase 1 Approved Touch List

- `src/main/java/org/example/backend_fundamentals/java/index.md`
- `src/main/java/org/example/backend_fundamentals/java/oop/equals_hashcode/hash_collections_traps/index.md`
- `src/main/java/org/example/backend_fundamentals/java/collections/maps/hashmap/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/wildcards/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/wildcards/exercise/index.md`
- `src/main/java/org/example/backend_fundamentals/java/generics/wildcards/solution/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/volatile_keyword/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/volatile_keyword/solution/index.md`
- `src/main/java/org/example/backend_fundamentals/java/concurrency/jmm/index.md`
- `src/main/java/org/example/backend_fundamentals/java/coding_fluency/streams/index.md`

## Progress Log

- Combined Java/JVM pass completed without commits.
- Kept Java root as a minimal generated-navigation hub after review; removed manual fast routes, priority map, practice table, skip list, and quick recall.
- Removed the extra shortcut revision route; it duplicated study-plan sequencing and was not needed.
- Tightened selected high-value theory pages: hash collection traps, HashMap internals, generic wildcards, volatile, JMM, and streams.
- Consolidated duplicated wildcard exercises and kept matching solution IDs.
- Removed the top-level Java interview drill hub; interview material should live under each owning topic/module.
- Added topic-local interview recap bundles for generics, JVM, concurrency, collections, OOP, and streams.
- Each recap bundle has a hub, quick revision page, and 2-3 question page; existing full exercises were not moved or deleted.
- Ran `node scripts/generate-homepage.js`; strict schema validation passed.
- Ran `npm test`; unit tests and docs build passed.
- Verified `/java/` on port `5173`: no manual route tables, no practice table, no shortcut revision page, no top-level interview drill hub, and only generated high-level topic cards.
- Verified `/java/interview_drills/` renders 404 after removal.
- Final review passed: topic-local recap links resolve, each recap has hub/quick-revision/questions pages, question pages have 2-3 questions, valid Java routes have no console/page errors, and no empty Java folders remain.
