---
order: 20
search: false
---

# Solutions

## Solution: frequency-counter - Frequency Counter

```java
int[] nums = {1, 2, 1, 3, 2, 1};
Map<Integer, Integer> freq = new HashMap<>();

for (int num : nums) {
    freq.put(num, freq.getOrDefault(num, 0) + 1);
}
```

Map key = number. Map value = count.

## Solution: word-frequency - Word Frequency

```java
String paragraph = "Java, Java maps. Maps count words!";
String cleaned = paragraph.toLowerCase().replaceAll("[^a-z0-9\\s]", "");

Map<String, Integer> freq = new HashMap<>();
for (String word : cleaned.split("\\s+")) {
    if (!word.isBlank()) {
        freq.put(word, freq.getOrDefault(word, 0) + 1);
    }
}
```

Normalize case and remove punctuation before counting.

## Solution: first-non-repeating-character - First Non-Repeating Character

```java
String input = "swiss";
Map<Character, Integer> freq = new HashMap<>();

for (char ch : input.toCharArray()) {
    freq.put(ch, freq.getOrDefault(ch, 0) + 1);
}

Character answer = null;
for (char ch : input.toCharArray()) {
    if (freq.get(ch) == 1) {
        answer = ch;
        break;
    }
}
```

Count first, then scan original order.

## Solution: group-anagrams - Group Anagrams

```java
String[] words = {"eat", "tea", "tan", "ate", "nat", "bat"};
Map<String, List<String>> groups = new HashMap<>();

for (String word : words) {
    char[] chars = word.toCharArray();
    Arrays.sort(chars);
    String key = new String(chars);
    groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(word);
}
```

Sorted letters form the grouping key.
