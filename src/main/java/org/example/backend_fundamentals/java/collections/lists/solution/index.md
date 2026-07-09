---
order: 20
search: false
---

# Solutions

## Solution: arrays-aslist-trap - The Fixed-Size Trap

```java
List<String> list = Arrays.asList("A", "B", "C");
list.set(0, "X"); // Succeeds. The list is now [X, B, C]
// list.add("D"); // Throws UnsupportedOperationException
```
`Arrays.asList` returns a lightweight wrapper directly over a backing array. Because Java arrays cannot change their length, you cannot add or remove elements. However, you *can* mutate the existing slots using `.set()`. 

## Solution: list-of-immutability - Fully Immutable Lists

```java
List<String> list = List.of("A", "B", "C");
// list.set(0, "X"); // Throws UnsupportedOperationException
// list.add("D");    // Throws UnsupportedOperationException

// List.of("A", null); // Throws NullPointerException at construction
```
`List.of()` returns a genuinely immutable collection. All mutation operations (add, remove, set, clear) throw `UnsupportedOperationException`. Furthermore, it proactively guards against `null` values by throwing a `NullPointerException` immediately at construction if any element is null.


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
public List<String> modifyList(List<String> list) {
    ArrayList<String> names = (ArrayList<String>) list;

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


## Solution: iterate-three-ways - Iterate Three Ways

```java
public int sumUsingIndexLoop(List<Integer> list) {
    int sum1 = 0;
    for (int i = 0; i < list.size(); i++) {
        sum1 += list.get(i);
    }
    return sum1;
}

public int sumUsingEnhancedFor(List<Integer> list) {
    int sum2 = 0;
    for (Integer num : list) {
        sum2 += num;
    }
    return sum2;
}

public int sumUsingIterator(List<Integer> list) {
    int sum3 = 0;
    Iterator<Integer> it = list.iterator();
    while (it.hasNext()) {
        sum3 += it.next();
    }
    return sum3;
}
```


## Solution: safe-removal-using-iterator - Safe Removal Using Iterator

```java
public List<Integer> removeEvens(List<Integer> list) {
    Iterator<Integer> it = list.iterator();
    while (it.hasNext()) {
        if (it.next() % 2 == 0) {
            it.remove();
        }
    }
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
    return new ArrayList<>(list.subList(from, to)); // Defensive copy
}
```


## Solution: merge-two-lists - Merge Two Lists

```java
public List<Integer> mergeLists(List<Integer> list1, List<Integer> list2) {
    List<Integer> merged = new ArrayList<>(list1);
    merged.addAll(list2);
    return merged;
}
```


## Solution: remove-duplicates-while-preserving-order - Remove Duplicates While Preserving Order

```java
public List<Integer> removeDuplicates(List<Integer> list) {
    List<Integer> uniqueList = new ArrayList<>();
    Set<Integer> seen = new HashSet<>();
    for (Integer num : list) {
        if (seen.add(num)) {
            uniqueList.add(num);
        }
    }
    return uniqueList;
}
```


## Solution: rotate-list-right - Rotate List Right

```java
public List<Integer> rotateRight(List<Integer> list, int k) {
    if (list.isEmpty()) return new ArrayList<>();
    k = k % list.size();
    if (k == 0) return new ArrayList<>(list);
    
    int splitPoint = list.size() - k;
    List<Integer> result = new ArrayList<>(list.subList(splitPoint, list.size()));
    result.addAll(list.subList(0, splitPoint));
    return result;
}
```


## Solution: arraylist-vs-linkedlist-experiment - ArrayList vs LinkedList Experiment

```java
public void experiment() {
    List<Integer> arrayList = new ArrayList<>();
    List<Integer> linkedList = new LinkedList<>();

    long arrayAdd = timeAddAtEnd(arrayList);
    long linkedAdd = timeAddAtEnd(linkedList);
    long arrayAccess = timeRandomAccess(arrayList);
    long linkedAccess = timeRandomAccess(linkedList);
    long arrayAddBeginning = timeAddBeginning(arrayList);
    long linkedAddBeginning = timeAddBeginning(linkedList);
    long arrayRemoveBeginning = timeRemoveBeginning(arrayList);
    long linkedRemoveBeginning = timeRemoveBeginning(linkedList);

    System.out.println("ArrayList add end: " + arrayAdd);
    System.out.println("LinkedList add end: " + linkedAdd);
    System.out.println("ArrayList index access: " + arrayAccess);
    System.out.println("LinkedList index access: " + linkedAccess);
    System.out.println("ArrayList add beginning: " + arrayAddBeginning);
    System.out.println("LinkedList add beginning: " + linkedAddBeginning);
    System.out.println("ArrayList remove beginning: " + arrayRemoveBeginning);
    System.out.println("LinkedList remove beginning: " + linkedRemoveBeginning);

    // Expect ArrayList to win index access; beginning operations can favor LinkedList.
}

private long timeAddAtEnd(List<Integer> list) {
    long start = System.nanoTime();
    for (int i = 0; i < 10_000; i++) {
        list.add(i);
    }
    return System.nanoTime() - start;
}

private long timeRandomAccess(List<Integer> list) {
    long start = System.nanoTime();
    for (int i = 0; i < list.size(); i++) {
        list.get(i);
    }
    return System.nanoTime() - start;
}

private long timeAddBeginning(List<Integer> list) {
    long start = System.nanoTime();
    for (int i = 0; i < 1_000; i++) {
        list.add(0, i);
    }
    return System.nanoTime() - start;
}

private long timeRemoveBeginning(List<Integer> list) {
    long start = System.nanoTime();
    while (!list.isEmpty()) {
        list.remove(0);
    }
    return System.nanoTime() - start;
}
```


## Solution: mini-problem-student-names - Mini Problem: Student Names

```java
public List<String> cleanNames(List<String> names) {
    List<String> cleaned = new ArrayList<>();
    for (String name : names) {
        cleaned.add(name.trim());
    }

    Iterator<String> iterator = cleaned.iterator();
    while (iterator.hasNext()) {
        if (iterator.next().isEmpty()) {
            iterator.remove();
        }
    }

    Collections.sort(cleaned);
    return cleaned;
}
```
