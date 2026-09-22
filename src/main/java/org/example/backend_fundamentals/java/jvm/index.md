---
order: 50
---

# JVM

This chapter answers the interview question behind many production symptoms: what happens after Java source is written, and which runtime boundary explains a slow startup, class-loading failure, or memory incident?

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `jdk_jre_jvm` | Separating development tools, runtime environment, and execution engine. |
| 2 | `compilation_pipeline` | Source code to `.class` bytecode through `javac`. |
| 3 | `bytecode_execution` | How the JVM loads, verifies, interprets, and JIT-compiles bytecode. |
| 4 | `platform_independence` | Why bytecode plus platform-specific JVMs make Java portable. |
| 5 | `gc` | Heap memory and garbage collection fundamentals. |

## Quick recall

**Q. Does the JVM compile `.java` source?**
A. No. `javac` creates class files; the JVM loads and executes their bytecode.

**Q. Why is Java portable but not magic?**
A. A compatible JVM can execute the same class file, but Java version, native-code, OS, and configuration dependencies can still break deployment.

**Q. Does GC free anything the application no longer wants?**
A. No. It reclaims only objects that are no longer reachable.
