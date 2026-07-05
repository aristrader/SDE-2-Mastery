---
order: 50
---

# Spring, WebFlux, and the Threading Model

## Frameworks, Libraries, and Dependencies
*   **Library:** Reusable code that *you* call (e.g., Jackson, Guava, Lombok).
*   **Framework:** Controls application flow and *calls your code* (Inversion of Control).
*   **Dependency:** The packaging mechanism (Maven/Gradle) used to bring code into your project. A dependency bundle (like a Spring Boot Starter) can contain frameworks, libraries, and servers.

## Spring Boot Request Lifecycle & Tomcat
*   **Misconception:** Everything goes through the main thread; the main thread receives requests and spawns sub-threads.
*   **Correction:** The main thread's only job is to start the Spring context and the embedded Tomcat server. After startup, the main thread essentially sleeps. HTTP requests are processed entirely by **Tomcat worker threads**.
*   **Misconception:** Controller is the servlet.
*   **Correction:** A Servlet is a Java component that receives HTTP requests and generates responses. The `DispatcherServlet` is the actual front-door servlet in Spring MVC. The Controller is just business logic built on top of it.

### The True Spring MVC Pipeline
1.  **OS:** Reserves Port 8080 (a port is just a number; sockets are the active connections inside it).
2.  **Tomcat:** Accepts the request on the socket and assigns it to a free **Worker Thread** from its pool (e.g., Thread 17 out of 200).
3.  **DispatcherServlet:** The worker thread enters Spring via the DispatcherServlet, which routes the request.
4.  **Business Logic:** Controller → Service → Repository → DB.
5.  **Response:** The response is returned, and Thread 17 goes back to the Tomcat pool.

## Blocking I/O vs. Thread Pools
In traditional Spring MVC (Blocking I/O), when a thread sends a DB query, it **BLOCKS** and waits. It cannot execute another request during this time.
*   **The Wait Problem:** If 200 Tomcat threads are all blocked waiting for the database, request #201 must wait in the queue. The CPU might be idle, but the application is stalled because the *thread pool* is exhausted.

## Non-Blocking I/O (WebFlux, Netty, Event Loop)
**Motivation:** Instead of blocking a thread while waiting for the DB, what if the thread could send the query and immediately go process another request?

### How it Works (Event Loop)
1.  **Request arrives** → Event Loop thread takes it.
2.  **Start DB Query** → Event Loop saves the request state and moves on to another request.
3.  **DB Finishes (Event)** → Event Loop is notified, retrieves the saved state, and resumes processing the response.
*   **Result:** A small number of threads (e.g., 8 threads on an 8-core CPU) can handle thousands of concurrent requests because they *never block*.

### The Reactive Stack Layering
*   **Java NIO** (OS-level non-blocking I/O)
*   **Netty** (Independent networking framework built on NIO; uses the Event Loop)
*   **Project Reactor** (Provides `Mono` and `Flux`—reactive streams APIs, conceptually similar to `CompletableFuture` but for a web framework)
*   **Spring WebFlux** (The reactive web framework built on top of Reactor and Netty)

### Crucial WebFlux Realizations
*   **Misconception:** Non-blocking I/O is faster and reduces request latency.
*   **Correction:** WebFlux does **NOT** make the database or network faster. Latency remains the same. It improves *thread utilization, scalability, and concurrency*.
*   **Misconception:** WebFlux solves database bottlenecks.
*   **Correction:** WebFlux frees up *application threads*, but the database still has a limited connection pool. The bottleneck simply moves to the database. WebFlux shines when dealing with many concurrent requests that spend most of their time waiting (API calls, Redis, Kafka).

## Quick recall
**Q. Does a Spring Boot application need `starter-web` to run?**
A. No. Without it, you just don't have an embedded Tomcat server (no HTTP listener). It can still run Kafka consumers or scheduled jobs perfectly fine.

**Q. Why not just use a massive thread pool instead of WebFlux?**
A. Context switching overhead. Too many threads thrash the CPU (saving/loading registers) and consume excessive stack memory. Thread pool sizing is always empirical (trial and error based on CPU vs I/O time).

**Q. In WebFlux, does the same thread that started a request finish it?**
A. Not necessarily. Thread A might start the DB query, and Thread B might resume the request when the DB response event fires.

**Q. Mono vs Flux in one sentence?**
A. Mono is a promise to produce zero or one value later; Flux is a promise to produce multiple values over time.
