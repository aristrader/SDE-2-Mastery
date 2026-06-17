# Tomcat vs Undertow (and Jetty, and Netty)

Deep-dive into the embedded servlet containers Spring Boot can run — what they are, how their thread models differ, when to pick which, and how they interact with the executor landscape already documented in this repo.

This service runs on **Undertow** (see `thread-pools-and-concurrency.md` — "Incoming HTTP Request (Undertow worker thread)"). This doc covers why, what the alternatives look like, and what the tradeoffs actually are.

Related docs:
- `thread-pools-and-concurrency.md` — pool inventory, monitoring.
- `executor-and-pool-sizing.md` — how to size thread pools.
- `async-executor-and-shutdown.md` — graceful shutdown for `@Async`.

---

## 1. What an embedded servlet container *is*

A **servlet container** is the thing that:
1. Opens a TCP listen socket on a port (e.g., 8080).
2. Accepts incoming connections.
3. Parses HTTP bytes into a `HttpServletRequest`.
4. Dispatches that request to your `Servlet` (in Spring Boot, the `DispatcherServlet`).
5. Takes your `HttpServletResponse` and writes HTTP bytes back.
6. Manages the lifecycle of all the above (startup, shutdown, thread pools, timeouts).

Before Spring Boot, you deployed a `.war` into a container like Tomcat that ran separately. Spring Boot **embeds** the container — your application is a jar that starts its own Tomcat/Undertow/Jetty instance in-process. Same HTTP semantics, different deployment model.

Spring Boot ships three options out of the box:
- **Tomcat** (default) — Apache Software Foundation, 1999-present, the reference implementation many teams default to.
- **Undertow** — JBoss/Red Hat, XNIO-based, historically positioned as "lightweight and fast."
- **Jetty** — Eclipse Foundation, long history, strong HTTP/2 and WebSocket support.

Plus a fourth option that's *not* a servlet container:
- **Netty** — via Spring WebFlux (reactive), not Spring MVC. Different programming model entirely.

---

## 2. The thread model — where they really differ

The HTTP stack has two fundamentally different architectures. Understanding this is the key to understanding all three containers.

### Model A: Thread-per-request (blocking I/O)

```
[TCP accept] ──→ hand socket to a worker thread ──→ worker reads request,
                                                    runs DispatcherServlet,
                                                    writes response,
                                                    closes/keeps socket,
                                                    returns to pool.
```

One worker thread per in-flight HTTP request. If you have 200 concurrent requests, you need 200 worker threads. Each thread blocks on `socket.read()` and `socket.write()`.

Pros: simple to reason about. Your code is blocking, stack traces are readable, thread-locals work.

Cons: max concurrency = worker pool size. Each thread costs ~1 MB of stack. 10K concurrent slow clients = 10K threads = 10 GB.

### Model B: Event loop (non-blocking I/O, NIO)

```
[Selector thread] ──→ watches N sockets for readiness events
     │
     ├─ socket 17 is readable → read some bytes, parse partial request
     ├─ socket 22 is writable → flush buffered response bytes
     └─ socket 88 has new connection → accept, register for read events
```

A small number of "selector" or "I/O" threads (usually `cores` of them) handle thousands of sockets non-blocking, by multiplexing via `epoll` (Linux) / `kqueue` (BSD) / IOCP (Windows). When there's *work* to do — parsed full request, need to call your handler — the work is handed off to a worker thread.

Pros: handles huge connection counts with tiny thread counts. Slow clients don't consume threads.

Cons: programming model gets harder if you expose it directly (callback hell, backpressure).

### The middle ground: NIO + worker threads

All modern embedded containers use **NIO for the socket layer** (so slow clients don't hold threads), then hand off parsed requests to a **worker thread pool** that runs your blocking servlet code. This is the "best of both worlds": event loop for the transport, thread-per-request for the application.

All three containers do this. The differences are in *how* they wire the two halves together and how configurable each piece is.

---

## 3. Tomcat — how it works

### Architecture

```
Acceptor thread (1-2)            ← accepts new TCP connections
    │
    ▼
Poller thread(s) (NIO selector)  ← watches sockets for I/O readiness
    │ (request fully read)
    ▼
Worker thread pool               ← runs DispatcherServlet
    │
    ▼
Your @Controller method
```

Tomcat's connector types:
- **NIO** (`Http11NioProtocol`) — default since Tomcat 8.5. Pollers + worker pool. The normal choice.
- **NIO2** (`Http11Nio2Protocol`) — uses `AsynchronousSocketChannel`. Slightly different event plumbing; rarely gives measurable benefit.
- **APR** (Apache Portable Runtime) — native OS libraries. Faster TLS; being phased out.
- **BIO** — blocking I/O. Removed in Tomcat 8.5+.

### Thread model

Two pools, visible in thread dumps:

- **Acceptor** thread(s): `http-nio-8080-Acceptor`. Count: usually 1.
- **Worker** threads: `http-nio-8080-exec-1`, `-exec-2`, etc. Count: configurable, default 200 max / 10 min-spare.

Key configuration (Spring Boot):

```properties
server.tomcat.threads.max=200                  # max worker threads
server.tomcat.threads.min-spare=10             # always-idle workers
server.tomcat.max-connections=8192             # max accepted TCP connections
server.tomcat.accept-count=100                 # queue for accepted-but-not-handled
server.tomcat.connection-timeout=20000         # ms before idle close
```

Flow:
- Tomcat accepts up to `max-connections` TCP connections (via NIO — they don't each need a thread).
- Each *active* request (one that's being handled right now) occupies one worker thread.
- If all 200 workers are busy, new requests queue up to `accept-count=100`.
- Beyond that, new connections are refused at the OS level.

### Defaults are generous

Tomcat's 200-thread default is aggressive. Many services never need that many — but it "just works" for most workloads, which is why it's the Spring Boot default. Sizing guidance from `executor-and-pool-sizing.md` applies directly: for I/O-bound workloads, cap it by downstream (DB pool, external rate limits).

---

## 4. Undertow — how it works

### Architecture

```
XNIO worker
    ├─ I/O threads (event loops)       ← socket accept + read + write, non-blocking
    │       │
    │       ▼ (when a request is parsed)
    └─ Worker threads (blocking pool)  ← runs DispatcherServlet
            │
            ▼
        Your @Controller method
```

Undertow is built on **XNIO**, a JBoss low-level I/O library. Two explicit pools, separated by role:

- **I/O threads** — handle all NIO operations. Count: `Math.max(availableProcessors(), 2)` by default. Small. These must never block — they handle thousands of sockets.
- **Worker threads** — run your blocking servlet code. Count: `I/O threads × 8` by default (e.g., 16 I/O threads → 128 worker threads).

This separation is more explicit than Tomcat's, though functionally similar. You'll see both pools in thread dumps:
- `XNIO-1 I/O-1`, `XNIO-1 I/O-2`, ... — I/O threads.
- `XNIO-1 task-1`, `XNIO-1 task-2`, ... — worker threads.

### Configuration in Spring Boot

```properties
server.undertow.threads.io=16          # I/O threads (event loops)
server.undertow.threads.worker=128     # worker thread pool (blocking)
server.undertow.buffer-size=1024       # per-connection buffer size
server.undertow.direct-buffers=true    # use off-heap buffers
```

### What made Undertow notable

- **Low memory footprint** — smaller heap usage per idle connection than Tomcat.
- **Explicit I/O/worker split** — clearer mental model, easier to tune for specific workloads.
- **Native HTTP/2** — had HTTP/2 support earlier than Tomcat.
- **High raw throughput** in benchmarks — often 5–15% faster than Tomcat for simple handlers, though the gap narrows or disappears for real-world Spring MVC apps where the app is the bottleneck.

### Current status (as of 2024-2026)

JBoss/Red Hat moved WildFly away from Undertow's stewardship in a reduced-priority mode. Undertow still works, still ships in Spring Boot 3.x, still gets security fixes — but active feature development has slowed. For new projects in 2025+, Tomcat or Jetty are often the safer long-term bets unless you have a specific reason to use Undertow.

For this service, we're on Undertow and it works well — no urgency to migrate. Worth being aware of, though, when making infrastructure decisions.

---

## 5. Jetty — the third option

### Architecture

Similar hybrid model: selector threads for NIO, worker threads for blocking application code. Pool sizing is automatic by default — Jetty tunes itself based on CPU count and load.

```
Acceptor threads
    │
    ▼
Selector threads (NIO)
    │
    ▼
QueuedThreadPool (workers)
    │
    ▼
Your @Controller method
```

### Configuration in Spring Boot

```properties
server.jetty.threads.max=200
server.jetty.threads.min=8
server.jetty.threads.acceptors=-1        # auto: depends on CPU count
server.jetty.threads.selectors=-1        # auto
```

### Why pick Jetty

- **Best HTTP/2 and WebSocket support** historically.
- **Strong alignment with Eclipse Foundation / modern Jakarta EE**.
- **Used heavily by Google infrastructure** (GCP, Android build tools) — proven at scale.
- **Competitive with Tomcat** on most benchmarks.

Jetty feels like "Tomcat done differently" — the big decision points rarely hinge on Jetty-specific features for a typical Spring Boot app.

---

## 6. Netty — the reactive outlier

Netty is not a servlet container. It's a **low-level asynchronous networking framework**. Spring WebFlux uses Netty as its default server.

Thread model: small event-loop pool (usually `cores × 2`). All request handling, all response writing, all business logic runs on event-loop threads — which must **never block**.

Pros: can handle tens of thousands of concurrent connections with ~16 threads. Excellent for streaming, long-lived connections, many-slow-clients scenarios.

Cons: you must write reactive code (`Mono`, `Flux`). Blocking inside a handler poisons the event loop. Debugging is harder (no linear stack traces). Entirely different programming model.

**Not the same tradeoff space** as Tomcat/Undertow/Jetty — you pick Netty only when you're committing to reactive programming.

---

## 7. Head-to-head comparison

| Aspect | Tomcat | Undertow | Jetty | Netty (WebFlux) |
|---|---|---|---|---|
| **Model** | NIO + blocking workers | XNIO + blocking workers | NIO + blocking workers | Pure reactive event loop |
| **Spring Boot default** | ✅ | — | — | ✅ for WebFlux |
| **Thread pool for handlers** | 1 pool (workers) | 2 pools (I/O + workers) | 1 pool (QueuedThreadPool) | Event loop only |
| **Default worker count** | 200 | ~128 (cores × 8) | 200 | ~16 (cores × 2) |
| **Memory per idle connection** | ~30 KB | ~10-15 KB | ~20 KB | ~5 KB |
| **HTTP/2 support** | ✅ | ✅ | ✅ (strong) | ✅ |
| **HTTP/3 support** | Experimental | No active work | Experimental | ✅ (via netty-incubator) |
| **Virtual threads (Java 21+)** | ✅ (3.2+ via `spring.threads.virtual.enabled`) | ✅ | ✅ | N/A (already non-blocking) |
| **Active development** | Very active | Reduced | Active | Very active |
| **Complexity of tuning** | Simple | Moderate (two pools) | Simple-ish | High |
| **Best for** | Default choice, broad ecosystem | Low-memory deployments | HTTP/2 / WebSocket heavy | High-concurrency reactive |

### Performance in practice

Microbenchmarks (Techempower, etc.) show all three blocking containers within ~15% of each other. In production Spring Boot apps, the **container is almost never the bottleneck** — your DB, external HTTP calls, or business logic are. Swapping Tomcat for Undertow almost never moves the P99 needle.

**Pick for operational reasons** (ecosystem, memory footprint, team familiarity), not for marginal throughput differences.

---

## 8. Switching containers in Spring Boot

One line in `pom.xml`. Exclude the default starter's Tomcat, include the replacement starter:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

No code changes. The same `@RestController` runs on any of them.

---

## 9. Virtual threads and the containers (Java 21+)

Spring Boot 3.2+ supports virtual threads via:

```properties
spring.threads.virtual.enabled=true
```

What this changes per container:

- **Tomcat** — the worker pool becomes a `VirtualThreadPerTaskExecutor`. Each request runs on its own virtual thread. The 200-thread cap is gone; scale limited only by memory and downstream.
- **Undertow** — same idea; worker thread pool is replaced by virtual-thread-per-task.
- **Jetty** — same.
- **Netty** — not applicable (WebFlux doesn't use blocking worker threads in the first place).

The implication: **with virtual threads, the container's worker pool sizing stops mattering much.** You can accept 50,000 concurrent requests on a Tomcat pod without tuning `server.tomcat.threads.max`. The practical limit becomes DB connections, external service rate limits, and memory — not thread count.

Caveats from `executor-deep-dive.md` §6 still apply:
- `synchronized` blocks pin virtual threads to carriers. Heavy synchronized use defeats the benefit. (Fixed in JDK 24.)
- Thread-local caches sized for "200 threads" now see 50,000 copies — memory bloat.
- DB connection pool (10 connections) will get exhausted near-instantly at high concurrency unless you add a `Semaphore` gate.

For this service: the stage executor and audit executor are separate executors *beyond* the container's worker pool. Switching the container to virtual threads doesn't automatically make them virtual — they're explicit `ThreadPoolExecutor`s. You'd flip them separately.

---

## 10. Where the container sits in this service

```
Client HTTP request
    ↓
Undertow XNIO I/O thread      ← reads bytes, parses HTTP, accepts connection
    ↓ (hand off parsed request)
Undertow worker thread         ← runs DispatcherServlet
    ↓
VerifyApiController.verify()
    ↓
VerifyApiService (on same worker thread)
    ↓
StageExecutor.execute(...)
    ├─→ traceableExecutorService (separate 16-thread pool)
    ├─→ taskExecutor (separate 4-8 thread pool, @Async audit)
    └─→ ... 
    ↓ (all stages complete)
Worker thread resumes, builds response
    ↓
Undertow XNIO I/O thread       ← writes bytes back to socket
```

Four distinct thread pools involved:
1. **XNIO I/O threads** (Undertow, small, non-blocking socket handling).
2. **Undertow worker threads** (handle servlet logic — initial controller + response assembly).
3. **`traceableExecutorService`** (16 threads — fan out verification stages in parallel).
4. **`taskExecutor`** (4-8 threads — `@Async` audit work).

The `thread-pools-and-concurrency.md` inventory was from the perspective of the application pools. The container pools are additional — they're the entry point.

---

## 11. Tuning and monitoring per container

### Tomcat

Metrics (via Spring Boot Actuator + Micrometer):
- `tomcat.threads.busy` — active workers.
- `tomcat.threads.current` — total pool size.
- `tomcat.threads.config.max` — configured max.
- `tomcat.sessions.*` — session metrics (if you use sessions).

Signs of trouble:
- `busy` pinned at `max` → pool saturated, requests queueing or rejected.
- Slow downstream + high `busy` → downstream is really the problem.

### Undertow

Undertow exposes fewer metrics out of the box. Use JMX or the per-pool `executor.*` metrics if you can get the thread pool beans. Common gauges:
- Worker pool size and active count (from thread dumps or Actuator).
- XNIO I/O thread count (fixed at startup).

Watch in thread dumps:
- Many `XNIO-*-task-*` threads in `RUNNABLE` or `TIMED_WAITING` on I/O → normal under load.
- All workers blocked on DB connection acquisition → downstream bottleneck.

### Jetty

- `jetty.threads.busy`, `.config.min`, `.config.max`.
- `jetty.threads.jobs` — queued jobs (backlog).

---

## 12. Common questions & deep-dive topics

### Q: Does swapping Tomcat for Undertow make my app faster?

Usually no. The container is rarely the bottleneck for a Spring Boot app doing real work (DB queries, external HTTP calls). You might see 5-15% on synthetic "hello world" benchmarks; on real workloads, usually unmeasurable.

**Switch for operational reasons** (memory footprint, ecosystem fit, specific feature), not for generic "performance."

### Q: How do I know how many worker threads my container is using right now?

Thread dump (`jcmd <pid> Thread.print`). Count threads matching the container's naming pattern:
- Tomcat: `http-nio-8080-exec-*`
- Undertow: `XNIO-*-task-*`
- Jetty: `qtp*`

Or use Actuator metrics (see §11).

### Q: My requests are timing out under load. Is my pool too small?

Sequence:
1. Check `tomcat.threads.busy` (or equivalent). At max? Pool is saturated.
2. Check thread dump: what are the workers *doing*? If they're all blocked on `HikariPool.getConnection()`, the DB pool is the bottleneck — more worker threads won't help.
3. If threads are running CPU-heavy code, you're CPU-bound — scaling workers just adds context-switching.
4. If they're waiting on external HTTP calls, scale workers up (but also check those services aren't rate-limiting you).

See `executor-and-pool-sizing.md` for the full decision tree.

### Q: What's `max-connections` vs `max-threads`?

- **`max-connections`** (Tomcat-specific term) — max accepted TCP connections. NIO means the container can hold many connections open with few threads.
- **`max-threads`** (worker threads) — max concurrent *requests being actively processed*.

A slow client holding a connection open doesn't consume a thread. Only an *active request* consumes a thread. So `max-connections` can be much larger than `max-threads` — that's the whole point of NIO.

### Q: Should I use `accept-count` to queue requests, or reject early?

Queueing helps smooth transient bursts. Rejecting early gives faster feedback to the client (503 retry now vs timeout later).

For user-facing APIs: small `accept-count` (10-50), fast rejection, client retries with backoff.
For internal services with retry-capable clients: larger `accept-count` (100-500), absorb bursts.

### Q: Connection keep-alive and threads

HTTP keep-alive keeps the TCP connection open across requests. With NIO, this doesn't consume a worker thread between requests — only the connection slot (`max-connections`). So keep-alive is nearly free in terms of threads. It saves TCP handshake cost on the next request.

### Q: How does HTTPS / TLS interact with the container?

TLS adds CPU cost (handshake = asymmetric crypto, bulk = symmetric). Tomcat and Undertow both do TLS termination on the I/O thread. For very high TLS traffic, offloading to a separate proxy (nginx, Envoy) or using native acceleration (Tomcat APR, JDK 21+ AES-GCM intrinsics) can help. Rarely the bottleneck for typical Spring Boot apps.

### Q: Can I run two containers simultaneously (different ports)?

Yes, but it's unusual. Easier pattern: one container with two connectors (Tomcat supports multiple `<Connector>` elements, Undertow supports multiple listeners). Spring Boot Actuator does this — the management endpoint can run on a separate port via `management.server.port`.

### Q: What about HTTP/2 and HTTP/3?

- **HTTP/2**: supported by all three (Tomcat, Undertow, Jetty). Multiplexing means many requests share one TCP connection — reduces connection count, keeps the thread-per-request model.
- **HTTP/3 (QUIC/UDP)**: experimental in most containers. Netty leads. If you need HTTP/3, go reactive.

### Q: How does graceful shutdown work per container?

Spring Boot 2.3+ has `server.shutdown=graceful`:
1. Stop accepting new connections.
2. Let in-flight requests complete up to `spring.lifecycle.timeout-per-shutdown-phase` (default 30s).
3. Force-close after timeout.

All three containers support this via Spring Boot's unified abstraction. Confirms with log lines like `Commencing graceful shutdown. Waiting for active requests to complete`.

### Q: What happens when the container's pool saturates but the `traceableExecutorService` is idle?

Bad news: the request never reaches the stage executor, because there's no Undertow worker to call it. The bottleneck is at the container, not the downstream pool.

This is a common misdiagnosis — someone looks at `traceableExecutorService` metrics, sees idle threads, and wonders why throughput is low. Answer: Undertow workers are all busy waiting on something, so no new requests get dispatched to the stage executor at all.

### Q: Does `virtualThreadPerTaskExecutor` eliminate the need for the stage executor?

Not automatically. Switching Undertow workers to virtual threads means the *container layer* scales. But the stage executor is an explicit fan-out pattern — multiple stages run in parallel per request. Even with virtual worker threads, you'd still want a separate executor (virtual or otherwise) to manage the fan-out, context propagation, and backpressure.

However: if worker threads are virtual, the stage executor could also be `newVirtualThreadPerTaskExecutor()`, removing the 16-thread cap. Tradeoff as in `executor-deep-dive.md` §6 — worth measuring.

### Q: What if I want to serve both servlet and reactive routes?

Possible but awkward. Spring Boot supports either Spring MVC (servlet) *or* WebFlux (reactive) as the primary stack. You can embed a separate reactive endpoint via a sidecar or a separate Spring context, but the ergonomics aren't great. Usually: pick one.

---

## 13. Mental model

- A **servlet container** = TCP listen socket + HTTP parser + thread pool + servlet dispatcher.
- All three (Tomcat, Undertow, Jetty) use **NIO for sockets + blocking worker pool for handlers**. The differences are mostly naming, pool structure, and defaults.
- **Netty (WebFlux)** is a different paradigm — everything runs on an event loop, no per-request thread.
- **Pick by operational fit**, not by microbenchmarks. Most Spring Boot bottlenecks are in the app, not the container.
- **Virtual threads (3.2+)** largely eliminate worker-pool sizing concerns, but don't change anything about the I/O layer (which was already non-blocking).
- This service runs **Undertow**. It works. No strong reason to change until the larger ecosystem question (active development) forces a migration.

---

## 14. Follow-up topics (for future deep-dives)

- **Undertow's XNIO API** — how the event loop plumbing actually works under the hood.
- **Tomcat's `NioEndpoint` internals** — Acceptor/Poller/Worker handoff mechanics.
- **HTTP/2 multiplexing** — how one TCP connection carrying 100 streams interacts with the thread-per-request model.
- **Keep-alive tuning** — timeouts, max requests per connection, impact on pooling.
- **TLS tuning** — session resumption, OCSP stapling, cipher preference.
- **Access logging** — each container's built-in access log format and how to route to a file or stdout.
- **Spring Boot Actuator and container metrics** — which metrics are auto-exposed, which need manual wiring.
- **Behind a load balancer / proxy** — X-Forwarded-For handling, `server.forward-headers-strategy`, trust boundaries.
- **WebSocket handling** — per-container upgrade paths, idle timeouts, thread attribution.
- **Reactive server in a non-reactive app** — using WebClient from a Tomcat-based service without swapping to WebFlux.
- **Java 21 virtual threads + container** — benchmarks, pinning, thread-local memory implications.
- **Graceful shutdown edge cases** — long-running requests, streaming responses, in-flight `@Async`.
- **Container security hardening** — header limits, URI length limits, connection rate limits, timeout tuning.
