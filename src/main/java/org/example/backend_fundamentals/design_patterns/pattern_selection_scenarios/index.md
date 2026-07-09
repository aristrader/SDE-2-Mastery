---
order: 60
---

# Design Pattern Practice — Production Scenarios

> **Cross-referenced from:** Part 04 (Design Patterns) rows 7 (Singleton), 8 (Factory Method / Abstract Factory), 9 (Builder), 14 (Strategy). Sister doc to `design_patterns/pattern_selection/index.md` (single-domain "one HR, many factories" mini-project) — this doc is broader (25 scenarios across all creational patterns) and shallower per scenario.

A scenario-based exercise sheet for practising **which pattern fits where** in real-world design. Each scenario is a brief production problem followed by *discussion prompts* and a *suggested approach*. Try to answer before reading the suggested approach.

The goal is not pattern recognition for its own sake &mdash; it's to feel the **trade-off** that makes one pattern fit and another awkward.

## How to use this doc

1. Pick a pattern section.
2. Read scenario 1's problem and decision question.
3. Spend 2&ndash;5 minutes thinking through the prompts before reading the suggested approach.
4. The suggested approach is a recommendation, not the only answer &mdash; if you reasoned to a different pattern with a clear justification, that's the actual win.
5. Move on to the next scenario.

---

## Singleton

### Scenario 1 &mdash; Application configuration

A web service reads configuration from `application.yml` at startup and exposes properties (database URL, feature flags, timeouts) to every layer.

**Decision:** how should config be exposed?

**Consider:**

- Configuration is identical across the whole process &mdash; mutability is undesirable.
- Tests want to swap config in (e.g., point at an in-memory database).
- Many classes need this; passing it through every constructor is tedious.

**Suggested approach:** singleton-scoped bean injected via constructor (Spring or manual DI). The "singleton-ness" is correct; the *Singleton pattern as a `getInstance()` static method* is wrong here because it makes the config impossible to swap in tests. **Singleton lifecycle, DI mechanism.**

### Scenario 2 &mdash; Per-request logger

Every request handler in a microservice writes log lines tagged with the request ID.

**Decision:** Singleton logger, or something else?

**Consider:**

- Log destination (console, file, log aggregation service) is process-global.
- The request ID tag is per-request, not global.
- Loggers compose: each class wants its own logger named after the class.

**Suggested approach:** **not a singleton you write yourself.** SLF4J's `LoggerFactory.getLogger(MyClass.class)` is a static factory method that returns a class-specific logger; the underlying logging framework manages process-global state internally. Per-request context is added via `MDC` (a thread-local), not by holding state on the logger.

### Scenario 3 &mdash; Database connection pool

Your app needs to reuse a fixed pool of N database connections across the whole process.

**Decision:** Singleton, or some other shape?

**Consider:**

- Only one pool should exist (otherwise N&times;number-of-pools connections).
- The pool has lifecycle: it must initialise on startup and close on shutdown.
- Tests want to substitute a different pool (in-memory DB, mock).

**Suggested approach:** **DI-managed singleton bean.** Spring's `DataSource` bean is the canonical example. The "one instance" guarantee is real, but expressing it as `Pool.getInstance()` makes lifecycle management awkward (when does it close?). A DI container handles construction, injection, and shutdown via lifecycle hooks.

### Scenario 4 &mdash; Per-user session

A web app keeps user-session data (cart, preferences, recently viewed items) accessible from request handlers.

**Decision:** Singleton, or something else?

**Consider:**

- This is *per-user*, not global.
- Multiple sessions exist concurrently.
- The framework already provides session abstractions.

**Suggested approach:** **not Singleton.** This is a common Singleton trap &mdash; "globally accessible" is mistaken for "globally single." Use the framework's session abstraction (Spring `HttpSession`, scoped beans with `@SessionScope`, etc.) which gives you per-user state with a singleton-like access pattern.

### Scenario 5 &mdash; Unique ID generator

The app needs a process-wide monotonically increasing ID for log correlation.

**Decision:** Singleton fits or not?

**Consider:**

- A single counter shared by all callers, otherwise IDs collide.
- High concurrency &mdash; thread safety matters.
- Persistence across process restarts may or may not be required.

**Suggested approach:** **singleton lifecycle is correct here**, with `AtomicLong` for thread safety. Whether you express it as a hand-written Singleton (`getInstance()`) or a DI-managed bean depends on the surrounding code &mdash; the latter is preferred in DI codebases. Across restarts, the "process-wide" requirement is no longer enough; introduce persistent storage (DB sequence, Snowflake-style ID generator).

---

## Simple Factory

### Scenario 1 &mdash; Notification dispatcher

The product team wants to send notifications via email, SMS, or push, chosen by a string in user preferences (`"email"`, `"sms"`, `"push"`).

**Decision:** Simple Factory, Strategy, or Registry?

**Consider:**

- The choice is *runtime-determined* from user data.
- Adding a new channel (e.g., WhatsApp) is a likely future change.
- A switch-on-string scattered everywhere is the smell to avoid.

**Suggested approach:** **Simple Factory** centralises the switch-on-string in one place. **Better in a DI codebase**: a `Map<String, NotificationSender>` registry populated by Spring; `Map.get(channel)` replaces the switch and adding a new sender requires zero changes to existing code (OCP). Simple Factory is the right *step zero*; the registry is the step one in any non-trivial system.

### Scenario 2 &mdash; Document parser by file extension

A document-processing service receives uploaded files and parses them based on extension (`.pdf`, `.docx`, `.csv`).

**Decision:** Simple Factory or Factory Method?

**Consider:**

- The decision is purely "which parser, given an extension".
- Each parser does completely different work internally.
- The extension is data, not a class hierarchy.

**Suggested approach:** **Simple Factory or registry**. The selection is keyed by data (extension string), so a `Map<String, Parser>` in a `ParserRegistry` works well. Factory Method would be wrong here &mdash; no inheritance hierarchy is involved on the *caller* side.

### Scenario 3 &mdash; Discount calculator by customer tier

E-commerce checkout applies a discount strategy based on customer tier (free, gold, platinum, vip).

**Decision:** Simple Factory or Strategy?

**Consider:**

- Each tier has *different logic* (percentage, flat amount, none).
- Tier may change as the customer transacts (within a single checkout flow even).
- Adding a new tier should require minimal change.

**Suggested approach:** **Strategy + Registry.** This is precisely the case where "factory" and "strategy" blur &mdash; both involve picking an implementation by a key. The Strategy framing is more accurate because the *behaviour* varies (calculation), not the *type* (the result is always a discount). A registry lookup retrieves the right strategy.

### Scenario 4 &mdash; HTTP client picker by environment

Tests need a mock HTTP client; staging needs a recording client; production needs a real client.

**Decision:** Simple Factory or DI?

**Consider:**

- The choice is *deployment-time*, not runtime.
- Tests must control which client is used.
- Production code shouldn't know there's a mock alternative.

**Suggested approach:** **Dependency Injection** &mdash; configure the right bean per environment via Spring profiles or constructor wiring in a manual setup. A Simple Factory with a `if (env == "test")` branch in production code is exactly the smell DI removes.

### Scenario 5 &mdash; Payment provider by country

A payment service routes through Razorpay for India, Stripe for the US, Adyen for Europe.

**Decision:** Simple Factory, Registry, or Strategy?

**Consider:**

- The route is determined by a transaction's country code.
- Each provider has wildly different API shapes &mdash; the abstraction over them needs design.
- New providers are added regularly as the company expands.

**Suggested approach:** **Registry** keyed by country code, with each `PaymentProvider` implementation as a Spring bean. Behind the registry, providers conform to a common `PaymentProvider` interface (Strategy shape). The combination is "Strategy + Registry" &mdash; Simple Factory would work for the initial version but creates an editing-the-factory problem every time a provider is added.

---

## Factory Method (GoF)

### Scenario 1 &mdash; Hiring pipeline per role

You're building an HR system where each role (Android, Backend, iOS, ML) has the same five-step onboarding template, but each step's specifics differ per role (which laptop, which email group, which Slack channel).

**Decision:** Factory Method or Simple Factory?

**Consider:**

- The *algorithm* (the five steps) is shared across roles.
- The *one varying thing* per step is different per role.
- New roles arrive periodically.

**Suggested approach:** **Factory Method.** The shared algorithm lives in an abstract base class as a `final` template method; each subclass overrides hooks for "create developer" and any role-specific bits. This is exactly the shape in `creational/factory/factory_method/`. Simple Factory wouldn't work because the *algorithm* is the part being shared; Simple Factory only handles object construction.

### Scenario 2 &mdash; UI form rendering per device

A mobile app and a web app share the same form-rendering algorithm: validate, layout, render fields, submit. The rendering of each field type differs per device (mobile uses native widgets, web uses HTML).

**Decision:** Factory Method or Abstract Factory?

**Consider:**

- One algorithm, many widget types per device.
- "Mobile" and "web" are platforms; widgets within them must match.

**Suggested approach:** **Abstract Factory** &mdash; the family is "all the widgets for this platform" (button, input, dropdown, etc.). Factory Method would only work if each form had *one* product to vary, but a form has many widgets that all need to match the platform.

### Scenario 3 &mdash; Test fixture builders per test type

Integration tests need a database fixture; unit tests need an in-memory fixture; performance tests need a load-pre-populated fixture. Each test base class's `setUp()` calls `createFixture()` to get its fixture.

**Decision:** Factory Method?

**Consider:**

- `setUp()` is a fixed sequence of steps every test type follows.
- Each test type provides its own fixture creation.
- No registry / runtime selection needed &mdash; the test type is the test class.

**Suggested approach:** **Factory Method.** Classic textbook fit: an abstract `IntegrationTestBase` with `setUp()` template method, `createFixture()` as the abstract hook. Each concrete test class overrides the hook.

### Scenario 4 &mdash; Game enemy spawning per level

A platform game spawns different enemies per level. Each level shares the spawning algorithm (find spawn point, create enemy, place, register with AI system) but spawns different enemy types.

**Decision:** Factory Method or Strategy?

**Consider:**

- Levels are coded as concrete classes (Level1, Level2, ...).
- The algorithm is identical; only "what enemy" differs.

**Suggested approach:** **Factory Method.** Each level extends a `LevelBase` whose `spawnEnemies()` template method calls an abstract `createEnemy()` hook. Strategy would also work but typically Strategy is used when the *behaviour to vary* is plug-and-play at the call site; here the level *is* the variation.

### Scenario 5 &mdash; Connection retry policy per service

A microservice client library wants the same retry algorithm (try, on failure compute backoff, sleep, retry, give up) but each consuming service might have its own backoff curve.

**Decision:** Factory Method or Strategy?

**Consider:**

- The algorithm is shared.
- The *one varying thing* (backoff curve) is small and self-contained.
- Consumers want to plug in a curve at call site, not subclass.

**Suggested approach:** **Strategy.** When the variance is one method's worth of logic and consumers want runtime substitution rather than subclassing, Strategy beats Factory Method. Factory Method is the right shape when you have a *type-shaped* variation (a whole subclass exists).

---

## Builder

### Scenario 1 &mdash; HTTP request with many optional headers

You're writing an HTTP client wrapper. A request has a URL (required), a method (required), and 0&ndash;N optional headers, query params, body, timeouts, retry policy, etc.

**Decision:** Builder, telescoping constructors, or JavaBeans setters?

**Consider:**

- Many optional fields.
- Want immutability for thread safety (multiple threads share the same `HttpRequest`).
- Validation needed: timeout > 0, URL non-blank, etc.

**Suggested approach:** **Builder, EJ Item 2 style.** This is the canonical fit. Java's own `HttpRequest.newBuilder()` is exactly this. Required fields in the Builder constructor; optional fields as fluent setters; validation in `build()`.

### Scenario 2 &mdash; Insurance policy with cross-field validation

An insurance product has dozens of fields with rules like "if `coverageType == FULL`, `deductible` is required" and "totalPremium = basePremium + addOns.sum()".

**Decision:** Builder, or constructor with all fields?

**Consider:**

- Cross-field invariants must hold *only after* all fields are set.
- Many fields, several optional.
- Validation should happen at one point in time.

**Suggested approach:** **Builder.** The cross-field invariant rules can only be checked when all fields have their final values &mdash; that's exactly what `build()` enables. A constructor with 30 parameters is unreadable, and JavaBeans setters leave the policy in a half-validated state until you remember to call `validate()` (which nobody does).

### Scenario 3 &mdash; Test data factories

Your tests need to construct domain objects (`User`, `Order`, `Invoice`) with sensible defaults but the ability to override one or two fields per test ("user with admin role", "order with status = REFUNDED").

**Decision:** Builder, copy constructors, or test data builders?

**Consider:**

- 95% of fields are the same default in most tests.
- Each test wants to override one or two specific fields.
- Tests should remain readable: `givenAnAdminUser()` is better than constructor with 12 args.

**Suggested approach:** **Test data builder pattern** &mdash; same shape as production Builder but with sensible defaults pre-filled, exposing setters only for the fields tests typically want to vary. This is one of Builder's most underrated production uses.

### Scenario 4 &mdash; Email message with optional attachments, cc, bcc

An email-sending service needs to construct messages with `to` (required), `subject` (required), `body` (required), and `cc`, `bcc`, `attachments` (each optional, possibly multiple).

**Decision:** Builder or constructor overloads?

**Consider:**

- Three required fields, three optional fields, two of which take collections.
- Adding a new optional field (`replyTo`?) shouldn't break call sites.

**Suggested approach:** **Builder** &mdash; the required fields go in the Builder constructor; the collection-valued optional fields get fluent `addCc(String)` / `addBcc(String)` / `addAttachment(File)` methods that internally accumulate. Adding a new optional field is a one-line change with no impact on existing call sites.

### Scenario 5 &mdash; SQL query construction

A reporting tool needs to generate SQL queries with optional WHERE clauses, joins, group-by, having, order-by, limit.

**Decision:** Builder, fluent string concatenation, or a query DSL?

**Consider:**

- Composition order doesn't always matter; some clauses are independent (where vs join).
- The output is text, not a structured object.
- Complexity grows fast: subqueries, parameter binding.

**Suggested approach:** **Builder** for simple needs (SELECT/FROM/WHERE/ORDER BY) &mdash; fluent setters that accumulate into a `StringBuilder`. For non-trivial reporting, use a real DSL (jOOQ, QueryDSL); rolling your own SQL builder is a long-tail tar pit.

---

## Static Factory Methods (EJ Item 1)

### Scenario 1 &mdash; Money value class

You're modelling `Money` with amount and currency. You want `Money.usd(100)`, `Money.eur(200)`, `Money.fromString("$10.50")`, `Money.zero(currency)`.

**Decision:** Constructor only, or static factory methods?

**Consider:**

- Construction strategies need *names* (USD vs EUR can't be distinguished by an `int` overload).
- A `zero` of any currency is a frequent, identical value &mdash; cache it.
- Parsing a string is a different operation than passing amount + currency.

**Suggested approach:** **Static factory methods.** This is the textbook fit for benefit #1 (names) and benefit #2 (caching of common values). The constructor is `private`; named factories `usd`, `eur`, `fromString`, `zero` cover the construction strategies.

### Scenario 2 &mdash; Returning a private subtype

A `Cache` library wants `Cache.bounded(maxSize)`, `Cache.unbounded()`, `Cache.weakKey()`. Each returns a different internal class but callers should only see `Cache`.

**Decision:** Static factory methods, or expose the internal classes?

**Consider:**

- Hiding the internal class is the whole point.
- Callers should code against `Cache`, not `BoundedCache` etc.

**Suggested approach:** **Static factory methods, returning the abstract type.** Benefit #3 (returning subtype) is the headline. The internal classes can be `package-private` or `private` nested classes, never exposed.

### Scenario 3 &mdash; Switching algorithm based on input size

You're writing a sort utility where small inputs go through insertion sort and large inputs through merge sort. The caller shouldn't know.

**Decision:** Static factory method or two separate utilities?

**Consider:**

- The caller knows nothing about input size strategy &mdash; they just want it sorted.
- Picking the right algorithm should be the utility's job.

**Suggested approach:** **Static factory method that returns a type-erased abstract.** `Sorter.optimalFor(input)` returns a `Sorter` instance whose concrete class is chosen by input size. Benefit #4 (returned class varies by input).

### Scenario 4 &mdash; Plugin discovery

A logging library needs to find logging providers (Logback, Log4j, JUL) at runtime based on what's on the classpath.

**Decision:** Static factory method or a registration API?

**Consider:**

- The caller doesn't know which logging provider exists.
- The provider may not even exist when the library is compiled.

**Suggested approach:** **Static factory method backed by `ServiceLoader`** &mdash; benefit #5 (returned class need not exist at compile time). `LoggerFactory.getLogger(name)` looks up an SPI provider at runtime; SLF4J does this exactly.

### Scenario 5 &mdash; Disambiguating constructor overloads

A `Duration` class can be constructed from milliseconds, seconds, minutes, or a parsed string. Each takes a long.

**Decision:** Static factory or constructor overloads?

**Consider:**

- Multiple `Duration(long)` overloads collide on signature.
- The caller's intent ("ten seconds") is lost in `new Duration(10)`.

**Suggested approach:** **Static factory methods** following the convention: `Duration.ofMillis(10)`, `Duration.ofSeconds(10)`, `Duration.ofMinutes(10)`, `Duration.parse("PT10S")`. Java's actual `java.time.Duration` is exactly this. Benefit #1 (names disambiguate).

---

## Abstract Factory

### Scenario 1 &mdash; Multi-platform UI toolkit

A cross-platform IDE renders the same UI on macOS, Windows, and Linux. Each platform's button, dropdown, and tab look different, but a button and a tab on the same platform must visually match.

**Decision:** Factory Method, Abstract Factory, or Strategy?

**Consider:**

- Multiple product types per platform (button, dropdown, tab, ...).
- Platform-internal consistency is a hard requirement &mdash; mismatched widgets break the UI.

**Suggested approach:** **Abstract Factory.** `UIFactory` declares `createButton`, `createDropdown`, `createTab`; `MacUIFactory`, `WindowsUIFactory`, `LinuxUIFactory` implement them. The type system prevents mixing widgets across platforms.

### Scenario 2 &mdash; Multi-tenant SaaS

A B2B SaaS app needs different *theme* (CSS, logo, colour palette), *data store* (each customer's DB shard), and *feature flags* per tenant.

**Decision:** Abstract Factory or DI with tenant-scoped beans?

**Consider:**

- Each tenant has many "products" (theme, data store, feature flags) that must match.
- The tenant axis is a runtime decision per request.

**Suggested approach:** **DI with tenant-scoped beans** is the production answer. The Abstract Factory shape (`TenantFactory.createTheme()`, `TenantFactory.createDataStore()`, etc.) is conceptually right and worth knowing, but in a Spring app the DI container handles per-tenant scoping more cleanly than hand-rolled factories.

### Scenario 3 &mdash; JDBC drivers

You're writing a database client that supports MySQL, Postgres, and Oracle. Each provides a different `Connection`, `Statement`, and `ResultSet` implementation, but they belong together (a Postgres `Statement` doesn't work on a MySQL `Connection`).

**Decision:** Abstract Factory or Factory Method?

**Consider:**

- Multiple coordinated products per vendor.
- Within one vendor, all artefacts must come from the same source.

**Suggested approach:** **Abstract Factory.** This is JDBC's actual design: `Driver` is the abstract factory, `Connection` is created by it, `Statement`/`ResultSet` are produced from `Connection` (sub-factory step). The "family must match" requirement is exactly the pattern's headline.

### Scenario 4 &mdash; Game asset bundle per regional theme

A mobile game has Halloween, Christmas, and Valentine's themes. Each theme replaces the title screen graphics, sound effects, sprites, and music.

**Decision:** Abstract Factory or asset registry?

**Consider:**

- Multiple coordinated assets per theme.
- Theme is a runtime config &mdash; chosen on app launch.
- Adding a new theme should be additive.

**Suggested approach:** **Abstract Factory.** `ThemeFactory` interface; `HalloweenThemeFactory`, `ChristmasThemeFactory`, etc., each producing matching `TitleGraphics`, `SoundPack`, `SpriteSet`, `MusicTrack`. The runtime "load theme" step picks the factory once; everything downstream gets matching assets.

### Scenario 5 &mdash; Test environment fixtures (database + cache + queue + storage)

Integration tests need a coordinated set of test infrastructure: in-memory DB + in-memory cache + in-memory queue + temp file storage. Production uses real DB + Redis + Kafka + S3. The test-vs-prod choice is environment-driven.

**Decision:** Abstract Factory or DI profile?

**Consider:**

- Multiple coordinated infrastructure clients.
- The choice is environment-time, not runtime per-call.
- All four artefacts must "match" &mdash; if you mock one and not the others, integration tests get weird.

**Suggested approach:** **DI profile-based wiring** in production code; **Abstract Factory** is a defensible alternative if you don't have a DI container. Spring profiles (`@Profile("test")` vs `@Profile("prod")`) effectively *are* an abstract factory at the DI-config layer &mdash; you pick the profile, you get a coordinated set of beans.

---

## Cross-pattern decision questions

Synthesis exercises &mdash; given a scenario, decide which of multiple patterns fits and why.

### Q1 &mdash; "I have many optional fields and need an immutable result"

Possible candidates: Builder, Static Factory Methods, telescoping constructors.

**Resolution:**
- *4+ optional fields, immutability, validation* &rarr; **Builder.**
- *2&ndash;3 optional fields, simple construction* &rarr; **Static factory methods** with named overloads (`of`, `from`).
- *Always-required fields only* &rarr; plain constructor.

### Q2 &mdash; "I want one logical instance shared everywhere"

Possible candidates: Singleton, DI singleton scope, Static factory methods (cached).

**Resolution:**
- *In a DI codebase* &rarr; **DI singleton-scoped bean.** Don't write `getInstance()`.
- *In plain Java with no framework* &rarr; **Bill Pugh holder Singleton** (thread-safe, lazy, simple).
- *Cached values of an immutable type* &rarr; **Static factory method with caching** (`Boolean.valueOf`, `Integer.valueOf`).

### Q3 &mdash; "I need to construct one of several similar things based on input"

Possible candidates: Simple Factory, Factory Method, Abstract Factory, Strategy + Registry.

**Resolution:**
- *Selection by string/enum, one product type* &rarr; **Simple Factory or Registry.**
- *Algorithm shared across subclasses, one varying product per subclass* &rarr; **Factory Method.**
- *Family of coordinated products that must match* &rarr; **Abstract Factory.**
- *Behaviour swap, not type swap* &rarr; **Strategy + Registry.**

### Q4 &mdash; "I need to call alternative construction strategies on the same class"

Possible candidates: Static factory methods, Builder.

**Resolution:**
- *Multiple ways to construct the same object, each with a meaningful name* &rarr; **Static factory methods** (`Money.usd`, `Money.eur`, `Money.fromString`).
- *One construction with many optional knobs* &rarr; **Builder.**
- These can coexist in the same class &mdash; static factories cover the common cases, the Builder covers the long tail.

### Q5 &mdash; "I have a class with internal state that varies but the algorithm is shared"

Possible candidates: Factory Method, Template Method, Strategy.

**Resolution:**
- *The algorithm is shared as a `final` method that calls hooks* &rarr; **Template Method** (Factory Method is its construction-flavoured cousin).
- *The algorithm needs runtime substitution by the caller* &rarr; **Strategy.**

---

## Notes on using these scenarios

- **Don't memorise.** The point is to internalise the *trade-offs* &mdash; immutability, OCP, family-coordination, runtime selection &mdash; not which scenario maps to which pattern.
- **Question the suggested approach.** Several scenarios admit multiple defensible answers. If you reasoned to a different pattern with a justification grounded in the trade-offs, you've understood the lesson.
- **Look at real codebases for variants.** When you see Spring's `BeanFactory`, JDBC's driver loading, or `Stream.builder()`, ask: which pattern is this, and which trade-off is it optimising for?

## Related

- `design_patterns/creational/CreationalPatternsRoadmap.md` &mdash; the order in which the patterns were learned.
- `design_patterns/creational/builder/Builder.md` &mdash; cross-cutting overview of the four builder variants.
- `design_patterns/creational/factory/Factory.md` &mdash; Simple Factory vs Factory Method.
- `design_patterns/creational/abstract_factory/AbstractFactory.md` &mdash; the four discussion learnings from the Abstract Factory build.
- `design_patterns/pattern_selection/index.md` &mdash; deeper, single-domain exercise on Strategy / Registry / DI for the "one HR, many factories" problem.
- `study_plan/deep_dives/DesignThinkingProcess.md` &mdash; the meta-process behind every scenario in this doc (pain &rarr; responsibilities &rarr; vary vs. stay &rarr; arrows &rarr; skeleton &rarr; verify).
