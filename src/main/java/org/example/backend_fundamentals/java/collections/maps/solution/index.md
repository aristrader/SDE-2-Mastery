---
order: 20
search: false
---

# Solutions

## Solution: basic-hashmap-operations - Basic HashMap Operations

```java
Map<String, Integer> marks = new HashMap<>();

marks.put("Asha", 90);
marks.put("Ravi", 82);

System.out.println(marks.get("Asha"));              // 90
System.out.println(marks.containsKey("Ravi"));      // true
System.out.println(marks.containsValue(82));        // true
System.out.println(marks.getOrDefault("Mina", 0));  // 0

marks.replace("Ravi", 88);
marks.remove("Asha");

System.out.println(marks.size());
System.out.println(marks.isEmpty());
```

`get()` returns `null` for a missing key unless you use `getOrDefault()`.

## Solution: duplicate-keys - Duplicate Keys

```java
Map<String, Integer> marks = new HashMap<>();

System.out.println(marks.put("Asha", 80)); // null
System.out.println(marks.put("Asha", 95)); // 80

System.out.println(marks.get("Asha"));     // 95
System.out.println(marks.size());          // 1
```

Duplicate keys replace the old value. `put()` returns the previous value, or `null` if no mapping existed.

## Solution: frequency-counter - Frequency Counter

```java
int[] nums = {1, 2, 1, 3, 2, 1};
Map<Integer, Integer> freq = new HashMap<>();

for (int num : nums) {
    freq.put(num, freq.getOrDefault(num, 0) + 1);
}

System.out.println(freq); // {1=3, 2=2, 3=1}
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

System.out.println(freq);
```

Normalize case and remove punctuation before counting, otherwise `"Java"` and `"java,"` become different keys.

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

System.out.println(answer); // w
```

Count first, then scan original order. Frequency maps do not preserve input order by themselves.

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

System.out.println(groups.values());
```

Sorted letters form the grouping key. All anagrams produce the same key.

## Solution: mutable-key-experiment - Mutable Key Experiment

```java
class EmployeeKey {
    String id;

    EmployeeKey(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EmployeeKey key && Objects.equals(id, key.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

Map<EmployeeKey, String> employees = new HashMap<>();
EmployeeKey key = new EmployeeKey("E1");
employees.put(key, "Asha");

key.id = "E2";

System.out.println(employees.get(key));         // usually null
System.out.println(employees.containsKey(key)); // usually false
```

Changing a key field after insertion changes the hash, so HashMap searches the wrong bucket.

## Solution: linkedhashmap-insertion-order - LinkedHashMap Insertion Order

```java
Map<Integer, String> employees = new LinkedHashMap<>();
employees.put(103, "Asha");
employees.put(101, "Ravi");
employees.put(102, "Mina");

for (Integer id : employees.keySet()) {
    System.out.println(id);
}
```

`LinkedHashMap` preserves insertion order. `HashMap` gives no ordering guarantee.

## Solution: treemap-navigation - TreeMap Navigation

```java
TreeMap<Integer, String> ranks = new TreeMap<>();
ranks.put(30, "Bronze");
ranks.put(10, "Gold");
ranks.put(20, "Silver");

System.out.println(ranks.keySet());      // [10, 20, 30]
System.out.println(ranks.firstKey());    // 10
System.out.println(ranks.lastKey());     // 30
System.out.println(ranks.higherKey(20)); // 30
System.out.println(ranks.lowerKey(20));  // 10
```

`TreeMap` keeps keys sorted and supports navigation in O(log n).

## Solution: student-marks-system - Student Marks System

```java
TreeMap<String, Integer> marks = new TreeMap<>();

marks.put("Ravi", 82);
marks.put("Asha", 91);
marks.put("Mina", 88);

marks.replace("Ravi", 86);
System.out.println(marks.get("Asha"));
marks.remove("Mina");

Map.Entry<String, Integer> topper = null;
for (Map.Entry<String, Integer> entry : marks.entrySet()) {
    if (topper == null || entry.getValue() > topper.getValue()) {
        topper = entry;
    }
}

System.out.println("Topper: " + topper);
System.out.println("Alphabetical: " + marks);
```

Use `TreeMap` because system must print students alphabetically by name. Topper still needs a scan unless you maintain a second index.

## Solution: shallow-immutability-trap - Shallow Immutability

```java
List<String> mutableList = new ArrayList<>();
mutableList.add("A");

Map<String, List<String>> map = Map.of("items", mutableList);
map.get("items").add("B"); // Succeeds!

System.out.println(map); // Prints {items=[A, B]}
```
`Map.of()` provides *shallow immutability*. It prevents you from changing which key points to which value (the structure of the map). However, if the value itself is a mutable object (like an `ArrayList`), anyone with a reference to it can still mutate its internal state. For deep immutability, you must wrap the value in `List.copyOf()` before putting it in the map.

## Solution: map-of-mutation - Freezing Map Methods

```java
// map.computeIfAbsent("newKey", k -> new ArrayList<>()); 
// Throws UnsupportedOperationException
```
Every single method that *could* mutate the map throws `UnsupportedOperationException` immediately when called on a `Map.of()` instance. `computeIfAbsent` attempts to put a value if the key is absent, which is a structural mutation, so the method is completely disabled on immutable maps.
