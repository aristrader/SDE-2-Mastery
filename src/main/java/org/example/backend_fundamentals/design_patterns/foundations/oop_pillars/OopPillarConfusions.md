# OOP Pillar Confusions — the pairs that blur

---

## Abstraction vs Encapsulation

The most commonly conflated pair.

| | Abstraction | Encapsulation |
|---|---|---|
| **Question it answers** | What should the caller see? | How do I protect the internals? |
| **Tool** | Interface / abstract class | `private` fields, access modifiers |
| **Concern** | Design (what to expose) | Protection (how to hide) |

**The tell:** Abstraction is about the *shape* of the API. Encapsulation is about the *safety* of the data behind it.

```java
// Abstraction — caller only knows List, not ArrayList
List<String> list = new ArrayList<>();

// Encapsulation — balance is private; no one outside BankAccount can touch it directly
private double balance;
```

You can have abstraction without encapsulation (a public abstract class with public fields) and encapsulation without abstraction (a concrete class with private fields and no interface).

---

## Polymorphism vs Abstraction

> Covered in the session that produced this doc.

| | Abstraction | Polymorphism |
|---|---|---|
| **What it is** | Design decision — hide details, expose a contract | Mechanism — one reference, many runtime behaviours |
| **Happens at** | Design / compile time | Runtime (virtual dispatch) |

Abstraction says *what to hide*. Polymorphism is *what makes hiding work*.

```java
List<String> list = new ArrayList<>(); // abstraction: caller knows only List
list.add("x");                         // polymorphism: JVM calls ArrayList.add() at runtime
```

---

## Inheritance vs Polymorphism

People say "inheritance gives you polymorphism" — true but they're not the same thing.

| | Inheritance | Polymorphism |
|---|---|---|
| **What it is** | Code reuse + IS-A hierarchy | One type reference behaving differently at runtime |
| **Requires the other?** | No — you can inherit without ever using a parent-type reference | No — interfaces give you polymorphism with zero inheritance |

```java
// Polymorphism via inheritance
Animal a = new Dog();   // Dog extends Animal
a.speak();              // calls Dog.speak()

// Polymorphism via interface — no inheritance involved
Drawable d = new Circle();  // Circle implements Drawable
d.draw();                   // calls Circle.draw()
```

Inheritance is one *way* to get polymorphism. It's not the only way.

---

## Inheritance vs Encapsulation

Inheritance breaks encapsulation — this is a well-known tension (Joshua Bloch: *"design for inheritance or prohibit it"*).

| | What happens |
|---|---|
| `private` field | Subclass cannot see it — encapsulation holds |
| `protected` field | Subclass can see and modify it — encapsulation weakened |
| Overriding a method | Subclass can change behaviour the parent assumed was fixed — fragile base class problem |

```java
class Base {
    protected int count = 0;       // exposed to subclass — encapsulation leak
    public void add() { count++; }
}

class Sub extends Base {
    @Override
    public void add() {
        count += 10;               // silently breaks Base's assumed invariant
        super.add();
    }
}
```

Prefer composition over inheritance when you need reuse but want encapsulation to hold.

---

## Quick recall

**Q. Abstraction vs encapsulation — one line each?**
A. Abstraction = decide what the caller sees. Encapsulation = protect what they don't.

**Q. Can you have polymorphism without inheritance?**
A. Yes — interfaces give you polymorphism with no parent class.

**Q. Why does inheritance weaken encapsulation?**
A. `protected` members and overridable methods let subclasses reach inside the parent's internals, breaking the assumption that only the class itself controls its state.

**Q. Polymorphism vs abstraction — which is the mechanism?**
A. Polymorphism is the mechanism (virtual dispatch at runtime). Abstraction is the design decision that polymorphism enforces.
