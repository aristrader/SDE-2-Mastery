---
title: Quick Revision
order: 10
search: false
---

# JVM Quick Revision

| Topic | One-line recall |
| --- | --- |
| JDK | Development kit: compiler, tools, runtime. |
| JRE | Runtime libraries plus JVM; not the full dev toolkit. |
| JVM | Loads, verifies, executes, optimizes bytecode. |
| Bytecode | Platform-neutral instruction format produced by `javac`. |
| Class loading | Finds class bytes and creates runtime class metadata. |
| Verification | Rejects invalid or unsafe bytecode before execution. |
| Interpreter | Starts executing bytecode quickly. |
| JIT | Compiles hot paths to native code after profiling. |
| GC | Reclaims unreachable heap objects, not reachable leaks. |
| Portability boundary | Class-file/runtime compatibility is portable; native dependencies and configuration are not. |

## Quick recall

**Q. Why portable?**
A. Same bytecode, compatible platform-specific JVM.

**Q. Why does warmup matter?**
A. The JVM can profile and JIT-optimize hot paths after execution starts.

**Q. Can GC fix leaks?**
A. No—not while a static collection, listener, queue, or other GC-root path keeps objects reachable.
