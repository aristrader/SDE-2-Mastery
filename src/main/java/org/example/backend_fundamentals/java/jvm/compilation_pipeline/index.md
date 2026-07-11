---
order: 20
---

# Java Compilation Pipeline

`javac` compiles Java source into JVM bytecode.

```text
.java source
  -> javac
  -> .class bytecode
```

The JVM does not compile `.java` files into bytecode. The JVM executes bytecode.

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

- **What creates bytecode?** `javac`.
- **Where is `javac`?** JDK.
- **Does JVM create `.class` files?** No.
- **What does JVM consume?** Bytecode.
