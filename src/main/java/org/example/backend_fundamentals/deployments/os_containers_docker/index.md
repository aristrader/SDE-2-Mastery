---
order: 20
---

# OS, Containers & Docker Fundamentals

Modern backend services run on containerized infrastructure (Kubernetes, AWS ECS, Docker). For a backend engineer, understanding operating system primitives, container isolation boundaries, and container image construction is essential for diagnosing CPU starvation, memory leaks (OOMKilled), resource contention, and deployment bottlenecks.

---

## 1. Process vs. Thread

Operating systems manage concurrent execution via processes and threads.

| Dimension | Process | Thread |
| :--- | :--- | :--- |
| **Definition** | A running instance of a program loaded from disk into memory. | A lightweight execution path inside a process. |
| **Memory & Resources** | Owns isolated virtual address space, heap, file descriptors (FDs), sockets, and page tables. | Shares heap, text (code), open FDs, and sockets of its parent process. Owns its own stack and registers (PC). |
| **Isolation** | High. Cannot directly read/write memory of another process without IPC. | Low. A memory corruption or segfault in one thread crashes the entire process. |
| **Communication** | Inter-Process Communication (IPC): Unix domain sockets, TCP/UDP loops, pipes, shared memory. | Direct in-memory reads/writes with concurrency synchronization (`volatile`, CAS, locks). |
| **Identification** | Process ID (PID). | Thread ID (TID / LWP on Linux). |
| **Mental Model Analogy** | A restaurant (independent kitchen, pantry inventory, dining floor). | Workers in the same restaurant (waiters and cooks sharing the single kitchen). |

### Memory Layout Comparison

```
+-------------------------------------------------------------------+
| Host Physical Memory / OS Virtual Memory Management               |
|                                                                   |
| +-----------------------------------+   +-----------------------+ |
| | Process A (PID 101)               |   | Process B (PID 102)   | |
| | +-------------------------------+ |   | +-------------------+ | |
| | | Shared Heap, Open FDs, Text   | |   | | Isolated Address  | | |
| | +-------------------------------+ |   | | Space             | | |
| | | Thread 1: Stack + Registers   | |   | +-------------------+ | |
| | | Thread 2: Stack + Registers   | |   |                       | |
| | +-------------------------------+ |   |                       | |
| +-----------------------------------+   +-----------------------+ |
+-------------------------------------------------------------------+
```

---

## 2. CPU Scheduling & Context Switching

The Linux CPU scheduler (CFS / EEVDF) rapidly switches CPU core execution among runnable threads to give the illusion of true parallelism across limited physical cores.

- **Context Switch:** The hardware/kernel operation of saving the execution state (CPU registers, Program Counter, stack pointer, memory mapping cache/TLB entries) of the currently running thread and restoring the state of the scheduled thread.
- **The "Too Many Threads" Pitfall (Context Switching Thrashing):**
  - Having more threads than hardware CPU cores does not guarantee higher throughput.
  - If an 8-core host runs 50,000 active compute threads, CPU cores spend dominant cycles executing kernel scheduler interrupts and invalidating hardware CPU caches (L1/L2/L3 cache misses and TLB shootdowns) rather than executing application instructions.
  - Sizing thread pools (e.g., Tomcat worker threads, database connection pools) to match I/O wait vs CPU-bound ratios prevents thrashing.

---

## 3. Container Internals: Namespaces & cgroups

A Linux container is **not** a lightweight virtual machine. A container is a standard Linux host process running under kernel isolation primitives: **Namespaces** and **Control Groups (cgroups)**.

```
+--------------------------------------------------------------------------------+
| Host Linux Kernel                                                              |
|                                                                                |
|  [ cgroups v2 ] Memory Limits (e.g. 512MB), CPU Quotas (e.g. 1.0 core)         |
|  [ Namespaces ] PID, MNT, NET, IPC, UTS, USER                                  |
|                                                                                |
|  +--------------------------------------------------------------------------+  |
|  | Container Process (Host PID 4827 -> Container PID 1)                     |  |
|  |  - Isolated VFS root mount via Pivot_Root / OverlayFS                    |  |
|  |  - Isolated Virtual Network Device (veth pair -> docker0 bridge)         |  |
|  +--------------------------------------------------------------------------+  |
+--------------------------------------------------------------------------------+
```

### Namespaces ("What the process can see")
Namespaces partition global system resources into isolated views:
- **PID Namespace:** Isolates the process hierarchy. The containerized entrypoint runs as PID 1 inside the container, while appearing as an ordinary PID (e.g., PID 4827) to the host kernel.
- **Mount (MNT) Namespace:** Isolates filesystem mount points. The container sees its own root filesystem (`/app`, `/etc`, `/var`) separate from the host.
- **Network (NET) Namespace:** Isolates network devices, routing tables, firewall rules, and port bindings. A container can bind to port `8080` without colliding with another container binding to port `8080` on the same host.
- **IPC / UTS / User Namespaces:** Isolate System V IPC/POSIX message queues, hostnames/domain names, and UID/GID mappings (e.g., non-root inside mapping to unprivileged user outside).

### Control Groups / cgroups ("What the process can use")
cgroups meter, throttle, and enforce hard and soft resource boundaries:
- **Memory Limits (`memory.max` / `memory.limit_in_bytes`):** Enforces maximum resident set size. When a process exceeds this quota, the Linux kernel OOM-killer (Out Of Memory) terminates the process (`Exit Code 137 / SIGKILL`).
- **CPU Quotas (`cpu.max` / `cpu.cfs_quota_us` & `cpu.cfs_period_us`):** Limits CPU time slices (e.g., 50ms per 100ms window = 0.5 CPU cores). Exceeding quota causes CPU throttling without killing the process.
- **Block I/O & PID Limits (`pids.max`):** Restricts disk read/write throughput and prevents fork-bomb denial-of-service by capping concurrent active threads/processes.

---

## 4. Docker Architecture & Mental Models

### Docker Image vs. Container
- **Image:** An immutable, static, read-only template built from layered filesystems (OCI specification).
- **Container:** A stateful, running instance of an image instantiated by creating an isolated process with a thin read-write container layer on top.
- **Mental Model Analogy:** Class (`Image`) vs. Object instance (`Container`).

### Dockerfile vs. docker-compose.yml
- **Dockerfile:** Defines **how to build** a single container image (the recipe: base runtime, dependencies, compiled JAR, exposed ports, entrypoint).
- **docker-compose.yml:** Defines **multi-container service orchestration** for local development (the environment topology: coordinating app service, Kafka, Zookeeper, Redis, PostgreSQL, shared networks, and volume mounts).

### Base Images (`FROM`) & Layer Caching
- `FROM eclipse-temurin:21-jre-alpine` does not compile your Java code; it provides an immutable root filesystem snapshot pre-installed with the Linux OS utilities and Java runtime.
- **Layer Caching:** Docker builds images in layered commits. Changing high-frequency artifacts (like `app.jar`) invalidates subsequent build steps. Ordering `pom.xml` dependency downloads before source copy optimizes cache hit rates.

### Software Supply Chain: Code vs. Binaries vs. Images
- **Source Code Repository (GitHub, GitLab, Bitbucket):** Tracks source code, configuration files, build descriptors (`pom.xml`), and `Dockerfile`.
- **Artifact Repository (Nexus, JFrog Artifactory):** Stores versioned build artifacts and library binaries (`.jar`, `.war`).
- **Container Image Registry (Docker Hub, AWS ECR, GCP Artifact Registry):** Stores versioned, scanned, runnable container images containing OS binaries, runtime, and application artifacts.

---

## 5. Java Runtime Ecosystem: JVM vs. JRE vs. JDK

| Component | Responsibility | Contents | Use Case |
| :--- | :--- | :--- | :--- |
| **JVM** (Java Virtual Machine) | Executes Java bytecode (`.class`), performs Just-In-Time (JIT) compilation, manages memory via Garbage Collection (GC). | Execution engine, memory areas (Heap, Metaspace, Stacks), GC threads. | Core execution runtime. |
| **JRE** (Java Runtime Environment) | Legacy package for running pre-compiled applications without development tools. | JVM + core Java standard library classes (`rt.jar` / `java.base`). | Production runtime image base. |
| **JDK** (Java Development Kit) | Complete development environment. | JRE / JVM + compiler (`javac`), debuggers (`jdb`), profilers (`jcmd`, `jstack`, `jmap`), and packaging tools (`jlink`, `jar`). | Local development and CI build stages. |

> **Interview Distinction:** Development requires JDK (`javac` + tools). Minimal production containers require only a slim JRE / runtime image to minimize image footprint and attack surface.

---

## 6. Real-World Failure Modes & Pitfalls

1. **The Container PID 1 Zombie Reaping Problem:**
   - In Unix, PID 1 has the special duty of reaping orphaned child processes (zombies) and forwarding signals (`SIGTERM`, `SIGINT`).
   - If a Java application is launched via a shell script (`CMD ./start.sh`) without `exec`, `/bin/sh` runs as PID 1. When `docker stop` sends `SIGTERM`, the shell often fails to forward it to the JVM. The container hangs for 10 seconds until Docker issues a forceful `SIGKILL` (Exit 137), aborting in-flight transactions without graceful shutdown.
   - *Fix:* Use the exec array form `ENTRYPOINT ["java", "-jar", "app.jar"]` or an init process (e.g., `tini` / `docker run --init`).
2. **JVM Container Awareness & cgroup Limits:**
   - Early JVM versions (prior to Java 8u191 / Java 10) read available CPU cores and physical memory from `/proc` on the host, ignoring container cgroup constraints. A container with a 512MB cgroup limit on a 64GB host would default its JVM heap to 16GB (1/4 of host RAM), triggering immediate kernel OOMKilled termination.
   - Modern JVMs (Java 11/17/21) support `UseContainerSupport` (enabled by default) and respect cgroup memory/CPU limits (`-XX:MaxRAMPercentage=75.0`).

---

## 7. Quick Recall

1. **Why does high thread count degrade performance despite having idle memory?**
   CPU cores spend excessive time executing kernel context switches, scheduler interrupts, and cache/TLB invalidations instead of user code instructions.
2. **What is the fundamental difference between a virtual machine and a container?**
   A VM virtualizes the hardware layer and runs a complete guest OS kernel via a hypervisor; a container shares the host Linux kernel and is isolated purely via namespaces and cgroups.
3. **What is the difference between Linux Namespaces and cgroups?**
   Namespaces determine **what a process can see** (visibility isolation: PID, mounts, network); cgroups determine **what a process can use** (resource limits: CPU, memory, I/O).
4. **What triggers a container OOMKilled (Exit Code 137) event?**
   The total resident memory of the processes in the container cgroup exceeds the configured limit (`memory.max`), prompting the kernel OOM killer to terminate the process with `SIGKILL` (128 + 9 = 137).
5. **Why should you use the exec syntax (`ENTRYPOINT ["java", "-jar", "app.jar"]`) over shell syntax?**
   Exec syntax ensures the JVM runs directly as PID 1 to receive `SIGTERM` signals for graceful shutdown, rather than being trapped behind a `/bin/sh` process wrapper.
6. **What is the difference between an Artifact Repository and a Container Registry?**
   An artifact repository (e.g., Artifactory) stores application packages and dependencies (`.jar`), whereas a container registry (e.g., AWS ECR) stores bootable OCI container images containing the OS layer, runtime, and application.
