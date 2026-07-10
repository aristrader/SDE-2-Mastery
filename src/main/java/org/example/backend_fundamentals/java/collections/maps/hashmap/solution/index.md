---
order: 20
search: false
---

# Solutions

## Solution: mutable-key-trap - The Orphaned Entry

```java
Person p = new Person("Alice");
Map<Person, String> map = new HashMap<>();
map.put(p, "Engineer");

p.setName("Bob"); // Mutation!

System.out.println(map.get(p)); // Prints null
System.out.println(map.size()); // Prints 1
```
The entry is permanently orphaned. When inserted, the map calculated the hash for "Alice" and cached it, placing the entry in Bucket X. After mutation, `map.get(p)` calculates the hash for "Bob", which leads to Bucket Y. It searches Bucket Y, finds nothing, and returns `null`. The entry is still stuck in Bucket X. Keys must be immutable!

## Solution: power-of-two - Bucket Index Calculation

```java
int capacity = 16;
int hash = 1042;
int index = (capacity - 1) & hash;
System.out.println(index); // Prints 2
```
When capacity is a power of two (like 16), `capacity - 1` is exactly a bitmask of all 1s for the needed lower bits (e.g., 15 is `00001111` in binary). The bitwise `&` isolates those lower 4 bits of the hash, effectively acting as an extremely fast modulo `hash % 16` operation. `1042 % 16` is `2`.
