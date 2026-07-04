# ConcurrentHashMap

---

## What it is

`ConcurrentHashMap<K,V>` is a thread-safe hash map designed for high-concurrency reads and writes. It replaces `Collections.synchronizedMap` (which locks the entire map on every operation) with fine-grained locking — or no locking at all for reads.

The internal structure mirrors `HashMap`: a bucket array where each bucket is empty, a linked list of nodes, or (after a threshold) a Red-Black tree. What differs is *how concurrent access is controlled*.

---

## Internal structure — same layout as HashMap

The bucket array is a single `Node<K,V>[] table` — identical layout to `HashMap`. The thread safety isn't in the data structure; it's in how access is coordinated.

Each bucket can be in one of four states:

| State | What it means |
|---|---|
| `null` | No key has hashed to this bucket yet |
| Single `Node` | Exactly one key in this bucket |
| Chain of `Node`s | Multiple keys collided here, stored as linked list |
| `TreeBin` | Chain grew to ≥8 entries (with `table.length` ≥ 64) → promoted to red-black tree |

```
table[]
  [0]  ->  null                           ← empty
  [1]  ->  Node(k1,v1) -> Node(k2,v2)     ← 2 collisions, linked list
  [2]  ->  null                           ← empty
  [3]  ->  TreeBin (Red-Black tree)       ← heavy collisions, treeified
```

Each bucket slot is independent. A thread modifying bucket `[1]` doesn't interfere with a thread modifying bucket `[3]`. This independence is what makes per-bucket locking work.

---

## How writes work — CAS + synchronized

Two write paths, depending on whether the bucket is empty:

| Bucket state | Mechanism |
|---|---|
| Empty | **CAS** (compare-and-swap) — atomically swap the new node in, no lock |
| Non-empty | **`synchronized`** on the bucket's head node — lock just that bucket |

### What CAS is — the counter analogy

CAS = compare-and-swap. A way to safely update a shared variable without using a lock.

Two threads both want to increment a shared counter currently at 5.

**Without protection:** both read 5, both write 6 → one update lost.

**With CAS:** each thread says *"if the counter is still 5, change it to 6"*.

- Thread A goes first: "Is it 5? Yes. Change to 6." ✓ Wins.
- Thread B tries: "Is it 5? No, it's 6." ✗ Loses. Retries with current value 6 → "If it's 6, change to 7." ✓ Wins on retry.

Both updates land. No lock needed. The CPU guarantees the check-and-swap happens as one indivisible step.

```java
AtomicInteger counter = new AtomicInteger(0);
counter.compareAndSet(0, 1);   // CAS: if counter == 0, set to 1
```

Trade-off: with a lock, threads wait. With CAS, threads never wait — but might retry if they lose the race.

### In CHM — CAS for empty buckets

When inserting into an empty bucket:

> "If bucket `[3]` is still `null`, put my new node there."

If another thread also tries to insert into bucket `[3]` at the same moment, only one wins. The loser retries — and now finds the bucket non-empty, so it switches to the `synchronized` path.

### Synchronized on the head node — the elegant trick

For non-empty buckets, you'd think CHM needs a separate `Lock` object per bucket. That would double the memory overhead.

Instead, CHM uses the **bucket's first node as the lock object**:

```java
synchronized (firstNodeOfBucket) {
    // modify the chain
}
```

Every Java object has a built-in monitor lock. The head node IS the lock for that bucket. Zero extra memory for the locking infrastructure.

That's why empty buckets need CAS — there's no head node to lock on.

---

## Why lock at the bin level specifically

Three possible granularities for locking; CHM picks the middle one.

| Granularity | Pros | Cons |
|---|---|---|
| Whole map | Simple | Only one writer at a time — serializes everything (this is `synchronizedMap`) |
| Per bin | Threads on different keys rarely collide | Need a lock per bucket |
| Per node | Maximum parallelism | Lots of locks, lots of memory, no real benefit |

**Why not whole-map?** Even unrelated writes wait for each other. With 100 cores, only one thread writes at a time. Bottleneck.

**Why not per-node?** Each node would need its own lock object. For a map with 1 million entries, that's 1 million extra lock objects. The marginal benefit over per-bin is tiny because chains are typically short with good hashing.

**Why per-bin is the goldilocks zone:**
- Two random keys have a low chance of colliding to the same bucket (~1/N where N is table size). Threads on unrelated keys rarely block each other.
- The work unit (modify a chain) maps cleanly to the lock unit (lock that bucket).
- Locking the bucket head node has **zero extra memory cost** (every object already has a monitor).

---

## Why reads are lock-free

Reads (`map.get(key)`) don't acquire any lock. Two things together make this safe.

### Piece 1 — volatile fields stop stale cached reads

Inside CHM, the important fields are marked `volatile`:

1. The bucket array reference (`table`)
2. Each node's value (`val`)
3. Each node's next pointer (`next`)

**What `volatile` does in plain English:** normally, each CPU core has its own cache. When Thread A writes a value, the write sits in Core 1's cache for a while — Thread B on Core 2 might keep seeing the old value from Core 2's cache.

`volatile` tells the JVM: *"For this field, always go to main memory. No caching tricks."* Writes flush to main memory; reads pull from main memory. No staleness due to caching.

### Piece 2 — writes are designed so readers never see a half-built state

Volatile alone isn't enough. You also need to make sure no reader catches the structure mid-update.

CHM does this by **finishing the new state before publishing it**.

**Example — adding a new node to a chain:**
- Step 1: Create the new Node fully (set its key, value, next pointer)
- Step 2: Only THEN attach it to the chain (one atomic write)

A reader walking the chain either sees the old chain (without the new node) or the full new chain (with it fully built). Never a half-attached node.

**Example — resizing:**
- Step 1: Build the entire new table separately
- Step 2: Only THEN swap `table` to point to the new array

Reader sees either the old array or the new array. Never a half-built one.

### Weakly consistent iteration

A reader walking a bucket can be raced by a writer adding/removing a node. The reader sees either:
- The new state ✓ (reached it after the change)
- The old state ✓ (passed before the change)

Either is fine. CHM documents this as **weakly consistent iteration** — your iteration is consistent with SOME point in time, just not necessarily the current instant. Iterators never throw `ConcurrentModificationException`.

### Why this scales

No lock for reads = no waiting, no contention. 100 threads can call `get()` on the same map at the same time with no slowdown. That's why CHM is the standard choice for read-heavy concurrent caches.

---

## Two kinds of "stale"

A common confusion when discussing lock-free reads.

| Type | Description | Does CHM prevent it? |
|---|---|---|
| **Stale-in-cache** | Reading an old value because of CPU cache staleness | **Yes** — volatile bypasses caches |
| **Stale-in-time** | Reading a value that becomes outdated between read and use | **No** — and no concurrent map can |

**Stale-in-time example:**
```
Time 1: Reader calls get("Alice") → walks bucket, finds value 100
Time 2: Writer puts "Alice" → 200
Time 3: Reader returns 100  ← outdated by the time of return
```

This is unavoidable. Locks don't prevent it either — the moment a read returns, the value is a snapshot of some past instant. By the time you ACT on it, the world may have moved.

**The right mental model:** `get(k)` gives you a value that was correct at SOME point during your call. Not "the current value right now" (which is undefined across threads).

- ✓ Fine for caches, lookups, displaying state.
- ✗ NOT fine for read-modify-write patterns — use `compute` / `merge` instead.

---

## Atomicity guarantees

Not every operation or combination of operations is atomic.

| Operation | Atomic? | Notes |
|---|---|---|
| `put(k, v)` | Yes | Single bin-level lock or CAS |
| `get(k)` | Yes | Lock-free volatile read |
| `get(k)` then `put(k, v)` | **No** | Two separate calls — another thread can mutate between them |
| `putIfAbsent(k, v)` | Yes | Check + insert happen under the same bin lock |
| `compute(k, fn)` | Yes | `fn` runs inside the bin lock — read + compute + write is one atomic unit |
| `merge(k, v, fn)` | Yes | Same — fn runs under the bin lock |

The classic race condition:

```java
// WRONG — not atomic
if (!map.containsKey(key)) {
    map.put(key, value);   // another thread can insert between these two calls
}

// RIGHT
map.putIfAbsent(key, value);

// WRONG — not atomic
int count = map.get(key);
map.put(key, count + 1);   // another thread can also read 'count' before this put

// RIGHT
map.compute(key, (k, v) -> v == null ? 1 : v + 1);
```

Rule: if your logic is "read the current value, decide something, write back" — that entire sequence must live inside `compute` or `merge`, not across two separate `get`/`put` calls.

The remapping function passed to `compute` / `merge` / `computeIfAbsent` runs inside the bin lock, but under high contention the JDK may retry it. The function must therefore be **side-effect-free** — no I/O, no external state mutation. A function that sends an email or increments an external counter on every invocation will misbehave under contention.

---

## `size()` is approximate

`HashMap`'s single `int size` field doesn't work under concurrent writes without a global lock.

`ConcurrentHashMap` uses a `LongAdder`-style approach:

- A `baseCount` field (updated via CAS under low contention).
- A `CounterCell[]` array — under high contention, threads update their own cell rather than fighting over `baseCount`.
- `size()` sums `baseCount` plus all `CounterCell` values.

Because `size()` reads cells that concurrent writers are updating, the result is eventually consistent — not the precise count at the instant you called it. If you need an exact count, you need external synchronization.

---

## Why null keys and values are banned

`HashMap` allows one null key and any number of null values. `ConcurrentHashMap` throws `NullPointerException` for both.

### The ambiguity problem

When `map.get(key)` returns `null`, two completely different things could be true:

1. The key isn't in the map at all
2. The key IS in the map, but mapped to `null`

### How HashMap lets you disambiguate

```java
Object value = map.get("Alice");
if (value == null) {
    if (map.containsKey("Alice")) {
        // Case 2: key exists, value is null
    } else {
        // Case 1: key doesn't exist
    }
}
```

This works because nothing else is touching the map between the two calls.

### Why it doesn't work concurrently

```
Thread A                              Thread B
--------                              --------
value = map.get("Alice")  → null
                                      map.remove("Alice")
exists = map.containsKey("Alice")  → false

A concludes "key doesn't exist" — wrong. At the time of the get, the key
existed (mapped to null). A just got bad luck on race timing.
```

Between A's two calls, B changed reality. A has no way to know which "now" each answer refers to.

### Java's design choice

Rather than letting users write subtly broken code, the designers chose: **ban null entirely**. If null can never be stored, then `get(key) == null` has only one meaning: the key isn't there. No ambiguity, no disambiguation needed.

### What to do if you hit this

Most often surfaces during HashMap → CHM migration:

```java
String value = computeSomething();   // might return null
map.put(key, value);                 // NPE if value is null
```

Fix options:
1. **Don't insert** if value is null (most common — you probably don't need the entry)
2. **Use a sentinel** value to mean "absent" (empty string, an enum constant, etc.)
3. **Use `Optional<V>` as the value type** — `ConcurrentHashMap<String, Optional<String>>`

---

## ConcurrentHashMap vs Collections.synchronizedMap

| | ConcurrentHashMap | synchronizedMap |
|---|---|---|
| Lock granularity | Per-bin | Entire map — one lock for everything |
| Read concurrency | Lock-free | Blocks all reads during any write |
| Write concurrency | Many threads, different bins | One thread at a time |
| Iteration | Weakly consistent — never throws `ConcurrentModificationException` | Must hold the map lock for the entire iteration or risk `ConcurrentModificationException` |
| Null keys/values | Not allowed | Allowed (depends on backing map) |
| Typical use | Almost every concurrent use case | Legacy code, or when you need null key/value support and can live with coarse locking |

`Collections.synchronizedMap` is a thin wrapper — every method acquires `this` as a monitor. Under any concurrent load it bottlenecks. Prefer `ConcurrentHashMap` unless you have a specific reason not to.

---

## Quick recall

**Q. How does ConcurrentHashMap manage concurrent access?**
A. A single flat array (same layout as HashMap). Empty bins use CAS to insert. Non-empty bins use `synchronized` on the first node. Lock granularity is per-bin, so contention between unrelated keys is rare.

**Q. What is CAS in one line?**
A. Compare-And-Swap — atomically: "if memory == expected, set to new; return whether you won." Lets you update shared state without a lock. CHM uses it for empty-bucket inserts.

**Q. Why use the bucket head node as the lock?**
A. Every Java object has a built-in monitor. Using the head node = zero extra memory overhead. That's why empty buckets need CAS — there's no head node to lock on.

**Q. Why don't reads need a lock?**
A. `table`, `Node.val`, and `Node.next` are all `volatile` (no stale cache), and writes finish building new state before publishing it (no half-built reads). Together, readers can safely look without locking.

**Q. What's the difference between "stale-in-cache" and "stale-in-time"?**
A. Stale-in-cache = reading an old value because of CPU cache staleness; volatile prevents this. Stale-in-time = the value becomes outdated between when you read and when you use it; unavoidable for any concurrent read.

**Q. Why is `get()` + `put()` not atomic, and what should you use instead?**
A. Another thread can mutate the bin between the two calls. Use `compute()`, `merge()`, or `putIfAbsent()` — they run the entire check-and-update under the bin lock.

**Q. What does `compute()` guarantee that `get()` + `put()` doesn't?**
A. The remapping function runs atomically inside the bin lock — no other thread can observe or modify that key between the read and the write.

**Q. Why must the function passed to `compute()` / `merge()` be side-effect-free?**
A. Under contention the JDK may retry the remapping function. If it has side effects (I/O, external counter), those execute multiple times unexpectedly.

**Q. Why are null keys and null values banned?**
A. A null return from `get()` could mean "absent" or "mapped to null". In a concurrent context the two-step disambiguation (`get` then `containsKey`) is racy — another thread can change the map between the calls. Banning null removes the ambiguity at the source.

**Q. Why lock at the bin level instead of whole-map or per-node?**
A. Whole-map (synchronizedMap) serializes all writes — too coarse. Per-node would need a lock per entry — too much memory, no real benefit. Per-bin matches the unit of work and uses the head node as the lock (zero extra memory).

**Q. When should you prefer synchronizedMap over ConcurrentHashMap?**
A. Almost never. One case: you need null key/value support and coarse locking is acceptable. Otherwise ConcurrentHashMap is strictly better.

**Q. Is `size()` exact?**
A. No — it sums a `baseCount` plus `CounterCell` values that concurrent writers are updating; the result is eventually consistent, not a precise snapshot.
