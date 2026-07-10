# Structured Practice Workspace Plan

## Status
Pilot implementation slice. Keep this plan focused; do not expand the older LMS architecture docs with a long checklist.

## Goal
Practice pages should stop behaving like one large prose worksheet plus one whole solution page. A learner should pick one exercise, write or edit code for that exercise, run it when runnable, and reveal only the matching solution.

## Markdown Convention
Use this convention only when a topic has multiple practice questions that should be individually selectable.

`exercise/index.md`:

~~~md
## Exercise: stable-id - Human title

### Goal
One short outcome.

### Task
Concrete instructions.

### Starter code

```java
public class StableIdPractice {
    public static void main(String[] args) {
    }
}
```

### Checks
- What output, behavior, or reasoning proves the answer.
~~~

`solution/index.md`:

~~~md
## Solution: stable-id - Human title

Explain the answer for that one exercise. Include code when it helps.
~~~

Rules:

- IDs are lowercase kebab-case and stable across renames.
- Every structured exercise ID must have exactly one matching structured solution ID.
- Exercise IDs cannot repeat inside a page.
- Solution IDs cannot repeat inside a page.
- Starter code is optional, but coding exercises should include one runnable `public static void main` when possible.
- Legacy unstructured practice pages remain valid until migrated.

## UI Contract
- Structured practice pages show a question list, selected prompt, editable starter code, run console, and a per-question solution panel.
- The solution button reveals only the selected question's solution.
- Unstructured practice pages keep the existing fallback workspace.
- The Read and Code tabs remain topic-level actions. Practice is a separate full-page workspace.

## Validation Contract
- `scripts/generate-homepage.js` parses structured exercises during navigation generation.
- The generated `navigation_map.json` includes structured practice metadata for the exercise route.
- Builds fail on duplicate exercise IDs, duplicate solution IDs, or missing matching solutions.
- The convention is opt-in: pages without `## Exercise: ...` headings are not forced into the new format.

## Pilot Scope
- Convert one Java topic first: `java/generics`.
- Keep the solution content concise but real.
- Verify generator tests, docs build, and desktop Playwright behavior before broad migration.

## Later Migration
- Migrate high-value topics gradually.
- Fill placeholder exercise/solution pages from the stub backlog before converting them.
- If a topic has only one simple prompt, keep it simple unless the structured UI improves learning.
