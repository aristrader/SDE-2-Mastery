---
order: 20
---

# Memory Areas — Heap, Stack, Metaspace, Code Cache

---

## The big picture

```
JVM Process
├── Heap              — shared across all threads
│   ├── Young Generation  (~1/3 of heap by default)
│   │   ├── Eden          (~80% of Young Gen)
│   │   ├── Survivor S0   (~10% of Young Gen)
│   │   └── Survivor S1   (~10% of Young Gen)
│   ├── Old Generation    (~2/3 of heap by default)
│   └── String Pool       — inside heap since Java 7
│
├── Metaspace         — native memory (outside heap), since Java 8
├── Code Cache        — JIT-compiled native code, native memory
│
└── Per-thread areas (one per thread)
    ├── Stack
    ├── PC Register
    └── Native Method Stack
```

---

## Heap

Objects live here. All `new` expressions allocate here. Shared across threads.

### Young Generation — how Minor GC works (ping-pong)

New objects start in **Eden**. When Eden fills up, a **Minor GC** runs:

```
Before GC:  Eden (full)   S0 (active, has survivors)   S1 (empty)

Minor GC:
  1. Scan Eden + S0 for live objects
  2. Copy them all into S1
  3. Eden + S0 are now empty
  4. S0 and S1 swap roles — S1 is now active, S0 is empty

Next Minor GC: same thing in reverse — live objects from Eden + S1 copied into S0
```

Why two Survivor spaces? Copying GC needs a clean destination — you can't copy objects in place.

**Age counter:** each survived copy ticks the object's age up by 1. At age 15 (default tenuring threshold) it gets **promoted** to Old Generation instead of copied again.

```
Minor GC 1:  Eden → S0  (age=1)
Minor GC 2:  S0   → S1  (age=2)
...
Minor GC 15: S?   → Old Gen  (promoted)
```

Minor GC is cheap — only touches Young Gen, runs frequently.

### Old Generation (Tenured)

Long-lived promoted objects live here. When Old Gen fills up, a **Major GC (Full GC)** runs — scans the entire heap, expensive, noticeable pause. If Full GC still can't free space → `OutOfMemoryError: Java heap space`.

**Default ratio:** Young Gen = ~1/3 of heap (`-XX:NewRatio=2` means Old:Young = 2:1). On a 3 GB heap: ~1 GB Young, ~2 GB Old. Tunable for high-churn apps.

### String Pool (inside heap since Java 7)

String literals and `.intern()` calls resolve to the pool. Same content → same object.

```java
String a = "hello";
String b = "hello";
a == b;              // true — same pool object

String c = new String("hello");
a == c;              // false — c is a new heap object
a.equals(c);         // true — same content
```

**Why it moved from PermGen to heap (Java 7):**
1. In PermGen, interned strings were never GC'd — pool could fill up causing OOM with no recovery.
2. PermGen was fixed-size and hard to tune; the heap is already dynamically managed.

Since Java 7, interned strings with no references get collected normally.

---

## Stack

Each thread has its own stack. Holds **frames** — one per active method call.

**A frame contains:**
- Local variables (method parameters + declared locals)
- Operand stack (workspace for bytecode instructions)
- Reference to the runtime constant pool

Frame pushed on method call, popped on return. **Primitives and references live on the stack; the objects they point to are on the heap.** Frame disappears instantly on return — no GC needed for stack memory.

```java
void foo() {
    int x = 42;           // value 42 on the stack frame
    Person p = new Person(); // reference p on stack; Person object on heap
}
// frame popped → x and p gone immediately
// Person object on heap → GC'd whenever nothing points to it
```

`StackOverflowError` = thread's stack is full (usually unbounded recursion).

---

## Metaspace

Holds **class metadata** — field names, method signatures, bytecode, constant pool, annotations. The *blueprint* of every loaded class.

Replaced PermGen in Java 8:
- PermGen: fixed-size inside the JVM heap → `OutOfMemoryError: PermGen space`
- Metaspace: native memory (outside heap) → auto-grows by default

**Static field values** are NOT in Metaspace — they live on the heap (on the `Class` object). Only the structure (that a field named `count` of type `int` exists) is in Metaspace.

### When Metaspace becomes a problem — dynamic class generation

Every new class blueprint grows Metaspace. In normal apps classes load at startup and stabilize, but some patterns generate classes continuously at runtime:

- **Groovy/scripting engines** — each unique script text compiles into a new class blueprint. 1000 different scripts = 1000 blueprints in Metaspace.
- **Frameworks generating runtime proxies without caching** — Spring AOP generates one proxy class per service at startup (fine). A framework generating a fresh proxy per request without caching produces unbounded blueprints.

**Why blueprints don't get cleaned up:** a class is only unloaded when its ClassLoader is GC'd. In server apps the ClassLoader lives forever → blueprints accumulate permanently.

**Fix:** cache compiled scripts/proxies so the same input reuses the existing blueprint. Cap with `-XX:MaxMetaspaceSize=256m` for early warning instead of silently consuming all native memory.

---

## Code Cache

JVM starts by interpreting bytecode line by line (slow). As it runs, the JIT compiler translates **hot methods** (called frequently) into native machine code, stored in the **Code Cache**.

```
First ~1000 calls:  JVM interprets placeOrder() — slow
JIT compiles it:    native machine code stored in Code Cache
All future calls:   CPU runs machine code directly — fast
```

Default size: ~240 MB (`-XX:ReservedCodeCacheSize`). If full, JVM stops JIT-compiling new methods and falls back to interpretation — severe performance degradation. You'll see: `CodeCache is full. Compiler has been disabled`.

Rarely an issue for typical Spring Boot services. Can hit very large apps or apps with heavy reflection.

---

## Native Methods and Native Method Stack

Some Java methods are implemented in C/C++ — declared with the `native` keyword, no body:

```java
public static native long currentTimeMillis();  // implemented in C, in the JVM itself
public static native void arraycopy(...);
```

Java can't directly access the OS (file system, clock, hardware) — it's isolated inside the JVM. Native methods are the escape hatch: they drop out of the JVM and call the OS directly via **JNI (Java Native Interface)**.

Daily examples: `System.currentTimeMillis()`, `Thread.sleep()`, default `Object.hashCode()`, all file I/O under the hood.

**Native Method Stack** — like the Java stack, but tracks C/C++ call frames during native method execution. One per thread, separate because C and Java have different calling conventions.

---

## Real-world config (from prod services)

All services use G1GC (`-XX:+UseG1GC`). Three heap strategies seen in practice:

| Strategy | Flag | Used when |
|---|---|---|
| Dynamic % of container RAM | `-XX:MaxRAMPercentage=90` | Pod size varies — no hardcoded `-Xmx` needed |
| Aggressive heap shrinking | `-XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=20` | Bursty traffic — grow during spikes, shrink back to save memory |
| Fixed heap | `-Xmx1512m -Xms256m` | Cron jobs with known memory profile |

No Metaspace or Code Cache flags set — G1GC sizes regions dynamically and defaults suffice for typical services.

---

## Quick recall

**Q. Heap vs stack — one-line difference?**
A. Heap is shared across threads, holds objects. Stack is per-thread, holds frames (locals + references), cleared instantly on method return.

**Q. Why two Survivor spaces?**
A. Copying GC needs a clean destination — one is always source, one is always target, they swap each GC cycle.

**Q. How does an object get promoted to Old Gen?**
A. Survives 15 Minor GC cycles (age threshold) or is too large for Survivor space.

**Q. Why did the string pool move to heap in Java 7?**
A. PermGen strings were never GC'd → OOM with no recovery. Heap strings are GC'd normally.

**Q. Why did Metaspace replace PermGen?**
A. PermGen was fixed-size → easy to exhaust. Metaspace uses native memory and auto-grows.

**Q. What causes `OutOfMemoryError: Metaspace`?**
A. Too many class blueprints loaded — usually dynamic class generation (Groovy scripts, uncached proxies) without caching or a size cap.

**Q. What is the Code Cache?**
A. Native memory where JIT-compiled machine code lives. If full, JVM falls back to interpretation — severe perf hit.

**Q. What are native methods?**
A. Java methods implemented in C/C++, declared with `native`. Used for OS-level operations (clock, file I/O, hardware) that the JVM can't do directly. Bridge is JNI.
