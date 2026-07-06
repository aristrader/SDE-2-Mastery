---
order: 20
---

# Java Compilation Pipeline (JDK, JRE, JVM)

## User understanding

- JDK = Java Development Kit.
- Comes with compiler and Java libraries.
- JRE = Java Runtime Environment.
- JRE is inside JDK.
- JVM is inside JRE.
- JVM contains whatever is required to run compiled Java code.

---

## Corrections

### Misconception

JVM contains Java libraries.

### Correction

The JVM is only the virtual machine responsible for executing Java bytecode.

The Java standard libraries are part of the JRE, not the JVM.

Conceptual structure:

JDK
├── javac
├── debugger
├── javadoc
└── JRE
    ├── Java Standard Libraries
    └── JVM

---

### Misconception

JRE and JVM "go together nowadays."

### Correction

Since Java 11, Oracle no longer distributes a separate JRE.

Practically, developers usually install the JDK.

Conceptually:

JDK
contains
JRE
contains
JVM

This conceptual model is still what interviewers expect.

---

## Java Compilation Pipeline

Pipeline:

.java
↓
javac (inside JDK)
↓
.class (Bytecode)
↓
JVM
↓
Native Machine Code

Important components inside JVM (high level only):

- Class Loader
- Bytecode Verifier
- Interpreter
- JIT Compiler

Need not know internals at this stage.

---

## Platform Independence

Important interview question:

Why is Java platform independent?

Answer:

- Java compiles source code into bytecode.
- Bytecode is not CPU-specific.
- Every platform has its own JVM implementation.
- JVM translates bytecode into native machine code.

Therefore:

Same .class file

↓

Runs on Windows

Runs on Linux

Runs on macOS

without recompilation.

---

## Follow-up Question

User asked:

"I think JVM converts Java class to bytecode?"

### Misconception

JVM converts Java source/class into bytecode.

### Correction

javac performs:

.java

↓

.class (bytecode)

The JVM never generates bytecode.

The JVM executes bytecode.

---

## Follow-up Question

User asked:

"Bytecode is basically 0 and 1, right?"

### Correction

Everything stored on a computer is ultimately bits.

The important distinction is:

Bytecode is an instruction set designed for the JVM.

Examples:

CPU instructions:

- MOV
- ADD
- JMP

JVM bytecode instructions:

- iload
- invokevirtual
- return
- iadd

---

## Follow-up Question

User asked:

"Are all languages platform independent like Python and C++?"

### C++

Not platform independent.

main.cpp

↓

g++

↓

Windows executable

Linux executable

Need recompilation for different operating systems.

---

### Python

Mostly platform independent, but differently.

Python source

↓

Python interpreter

↓

Machine instructions

CPython internally creates Python bytecode (.pyc) executed by the Python Virtual Machine.

Different from Java bytecode.

---

### Java

Java source

↓

javac

↓

JVM bytecode

↓

JVM

↓

Machine code

---

## Final revision

Know:

- JDK = development tools + runtime.
- JRE = runtime + libraries + JVM (conceptually).
- JVM executes bytecode.
- javac creates bytecode.
- Java is platform independent because of bytecode + JVM.

---



