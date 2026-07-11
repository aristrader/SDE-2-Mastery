---
order: 10
---

# Exception Hierarchy

Java separates serious JVM failures from application failures:

```text
Throwable
├── Error
└── Exception
    ├── RuntimeException
    └── checked exceptions
```

Do not catch `Error` in normal application code. Catch exceptions you can handle, translate, or add context to.

## `throw` vs `throws`

- `throw` creates the actual control-flow jump by throwing an exception object.
- `throws` declares that a method may let a checked exception escape.

Unchecked exceptions may be thrown without a `throws` declaration, but the caller can still catch them.

## Quick recall

- **Compiler-enforced branch?** Checked exceptions.
- **Programming bug branch?** Usually unchecked exceptions.
- **Keyword that throws now?** `throw`.
- **Keyword on method signature?** `throws`.
