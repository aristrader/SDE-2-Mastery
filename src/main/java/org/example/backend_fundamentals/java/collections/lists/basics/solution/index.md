---
order: 20
search: false
---

# Solutions

## Solution: basic-arraylist-operations - Basic ArrayList Operations

```java
public record ListSummary(int first, int last, int size, boolean containsTarget) {
}

public ListSummary basicOperations(int target) {
    List<Integer> numbers = new ArrayList<>();
    numbers.add(10);
    numbers.add(20);
    numbers.add(30);
    numbers.add(40);
    numbers.add(50);

    return new ListSummary(
            numbers.get(0),
            numbers.get(numbers.size() - 1),
            numbers.size(),
            numbers.contains(target)
    );
}
```

## Solution: insert-update-and-remove - Insert, Update, and Remove

```java
public List<String> modifyList(List<String> input) {
    List<String> names = new ArrayList<>(input);

    names.add("End");
    names.add(1, "Inserted");
    names.set(2, "Updated");
    names.remove(0);
    names.remove("End");
    return names;
}
```

## Solution: integer-remove-gotcha - Integer Remove Gotcha

```java
public List<Integer> removeByValue(List<Integer> list, int target) {
    list.remove(Integer.valueOf(target));
    return list;
}
```

## Solution: sort-and-reverse - Sort and Reverse

```java
public record SortedLists(List<Integer> ascending, List<Integer> descending) {
}

public SortedLists sortAndReverse(List<Integer> input) {
    List<Integer> ascending = new ArrayList<>(input);
    Collections.sort(ascending);

    List<Integer> descending = new ArrayList<>(ascending);
    Collections.reverse(descending);

    return new SortedLists(ascending, descending);
}
```

## Solution: sublist-practice - subList Practice

```java
public List<String> getSublist(List<String> list, int from, int to) {
    return new ArrayList<>(list.subList(from, to));
}
```

## Solution: merge-two-lists - Merge Two Lists

```java
public List<Integer> mergeLists(List<Integer> first, List<Integer> second) {
    List<Integer> merged = new ArrayList<>(first);
    merged.addAll(second);
    return merged;
}
```

## Solution: remove-duplicates-while-preserving-order - Remove Duplicates While Preserving Order

```java
public List<Integer> removeDuplicates(List<Integer> list) {
    List<Integer> result = new ArrayList<>();
    Set<Integer> seen = new HashSet<>();

    for (Integer value : list) {
        if (seen.add(value)) {
            result.add(value);
        }
    }

    return result;
}
```

## Solution: rotate-list-right - Rotate List Right

```java
public List<Integer> rotateRight(List<Integer> list, int k) {
    if (list.isEmpty()) {
        return new ArrayList<>();
    }

    int shift = k % list.size();
    if (shift == 0) {
        return new ArrayList<>(list);
    }

    int split = list.size() - shift;
    List<Integer> result = new ArrayList<>(list.subList(split, list.size()));
    result.addAll(list.subList(0, split));
    return result;
}
```

## Solution: mini-problem-student-names - Mini Problem: Student Names

```java
public List<String> cleanNames(List<String> names) {
    List<String> cleaned = new ArrayList<>();

    for (String name : names) {
        String trimmed = name.trim();
        if (!trimmed.isEmpty()) {
            cleaned.add(trimmed);
        }
    }

    Collections.sort(cleaned);
    return cleaned;
}
```
