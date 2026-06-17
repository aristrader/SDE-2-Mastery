# Notion Layout — Page Map

Live view of the Notion structure. Only pages that are **published or ready to publish** appear here — held/todo docs are excluded. Update this file whenever a doc is created, updated, or moved to Notion.

**Legend:**
- `✅` — page exists in Notion and is in sync with the local doc
- `📋` — doc is ready to paste into Notion (first time — page not yet created in Notion)
- `🔄` — page exists in Notion but the **local doc has been updated since** — needs re-paste to sync
- `🔲` — page exists in Notion but is empty / a stub
- `⬜` — placeholder for a section that doesn't exist yet in Notion

**Status flow:** new doc → `📋` → (user pastes) → `✅` → (local update) → `🔄` → (user re-pastes) → `✅`. Status only moves forward to `✅` when the user explicitly confirms Notion has been updated.

---

```
BACKEND FUNDAMENTALS  ✅  (root page)
│  Note: "Read design patterns first, then HLD and LLD"
│  Reference: Effective Java by Joshua Bloch
│
├── JAVA  ✅
│   ├── Nested / Inner Classes in Java  ✅
│   │     backend_fundamentals/java/nested_classes/NestedClassDemo.java
│   ├── Java Foundations  ✅
│       ├── Access Modifiers Deep Dive  ✅
│       │     backend_fundamentals/java/foundations/access_modifiers/AccessModifiersDeepDive.md
│       ├── Hashing  ✅
│       │     backend_fundamentals/java/foundations/hashing/Hashing.md
│       ├── Streams  📋
│       │     backend_fundamentals/java/foundations/streams/Streams.md
│       ├── Abstract Class vs Interface  📋
│       │     backend_fundamentals/java/foundations/abstract_class_vs_interface/AbstractClassVsInterface.md
│       ├── Object Model  📋
│       │     backend_fundamentals/java/foundations/object_model/ObjectModel.md
│       ├── String Immutability  📋
│       │     backend_fundamentals/java/foundations/string/StringImmutability.md
│       ├── Generics  📋
│       │     backend_fundamentals/java/foundations/generics/Generics.md
│       ├── equals / hashCode / Comparable / Comparator  📋
│       │     backend_fundamentals/java/foundations/equals_hashcode/EqualsHashCode.md
│       ├── Exception Handling  📋
│       │     backend_fundamentals/java/foundations/exceptions/ExceptionHandling.md
│       ├── Memory Areas  📋
│       │     backend_fundamentals/java/foundations/memory_areas/MemoryAreas.md
│       ├── Garbage Collection  📋
│       │     backend_fundamentals/java/foundations/gc/GarbageCollection.md
│       ├── HashMap Internals  📋
│       │     backend_fundamentals/java/foundations/hashmap/HashMap.md
│       ├── ConcurrentHashMap  📋
│       │     backend_fundamentals/java/foundations/concurrent_hashmap/ConcurrentHashMap.md
│       ├── LinkedHashMap  📋
│       │     backend_fundamentals/java/foundations/linked_hashmap/LinkedHashMap.md
│       ├── TreeMap  📋
│       │     backend_fundamentals/java/foundations/treemap/TreeMap.md
│       └── Collections  ✅
│           ├── Lists  🔄
│           │     backend_fundamentals/java/foundations/collections/lists/Lists.md
│           ├── Maps  🔄
│           │     backend_fundamentals/java/foundations/collections/maps/Maps.md
│           └── Sets  🔄
│                 backend_fundamentals/java/foundations/collections/sets/Sets.md
│   └── Concurrency  ⬜  (content docs ready — no Notion page yet)
│       ├── Thread Lifecycle  📋
│       │     backend_fundamentals/java/concurrency/thread_lifecycle/ThreadLifecycle.md
│       ├── Race Conditions  📋
│       │     backend_fundamentals/java/concurrency/race_conditions/RaceConditions.md
│       ├── Java Memory Model  📋
│       │     backend_fundamentals/java/concurrency/jmm/JavaMemoryModel.md
│       ├── synchronized  📋
│       │     backend_fundamentals/java/concurrency/synchronized_keyword/Synchronized.md
│       ├── volatile  📋
│       │     backend_fundamentals/java/concurrency/volatile_keyword/Volatile.md
│       ├── ExecutorService & ThreadPoolExecutor  📋
│       │     backend_fundamentals/java/concurrency/executor_service/ExecutorService.md
│       ├── CompletableFuture  📋
│       │     backend_fundamentals/java/concurrency/completable_future/CompletableFuture.md
│       └── Locks  📋
│             backend_fundamentals/java/concurrency/locks/Locks.md
│   └── Coding Fluency  ⬜  (content docs ready — no Notion pages yet)
│       ├── Streams Core  📋
│       │     backend_fundamentals/java/coding_fluency/streams/StreamsCore.md
│       ├── Stream Collectors  📋
│       │     backend_fundamentals/java/coding_fluency/streams/StreamCollectors.md
│       ├── Method References  📋
│       │     backend_fundamentals/java/coding_fluency/streams/MethodReferences.md
│       ├── Optional  📋
│       │     backend_fundamentals/java/coding_fluency/optional/Optional.md
│       ├── Lombok  📋
│       │     backend_fundamentals/java/coding_fluency/lombok/Lombok.md
│       ├── Records  📋
│       │     backend_fundamentals/java/coding_fluency/records/Records.md
│       ├── Immutable Collections  📋
│       │     backend_fundamentals/java/coding_fluency/immutable_collections/ImmutableCollections.md
│       ├── Constructor Injection  📋
│       │     backend_fundamentals/java/coding_fluency/spring_basics/ConstructorInjection.md
│       ├── JPA Entity & Repository  📋
│       │     backend_fundamentals/java/coding_fluency/spring_basics/JpaEntity.md
│       ├── Feign Client  📋
│       │     backend_fundamentals/java/coding_fluency/spring_basics/FeignClient.md
│       ├── Global Exception Handling  📋
│       │     backend_fundamentals/java/coding_fluency/spring_basics/GlobalExceptionHandling.md
│       └── Configuration Properties  📋
│             backend_fundamentals/java/coding_fluency/spring_basics/ConfigurationProperties.md
│
├── SPRING BOOT  ⬜  (content docs ready — no Notion pages yet)
│   ├── IoC Container  📋
│   │     backend_fundamentals/spring/ioc_container/IoCContainer.md
│   ├── Dependency Injection  📋
│   │     backend_fundamentals/spring/dependency_injection/DependencyInjection.md
│   ├── Bean Lifecycle  📋
│   │     backend_fundamentals/spring/bean_lifecycle/BeanLifecycle.md
│   ├── Auto-Configuration  📋
│   │     backend_fundamentals/spring/auto_configuration/AutoConfiguration.md
│   ├── Starter Ecosystem  📋
│   │     backend_fundamentals/spring/starters/StarterEcosystem.md
│   ├── Actuator  📋
│   │     backend_fundamentals/spring/actuator/Actuator.md
│   ├── Spring MVC  📋
│   │     backend_fundamentals/spring/mvc/SpringMVC.md
│   ├── Spring REST  📋
│   │     backend_fundamentals/spring/rest/SpringRest.md
│   ├── Exception Handling  📋
│   │     backend_fundamentals/spring/exception_handling/ExceptionHandling.md
│   ├── JPA Repository  📋
│   │     backend_fundamentals/spring/jpa_repository/JpaRepository.md
│   ├── Derived Queries  📋
│   │     backend_fundamentals/spring/derived_queries/DerivedQueries.md
│   ├── Transactions  📋
│   │     backend_fundamentals/spring/transactions/Transactions.md
│   ├── Entity Lifecycle  📋
│   │     backend_fundamentals/spring/entity_lifecycle/EntityLifecycle.md
│   ├── Feign Client  📋
│   │     backend_fundamentals/spring/feign/Feign.md
│   ├── Security  ⬜
│   │   ├── Security Filter Chain  📋
│   │   │     backend_fundamentals/spring/security/SecurityFilterChain.md
│   │   ├── Authentication Providers  📋
│   │   │     backend_fundamentals/spring/security/AuthenticationProviders.md
│   │   ├── Authorization  📋
│   │   │     backend_fundamentals/spring/security/Authorization.md
│   │   ├── OAuth2 Resource Server  📋
│   │   │     backend_fundamentals/spring/security/OAuth2ResourceServer.md
│   │   └── JWT Validation  📋
│   │         backend_fundamentals/spring/security/JwtValidation.md
│   ├── Spring Caching  📋
│   │     backend_fundamentals/spring/caching/SpringCaching.md
│   ├── Jackson Customization  📋
│   │     backend_fundamentals/spring/jackson/JacksonCustomization.md
│   ├── Async & MDC  📋
│   │     backend_fundamentals/spring/async_mdc/AsyncMdc.md
│   └── Lazy / Eager & N+1  📋
│         backend_fundamentals/spring/jpa_lazy_eager/LazyEagerN1.md
│
├── SYSTEM DESIGN  ✅
│   ├── DESIGN PATTERNS  ✅
│       │
│       ├── CreationalPatternsRoadmap  🔄
│       │     backend_fundamentals/design_patterns/creational/CreationalPatternsRoadmap.md
│       │
│       ├── CreationalPatternsQuickRef  ✅
│       │     backend_fundamentals/design_patterns/creational/CreationalPatternsQuickRef.md
│       │
│       ├── Singleton  ✅
│       │     backend_fundamentals/design_patterns/creational/singleton/Singleton.md
│       │
│       ├── Factory  🔄
│       │     backend_fundamentals/design_patterns/creational/factory/Factory.md  (overview page)
│       │   ├── SimpleFactory  🔄
│       │   │     backend_fundamentals/design_patterns/creational/factory/simple_factory/SimpleFactory.md
│       │   ├── FactoryMethodBasic  🔄
│       │   │     backend_fundamentals/design_patterns/creational/factory/factory_method_basic/FactoryMethodBasic.md
│       │   └── FactoryMethodProd  🔄
│       │         backend_fundamentals/design_patterns/creational/factory/factory_method/FactoryMethodProd.md
│       │
│       ├── Builder  ✅
│       │     backend_fundamentals/design_patterns/creational/builder/Builder.md  (overview page)
│       │   ├── BuilderBasic  ✅
│       │   │     backend_fundamentals/design_patterns/creational/builder/simple_builder/BuilderBasic.md
│       │   ├── BuilderLombok  ✅
│       │   │     backend_fundamentals/design_patterns/creational/builder/lombok_builder/BuilderLombok.md
│       │   ├── BuilderDirector  ✅
│       │   │     backend_fundamentals/design_patterns/creational/builder/director_builder/BuilderDirector.md
│       │   └── BuilderDirectorGof  ✅
│       │         backend_fundamentals/design_patterns/creational/builder/director_builder_gof/BuilderDirectorGof.md
│       │
│       ├── StaticFactoryMethods  ✅
│       │     backend_fundamentals/design_patterns/creational/static_factory_methods/StaticFactoryMethods.md
│       │
│       ├── AbstractFactory  ✅
│       │     backend_fundamentals/design_patterns/creational/abstract_factory/AbstractFactory.md
│       │
│       ├── Prototype  ✅
│       │     backend_fundamentals/design_patterns/creational/prototype/Prototype.md
│       │
│       ├── Foundations  ⬜  (in progress)
│       │   ├── OOP Pillars  ⬜  (all four content docs ready)
│       │   │   ├── Encapsulation  📋
│       │   │   │     backend_fundamentals/design_patterns/foundations/oop_pillars/encapsulation/Encapsulation.md
│       │   │   ├── Polymorphism  📋
│       │   │   │     backend_fundamentals/design_patterns/foundations/oop_pillars/polymorphism/Polymorphism.md
│       │   │   ├── Abstraction  📋
│       │   │   │     backend_fundamentals/design_patterns/foundations/oop_pillars/abstraction/Abstraction.md
│       │   │   ├── Inheritance  📋
│       │   │   │     backend_fundamentals/design_patterns/foundations/oop_pillars/inheritance/Inheritance.md
│       │   │   └── OOP Pillar Confusions  📋
│       │   │         backend_fundamentals/design_patterns/foundations/oop_pillars/OopPillarConfusions.md
│       │   ├── SOLID Principles  📋
│       │   │     backend_fundamentals/design_patterns/foundations/solid/SolidPrinciples.md
│       │   ├── Supporting Principles  📋
│       │   │     backend_fundamentals/design_patterns/foundations/supporting_principles/SupportingPrinciples.md
│       │   ├── Coupling, Cohesion & Code Smells  📋
│       │   │     backend_fundamentals/design_patterns/foundations/coupling_cohesion_smells/CouplingCohesionSmells.md
│       │   └── DIP vs DI  📋
│       │         backend_fundamentals/design_patterns/foundations/dip_vs_di/DipVsDi.md
│       │
│       ├── ⬜ Structural Patterns  (not started — Adapter, Decorator, Facade,
│       │         Composite, Proxy, Bridge, Flyweight)
│       │
│       └── ⬜ Behavioural Patterns  (not started — Strategy, Observer, State,
│               Command, Template Method, Iterator, Chain of Responsibility,
│               Visitor, Mediator, Memento, Interpreter)
│   │
│   ├── Load Balancing  📋
│   │     backend_fundamentals/system_design/load_balancing/LoadBalancing.md
│   ├── Clustering  📋
│   │     backend_fundamentals/system_design/clustering/Clustering.md
│   ├── Caching & Distributed Cache  📋
│   │     backend_fundamentals/system_design/caching/CachingAndDistributedCache.md
│   ├── CDN  📋
│   │     backend_fundamentals/system_design/cdn/Cdn.md
│   └── Availability, Reliability & Fault Tolerance  📋
│         backend_fundamentals/system_design/availability/AvailabilityReliabilityFaultTolerance.md
│
├── NETWORKING  ⬜  (content docs ready — no Notion page yet)
│   ├── IP Addressing, NAT, DHCP & MAC  📋
│   │     backend_fundamentals/networking/ip_addressing/IpAddressingNatDhcp.md
│   ├── TCP vs UDP  📋
│   │     backend_fundamentals/networking/tcp_vs_udp/TcpVsUdp.md
│   ├── DNS — Resolution & Traffic Steering  📋
│   │     backend_fundamentals/networking/dns/DnsResolution.md
│   └── Proxies, VPNs & National Firewalls  📋
│         backend_fundamentals/networking/proxies_vpn/ProxiesVpnFirewalls.md
│
├── DATABASES  ⬜  (content docs ready — no Notion page yet)
│   ├── SQL vs NoSQL  📋
│   │     backend_fundamentals/databases/sql_vs_nosql/SqlVsNosql.md
│   └── Graph Databases & GraphQL  📋
│         backend_fundamentals/databases/graph_and_graphql/GraphDbAndGraphQl.md
│
└── WEB RELATED CONCEPTS  🔲  [blank — no content yet]
```

---

## 📋 / 🔄 Needs Notion sync

Anything not currently `✅`. Pages in `📋` need a first paste; pages in `🔄` need a re-paste because the local file has been updated since the Notion version was created.

| Doc | Status | Notion destination | Note |
| --- | --- | --- | --- |
| Streams | 📋 | JAVA → Java Foundations → Streams (new sibling page under Java Foundations) | First paste |
| Abstract Class vs Interface | 📋 | JAVA → Java Foundations → Abstract Class vs Interface | First paste |
| Exception Handling | 📋 | JAVA → Java Foundations → Exception Handling | First paste |
| Memory Areas | 📋 | JAVA → Java Foundations → Memory Areas | First paste |
| Garbage Collection | 📋 | JAVA → Java Foundations → Garbage Collection | First paste |
| HashMap Internals | 📋 | JAVA → Java Foundations → HashMap Internals | First paste |
| ConcurrentHashMap | 📋 | JAVA → Java Foundations → ConcurrentHashMap | First paste |
| LinkedHashMap | 📋 | JAVA → Java Foundations → LinkedHashMap | First paste |
| TreeMap | 📋 | JAVA → Java Foundations → TreeMap | First paste |
| Thread Lifecycle | 📋 | JAVA → Concurrency → Thread Lifecycle (new section under JAVA) | First paste — opens the Concurrency branch |
| Race Conditions | 📋 | JAVA → Concurrency → Race Conditions | First paste |
| Java Memory Model | 📋 | JAVA → Concurrency → Java Memory Model | First paste |
| synchronized | 📋 | JAVA → Concurrency → synchronized | First paste |
| volatile | 📋 | JAVA → Concurrency → volatile | First paste |
| ExecutorService & ThreadPoolExecutor | 📋 | JAVA → Concurrency → ExecutorService & ThreadPoolExecutor | First paste |
| CompletableFuture | 📋 | JAVA → Concurrency → CompletableFuture | First paste |
| Locks | 📋 | JAVA → Concurrency → Locks | First paste — completes the Concurrency set |
| Encapsulation | 📋 | DESIGN PATTERNS → Foundations → OOP Pillars → Encapsulation (new sub-tree) | First paste — opens the Foundations branch under DESIGN PATTERNS |
| Polymorphism | 📋 | DESIGN PATTERNS → Foundations → OOP Pillars → Polymorphism | First paste |
| Abstraction | 📋 | DESIGN PATTERNS → Foundations → OOP Pillars → Abstraction | First paste |
| Inheritance | 📋 | DESIGN PATTERNS → Foundations → OOP Pillars → Inheritance | First paste — completes the four-pillar set |
| SOLID Principles | 📋 | DESIGN PATTERNS → Foundations → SOLID Principles (sibling of OOP Pillars) | First paste |
| Supporting Principles | 📋 | DESIGN PATTERNS → Foundations → Supporting Principles (sibling of SOLID Principles) | First paste |
| Coupling, Cohesion & Code Smells | 📋 | DESIGN PATTERNS → Foundations → Coupling, Cohesion & Code Smells | First paste |
| DIP vs DI | 📋 | DESIGN PATTERNS → Foundations → DIP vs DI | First paste |
| CreationalPatternsRoadmap | 🔄 | DESIGN PATTERNS → CreationalPatternsRoadmap | Re-paste — cross-ref paths updated after `DesignThinkingProcess.md` / `designpatternQuestions.md` moved to `todo/study_plan/deep_dives/` (+ new `PatternSelectionScenarios.md` link) |
| Factory (overview) | 🔄 | DESIGN PATTERNS → Factory | Re-paste — Related-files paths updated to `todo/study_plan/deep_dives/DesignThinkingProcess.md` and the foundations / java foundations paths |
| SimpleFactory | 🔄 | DESIGN PATTERNS → Factory → SimpleFactory | Re-paste — DesignThinkingProcess path updated |
| FactoryMethodBasic | 🔄 | DESIGN PATTERNS → Factory → FactoryMethodBasic | Re-paste — DesignThinkingProcess + AccessModifiers paths updated |
| FactoryMethodProd | 🔄 | DESIGN PATTERNS → Factory → FactoryMethodProd | Re-paste — DesignThinkingProcess + AccessModifiers paths updated |
| Lists | 🔄 | JAVA → Java Foundations → Collections → Lists | Re-paste — `Done when` checklist converted to `Quick recall` Q&A during cleanup sweep |
| Maps | 🔄 | JAVA → Java Foundations → Collections → Maps | Re-paste — `Done when` checklist converted to `Quick recall` Q&A during cleanup sweep |
| Sets | 🔄 | JAVA → Java Foundations → Collections → Sets | Re-paste — `Done when` checklist converted to `Quick recall` Q&A during cleanup sweep |
| IoC Container | 📋 | SPRING BOOT → IoC Container (new section) | First paste — opens the Spring Boot branch |
| Dependency Injection | 📋 | SPRING BOOT → Dependency Injection | First paste |
| Bean Lifecycle | 📋 | SPRING BOOT → Bean Lifecycle | First paste |
| Auto-Configuration | 📋 | SPRING BOOT → Auto-Configuration | First paste |
| Starter Ecosystem | 📋 | SPRING BOOT → Starter Ecosystem | First paste |
| Actuator | 📋 | SPRING BOOT → Actuator | First paste |
| Spring MVC | 📋 | SPRING BOOT → Spring MVC | First paste |
| Spring REST | 📋 | SPRING BOOT → Spring REST | First paste |
| Exception Handling (Spring) | 📋 | SPRING BOOT → Exception Handling | First paste |
| JPA Repository | 📋 | SPRING BOOT → JPA Repository | First paste |
| Derived Queries | 📋 | SPRING BOOT → Derived Queries | First paste |
| Transactions | 📋 | SPRING BOOT → Transactions | First paste |
| Entity Lifecycle | 📋 | SPRING BOOT → Entity Lifecycle | First paste |
| Feign Client (Spring) | 📋 | SPRING BOOT → Feign Client | First paste |
| Security Filter Chain | 📋 | SPRING BOOT → Security → Security Filter Chain | First paste — opens the Security sub-section |
| Authentication Providers | 📋 | SPRING BOOT → Security → Authentication Providers | First paste |
| Authorization | 📋 | SPRING BOOT → Security → Authorization | First paste |
| OAuth2 Resource Server | 📋 | SPRING BOOT → Security → OAuth2 Resource Server | First paste |
| JWT Validation | 📋 | SPRING BOOT → Security → JWT Validation | First paste — completes the Security sub-section |
| Spring Caching | 📋 | SPRING BOOT → Spring Caching | First paste |
| Jackson Customization | 📋 | SPRING BOOT → Jackson Customization | First paste |
| Async & MDC | 📋 | SPRING BOOT → Async & MDC | First paste |
| Lazy / Eager & N+1 | 📋 | SPRING BOOT → Lazy / Eager & N+1 | First paste — completes the Spring Boot set. Content updated from DB ChatGPT thread (gotchas: circular-ref vs N+1, LAZY-default reasoning, 2nd-level cache) |
| Streams Core | 📋 | JAVA → Coding Fluency → Streams Core (new section) | First paste — opens the Coding Fluency branch |
| Stream Collectors | 📋 | JAVA → Coding Fluency → Stream Collectors | First paste |
| Method References | 📋 | JAVA → Coding Fluency → Method References | First paste |
| Optional | 📋 | JAVA → Coding Fluency → Optional | First paste |
| Lombok | 📋 | JAVA → Coding Fluency → Lombok | First paste |
| Records | 📋 | JAVA → Coding Fluency → Records | First paste |
| Immutable Collections | 📋 | JAVA → Coding Fluency → Immutable Collections | First paste |
| Constructor Injection | 📋 | JAVA → Coding Fluency → Constructor Injection | First paste |
| JPA Entity & Repository | 📋 | JAVA → Coding Fluency → JPA Entity & Repository | First paste |
| Feign Client (Coding Fluency) | 📋 | JAVA → Coding Fluency → Feign Client | First paste |
| Global Exception Handling | 📋 | JAVA → Coding Fluency → Global Exception Handling | First paste |
| Configuration Properties | 📋 | JAVA → Coding Fluency → Configuration Properties | First paste — completes the Coding Fluency set |
| IP Addressing, NAT, DHCP & MAC | 📋 | NETWORKING → IP Addressing, NAT, DHCP & MAC (new top-level section) | First paste — opens the NETWORKING branch |
| TCP vs UDP | 📋 | NETWORKING → TCP vs UDP | First paste |
| DNS — Resolution & Traffic Steering | 📋 | NETWORKING → DNS — Resolution & Traffic Steering | First paste — content expanded with second DNS thread (hierarchy, TTL trade-offs, root-hints) |
| Proxies, VPNs & National Firewalls | 📋 | NETWORKING → Proxies, VPNs & National Firewalls | First paste |
| Load Balancing | 📋 | SYSTEM DESIGN → Load Balancing (new section) | First paste — opens HLD docs under SYSTEM DESIGN |
| Clustering | 📋 | SYSTEM DESIGN → Clustering | First paste |
| Caching & Distributed Cache | 📋 | SYSTEM DESIGN → Caching & Distributed Cache | First paste |
| CDN | 📋 | SYSTEM DESIGN → CDN | First paste |
| Availability, Reliability & Fault Tolerance | 📋 | SYSTEM DESIGN → Availability, Reliability & Fault Tolerance | First paste |
| SQL vs NoSQL | 📋 | DATABASES → SQL vs NoSQL (new top-level section) | First paste — opens the DATABASES branch |
| Graph Databases & GraphQL | 📋 | DATABASES → Graph Databases & GraphQL | First paste |

---

## What gets added here next

**Java Foundations** is in place under JAVA. **Concurrency** (8 docs, all 📋) and **Coding Fluency** (12 docs, all 📋) are open under JAVA. **SPRING BOOT** now has 23 docs ready (all 📋). A **Foundations** section under DESIGN PATTERNS is opening — Encapsulation is its first page (📋). Future siblings: Abstraction, Polymorphism, Inheritance, then SOLID and supporting design principles. A **NETWORKING** top-level section is opening with 4 pages (IP Addressing, TCP vs UDP, DNS, Proxies/VPNs — all 📋). **SYSTEM DESIGN** now also has 5 HLD pages ready (Load Balancing, Clustering, Caching & Distributed Cache, CDN, Availability/Reliability/FT — all 📋). Future siblings come from the ChatGPT doc-import flow (Part 7/9/11 topics).

When structural patterns begin, add child pages under `DESIGN PATTERNS → Structural Patterns`.

---

## Source map (quick lookup)

| Notion page | Source file |
| --- | --- |
| CreationalPatternsRoadmap | `backend_fundamentals/design_patterns/creational/CreationalPatternsRoadmap.md` |
| CreationalPatternsQuickRef | `backend_fundamentals/design_patterns/creational/CreationalPatternsQuickRef.md` |
| Singleton | `backend_fundamentals/design_patterns/creational/singleton/Singleton.md` |
| Factory (overview) | `backend_fundamentals/design_patterns/creational/factory/Factory.md` |
| SimpleFactory | `backend_fundamentals/design_patterns/creational/factory/simple_factory/SimpleFactory.md` |
| FactoryMethodBasic | `backend_fundamentals/design_patterns/creational/factory/factory_method_basic/FactoryMethodBasic.md` |
| FactoryMethodProd | `backend_fundamentals/design_patterns/creational/factory/factory_method/FactoryMethodProd.md` |
| Builder (overview) | `backend_fundamentals/design_patterns/creational/builder/Builder.md` |
| BuilderBasic | `backend_fundamentals/design_patterns/creational/builder/simple_builder/BuilderBasic.md` |
| BuilderLombok | `backend_fundamentals/design_patterns/creational/builder/lombok_builder/BuilderLombok.md` |
| BuilderDirector | `backend_fundamentals/design_patterns/creational/builder/director_builder/BuilderDirector.md` |
| BuilderDirectorGof | `backend_fundamentals/design_patterns/creational/builder/director_builder_gof/BuilderDirectorGof.md` |
| StaticFactoryMethods | `backend_fundamentals/design_patterns/creational/static_factory_methods/StaticFactoryMethods.md` |
| AbstractFactory | `backend_fundamentals/design_patterns/creational/abstract_factory/AbstractFactory.md` |
| Prototype | `backend_fundamentals/design_patterns/creational/prototype/Prototype.md` |
| Encapsulation | `backend_fundamentals/design_patterns/foundations/oop_pillars/encapsulation/Encapsulation.md` |
| Polymorphism | `backend_fundamentals/design_patterns/foundations/oop_pillars/polymorphism/Polymorphism.md` |
| Abstraction | `backend_fundamentals/design_patterns/foundations/oop_pillars/abstraction/Abstraction.md` |
| Inheritance | `backend_fundamentals/design_patterns/foundations/oop_pillars/inheritance/Inheritance.md` |
| SOLID Principles | `backend_fundamentals/design_patterns/foundations/solid/SolidPrinciples.md` |
| Supporting Principles | `backend_fundamentals/design_patterns/foundations/supporting_principles/SupportingPrinciples.md` |
| Coupling, Cohesion & Code Smells | `backend_fundamentals/design_patterns/foundations/coupling_cohesion_smells/CouplingCohesionSmells.md` |
| DIP vs DI | `backend_fundamentals/design_patterns/foundations/dip_vs_di/DipVsDi.md` |
| Access Modifiers Deep Dive | `backend_fundamentals/java/foundations/access_modifiers/AccessModifiersDeepDive.md` |
| Hashing | `backend_fundamentals/java/foundations/hashing/Hashing.md` |
| Lists | `backend_fundamentals/java/foundations/collections/lists/Lists.md` |
| Maps | `backend_fundamentals/java/foundations/collections/maps/Maps.md` |
| Sets | `backend_fundamentals/java/foundations/collections/sets/Sets.md` |
| Streams | `backend_fundamentals/java/foundations/streams/Streams.md` |
| Abstract Class vs Interface | `backend_fundamentals/java/foundations/abstract_class_vs_interface/AbstractClassVsInterface.md` |
| Exception Handling | `backend_fundamentals/java/foundations/exceptions/ExceptionHandling.md` |
| Memory Areas | `backend_fundamentals/java/foundations/memory_areas/MemoryAreas.md` |
| Garbage Collection | `backend_fundamentals/java/foundations/gc/GarbageCollection.md` |
| HashMap Internals | `backend_fundamentals/java/foundations/hashmap/HashMap.md` |
| ConcurrentHashMap | `backend_fundamentals/java/foundations/concurrent_hashmap/ConcurrentHashMap.md` |
| LinkedHashMap | `backend_fundamentals/java/foundations/linked_hashmap/LinkedHashMap.md` |
| TreeMap | `backend_fundamentals/java/foundations/treemap/TreeMap.md` |
| Thread Lifecycle | `backend_fundamentals/java/concurrency/thread_lifecycle/ThreadLifecycle.md` |
| Race Conditions | `backend_fundamentals/java/concurrency/race_conditions/RaceConditions.md` |
| Java Memory Model | `backend_fundamentals/java/concurrency/jmm/JavaMemoryModel.md` |
| synchronized | `backend_fundamentals/java/concurrency/synchronized_keyword/Synchronized.md` |
| volatile | `backend_fundamentals/java/concurrency/volatile_keyword/Volatile.md` |
| ExecutorService & ThreadPoolExecutor | `backend_fundamentals/java/concurrency/executor_service/ExecutorService.md` |
| CompletableFuture | `backend_fundamentals/java/concurrency/completable_future/CompletableFuture.md` |
| Locks | `backend_fundamentals/java/concurrency/locks/Locks.md` |
| IoC Container | `backend_fundamentals/spring/ioc_container/IoCContainer.md` |
| Dependency Injection | `backend_fundamentals/spring/dependency_injection/DependencyInjection.md` |
| Bean Lifecycle | `backend_fundamentals/spring/bean_lifecycle/BeanLifecycle.md` |
| Auto-Configuration | `backend_fundamentals/spring/auto_configuration/AutoConfiguration.md` |
| Starter Ecosystem | `backend_fundamentals/spring/starters/StarterEcosystem.md` |
| Actuator | `backend_fundamentals/spring/actuator/Actuator.md` |
| Spring MVC | `backend_fundamentals/spring/mvc/SpringMVC.md` |
| Spring REST | `backend_fundamentals/spring/rest/SpringRest.md` |
| Exception Handling (Spring) | `backend_fundamentals/spring/exception_handling/ExceptionHandling.md` |
| JPA Repository | `backend_fundamentals/spring/jpa_repository/JpaRepository.md` |
| Derived Queries | `backend_fundamentals/spring/derived_queries/DerivedQueries.md` |
| Transactions | `backend_fundamentals/spring/transactions/Transactions.md` |
| Entity Lifecycle | `backend_fundamentals/spring/entity_lifecycle/EntityLifecycle.md` |
| Feign Client (Spring) | `backend_fundamentals/spring/feign/Feign.md` |
| Security Filter Chain | `backend_fundamentals/spring/security/SecurityFilterChain.md` |
| Authentication Providers | `backend_fundamentals/spring/security/AuthenticationProviders.md` |
| Authorization | `backend_fundamentals/spring/security/Authorization.md` |
| OAuth2 Resource Server | `backend_fundamentals/spring/security/OAuth2ResourceServer.md` |
| JWT Validation | `backend_fundamentals/spring/security/JwtValidation.md` |
| Spring Caching | `backend_fundamentals/spring/caching/SpringCaching.md` |
| Jackson Customization | `backend_fundamentals/spring/jackson/JacksonCustomization.md` |
| Async & MDC | `backend_fundamentals/spring/async_mdc/AsyncMdc.md` |
| Lazy / Eager & N+1 | `backend_fundamentals/spring/jpa_lazy_eager/LazyEagerN1.md` |
| Streams Core | `backend_fundamentals/java/coding_fluency/streams/StreamsCore.md` |
| Stream Collectors | `backend_fundamentals/java/coding_fluency/streams/StreamCollectors.md` |
| Method References | `backend_fundamentals/java/coding_fluency/streams/MethodReferences.md` |
| Optional | `backend_fundamentals/java/coding_fluency/optional/Optional.md` |
| Lombok | `backend_fundamentals/java/coding_fluency/lombok/Lombok.md` |
| Records | `backend_fundamentals/java/coding_fluency/records/Records.md` |
| Immutable Collections | `backend_fundamentals/java/coding_fluency/immutable_collections/ImmutableCollections.md` |
| Constructor Injection | `backend_fundamentals/java/coding_fluency/spring_basics/ConstructorInjection.md` |
| JPA Entity & Repository | `backend_fundamentals/java/coding_fluency/spring_basics/JpaEntity.md` |
| Feign Client (Coding Fluency) | `backend_fundamentals/java/coding_fluency/spring_basics/FeignClient.md` |
| Global Exception Handling | `backend_fundamentals/java/coding_fluency/spring_basics/GlobalExceptionHandling.md` |
| Configuration Properties | `backend_fundamentals/java/coding_fluency/spring_basics/ConfigurationProperties.md` |
| IP Addressing, NAT, DHCP & MAC | `backend_fundamentals/networking/ip_addressing/IpAddressingNatDhcp.md` |
| TCP vs UDP | `backend_fundamentals/networking/tcp_vs_udp/TcpVsUdp.md` |
| DNS — Resolution & Traffic Steering | `backend_fundamentals/networking/dns/DnsResolution.md` |
| Proxies, VPNs & National Firewalls | `backend_fundamentals/networking/proxies_vpn/ProxiesVpnFirewalls.md` |
| Load Balancing | `backend_fundamentals/system_design/load_balancing/LoadBalancing.md` |
| Clustering | `backend_fundamentals/system_design/clustering/Clustering.md` |
| Caching & Distributed Cache | `backend_fundamentals/system_design/caching/CachingAndDistributedCache.md` |
| CDN | `backend_fundamentals/system_design/cdn/Cdn.md` |
| Availability, Reliability & Fault Tolerance | `backend_fundamentals/system_design/availability/AvailabilityReliabilityFaultTolerance.md` |
| SQL vs NoSQL | `backend_fundamentals/databases/sql_vs_nosql/SqlVsNosql.md` |
| Graph Databases & GraphQL | `backend_fundamentals/databases/graph_and_graphql/GraphDbAndGraphQl.md` |
