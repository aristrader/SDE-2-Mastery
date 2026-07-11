---
order: 20
---

# Object-Oriented Programming

This chapter moves from object mechanics into the four OOP pillars and the Java rules that support them. Read the mechanics first, then the pillars, then the design tradeoffs.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `classes_and_objects` | Classes, objects, constructors, `this`, and object creation. |
| 2 | `methods` | Signatures, pass-by-value, and varargs. |
| 3 | `object_model` | References, identity, lifecycle, and runtime object behavior. |
| 4 | `static_and_final` | Class-level members, final variables, final methods, and final classes. |
| 5 | `access_modifiers` | Java visibility mechanics used by encapsulation. |
| 6 | `pillars` | Encapsulation, abstraction, inheritance, polymorphism, and composition trade-offs. |
| 7 | `nested_classes` | Inner, static nested, local, and anonymous classes. |
| 8 | `method_dispatch` | Focused dispatch revision after inheritance/polymorphism. |
| 9 | `abstract_class_vs_interface` | Choosing Java abstraction mechanisms. |
| 10 | `equals_hashcode` | Object equality, hashing, and collection behavior. |

## Four pillars map

| Pillar | Main page | One-line purpose |
| --- | --- | --- |
| Encapsulation | `pillars/encapsulation` | Protect state and invariants. |
| Abstraction | `pillars/abstraction` | Expose the useful contract, hide implementation detail. |
| Inheritance | `pillars/inheritance` | Model a true substitutable IS-A relationship. |
| Polymorphism | `pillars/polymorphism` | Let the runtime implementation decide behavior behind a common type. |

Keep the common confusions local to the detailed pages: abstraction vs encapsulation, inheritance vs polymorphism, and composition vs inheritance each have their own focused treatment.

## Quick recall

**Q. Abstraction vs encapsulation — one line each?**
A. Abstraction = decide what the caller sees. Encapsulation = protect what they don't.

**Q. Can you have polymorphism without inheritance?**
A. Yes — interfaces give you polymorphism with no parent class.

**Q. Why does inheritance weaken encapsulation?**
A. `protected` members and overridable methods let subclasses reach inside the parent's internals, breaking the assumption that only the class itself controls its state.

**Q. Polymorphism vs abstraction — which is the mechanism?**
A. Polymorphism is the mechanism (virtual dispatch at runtime). Abstraction is the design decision that polymorphism enforces.
