---
order: 20
---

# Singleton Pattern in Java

## What is the Singleton Pattern?

The **Singleton pattern** restricts the instantiation of a class and ensures that **only one instance** of the class exists in the Java Virtual Machine.

To implement a singleton pattern, we have different approaches, but all of them share the following common concepts:

- **Private constructor** — to restrict instantiation of the class from other classes.
- **Private static variable** of the same class — this is the only instance of the class.
- **Public static method** that returns the instance of the class — this is the global access point for the outside world to get the instance of the singleton class.

This document covers five files that live together in `org.example.design_patterns.creational.singleton`:

| File | Role |
| --- | --- |
| `NoSingleton.java` | Counter-example — intentionally **not** a singleton. |
| `EagerInitializationSingleton.java` | Instance created at class-load time. |
| `LazyInitializationSingleton.java` | Instance created on first access. **Not thread-safe.** |
| `ThreadSafeSingleton.java` | Two variants: `synchronized` method and double-checked locking. |
| `BillPughSingleton.java` | Initialization-on-demand holder idiom — the recommended approach. |

---

## 1. `NoSingleton` — the counter-example

A plain class with a package-private constructor. Every `new NoSingleton(...)` call produces a **distinct object**. It exists to contrast with the real singletons and to illustrate why the pattern is needed when exactly one shared instance is required.

```java
package org.example.design_patterns.creational.singleton;

public class NoSingleton {

  int x;

  NoSingleton(int value) {
    x = value;
  }

  public static void main(String[] args) {
    NoSingleton obj1 = new NoSingleton(5);
    NoSingleton obj2 = new NoSingleton(5);
    if (obj1.equals(obj2)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
```

**Output:** `DIFFERENT` — two separately constructed objects are distinct instances even when their fields are equal.

---

## 2. `EagerInitializationSingleton` — eager initialization

The singleton instance is created **at class loading time**, when the static field is initialized. This approach is inherently thread-safe because the JVM guarantees that static initialization of a class completes before the class is made available to any thread.

**Best used when** the singleton is lightweight and will be needed throughout the program's execution. If the instance is expensive to create or may never be used, prefer a lazy approach.

```java
package org.example.design_patterns.creational.singleton;

public class EagerInitializationSingleton {

  private static final EagerInitializationSingleton obj = new EagerInitializationSingleton();

  private EagerInitializationSingleton() {}

  public static EagerInitializationSingleton getInstance() {
    return obj;
  }

  public static void main(String[] args) {
    EagerInitializationSingleton obj3 = EagerInitializationSingleton.getInstance();
    EagerInitializationSingleton obj4 = EagerInitializationSingleton.getInstance();
    if (obj3.equals(obj4)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
```

**Pros:** simple, thread-safe out of the box.
**Cons:** instance is created even if it is never used — wasted work if construction is expensive.

---

## 3. `LazyInitializationSingleton` — lazy initialization (not thread-safe)

The singleton instance is not created until it is requested via `getInstance()`. This defers the cost of construction until the instance is actually needed.

**Warning:** this implementation is **not thread-safe**. Two threads calling `getInstance()` concurrently when `obj` is still `null` may each create their own instance.

```java
package org.example.design_patterns.creational.singleton;

public class LazyInitializationSingleton {

  private static LazyInitializationSingleton obj;

  int x;

  private LazyInitializationSingleton() {}

  public static LazyInitializationSingleton getInstance() {
    if (obj == null) {
      obj = new LazyInitializationSingleton();
    }
    return obj;
  }

  public static void main(String[] args) {
    LazyInitializationSingleton obj3 = LazyInitializationSingleton.getInstance();
    LazyInitializationSingleton obj4 = LazyInitializationSingleton.getInstance();
    if (obj3.equals(obj4)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
```

**Pros:** lazy; cheap until used.
**Cons:** broken under concurrency — need one of the approaches below.

---

## 4. `ThreadSafeSingleton` — thread-safe variants

This class demonstrates two approaches to safe lazy initialization under concurrent access.

### 4a. Fully synchronized method — `getInstance()`

Uses a fully `synchronized` method. Simple and correct, but **every call acquires the monitor**, which adds contention even after the instance has been created.

```java
public static synchronized ThreadSafeSingleton getInstance() {
  if (obj == null) {
    obj = new ThreadSafeSingleton();
  }
  return obj;
}
```

Equivalent to wrapping the whole body in `synchronized (ThreadSafeSingleton.class) { ... }` — marking a **static** method `synchronized` automatically locks on the class's `Class` object.

### 4b. Double-checked locking — `getInstanceUsingDoubleLocking()`

Uses the **double-checked locking** idiom to avoid synchronization on the fast path.

- The **first `null` check** avoids acquiring the monitor once the instance has been initialized.
- The **second `null` check** inside the synchronized block guards against two threads both passing the first check before either has created the instance.

```java
public static ThreadSafeSingleton getInstanceUsingDoubleLocking() {
  if (instance == null) {
    synchronized (ThreadSafeSingleton.class) {
      if (instance == null) {
        instance = new ThreadSafeSingleton();
      }
    }
  }
  return instance;
}
```

**Caveat:** for this idiom to be correct under the Java Memory Model, the backing field (`instance`) should be declared `volatile`. Without `volatile`, another thread may observe a non-null but partially constructed instance due to instruction reordering.

```java
private static volatile ThreadSafeSingleton instance;
```

### Understanding `synchronized (ThreadSafeSingleton.class)`

Every object in Java has a built-in lock (its "monitor"). `synchronized (X)` acquires the lock on object `X`, runs the block, then releases it. Two blocks lock each other out only if they name the **same** object.

- `this` is not available in a `static` method.
- `instance` is `null` at the moment you need to lock.
- `ThreadSafeSingleton.class` is the unique `Class` metadata object — there is exactly one per class per classloader. It is always non-null and shared across all threads, making it a natural class-wide lock.

A stylistic alternative is a dedicated private lock object:

```java
private static final Object LOCK = new Object();
// ...
synchronized (LOCK) { ... }
```

This prevents outside code from locking on your class object and potentially causing deadlocks, which is why it is often preferred in production code.

### Multithreaded test

The `main` method races many threads against both variants and verifies that only one unique instance is observed. A `CountDownLatch` is used as a starting gate so every worker is already parked before any call to the supplier happens — this maximises the chance of a race on first-time initialisation. Identity is compared via `System.identityHashCode(Object)` so a broken `equals`/`hashCode` can't hide a bug.

```java
public static void main(String[] args) throws InterruptedException {
  int threadCount = 50;
  runConcurrencyTest("getInstance()", threadCount, ThreadSafeSingleton::getInstance);
  runConcurrencyTest(
      "getInstanceUsingDoubleLocking()",
      threadCount,
      ThreadSafeSingleton::getInstanceUsingDoubleLocking);
}

private static void runConcurrencyTest(
    String label, int threadCount, Supplier<ThreadSafeSingleton> supplier)
    throws InterruptedException {

  ExecutorService executor = Executors.newFixedThreadPool(threadCount);
  CountDownLatch startGate = new CountDownLatch(1);
  CountDownLatch doneGate = new CountDownLatch(threadCount);
  Set<Integer> identityHashes = ConcurrentHashMap.newKeySet();

  for (int i = 0; i < threadCount; i++) {
    executor.submit(() -> {
      try {
        startGate.await();
        ThreadSafeSingleton result = supplier.get();
        identityHashes.add(System.identityHashCode(result));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        doneGate.countDown();
      }
    });
  }

  startGate.countDown();
  doneGate.await();
  executor.shutdown();

  String verdict = identityHashes.size() == 1 ? "PASS" : "FAIL";
  System.out.printf(
      "%-35s -> %d threads saw %d unique instance(s). %s%n",
      label, threadCount, identityHashes.size(), verdict);
}
```

**Sample output:**

```
getInstance()                       -> 50 threads saw 1 unique instance(s). PASS
getInstanceUsingDoubleLocking()     -> 50 threads saw 1 unique instance(s). PASS
```

---

## 5. `BillPughSingleton` — initialization-on-demand holder (recommended)

Prior to Java 5, the Java memory model had several issues that caused earlier singleton approaches (such as double-checked locking) to fail when multiple threads requested the instance simultaneously.

This implementation relies on a private static inner class, `SingletonHelper`, that holds the singleton instance. The helper class is **not loaded into memory** when `BillPughSingleton` is loaded; it is only loaded when `getInstance()` is first invoked, at which point the JVM guarantees thread-safe class initialization and creates the instance.

This is the most widely used approach for singletons because it is **lazy, thread-safe, and requires no explicit synchronization**.

```java
package org.example.design_patterns.creational.singleton;

public class BillPughSingleton {

  private BillPughSingleton() {}

  private static class SingletonHelper {
    private static final BillPughSingleton INSTANCE = new BillPughSingleton();
  }

  public static BillPughSingleton getInstance() {
    return SingletonHelper.INSTANCE;
  }

  public static void main(String[] args) {
    BillPughSingleton obj3 = BillPughSingleton.getInstance();
    BillPughSingleton obj4 = BillPughSingleton.getInstance();
    if (obj3.equals(obj4)) {
      System.out.println("SAME");
    } else {
      System.out.println("DIFFERENT");
    }
  }
}
```

**Pros:** lazy, thread-safe, no synchronization overhead, no `volatile` needed.
**Cons:** none worth mentioning — this is the idiomatic Java singleton.

---

## Comparison at a Glance

| Approach | Lazy? | Thread-safe? | Locking overhead | Notes |
| --- | --- | --- | --- | --- |
| `NoSingleton` | n/a | n/a | n/a | Not a singleton — each `new` is a new object. |
| `EagerInitializationSingleton` | No | Yes | None | Always created, even if unused. |
| `LazyInitializationSingleton` | Yes | **No** | None | Broken under concurrency. |
| `ThreadSafeSingleton` (`synchronized`) | Yes | Yes | On every call | Simple, but contended. |
| `ThreadSafeSingleton` (DCL) | Yes | Yes (if `volatile`) | Only on first init | Historically fragile; needs `volatile`. |
| `BillPughSingleton` | Yes | Yes | None | **Recommended.** |
| `enum` singleton | No | Yes | None | Best for serialization / reflection safety. |

---

## When (and when not) to use the Singleton pattern

**Use it for:**

- Stateless utility or registry objects where "there is only one" is a real invariant (loggers, metric registries, configuration caches).
- Library code that must not depend on a DI container.
- Objects that are genuinely expensive to build and safe to share (thread pools, caches, compiled regex tables).

**Avoid it for:**

- **Database connections** — you want a connection **pool**, not a single shared connection.
- Anything with mutable state you might want to mock in tests — singletons are global state and hard to substitute.
- **Spring-managed beans** — `@Service`, `@Repository`, `@Component`, `@Controller`, and `@Configuration` beans are singleton-scoped by default. The container gives you singleton behavior plus dependency injection benefits (testability, configurable scopes). Writing `getInstance()` inside a Spring bean is an anti-pattern.

## Singleton vs Dependency Injection

| Aspect | Singleton pattern | Dependency injection |
| --- | --- | --- |
| What it controls | How many instances exist | How instances are wired together |
| Who creates the object | The class itself (via `getInstance()`) | An external container or caller |
| How consumers get it | **Pull** — `Foo.getInstance()` | **Push** — constructor/setter parameter |
| Coupling | Consumers depend on the concrete class | Consumers depend on an interface/type |
| Testability | Hard — global state, can't swap | Easy — inject a mock |
| Lifecycle flexibility | Fixed at "one forever" | Configurable: singleton, request, session, prototype |

Spring's default bean scope is singleton — you get "one shared instance" **and** the flexibility of DI. For new code, prefer DI over hand-rolled singletons.

---

## TL;DR

- **Textbook answer:** private constructor + private static field + public static accessor.
- **Safe + lazy + simple:** use the **Bill Pugh** holder idiom.
- **Need serialization/reflection safety too:** use an `enum` singleton.
- **In a Spring app:** don't write singletons by hand — use `@Service` / `@Component` and let the container manage the lifecycle.
