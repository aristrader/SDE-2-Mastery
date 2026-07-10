---
order: 20
---

# Frequency and Grouping Patterns

Maps become powerful when the key is not just an ID, but a computed fact about the input.

Use these patterns when you catch yourself asking:

- "How many times did this value appear?"
- "Have I seen this before?"
- "Which values belong together?"
- "Can I turn repeated scanning into one lookup?"

## Frequency counter

Use a map when each distinct input value needs a count.

The explicit version is easiest to understand first:

```java
Map<String, Integer> freq = new HashMap<>();

for (String word : words) {
    if (freq.containsKey(word)) {
        freq.put(word, freq.get(word) + 1);
    } else {
        freq.put(word, 1);
    }
}
```

Same logic, more compact with `getOrDefault`:

```java
Map<String, Integer> freq = new HashMap<>();

for (String word : words) {
    freq.put(word, freq.getOrDefault(word, 0) + 1);
}
```

`getOrDefault(word, 0)` means: "if this word was never seen, treat its current count as zero."

Mental model:

```text
key   = thing being counted
value = count so far
```

Examples:

- character frequency
- word frequency
- number frequency
- inventory count
- vote count

## First non-repeating pattern

Some answers depend on original order. A frequency map does not preserve the answer by itself.

Use two passes:

1. Count everything.
2. Scan the original input again and return the first item whose count is `1`.

```java
Map<Character, Integer> freq = new HashMap<>();

for (char ch : input.toCharArray()) {
    freq.put(ch, freq.getOrDefault(ch, 0) + 1);
}

for (char ch : input.toCharArray()) {
    if (freq.get(ch) == 1) {
        return ch;
    }
}
```

The second pass is what preserves original order.

## Grouping pattern

Use grouping when one key maps to many values.

```java
Map<String, List<String>> groups = new HashMap<>();

for (String word : words) {
    String key = buildKey(word);
    groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(word);
}
```

Mental model:

```text
key   = group identity
value = all items in that group
```

For anagrams, the group identity can be sorted characters:

```java
char[] chars = word.toCharArray();
Arrays.sort(chars);
String key = new String(chars);
```

`eat`, `tea`, and `ate` all produce `aet`, so they land in the same list.

## Lookup table pattern

Use a map when you want to trade memory for faster repeated lookup.

Without a map:

```text
for each query -> scan the whole list
```

With a map:

```text
build index once -> answer each query by key
```

This is the same idea behind caches, ID-to-object maps, and precomputed counts.

## Which update method?

| Need | Pattern |
| --- | --- |
| Increment count | `put(key, getOrDefault(key, 0) + 1)` |
| Increment count compactly | `merge(key, 1, Integer::sum)` |
| Create list if absent | `computeIfAbsent(key, ignored -> new ArrayList<>())` |
| Read without changing map | `get` / `getOrDefault` |

## Quick recall

- **Counter key?** Thing being counted.
- **Counter value?** Count so far.
- **Grouping key?** Shared identity for related values.
- **Grouping value?** Collection of values in that group.
- **Order-sensitive answer?** Count first, then scan original input.
