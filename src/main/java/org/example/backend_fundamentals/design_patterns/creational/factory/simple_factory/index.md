---
order: 10
---

# Simple Factory

The textbook "Simple Factory" (also called Static Factory). **Not** a Gang of Four pattern — GoF lists only Factory Method and Abstract Factory. But Simple Factory is the shape most developers reach for first, and it's worth treating as its own artifact because it's so common in practice.

## The shape

```
Employee (interface)
    ├── AndroidDeveloper
    └── BackendDeveloper

Developer (enum)                    — discriminator the caller passes in
DeveloperSimpleFactory              — ONE class with ONE static method and if/else
SimpleFactoryRun                    — client calls the static factory
```

## The code

```java
public class DeveloperSimpleFactory {

  public static Employee getDeveloper(Developer developer) {
    if (developer.equals(Developer.ANDROID_DEVELOPER)) {
      return new AndroidDeveloper();
    } else if (developer.equals(Developer.BACKEND_DEVELOPER)) {
      return new BackendDeveloper();
    } else {
      return null;
    }
  }
}
```

Usage:

```java
Employee e1 = DeveloperSimpleFactory.getDeveloper(Developer.ANDROID_DEVELOPER);
Employee e2 = DeveloperSimpleFactory.getDeveloper(Developer.BACKEND_DEVELOPER);
```

## What makes it "Simple Factory"

- **One class** owns all creation logic.
- **Selection is data-driven** — the caller passes an enum (or string, or `Class`) and an `if`/`else` (or `switch`) picks the concrete type.
- **No polymorphism in the factory itself** — the factory is a plain class with a static method.
- **Adding a new type = edit the factory**.

## Pros

- Trivially simple to read, write, and explain.
- Clients never see `new AndroidDeveloper()` — the factory hides concrete types.
- Fine when the product set is small and stable, and you have no other variation to express.

## Cons

- **OCP violation.** Adding `IosDeveloper` means editing the factory (and often editing the discriminator enum). A file that used to compile and was tested now changes every time a new type appears.
- **No home for shared creation logic.** The factory is a dumb builder. If you want "check budget before hiring," "log the hire," or "send a welcome email," none of that fits in `getDeveloper(...)` — it belongs somewhere else, which means every caller writes it themselves.
- **Static `null` returns are fragile.** The example returns `null` for unknown enum values, forcing every caller into defensive `null` checks. Throwing `IllegalArgumentException` is safer, but the issue is structural — the factory has no way to express "this shouldn't happen."
- **Clients have to know the enum.** Anyone calling the factory has to depend on the `Developer` enum. That knowledge leaks outward through every caller.

## When to use it

- Product set small and stable (1–3 types; unlikely to grow).
- No shared creation logic beyond `return new X()`.
- You want the shortest possible code that hides `new`.

## When to upgrade to Factory Method

Any of:

- New product types will arrive over time (especially from other teams / plugins).
- Creation has steps that should be reused (budget check, logging, validation).
- You want to eliminate the discriminator enum from client code.

See the two Factory Method variants in `factory_method_basic/` and `factory_method/`.

## Files in this package

| File | Role |
| --- | --- |
| `Employee.java` | Product interface |
| `AndroidDeveloper.java`, `BackendDeveloper.java` | Concrete products |
| `Developer.java` | Enum discriminator |
| `DeveloperSimpleFactory.java` | The static factory (uses `@UtilityClass` from Lombok) |
| `SimpleFactoryRun.java` | Runnable demo |

Run:

```bash
mvn -q exec:java -Dexec.mainClass="org.example.design_patterns.creational.simple_factory.SimpleFactoryRun"
```

