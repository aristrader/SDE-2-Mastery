---
order: 70
---

# Low-Level Design Interview Framework

An LLD round tests whether you can turn an ambiguous prompt into small, coherent objects and prove that the important behavior works. Do not start by writing code or by forcing design patterns into the model.

## Delivery flow for a 35-45 minute round

| Phase | Time | Output |
| --- | --- | --- |
| Requirements | 5 min | Core actions, business rules, invalid cases, and explicit exclusions |
| Entities and relationships | 3 min | A few entities, ownership arrows, and the object coordinating the workflow |
| Class design | 10-15 min | State, public methods, responsibilities, and focused interfaces |
| Implementation and verification | 10-12 min | Main-path pseudocode/Java, key invalid path, and one scenario trace |
| Extensibility | Remaining time | A clean answer to one likely follow-up; do not rewrite the design |

The interviewer may change the order or ask for code sooner. Follow that direction, but gently return to the flow: requirements first, structure second, behavior third. For a short round, spend less time on extensions, not on requirements or the core method.

## 1. Clarify requirements

Ask enough questions to turn the prompt into a small spec:

- Which operations must the system support?
- What rules decide success, rejection, and state transitions?
- Which invalid inputs or illegal operations must be handled?
- Is UI, persistence, networking, concurrency, authentication, or extensibility in scope?

Write the agreed requirements and exclusions. For a parking lot, vehicle entry, allocation, exit, fee calculation, and full-lot behavior may be in scope; database persistence and a mobile UI usually are not unless stated.

Your whiteboard output should be short and explicit:

```text
Requirements
1. ...
2. ...

Out of scope
- ...
```

## 2. Find entities and relationships

Start with meaningful nouns from the requirements. Create an entity when it owns changing state or enforces a rule. Keep data that has no independent behavior as a field instead of creating a class for every noun.

Then decide ownership:

- Which object coordinates the use case?
- Which object owns each invariant and state transition?
- Which objects are composed together, and which are just collaborators?

Use simple boxes and arrows. Formal UML is optional; clear ownership matters more than notation.

```text
ParkingLot -> Floors -> ParkingSpots
ParkingLot -> SpotFinder
ParkingLot -> PricingPolicy
```

This is enough structure for an interviewer to follow. Do not spend time on formal UML notation unless they explicitly request it.

## 3. Design classes from requirements

For each core class, derive two things from the requirements:

| Ask | Result |
| --- | --- |
| What must this object remember to uphold its rules? | State: fields, enum state, collections, collaborators |
| What must callers be able to do or ask? | Behavior: small public methods with clear inputs and results |

Keep a rule with the object that owns the necessary state. `ParkingSpot` decides whether it fits a vehicle; `ParkingLot` coordinates allocation across floors; a caller should not pull out fields and recreate those rules.

Use an interface only when behavior genuinely varies, such as `PricingPolicy` or `PaymentProcessor`. Introduce Strategy, State, Factory, or Observer because a requirement needs variation, lifecycle, construction control, or notification, not because a pattern name is expected.

## 4. Implement the core behavior

Ask whether the interviewer wants Java, pseudocode, or a walk-through. Start with the most important method and its normal path. Then cover the edge cases that enforce the business rules.

```java
Ticket park(Vehicle vehicle) {
    ParkingSpot spot = spotFinder.findSpotFor(vehicle);
    if (spot == null) {
        throw new LotFullException();
    }
    spot.occupy(vehicle);
    return ticketFactory.create(vehicle, spot);
}
```

The code is not the point by itself. Explain which object validates each step, how state changes, and how the result is returned. Avoid complete getters/setters, persistence plumbing, or every constructor unless the interviewer asks.

### Verify with one scenario

Trace one concrete scenario: initial state, operation, collaborating calls, state after the operation, and result. This catches missing transitions and proves that the classes work together.

Do this before extensions. It is a quick proof that your core logic works and often exposes a missing transition while you can still fix it.

## 5. Handle extensions without redesigning

For a follow-up, point to the existing boundary and explain the smallest extension. Example: a new pricing rule becomes another `PricingPolicy`; it should not require rewriting `Ticket` and every checkout call. If the follow-up is concurrency, state the invariant first, then choose synchronization or a transaction only where simultaneous updates can violate it.

Stay high level unless asked to implement the extension. The signal is that the original responsibilities and mutation boundaries make the change local, not that you can name many patterns.

## Interview traps

1. **Starting with code.** You will optimize the wrong structure and hide your design reasoning.
2. **A class per noun.** Prefer a field when the concept has no independent state or behavior.
3. **Anemic objects.** Do not expose state so a service can reimplement every rule elsewhere.
4. **Pattern dumping.** A pattern without a concrete variation point is extra complexity.
5. **Skipping verification.** A short scenario trace often exposes a missing state change before the interviewer does.
6. **Redesigning for a follow-up.** Point to the existing extension point before adding a new abstraction.

## Quick recall

**Q. What is the first LLD interview step?**
A. Agree on operations, rules, invalid cases, and what is out of scope.

**Q. How do you decide whether something deserves a class?**
A. It needs independent state or behavior that owns a meaningful rule.

**Q. Where should a business rule live?**
A. With the object that owns the state needed to enforce it.

**Q. When should you add a design pattern?**
A. Only when a requirement creates a real variation or lifecycle problem that the pattern simplifies.

**Q. How do you end an LLD round?**
A. Trace a non-trivial scenario, then show where a likely extension fits without rewriting the core design.
