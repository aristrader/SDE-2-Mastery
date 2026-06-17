# Static Factory Methods — Effective Java Item 1

A **static factory method** is a static method on a class that returns an instance of *that same class*. It's an alternative path to construction, parallel to the constructor, and the most common "factory-ish" shape in modern Java.

This is not a GoF pattern; it's an idiom from *Effective Java* Item 1, "Consider static factory methods instead of constructors." This package walks the idea with a small `Temperature` demo and grounds the five benefits with examples from this repo and the JDK.

## The strict definition (and the trap)

Bloch's strict definition: a static method that returns *an instance of the class it's declared on*.

| Method | Declared on | Returns | Static factory method *for*… |
| --- | --- | --- | --- |
| `Boolean.valueOf(true)` | `Boolean` | `Boolean` | `Boolean` ✅ |
| `List.of(1, 2, 3)` | `List` | `List` | `List` ✅ |
| `Temperature.celsius(20)` | `Temperature` | `Temperature` | `Temperature` ✅ |
| `JobOffer.builder()` | `JobOffer` | `JobOfferBuilder` | **Not** `JobOffer` &mdash; it's a static method on `JobOffer` that helps with construction, but the term reserves itself for the same-class case |

Most casual writing on the topic ignores this distinction. Item 1's benefits all assume the strict form.

## The five benefits over constructors

### 1. They have names

Constructor overloads collapse meaning:

```java
// What does this construct? You have to read the docs.
new BigInteger(int bitLength, int certainty, Random rnd);

// Self-documenting:
BigInteger.probablePrime(bitLength, rnd);
```

In this package, `Temperature` is the headline example: three factories that all take a single `double` &mdash; no overloaded constructor could disambiguate them.

```java
Temperature.celsius(20);     // 20°C
Temperature.fahrenheit(68);  // 68°F
Temperature.kelvin(293.15);  // 293.15K
```

### 2. They are not required to create a new object each call

This is the headline benefit. Constructors must always allocate; static factories don't.

The JDK leans on this constantly:

```java
Boolean.valueOf(true)   // returns the singleton Boolean.TRUE
Integer.valueOf(5)      // returns a cached Integer for values in -128..127
```

In `Temperature`, every factory funnels through a `ConcurrentHashMap` cache, so:

```java
Temperature a = Temperature.celsius(20);
Temperature b = Temperature.celsius(20);
a == b;  // true — same instance

Temperature c = Temperature.fahrenheit(68);
a == c;  // true — 68°F is 20°C, and the cache key is Celsius
```

This single benefit is what lets immutable value classes be space-efficient, and what enables the broader notion of **instance-controlled classes** — the class controls how many instances exist. Singleton is a special case (one instance allowed); flyweight is another (one per equivalence class).

### 3. They can return any subtype of their declared return type

```java
public static <T> List<T> unmodifiableList(List<? extends T> list) {
  // Returns an instance of a private inner class.
  // Callers see only List.
}
```

The `Collections.unmodifiableList(...)` factory returns an instance of a private inner class; callers see only `List`. This lets the API hide implementation details and swap them later without breaking callers — *the public API is the return type, not the concrete class*. A constructor can't do this; it always returns an instance of *its own* class.

### 4. The returned class can vary based on input

```java
EnumSet.of(JACK, QUEEN, KING);
// Returns either RegularEnumSet (≤64 elements, bitvector implementation) or
// JumboEnumSet (>64 elements, long-array implementation), depending on the
// enum's size. Caller sees only EnumSet.
```

Same method, different concrete type per call. The factory picks the optimal implementation for the input.

### 5. The returned class need not exist when the factory is written

The "service provider" pattern. The factory looks up an implementation at runtime:

- `DriverManager.getConnection(url)` finds whichever JDBC driver is on the classpath.
- `ServiceLoader.load(MyService.class)` discovers implementations via `META-INF/services/`.

Useful for plugin architectures where the concrete class is loaded dynamically.

## The two limitations

1. **Classes with *only* static factories (no public/protected constructor) cannot be subclassed via `extends`.** Subclasses need a parent constructor to call via `super(...)`. Some consider this a feature (it nudges callers toward composition, EJ Item 18); others see it as a real cost.
2. **They're harder to find in the docs.** Constructors get a dedicated section in JavaDoc; static factories blend in with regular static methods. The standard mitigation is following the naming conventions below so they're recognisable on sight.

## Naming conventions

Following these makes intent obvious to readers:

| Convention | Meaning | Example |
| --- | --- | --- |
| `from` | Type-conversion taking one parameter | `Date.from(instant)`, `Temperature.fromString("20C")` |
| `of` | Aggregation taking multiple parameters | `List.of(a, b, c)`, `EnumSet.of(JACK, QUEEN)` |
| `valueOf` | Verbose alternative to `from` / `of` | `BigInteger.valueOf(Long.MAX_VALUE)` |
| `instance` / `getInstance` | Returns an instance described by parameters; may not be new | `StackWalker.getInstance(opts)`, `Singleton.getInstance()` |
| `create` / `newInstance` | Like `getInstance` but guarantees a *new* object | `Array.newInstance(cls, len)` |
| `getXxx` | Like `getInstance` but on a *different* class | `Files.getFileStore(path)` |
| `newXxx` | Like `newInstance` but on a different class | `Files.newBufferedReader(path)` |
| `xxx` | Concise alternative when meaning is obvious | `Collections.list(enumeration)` |

Once you see these conventions, you can't unsee them in the JDK.

## When to use a static factory method

Reach for one when at least one of these applies:

- You want a **meaningful name** to disambiguate construction strategies.
- You want to **cache instances** for value classes that appear repeatedly.
- You want to **return a subtype** the caller shouldn't depend on.
- You want construction to **defer to runtime** (service-provider pattern).

Stick with a plain constructor when none of these apply. Static factories aren't always better &mdash; they're better when one of the five benefits actively earns its keep.

## How this connects to other patterns in this repo

Three connections worth keeping in your head:

1. **Singleton's `getInstance()` is itself a static factory method.** The Singleton "pattern" is a special case of EJ Item 1's benefit #2: the factory chooses not to create a new instance. Your four singleton variants in `creational/singleton/` are all named per the `getInstance` convention.

2. **Simple Factory's `EmployeeFactory.create(EmployeeType)` returns `Employee`, not `EmployeeFactory`.** So strictly per Bloch, it's *not* a static factory method *for `EmployeeFactory`*. It's a static method on `EmployeeFactory` that's a factory &mdash; different thing. Most Java tutorials are loose with this; the distinction sharpens once Item 1 clicks.

3. **`JobOffer.builder()` is the same trap.** Static method on `JobOffer`, but returns `JobOfferBuilder`, so not a static factory method *for `JobOffer`*. A real static factory method for `JobOffer` would look like `JobOffer.of(salary, city)`.

## The demo class

`Temperature.java` shows the two most common benefits in practice:

- **Names disambiguate:** `celsius`, `fahrenheit`, `kelvin` &mdash; three factories with identical signatures that overloaded constructors could not represent.
- **Instance control:** all factories funnel through a `ConcurrentHashMap` cache; equivalent inputs return the same instance regardless of which factory was called.

The runner (`TemperatureRun.java`) walks five scenarios:

1. Three named factories all encoding 20°C from different units.
2. The `from` convention via `fromString("100C")`.
3. `==` checks proving the cache: `celsius(20) == fahrenheit(68)` is `true`.
4. `absoluteZero()` returns the same singleton on every call.
5. Validation: invalid Kelvin and unknown units throw.

Run it with:

```bash
mvn -q exec:java -Dexec.mainClass="org.example.design_patterns.creational.static_factory_methods.TemperatureRun"
```

## Caveat about the demo's caching

`Temperature` uses an unbounded `ConcurrentHashMap` for the cache. That's fine for a teaching demo but a leak risk in production code &mdash; the cache grows forever. Real-world variants either cap the cache (LRU / weak references) or apply caching only to a known-finite domain (`Boolean.valueOf`'s two values, `Integer.valueOf`'s `-128..127` range). Worth knowing the pattern; worth not copying the unbounded form.

## When constructors are still right

- One construction strategy, no naming issue, no caching, no subtypes &mdash; just write `new`.
- You want callers to subclass via inheritance &mdash; static factories alone don't enable this.

## Related

- `creational/CreationalPatternsRoadmap.md` &mdash; this is "Side Quest A" between Builder and Abstract Factory.
- `creational/singleton/Singleton.md` &mdash; `getInstance()` is the canonical static factory method; the Singleton pattern is a special case of Item 1.
- `creational/factory/Factory.md` &mdash; Simple Factory's `create(...)` is a static method that *is* a factory but is not strictly an Item-1 static factory method; the doc clarifies the terminology.
- `creational/builder/Builder.md` &mdash; the `builder()` entry point Lombok generates is the same trap.
- `todo/FoundationsToRead.md` &raquo; "Static factory methods (Effective Java Item 1)" &mdash; the foundations checklist entry.
