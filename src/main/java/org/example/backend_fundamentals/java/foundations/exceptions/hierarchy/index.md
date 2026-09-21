---
order: 10
---

# Exception Hierarchy

Before choosing a `catch`, classify the throwable. The class hierarchy determines both what ordinary application code should recover from and what the compiler makes visible in a method contract.

```text
Throwable
├── Error
└── Exception
    ├── RuntimeException
    └── checked exceptions
```

`RuntimeException` and its subclasses, plus `Error` and its subclasses, are unchecked. Every other `Throwable` subclass is checked. Do not catch `Error` in normal application code; catch an exception only when you can recover, translate it at a boundary, or add actionable context. This hierarchy and classification are defined by the [JLS](https://docs.oracle.com/javase/specs/jls/se21/html/jls-11.html#jls-11.1.1).

## Checked versus unchecked is a contract decision

Checked does not mean “safe” and unchecked does not automatically mean “bug.” A checked type makes a recoverable condition explicit in every caller's contract. An unchecked type is suitable for violated preconditions and failures that a local caller cannot sensibly resolve. For a normal business outcome, return a result instead of using exceptions as a branch.

## `throw` vs `throws`

- `throw` creates the actual control-flow jump by throwing an exception object.
- `throws` declares that a method may let a checked exception escape.

Unchecked exceptions may be thrown without a `throws` declaration, but a caller may still catch them. For checked exceptions, the compiler requires a catch or a compatible `throws` declaration when the method body can throw one.

## Catch precisely

Catch the most specific type that changes your behavior. Put a subtype catch before its supertype; otherwise the subtype catch is unreachable. Avoid `catch (Exception e)` merely to log and continue: either recover, translate with the cause, or let the failure reach its owner.

## Quick recall

- **Compiler-enforced branch?** Checked exceptions: catch or declare them.
- **Programming bug branch?** Often an unchecked exception; classification is still a contract decision.
- **Keyword that throws now?** `throw`.
- **Keyword on method signature?** `throws`.
