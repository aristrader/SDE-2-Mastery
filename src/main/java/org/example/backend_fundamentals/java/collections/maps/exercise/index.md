---
order: 10
search: false
---

# Practice

## Exercise: basic-hashmap-operations - Basic HashMap Operations

### Objective
Practice core `HashMap` operations.

### Task
Create a `HashMap<String, Integer>` for student marks.

Use:

- `put`
- `get`
- `containsKey`
- `containsValue`
- `remove`
- `replace`
- `getOrDefault`
- `size`
- `isEmpty`

### Checks
- Can you explain what each operation returns?
- What happens when you call `get()` for a missing key?

## Exercise: duplicate-keys - Duplicate Keys

### Objective
Understand that map keys are unique.

### Task
Insert the same key multiple times into a `HashMap`.

Observe:

- return value from `put()`
- final value stored for that key
- final map size

### Checks
- Does duplicate `put()` add a second entry or replace the old value?

## Exercise: frequency-counter - Frequency Counter

### Objective
Use a map as a counting table.

### Task
Given an `int[]`, return the frequency of every number.

Use:

```java
getOrDefault()
```

### Checks
- Does each unique number appear exactly once in the result?
- Are counts correct for repeated numbers?

## Exercise: word-frequency - Word Frequency

### Objective
Count words with a `HashMap`.

### Task
Given a paragraph, count occurrences of every word.

Ignore punctuation.

### Checks
- Are words normalized consistently?
- Are punctuation marks excluded from keys?

## Exercise: first-non-repeating-character - First Non-Repeating Character

### Objective
Use character frequencies to solve a classic string problem.

### Task
Given a string, return the first character whose frequency is one.

Use `HashMap`.

### Checks
- Does the answer preserve original character order?
- What do you return when no such character exists?

## Exercise: group-anagrams - Group Anagrams

### Objective
Use a map to group related values.

### Task
Group words that are anagrams.

Use `HashMap`.

Do not use streams.

### Checks
- Do anagrams land in the same group?
- Do non-anagrams stay separate?

## Exercise: mutable-key-experiment - Mutable Key Experiment

### Objective
See why map keys should be immutable.

### Task
Create a custom class and use it as a `HashMap` key.

After insertion, modify a field used by `equals()` and `hashCode()`.

Observe:

- `get()`
- `containsKey()`

### Checks
- Write a one-line explanation of the result.

## Exercise: linkedhashmap-insertion-order - LinkedHashMap Insertion Order

### Objective
Verify insertion-order iteration.

### Task
Store employee IDs in a `LinkedHashMap`.

Print them and verify insertion order.

### Checks
- Does iteration match insertion order?
- How would `HashMap` differ?

## Exercise: treemap-navigation - TreeMap Navigation

### Objective
Practice sorted map navigation.

### Task
Insert random integers into a `TreeMap`.

Try:

- `firstKey()`
- `lastKey()`
- `higherKey()`
- `lowerKey()`

### Checks
- Are keys automatically sorted?
- What do `higherKey()` and `lowerKey()` return for boundary values?

## Exercise: student-marks-system - Student Marks System

### Objective
Choose the correct `Map` implementation for a small problem.

### Task
Build a student marks system.

Support:

- add student
- update marks
- lookup marks
- remove student
- print topper
- print all students alphabetically

Choose the appropriate `Map` implementation and explain why.

### Checks
- Does the implementation support alphabetical output naturally?
- Is topper calculation correct?

## Exercise: shallow-immutability-trap - Shallow Immutability

### Goal
Understand that `Map.of()` freezes the map structure, but does not freeze mutable value objects.

### Task
Create a `List<String> mutableList = new ArrayList<>(); mutableList.add("A");`.
Create a map: `Map<String, List<String>> map = Map.of("items", mutableList);`.
Attempt to add an item to the list through the map: `map.get("items").add("B");`.
Print the map.

### Checks
- Did the `add("B")` operation succeed? Why doesn't `Map.of` prevent this?

## Exercise: map-of-mutation - Freezing Map Methods

### Goal
Understand the breadth of methods that `Map.of()` disables.

### Task
Using the map from the previous exercise, try to safely calculate a new value using `map.computeIfAbsent("newKey", k -> new ArrayList<>());`.

### Checks
- What exception is thrown? 
- Why does `computeIfAbsent` fail even if you think you are just "reading" a frozen map?
