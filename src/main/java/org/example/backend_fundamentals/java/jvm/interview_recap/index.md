---
title: Interview Recap
order: 90
---

# JVM Interview Recap

Use this after the JVM pages when you only need interview recall.

## Quick recall

**Q. What is the concise source-to-running path?**
A. `javac` produces class-file bytecode; the JVM loads, links, initializes, executes, and may JIT-optimize hot code.

**Q. How do you distinguish common class-runtime failures?**
A. `ClassNotFoundException` is a checked lookup failure (often `Class.forName`); `NoClassDefFoundError` means a needed definition was unavailable at use or initialization previously failed; `VerifyError` means invalid bytecode failed linkage verification.

**Q. What does a memory leak mean on the JVM?**
A. Objects are still reachable even though the application no longer needs them.
