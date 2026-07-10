---
order: 10
search: false
---

# Practice

## Exercise: navigable-methods - Range and Proximity Queries

### Goal
Use `TreeMap`'s `NavigableMap` methods to perform proximity searches that are impossible in `HashMap`.

### Task
Create a `TreeMap<Integer, String> schedule = new TreeMap<>();`.
Add three bookings: `1000` (10:00 AM), `1300` (1:00 PM), and `1500` (3:00 PM).
A user requests a booking at `1200`. Find the first available booking that is *at or after* `1200` using `ceilingKey`.

### Checks
- Which key does `ceilingKey(1200)` return? What would `HashMap` require you to do to find this?

## Exercise: submap-live-view - Live SubMap Views

### Goal
Understand that `subMap` returns a live view backed by the original `TreeMap`.

### Task
Using the `schedule` map from above, create a view of the afternoon: `SortedMap<Integer, String> afternoon = schedule.tailMap(1200);`.
Add a new booking to the original map: `schedule.put(1600, "4:00 PM");`.
Print the `afternoon` view map.

### Checks
- Does the `afternoon` map show the new `1600` booking? Why?
