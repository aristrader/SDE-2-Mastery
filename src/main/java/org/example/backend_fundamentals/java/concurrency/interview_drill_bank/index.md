---
order: 130
---

# Concurrency Interview Drill Bank

Use this after the topic exercises. These are compact oral drills for repetition, not replacement for coding practice.

## Shared State Basics

**Q. Why is `count++` not thread-safe?**
A. It is read, modify, write. Two threads can read the same old value and lose one update.

**Q. Does `count = count + 1` fix it?**
A. No. It is the same read-modify-write pattern.

**Q. Can a race produce the correct output sometimes?**
A. Yes. Lucky scheduling does not prove thread safety.

**Q. What is a critical section?**
A. The whole code region whose shared-state invariant must be protected, not just one line.

**Q. When is synchronization unnecessary?**
A. No shared mutable state, immutable shared state, or thread-confined state.

## Locks And Monitors

**Q. What does an instance synchronized method lock?**
A. `this`.

**Q. What does a static synchronized method lock?**
A. The class object, such as `Counter.class`.

**Q. Do two different instances block each other on synchronized instance methods?**
A. No, unless they coordinate through another shared lock or shared state.

**Q. Why is `synchronized(new Object())` broken?**
A. Every call locks a fresh object, so threads do not exclude each other.

**Q. Why avoid locking on string literals or boxed values?**
A. They may be shared or cached by unrelated code.

**Q. Why should a dedicated lock usually be `private final`?**
A. Private prevents external interference; final prevents lock replacement.

**Q. Why avoid holding locks during slow I/O?**
A. It increases contention, latency, and deadlock risk.

## volatile, Atomic, synchronized

**Q. What does `volatile` guarantee?**
A. Visibility and ordering for reads/writes of that variable, not compound atomicity.

**Q. Best fit for a shutdown flag?**
A. `volatile boolean`.

**Q. Best fit for one request counter?**
A. `AtomicInteger`, or `LongAdder` under high contention.

**Q. Best fit for a money transfer across two balances?**
A. `synchronized` or higher-level transactional locking.

**Q. Why are two atomics not enough for a combined snapshot?**
A. The two reads or resets happen at different moments unless coordinated.

**Q. What does CAS mean?**
A. Update only if the current value still equals the expected value.

**Q. Why can a CAS loop spin?**
A. Another thread changed the value first, so the operation retries with the latest value.

## Thread Lifecycle

**Q. `run()` vs `start()`?**
A. `run()` is a normal method call; `start()` creates a new execution path.

**Q. Can a `Thread` be started twice?**
A. No. The second `start()` throws `IllegalThreadStateException`.

**Q. What does `join()` do?**
A. The caller waits for the target thread to terminate.

**Q. Is `sleep()` synchronization?**
A. No. It delays a thread but does not create a correctness guarantee.

**Q. What should you do after catching `InterruptedException` if you cannot throw it?**
A. Restore interrupt status with `Thread.currentThread().interrupt()`.

## ExecutorService And Future

**Q. Why use an executor instead of `new Thread()` per task?**
A. Thread reuse, bounded concurrency, queueing, lifecycle, and result tracking.

**Q. What is dangerous about `newFixedThreadPool(n)`?**
A. It uses an unbounded queue, so memory can grow under sustained overload.

**Q. What is dangerous about `newCachedThreadPool()`?**
A. It can create a very large number of threads under burst load.

**Q. `execute()` vs `submit()`?**
A. `execute()` returns void; `submit()` returns a `Future` and captures failures in it.

**Q. What happens if a submitted failing future is ignored?**
A. The exception can be operationally invisible.

**Q. What does `Future.get()` do?**
A. Blocks until completion and returns result or throws `ExecutionException`.

**Q. Why submit independent tasks before waiting?**
A. It allows overlap; immediate `get()` serializes work.

**Q. What does `CallerRunsPolicy` do?**
A. The submitting thread runs rejected work, slowing producers and creating backpressure.

## CompletableFuture

**Q. `runAsync()` vs `supplyAsync()`?**
A. No result vs result.

**Q. `thenApply()` vs `thenCompose()`?**
A. Plain value transform vs async-returning dependent step.

**Q. What bug creates `CompletableFuture<CompletableFuture<T>>`?**
A. Using `thenApply()` with a function that returns a future.

**Q. `thenCompose()` vs `thenCombine()`?**
A. Dependency vs independence.

**Q. What does `allOf()` return?**
A. `CompletableFuture<Void>`.

**Q. How do you read values after `allOf()`?**
A. Join original futures after `allOf()` completes.

**Q. Does `join()` block?**
A. Yes. CompletableFuture is composable, not magic non-blocking retrieval.

**Q. `get()` vs `join()` failure wrappers?**
A. `ExecutionException` vs `CompletionException`.

**Q. Why avoid the common pool for blocking calls?**
A. Blocking work can starve unrelated common-pool users.

## Failure, Timeout, Retry

**Q. `exceptionally()` vs `handle()`?**
A. Failure-only fallback vs success-or-failure transformation.

**Q. `whenComplete()` best use?**
A. Logging, metrics, tracing, and observability without intentionally changing the result.

**Q. Where attach fallback for optional recommendations?**
A. On the recommendation future itself.

**Q. Why is one final fallback dangerous?**
A. It may hide required dependency failures.

**Q. `orTimeout()` vs `completeOnTimeout()`?**
A. Timeout failure vs timeout fallback value.

**Q. Does timeout stop the underlying remote call?**
A. Not necessarily. It changes the future's visible result.

**Q. What must exist before retrying payment?**
A. Idempotency or equivalent duplicate-side-effect protection.

## Concurrent Collections

**Q. Why not share a mutable `HashMap` across threads?**
A. Concurrent mutation can lose updates or corrupt state.

**Q. Why can `synchronizedMap` scale poorly?**
A. It serializes access through one map-level lock.

**Q. Why does `ConcurrentHashMap` reject null values?**
A. `get(key) == null` must mean no mapping exists.

**Q. `containsKey()` then `put()` on `ConcurrentHashMap` - atomic?**
A. No. Use `putIfAbsent()` or `computeIfAbsent()`.

**Q. When use `merge()`?**
A. Atomic per-key accumulation, such as endpoint counters.

**Q. Does a concurrent map make mutable values thread-safe?**
A. No. The stored values need their own safety.

## Progress Failures

**Q. What is deadlock?**
A. Threads wait forever in a circular dependency.

**Q. Best classic deadlock prevention?**
A. Consistent lock ordering.

**Q. Deadlock vs livelock?**
A. Deadlock waits; livelock keeps running but makes no progress.

**Q. Starvation?**
A. One task never gets a needed resource while other work continues.

**Q. Thread-pool starvation deadlock?**
A. Pool workers block waiting for child tasks queued to the same pool, with no worker free to run them.

## Quick recall

**Q. First decision in a concurrency question?**
A. Is this shared-state safety, async execution, or progress/resource management?

**Q. Smallest safe shared-state decision table?**
A. Visibility flag -> `volatile`; one atomic value -> atomic class; multi-step invariant -> lock/transaction.

**Q. First decision in a CompletableFuture workflow?**
A. Which calls are dependent, which are independent, and which failures are optional.

**Q. Most common senior-level async bug?**
A. Blocking too early or hiding required failures with broad fallback.
