---
order: 50
---

# Access Modifiers: make the legal collaboration boundary explicit

Access modifiers answer “who may depend on this?” They are not decoration. Start narrow, then widen only when a real caller or extension point requires it; public APIs are expensive to retract.

## Visibility levels

| Modifier | Visible from |
| --- | --- |
| `private` | same class only |
| package-private | same package |
| `protected` | same package, plus subclasses under a cross-package rule |
| `public` | everywhere |

Top-level classes can only be `public` or package-private. A top-level class cannot be `private` or `protected`; a nested class can use all four levels.

## `protected` is not “subclasses can access anything”

Within the declaring package, `protected` behaves like package-private access. From another package, code must be inside a subclass, and it may access the member through `this`, `super`, or a reference whose compile-time type is that subclass (or a subtype)—not through an arbitrary parent reference.

```java
// package ledger;
public class Account {
    protected long balance;
}

// package reporting;
class ReportAccount extends Account {
    long ownBalance() {
        return this.balance; // valid
    }

    long otherBalance(Account other) {
        // return other.balance; // illegal across packages
        return 0;
    }
}
```

The naive model exposes a parent’s internals to every child-held `Account`. Java prevents that leak across package boundaries. Prefer a protected method with a narrow behavioral contract over exposing mutable protected fields.

## Local, instance, and static variables

| Variable kind | Scope/lifetime | Default value? |
| --- | --- | --- |
| local variable | method/block execution | no |
| instance field | one copy per object | yes |
| static field | one copy per class | yes |

Local variables must be assigned before use. Instance and static fields receive Java defaults: `0`, `false`, `null`, etc.

## Source structure and initialization boundary

1. A `.java` file can have only one `public` top-level class.
2. The filename must exactly match that public class.
3. Other top-level classes in the same file must be package-private.
4. Nested classes can use all visibility modifiers.

```java
public class Card {
    private class CardDetails {
        // only Card can use this nested class
    }
}

class Helper {
    // package-private top-level class
}
```

## `final` protects a binding, not an object

`final Customer customer` prevents assigning a different `Customer` to that variable. It does not make the referenced object immutable: mutable fields can still change. Combine `final` references with private final state and no mutators when callers need an immutable value.

## Quick recall

- **Do local variables get defaults?** No.
- **Do instance/static fields get defaults?** Yes.
- **Can a top-level class be private?** No.
- **Best default visibility?** The narrowest one that supports the design.
- **What does cross-package `protected` require?** Subclass code and a qualifying subclass receiver, not any parent reference.
- **Does `final` make an object immutable?** No; it prevents reassignment of that reference.
