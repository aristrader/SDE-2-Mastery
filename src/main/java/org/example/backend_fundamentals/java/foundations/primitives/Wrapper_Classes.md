# Wrapper Classes

## User understanding

Wrapper classes provide utility methods like:

- parseInt
- Boolean.valueOf

Useful when additional methods are required.

---

## Correction

### Misconception

Main reason wrappers exist is because they provide methods.

### Correction

Primary reason:

Collections and generics work only with objects.

Example:

`List<int>`

Invalid.

`List<Integer>`

Valid.

---

## Wrapper Mapping

int → Integer

long → Long

double → Double

float → Float

boolean → Boolean

char → Character

byte → Byte

short → Short

---

## Autoboxing

Integer x = 10;

Compiler converts to:

Integer.valueOf(10)

---

## Unboxing

Integer x = 10;

int y = x;

Compiler converts to:

x.intValue()

---

## Primitive vs Wrapper

Primitive:

- faster
- no null
- calculations

Wrapper:

- collections
- null allowed
- optional values

Example:

Integer age

null = unknown

0 = newborn

---

## Interview Trap

Integer x = null;

int y = x;

Throws:

NullPointerException

because of auto-unboxing.

---

