# Design Thinking Process — Deriving Class Structure From Problems

> **Cross-referenced from:** Part 04 (Design Patterns) — the meta-process behind every pattern row; cited by `design_patterns/foundations/coupling_cohesion_smells/CouplingCohesionSmells.md` for "name the pain" (Step 1) and by `design_patterns/foundations/supporting_principles/SupportingPrinciples.md` for the YAGNI / Rule-of-Three test.

A generic, pattern-agnostic process for going from *"I need to change or add something"* to *"here is the class structure I will write"*. This document is a **living doc** — it grows as new design discussions surface new insights.

**Prerequisites (assumed familiar):** SRP, OCP, LSP, ISP, DIP, DRY, KISS, YAGNI, abstract class vs. interface, polymorphism, composition vs. inheritance. Foundation docs at `design_patterns/foundations/{solid,supporting_principles,oop_pillars}/` cover each one.

---

## The mindset shift

> **You don't start by picking a pattern. You start by naming the pain.**

The most common design mistake: *"I'm going to use pattern X here"* before the pain is even named. That produces forced, overengineered code where the pattern is doing the driving instead of the problem.

The right flow is always:

```
Pain → Questions → Responsibilities → Shape → Code → Verify → Iterate
```

Pattern names are **vocabulary for recognized outputs** of this process — they're not starting points. You do the process honestly, and at the end someone says *"oh, that's Strategy"* or *"that's Decorator"* — and now the name is useful *because* you have the mental model to go with it.

And if no pattern name fits? Also fine. Not every problem maps to a book. The process is the skill; patterns are the labels.

---

## Three guiding ideas (one-line refreshers)

Everything below is a specific application of one of these three:

1. **Encapsulate what varies.** Separate the parts that change per case from the parts that stay the same.
2. **Program to an interface, not an implementation.** Every dependency arrow should point at an abstraction.
3. **Favor composition over inheritance.** Inheritance is a commitment; composition is a configuration. Prefer the looser one unless there's a real *is-a* with reusable behavior.

If you remember nothing else from this file, remember those three.

---

## The 6-step process

### Step 1 — Name the pain (one sentence each, plain English)

Before any typing, say out loud what's wrong. Write it down. **No pattern-speak allowed at this step.**

Examples of well-named pain:
- *"Every new payment method forces me to edit the same big switch statement."*
- *"My controller knows about three database fields it should never see."*
- *"I can't add a rule without breaking the existing ones."*
- *"The same 20 lines appear in five classes."*

Examples of badly named pain (pattern-first thinking):
- *"I think this needs a factory here."* ← which pain are you solving?
- *"Let me add an interface so it's extensible."* ← extensible for what concrete case?

**If you can't name a pain, STOP.** You don't have a problem yet. Don't design for imaginary future needs (YAGNI). Wait until the pain is real.

### Step 2 — List the responsibilities (verbs, not nouns)

Forget classes for a moment. List the **jobs that have to get done**, one verb per line. Examples:

- "Validate the user's input."
- "Compute the tax for an order."
- "Send the confirmation email."
- "Decide which tax rule to apply."

Each distinct job is a **candidate for its own class or method**. If one class is doing two wildly different jobs, that's a **Single Responsibility** smell — the most common design smell there is.

Rule of thumb: if you use the word *"and"* to describe a class's job (*"it validates input **and** computes tax"*), split it.

### Step 3 — Separate what varies from what stays the same

Draw two columns. Put each responsibility in the column it belongs to.

| Varies across cases | Stays the same across cases |
| --- | --- |
| Which algorithm is used | The surrounding workflow |
| Which data source is read | The parsing / validation |
| Which concrete object is built | The steps taken once it exists |

**Rule:** what varies → push to a subclass, a strategy, a parameter, or a map. What stays the same → hoist to a parent class, a helper, or a template method.

This table alone often dictates most of the class structure.

### Step 4 — Sketch dependencies as arrows, not code

On paper, a whiteboard, or a scratch file: draw boxes for the classes you're imagining, and arrows from each class to whatever it depends on. **No implementation, just shape.**

Then verify, in order:

- **Every arrow points at an abstraction**, not a concrete. A concrete-to-concrete arrow is a coupling smell. Flip it by introducing an interface (Dependency Inversion Principle).
- **No cycles.** A → B → A means one of those should depend on an abstraction the other implements.
- **The client has few outgoing arrows.** A class that depends on six other concrete classes knows too much.
- **Arrows point "downhill"** — high-level policy depends on stable abstractions, not on low-level details.

If the picture feels messy, the code will feel messy. Fix the picture before typing code.

### Step 5 — Write the abstractions first, then one concrete

Do **not** start by typing the first concrete class. Type the abstractions, in this order, with empty bodies:

1. **Interface** (the *what*).
2. **Abstract class / skeleton** (the *how, shared*).
3. **One concrete implementation** (the *how, specific*).
4. **One client that uses it.**

Now *stop* and **read the client code**, pretending you've never seen any of this before. Ask:

- Does the client say what it *means*, not how it's *built*?
- Is anything awkward or ceremonial?
- Can a new reader figure out what's happening without reading implementations?

If any answer is "no," change the shape now. Refactoring is free at this stage. It gets expensive after you've written five concrete classes against the shape.

### Step 6 — Verify with the "new case" test and iterate

The ultimate tests for whether a design is earning its complexity:

**"New case" test (OCP):** Imagine adding a new case (new type, new algorithm, new rule). If the answer involves editing *any* existing file, you've missed OCP and probably need more abstraction. If the answer is *"add a new class, nothing existing changes,"* the design is doing its job.

**"Duplication" test (DRY):** Is the same logic appearing in multiple sibling concretes? Hoist it to the parent or extract a helper.

**"Pull-down" test:** Is the parent class holding things only one subclass uses? Push that down into the subclass.

**"Call-site" test:** Is the client code ugly? Always the abstraction's fault, never the client's. The client is your *customer*.

**Iterate.** Your first shape is almost never your final shape. That's fine — the point of Steps 4 and 5 is that early iteration is cheap.

---

## Worked example (generic): "send a notification"

Walking through the 6 steps on a neutral scenario. We deliberately do **not** name a pattern until the end.

### Context

You have a `SignupService` that sends a welcome email after signup. Now product wants the welcome to also go out by SMS for some users, by Slack for some internal test users, and eventually by push notification.

### Step 1 — Name the pain

- *"`SignupService` directly calls an email library. If I add SMS, I have to edit `SignupService`."*
- *"I can't easily unit-test signup without actually sending email."*
- *"Adding push later means another edit in the same place."*

No pattern name yet. Just three plain sentences.

### Step 2 — Responsibilities

- "Run the signup flow (create user, log it, emit analytics)."
- "Decide which channel(s) to notify on for a given user."
- "Send a notification via a specific channel (email, SMS, Slack, push)."
- "Format the message for a specific channel."

Four distinct jobs. `SignupService` is currently doing at least three of them. SRP violation.

### Step 3 — Varies vs. stays

| Varies | Stays the same |
| --- | --- |
| Which channel(s) | The signup flow around it |
| How to format for that channel | That the user gets *some* welcome |
| The specific library used | "A welcome is sent" as a contract |

### Step 4 — Sketch

```
Notifier (interface)                ← abstraction
   ↑ implements
EmailNotifier   SmsNotifier   SlackNotifier

SignupService ──── depends on ────→ Notifier
```

Every arrow points at an abstraction. No concrete dependency from `SignupService` to any specific channel. ✓

### Step 5 — Abstractions first

```java
public interface Notifier {
  void send(User user, Message message);
}

public class SignupService {
  private final Notifier notifier;
  public SignupService(Notifier notifier) { this.notifier = notifier; }
  public void signup(User user) {
    // ... create, log ...
    notifier.send(user, Message.welcome(user));
  }
}

public class EmailNotifier implements Notifier {
  public void send(User user, Message message) { /* ... */ }
}
```

Read `SignupService`: *"a signup service is created with a notifier; signup sends a welcome via that notifier."* Clear. The client **says what it means**. ✓

### Step 6 — Verify

- **"New case" test:** add SMS? Create `SmsNotifier implements Notifier`. `SignupService` untouched. ✓ OCP respected.
- **"Duplication" test:** would all notifiers share "find the user's preferred locale and look up a template"? If yes, extract a helper class or move into an abstract base. Defer until the second notifier is written — YAGNI.
- **"Call-site" test:** `new SignupService(new EmailNotifier())` is readable. In a DI framework, the container injects the notifier and the call site is even cleaner.

### Naming what we built

Now — *only now* — ask: "does this match a known pattern name?"

- Is `Notifier` passed in from outside and chosen per-call? **Strategy.**
- Is it created by a separate factory? **Factory + Strategy.**
- Does an abstract creator own a workflow with a plug-in step? **Factory Method / Template Method.**
- Is Spring injecting implementations from config? **Dependency Injection.**

Depending on which pain dominated and how it evolves, the *same* structure can be called several different things. The process doesn't change; only the label changes.

---

## The reusable checklist (use for every non-trivial design task)

- [ ] What pain am I solving? (one plain-English sentence, no pattern-speak)
- [ ] Who is being forced to know too much? (coupling smell)
- [ ] What varies? What stays the same? (two-column table)
- [ ] What are the distinct responsibilities? (verbs, not nouns)
- [ ] Can I draw the dependencies as arrows? Do any point at concretes?
- [ ] What does the client code at the point of use look like — does it say what it means?
- [ ] Add a new case mentally: do I edit any existing file? (OCP test)
- [ ] Is any logic repeated across siblings? (pull up)
- [ ] Is the parent holding stuff only one child uses? (push down)
- [ ] Does a known pattern name fit what I just built? *(This is the **last** question, not the first.)*

---

## Common traps

1. **Pattern-first thinking.** *"I'll use Factory Method here."* — before the pain is named. Usually produces over-abstracted code no one needs.
2. **Premature abstraction.** Designing for the second case before the first case is stable, or for types that don't exist yet. YAGNI.
3. **God classes.** One class doing three jobs because they *"feel related."* The responsibility-listing step (Step 2) catches this.
4. **Downward arrows to concretes.** `HighLevelService → ConcreteThing` — flip the arrow via an interface (Dependency Inversion Principle).
5. **Copy-paste across siblings.** If two concrete implementations have the same helper method body, the parent or a helper is missing.
6. **Overriding the template method.** When a parent class owns a workflow and wants subclasses to only plug into one step, mark that workflow method `final`. Otherwise subclasses silently replace the whole flow.
7. **Ignoring the call-site test.** If `main` / the controller / the test reads awkwardly, the abstraction is wrong — the client is your *customer*, not a grudging consumer.
8. **Naming classes by pattern.** `FactoryImpl`, `AbstractManagerAdapter`, `SingletonHelper` — meaningless. Prefer role-based names (`DeveloperHiringProcess`, `PaymentGateway`) that a new reader understands without knowing the pattern.
9. **Designing in your head.** Type the skeleton; read the client; adjust. Most "clever" designs collapse the moment you try to use them.
10. **Forgetting that "no pattern" is a valid answer.** Plain classes with good names and clear responsibilities beat forced patterns every time.

---

## Heuristics from design review

These came out of the LLD coaching review before returning to Parking Lot. They are intentionally phrased as questions because that is how they should be used in a code review.

**Who should know about whom?** This is the practical form of DIP. `CheckoutService` may know that it needs to charge a payment, but it should not know how card, UPI, or wallet payments are implemented. It should depend on a `PaymentProcessor` abstraction and, if the choice is runtime-driven, delegate selection to a resolver.

**Who owns this rule?** If a rule uses only an entity's internal state, keep it on the entity. `Order.calculateSubtotal()` belongs on `Order`. If a rule coordinates external systems, keep it outside the entity. `releaseInventory()` and `sendCancellationEmail()` belong to services because they talk to inventory and messaging.

**Is this a utility or a business concept?** Method size is not the deciding factor. A four-line `calculateShipping()` can still be a real `ShippingPolicy` if shipping varies by region, carrier, weight, customer tier, or campaign. A static utility is fine for pure mechanics; business policy usually deserves a named abstraction.

**What varies independently?** If employee discounts, premium-customer discounts, coupons, and campaign discounts change for different reasons, they should not live in one `if/else` pile just because they all return a number. Separate independent rules first; only then decide whether the implementation shape is Strategy, policy objects, a rule engine, or plain methods.

**Where is the runtime decision made?** Replacing `CardPaymentProcessor` with `PaymentProcessor` fixes type coupling, but it does not answer "which processor for this request?" If that decision leaks into `CheckoutService`, the service still knows too much. Put the decision in a resolver or registry.

## Mindset progression

Early design questions often sound syntax-driven:

- "Should this be an enum or a class?"
- "Should this be an interface or an abstract class?"
- "Should I use Strategy here?"

Those are not bad questions, but they are second-order questions. The better first questions are responsibility-driven:

- "Who owns this business rule?"
- "What changes independently?"
- "Which class is being forced to know too much?"
- "What should the caller be able to say without knowing implementation details?"

Once those are answered, the syntax usually follows. Interfaces, abstract classes, enums, strategies, resolvers, and services are tools for expressing the responsibility split, not substitutes for deciding the split.

---

## When to add another layer of abstraction

One of the most common judgement calls: *"Should I extract an interface above this abstract class? Should I add a factory? Should I split this concrete class into a contract + implementation?"*

The instinct to add layers is usually good — but unrooted, it becomes **speculative generality** (another word for premature abstraction). Use this test.

### The YAGNI test — can you name a second use case?

Before adding a layer (interface above a class, abstract above a concrete, etc.), ask:

- **Can I name a concrete second implementation that needs this abstraction today?**
- **If yes, name it and sketch its shape in one sentence.**
- **If no, don't add the layer.** Wait until the second case appears.

If you can't name the second implementation, you're designing against imagined requirements — which rarely match reality.

### When the layer is earned

The layer starts paying off when one of these is concretely true:

1. **A genuinely different variant appears.** Not just a different concrete, but a different *flow* — something that couldn't cleanly inherit from the current abstraction.
2. **Testing / mocking pressure.** Tests routinely need a fake of the type. Interfaces are vastly easier to mock than abstract classes with shared helpers.
3. **Cross-module dependencies.** Code in module A depends on behavior provided by module B, and you want A to depend only on the contract — not on B's implementation details.
4. **Multiple consumers, diverging.** Two or more clients start wanting different views of the same thing (Interface Segregation Principle: fat interfaces should split).

If none of these are true, skip the layer.

### The "wait for the third duplicate" rule

A widely used heuristic for extracting abstractions:

- **One occurrence:** just write it.
- **Two occurrences:** consider it, but don't refactor yet — two is a coincidence.
- **Three occurrences:** *now* the pattern is real — extract the abstraction.

Applied to interfaces: don't extract an interface above one concrete type. Extract it when the second or third related type appears and shows a genuine shared shape.

### The refactor-later escape hatch

Modern IDEs make retroactive abstraction cheap:

- IntelliJ's *Extract Interface* promotes a class's API to an interface in seconds.
- *Pull Members Up* / *Push Members Down* moves code between levels of a hierarchy.
- *Replace Inheritance with Delegation* swaps `extends` for composition.

Because the refactor is cheap, **defer the decision until you have information**. Adding a wrong abstraction early is usually more expensive than extracting the right one later.

### The question to ask

> **"If I discovered a third use case tomorrow, could I refactor to add this layer then?"**

If yes (and the IDE can do it for you), don't add it now. Wait.

### The related principle: abstraction shape follows from state

A companion insight that helps you pick the right *kind* of abstraction:

> **The abstraction shape follows from what state you need to hold.**

- Need to hold references to collaborators, config, or mutable state? → **must be a class** (abstract or concrete). Interfaces cannot hold instance fields.
- Need to enforce construction invariants? → **must be a class** (interfaces have no constructors).
- Need shared behavior but no state? → **interface with `default` methods** is enough.
- Need a pure contract with zero implementation? → **interface with only abstract methods**.
- Need neither state nor contract — just a bag of functions? → **`final class` with static methods** (or `@UtilityClass`).

Match the tool to the state requirements. Don't pick the tool first, then contort the design to fit.

A consequence of this principle: if you're about to call something a "service" and it has only static methods, either (a) rename it to `...Utils` / `...Helper` to be honest about what it is, or (b) promote it to an instance class held as a field somewhere — which forces the holder to be a class, not an interface.

---

## When *no* pattern fits

Sometimes you finish Steps 1–6 and nothing from the GoF catalogue applies. That is **not a failure** — it's often the right answer.

Signs you should *not* be reaching for a pattern name:
- The variation is small enough to handle with a parameter or a `switch` inside one method.
- There's exactly one case today and no concrete need for a second.
- You're about to introduce three classes where one function would do (KISS).
- The abstraction has no clients that would benefit — you're the only user, and you know the concrete type.

**Plain boring code that clearly says what it does is almost always better than a pattern that technically applies.** Patterns earn their complexity through multiple use cases, extensibility, or team comprehension — not just because they're in a book.

---

## Applying this process to new areas

This process works for more than just class design. It scales to:

- **API design** (what does the caller need to say?)
- **Service decomposition** (what are the distinct responsibilities?)
- **Database schema** (what varies per row vs. stays the same?)
- **Architectural decisions** (what will need to change independently?)

The prompts change slightly, but the shape — *pain → responsibilities → vary vs. stay → arrows → skeleton → verify* — stays the same.

---

## How this file is maintained

This is a **living document**. Whenever a design discussion surfaces a new heuristic, trap, or nuance — and the insight is general enough to apply across patterns — add it here. If the insight is pattern-specific (e.g. *"how double-checked locking fails without `volatile`"*), put it in that pattern's own file instead.

Good candidates for updates:
- New heuristics for identifying responsibilities.
- Failure modes seen in real code reviews.
- New "smells" that signal a step was skipped.
- Clarifications to the checklist when it's been ambiguous in practice.

Bad candidates for updates here:
- Mechanics of a specific pattern (belongs in that pattern's `.md`).
- Library- or framework-specific advice.
- One-off project decisions.

---

## Related files

- `design_patterns/foundations/solid/SolidPrinciples.md` — SRP, OCP, LSP, ISP, DIP with one-line tests.
- `design_patterns/foundations/supporting_principles/SupportingPrinciples.md` — DRY, KISS, YAGNI, Rule of Three, SLAP.
- `design_patterns/foundations/oop_pillars/` — Encapsulation, Polymorphism, Abstraction, Inheritance.
- `design_patterns/foundations/coupling_cohesion_smells/CouplingCohesionSmells.md` — smell-vocabulary for Step 1 ("name the pain").
- `design_patterns/pattern_selection/index.md` — Strategy / Registry / DI for "one HR, many factories" — applied case study.
- `design_patterns/pattern_selection/exercise/index.md` — mixed OO design-review exercise from the checkout example.
- `design_patterns/pattern_selection_scenarios/index.md` — 25 scenario-based pattern-selection exercises across the creational patterns.
- `design_patterns/behavioral/strategy_vs_template_method/index.md` — deciding whether the varying behavior should be passed in or owned by a parent workflow.
- `design_patterns/creational/CreationalPatternsRoadmap.md` — pattern-learning order.
