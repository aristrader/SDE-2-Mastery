---
title: Interview Questions
order: 20
search: false
---

# OOP Interview Questions

## Question 1: Why does overriding only `equals()` break `HashMap`?

Answer shape: hash lookup starts with `hashCode()`. Equal objects with different hashes can land in different buckets, so `equals()` may never run.

Related full practice: [hash collection traps](../../equals_hashcode/hash_collections_traps/exercise/).

## Question 2: When would you prefer composition over inheritance?

Answer shape: prefer composition when behavior varies independently, the relationship is not a true is-a relationship, or inheritance would expose/force unwanted parent behavior.

## Question 3: What is dynamic dispatch?

Answer shape: for overridden instance methods, Java chooses the method implementation from the actual runtime object, not the declared reference type.
