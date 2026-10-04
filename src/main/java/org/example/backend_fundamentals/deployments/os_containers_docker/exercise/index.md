---
order: 10
search: false
---

# Exercises: OS, Containers & Docker

Practical interview exercises analyzing operating system concurrency bottlenecks, container isolation boundaries, signal handling, and cgroup resource management.

---

## Exercise: os-thread-concurrency-bottleneck - Concurrency Tuning & Context Switching Diagnosis

### Scenario
An SDE2 is designing a high-throughput synchronous payment webhook service running on an AWS EC2 instance with 4 vCPUs. The service currently spins up an unbounded thread pool (`Executors.newCachedThreadPool()`). During a flash-sale traffic spike (15,000 concurrent inbound requests), the service experiences severe latency degradation: CPU utilization hits 100%, but request throughput drops to nearly zero and request timeouts surge.

### Tasks
1. Identify the operating system and hardware mechanism causing CPU saturation and throughput collapse.
2. Explain the difference in resource overhead between creating 15,000 OS threads versus using a bounded thread pool sized according to CPU/IO wait ratios.
3. Recommend the optimal thread pool sizing formula and configuration for this 4-vCPU service where average payment gateway I/O latency is 190ms and CPU compute processing time is 10ms.

### Acceptance Criteria
- Explicitly explain context switching overhead, CPU cache thrashing (L1/L2/L3), and kernel scheduler load.
- Compare stack memory footprint and OS thread descriptor allocation against bounded execution.
- Calculate the recommended worker thread count using Little's Law or the classic I/O-to-Compute ratio formula ($N_{\text{threads}} = N_{\text{CPU}} \times (1 + \frac{\text{Wait Time}}{\text{Compute Time}})$).

---

## Exercise: container-isolation-and-signal-reaping - Container PID 1 Signal Handling & Isolation Triage

### Scenario
A production Spring Boot microservice deployed in a Docker container fails to perform graceful shutdown during deployments. In-flight database transactions are abruptly terminated, resulting in data inconsistency. Inspecting the container logs reveals that whenever a deployment or `docker stop` is executed, the container hangs for exactly 10 seconds and exits with `Exit Code 137`.

The microservice `Dockerfile` contains the following definition:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/payment-service.jar app.jar
COPY start.sh start.sh
RUN chmod +x start.sh
CMD ./start.sh
```
Inside `start.sh`:
```bash
#!/bin/sh
echo "Starting payment service..."
java -jar app.jar
```

### Tasks
1. Diagnose why `docker stop` waits 10 seconds before terminating the container with Exit Code 137.
2. Explain the role of Linux PID Namespaces and why the JVM fails to receive the `SIGTERM` signal.
3. Provide two distinct production-grade solutions to fix the `Dockerfile` / execution model so that the JVM receives `SIGTERM` and initiates Spring Boot's graceful shutdown hook.

### Acceptance Criteria
- Explain the difference between shell form (`CMD ./start.sh`) and exec form (`ENTRYPOINT ["..."]`), and why `/bin/sh` does not forward signals to sub-processes.
- Explain the origin of `Exit Code 137` ($128 + 9$ where 9 is `SIGKILL`).
- Provide the corrected `Dockerfile` syntax using direct exec form and alternatively using `tini` / `exec` in shell scripts.

---

## Exercise: cgroup-limits-and-jvm-oom - JVM Container Memory Sizing & cgroup Constraints

### Scenario
A Java microservice container is configured with a Docker memory limit of `--memory=512m`. The JVM is launched with `-Xmx512m`. Shortly after startup under heavy traffic, the container terminates abruptly with no Java `java.lang.OutOfMemoryError` stack trace in the application log files. The orchestrator reports the container state as `OOMKilled` (Exit Code 137).

### Tasks
1. Explain why setting `-Xmx512m` inside a 512MB container triggers a kernel OOMKilled termination instead of throwing a JVM `OutOfMemoryError`.
2. Detail all non-heap memory regions of the JVM process that consume host/cgroup resident memory.
3. Provide the correct JVM memory configuration flags (`-XX:MaxRAMPercentage`, off-heap margins) to safely operate within a 512MB cgroup limit.

### Acceptance Criteria
- Differentiate between JVM Heap and Non-Heap memory (Metaspace, Thread Stacks, Native Memory, Direct Byte Buffers, Code Cache, GC overhead).
- Explain how Linux kernel cgroups enforce `memory.max` via the OOM Killer when total RSS exceeds the boundary.
- Formulate a resilient production JVM configuration that leaves sufficient memory headroom for off-heap allocations.
