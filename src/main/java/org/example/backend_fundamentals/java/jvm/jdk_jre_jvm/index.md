---
order: 10
---

# Deep Dive: JDK, JRE, and JVM

## Overview
Modern macOS does NOT generally come with a full JDK pre-installed; it requires manual installation (e.g., Oracle JDK, Temurin/Adoptium).
* Verify runtime: `java -version`
* Verify compiler: `javac -version`

## Component Breakdown

### JVM (Java Virtual Machine)
* **Definition:** The runtime engine that executes Java bytecode.
* **Flow:** `.class File` → `JVM` → `Machine Instructions` → `CPU`

### JRE (Java Runtime Environment)
* **Definition:** The environment required to *run* Java applications. Includes the JVM and Runtime Libraries.
* **Note:** Modern Java distributions make the separate JRE concept less prominent.

### JDK (Java Development Kit)
* **Definition:** Everything required to *develop* and run Java applications. 
* **Contains:** JVM + Compiler (`javac`) + Standard Java Libraries + Development Tools.

## Standard Java Libraries
Libraries included in the JDK that do not require external dependencies:
* **Collections:** ArrayList, LinkedList, HashMap, HashSet, TreeMap, PriorityQueue
* **Concurrency:** Thread, ExecutorService, CompletableFuture, Semaphore, CountDownLatch
* **Date/Time:** LocalDate, LocalDateTime, Instant, Duration
* **I/O:** File, Files, InputStream, OutputStream, BufferedReader
* **Networking:** Socket, URL, HttpClient
* **Utilities:** Math, Random, UUID, Optional

## End-to-End Execution Flow
```text
Main.java
     ↓
javac (from JDK)
     ↓
Main.class
     ↓
JVM (from JDK)
     ↓
Execution
```
