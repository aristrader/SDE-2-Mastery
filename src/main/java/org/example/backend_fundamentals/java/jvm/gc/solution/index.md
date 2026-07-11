---
order: 20
search: false
---

# Garbage Collection Solutions

## Solution: reachability-and-leak - Reachability and Memory Leak

```java
static final List<byte[]> cache = new ArrayList<>();

static void temporaryObjects() {
    byte[] data = new byte[1024];
}

static void leakingObjects() {
    cache.add(new byte[1024]);
}
```

`data` becomes unreachable after `temporaryObjects()` returns, so it is eligible for GC. Objects added to `cache` remain reachable through the static field, so GC must keep them even if the application no longer needs them.
