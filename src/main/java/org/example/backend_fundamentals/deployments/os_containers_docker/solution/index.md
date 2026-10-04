---
order: 20
search: false
---

# Solutions: OS, Containers & Docker

Detailed solutions and diagnostic walkthroughs for OS concurrency, container signal handling, and cgroup memory management exercises.

---

## Solution: os-thread-concurrency-bottleneck - Concurrency Tuning & Context Switching Diagnosis

### 1. Root Cause Analysis
- **Context Switching Overhead:** When 15,000 threads compete for 4 CPU cores, the OS kernel scheduler must constantly interrupt running threads via timer interrupts. Saving registers, updating process control blocks, and scheduling the next thread consumes significant CPU time.
- **Cache Thrashing (L1/L2/L3 & TLB Misses):** Every time a core switches between completely different threads and execution contexts, the CPU L1 and L2 data/instruction caches and Translation Lookaside Buffer (TLB) entries are invalidated. Threads constantly stall waiting for memory fetches from main RAM rather than executing pipeline instructions.
- **Memory Overhead:** Each Java thread allocates a native stack (typically 1MB via `-Xss1024k`). 15,000 threads consume $\approx 15\text{ GB}$ of resident virtual memory solely for thread stacks, causing high memory pressure and GC pauses.

### 2. Thread Pool Sizing Calculation
For an I/O-intensive workload (190ms payment gateway network latency, 10ms CPU processing time):
$$\text{Wait Time } (W) = 190\text{ ms}, \quad \text{Compute Time } (C) = 10\text{ ms}, \quad N_{\text{CPU}} = 4$$

Using the classic thread pool sizing formula:
$$N_{\text{threads}} = N_{\text{CPU}} \times \left(1 + \frac{W}{C}\right) = 4 \times \left(1 + \frac{190}{10}\right) = 4 \times (1 + 19) = 80 \text{ threads}$$

### 3. Recommended Production Configuration
```java
// Bounded thread pool with bounded blocking queue and backpressure rejection policy
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    80,                                        // Core pool size
    80,                                        // Max pool size
    60L, TimeUnit.SECONDS,                     // Keep-alive time
    new ArrayBlockingQueue<>(1000),            // Bounded queue prevents OutOfMemoryError
    new ThreadFactoryBuilder().setNameFormat("payment-worker-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy()   // Backpressure: throttling client traffic
);
```
*(Alternatively in Java 21+: Use Virtual Threads `Executors.newVirtualThreadPerTaskExecutor()` which decouple millions of lightweight user-mode continuations from limited OS carrier threads).*

---

## Solution: container-isolation-and-signal-reaping - Container PID 1 Signal Handling & Isolation Triage

### 1. Diagnosis of the 10-Second Hang & Exit Code 137
- When `docker stop` is invoked, the Docker daemon sends a `SIGTERM` signal to **PID 1** inside the container's PID namespace.
- In the provided `Dockerfile`, `CMD ./start.sh` executes `/bin/sh` as PID 1. The script runs `java -jar app.jar` as a child process (PID 2+).
- By default, standard Unix shell binaries (`/bin/sh`, `/bin/bash`) do **not** forward OS signals (`SIGTERM`, `SIGINT`) to child processes unless explicitly trapped.
- Because PID 1 (`/bin/sh`) ignores `SIGTERM`, the JVM process never receives the shutdown signal and continues running.
- After a default timeout of 10 seconds (`--time=10`), the Docker daemon sends an unmaskable `SIGKILL` (signal 9) directly to all processes in the container.
- **Exit Code 137:** Standard Unix exit code convention for processes killed by a signal is $128 + N$. For `SIGKILL` (signal 9), $128 + 9 = 137$.

### 2. Production Fixes

#### Fix Option A: Direct Exec Form in Dockerfile (Recommended)
Bypass the shell wrapper completely and launch the JVM directly as PID 1:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/payment-service.jar app.jar
# Use exec array syntax (JSON array format)
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Fix Option B: Shell Script with `exec`
If pre-startup initialization logic is mandatory in `start.sh`, use the `exec` builtin command. `exec` replaces the shell process with the Java process while preserving PID 1:
```bash
#!/bin/sh
echo "Performing startup initialization..."
# 'exec' replaces /bin/sh with java, retaining PID 1
exec java -jar app.jar
```

#### Fix Option C: Init Process (`tini`)
Use a minimal init process to handle zombie reaping and proper signal forwarding:
```dockerfile
RUN apk add --no-cache tini
ENTRYPOINT ["/sbin/tini", "--", "java", "-jar", "app.jar"]
```

---

## Solution: cgroup-limits-and-jvm-oom - JVM Container Memory Sizing & cgroup Constraints

### 1. Why OOMKilled Occurred Instead of `java.lang.OutOfMemoryError`
- The `OutOfMemoryError` exception is thrown internally by the JVM only when the **Java Heap** cannot satisfy an object allocation after garbage collection.
- However, a JVM process consumes significant memory **outside** the Java Heap:
  $$\text{Total Process Resident Memory (RSS)} = \text{Heap} + \text{Metaspace} + \text{Thread Stacks} + \text{Code Cache} + \text{Direct Buffers (Netty/NIO)} + \text{Native JVM GC/C++ runtime}$$
- When `-Xmx512m` is set on a 512MB container, the heap alone uses up to 512MB. As soon as the JVM allocates Metaspace or thread stacks, total process RSS exceeds the 512MB cgroup limit (`memory.max`).
- The Linux kernel cgroup subsystem detects this breach and immediately triggers the kernel OOM Killer, terminating the process via `SIGKILL` (Exit 137). Because the termination is issued by the kernel, the JVM is killed instantly before it can log a Java stack trace.

### 2. Non-Heap Memory Breakdown
- **Metaspace:** Class metadata, method bytecodes, and constant pools (`-XX:MaxMetaspaceSize`).
- **Thread Stacks:** Memory allocated for each thread stack ($N_{\text{threads}} \times \text{-Xss}$, e.g. $200 \times 1\text{MB} = 200\text{MB}$).
- **Direct Byte Buffers (NIO):** Off-heap buffers used by Netty, gRPC, and Spring WebFlux.
- **Code Cache:** JIT-compiled native machine code instructions (`-XX:ReservedCodeCacheSize`).
- **JVM Internal Overheads:** Garbage collector metadata, mark bit maps, and C++ symbol tables.

### 3. Recommended Production Configuration
Leave at least 25% to 30% headroom for non-heap allocations when configuring container memory:

```dockerfile
# Sizing JVM heap to 70-75% of the cgroup memory limit
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=75.0", \
    "-XX:MaxMetaspaceSize=128m", \
    "-Xss512k", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-jar", "app.jar"]
```

With `--memory=512m`:
- Max Heap (75%): $\approx 384\text{MB}$
- Off-heap Headroom (25%): $\approx 128\text{MB}$ (sufficient for Metaspace, 50-100 thread stacks at 512KB, and GC overhead).
