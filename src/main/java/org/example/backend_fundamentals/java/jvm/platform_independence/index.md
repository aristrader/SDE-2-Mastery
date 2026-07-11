---
order: 40
---

# Platform Independence

Java is platform independent because source compiles to JVM bytecode, and each platform provides its own JVM implementation.

```text
Java source
  -> javac
  -> JVM bytecode
  -> platform-specific JVM
  -> native machine code
```

The same `.class` file can run on Windows, Linux, and macOS if a compatible JVM exists.

## Compared with C++ and Python

| Language | Portability model |
| --- | --- |
| C++ | compile separately for each target OS/CPU |
| Python | source runs through a Python interpreter; CPython also creates Python bytecode |
| Java | compile once to JVM bytecode; run on platform-specific JVMs |

Java bytecode and Python bytecode are not the same thing. Each targets its own runtime.

## Quick recall

- **Why can Java run cross-platform?** Bytecode plus platform-specific JVM.
- **Does C++ usually need recompilation?** Yes.
- **Is Python portability the same as Java?** No; similar idea, different runtime model.
