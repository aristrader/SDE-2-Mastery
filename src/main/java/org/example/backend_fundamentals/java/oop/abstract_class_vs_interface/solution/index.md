---
order: 20
search: false
---

# Solutions

## Solution: abstract-factory - Abstract Factory variation
An abstract class is natural here because `deliverSet()` represents shared business logic (the orchestration of creating a suite of furniture and generating a slip) that applies identically to all factories. You would flip to an interface if the factories needed to participate in multiple distinct type hierarchies (since Java lacks multiple class inheritance) or if there was strictly zero shared state or template logic.

## Solution: strategy-flavours - Strategy pattern in two flavours
Interfaces are the default choice for Strategy because they keep the strategies highly decoupled and easily mockable. You elevate a Strategy interface to an abstract class when every strategy *must* execute some pre/post hooks (like timing logging, common validations, or audit trails) that you don't want to duplicate across all concrete classes.

## Solution: default-methods - Default methods test
A `default` method works perfectly for convenience methods that only rely on the other interface methods (e.g., `default void runTwice() { run(); run(); }`). You promote it to an abstract class when the shared method needs to access shared *state* (fields), since interfaces cannot hold instance variables.

## Solution: builder-hierarchy - Builder hierarchy (EJ Item 2)
`Pizza` is an abstract class because it holds state (the `Set<Topping> toppings` field) and provides the actual mutation methods for that state. An interface cannot hold this state, forcing every subclass to duplicate the topping collection field and its mutators.
