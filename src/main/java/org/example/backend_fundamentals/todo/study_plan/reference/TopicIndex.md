# Topic Index

Concept-keyed lookup across all 32 Parts. **Not exhaustive** — covers the ~150 high-leverage / multi-Part concepts. If you don't find a term here, grep the `parts/` folder.

Format: **Topic** → Part NN — secondary cross-references.

---

## A

- **ACID** → Part 06 (DB fundamentals)
- **Active-active vs active-passive** → Part 07 (HA), Part 31 (DC-JKT vs AWS-SG)
- **Adapter pattern** → Part 04 (Structural), Part 31 (vendor abstraction)
- **AI agents (tool loop, termination, deterministic-workflow alternative)** → Part 23
- **AtomicInteger / AtomicReference / LongAdder** → Part 02
- **Anycast (same IP from many locations — CDNs, DNS, Global Accelerator)** → Part 11
- **ArgoCD sync loop / kustomization.yaml** → Part 22
- **AOP (Aspect-Oriented Programming)** → Part 03, Part 1b (Spring proxies)
- **API keys (use cases, storage, rotation)** → Part 18
- **API gateway** → Part 12, Part 13 (AWS API Gateway)
- **API Gateway (AWS) — REST vs HTTP API, throttling, Lambda authorizers** → Part 13
- **API versioning** → Part 12
- **App attestation (Play Integrity, App Attest)** → Part 29, Part 31
- **AppConfig (AWS) — feature flags, configuration profiles** → Part 13
- **APCER / BPCER / ACER** → Part 29
- **@Async + thread-context propagation (MDC, tenant, SecurityContext)** → Part 03, Part 1b
- **Async / long-running APIs (`202`, status resource, callbacks)** → Part 12
- **AsyncProfiler** → Part 08, Part 25
- **@Scheduled (fixedRate vs fixedDelay vs cron, TaskScheduler config)** → Part 03, Part 1b
- **Audit logging (authz decisions)** → Part 19
- **Availability, reliability, fault tolerance (HA vs FT, Nines, Sequence vs Parallel)** → Part 07
- **Audit logs** → Part 21 (observability), Part 28 (tamper-evident), Part 31

## B

- **Backpressure** → Part 02 (concurrency), Part 07, Part 10 (messaging)
- **BlockingQueue / wait-notify producer-consumer** → Part 02
- **Backup & restore** → Part 06, Part 13 (AWS)
- **Back-of-the-envelope estimation / capacity estimation** → Part 08, Part 30, `performance/capacity_estimation/`
- **BAC vs PACE** → Part 29 (passport NFC)
- **BFF (Backend-For-Frontend, service-layer aggregation)** → Part 06
- **Bean lifecycle (Spring)** → Part 03
- **Bean validation (@Valid, @NotNull, custom @Constraint)** → Part 03, Part 1b
- **BeanPostProcessor / @PostConstruct** → Part 03
- **Behavioral interviews / STAR** → Part 30, PracticeProblems § 4.1
- **Bias monitoring (ML)** → Part 29
- **Bill Pugh Singleton** → Part 04, PracticeProblems § 3 Q1
- **Bitbucket Pipelines** → Part 22 (CI/CD)
- **Blue-green deployment** → Part 26
- **Brag doc** → Part 24, Part 30
- **Broadcast / fanout** → Part 10, Part 29

## C

- **C4 model** → Part 05
- **CAP / PACELC** → Part 07
- **Canary release** → Part 26, Part 29 (ML)
- **Cassandra** → Part 06 (NoSQL)
- **Cache-aside / read-through / write-through / write-behind** → Part 09
- **Cache consistency and stale-data handling** → Part 09
- **Cache eviction (LRU/LFU baseline; secondary policies)** → Part 09
- **@Cacheable / @CacheEvict / @CachePut** → Part 09, Part 1b
- **@Cacheable self-invocation trap** → Part 09
- **Cache key design (tenant-scoped, versioned)** → Part 09
- **Cache stampede + jittered TTL** → Part 09
- **CDC (Change Data Capture)** → Part 10
- **CDN (Content Delivery Network)** → Part 07
- **@ConfigurationProperties (binding YAML, @Validated)** → Part 03, Part 1b
- **@ControllerAdvice / global exception handling** → Part 03, Part 1b
- **CompletableFuture (thenApply, thenCompose, allOf, exceptionally)** → Part 02, Part 1b, `java/concurrency/completable_future/`
- **Constructor injection pattern** → Part 04, Part 1b
- **CGNAT (Carrier-Grade NAT)** → Part 11
- **CDD vs EDD (KYC)** → Part 29
- **Cell-based architecture** → Part 29
- **Certificate pinning (mobile)** → Part 31
- **Chunking (RAG)** → Part 23, `gen_ai/rag/index.md`
- **Circuit breaker (Resilience4j)** → Part 03, Part 31
- **Client-Server Communication Patterns** → Part 11
- **Clustering (heartbeats, leader election, failover — Redis/Kafka)** → Part 07
- **Compaction (Kafka)** → Part 10
- **Compensating transactions / Saga** → Part 29, Part 04
- **Compliance reporting** → Part 28, Part 31
- **Concurrency primitives** → Part 02 (entire Part)
- **Concurrent collections (ConcurrentHashMap, CopyOnWriteArrayList)** → Part 02, Part 01
- **Connection pool / HikariCP** → Part 03, Part 08
- **Consistency models** → Part 07
- **Container security (Trivy, Snyk, ECR scanning, distroless, SBOM, Sigstore)** → Part 22
- **Contract testing (Pact)** → Part 25
- **CORS** → Part 11, Part 18
- **Comparable / Comparator contracts and sorting** → Part 01
- **Cost Anomaly Detection (AWS)** → Part 14
- **CSRF** → Part 18

## D

- **Data residency (per-tenant)** → Part 28, Part 29, Part 31
- **Database federation (vs sharding, cross-DB joins/transactions, BFF)** → Part 06
- **Database private IPs / hacking methods (Lateral movement, App compromise)** → Part 11
- **Datadog** → Part 21, Part 31 (KYC funnel)
- **Deadlock** → Part 02, Part 06
- **Deadlock / livelock / starvation** → Part 02, Part 27
- **Deepfakes** → Part 29
- **Defense-in-depth** → Part 15, Part 16, Part 17, Part 31
- **Design docs** → Part 24
- **Domain-Driven Design / Bounded Context (strategic DDD, context ≠ DB, DDD → service boundaries)** → Part 07, Part 04 (tactical building blocks)
- **Design thinking process (pain → responsibilities → vary/stay → arrows → skeleton → verify)** → Part 04, deep_dives/DesignThinkingProcess.md
- **Design review for OO responsibilities (checkout review: discount, payment resolver, entity vs service, shipping policy)** → Part 04; design_patterns/pattern_selection/exercise/index.md
- **Device fingerprinting** → Part 29
- **DHCP & MAC addresses (leases, identity vs location, spoofing)** → Part 11
- **DI vs DIP** → Part 04, PracticeProblems § 3 Q18
- **Distributed ID generation (UUID vs ticket server vs Snowflake)** → `system_design/concepts/distributed_id_generation/`
- **Distributed locks (Redlock, ZK)** → Part 07, PracticeProblems § 2 #19
- **DNS / DNS-based discovery** → Part 11, Part 13
- **DORA metrics** → Part 26
- **Double-entry ledger** → Part 29
- **Dropwizard metrics / Micrometer** → Part 21

## E

- **ECS / EKS** → Part 13, Part 22
- **EDNS Client Subnet (ECS)** → Part 11
- **Egress cost optimization (NAT GW, CloudFront, VPC endpoints)** → Part 14
- **eKYC vs in-person** → Part 29
- **Embeddings (face / text)** → Part 29, Part 23, `gen_ai/rag/index.md`
- **Encryption at rest / in transit** → Part 16, Part 28
- **Enums with behaviour (abstract methods, interface impl)** → Part 04, Part 1b
- **EnvelopeEncryption (KMS)** → Part 13, Part 16, Part 31
- **EventBridge** → Part 13 (schema registry, audit / event bus pattern), Part 10
- **EXIF stripping** → Part 31
- **Expand-contract migration** → Part 26
- **Exponential backoff + jitter** → Part 07, Part 29

## F

- **FaceNet / ArcFace** → Part 29
- **FAR / FRR** → Part 29, Part 31
- **FAR / FRR threshold tuning** → Part 29
- **Feature flags** → Part 26, Part 29
- **Feign / Feign interceptors** → Part 03, Part 1b
- **Functional interfaces (Function, Predicate, Consumer, Supplier, BiFunction)** → Part 01, Part 1b
- **Fingerprinting (device)** → Part 29
- **FinOps (cost allocation tags, chargeback/showback, Cost Anomaly Detection)** → Part 14
- **Flaky tests** → Part 25
- **Flow type (KYC)** → Part 29, Part 31
- **Flyway / Liquibase** → Part 26
- **Forgery detection (docs)** → Part 29
- **Function / tool calling (LLMs)** → Part 23
- **Funnel metrics (KYC)** → Part 31

## G

- **Garbage collection (G1, ZGC, Shenandoah)** → Part 01
- **GDPR** → Part 28
- **GenAI / LLMs** → Part 23 (backend baseline in Sprint; advanced depth in Consolidation)
- **Global vs distributed cache** → Part 09
- **gp3 vs io2 (RDS storage)** → Part 14
- **gRPC** → Part 11, Part 12
- **GuardDuty (AWS threat detection)** → Part 13

## H

- **HashMap internals** → Part 01
- **Hashing / consistent hashing (virtual nodes)** → Part 07, Part 09
- **Heap-dump diagnosis (retained heap, retaining owner, common Java leak causes)** → Part 27
- **Hexagonal / ports & adapters** → Part 04
- **HikariCP** → Part 03, Part 06
- **HMAC-SHA256 (webhook signing)** → Part 20, Part 29
- **HSTS / CSP / X-Frame-Options** → Part 18
- **HLD interview framework (clarify → high-level design → deep dive → wrap-up)** → Part 30, `system_design/interview_framework/`
- **Hot key detection and mitigation** → Part 09
- **HTTP status codes / methods** → Part 11
- **HTTP/2, HTTP/3 (QUIC)** → Part 11
- **HTTPS handshake** → Part 11, Part 16
- **HttpInterface / @HttpExchange (Spring 6 declarative client)** → Part 03
- **Hybrid SDK distribution** → Part 29

## I

- **IAL / AAL (NIST 800-63)** → Part 29
- **IAM roles / policies / trust relationships / instance profiles** → Part 22, Part 13
- **IANA / RIRs / IP allocation hierarchy (ARIN, RIPE NCC, APNIC, LACNIC, AFRINIC)** → Part 11
- **ICAO 9303** → Part 29
- **Idempotency-Key** → Part 12, Part 29
- **Image quality assessment** → Part 29, Part 31
- **iBeta certification** → Part 29
- **Indexes (B+ tree internals, pages/fanout, clustered vs secondary, dense vs sparse, AUTO_INCREMENT vs UUID)** → Part 06
- **Inflight / draining (LB)** → Part 11, Part 26
- **Injection (SQL, prompt)** → Part 18 (SQL), Part 23 (prompt)
- **Inspector (AWS — vulnerability assessment)** → Part 13
- **Ingress controller (nginx) / ingressClassName / nginx annotations** → Part 22
- **IPv4 exhaustion + secondary market (RIR depletion, brokers, legacy /8 blocks)** → Part 11
- **IP Address Types (Public/Private, Static/Dynamic)** → Part 11
- **IPv4 vs IPv6 (32-bit vs 128-bit, dual-stack, where each dominates)** → Part 11
- **Isolation levels** → Part 06
- **Istio / service mesh** → Part 22

## J

- **Jackson (custom serializers, polymorphic types)** → Part 03, Part 1b
- **JaCoCo (coverage)** → Part 25
- **Java Memory Model (JMM)** → Part 02
- **JDK vs JRE vs JVM** → Part 01
- **JIT compilation** → Part 01
- **JMeter / Gatling / k6** → Part 25, Part 08
- **JPA / Hibernate** → Part 03, Part 06
- **JPA N+1 problem + JOIN FETCH / @EntityGraph fix** → Part 03, Part 1b
- **JWT** → Part 18, Part 20
- **JWT → GrantedAuthority mapping (Spring Security)** → Part 19

## K

- **KMS (envelope encryption)** → Part 13, Part 16, Part 31
- **Kafka (partitioning, ordering, exactly-once)** → Part 10
- **@KafkaListener + DefaultErrorHandler + DLT recoverer** → Part 10
- **Kill switch placement** → Part 26, Part 31 trick question #3
- **Key-value store (Dynamo/Cassandra-style, quorum, vector clocks, hinted handoff, Merkle repair)** → Part 06, Part 07, `databases/key_value_store/`
- **KTP / MyKad / NRIC / PhilSys** → Part 29, Part 31
- **Kubernetes RBAC + Pod Security Standards** → Part 22
- **KYC orchestration state machine** → Part 29, Part 31
- **KYC funnel observability** → Part 31

## L

- **Lazy initialization** → Part 04 (Singleton)
- **LLD interview framework (requirements → entities → class design → core behavior → verification)** → Part 30, `system_design/lld_interview_framework/`
- **Liveness (active / passive / PAD)** → Part 29
- **Load balancer (L4 vs L7, redundancy, draining, consistent hashing)** → Part 07, Part 11, Part 26
- **Local vs distributed cache** → Part 09
- **Lombok (@Builder, @Value, @Data, @Slf4j, @RequiredArgsConstructor)** → Part 04, Part 1b
- **Long polling** → Part 11
- **LRU / LFU cache** → PracticeProblems § 1 #7-8
- **LLD Parking Lot** → Part 04, Part 30, `system_design/case_studies/parking_lot/`

## M

- **MapStruct (@Mapper, @Mapping, nested mapping)** → Part 1b
- **Macie (AWS S3 PII discovery)** → Part 13
- **Method references (4 forms: static, bound, unbound, constructor)** → Part 01, Part 1b
- **Manual review queue (KYC)** → Part 29, Part 31
- **MasterSchedule** → MasterSchedule.md (top-level file, not in a Part)
- **MCP (Model Context Protocol)** → Part 23
- **Message broker / event streaming (RabbitMQ vs Kafka, commit log, replay, ESB)** → Part 07, Part 10
- **Micrometer Observation API** → Part 03, Part 21
- **mTLS** → Part 16, Part 17, Part 29
- **Model drift / canary (ML)** → Part 29
- **Monolith vs microservices (modular monolith, distributed monolith anti-pattern, SOA, cohesion/coupling, when-not-to)** → Part 07
- **Multi-version deployment compatibility** → Part 26
- **MRZ parsing** → Part 29, Part 31
- **MVCC** → Part 06

## N

- **Normalization / normal forms (1NF–BCNF, functional/partial/transitive dependencies)** → Part 06
- **N-tier / layered architecture (layer vs tier, closed vs open layers)** → Part 07
- **News feed HLD (fanout on write/read, hybrid, feed cache, post cache)** → Part 07, `system_design/case_studies/news_feed/`
- **NFC chip reading (e-passport)** → Part 29, Part 31
- **NIST FRVT** → Part 29
- **NIST 800-63 IAL/AAL** → Part 29
- **Noisy neighbor isolation** → Part 29
- **Non-linear step ordering (KYC)** → Part 29
- **Notification system HLD (push, SMS, email, queues, retries, dedupe)** → Part 07, `system_design/case_studies/notification_system/`
- **Notification dispatch LLD (type vs channel vs provider)** → Part 04, `system_design/case_studies/notification_lld/`

## O

- **OAuth / OIDC / PKCE** → Part 18
- **Optional (orElse vs orElseGet, map, flatMap, ifPresent)** → Part 01, Part 1b
- **OCR** → Part 29
- **Open / closed circuit (resilience)** → Part 07, Part 31
- **OpenSearch / ELK** → PracticeProblems § 2 #20
- **OpenTelemetry** → Part 21
- **OSI Model (vs TCP/IP, mental framework)** → Part 11
- **Outbox + CDC** → Part 10, Part 29, `databases/distributed_transactions/index.md`
- **Outbox pattern / dual-write problem** → Part 04, Part 10, `databases/distributed_transactions/index.md`

## P

- **PACE vs BAC (NFC)** → Part 29
- **Pagination (offset, cursor, keyset)** → Part 12, `databases/pagination/index.md`
- **Publish-subscribe / point-to-point / fan-out (event vs command)** → Part 07, Part 10
- **Pattern matching in switch (instanceof binding, exhaustive)** → Part 01, Part 1b
- **Partial completion (KYC)** → Part 29, Part 31
- **PCI-DSS** → Part 28
- **PDPA (Malaysia)** → Part 28
- **Permission caching + TTL** → Part 19
- **Pre-signed URLs (S3)** → Part 13, Part 31
- **Pattern selection — Strategy vs Registry vs Spring DI ("one HR, many factories")** → design_patterns/pattern_selection/index.md, Part 04
- **Pattern selection scenarios (Singleton / Factory / Builder / Static Factory / Abstract Factory — 25 production scenarios)** → design_patterns/pattern_selection_scenarios/index.md, Part 04
- **PaymentProcessorResolver / runtime strategy selection** → Part 04; design_patterns/behavioral/strategy_vs_template_method/index.md, design_patterns/pattern_selection/exercise/index.md
- **PEP screening** → Part 29
- **Plugins (AI capability packaging)** → Part 23
- **Polling (Short vs Long)** → Part 11
- **Polling vs webhook** → Part 29, Part 31 FAQ #4
- **Promotion criteria** → Part 30
- **Proxy (forward vs reverse), VPN tunneling, national firewalls** → Part 11, Part 07
- **PromQL / Grafana** → Part 21
- **Prompt injection** → Part 23
- **Prompting basics** → Part 23
- **Proxy pattern** → Part 04, PracticeProblems § 3 Q12

## Q

- **Quality assessment (pre-vendor)** → Part 31, Part 29
- **Queue (in-memory / distributed)** → Part 10
- **Quorum (N/W/R, R+W>N, leaderless reads/writes)** → Part 06

## R

- **RAG (retrieval-augmented generation)** → Part 23, `gen_ai/rag/index.md`
- **Rate limiting (token bucket, sliding window, distributed)** → Part 07, Part 09, PracticeProblems § 1 #9, § 2 #13
- **RBAC / ABAC / ReBAC** → Part 19
- **Reactor / Project Reactor** → Part 02, Part 03
- **Records as DTOs (compact constructor, validation)** → Part 01, Part 1b
- **Redis (data structures, eviction, persistence)** → Part 09
- **Redis Cluster (sharding, hash slots, failover)** → Part 09
- **Refactoring smells** → Part 04
- **Regional ID schemes (SEA)** → Part 29, Part 31
- **Registry / Service Locator pattern** → design_patterns/pattern_selection/index.md, Part 04
- **Replay protection (webhook)** → Part 29
- **REST vs GraphQL vs gRPC (comparison & architecture)** → Part 12
- **RFC 1918 private IP ranges (10/8, 172.16-31/12, 192.168/16)** → Part 11
- **Read repair / anti-entropy** → Part 06
- **Replica lag** → Part 06, Part 31
- **Replication (leader/follower, sync/async/semi-sync, failover, multi-leader, leaderless quorum, WAL)** → Part 06
- **Retry budget** → Part 29
- **Retry topics pattern / @RetryableTopic** → Part 10
- **Right-to-be-forgotten** → Part 28, Part 29 cross-q #26
- **Rolling deployment** → Part 26
- **RSA / ECC / Ed25519** → Part 15

## S

- **Scalability (vertical vs horizontal, active redundancy)** → Part 07
- **Sequential vs random IO** → Part 07

- **S3 pre-signed URL** → Part 13, Part 31
- **Saga / orchestration vs choreography** → Part 04, PracticeProblems § 3 Q16, Part 29
- **Sanctions / PEP / adverse media** → Part 29
- **SBOM (Software Bill of Materials)** → Part 22
- **SDK ↔ backend auth** → Part 29, Part 31
- **SDK crash resilience** → Part 31
- **Security Hub (AWS aggregator)** → Part 13
- **Semantic versioning** → Part 26
- **Sequence diagram** → Part 05
- **Server-Sent Events (SSE)** → Part 11
- **Service discovery** → Part 11
- **Session token (short-lived)** → Part 31
- **Sharding (database strategies, shard keys, routing/mongos/Vitess, scatter-gather)** → Part 06; **consistent hashing and hot-partition mitigation** → Part 07
- **Shadow traffic** → Part 26, Part 31
- **Side-channel / timing attacks** → Part 15
- **Sigstore / cosign (supply-chain signing)** → Part 22
- **Site-to-Site (S2S) VPN** → Part 13
- **Short polling** → Part 11
- **Slow-query investigation (`EXPLAIN ANALYZE`, scans, indexes, row estimates, locks)** → Part 27
- **Snyk (image scanning)** → Part 22
- **SNS — topics, subscriptions, fanout, FIFO** → Part 13, Part 10
- **SOC 2** → Part 28
- **Solr / Elasticsearch** → Part 06
- **SOLID** → Part 04
- **Spring AOP / Proxies** → Part 03
- **Spring Boot actuator** → Part 03, Part 21
- **Spring Security architecture (filter chain, SecurityContext, @PreAuthorize)** → Part 19
- **Skills (reusable agent instructions/workflows)** → Part 23
- **SQL vs NoSQL (schemaless myth, transactions myth, relational-modeling argument)** → Part 06
- **SQS — queue types, DLQ, visibility timeout, redrive** → Part 13, Part 10
- **SSE-KMS** → Part 13, Part 16
- **SSH (key-based auth, ssh-agent, ProxyJump, port forwarding)** → Part 22
- **SSRF protection** → Part 29
- **State diagram (KYC state machine)** → Part 05, Part 29
- **STAR stories** → Part 30, PracticeProblems § 4.1
- **Strategy vs Template Method** → Part 04; design_patterns/behavioral/strategy_vs_template_method/index.md
- **Step Functions (workflows, state types, KYC orchestration fit)** → Part 13
- **Sealed classes (permits, exhaustive switch)** → Part 01, Part 1b
- **Sticky sessions / session affinity** → Part 11
- **Stream collectors (groupingBy, toMap, joining, partitioningBy)** → Part 01, Part 1b
- **Switch expressions (arrow syntax, yield, type patterns)** → Part 01, Part 1b
- **Streaming SQL / Flink** → Part 10
- **Structured concurrency / virtual threads (Loom)** → Part 02
- **System design framework** → Part 30

## T

- **TaskDecorator (context propagation)** → Part 03, Part 1b
- **TCP (handshake, congestion, TIME_WAIT)** → Part 11
- **TCP vs UDP** → Part 11
- **Text blocks (Java 15+)** → Part 01, Part 1b
- **Telemetry (SDK, PII-safe)** → Part 31
- **Terraform state (remote backend, encryption, locking)** → Part 22
- **TestContainers** → Part 25
- **Test pyramid** → Part 25
- **Threshold tuning (FAR/FRR)** → Part 29
- **TLS 1.2 / 1.3** → Part 16
- **Token bucket / leaky bucket** → Part 09, PracticeProblems § 1 #9
- **Tokenization vs encryption** → Part 28
- **Top-K heavy hitters / trending counter (windowed aggregation, materialized ranking, local-to-global merge)** → Part 07, `system_design/case_studies/top_k_heavy_hitters/`
- **Timeout at an external-call boundary (unknown outcome, idempotency, status lookup)** → Part 07 (timeouts/retries), Part 31, `system_design/concepts/resilience/`
- **Tracing (OpenTelemetry, Jaeger)** → Part 21
- **Transactions / distributed commit (states/lifecycle, 2PC blocking, 3PC, Saga/compensating)** → Part 06, Part 07
- **Trivy (image scanning)** → Part 22
- **Trunk-based development** → Part 26

## U

- **UML (class / sequence / state / activity)** → Part 05
- **UBO screening** → Part 29
- **UDP (DNS, VoIP, gaming)** → Part 11
- **URL shortener HLD (base62, redirects, cache, sharding)** → Part 07, PracticeProblems § 2 #1, `system_design/case_studies/url_shortener/`

## V

- **Varints / Protobuf encoding** → Part 11, Part 12
- **Vector databases (pgvector, Pinecone)** → Part 23, `gen_ai/rag/index.md`
- **Vendor abstraction / adapter** → Part 31
- **Vendor failover** → Part 31
- **Verifiable Credentials (W3C VC)** → Part 29
- **Virtual threads (Loom)** → Part 02
- **VPC / subnets / NAT / IGW / security groups / NACLs** → Part 13, Part 22

## W

- **Web crawler HLD (URL frontier, politeness, dedup, robots.txt)** → Part 07, PracticeProblems § 2 #14, `system_design/case_studies/web_crawler/`
- **Webhook signing (HMAC + timestamp + nonce)** → Part 20, Part 29
- **Webhooks** → Part 11
- **WebMvcTest / SpringBootTest** → Part 25
- **WebSockets** → Part 11
- **var (local type inference, limits)** → Part 01, Part 1b
- **WebClient (reactive)** → Part 03, Part 1b
- **White-label SDK** → Part 29, Part 31
- **WireMock** → Part 25
- **Write-ahead correlation ID** → Part 31

## X

- **X.509 cert anatomy** → Part 17

## Y

- **YouTube streaming vs Video calls (TCP vs UDP)** → Part 11

## Z

- **Zero-downtime deployment** → Part 26
- **Zookeeper** → Part 07

---

## How to maintain this index

- When you finish studying a topic and notice "I'd search for this by a different name," add an entry here.
- Cross-references (for example, `→ Part 16, Part 28`) point to the *primary* coverage first, then secondary.
- Do not include row numbers because priority changes can reorder Part inventories.
- Do not list every topic — only concepts you would search for. Aim for ~150-200 total entries.
- Alphabetize within each letter; spelling matches how you'd type the search.
