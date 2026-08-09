# Topic Index

Concept-keyed lookup across all 32 Parts. **Not exhaustive** — covers the ~150 high-leverage / multi-Part concepts. If you don't find a term here, grep the `parts/` folder.

Format: **Topic** → Part NN (row #) — secondary cross-refs.

---

## A

- **ACID** → Part 06 (DB fundamentals)
- **Active-active vs active-passive** → Part 07 (HA), Part 31 (DC-JKT vs AWS-SG)
- **Adapter pattern** → Part 04 (Structural), Part 31 row 9 (vendor abstraction)
- **AtomicInteger / AtomicReference / LongAdder** → Part 02 row 14
- **Anycast (same IP from many locations — CDNs, DNS, Global Accelerator)** → Part 11 row 24
- **ArgoCD sync loop / kustomization.yaml** → Part 22 row 18
- **AOP (Aspect-Oriented Programming)** → Part 03, Part 1b row 20 (Spring proxies)
- **API gateway** → Part 12, Part 13 (AWS API Gateway)
- **API Gateway (AWS) — REST vs HTTP API, throttling, Lambda authorizers** → Part 13
- **API versioning** → Part 12
- **App attestation (Play Integrity, App Attest)** → Part 29 row 73, Part 31 row 30
- **AppConfig (AWS) — feature flags, configuration profiles** → Part 13
- **APCER / BPCER / ACER** → Part 29 row 13
- **@Async + thread-context propagation (MDC, tenant, SecurityContext)** → Part 03, Part 1b row 22
- **AsyncProfiler** → Part 08, Part 25 row 20
- **@Scheduled (fixedRate vs fixedDelay vs cron, TaskScheduler config)** → Part 03, Part 1b row 23
- **Audit logging (authz decisions)** → Part 19
- **Availability, reliability, fault tolerance (HA vs FT, Nines, Sequence vs Parallel)** → Part 07 row 66
- **Audit logs** → Part 21 (observability), Part 28 row 8 (tamper-evident), Part 31 row 54

## B

- **Backpressure** → Part 02 (concurrency), Part 07 (row 60), Part 10 (messaging)
- **BlockingQueue / wait-notify producer-consumer** → Part 02 rows 11, 16
- **Backup & restore** → Part 06, Part 13 (AWS)
- **Back-of-the-envelope estimation / capacity estimation** → Part 08 row 3, Part 30 row 3, `performance/capacity_estimation/`
- **BAC vs PACE** → Part 29 row 8 (passport NFC)
- **BFF (Backend-For-Frontend, service-layer aggregation)** → Part 06 (row 41)
- **Bean lifecycle (Spring)** → Part 03
- **Bean validation (@Valid, @NotNull, custom @Constraint)** → Part 03, Part 1b row 16
- **BeanPostProcessor / @PostConstruct** → Part 03
- **Behavioral interviews / STAR** → Part 30 row 4, PracticeProblems § 4.1
- **Bias monitoring (ML)** → Part 29 row 100
- **Bill Pugh Singleton** → Part 04, PracticeProblems § 3 Q1
- **Bitbucket Pipelines** → Part 22 (CI/CD)
- **Blue-green deployment** → Part 26 row 1
- **Brag doc** → Part 24, Part 30 row 9
- **Broadcast / fanout** → Part 10, Part 29 row 135

## C

- **C4 model** → Part 05 row 7
- **CAP / PACELC** → Part 07
- **Canary release** → Part 26 row 2, Part 29 row 98 (ML)
- **Cassandra** → Part 06 (NoSQL)
- **Cache-aside / read-through / write-through / write-behind** → Part 09
- **@Cacheable / @CacheEvict / @CachePut** → Part 09, Part 1b row 17
- **@Cacheable self-invocation trap** → Part 09
- **Cache key design (tenant-scoped, versioned)** → Part 09
- **Cache stampede + jittered TTL** → Part 09
- **CDC (Change Data Capture)** → Part 10
- **CDN (Content Delivery Network)** → Part 07 row 8
- **@ConfigurationProperties (binding YAML, @Validated)** → Part 03, Part 1b row 12
- **@ControllerAdvice / global exception handling** → Part 03, Part 1b row 11
- **CompletableFuture (thenApply, thenCompose, allOf, exceptionally)** → Part 02 row 7, Part 1b row 14, `java/concurrency/completable_future/`
- **Constructor injection pattern** → Part 04, Part 1b row 8
- **CGNAT (Carrier-Grade NAT)** → Part 11 row 24
- **CDD vs EDD (KYC)** → Part 29 row 5
- **Cell-based architecture** → Part 29 row 138
- **Certificate pinning (mobile)** → Part 31 row 32
- **Chunking (RAG)** → Part 23 row 14, `gen_ai/rag/index.md`
- **Circuit breaker (Resilience4j)** → Part 03, Part 31 row 39
- **Client-Server Communication Patterns** → Part 11 rows 28-30
- **Clustering (heartbeats, leader election, failover — Redis/Kafka)** → Part 07 row 67
- **Compaction (Kafka)** → Part 10
- **Compensating transactions / Saga** → Part 29 row 106, Part 04
- **Compliance reporting** → Part 28, Part 31 row 54
- **Concurrency primitives** → Part 02 (entire Part)
- **Concurrent collections (ConcurrentHashMap, CopyOnWriteArrayList)** → Part 02 row 15, Part 01 row 8
- **Connection pool / HikariCP** → Part 03, Part 08
- **Consistency models** → Part 07
- **Container security (Trivy, Snyk, ECR scanning, distroless, SBOM, Sigstore)** → Part 22
- **Contract testing (Pact)** → Part 25 row 13
- **CORS** → Part 11, Part 18
- **Comparable / Comparator contracts and sorting** → Part 01 row 3
- **Cost Anomaly Detection (AWS)** → Part 14
- **CSRF** → Part 18

## D

- **Data residency (per-tenant)** → Part 28 row 11, Part 29 row 37, Part 31 row 48
- **Database federation (vs sharding, cross-DB joins/transactions, BFF)** → Part 06 (row 41)
- **Database private IPs / hacking methods (Lateral movement, App compromise)** → Part 11 row 15b
- **Datadog** → Part 21, Part 31 (KYC funnel)
- **Deadlock** → Part 02, Part 06
- **Deadlock / livelock / starvation** → Part 02 row 10, Part 27 rows 1-2
- **Deepfakes** → Part 29 row 54
- **Defense-in-depth** → Part 15, Part 16, Part 17, Part 31
- **Design docs** → Part 24 row 1
- **Domain-Driven Design / Bounded Context (strategic DDD, context ≠ DB, DDD → service boundaries)** → Part 07 (row 70), Part 04 (row 59 tactical building blocks)
- **Design thinking process (pain → responsibilities → vary/stay → arrows → skeleton → verify)** → Part 04 row 1, deep_dives/DesignThinkingProcess.md
- **Design review for OO responsibilities (checkout review: discount, payment resolver, entity vs service, shipping policy)** → Part 04 rows 1, 15, 17, 23; design_patterns/pattern_selection/exercise/index.md
- **Device fingerprinting** → Part 29 row 112
- **DHCP & MAC addresses (leases, identity vs location, spoofing)** → Part 11 row 31
- **DI vs DIP** → Part 04, PracticeProblems § 3 Q18
- **Distributed ID generation (UUID vs ticket server vs Snowflake)** → `system_design/concepts/distributed_id_generation/`
- **Distributed locks (Redlock, ZK)** → Part 07, PracticeProblems § 2 #19
- **DNS / DNS-based discovery** → Part 11, Part 13
- **DORA metrics** → Part 26 row 17
- **Double-entry ledger** → Part 29 rows 103, 117
- **Dropwizard metrics / Micrometer** → Part 21

## E

- **ECS / EKS** → Part 13, Part 22
- **EDNS Client Subnet (ECS)** → Part 11 row 11
- **Egress cost optimization (NAT GW, CloudFront, VPC endpoints)** → Part 14
- **eKYC vs in-person** → Part 29 row 4
- **Embeddings (face / text)** → Part 29 row 15, Part 23 row 12, `gen_ai/rag/index.md`
- **Encryption at rest / in transit** → Part 16, Part 28 row 7
- **Enums with behaviour (abstract methods, interface impl)** → Part 04, Part 1b row 19
- **EnvelopeEncryption (KMS)** → Part 16, Part 31 row 21
- **EventBridge** → Part 13 (schema registry, audit / event bus pattern), Part 10
- **EXIF stripping** → Part 31 row 46
- **Expand-contract migration** → Part 26 row 6
- **Exponential backoff + jitter** → Part 07, Part 29 row 33

## F

- **FaceNet / ArcFace** → Part 29 row 15
- **FAR / FRR** → Part 29 row 16, Part 31 row 21
- **FAR / FRR threshold tuning** → Part 29 row 17
- **Feature flags** → Part 26 row 4, Part 29 row 137
- **Feign / Feign interceptors** → Part 03, Part 1b row 10
- **Functional interfaces (Function, Predicate, Consumer, Supplier, BiFunction)** → Part 01 row 14, Part 1b row 13
- **Fingerprinting (device)** → Part 29 row 112
- **FinOps (cost allocation tags, chargeback/showback, Cost Anomaly Detection)** → Part 14
- **Flaky tests** → Part 25
- **Flow type (KYC)** → Part 29 row 36, Part 31 row 17
- **Flyway / Liquibase** → Part 26 row 12
- **Forgery detection (docs)** → Part 29 row 47
- **Funnel metrics (KYC)** → Part 31 row 18

## G

- **Garbage collection (G1, ZGC, Shenandoah)** → Part 01
- **GDPR** → Part 28 rows 1-2
- **GenAI / LLMs** → Part 23 (entire Part — Consolidation only)
- **Global vs distributed cache** → Part 09
- **gp3 vs io2 (RDS storage)** → Part 14
- **gRPC** → Part 11, Part 12
- **GuardDuty (AWS threat detection)** → Part 13

## H

- **HashMap internals** → Part 01
- **Hashing / consistent hashing (virtual nodes)** → Part 07, Part 09
- **Hexagonal / ports & adapters** → Part 04
- **HikariCP** → Part 03, Part 06
- **HMAC-SHA256 (webhook signing)** → Part 20, Part 29 row 31
- **HSTS / CSP / X-Frame-Options** → Part 18
- **HLD interview framework (clarify → high-level design → deep dive → wrap-up)** → Part 30 row 3, `system_design/interview_framework/`
- **HTTP status codes / methods** → Part 11 row 7
- **HTTP/2, HTTP/3 (QUIC)** → Part 11
- **HTTPS handshake** → Part 11 row 7, Part 16
- **HttpInterface / @HttpExchange (Spring 6 declarative client)** → Part 03
- **Hybrid SDK distribution** → Part 29 row 71

## I

- **IAL / AAL (NIST 800-63)** → Part 29 rows 2-3
- **IAM roles / policies / trust relationships / instance profiles** → Part 22, Part 13
- **IANA / RIRs / IP allocation hierarchy (ARIN, RIPE NCC, APNIC, LACNIC, AFRINIC)** → Part 11 row 25
- **ICAO 9303** → Part 29 row 6
- **Idempotency-Key** → Part 12, Part 29 row 64
- **Image quality assessment** → Part 29 row 45, Part 31 row 14
- **iBeta certification** → Part 29 row 50
- **Indexes (B+ tree internals, pages/fanout, clustered vs secondary, dense vs sparse, AUTO_INCREMENT vs UUID)** → Part 06 (row 3)
- **Inflight / draining (LB)** → Part 11, Part 26
- **Injection (SQL, prompt)** → Part 18 (SQL), Part 23 row 25 (prompt)
- **Inspector (AWS — vulnerability assessment)** → Part 13
- **Ingress controller (nginx) / ingressClassName / nginx annotations** → Part 22 row 9
- **IPv4 exhaustion + secondary market (RIR depletion, brokers, legacy /8 blocks)** → Part 11 row 26
- **IP Address Types (Public/Private, Static/Dynamic)** → Part 11 row 15b
- **IPv4 vs IPv6 (32-bit vs 128-bit, dual-stack, where each dominates)** → Part 11 row 14
- **Isolation levels** → Part 06
- **Istio / service mesh** → Part 22

## J

- **Jackson (custom serializers, polymorphic types)** → Part 03, Part 1b row 15
- **JPA N+1 problem + JOIN FETCH / @EntityGraph fix** → Part 03, Part 1b row 21
- **JaCoCo (coverage)** → Part 25 row 19
- **Java Memory Model (JMM)** → Part 02
- **JIT compilation** → Part 01
- **JMeter / Gatling / k6** → Part 25 row 10, Part 08
- **JPA / Hibernate** → Part 03, Part 06
- **JWT** → Part 18, Part 20
- **JWT → GrantedAuthority mapping (Spring Security)** → Part 19

## K

- **KMS (envelope encryption)** → Part 16, Part 31 row 21
- **Kafka (partitioning, ordering, exactly-once)** → Part 10
- **@KafkaListener + DefaultErrorHandler + DLT recoverer** → Part 10
- **Kill switch placement** → Part 26 row 16, Part 31 trick question #3
- **Key-value store (Dynamo/Cassandra-style, quorum, vector clocks, hinted handoff, Merkle repair)** → Part 06 row 11, Part 07 rows 24-26/31, `databases/key_value_store/`
- **KTP / MyKad / NRIC / PhilSys** → Part 29 row 40, Part 31 row 1
- **Kubernetes RBAC + Pod Security Standards** → Part 22
- **KYC orchestration state machine** → Part 29 rows 22-27, Part 31 rows 3, 22-23
- **KYC funnel observability** → Part 31 rows 18-20

## L

- **Lazy initialization** → Part 04 (Singleton)
- **LLD interview framework (requirements → entities → class design → core behavior → verification)** → Part 30 row 2, `system_design/lld_interview_framework/`
- **Liveness (active / passive / PAD)** → Part 29 rows 10-13
- **Load balancer (L4 vs L7, redundancy, draining, consistent hashing)** → Part 07 row 6, Part 11, Part 26
- **Local vs distributed cache** → Part 09
- **Lombok (@Builder, @Value, @Data, @Slf4j, @RequiredArgsConstructor)** → Part 04, Part 1b row 5
- **Long polling** → Part 11 row 30
- **LRU / LFU cache** → PracticeProblems § 1 #7-8
- **LLD Parking Lot** → Part 04 row 44, Part 30 row 2, `system_design/case_studies/parking_lot/`

## M

- **MapStruct (@Mapper, @Mapping, nested mapping)** → Part 1b row 25
- **Macie (AWS S3 PII discovery)** → Part 13
- **Method references (4 forms: static, bound, unbound, constructor)** → Part 01, Part 1b row 3
- **Manual review queue (KYC)** → Part 29 row 70, Part 31 row 59
- **MasterSchedule** → MasterSchedule.md (top-level file, not in a Part)
- **MCP (Model Context Protocol)** → Part 23 row 21
- **Message broker / event streaming (RabbitMQ vs Kafka, commit log, replay, ESB)** → Part 07 (row 11), Part 10 (rows 5, 12)
- **Micrometer Observation API** → Part 03, Part 21
- **mTLS** → Part 16, Part 17, Part 29 row 84
- **Model drift / canary (ML)** → Part 29 rows 98-99
- **Monolith vs microservices (modular monolith, distributed monolith anti-pattern, SOA, cohesion/coupling, when-not-to)** → Part 07 (row 69)
- **MRZ parsing** → Part 29 row 7, Part 31 row 1
- **MVCC** → Part 06

## N

- **Normalization / normal forms (1NF–BCNF, functional/partial/transitive dependencies)** → Part 06 (row 1)
- **N-tier / layered architecture (layer vs tier, closed vs open layers)** → Part 07 (row 68)
- **News feed HLD (fanout on write/read, hybrid, feed cache, post cache)** → Part 07 row 46, `system_design/case_studies/news_feed/`
- **NFC chip reading (e-passport)** → Part 29 row 8, Part 31 row 29
- **NIST FRVT** → Part 29 row 18
- **NIST 800-63 IAL/AAL** → Part 29 rows 2-3
- **Noisy neighbor isolation** → Part 29 row 88
- **Non-linear step ordering (KYC)** → Part 29 row 23
- **Notification system HLD (push, SMS, email, queues, retries, dedupe)** → Part 07 row 53, `system_design/case_studies/notification_system/`
- **Notification dispatch LLD (type vs channel vs provider)** → Part 04 row 61, `system_design/case_studies/notification_lld/`

## O

- **OAuth / OIDC / PKCE** → Part 18
- **Optional (orElse vs orElseGet, map, flatMap, ifPresent)** → Part 01 row 13, Part 1b row 4
- **OCR** → Part 29 row 43
- **Open / closed circuit (resilience)** → Part 07, Part 31 row 39
- **OpenSearch / ELK** → PracticeProblems § 2 #20
- **OpenTelemetry** → Part 21
- **OSI Model (vs TCP/IP, mental framework)** → Part 11 row 1
- **Outbox + CDC** → Part 10, Part 29 row 134, `databases/distributed_transactions/index.md`
- **Outbox pattern / dual-write problem** → Part 04 row 40, Part 10 row 16, `databases/distributed_transactions/index.md`

## P

- **PACE vs BAC (NFC)** → Part 29 row 8
- **Pagination (offset, cursor, keyset)** → Part 12 row 4, `databases/pagination/index.md`
- **Publish-subscribe / point-to-point / fan-out (event vs command)** → Part 07 (row 12), Part 10 (rows 10, 13)
- **Pattern matching in switch (instanceof binding, exhaustive)** → Part 01 row 31, Part 1b rows 24, 27
- **Partial completion (KYC)** → Part 29 row 65, Part 31 row 62
- **PCI-DSS** → Part 28 row 13
- **PDPA (Malaysia)** → Part 28
- **Permission caching + TTL** → Part 19
- **Pre-signed URLs (S3)** → Part 13, Part 31 row 5
- **Pattern selection — Strategy vs Registry vs Spring DI ("one HR, many factories")** → design_patterns/pattern_selection/index.md, Part 04 rows 15, 23
- **Pattern selection scenarios (Singleton / Factory / Builder / Static Factory / Abstract Factory — 25 production scenarios)** → design_patterns/pattern_selection_scenarios/index.md, Part 04 rows 8, 9, 10
- **PaymentProcessorResolver / runtime strategy selection** → Part 04 rows 15, 23; design_patterns/behavioral/strategy_vs_template_method/index.md, design_patterns/pattern_selection/exercise/index.md
- **PEP screening** → Part 29 row 20
- **Polling (Short vs Long)** → Part 11 row 30
- **Polling vs webhook** → Part 29 row 85, Part 31 FAQ #4
- **Promotion criteria** → Part 30 row 6
- **Proxy (forward vs reverse), VPN tunneling, national firewalls** → Part 11 row 32, Part 07 row 7
- **PromQL / Grafana** → Part 21
- **Prompt injection** → Part 23 row 25
- **Proxy pattern** → Part 04, PracticeProblems § 3 Q12

## Q

- **Quality assessment (pre-vendor)** → Part 31 row 14, Part 29 row 45
- **Queue (in-memory / distributed)** → Part 10
- **Quorum (N/W/R, R+W>N, leaderless reads/writes)** → Part 06 (row 38)

## R

- **RAG (retrieval-augmented generation)** → Part 23 rows 12-17, `gen_ai/rag/index.md`
- **Rate limiting (token bucket, sliding window, distributed)** → Part 07 row 13, Part 09, PracticeProblems § 1 #9, § 2 #13
- **RBAC / ABAC / ReBAC** → Part 19
- **Reactor / Project Reactor** → Part 02, Part 03
- **Records as DTOs (compact constructor, validation)** → Part 01 row 31, Part 1b row 6
- **Redis (data structures, eviction, persistence)** → Part 09
- **Redis Cluster (sharding, hash slots, failover)** → Part 09
- **Refactoring smells** → Part 04
- **Regional ID schemes (SEA)** → Part 29 row 40, Part 31 row 1
- **Registry / Service Locator pattern** → design_patterns/pattern_selection/index.md, Part 04 row 23
- **Replay protection (webhook)** → Part 29 row 32
- **REST vs GraphQL vs gRPC (comparison & architecture)** → Part 12 (row 2)
- **RFC 1918 private IP ranges (10/8, 172.16-31/12, 192.168/16)** → Part 11 row 15
- **Read repair / anti-entropy** → Part 06 (row 38)
- **Replica lag** → Part 06, Part 31 row 61
- **Replication (leader/follower, sync/async/semi-sync, failover, multi-leader, leaderless quorum, WAL)** → Part 06 (rows 9, 13, 28, 38)
- **Retry budget** → Part 29 row 66
- **Retry topics pattern / @RetryableTopic** → Part 10
- **Right-to-be-forgotten** → Part 28 row 9, Part 29 cross-q #26
- **Rolling deployment** → Part 26 row 3
- **RSA / ECC / Ed25519** → Part 15

## S

- **Scalability (vertical vs horizontal, active redundancy)** → Part 07 row 19
- **Sequential vs random IO** → Part 07 row 19

- **S3 pre-signed URL** → Part 13, Part 31 row 5
- **Saga / orchestration vs choreography** → PracticeProblems § 3 Q16, Part 29 row 106
- **Sanctions / PEP / adverse media** → Part 29 rows 19-21
- **SBOM (Software Bill of Materials)** → Part 22
- **SDK ↔ backend auth** → Part 29 row 74, Part 31 row 4
- **SDK crash resilience** → Part 31 row 6
- **Security Hub (AWS aggregator)** → Part 13
- **Semantic versioning** → Part 26 row 11
- **Sequence diagram** → Part 05 row 2
- **Server-Sent Events (SSE)** → Part 11 row 29
- **Service discovery** → Part 11
- **Session token (short-lived)** → Part 31 row 4
- **Sharding (database strategies, shard keys, routing/mongos/Vitess, scatter-gather)** → Part 06 (rows 10, 35); **consistent hashing and hot-partition mitigation** → Part 07 (rows 30, 42)
- **Shadow traffic** → Part 26 row 9, Part 31 row 38
- **Side-channel / timing attacks** → Part 15
- **Sigstore / cosign (supply-chain signing)** → Part 22
- **Site-to-Site (S2S) VPN** → Part 13 row 33
- **Short polling** → Part 11 row 30
- **Snyk (image scanning)** → Part 22
- **SNS — topics, subscriptions, fanout, FIFO** → Part 13, Part 10
- **SOC 2** → Part 28 row 14
- **Solr / Elasticsearch** → Part 06
- **SOLID** → Part 04
- **Spring AOP / Proxies** → Part 03
- **Spring Boot actuator** → Part 03, Part 21
- **Spring Security architecture (filter chain, SecurityContext, @PreAuthorize)** → Part 19
- **SQL vs NoSQL (schemaless myth, transactions myth, relational-modeling argument)** → Part 06 row 40
- **SQS — queue types, DLQ, visibility timeout, redrive** → Part 13, Part 10
- **SSE-KMS** → Part 13, Part 16
- **SSH (key-based auth, ssh-agent, ProxyJump, port forwarding)** → Part 22 row 25
- **SSRF protection** → Part 29 row 82
- **State diagram (KYC state machine)** → Part 05 row 3, Part 29 row 22
- **STAR stories** → Part 30 row 4, PracticeProblems § 4.1
- **Strategy vs Template Method** → Part 04 rows 15, 17; design_patterns/behavioral/strategy_vs_template_method/index.md
- **Step Functions (workflows, state types, KYC orchestration fit)** → Part 13
- **Sealed classes (permits, exhaustive switch)** → Part 01 row 31, Part 1b row 27
- **Sticky sessions / session affinity** → Part 11
- **Stream collectors (groupingBy, toMap, joining, partitioningBy)** → Part 01 row 12, Part 1b rows 1, 2
- **Switch expressions (arrow syntax, yield, type patterns)** → Part 01 row 32, Part 1b row 24
- **Streaming SQL / Flink** → Part 10
- **Structured concurrency / virtual threads (Loom)** → Part 02
- **System design framework** → Part 30 row 3

## T

- **TaskDecorator (context propagation)** → Part 03, Part 1b row 22
- **TCP (handshake, congestion, TIME_WAIT)** → Part 11 rows 2-3
- **TCP vs UDP** → Part 11 rows 2, 4
- **Text blocks (Java 15+)** → Part 01 row 32, Part 1b row 26
- **Telemetry (SDK, PII-safe)** → Part 31 row 34
- **TestContainers** → Part 25 row 12
- **Test pyramid** → Part 25 row 1
- **Threshold tuning (FAR/FRR)** → Part 29 row 17
- **TLS 1.2 / 1.3** → Part 16
- **Token bucket / leaky bucket** → Part 09, PracticeProblems § 1 #9
- **Tokenization vs encryption** → Part 28 row 16
- **Tracing (OpenTelemetry, Jaeger)** → Part 21
- **Transactions / distributed commit (states/lifecycle, 2PC blocking, 3PC, Saga/compensating)** → Part 06 (rows 6, 19), Part 07 (rows 37, 39)
- **Trivy (image scanning)** → Part 22
- **Trunk-based development** → Part 26 row 10

## U

- **UML (class / sequence / state / activity)** → Part 05
- **UBO screening** → Part 29 row 129
- **UDP (DNS, VoIP, gaming)** → Part 11 row 4
- **URL shortener HLD (base62, redirects, cache, sharding)** → Part 07 row 44, PracticeProblems § 2 #1, `system_design/case_studies/url_shortener/`

## V

- **Varints / Protobuf encoding** → Part 11 row 13, Part 12 row 2
- **Vector databases (pgvector, Pinecone)** → Part 23 row 13, `gen_ai/rag/index.md`
- **Vendor abstraction / adapter** → Part 31 row 9
- **Vendor failover** → Part 31 row 11
- **Verifiable Credentials (W3C VC)** → Part 29 row 121
- **Virtual threads (Loom)** → Part 02
- **VPC / subnets / NAT / IGW / security groups / NACLs** → Part 13, Part 22

## W

- **Web crawler HLD (URL frontier, politeness, dedup, robots.txt)** → Part 07 row 57, PracticeProblems § 2 #14, `system_design/case_studies/web_crawler/`
- **Webhook signing (HMAC + timestamp + nonce)** → Part 20, Part 29 rows 30-33
- **Webhooks** → Part 11 row 22
- **WebMvcTest / SpringBootTest** → Part 25 row 4
- **WebSockets** → Part 11 row 28
- **var (local type inference, limits)** → Part 01 row 32, Part 1b row 26
- **WebClient (reactive)** → Part 03, Part 1b row 18
- **White-label SDK** → Part 29 row 91, Part 31 row 50
- **WireMock** → Part 25 row 14
- **Write-ahead correlation ID** → Part 31 row 6

## X

- **X.509 cert anatomy** → Part 17

## Y

- **YouTube streaming vs Video calls (TCP vs UDP)** → Part 11 row 4

## Z

- **Zero-downtime deployment** → Part 26 row 1-3
- **Zookeeper** → Part 07

---

## How to maintain this index

- When you finish studying a row in a Part and notice "I'd search for this by a different name," add a row here.
- Cross-references (e.g., `→ Part 16, Part 28 row 7`) point to the *primary* coverage first, then secondary.
- Don't list every row — only concepts you'd search for. Aim for ~150-200 total entries.
- Alphabetize within each letter; spelling matches how you'd type the search.
