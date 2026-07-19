---
title: Interview Questions
order: 20
search: false
---

# Collections Interview Questions

## Question 1: What happens inside `HashMap.get(key)`?

Answer shape: compute spread hash, mask into bucket index, inspect that bucket, compare candidate keys with `equals()`, return value or miss.

Related full practice: [HashMap exercise](../../maps/hashmap/exercise/).

## Question 2: Why can mutating a key break lookup?

Answer shape: the entry stays in the bucket chosen by the old hash. After mutation, lookup computes a new hash and searches another bucket.

Related full practice: [hash collection traps](../../../oop/equals_hashcode/hash_collections_traps/exercise/).

## Question 3: Pick between `HashMap`, `LinkedHashMap`, `TreeMap`, and `ConcurrentHashMap`.

Answer shape: default lookup map is `HashMap`; predictable insertion/access order is `LinkedHashMap`; sorted keys is `TreeMap`; shared concurrent access is `ConcurrentHashMap`.
