---
order: 40
---

# Access Modifier Deep Dive

Use modifiers to make ownership, extension, and call boundaries enforceable by the compiler.

## Top-level types

Top-level classes can be only:

- `public`
- package-private

They cannot be `private`, `protected`, or `static`.

## Member visibility and override boundary

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

Across packages, `protected` access requires subclass context. A subclass cannot freely access a protected member through an arbitrary parent reference from another package. This is why protected fields are a poor extension API: they expose representation and make invariants difficult to maintain. Prefer a protected operation with a precise contract.

## Quick recall

- **Top-level private class?** Not allowed.
- **Reduce method visibility while overriding?** Not allowed.
- **Private method polymorphic?** No.
- **Static method polymorphic?** No, hidden by compile-time reference type.
- **Cross-package protected receiver?** `this`, `super`, or a qualifying subclass reference—not an arbitrary parent reference.
