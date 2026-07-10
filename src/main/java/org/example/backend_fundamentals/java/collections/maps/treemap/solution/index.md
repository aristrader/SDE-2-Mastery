---
order: 20
search: false
---

# Solutions

## Solution: navigable-methods - Range and Proximity Queries

```java
TreeMap<Integer, String> schedule = new TreeMap<>();
schedule.put(1000, "10:00 AM");
schedule.put(1300, "1:00 PM");
schedule.put(1500, "3:00 PM");

Integer nextSlot = schedule.ceilingKey(1200);
System.out.println(nextSlot); // Prints 1300
```
`ceilingKey(1200)` navigates the Red-Black tree in O(log n) time to find the smallest key that is greater than or equal to 1200. To do this in a `HashMap`, you would have to iterate over all keys, sort them, and search manually (O(n log n)).

## Solution: submap-live-view - Live SubMap Views

```java
SortedMap<Integer, String> afternoon = schedule.tailMap(1200);
System.out.println(afternoon); // Prints {1300=1:00 PM, 1500=3:00 PM}

schedule.put(1600, "4:00 PM");

System.out.println(afternoon); // Prints {1300=1:00 PM, 1500=3:00 PM, 1600=4:00 PM}
```
The views returned by `headMap`, `tailMap`, and `subMap` are **live**. They do not copy the data. They are simply windows into the original `TreeMap` that restrict operations to a specific key range. Mutating the original map is instantly visible in the view (as long as the new key falls within the view's bounds).
