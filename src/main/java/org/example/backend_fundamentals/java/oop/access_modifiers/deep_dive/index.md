---
order: 40
---

# Access Modifier Deep Dive

Use modifiers to express ownership and extension rules.

## Top-level types

Top-level classes can be only:

- `public`
- package-private

They cannot be `private`, `protected`, or `static`.

## Member visibility

| Modifier | Visible from |
| --- | --- |
| `private` | same class only |
| package-private | same package |
| `protected` | same package plus subclasses with the protected-access rule |
| `public` | everywhere |

Default to the narrowest visibility that supports the design.

## Overriding rules

You cannot reduce visibility when overriding. `private` methods are not overridden. `static` methods are hidden, not overridden. Constructors cannot be `abstract`.

## Template-method modifier pattern

```java
public final void onboard() {
    checkBudget();
    createDeveloper();
    provisionLaptop();
}

protected abstract Developer createDeveloper();

private void checkBudget() {}
private void provisionLaptop() {}
```

- `public final` on the template method: callers can use it; subclasses cannot reorder it.
- `protected abstract` on the hook: subclasses must provide the variable step.
- `private` on helper steps: subclass cannot accidentally depend on or override internals.

## Protected across packages

Across packages, `protected` access requires subclass context. A subclass cannot freely access a protected member through any parent reference from another package.

## Quick recall

- **Top-level private class?** Not allowed.
- **Reduce method visibility while overriding?** Not allowed.
- **Private method polymorphic?** No.
- **Static method polymorphic?** No, hidden by compile-time reference type.
- **Detailed walkthrough?** See `reference_notes`.
