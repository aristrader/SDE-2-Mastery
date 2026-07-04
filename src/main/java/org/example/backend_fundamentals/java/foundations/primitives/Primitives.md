# Primitive Types

## User understanding

Primitive types:

- int
- float
- etc.

Objects:

- Integer
- Double

Primitive types are passed by value.

Objects are passed by reference.

---

## Corrections

### Biggest Misconception

Objects are passed by reference.

### Correction

Java is ALWAYS pass-by-value.

Difference:

Primitive:

Value copied.

Object:

Reference value copied.

The object itself is never copied.

---

## Example

Primitive:

int x = 10;

foo(x);

foo gets another copy of 10.

---

Object:

Person p = new Person();

foo(p);

foo gets another copy of the reference.

Both references point to the same object.

---

Changing:

p.name = "John";

is visible.

Reassigning:

p = new Person();

is NOT visible.

---

## Primitive Types

Exactly eight:

- byte
- short
- int
- long
- float
- double
- char
- boolean

Need not memorize all sizes except:

int = 32-bit

long = 64-bit

double = 64-bit

---

## Primitive vs Objects

Primitive:

Stores value.

Objects:

Variables store references.

---

## Interview points

Primitive:

- cannot be null
- faster
- no methods

---

