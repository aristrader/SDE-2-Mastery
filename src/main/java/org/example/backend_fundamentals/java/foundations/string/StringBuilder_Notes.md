# StringBuilder

## User understanding

User explained:

StringBuilder is used when repeatedly appending strings.

Otherwise:

Repeated concatenation creates many String objects.

StringBuilder builds everything first.

Then creates one final String.

User additionally believed:

The String Pool would otherwise contain all intermediate values.

---

## Review

Main intuition:

Correct.

---

## Example

Repeated concatenation:

```java
String s = "";

for (...) {

    s = s + i;

}
```

Creates many temporary String objects.

Conceptually:

""

↓

"0"

↓

"01"

↓

"012"

↓

...

---

## StringBuilder

Example:

```java
StringBuilder sb = new StringBuilder();

for (...) {

    sb.append(i);

}

String result = sb.toString();
```

Only final immutable String is created.

---

## Misconception

Intermediate Strings go into the String Pool.

### Correction

Generally false.

Intermediate Strings created through concatenation are normal heap objects.

Most are never placed into the String Pool.

Eventually they become eligible for garbage collection.

The performance gain comes from:

- fewer allocations
- fewer temporary objects
- less garbage collection

NOT from reducing String Pool entries.

---

## StringBuilder vs StringBuffer

Covered briefly.

StringBuilder

- not synchronized
- faster

StringBuffer

- synchronized
- thread-safe
- slightly slower

---

## Final Revision

Know:

- String immutable.
- StringBuilder mutable.
- Use StringBuilder for repeated appends.
- `toString()` creates final immutable String.
- StringBuilder preferred unless thread safety is required.

---

