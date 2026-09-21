---
order: 20
---

# Methods: API selection and argument passing

The interview question behind methods is: **which declaration does Java select, what value crosses the call boundary, and which behavior can change later?** Keep compile-time selection separate from runtime dispatch; most method bugs come from mixing them.

## A method declaration is an API contract

For overload selection, a method name plus its parameter types (and their order) distinguish callable declarations. A return type and `throws` clause do not let Java choose between two calls, so they cannot create a useful overload.

```java
void publish(String topic) { }
void publish(String topic, int priority) { } // valid overload

// int publish(String topic) { return 0; }   // compile error: return type alone differs
```

Overload only when the operations have the same meaning with a natural variation in input. `save(Order)` and `save(String rawJson)` often need different names or a boundary type; making callers guess which conversion an overload performs makes an API harder to reason about.

## Compile-time overload selection

The compiler chooses an overload from the method name, the compile-time types of the arguments, and the declared type through which the call is made. It prefers applicable fixed-arity choices before a varargs fallback. It does not inspect the object's runtime class to select an overload.

```java
class Formatter {
    void format(int value)    { System.out.println("int"); }
    void format(long value)   { System.out.println("long"); }
    void format(Integer value){ System.out.println("Integer"); }
}

new Formatter().format(7); // int: exact primitive match wins
```

`null` is a useful interview trap. `format(String value)` and `format(Integer value)` make `format(null)` ambiguous because neither unrelated reference type is more specific. Do not expose overload sets that turn ordinary caller values into compiler puzzles.

## Java passes every argument by value

For a primitive, the copied value is the number or boolean. For an object, the copied value is a reference to the same object. A callee can mutate that object through its reference, but reassigning the parameter cannot replace the caller's reference.

```java
void rename(Customer customer) {
    customer.setName("updated");       // caller observes this mutation
    customer = new Customer("c-2");    // only this local copy changes
}
```

The naive explanation “objects are passed by reference” predicts the second line changes the caller. It does not. Prefer immutable values when shared mutation is not part of the contract; otherwise name and document the mutation explicitly.

## Varargs are array parameters

`String... messages` is a variable-arity parameter represented as a `String[]` inside the method. It must be the last parameter, and a declaration may have only one varargs parameter.

```java
void log(int level, String... messages) {
    for (String message : messages) {
        System.out.println(level + ": " + message);
    }
}
```

Varargs are convenient at a call site, but they allocate or receive an array and can make an overload set surprising. Use them for genuinely small optional lists such as formatting arguments. For a large or already-owned collection, accept `List<String>` or a stream-oriented API instead. Never mutate the varargs array unless the contract clearly says so; a caller can pass its own array directly.

## What methods can vary at runtime?

An overridable instance method may use the runtime object's implementation; that is dynamic dispatch and belongs in the next page. `static`, `private`, and `final` methods have different rules, so do not treat every same-named method as polymorphism. Add `@Override` whenever you intend to override: it turns a near-miss signature into a compiler error.

## Quick recall

- **What decides an overload?** Compile-time argument/reference types and applicable parameter lists, not the runtime object's class.
- **Can return type alone overload a method?** No; it is not a call-selection input.
- **What crosses an object call boundary?** A copy of the reference, so object mutation is visible but parameter reassignment is local.
- **What is a varargs parameter inside the method?** An array.
- **Where may varargs appear?** Once, as the last parameter.
- **Why use `@Override`?** It proves that a declaration overrides rather than accidentally overloads.
