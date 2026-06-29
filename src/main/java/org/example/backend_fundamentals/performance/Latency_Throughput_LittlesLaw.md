# Latency, Throughput & Little's Law

## The Latency Hierarchy
Interviewers do NOT want memorized nanoseconds; they want intuition.
When you evaluate an architecture choice (e.g., using Redis instead of hitting the DB directly), knowing the latency ladder explains the *why*.

**The Ladder:**
Registers
↓ (fastest)
L1 Cache (Private to CPU core)
↓
L2 Cache (Slightly slower, private)
↓
L3 Cache (Shared across cores, slower but faster than RAM)
↓
RAM (Stores heap, objects, stack frames. Much slower than cache, vastly faster than SSD)
↓
SSD (Persistent storage. Stores OS, Java, JARs, DB files)
↓
Same Machine (Network over localhost, small overhead)
↓
Same Rack
↓
Same Availability Zone (AZ)
↓
Cross Region (Large latency. Avoid for every request)
↓
Internet (Largest latency)

**Key Analogy (Memory vs. Storage):**
Opening a Word Document:
- Initially: SSD
- Opening: SSD → RAM
- Editing: RAM (CPU executes here)
- Saving: RAM → SSD

### Common Misconceptions
*   **Misconception: "In-memory means instant."**
    *   **Correction:** RAM access still has latency; it's simply much cheaper than disk or network.
*   **Misconception: "RAM is built on SSD."**
    *   **Correction:** Completely different hardware. RAM is DRAM (volatile); SSD is NAND Flash (persistent).
*   **Misconception: "Redis stores Java objects."**
    *   **Correction:** Redis stores serialized bytes (JSON/Binary). Your app serializes the Java object before sending it over the network to Redis.

## CPU Cores & Context Switching
*   **Analogy:** One core = One chef. Four cores = Four chefs. Each core executes instructions independently and simultaneously.
*   **The Wait Problem:** A CPU is extremely fast, but it spends most of its life waiting (for RAM, Disk, Network, DB, Locks).
*   **Context Switching:** If you have 8 cores but 300 threads, only ~8 CPU-intensive threads run simultaneously. The OS continuously swaps threads so everyone makes progress. This context switching costs CPU time (saving registers, cache disruption).

## Throughput vs. Latency vs. Concurrency
*   **Latency:** Time for *one* request (e.g., 150 ms).
*   **Throughput:** Requests completed per second (e.g., 1000 RPS).
*   **Concurrency:** How many requests are *currently in progress* in the system.

### Misconception on Scaling
*   **Misconception:** Increasing concurrency automatically increases throughput and reduces latency.
*   **Correction:** Increasing concurrency only helps until you hit a bottleneck. Beyond that, context switching, lock contention, and memory pressure will *reduce* throughput and *increase* latency.

## Little's Law
**Formula:** `Concurrency = Throughput × Latency`  (L = λ × W)

*   **Meaning:** Active Requests = Requests/sec × Average request time.
*   **Restaurant Analogy:** 10 customers arrive per second. Each stays 5 seconds. Total inside = 50.
*   **Interview Example:** If you have 1000 RPS and 100 ms (0.1s) latency, how many active requests? `1000 × 0.1 = 100 active requests.`

### Crucial Concurrency Distinction
*   **Misconception:** Concurrency is the number of requests the CPU is executing.
*   **Correction:** Concurrency is the number of requests *alive* in the system. Requests spend most of their time waiting for I/O. That waiting allows the CPU to context-switch and process other requests.

## Quick recall
**Q. Why is a database query slower than a Redis hit?**
A. DB involves network, parsing, execution, storage lookup, and response. Redis avoids the heavy storage/parsing steps, retrieving serialized bytes directly from memory.

**Q. Does Spring Boot execute code directly from the SSD?**
A. No. `java -jar` loads the application from SSD into RAM, and the CPU executes instructions from RAM/Cache.

**Q. If you have 500 RPS and 200 ms latency, what is your concurrency?**
A. 500 × 0.2 = 100 active requests in flight.

**Q. Why can't we just create 10,000 threads for 10,000 requests?**
A. Threads are expensive (stack memory, JVM overhead) and context-switching 10,000 threads on an 8-core machine will waste CPU time thrashing rather than doing useful work.
