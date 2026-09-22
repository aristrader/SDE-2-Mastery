---
order: 50
---

# Garbage Collection

---

## Start with reachability, not “unused”

GC reclaims heap objects that are no longer reachable from GC roots such as live thread stacks, static fields, and JVM runtime references. A static cache, listener registry, queue, or `ThreadLocal` can keep an object reachable forever even if the application no longer wants it. That is a memory leak in a garbage-collected program.

GC manages memory, not external resources. Close files, sockets, database connections, and locks explicitly with `try-with-resources`; waiting for reachability is neither prompt nor safe.

## The generational hypothesis

The core assumption behind the common generational JVM collector model:

> **Most objects die young.**

In practice, a `String` built to format a log line, a `ResponseDto` per HTTP request, or a local `List` inside a method is typically short-lived. A small fraction (DB connection pools, caches, singletons) live for the entire app lifetime.

This justifies splitting the heap into Young and Old generations and collecting them separately — most of the time you only need to scan the small, cheap young region.

---

## Collector names are not a universal contract

“Minor,” “major,” and “full” are convenient terms, but exact phases and log labels depend on the collector and JDK. Prefer the collector's real terms when diagnosing it.

| Useful model | What it means | What to avoid claiming |
|---|---|---|
| Young collection | Reclaims young-generation regions; it is often cheap because most young objects are dead. | Every young collection has the same pause or copying algorithm. |
| Old/mixed collection | Reclaims old-generation work, sometimes alongside young work. | “Major GC” has one standard JVM meaning. |
| Full GC | A broad, expensive fallback in many HotSpot collector modes. | It always collects every memory area or is always caused by a leak. |

Young copying collection commonly moves only live objects and abandons dead space. Old-space work is often more expensive because there is more live data to trace, but measure the actual collector logs before deciding why a pause occurred.

---

## Stop-the-world pauses

Most collectors need **stop-the-world** (STW) phases. During one, Java application threads do not make progress, so requests can stall even if CPU use looks low.

STW lets the runtime obtain a consistent view of roots or safely complete a phase that cannot run concurrently. Concurrent collectors reduce pause work with barriers and concurrent marking/relocation; they do not make latency free.

G1, ZGC, and Shenandoah use concurrent phases to reduce STW work; their remaining pause behavior still differs by collector and workload.

---

## G1GC: the practical default model

Current HotSpot Java SE guidance generally selects G1 through VM ergonomics for server-style workloads, but collector choice is not an unconditional Java SE contract. Verify the collector in the target runtime rather than inferring it from a Java version. G1 divides the heap into equal-sized regions that are dynamically assigned young, survivor, old, or humongous roles; it remains generational rather than removing that model.

```
Heap divided into N equal regions (e.g. 2048 regions of 2MB each on a 4GB heap)

[ E ][ E ][ E ][ S ][ O ][ O ][ E ][ O ][ S ][ H ][ H ]...
  E = Eden   S = Survivor   O = Old   H = Humongous (large objects ≥ 50% region size)
```

**Why "Garbage First":** G1 collects the regions with the most garbage first — highest return for lowest cost, via a priority list by garbage density.

**Useful G1 cycle model:**
1. **Young collection** reclaims Eden/Survivor regions with an STW pause.
2. **Concurrent marking** identifies live old-region data while the application runs.
3. **Mixed collections** include young regions plus selected old regions with high reclaim value.
4. **Full GC** is a costly fallback to investigate, not a normal target state.

`-XX:MaxGCPauseMillis` is a pause-time goal, not an SLA. G1 uses it when selecting collection work, but allocation rate, heap size, live data, and CPU determine whether the goal is achievable.

---

## ZGC and Shenandoah (low-latency GCs)

These are concurrent, low-latency choices for workloads whose measured pause requirement justifies their CPU and operational cost.

| GC | Primary intent | How |
|---|---|---|
| G1 | Balanced general-purpose throughput and pause goals | Region-based collection with concurrent marking |
| ZGC | Very low pause latency | Highly concurrent collection/relocation |
| Shenandoah | Very low pause latency | Concurrent evacuation using load barriers |

**Trade-off:** concurrent collectors use CPU and runtime machinery to reduce pause work. Start with the default, observe the real latency and GC logs, then choose based on a measured throughput-versus-latency requirement.

---

## GC tuning intuition

Defaults are usually the right starting point. Tune only after collecting workload-representative evidence.

**Heap size:** `-Xms` and `-Xmx` bound initial and maximum heap. Making them equal can reduce heap-growth variability when capacity is deliberately reserved; it also removes elasticity and is not a universal production rule. In containers, budget for native memory, threads, code cache, and direct buffers—not just the Java heap.

**Pause goal:** a lower G1 `-XX:MaxGCPauseMillis` goal can trade throughput for shorter pauses. It is a target, not a guarantee.

**Diagnose before tuning:** enable current unified GC logging (for example, `-Xlog:gc*`), correlate pauses with request latency, then inspect allocation rate, post-GC live heap, promotion pressure, and container memory headroom.

**Symptoms and causes:**

| Symptom | Likely cause |
|---|---|
| Frequent costly collections with little reclamation | Live set is large: investigate retention/leak, heap headroom, and workload growth. |
| Long STW pauses | Inspect collector phase, live data, allocation pressure, CPU, and heap configuration. |
| High allocation rate | Reduce avoidable allocation only after profiling; changing generation sizes blindly can hide the cause. |
| `OutOfMemoryError: Java heap space` | Heap demand exceeded the limit: distinguish a retained-object leak from legitimate capacity demand with a heap dump. |
| `OutOfMemoryError: GC overhead limit exceeded` | The VM is spending nearly all its time collecting while recovering very little; investigate live retention and heap sizing. |

---

## Quick recall

**Q. What makes an object eligible for GC?**
A. It is unreachable from GC roots. “The method no longer needs it” is irrelevant if a static collection or listener still references it.

**Q. What is the generational hypothesis?**
A. Most objects die young, which makes young-generation collection efficient; exact generation mechanics depend on the collector.

**Q. Why is a young collection often cheap?**
A. It commonly copies only live young objects and abandons dead space, but its cost still depends on the live set and collector.

**Q. What is a Stop-The-World pause?**
A. Java application threads stop making progress while the JVM performs a required GC phase; concurrent collectors reduce, not eliminate, such pauses.

**Q. What makes G1 useful?**
A. It uses dynamically assigned regions, concurrent marking, mixed collections, and a pause-time goal; it still has young and old roles.

**Q. G1 versus ZGC/Shenandoah—how do you choose?**
A. Start with measured pause and throughput needs. Choose a more concurrent collector only when its lower latency benefit justifies the CPU and operational trade-off.

**Q. Should `-Xms` equal `-Xmx` in production?**
A. Only when reserved capacity and measured variability justify fixing the heap size; it is a trade-off, not a universal rule.
