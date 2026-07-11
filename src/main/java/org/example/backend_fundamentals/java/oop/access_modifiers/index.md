---
order: 50
---

# Access Modifiers and Source Structure

Access modifiers define who can see a class member or nested type. Use the narrowest visibility that still lets the design work.

## Visibility levels

| Modifier | Visible from |
| --- | --- |
| `private` | same class only |
| package-private | same package |
| `protected` | same package, plus subclasses through the protected-access rule |
| `public` | everywhere |

Top-level classes can only be `public` or package-private. A top-level class cannot be `private` or `protected`.

## Local, instance, and static variables

| Variable kind | Scope/lifetime | Default value? |
| --- | --- | --- |
| local variable | method/block execution | no |
| instance field | one copy per object | yes |
| static field | one copy per class | yes |

Local variables must be assigned before use. Instance and static fields receive Java defaults: `0`, `false`, `null`, etc.

## Source file rules

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

## Related topics

- `static_and_final` covers `static`, `final`, constants, final references, final methods, and final classes.
- `access_modifiers/deep_dive` covers modifier choices in template methods and inheritance hooks.

## Quick recall

- **Do local variables get defaults?** No.
- **Do instance/static fields get defaults?** Yes.
- **Can a top-level class be private?** No.
- **Best default visibility?** The narrowest one that supports the design.
