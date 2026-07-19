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

## Quick recall

- **Why portable?** Same bytecode, platform-specific JVM.
- **Why warmup matters?** JIT optimizes after runtime profiling.
- **Can GC fix leaks?** No, not if references are still reachable.
