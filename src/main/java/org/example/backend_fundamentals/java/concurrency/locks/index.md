---
order: 40
---

# Locks

---

## synchronized recap — what ReentrantLock is solving

`synchronized` is the JVM's built-in lock: mutual exclusion, automatic release on exception, JIT-friendly. It works well for most cases. But it has hard limits:

- Can't attempt a lock without blocking forever
- Can't be interrupted while waiting
- Single implicit condition variable per object (`wait`/`notify`)
- Always unfair (no FIFO ordering)

`ReentrantLock` removes each of these constraints — at the cost of requiring explicit `unlock()`.

---

## ReentrantLock

### Basic pattern — unlock MUST be in finally

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // critical section
} finally {
    lock.unlock();  // guaranteed release even if exception is thrown
}
```

`synchronized` releases automatically on any exit. `ReentrantLock` does not — forgetting `finally` causes a permanent deadlock.

### tryLock — non-blocking and timed acquisition

```java
if (lock.tryLock()) {           // returns immediately: true if acquired, false if not
    try { ... } finally { lock.unlock(); }
} else {
    // do something else — no blocking
}

if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {   // wait at most 500ms
    try { ... } finally { lock.unlock(); }
} else {
    // timed out
}
```

No equivalent in `synchronized`. Enables deadlock avoidance by backing off when a lock isn't available.

### Interruptible locking

```java
lock.lockInterruptibly();   // throws InterruptedException if thread is interrupted while waiting
```

With `synchronized`, a thread waiting to enter a monitor cannot be interrupted — it blocks until it acquires or the JVM shuts down. `lockInterruptibly()` allows the waiting thread to respond to cancellation.

### Fairness

```java
ReentrantLock fairLock = new ReentrantLock(true);   // fair
ReentrantLock unfairLock = new ReentrantLock();     // unfair (default)
```

- **Unfair (default):** a thread that just released can immediately re-acquire. Higher throughput because it avoids context switches. May starve long-waiting threads.
- **Fair:** waiting threads are served in FIFO order. Prevents starvation. Lower throughput — every acquisition requires a context switch to wake the next waiter.

Unfair is the right default. Use fair only when starvation is a documented operational problem.

### Multiple condition variables

`synchronized` gives you one condition per object (`wait`/`notify`). `ReentrantLock` gives you as many as you need:

```java
ReentrantLock lock = new ReentrantLock();
Condition notFull  = lock.newCondition();
Condition notEmpty = lock.newCondition();

// Producer
lock.lock();
try {
    while (buffer.isFull()) notFull.await();   // wait on "not full" condition
    buffer.add(item);
    notEmpty.signal();                          // signal "not empty" condition only
} finally { lock.unlock(); }

// Consumer
lock.lock();
try {
    while (buffer.isEmpty()) notEmpty.await();
    buffer.remove();
    notFull.signal();
} finally { lock.unlock(); }
```

With `synchronized` you'd call `notifyAll()` and wake every waiter regardless. Separate conditions allow targeted signaling.

---

## ReentrantReadWriteLock

Maintains two separate lock views over a single lock state: `readLock()` and `writeLock()`.

- **Read lock:** multiple threads may hold it simultaneously — as long as no thread holds the write lock.
- **Write lock:** exclusive. No other reader or writer may hold any lock.

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock  = rwLock.readLock();
Lock writeLock = rwLock.writeLock();

// Any number of readers in parallel
readLock.lock();
try { return cache.get(key); } finally { readLock.unlock(); }

// Exclusive write
writeLock.lock();
try { cache.put(key, value); } finally { writeLock.unlock(); }
```

### When to use

Read-heavy, write-rare workloads: in-memory caches, lookup tables, configuration snapshots. If writes are frequent, the overhead of tracking the read count outweighs the benefit.

### Lock downgrade — supported; upgrade — not

```java
writeLock.lock();
try {
    update();
    readLock.lock();    // acquire read lock WHILE holding write lock
} finally {
    writeLock.unlock(); // release write lock — now holding only read lock (downgraded)
}
// ... continue reading under readLock, then release it
readLock.unlock();
```

Upgrade (read → write) is not supported and will deadlock: two threads each holding a read lock and each waiting for the other to release before acquiring the write lock.

### Writer starvation under heavy reads

Even with `ReentrantReadWriteLock(true)` (fair mode), a continuous stream of readers can delay writers. `StampedLock` with optimistic reads is the better tool for extreme read-heavy scenarios.

---

## StampedLock (mention — full coverage in row 13)

`StampedLock` adds an **optimistic read** mode on top of the read/write lock model:

1. Call `tryOptimisticRead()` — returns a stamp without acquiring any lock.
2. Read the data.
3. Call `validate(stamp)` — returns `true` if no write happened since step 1.
4. If `validate` returns `false`, fall back to a full `readLock()`.

Near-zero overhead reads when writes are rare. Key difference from `ReentrantReadWriteLock`: `StampedLock` is **non-reentrant** — a thread that holds a stamp-based lock and tries to acquire the same lock again will deadlock.

---

## When to prefer ReentrantLock over synchronized

| Need | Use |
|---|---|
| `tryLock()` or timed/interruptible locking | `ReentrantLock` |
| Multiple condition variables | `ReentrantLock` |
| Fair scheduling (FIFO) | `ReentrantLock(true)` |
| Simple mutual exclusion, no special requirements | `synchronized` |

Prefer `synchronized` by default — simpler, JIT-optimized aggressively (lock elision, biased locking in older JVMs), and the performance difference is negligible in most workloads. Reach for `ReentrantLock` only when you need a capability `synchronized` can't provide.

---

## Quick recall

**Q. What happens if you forget `finally { lock.unlock(); }` with `ReentrantLock`?**
A. Any exception in the critical section leaves the lock permanently held — all waiting threads deadlock indefinitely.

**Q. `tryLock()` vs `lock()` — key difference?**
A. `tryLock()` returns immediately (`true`/`false`); `lock()` blocks until acquired. `synchronized` has no `tryLock` equivalent.

**Q. Fair vs unfair lock — trade-off?**
A. Fair prevents starvation (FIFO order) but lowers throughput due to forced context switches. Unfair allows barging — higher throughput, possible starvation.

**Q. `ReentrantReadWriteLock` — what's the upgrade vs downgrade rule?**
A. Downgrade (write → read) is supported: acquire readLock while holding writeLock, then release writeLock. Upgrade (read → write) deadlocks — never attempt it.

**Q. When does `ReentrantReadWriteLock` stop helping?**
A. When writes are frequent — the overhead of tracking the reader count exceeds the parallelism benefit. Also, heavy read load can starve writers even in fair mode.

**Q. `StampedLock` vs `ReentrantReadWriteLock` — what's the key trade-off?**
A. `StampedLock` optimistic reads have lower overhead, but it's non-reentrant and harder to use correctly; `ReentrantReadWriteLock` is reentrant and simpler.


<ExerciseNav />
