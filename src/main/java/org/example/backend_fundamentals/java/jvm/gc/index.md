---
order: 30
---

# Garbage Collection

---

## The generational hypothesis

The core assumption behind all modern GC design:

> **Most objects die young.**

In practice: a `String` built to format a log line, a `ResponseDto` per HTTP request, a local `List` inside a method — all created and discarded within milliseconds. A small fraction (DB connection pools, caches, singletons) live for the entire app lifetime.

This justifies splitting the heap into Young and Old generations and collecting them separately — most of the time you only need to scan the small, cheap young region.

---

## Minor GC vs Major GC vs Full GC

| Type | What it collects | Frequency | Cost |
|---|---|---|---|
| Minor GC | Young Gen only (Eden + Survivors) | Very frequent | Cheap — milliseconds |
| Major GC | Old Gen (sometimes Young too) | Infrequent | Expensive — tens of ms to seconds |
| Full GC | Entire heap + Metaspace | Rare (usually a sign of trouble) | Most expensive — can pause for seconds |

Minor GC is cheap because it uses **copying collection** — it only touches live objects; dead ones are abandoned. Since most young objects are dead, there's little to copy.

Major GC is expensive because Old Gen has many more live objects to scan and compact.

---

## Stop-the-world pauses

Most GC work requires **stopping all application threads** (Stop-The-World, STW) for at least some phase. During a STW pause the app is frozen — no requests served, no processing.

**Why STW is needed:** if objects move in memory while threads run, a thread might read a stale reference (object moved, pointer not yet updated). Pausing everything keeps memory consistent during GC.

Modern GCs (G1, ZGC, Shenandoah) minimize STW by running most work **concurrently** with the app.

---

## G1GC (Garbage First — default since Java 9)

G1 replaces the old Young/Old Gen layout with a **region-based heap** — equal-sized regions (~1–32 MB each) dynamically assigned roles:

```
Heap divided into N equal regions (e.g. 2048 regions of 2MB each on a 4GB heap)

[ E ][ E ][ E ][ S ][ O ][ O ][ E ][ O ][ S ][ H ][ H ]...
  E = Eden   S = Survivor   O = Old   H = Humongous (large objects ≥ 50% region size)
```

**Why "Garbage First":** G1 collects the regions with the most garbage first — highest return for lowest cost, via a priority list by garbage density.

**G1 GC cycle:**
1. **Minor GC** — collect Eden + Survivor regions. STW, fast.
2. **Concurrent marking** — scan Old Gen regions for live objects. Runs concurrently with the app.
3. **Mixed GC** — collect Eden + Survivor + the most garbage-dense Old Gen regions together. STW.
4. **Full GC** — last resort if concurrent marking can't keep up. STW, slow.

**Key advantage over old Parallel GC:** G1 lets you set a pause target (`-XX:MaxGCPauseMillis=200`) and tries to meet it by choosing how many regions to collect per cycle. No guarantee, but it tries.

---

## ZGC and Shenandoah (low-latency GCs)

Designed for applications where long GC pauses are unacceptable (real-time, financial, interactive).

| GC | STW pause target | How |
|---|---|---|
| G1 | ~200ms (configurable) | Concurrent marking, region-based collection |
| ZGC | < 1ms (sub-millisecond) | Almost entirely concurrent — even compaction runs concurrently |
| Shenandoah | < 1ms | Concurrent compaction via load barriers |

**Trade-off:** ZGC and Shenandoah burn more CPU doing GC concurrently. G1 is more CPU-efficient but pauses longer. Pick based on throughput vs latency priority.

---

## GC tuning intuition

Defaults are good — you rarely need to tune extensively. When you do:

**Heap size (`-Xms`, `-Xmx`):**
- Set `-Xms` = `-Xmx` in production — prevents the JVM from repeatedly growing the heap (each resize can trigger a Full GC).
- Or use `-XX:MaxRAMPercentage=75` in containers to dynamically size based on pod memory.

**Pause target (G1):**
- `-XX:MaxGCPauseMillis=200` — default 200ms. Tune down for latency-sensitive apps, up for higher throughput.

**Diagnosing GC problems:**
- Enable GC logging: `-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=20m`
- Look for: frequent Full GCs (heap too small or memory leak), long STW pauses (Old Gen too full), rapid Minor GC (objects promoted too fast)

**Symptoms and causes:**

| Symptom | Likely cause |
|---|---|
| Frequent Full GC | Heap too small, or memory leak filling Old Gen |
| Long STW pauses | Old Gen fragmented, too many live objects |
| High allocation rate | Short-lived objects created too fast — increase Young Gen |
| `OutOfMemoryError: Java heap space` | Heap exhausted — leak or heap too small |
| `OutOfMemoryError: GC overhead limit exceeded` | JVM spending >98% of time in GC, reclaiming <2% — effectively a leak |

---

## Quick recall

**Q. What is the generational hypothesis?**
A. Most objects die young — justifies separate Young/Old regions and cheap Minor GC.

**Q. Why is Minor GC cheap?**
A. Copying collection — only live objects are copied, dead ones abandoned. Most young objects are dead so there's little to copy.

**Q. What is a Stop-The-World pause?**
A. All app threads are frozen while GC does its work, to prevent threads reading stale references during object moves.

**Q. What makes G1 different from old GCs?**
A. Region-based heap (not fixed Young/Old split), collects highest-garbage regions first, configurable pause target.

**Q. G1 vs ZGC — when to pick which?**
A. G1 for most apps (good throughput, ~200ms pauses). ZGC/Shenandoah for latency-critical apps needing sub-millisecond pauses.

**Q. What does `GC overhead limit exceeded` mean?**
A. JVM is spending almost all its time in GC but reclaiming almost nothing — effectively a memory leak.

**Q. `-Xms` = `-Xmx` in production — why?**
A. Prevents JVM from repeatedly resizing the heap. Each resize can trigger a Full GC and introduces latency spikes.

