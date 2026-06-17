# What is Netty (and why our fat-jar has 30 of its JARs)

## Short answer

Netty is the asynchronous network framework that sits underneath almost
every HTTP client and server in modern Java. We don't import it
directly, but four major libraries we depend on use Netty for their
networking, so it ends up on our classpath transitively.

It implements:

- TCP / UDP socket handling (non-blocking, one event loop thread for
  thousands of connections)
- HTTP/1.1, HTTP/2, WebSocket
- TLS/SSL
- Native Linux event loops (`epoll`) via JNI for high-throughput I/O
- DNS resolution
- A long list of protocol codecs (Redis RESP, MQTT, SMTP, STOMP,
  Memcache, LZ4/Brotli/Snappy compression, ...)

If you've heard of it: gRPC's Java implementation, Cassandra driver,
Elasticsearch, Akka HTTP, Spring WebFlux's HTTP client, Vert.x — all
built on Netty.

## How it gets onto our classpath

Four paths, only the first two are actively exercised:

| Library we use | What pulls Netty in | Used at runtime? |
|---|---|---|
| **Spring WebFlux + WebClient** (`spring-boot-starter-webflux`) | Reactor Netty (Spring's reactive HTTP client) is built on Netty | **Yes** — every WebClient call to VIDA / KeyCloak / Tijori |
| **Spring Data Redis** (Lettuce) | Lettuce is built on Netty, talks RESP over TCP | **Yes** — front-side cache reads/writes |
| AWS SDK v2 | Bundles `netty-nio-client` as a default option even when you don't use it | No — we use `url-connection-client` (pure Java) |
| Kafka producer | Doesn't use Netty directly, but transitive deps drag in Netty utilities | No |

That's why our fat-jar has ~30 `netty-*` JARs even though no source
file imports `io.netty.*`. Maven resolution is transitive; everyone
depends on Netty; it all lands in our build.

## What lives in those 30 JARs

Three layers:

1. **Foundation** — `netty-common`, `netty-buffer`, `netty-transport`,
   `netty-resolver`, `netty-resolver-dns`. Event-loop machinery,
   `ByteBuf` memory pool, async DNS.
2. **Codecs** — `netty-codec`, `netty-codec-http`, `netty-codec-http2`,
   `netty-codec-redis`, plus a dozen others for protocols we don't
   speak (`mqtt`, `smtp`, `stomp`, `memcache`, `xml`, ...). Each codec
   parses bytes off the wire into structured messages.
3. **Native transports** — `netty-transport-native-epoll` (Linux),
   `netty-transport-native-kqueue` (macOS/BSD). Per-platform JARs
   (`linux-x86_64`, `linux-aarch_64`, `osx-x86_64`, `osx-aarch_64`)
   loaded at runtime if the OS matches; otherwise the portable Java
   path is used.

The codecs we don't speak (MQTT, SMTP, STOMP, etc.) are on the
classpath as JARs but their classes are never loaded. They contribute
to scanner CVE noise without being a real attack surface.

## Where Netty actually runs in our service

Walking through one verify request:

1. **Inbound HTTP arrives at Undertow.** Undertow is *not* Netty-based
   — it's Red Hat's XNIO server. So inbound traffic never touches
   Netty.
2. **WebClient calls to VIDA / KeyCloak / Tijori from verification
   stages.** This is where Netty runs:
   - DNS lookup via `netty-resolver-dns`
   - TCP + TLS via `netty-handler` (SSLHandler)
   - HTTP framing via `netty-codec-http` / `netty-codec-http2`
   - Event loop via `netty-transport-native-epoll` on Linux
3. **Lettuce calls to Redis** for the front-side cache:
   - TCP via `netty-transport`
   - RESP via `netty-codec-redis`
4. **AWS S3 / STS / Kinesis** — goes through `url-connection-client`
   (pure Java), not Netty.
5. **Kafka publish** — Kafka client's own NIO layer, not Netty.

So Netty's actual job in our service is: outgoing HTTP via WebClient,
and Redis via Lettuce. That's it. Everything else from Netty's
ecosystem ships in our image but never executes.

## Why Netty has a lot of CVEs

Three structural reasons:

1. **It parses untrusted bytes for a living.** Every decoder class
   takes attacker-controllable input and turns it into structured
   data. Parsers are historically the largest source of security bugs
   in any language ecosystem. Netty's surface is huge: HTTP/1.1,
   HTTP/2, DNS, RESP, MQTT, multiple compression formats. Every parser
   is a CVE target.
2. **It runs at the network edge.** A Netty bug bypasses
   application-level input validation entirely. A request-smuggling
   bug in `netty-codec-http` lets an attacker fool the service into
   treating two requests as one, no matter how careful the controller
   validation is.
3. **Everyone uses it.** Cloudflare, AWS, Spring, Cassandra,
   Elasticsearch — all built on Netty. Researchers actively look for
   bugs because the impact is huge. The flip side: fixes ship fast
   because the project has dedicated maintainers and a strong security
   process.

## What this means for us

- We can't fix Netty bugs in our code; we wait for upstream patches.
- We override the `<netty.version>` Spring Boot ships with, often, so
  we can pick up patches faster than the Spring Boot release cadence.
- **Most CVEs don't apply to our exercised surface** — e.g., a CVE in
  `netty-codec-mqtt` doesn't matter because we don't speak MQTT. But
  scanners flag it because the JAR is on the classpath. That's the
  source of the false-positive noise we sometimes chase.
- Real Netty CVEs are exploitable directly from the public internet
  (since Netty handles bytes the moment they arrive). When one drops,
  bump quickly.

## Related

- `dependency-resolution-and-cve-verification.md` — playbook for
  diagnosing scanner findings against Netty (and any other dep).
- `tomcat-vs-undertow.md` — why our inbound serving uses Undertow
  (not Netty).
- `tech-debt-todo.md` — running list of pending CVE bumps.
