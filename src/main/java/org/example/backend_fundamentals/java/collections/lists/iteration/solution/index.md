---
order: 20
search: false
---

# Solutions

## Solution: iterate-three-ways - Iterate Three Ways

```java
public int sumUsingIndexLoop(List<Integer> list) {
    int sum = 0;
    for (int i = 0; i < list.size(); i++) {
        sum += list.get(i);
    }
    return sum;
}

public int sumUsingEnhancedFor(List<Integer> list) {
    int sum = 0;
    for (Integer value : list) {
        sum += value;
    }
    return sum;
}

public int sumUsingIterator(List<Integer> list) {
    int sum = 0;
    Iterator<Integer> it = list.iterator();
    while (it.hasNext()) {
        sum += it.next();
    }
    return sum;
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
