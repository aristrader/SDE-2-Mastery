---
order: 20
search: false
---

# Solutions

## Solution: pass-by-value-primitives - Primitives are Pass-by-Value

```java
public class Main {
    static void changeValue(int x) {
        x = 20; // Only modifies the local copy
    }

    public static void main(String[] args) {
        int x = 10;
        changeValue(x);
        System.out.println(x); // Prints 10
    }
}
```
Java is always pass-by-value. For primitives, the literal value (`10`) is copied into the method parameter. Changing the parameter only changes the local copy.

## Solution: copied-reference-value - Mutation Is Not Reassignment

```java
public class Main {
    static void change(StringBuilder value) {
        value.append("!");
        value = new StringBuilder("replacement");
    }

    public static void main(String[] args) {
        StringBuilder value = new StringBuilder("draft");
        change(value);
        System.out.println(value); // draft!
    }
}
```

The parameter receives a copied reference value. Both references identify the original builder, so `append` is visible. Reassigning the parameter changes only that local reference.

## Solution: primitive-vs-object-defaults - Default Values

```java
public class DefaultTest {
    int primitiveInt;
    Integer objectInt;

    public static void main(String[] args) {
        DefaultTest test = new DefaultTest();
        System.out.println(test.primitiveInt); // Prints 0
        System.out.println(test.objectInt);    // Prints null
    }
}
```
Primitives store values and have non-null defaults (like `0` or `false`). Wrapper variables store reference values and, like all object references, their default value is `null`. Assigning `test.objectInt` to an `int` would unbox `null` and throw `NullPointerException`.

## Solution: numeric-conversion-boundary - Predict the Loss

```java
public class Main {
    public static void main(String[] args) {
        byte retries = 10;
        // retries = retries + 1; // does not compile: byte + int becomes int
        retries += 1;             // compound assignment narrows back to byte

        int exact = 16_777_217;
        float approximate = exact;
        System.out.println((int) approximate); // 16_777_216

        long externalId = 3_000_000_000L;
        System.out.println((int) externalId); // truncated, not validated

        try {
            System.out.println(Math.toIntExact(externalId));
        } catch (ArithmeticException exception) {
            System.out.println("externalId does not fit in int");
        }
    }
}
```

Ordinary arithmetic promotes `byte`, `short`, and `char` operands to `int`. `+=` is convenient but can hide a
narrowing conversion, so it is not a range check. `float` cannot represent every `int` once values exceed its precise
integer range. `Math.toIntExact` is the correct boundary when an out-of-range external value must fail rather than
silently change.
