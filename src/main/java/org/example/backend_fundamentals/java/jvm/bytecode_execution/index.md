---
order: 30
---

# Bytecode Execution

Bytecode is not native CPU machine code. It is an instruction set for the JVM.

Examples:

```text
JVM bytecode: iload, invokevirtual, return, iadd
CPU code:     MOV, ADD, JMP
```

Everything is stored as bits eventually, but the important interview distinction is the instruction set target:

- native executable targets an operating system and CPU
- JVM bytecode targets the JVM

## High-level JVM execution pieces

| Piece | Job |
| --- | --- |
| Class loader | loads `.class` files |
| Bytecode verifier | checks bytecode safety |
| Interpreter | executes bytecode instruction by instruction |
| JIT compiler | compiles hot bytecode paths to native machine code |

At this stage, know the roles. Deep class loading and JIT internals can come later.

## Quick recall

- **Is bytecode CPU-specific?** No.
- **What checks bytecode safety?** Bytecode verifier.
- **What optimizes hot code?** JIT compiler.
