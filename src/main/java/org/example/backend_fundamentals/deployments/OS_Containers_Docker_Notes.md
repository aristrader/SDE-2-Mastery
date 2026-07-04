# OS, Containers & Docker Fundamentals

This document captures foundational mental models for operating systems, container internals, and Docker usage, synthesized from recent study sessions.

## 1. Process vs. Thread

*   **Process:** A running instance of a program (e.g., a file on disk loaded into memory).
    *   **Owns:** Memory space, file descriptors, network connections, and resources.
    *   **Isolation:** High. Processes cannot freely read each other's memory.
    *   **Identification:** Process ID (PID).
    *   **Analogy:** A restaurant (has its own kitchen, inventory, tables).
*   **Thread:** A lightweight unit of execution within a process.
    *   **Shares:** Memory, files, and network connections with other threads in the same process.
    *   **Communication:** Fast and easy because memory is shared (unlike processes which require IPC, sockets, or pipes).
    *   **Analogy:** Workers in the restaurant (waiters, cooks) sharing the same kitchen.

## 2. CPU Scheduling & Context Switching

*   **CPU Scheduler:** Rapidly switches the CPU among multiple processes/threads (thousands of times per second) to create the illusion of simultaneous execution.
*   **Context Switch:** The act of saving the state of the currently running process/thread and loading the state of the next one.
*   **The "Too Many Threads" Problem:** More threads do not always equal more speed. If you have 8 cores and 50,000 threads, the CPU spends excessive time context switching rather than doing useful work.

## 3. Container Internals: Namespaces and cgroups

Containers are fundamentally just regular Linux processes with applied isolation.

*   **Namespaces ("What you can see"):** Provide visibility isolation. They trick the process into thinking it's the only one on the machine.
    *   **PID Namespace:** The container sees its own PID 1, while the host sees the container process as, for example, PID 4827.
    *   **Filesystem Namespace:** The container has its own isolated view of the filesystem (`/app`, `/config`).
    *   **Network Namespace:** The container gets its own isolated network stack (ports, IP addresses).
*   **cgroups / Control Groups ("What you can use"):** Provide resource limits. They restrict how much CPU, memory, or disk I/O a process can consume, preventing a single container from starving the host.

## 4. Docker Mental Models

*   **Docker Image vs. Container:**
    *   **Image:** A blueprint or template. It is immutable and not running.
    *   **Container:** A running instance of an image.
    *   **Analogy:** Class vs. Object.
*   **Dockerfile vs. docker-compose.yml:**
    *   **Dockerfile:** Defines *how to build* an image (the recipe). Includes the base image (`FROM openjdk:21`), copied artifacts, and startup commands.
    *   **docker-compose.yml:** Defines *what containers run together* (the party setup). Used for orchestrating multiple connected containers (e.g., Kafka + Zookeeper + UI).
*   **Base Images (`FROM`):** `FROM openjdk:21` does not compile Java; it pulls a pre-built Linux environment that already has the Java runtime installed. Your `app.jar` is added on top.
*   **Registries vs. Artifacts:**
    *   **Source Repository (GitHub, Bitbucket):** Stores code, `pom.xml`, `Dockerfile`.
    *   **Artifact Repository (Artifactory, Nexus):** Stores built binaries (e.g., `app.jar`).
    *   **Docker Registry (Docker Hub, AWS ECR):** Stores built Docker images.

## 5. Java Ecosystem: JVM vs JRE vs JDK

*   **JVM (Java Virtual Machine):** Responsible for executing bytecode (`.class` files) and translating it to machine code.
*   **JRE (Java Runtime Environment):** Historically contained the JVM plus runtime libraries and components needed to run Java applications.
*   **JDK (Java Development Kit):** Contains the compiler (`javac`), developer tools, and the runtime. Used to build applications. (Interview simplification: JDK = Development + Runtime; JVM = Executes Bytecode).
