# Part 25 — Testing

> **Sprint allocation:** Light touch — solid baseline; pass through during overflow days. **Budget: ~1-2 hrs (overflow slot).**

## 25 Testing — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 1 | Test pyramid — many unit, some integration, few e2e | 🔴 💼 🎯 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 2 | Unit tests — JUnit 5 (Jupiter), AssertJ, Hamcrest | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: JUnit 5 test class with @ParameterizedTest + @MethodSource + AssertJ chained assertions (15 min) |
| 3 | Mocking — Mockito basics, when to mock vs not (rule: mock at boundaries, not internals) | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: Mockito @Mock + @InjectMocks + when().thenReturn() + verify().times(N) (15 min) |
| 4 | Spring slice tests — @WebMvcTest, @DataJpaTest, @JsonTest, @WebFluxTest | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: @WebMvcTest one controller, mock its service via @MockBean, hit via MockMvc (20 min) |
| 5 | Spring full-context tests — @SpringBootTest, MockMvc, TestRestTemplate | 🔴 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 6 | Arrange-Act-Assert (AAA) structure | 🔴 💼 | M | 30 min | [ ] | [ ] | [ ] | [ ] | | |
| 7 | One concept per test (not necessarily one assertion call) | 🔴 💼 | M | 30 min | [ ] | [ ] | [ ] | [ ] | | |
| 8 | Naming — `should_X_when_Y` or BDD `given/when/then` | 🔴 💼 | M | 30 min | [ ] | [ ] | [ ] | [ ] | | |
| 9 | Test isolation — no order dependence, no shared mutable state | 🔴 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 10 | Load testing — JMeter, Gatling, k6, Locust | 🔴 💼 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | (Cross-ref Part 8 load testing) |
| 11 | Integration tests — with real DB, real Redis, real Kafka | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 12 | TestContainers — Postgres, MySQL, Redis, Kafka, Elasticsearch | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | 💻 Warm-up: @Testcontainers + PostgreSQLContainer for an integration test against a real Postgres (30 min) |
| 13 | Contract testing — Pact, Spring Cloud Contract (consumer-driven) | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 14 | WireMock for external HTTP | 🟠 💼 | MP | 1 hr 30 min | [ ] | [ ] | [ ] | [ ] | | |
| 15 | EmbeddedKafka, embedded Redis | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 16 | Test data builders, ObjectMother pattern | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 17 | Fixtures vs factories | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 18 | TDD — when it helps, when it doesn't | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 19 | Coverage — JaCoCo; line vs branch coverage and their limits | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 20 | Profiling under load — async-profiler, JFR | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | (Cross-ref Part 8 profiling) |
| 21 | Chaos engineering — Chaos Monkey, Gremlin, Litmus | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] | | |
| 22 | Game days — planned failure injection | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |
| 23 | Test pyramid inversion — when API tests dominate (microservices reality) | 🟡 | M | 45 min | [ ] | [ ] | [ ] | [ ] | | |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~12 hrs | ~1.1 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~28.5 hrs | ~2.6 wk | |
| Full Part (all items including 🟡) | ~29.25 hrs | ~2.65 wk | |

## Frequently asked

1. **Q:** Test pyramid — what shape does it have at your KYC platform?
   - **Why asked:** Real-world architecture. Classic: many unit (cheap, fast) + some integration (real DB / Redis) + few e2e (browser / full stack). Microservices reality: pyramid can invert toward "integration honeycomb" — heavy on contract + integration tests because mocking inter-service boundaries doesn't catch enough.
2. **Q:** What's "mock at boundaries, not internals"?
   - **Why asked:** Test design senior signal. Mock the external API, the database, the message broker — things you don't own / can't run cheaply. Don't mock your own service / repository / utility class — testing the mock instead of the real code. Internal class collaboration should usually be tested integration-style (within the bounded context).
3. **Q:** Walk through @WebMvcTest vs @SpringBootTest.
   - **Why asked:** Spring testing knowledge. @WebMvcTest: slice — loads only web layer for one controller. Fast (~1s context). Use for controller-level testing in isolation. @SpringBootTest: full context. Slow (~10s+). Use for end-to-end Spring testing OR integration testing across the whole app.
4. **Q:** TDD — when does it work for you, when does it not?
   - **Why asked:** Engineering judgment. Works: well-understood logic (algorithms, business rules with clear inputs/outputs). Doesn't work as well: exploratory work, UI design, performance tuning. Senior signal: "TDD where it earns its keep" — pragmatic, not dogmatic.
5. **Q:** Contract testing — when does it earn its keep?
   - **Why asked:** Microservices testing. Service A consumes Service B's API. Without contract: A's tests mock B (might be wrong), B's tests don't know A's expectations. With contract (Pact / Spring Cloud Contract): A defines its expectations of B; B's CI runs A's expectations against actual B. Catches API regressions before integration.
6. **Q:** Walk through TestContainers with a real example.
   - **Why asked:** Modern integration testing. `@Testcontainers` + `@Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")`. Test runs against ephemeral Postgres container. Cleans up after. No more H2-vs-Postgres-dialect surprises.
7. **Q:** Coverage targets — what's a sensible threshold?
   - **Why asked:** Pragmatic. 70-80% line coverage is common floor. Branch coverage stricter (catches uncovered conditional branches). Don't worship 100% — last 10% often costs disproportionately. Critical paths (auth, payments, KYC verification) deserve higher coverage; logging utilities less so.

## Trick questions / gotchas

1. **Q:** Your unit test passes but the deployed app fails. The test mocked the database with H2 in-memory. Why didn't it catch the bug?
   - **Gotcha:** Dialect divergence. H2 silently accepts SQL that Postgres rejects (or vice versa). H2 doesn't behave like real Postgres on isolation levels, gap locks, certain index quirks. Fix: TestContainers with real Postgres for repository tests. Don't trust H2 for production DB validation.
2. **Q:** You wrote 100 unit tests with 90% coverage. Production has a deserialization bug. What's the gap?
   - **Gotcha:** Coverage measures lines executed, NOT scenarios validated. The bug may live in a code path that's tested but only with happy-path data. Coverage gap: edge cases, malformed input, race conditions. Property-based testing or fuzzing catches these.
3. **Q:** Your tests pass locally, fail in CI. They pass on retry. What's likely?
   - **Gotcha:** Flaky tests. Common causes: (1) time-of-day or timezone dependence, (2) test order dependence (shared state between tests), (3) network calls (DNS / external service), (4) resource leaks from prior tests, (5) thread-pool exhaustion across parallel test runners. Fix root cause, don't retry-mask.
4. **Q:** You mock `MyRepository`. Test passes. Production query has wrong WHERE clause. Why didn't the test catch it?
   - **Gotcha:** Over-mocking. The mock returned canned data, but you never validated the actual SQL / JPA query. Fix: use TestContainers + real repository for query-correctness testing. Pure mock for unrelated business logic; real DB for query logic.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **Spring slice tests + TestContainers integration tests** (~3 hrs combined rows 4 + 12) — modern Spring testing stack. @WebMvcTest for controllers, TestContainers for repositories, full @SpringBootTest sparingly.
- **Contract testing with Pact** (~3 hrs row 13) — KYC platform is multi-service. Consumer-driven contracts catch cross-service breaks. Worth the setup investment.
- **Test discipline + naming + AAA** (~2 hrs combined rows 6-9) — get the team to write maintainable tests. Soft skill but high leverage for codebase quality.
- **Load testing setup with k6** (~2 hrs row 10) — cross-ref Part 8. Reproducible scripts in git; tied to CI for regression catching.

## Hands-on exercises (Practice + Advanced)

Warm-up testing exercises are listed inline in the topic-table Resources column (counted in main Time summary). Longer exercises below are tracked separately.

### Practice — mid-level (~30-60 min each)

1. **TestContainers Postgres integration test** (~60 min) — Spring Boot + `@Testcontainers` + `PostgreSQLContainer`. Test a repository method with real SQL. Verify query correctness (not just returning data shape).
2. **WireMock for external HTTP** (~45 min) — service that calls a vendor API. Wire-up WireMock in test. Stub success, error, timeout scenarios. Verify your retry + fallback logic.
3. **Contract test with Pact (consumer side)** (~60 min) — define consumer expectations of a vendor service. Generate the pact file. Verify provider matches. Catch a breaking change scenario.

### Advanced — senior-grade depth (~60+ min each)

4. **k6 load test pipeline** (~75 min) — write a k6 script that ramps to 100 VUs over 30s, sustains 2 min, ramps down. Capture p50/p95/p99. Run against local Spring Boot service. Tie into CI (artifact upload of results).
5. **Chaos engineering experiment** (~90 min) — pick a non-prod service. Inject latency on one downstream dependency (via WireMock or container CPU throttling). Verify circuit breaker activates. Document expected vs observed behavior.

### Hands-on time summary (Practice + Advanced only)

| Tier | Total time | Weeks @ 10–12 hrs/wk | Actual time |
|------|------------|----------------------|-------------|
| Practice (mid-level) | ~2.75 hrs | ~0.25 wk | |
| Advanced (senior-grade) | ~2.75 hrs | ~0.25 wk | |
| **Combined hands-on (Practice + Advanced)** | **~5.5 hrs** | **~0.5 wk** | |

> Warm-up exercises (counted in main Time summary above) total ~80 min for Part 25 across 4 in-table warm-ups.

## Quick recall

**Q. Mock at boundaries, not internals — one-line rule.**
A. Mock external systems (DB, message broker, third-party API). Don't mock your own service classes — you'd be testing the mock instead of real behavior.

**Q. @WebMvcTest vs @SpringBootTest?**
A. @WebMvcTest: slice loading only web layer for one controller (fast, isolated). @SpringBootTest: full app context (slow, end-to-end). Pick smallest scope that exercises the behavior.

**Q. AAA structure?**
A. Arrange (set up inputs + mocks), Act (call the unit under test), Assert (verify outcome). Visual separation matters; one concept per test.

**Q. TestContainers gain over H2?**
A. Real Postgres / MySQL / Redis behavior. Catches dialect divergence, isolation level quirks, gap-lock specifics. Slightly slower but radically more reliable.

**Q. Test pyramid — typical shape?**
A. Wide base of fast unit tests, narrower middle of integration tests, narrow top of e2e tests. Microservices often invert toward "honeycomb" — heavy integration, lighter unit.

**Q. Coverage threshold — sensible?**
A. 70-80% line coverage as floor; branch coverage stricter. Critical paths (auth, payments) higher; logging utilities lower. Don't chase 100% — diminishing returns.
