---
order: 10
---

# Swapnil Agarwal — Resume

+91 7773054360 | swapnilagarwal2000@gmail.com | [LinkedIn](https://www.linkedin.com/in/agarwal-swapnil/) |
[LeetCode](https://leetcode.com/aristrader/)

## Education

**Maulana Azad National Institute of Technology** | Bhopal, Madhya Pradesh, India
B.Tech, Computer Science and Engineering, CGPA: 8.3/10 | 2022

**Pragati Vidya Peeth** | Gwalior, Madhya Pradesh, India
CBSE, 12th Standard, Percentage: 93.6% | 2018

## Professional experience

### VIDA — Software Development Engineer II

Bengaluru, India | April 2026 – Present
Skills: Java, Spring Boot, REST APIs, OpenFeign, MySQL, Redis, Kafka, AWS

- Built a reusable library to standardize third-party vendor integrations, halving time to integrate new
  vendors.
- Shipped NFC passport verification as a new alternate KYC path, using a zero-trust design where the backend
  owns the final decision; supported two critical client opportunities.
- Designed a reusable manual-review workflow for Singapore KYC, routing cases to Jira, ingesting reviewer
  decisions, recomputing KYC status, and preserving SDK, API, and webhook contracts for a ~$400K annual
  opportunity.

### VIDA — Software Development Engineer I

Bengaluru, India | January 2024 – April 2026
Skills: Java, Spring Boot, REST APIs, Redis, Kafka, MySQL, AWS

- Led the production turnaround of an ID-verification platform with >90% downtime, taking it from no active
  onboarding to 20+ live customers through core-flow standardization, memory-leak fixes, and observability.
- Cut average verification latency from ~8s to <2s by eliminating redundant calls, parallelizing processing,
  fixing routing, caching results, and streamlining orchestration; validated OCR/IDV at 20 TPS with ~1.4s p99.
- Upgraded a third-party document-verification SDK from v2.4 to v3.7, resolving production defects and
  cutting processing time from ~15s to 3–4s; evaluated 15 document types across 3,500+ OCR/FAR/FRR samples
  to refine models, configurations, and validation logic, improving internal verification accuracy by 70%+.
- Expanded a KYC portfolio contributing ~5% of company revenue with vendor and model integrations, device
  fraud, video liveness, 1:N face matching, blacklist checks, configurable controls, and billing fixes.
- Built backend-managed KYC orchestration with configurable workflows, a unified status, idempotent retries,
  audit history, and concurrency-safe updates; published enriched Kafka events for self-service transaction
  visibility.
- Replaced Base64 document responses with concurrent S3 presigned URLs, reducing payloads from 1–2 MB to
  45–90 KB, offloading document downloads to S3, and securing access with time-limited URLs.
- Built Bring Your Own Key (BYOK) encryption for multi-tenant document storage using AWS KMS envelope
  encryption, letting regulated customers control and revoke the keys that protect their data.
- Built a centralized image-storage service, consolidating verification and liveness artifacts in AWS S3 to
  reduce storage footprint and costs by ~60% and enable configurable PII retention.

### tiket.com — Software Development Engineer I

Remote | July 2022 – January 2024
Skills: Java, Spring Boot, MongoDB, Redis, Python, Golang, InfluxDB, Grafana

- Optimized an Agoda-sync microservice through metrics integration, flow optimization, fewer database calls,
  error resolution, and asynchronous outbound calls, improving performance by 300%.
- Took ownership of InfluxDB monitoring, identifying and resolving issues to improve uptime from 60% to 99%.
- Implemented application-level joins for a heavy query, breaking it into smaller parts and reducing MongoDB
  load by 20%.
- Enhanced Viator API integration, onboarding 30,000+ products with questionnaire support.
- Collaborated on BMG integration, resolving caching and product-update issues to prevent booking failures
  and increase booking success by 15%.

### JPMorgan Chase & Co. — Software Engineer Intern

Bangalore, India | January 2022 – June 2022
Skills: Python, schedulers

- Facilitated data migration from legacy to new systems, ensuring continuity and accuracy.
- Automated report generation using Python and cron jobs, improving reporting reliability and delivery.

## Technical skills

- Languages, frameworks, and tools: Java, Spring Boot, Maven, REST APIs, microservices, Kafka, Redis,
  Elasticsearch, AWS (S3, IAM), OAuth 2.0, observability, Git, Python, Golang, C++, data structures and
  algorithms
- Databases and caching: MySQL, PostgreSQL, MongoDB, SQL, NoSQL
