---
order: 170
---

# LRU Cache LLD

Practice combining a hash map and doubly linked list to keep `get`, `put`, and eviction O(1) on average.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Record your entities and class diagram in the
[playground worksheet](playground/entity_identification_and_class_diagrams.md).

## Working order

1. Confirm the cache contract, capacity, missing-key behavior, and concurrency scope.
2. State the LRU invariant before proposing a data structure.
3. Walk through one cache hit and one insertion that evicts the least recently used key.
4. Derive the map and linked-list responsibilities from those two flows.

For the JDK alternative, see the [LinkedHashMap LRU pattern](../../../java/collections/maps/linked_hashmap/).

## Quick recall

- The map gives direct node lookup; the list records recency order.
- Every read and write makes that key most recently used.
- A full cache evicts the least recently used node before adding a new one.
