---
order: 20
---

# Java Compilation Pipeline

`javac` translates Java source into class-file bytecode. That separation is why a build machine and a production JVM play different roles.

```text
.java source
  -> javac
  -> .class bytecode
```

The JVM does not compile `.java` files into bytecode. It loads and executes class files (or another conforming binary representation).

## Common correction

Wrong mental model:

```text
JVM converts Java source to bytecode
```

Correct model:

```text
javac creates bytecode
JVM loads and executes bytecode
```

## Compile-time boundary

Compilation catches language and type errors that can be proven from the source and its compile-time dependencies. It cannot prove that the production class path, configuration, network, database, or runtime Java version will be valid.

`javac --release 21` is a useful build boundary: it compiles for the specified Java platform API and class-file level. A class file built for a newer Java release cannot run on an older JVM that does not understand its class-file version.

## Where the tools live

`javac` is part of the JDK. The JVM and standard libraries are runtime pieces.

```text
JDK
├── javac
├── debugger
├── javadoc
└── runtime pieces
    ├── standard libraries
    └── JVM
```

Since Java 11, developers usually install a full JDK rather than a separately distributed JRE. The conceptual interview model still stays useful: JDK for development, runtime pieces for running, JVM for executing bytecode.

## Quick recall

**Q. What creates bytecode?**
A. `javac` (or another Java compiler), not the JVM.

**Q. Does a successful compile prove the service will start?**
A. No. Loading, linkage, configuration, dependencies, and runtime-version compatibility still happen later.

**Q. What does `--release` protect?**
A. It sets the intended Java platform target so the build does not accidentally use a newer API or class-file level.
