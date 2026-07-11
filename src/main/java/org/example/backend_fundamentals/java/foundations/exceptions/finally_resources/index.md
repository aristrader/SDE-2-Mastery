---
order: 30
---

# finally and Resources

`finally` runs after the `try` block whether the body succeeds, throws, or returns. That makes it useful for cleanup, but dangerous for control flow.

Prefer `try-with-resources` for `AutoCloseable` objects. It closes resources automatically, in reverse creation order, and preserves close failures as suppressed exceptions.

## Quick recall

- **Does `finally` run after return?** Yes.
- **Should `finally` return?** No, it can suppress the real result or exception.
- **Resource close order?** Reverse of creation.
- **Where is close failure stored?** `getSuppressed()`.
