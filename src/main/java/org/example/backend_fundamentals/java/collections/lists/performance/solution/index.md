---
order: 20
search: false
---

# Solutions

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

    System.out.println("ArrayList add end: " + arrayAdd);
    System.out.println("LinkedList add end: " + linkedAdd);
    System.out.println("ArrayList index access: " + arrayAccess);
    System.out.println("LinkedList index access: " + linkedAccess);
    System.out.println("ArrayList add beginning: " + arrayAddBeginning);
    System.out.println("LinkedList add beginning: " + linkedAddBeginning);
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
```

Expect `ArrayList` to dominate random access. Treat this as intuition-building, not a proper benchmark.
