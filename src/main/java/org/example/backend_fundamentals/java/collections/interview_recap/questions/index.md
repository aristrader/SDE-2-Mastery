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

Answer shape: the entry keeps the hash cached at insertion. If mutation changes a field used by `hashCode()`, lookup can compute a different hash and miss the stored entry; a later resize does not repair that mismatch.

Related full practice: [hash collection traps](../../../oop/equals_hashcode/hash_collections_traps/exercise/).

## Question 3: Pick between `HashMap`, `LinkedHashMap`, `TreeMap`, and `ConcurrentHashMap`.

Answer shape: default lookup map is `HashMap`; predictable insertion/access order is `LinkedHashMap`; sorted keys is `TreeMap`; shared concurrent access is `ConcurrentHashMap`.

## Quick recall

**Q. What is the safe default FIFO queue implementation?**
A. `ArrayDeque`.

**Q. What makes a hash-based lookup reliable?**
A. A stable key whose `equals()` and `hashCode()` obey their contract.
