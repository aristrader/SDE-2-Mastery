---
order: 10
search: false
---

# CompletableFuture Mixed Review

Use this after the focused submodule exercises.

## Exercise: classify-completablefuture-workflow - Classify CompletableFuture Workflow

### Goal
Pick the right composition method before writing code.

### Task
For each step in a dashboard workflow, classify it as:

- synchronous transform
- dependent async call
- independent async call
- optional dependency
- required dependency
- boundary blocking point

### Checks
- Name where `thenApply()`, `thenCompose()`, `thenCombine()`, and `allOf()` fit.
- Name which failures should propagate.
- Name which failures may degrade.

## Exercise: spot-completablefuture-bugs - Spot CompletableFuture Bugs

### Goal
Catch common interview bugs quickly.

### Task
Review snippets containing:

- immediate `join()` after `supplyAsync()`
- `thenApply()` around an async-returning method
- one broad final `exceptionally()`
- default common pool for blocking calls
- ignored returned future

For each, explain the bug and smallest fix.

### Checks
- Do not over-fix with new abstractions.
- Preserve required failures.
- Avoid blocking inside helper methods.

## Exercise: end-to-end-completablefuture-review - End-To-End CompletableFuture Review

### Goal
Tie the submodules together.

### Task
Design a service that fetches a user, orders, recommendations, and notifications. User and orders are required. Recommendations and notifications are optional. Add per-service timeouts, an overall timeout, warnings, and a custom executor.

### Checks
- Start independent work immediately.
- Attach fallback to optional futures only.
- Use `allOf()` to assemble.
- Block only at the caller boundary.
