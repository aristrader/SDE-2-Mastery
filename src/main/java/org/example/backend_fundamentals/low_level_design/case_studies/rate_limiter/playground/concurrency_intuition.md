# Concurrency Intuition for an In-Memory Rate Limiter

This note builds a repeatable review method from shared mutable state to a correct critical section. Do not start by memorising a concurrency recipe. First identify what can interleave, show the broken outcome, then choose the smallest mechanism that prevents it.

The active `RateLimiter.java` playground already uses ConcurrentHashMap, computeIfAbsent, and a per-bucket monitor. This note explains why those choices fit. It does not alter that user-owned implementation.

## Scope and invariant

A lazy-refill token bucket keeps one mutable Bucket for each RateLimitKey. On a request, it calculates tokens earned since the previous refill, caps the result, then permits the request only when one token remains.

The invariant is:

> For one logical client key, one request may consume at most one token from one coherent, current bucket state.

## The reusable review flow

When several threads can call an operation:

1. List every state value the operation touches.
2. Mark what is shared, then what is mutable.
3. Find compound operations: check-then-act, read-modify-write, or multi-field transitions.
4. Write a two-thread interleaving and state the broken business outcome.
5. Define the sequence that must appear indivisible: the critical section.
6. Choose the smallest stable object every conflicting operation definitely shares.
7. Audit containers separately from the values stored inside them.
8. Check creation, replacement, expiry, removal, and retry paths too.

Shared state is not automatically dangerous. The warning sign is shared, mutable state used in a compound operation.

## 1. Find shared mutable state

| State | Shared? | Mutable? | Concurrency concern |
| --- | --- | --- | --- |
| maxTokens | Yes | No, final configuration | Normally none |
| refillTokensPerSecond | Yes | No, final configuration | Normally none |
| Map contents | Yes | Yes | Keys can be created, replaced, and removed |
| Bucket availableTokens | Yes | Yes | Permit decisions change it |
| Bucket lastRefillNanos | Yes | Yes | It must agree with the balance |
| Refill local variables | No | Locally yes | Each invocation owns them |

A final map means its reference cannot be rebound. It does not make its entries immutable: put, remove, and value mutation remain possible.

The token balance and the last-refill time represent one fact: how many tokens exist as of what time. Do not let another thread observe or modify that fact halfway through its transition.

## 2. Race one: get, test, create, put

A naïve get-or-create flow is a check-then-act race:

```java
Bucket bucket = bucketsByKey.get(key);
if (bucket == null) {
  bucket = new Bucket(maxTokens, System.nanoTime());
  bucketsByKey.put(key, bucket);
  return true;
}
```

With burst capacity one:

```text
Thread A                         Thread B
get(key) -> null                 get(key) -> null
create Bucket A                  create Bucket B
put(key, Bucket A)               put(key, Bucket B)
return true                      return true
```

Both requests were admitted from one permit, and the final put overwrote the other bucket reference. The reusable pattern is: observe shared state, then act because of the observation. The complete sequence must be atomic.

## 3. Race two: refill is a multi-field transition

Refill is not one action. It reads the previous timestamp and balance, calculates earned allowance, writes a capped balance, then records a new timestamp.

```java
long now = System.nanoTime();
long elapsedNanos = now - bucket.getLastRefillNanos();
double earned = elapsedNanos / 1_000_000_000.0 * refillTokensPerSecond;

bucket.setAvailableTokens(Math.min(maxTokens,
    bucket.getAvailableTokens() + earned));
bucket.setLastRefillNanos(now);
```

If two threads both read an old timestamp and balance, they can calculate against the same interval and overwrite each other’s partial work. The deeper failure is that balance and timestamp no longer describe the same bucket instant.

## 4. Race three: check and consume

```java
if (bucket.getAvailableTokens() >= 1) {
  bucket.setAvailableTokens(bucket.getAvailableTokens() - 1);
  return true;
}
```

With one token, two threads can both read one, both pass, both write zero, and both return true. With two tokens, both can calculate one and write one: two requests were served but only one decrement remains. This is the classic lost update.

A read-modify-write expression such as x = x - 1 is several steps, not one atomic business decision.

## 5. Derive the critical section from the business operation

The protected operation is not merely the assignment to availableTokens:

```text
BEGIN one-bucket critical section
  refill from previous timestamp
  cap at capacity
  check for one token
  consume exactly one token when allowed
  record the new timestamp
END critical section
```

A competing request for the same key must see the bucket before or after this operation, never interfere during it.

## 6. Lock choice: correctness, then granularity

A lock is useful only when every conflicting operation definitely acquires the same monitor. Independent keys should not block each other unnecessarily.

| Candidate lock | Do conflicting requests share it? | Do unrelated keys block? | Result |
| --- | --- | --- | --- |
| this RateLimiter instance | Yes | Yes | Correct but broad |
| Caller-supplied RateLimitKey | Not guaranteed | Usually no | Incorrect identity |
| Stored Bucket | Yes, after atomic creation | No | Correct per-key lock |

Java monitors use object identity, not equals. Two separately created but equal key objects have different monitors:

```java
RateLimitKey first = new RateLimitKey(123, "/orders");
RateLimitKey second = new RateLimitKey(123, "/orders");

first.equals(second); // true
first == second;      // false
```

After atomic creation, every request for one logical key retrieves the same stored Bucket. That makes the bucket a stable per-key lock.

```java
synchronized (bucket) {
  refill(bucket);

  if (bucket.getAvailableTokens() < 1) {
    return false;
  }

  bucket.setAvailableTokens(bucket.getAvailableTokens() - 1);
  return true;
}
```

Changing fields on the bucket never changes its monitor identity.

## 7. Important synchronized semantics

An instance method marked synchronized locks this, not the passed bucket:

```java
public synchronized void refill(Bucket bucket) { ... }

// Equivalent monitor choice:
public void refill(Bucket bucket) {
  synchronized (this) { ... }
}
```

That serializes all client keys through one limiter. If refill is called only from code that already holds the bucket monitor, it needs no second synchronization. Calling a helper does not release a monitor already held by the same thread.

## 8. The map and bucket are separate problems

A per-bucket monitor protects Bucket fields. It does not make the map safe while unrelated keys are accessed concurrently.

```java
private final Map<RateLimitKey, Bucket> bucketsByKey =
    new ConcurrentHashMap<>();
```

ConcurrentHashMap protects its own structure and individual concurrent map operations. Synchronizing on a stored bucket protects the state inside that value. Neither layer replaces the other.

A concurrent map does not make an arbitrary call sequence atomic. Use its atomic per-key operation:

```java
Bucket bucket = bucketsByKey.computeIfAbsent(
    rateLimitKey,
    key -> new Bucket(maxTokens, System.nanoTime())
);
```

This converges competing callers on one stored non-null bucket. Keep the mapping function short; it should not do I/O, block, or recursively change the same map.

## 9. Current design boundary and intended shape

Lazy refill needs no thread per client. It performs no idle work and calculates allowance when the next request arrives.

```java
private final ConcurrentHashMap<RateLimitKey, Bucket> bucketsByKey =
    new ConcurrentHashMap<>();

public boolean tryConsume(RateLimitKey key) {
  if (key == null) {
    throw new IllegalArgumentException("rateLimitKey cannot be null");
  }

  Bucket bucket = bucketsByKey.computeIfAbsent(
      key, ignored -> new Bucket(maxTokens, System.nanoTime()));

  synchronized (bucket) {
    refill(bucket);
    if (bucket.getAvailableTokens() < 1) {
      return false;
    }
    bucket.setAvailableTokens(bucket.getAvailableTokens() - 1);
    return true;
  }
}
```

This is an illustrative target, not a replacement for the playground.

**Current-playground learning point:** its concurrent map, atomic creation, and bucket monitor are now present. Its creation mapping starts at full capacity, and the new bucket continues through the same refill-and-consume path as every later request. That is the simpler model: initialise full, then consume exactly one token through one shared path. The alternative model—initialise the post-consumption state—must return immediately instead of consuming again.

## 10. Match each problem to the mechanism

| Problem | Why it fails | Matching mechanism |
| --- | --- | --- |
| Concurrent map access | Ordinary HashMap does not support concurrent structural updates | ConcurrentHashMap |
| Absent, create, store | Individually safe calls still leave a gap | computeIfAbsent |
| One bucket transition | Refill, check, and consume can interleave | synchronize on stored bucket |
| Lock only around decrement | The decision already used stale state | Protect refill through timestamp update |
| Future expiry or removal | One key can acquire two bucket objects | Make lifecycle changes follow the same coordination rule |

## 11. Transfer this reasoning

- **Inventory or vending machine:** checking stock then decrementing is check-then-act plus read-modify-write. Protect the item or slot state.
- **Bank balance:** read, validate funds, and debit are one business operation. A database transaction or conditional update may own that boundary instead.
- **Seat reservation:** check free then reserve must have one winner.
- **Cache initialization:** get, absent, create, put has the same creation race.
- **Splitwise:** read balances, calculate, and write related balances together; lock or transaction scope follows the invariant.

## Common mistakes

- Add synchronized everywhere before finding the conflicting state.
- Lock a caller/request object whose identity differs across equivalent requests.
- Treat a final collection as immutable.
- Treat map values as thread-safe because the map is concurrent.
- Treat two atomic map calls as an atomic sequence.
- Lock decrement but leave validation outside the lock.
- Use one global lock when independent keys can proceed separately.
- Forget expiry/removal can break the one-key/one-bucket assumption.
- Start one infinite refill thread per key instead of lazy refill or one deliberate shared scheduler.

## Interview delivery

“First I identify shared mutable state: map structure and each bucket’s balance/timestamp pair. I trace a get-then-put race and a refill/check/decrement race. I use ConcurrentHashMap computeIfAbsent to establish one bucket per key, then lock that stored bucket while I refill, cap, check, consume, and advance time. The map protects its structure; the bucket monitor protects value state. This is per-process only—multiple service instances need shared limiter state or a distributed rate-limit design.”

## Quick recall

**Q. What is the first thing to inspect in concurrent code?**
A. Shared mutable state and compound operations on it, not lock keywords.

**Q. Why is a final map still mutable?**
A. Final prevents rebinding the reference, not mutations of entries or values.

**Q. Why not lock an equal key?**
A. Synchronization uses object identity; equal caller-created keys can have different monitors.

**Q. What does ConcurrentHashMap protect?**
A. Its own structure and atomic map operations, not fields inside each stored bucket.

**Q. What belongs in the bucket critical section?**
A. Refill, cap, availability check, consume, and timestamp update.

**Q. What does computeIfAbsent solve?**
A. Atomic per-key creation; it does not protect later bucket mutation.
