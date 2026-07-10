---
order: 50
---

# Access Modifiers, Variables, and Source Structure

## User understanding

Local variables:

Inside methods.

Instance variables:

Inside class.

Static variables:

Shared by every object.

Lifetime:

Local → method.

Static → class.

Thought local/static need not have default values.

Final variables cannot change.

Final object reference cannot be reassigned.

Internal mutable state may still change.

---

## Corrections

### Misconception

Static variables do not have default values.

### Correction

Static variables DO receive default values.

---

### Default Values

Local:

No default value.

Must initialize before use.

Instance:

Receive defaults.

Example:

int → 0

boolean → false

String → null

Static:

Also receive defaults.

---

### Misconception

Final variables must always be initialized at declaration.

### Correction

They may be initialized later exactly once.

Example:

final int x;

x = 10;

Valid.

---

## Final references

Correct understanding.

Example:

final Person p = new Person();

Not allowed:

p = new Person();

Allowed:

p.age = 30;

Reference immutable.

Object may still mutate.

---

## static final

Common for constants.

Example:

public static final double PI = ...

---

## Source file structure

Java enforces strict rules on how `.java` files are structured.

Top-level classes:

1. A file can have only one `public` top-level class.
2. The filename must exactly match that public class.
3. Other top-level classes in the same file must be package-private.
4. Top-level classes cannot be `private` or `protected`.

Nested classes are different: a class declared inside another class can be `private`, `protected`, `public`, or package-private.

```java
public class Card {
    private class CardDetails {
        // only Card can see and use this nested class
    }
}

class Helper {
    // package-private top-level class
}
```

---

## Final revision

Know:

Local:

- method scope
- no defaults

Instance:

- per object
- default values

Static:

- one copy
- default values
- lifetime tied to class

Final:

Reference cannot change.

Object may.

## Quick recall

- **Do local variables get defaults?** No.
- **Do instance/static fields get defaults?** Yes.
- **Can a top-level class be private?** No.
- **Does `final` make an object immutable?** No, it prevents reassignment of that variable/reference.
- **Best default visibility?** The narrowest one that supports the design.

---
