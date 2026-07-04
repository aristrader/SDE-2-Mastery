# Source File Structure

## User understanding

- Packages within packages.
- Imports at the top.
- Class declaration.
- Main method.
- Public/private methods.
- Class should always be public.
- Only one public class per file.
- Other classes should be private.
- File name matches class name.
- Package-private discussed.
- Nested classes explained using Card/CardDetails example.

---

## Corrections

### Misconception

Every top-level class must be public.

### Correction

Top-level classes may be:

- public
- package-private

They cannot be:

- private
- protected

---

### Misconception

Private classes are disallowed because they cannot be instantiated.

### Correction

Not true.

Private nested classes are completely valid.

Example:

public class Card {

    private static class CardDetails {

    }

}

Private does NOT prevent instantiation.

---

### Misconception

Other classes in the same file must be private.

### Correction

They are package-private.

Example:

public class A {

}

class B {

}

class C {

}

B and C are package-private.

---

### Misconception

Package-private applies only to methods.

### Correction

Package-private applies to:

- classes
- methods
- constructors
- variables

---

## Main Method

Signature:

public static void main(String[] args)

Spring Boot still has a main method:

SpringApplication.run(...)

---

## Nested Classes

Good intuition:

Use nested classes when they only make sense inside the enclosing class.

Example:

Card

↓

CardDetails

---

## Source File Ordering

Order:

package

↓

imports

↓

class

Only one package declaration.

Package declaration must be first non-comment statement.

---

## Follow-up Question

User asked:

"Why can't top-level classes be private? Is it because they can't be instantiated?"

### Misconception

Private classes are forbidden because they cannot be instantiated.

### Correction

Private has meaning only relative to an enclosing class.

Top-level classes have no enclosing class.

Therefore:

"Private top-level class"

has no meaningful scope.

That is why Java simply disallows it.

---

## Comparison

Private nested class:

public class Card {

    private class CardDetails {

    }

}

Makes perfect sense because CardDetails belongs to Card.

---

## Final revision

Know:

- One public class per file.
- File name matches public class.
- Other top-level classes are package-private.
- Top-level classes cannot be private/protected.
- Nested classes may be private.

---

