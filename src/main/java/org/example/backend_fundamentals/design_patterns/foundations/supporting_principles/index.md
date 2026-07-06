---
order: 40
---

# Supporting Principles

> Lower-profile than SOLID, but they decide whether your code stays maintainable. Pay special attention to the **YAGNI + Rule of Three pair** and to **SLAP**.

---

## DRY — Don't Repeat Yourself

Duplicate knowledge is the enemy. The Pragmatic Programmer (Hunt & Thomas, 1999) phrases it precisely: *"Every piece of knowledge must have a single, unambiguous, authoritative representation within a system."* The unit is **knowledge** — a business rule, a formula, a constant, an invariant — not bytes of source code that happen to look alike.

**Caveat:** what matters is duplicate *knowledge*, not duplicate code. Two methods with the same body but different domain concepts are coincidental duplication, not a DRY violation. Extracting a "shared helper" from them creates the *wrong abstraction* — Sandi Metz: *"Duplication is far cheaper than the wrong abstraction."*

**The one-line test:** *"Will both copies need to change for the same reason in the future?"* If yes → same knowledge → DRY it. If no → coincidental shape → leave separate.

**A trap to internalise.** Today both `calculateInvoiceTax(a)` and `calculateOrderShipping(a)` happen to be `a * 10 / 100`. Tempting "DRY" extracts a `tenPercent(a)` helper. Six months later, tax becomes region-specific (EU 19%, US 7.25%) and shipping gets a flat-rate floor. The "shared" helper now has to either branch on caller (defeats the point) or get abandoned by one side (back to two implementations with a stale abstraction in between). The original duplication would have been cheaper. Tax law and shipping policy are different domains; their arithmetic shape today was a coincidence, not knowledge.

---

## KISS — Keep It Simple, Stupid

Simplicity beats cleverness. If a simpler design works, use it.

---

## YAGNI — You Aren't Gonna Need It

Don't add abstractions or features for hypothetical future needs. Build for today.

---

## Rule of Three

Wait until the same shape appears **three** times before abstracting it. Two similar pieces of code are often coincidence; the third is evidence of a genuine pattern. A direct guardrail against premature DRY — and the practical answer to *"YAGNI said wait, so when do I abstract?"*.

### YAGNI + Rule of Three — the pair

These two answer the same question from opposite ends:

- **YAGNI:** *"When should I wait?"* Until the pain is real.
- **Rule of Three:** *"When should I finally abstract?"* When the third occurrence appears.

Together they form the guardrail against both premature and late abstraction.

---

## Premature optimization is the root of all evil (Knuth)

Don't optimize for performance until you have *measured* a real bottleneck. The speed-focused sibling of YAGNI — avoid speculative complexity whether the speculation is about features or performance.

---

## Law of Demeter ("principle of least knowledge")

An object should only talk to its immediate friends — avoid chains like `a.getB().getC().doThing()`.

The shortcut: each method should call methods on
- itself,
- its parameters,
- objects it creates,
- its direct fields.

Reaching through a chain (`a.b.c.d`) means the calling code knows too much about A's internals — and any change deep in the chain ripples outward.

---

## Separation of Concerns

Each module/layer should address one concern. Overlaps with SRP but applied at module/architecture level.

---

## Tell, Don't Ask

Tell objects what to do rather than asking them for their state and acting on it externally.

```java
// Asking — caller has to know about internal state
if (account.getBalance() > amount) {
    account.setBalance(account.getBalance() - amount);
}

// Telling — caller delegates the decision
account.withdraw(amount);
```

The "telling" version moves logic into the class that owns the state. Pairs naturally with encapsulation.

---

## Fail Fast

Detect invalid state at the earliest point possible (usually the constructor or method entry) and throw, rather than letting bad data propagate and fail mysteriously later. Pairs with preconditions in Design by Contract.

---

## Principle of Least Astonishment (POLA)

An API should behave the way a reasonable caller expects. If a method called `getX()` mutates state, that is astonishing — and wrong. Drives naming, return types, and side-effect discipline.

---

## Boy Scout Rule

Leave the code a little cleaner than you found it. Small continuous improvements beat big-bang rewrites, and keep rot from compounding.

---

## SLAP — Single Level of Abstraction Principle

Inside a single method, every statement should sit at the same conceptual altitude. Mixing high-level orchestration (`notifier.send(...)`) with low-level detail (`byte[] encoded = base64(msg.getBody())`) in the same method is a smell. Extract the detail into a named helper.

It's the method-level version of SRP.

The `onboard()` template method in `DeveloperHiringProcess` respects SLAP — it only coordinates:

```java
public final Employee onboard() {
    checkBudget();
    Employee e = createDeveloper();
    provisionLaptop(e);
    emailService.create(e);
    offerLetterService.send(e);
    return e;
}
```

Every line is at the same altitude — workflow steps. The details live inside `checkBudget()`, `provisionLaptop()`, etc.

---

## Quick recall

**Q. DRY — what's the unit, and the one-line test?**
A. The unit is **knowledge** (business rule, formula, constant, invariant), not source code. Test: *"Will both copies need to change for the same reason?"* Yes → DRY it. No → leave it. (Sandi Metz: "Duplication is far cheaper than the wrong abstraction.")

**Q. YAGNI vs Rule of Three — what does each answer?**
A. YAGNI: *"when should I wait?"* — until the pain is real. Rule of Three: *"when should I finally abstract?"* — when the third occurrence appears. Together they guard against both premature and late abstraction.

**Q. Law of Demeter — which methods can a method call?**
A. Methods on (1) itself, (2) its parameters, (3) objects it creates, (4) its direct fields. No chains like `a.b.c.d.method()`.

**Q. Tell-Don't-Ask in one example?**
A. `account.withdraw(amount)` instead of `if (account.getBalance() > amount) account.setBalance(account.getBalance() - amount)`. Move the decision into the class that owns the state.

**Q. SLAP — the rule?**
A. Inside one method, every statement should sit at the same conceptual altitude. Mixing orchestration with low-level detail in a single method is the smell — extract the detail into a named helper. SLAP is SRP applied at the method level.

**Q. Fail Fast — when and why?**
A. Detect invalid state at the earliest point (usually constructor or method entry) and throw. Don't let bad data propagate and fail mysteriously later — the closer the error is to the root cause, the cheaper the fix.

**Q. POLA — what does it govern?**
A. API behaviour matches reasonable caller expectations. `getX()` that mutates state is astonishing — and wrong. Drives naming, return types, and side-effect discipline.

---

## Related topics

- **SOLID — SRP** — SLAP is SRP applied at the method level.
- **Coupling and Cohesion** — Tell Don't Ask + Law of Demeter both reduce coupling.
- **Encapsulation** — Tell Don't Ask is the behavioural side of encapsulation.


