---
order: 50
---

# JVM

The JVM chapter explains what happens after Java source is written: tools, bytecode, class loading, execution, and garbage collection.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `jdk_jre_jvm` | Separating development tools, runtime environment, and execution engine. |
| 2 | `compilation_pipeline` | Source to bytecode to JVM execution flow. |
| 3 | `gc` | Heap memory and garbage collection fundamentals. |

## Quick recall

- **Need to compile Java?** JDK.
- **Need to run bytecode?** JVM plus runtime libraries.
- **Why platform independent?** Same bytecode can run on JVMs for different platforms.
- **What cleans unreachable heap objects?** Garbage collector.
