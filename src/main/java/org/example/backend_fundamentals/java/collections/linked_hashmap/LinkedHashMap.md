# LinkedHashMap

---

## What LinkedHashMap adds to HashMap

`LinkedHashMap<K, V>` extends `HashMap<K, V>` — same bucket array, same hash function, same O(1) average get/put. The only structural addition is a **doubly-linked list threaded through every entry**.

Each entry has two extra pointers:

```
HashMap bucket array
  [0] -> null
  [1] -> Entry{key=A, val=1, before=null, after=Entry{B}} <-> Entry{key=B, val=2, before=Entry{A}, after=Entry{C}} <-> ...
  [2] -> ...

Linked list: head -> A <-> B <-> C <-> ... <-> Z <- tail
```

- **`head`** — eldest entry (inserted or accessed least recently)
- **`tail`** — newest entry (inserted or accessed most recently)

Iteration follows the linked list, not the bucket array — predictable, stable order, unlike plain `HashMap`, which reorders entries freely during resizing.

---

## Insertion order vs access order

`LinkedHashMap` has two modes, selected at construction time.

| Mode | Controlled by | Behaviour |
|---|---|---|
| Insertion order | `accessOrder=false` (default) | Entries iterate in the order keys were **first inserted**. Reinserting an existing key does not move it. |
| Access order | `accessOrder=true` | Every `get()` or `put()` of an **existing** key moves that entry to the **tail** of the list (most recently used). |

Constructor for access order:

```java
new LinkedHashMap<>(initialCapacity, loadFactor, accessOrder)
// e.g.
new LinkedHashMap<>(16, 0.75f, true)   // access-order mode
new LinkedHashMap<>(16, 0.75f, false)  // insertion-order mode (default)
```

In access-order mode, the list orders entries from least recently used (head) to most recently used (tail) — exactly what an LRU eviction policy needs.

---

## `removeEldestEntry()` hook

After every `put()` (including a put that updates an existing key), `LinkedHashMap` calls:

```java
protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
```

`eldest` is the **head** of the linked list — the entry in the map the longest (insertion order) or accessed least recently (access order).

- Default implementation: always returns `false`. Nothing is ever evicted.
- Override to return `true` when eviction should happen.

When it returns `true`, the map removes `eldest` automatically before returning from `put()`. No manual bookkeeping needed.

---

## LRU cache pattern

The standard bounded LRU cache using `LinkedHashMap`:

```java
int capacity = 100;

Map<Integer, String> lruCache = new LinkedHashMap<>(capacity, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > capacity;
    }
};
```

How it works:

- `accessOrder=true` — every `get()` or `put()` moves the touched entry to the tail.
- `removeEldestEntry` fires after every `put()`. When size exceeds capacity, the head (least recently used) is evicted.
- `get()` and `put()` remain O(1) average. Eviction is O(1) — removing the head of a doubly-linked list is pointer surgery.

No external data structures, no manual LRU tracking.

---

## Thread safety

`LinkedHashMap` is **not thread-safe** — same guarantee as `HashMap`. Concurrent reads are fine; any concurrent modification is not.

For a thread-safe LRU cache, wrap it:

```java
Map<K, V> safe = Collections.synchronizedMap(lruCache);
```

Critical caveat: iteration over a `synchronizedMap`-wrapped map is **not automatically synchronized**. You must manually lock:

```java
synchronized (safe) {
    for (Map.Entry<K, V> e : safe.entrySet()) { ... }
}
```

For high-concurrency scenarios with access-order eviction, use a dedicated library. Caffeine's `Cache` / `LoadingCache` is built on a concurrent linked hash map structure; Guava's older `CacheBuilder` does the same. There is no JDK built-in `ConcurrentLinkedHashMap`; pull in Caffeine (the de-facto standard) rather than rolling your own.

---

## When to use LinkedHashMap

| Use case | Reason |
|---|---|
| Predictable iteration order (insertion order) | Building an ordered response map, deduplicating while preserving sequence |
| Bounded LRU cache | `accessOrder=true` + `removeEldestEntry` override — all in one class |
| Access-order tracking without custom bookkeeping | The linked list does the work; you just read or iterate |

Do not use it when:
- You need high concurrency — use Caffeine or a `ConcurrentHashMap`-backed structure.
- You need a true LRU cache with thread safety — the `synchronizedMap` wrapper serialises all operations, killing throughput under contention.

---

## Quick recall

**Q. What does LinkedHashMap add over HashMap structurally?**
A. A doubly-linked list through every entry (`before`/`after` pointers), with a `head` and `tail`. Iteration follows the list, not the bucket array.

**Q. Insertion order vs access order — what's the difference?**
A. Insertion order (default): position set on first insert, reinsertion doesn't move it. Access order (`accessOrder=true`): every `get()` or `put()` of an existing key moves it to the tail.

**Q. How do you implement a bounded LRU cache with LinkedHashMap?**
A. Use `accessOrder=true` so `get()`/`put()` promotes entries to the tail, then override `removeEldestEntry()` to return `true` when `size() > capacity`. The head (LRU entry) is evicted automatically.

**Q. When is `removeEldestEntry` called and what does it receive?**
A. After every `put()`. It receives the current head of the linked list — the eldest (least recently used) entry. If it returns `true`, that entry is removed.

**Q. Is LinkedHashMap thread-safe?**
A. No. Wrap with `Collections.synchronizedMap()` for basic safety, but manually synchronize any iteration block. For high-concurrency caches, use Caffeine instead.
