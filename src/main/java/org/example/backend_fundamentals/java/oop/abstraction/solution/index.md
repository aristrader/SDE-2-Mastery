---
order: 20
search: false
---

# Solutions

## Solution: leaky-abstraction - Identify and fix a leaky abstraction

```java
// Before: Leaky Abstraction (Forces caller to know the type and cast)
class OldCache {
    public Object get(String key) { return "Data"; }
}
// Caller: String data = (String) cache.get("key");

// After: Clean Abstraction
class CleanCache<T> {
    private T data;
    public CleanCache(T data) { this.data = data; }
    public T get(String key) { return data; }
}
// Caller: String data = cache.get("key"); // No cast!
```

## Solution: program-to-interface - Program to an interface

```java
// Before: Too concrete, tightly coupled to ArrayList
public void processItems(ArrayList<String> items) {
    for (String item : items) System.out.println(item);
}

// After: Abstracted to List. Caller can now swap implementations without breaking this method.
public void processItems(List<String> items) {
    for (String item : items) System.out.println(item);
}
```
