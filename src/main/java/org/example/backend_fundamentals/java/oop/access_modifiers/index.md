---
order: 30
---

# Variables and Scope

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

---



<ExerciseNav />
