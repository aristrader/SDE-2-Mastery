# Swapnil Agarwal — long-form resume / bullet bank

This is the one active long-form resume. It is not the final one-page resume. It holds the strongest,
generalized candidate bullets and a separate review list for valid work that is weaker, duplicative, or
needs a decision before it earns a place.

Source preservation:

- `resume_master.md` keeps the original resume and raw review material.
- `vida_impact_questions.md` keeps Swapnil’s verbatim impact answers and the full NFC design.
- `vida_work_context.md` keeps technical, business, implementation/design-boundary, and interview context.
- Nothing should be deleted from those three files during resume curation.
- Before removing a duplicate from this draft, fold its factual detail into a surviving bullet or verify it
  already exists in one of those source files. Use `Rejected` only for a factually incorrect/non-resume
  claim; never discard raw work context.

## Curation workflow

1. **Current phase — long-form selection:** retain strong candidate bullets below; keep weaker/
   overlapping items in the review section instead of discarding them.
2. **Review phase:** mark each candidate `Keep`, `Maybe`, `Park`, or `Incorrect`; we will strengthen,
   combine, or move it without losing its source context.
3. **Final-resume phase:** create one separate one-page file using the highest-signal bullets only. The
   final resume will be tailored per job description, but this long-form file remains the reusable source.

## Contact

Swapnil Agarwal · +91 7773054360 · swapnilagarwal2000@gmail.com
LinkedIn: linkedin.com/in/agarwal-swapnil · GitHub: github.com/swapnil78945 · LeetCode: aristrader

## Profile

Backend engineer with 4+ years of experience building Java/Spring Boot microservices and REST APIs for
identity verification, KYC orchestration, fraud controls, secure document storage, and third-party
integrations. Owns delivery end to end: API and data-model design, performance tuning, security
controls, testing, rollout readiness, observability, and customer enablement.

## Skills

**Languages:** Java

**Frameworks & APIs:** Spring Boot, REST API design, versioned APIs, microservices, OpenFeign,
external API integrations

**Distributed systems:** Apache Kafka, event-driven and asynchronous processing, Redis caching,
idempotent workflow orchestration, retry/backoff, error handling

**Data, cloud & security:** MySQL, PostgreSQL, MongoDB, AWS S3, AWS KMS, secure object storage,
presigned URLs, envelope encryption, customer-managed encryption (BYOK)

**Engineering:** performance tuning, production troubleshooting, observability/monitoring, API and
data-model design, secure multi-tenant systems, testing, CI/CD

## Experience

### VIDA — Software Development Engineer I → Software Development Engineer II

January 2024 – Present · India

Java, Spring Boot, Redis, Kafka, MySQL, AWS

#### Platform scale, performance, and client integration

- **Owned the end-to-end turnaround and scale-up of a Java/Spring Boot identity-verification
  microservices platform**, owning design, production hardening, releases, and customer enablement
  across flow redesign, cloud/runtime routing fixes, memory-leak remediation, response
  standardization, dependency/static-analysis/logging remediation, and storage cleanup; led a refactor
  of ~5,000 lines across at least two model/service upgrades and broader verification-flow changes, then
  deployed and stabilized the verification, liveness, shared-storage, and Indonesia OCR/IDV
  services, growing the platform from reported >90% downtime with no active onboarding to **20+ live
  customers in 2 active
  regions**, with 2 more in preparation, contributing roughly **5% of company revenue**.
  My-verify service (internally calls liveness service (holds liveness, facematch, active liveness and
  color captcha), tijori service, sends mas events, calls relevant model, calls id fraud shield),
  maintains kyc transaction details

- **Re-engineered high-latency production REST API paths to meet customer SLAs**, removing unnecessary
  downstream liveness/face-match calls, correcting AWS/server-version routing defects, and simplifying
  service orchestration; reduced reported average latency from **8–10s to <2s**, with 5-TPS validation
  at ~**1.4s p99** for OCR/IDV and ~**2.14s** for video liveness/face match.
  my-verify service

- **Expanded a live document-verification product from one Malaysian card to the major Malaysian card
  set plus Indonesia KTP**, integrating card and verification models for MyTentera, MyKas, MyPR, and
  KTP, including KTP forgery detection; added front/back verification, PNG input, configurable portrait
  extraction, passport date-format support, and backward-compatible OCR, document-authentication,
  liveness, and face-match flows to support Malaysia expansion and new-market/client conversations.
  my-verify

- **Upgraded the document-verification SDK from v2.7 to v3.4 and hardened the legacy verification
  path**, diagnosing critical production issues, reducing typical processing latency from ~**15s to
  3–4s**, and increasing internal verification accuracy by at least **70%**, alongside improved
  model-run success rates; validated card/model behavior through ~150-card testing and FAR/FRR analysis
  across **3,000+** Malaysian/KTP and **500+** OCR-scoring samples.
  document-verification SDK from v2.7 to v3.4 - this was basically a third party vendor, integrated
  into asg-neo which is called by my-verify

- **Designed and shipped versioned V2 REST API contracts** for a growing verification product, exposing
  richer spoof, image-quality, face-mismatch, multi-error, and verification-result evidence while
  preserving SDK/client compatibility; redesigned the summary contract for easier integration and cut
  its response payload by roughly **70%**, reworked multi-error response propagation and error handling,
  and avoided false passport rejections from unreliable landmark signals.
  my-verify

#### KYC orchestration, configuration, and product controls

- **Built an idempotent, stateful KYC workflow-orchestration service** that replaced client-managed
  point calls with a backend-owned journey spanning OCR, document verification, liveness, face match,
  fraud, and alternate image/NFC capture; designed per-operation persistence, idempotent retries,
  historical attempts, and deterministic status recomputation for a single client-visible KYC state.
  built into my-verify service and idi-mobile (idi-mobile is entry gate for sdk calls and internally calls my-verify)

- **Introduced a Redis-cached, per-product verification-policy engine** for critical OCR/landmark
  fields, minimum-pass thresholds, fraud/risk controls, and hard-reject versus warning behavior;
  enabled customer-specific card-failure acceptance, conversion/compliance policy, and phased rollout
  without application releases.
  my-verify

- **Built configurable OCR-scoring and model-evaluation paths for Malaysian cards and passports**,
  combining product-level toggles and allowlists with vendor/open-source POCs, custom OCR matching,
  scoring flows, and FAR/FRR analysis; used manual validation, model-data support, and findings-driven
  configuration/logic adjustments to improve verification reliability and support future in-house model
  development.
  part of the third party intgrations doing pocs testings and findings newer alternative to current
  vendors and fine tuning configs, custom logics, etc, this was done via 2 way one was a eaarlier hacky
  way of introducing paddle ocr models and writting cutom matching logics, another was the proper
  vendor route the regula integration

#### Fraud, liveness, and identity-risk controls

- **Integrated a third-party fraud-decisioning API into the asynchronous KYC workflow** using
  OpenFeign, request-context propagation, bounded retry/backoff for in-progress decisions, and
  persisted risk evaluations; propagated the resulting policy decision through KYC state, final API
  responses, billing events, and other fraud modules with configurable warning/reject thresholds.
  third party fraud decision metric integration was into id fraud shield, id fraud shield service was
  integrated into my-verify and liveness service, the changes were accommodated to make sure fraud
  effects everything including billing events, final response, kyc tables, other fraud detection modules
  are integrated into liveness service for in house fraud models and signal collections

- **Strengthened liveness anti-spoofing by replacing max-score evaluation with consensus-based
  multi-frame decisioning**, improving conversion while retaining fraud-detection effectiveness; also
  delivered configuration-controlled image-manipulation checks for tampered liveness submissions and
  color-print detection for reproduced identity cards, without imposing a one-size-fits-all rejection
  policy.
  liveness service

- **Built 1:N face-duplicate and watchlist capabilities for onboarding-risk controls**, including
  eligibility gates, portrait/document prerequisites, direct face enrollment/search APIs, and
  response-level duplicate evidence; implemented risk-tiered warning versus rejection outcomes so
  customers can detect duplicate accounts and blacklist repeat fraud actors without automatic rejection
  in every case.
  1:n model and face watchlist/blacklist are different models, they were integrated into my-verify

#### Storage, encryption, traceability, and billing reliability

- **Re-architected verification-artifact storage around a shared AWS S3-backed object-storage
  lifecycle**, integrating a central storage service across verification and liveness flows to replace
  fragmented database/object-store paths for storing, retrieving, and auditing media; reduced storage
  cost by **90%** and added per-product controls over image and transaction-data persistence.
  this is basically the tijori servie and the integration of the service in all my-verofy liveness, etc
  and removing dupllicate entries from different manual s3 storages db storages, etc

- **Eliminated Base64 media proxying from V2 transaction retrieval by issuing concurrent, short-lived
  AWS S3 presigned URLs** for front, back, and portrait artifacts; reduced payloads from **1–2 MB to
  45–90 KB**, offloaded downloads from application servers, and protected URLs as bearer credentials
  with log redaction and explicit missing-document/error handling.
  my-verify

- **Implemented live customer-managed encryption (BYOK) for multi-tenant verification media** with AWS
  KMS envelope encryption, per-product key mapping, partitioned shared-object storage, managed-key
  fallback, and backward-compatible decryption; enabled regulated customers to retain cryptographic-key
  control and revocation capability without duplicated storage or broken existing-data access.
  tijori service

- **Built customer-facing transaction traceability across distributed verification services**,
  propagating group-level correlation identifiers and publishing enriched Kafka events for images,
  scores, warnings, errors, summaries, and back-side processing; provided the customer portal and
  support teams with an auditable, end-to-end investigation trail.
  the portal is ssp and works on kafka events, ssp is taken care by others, we did event integrations
  and enrichement in the library for supporting richer details for helping showcase scores, images,
  errors, status, etc was done by me. change done in my-verify and liveness

- **Reworked Kafka billing-event and failure-path handling** after identifying an estimated **20–30%**
  missed-event gap; added coverage-tracking events, aligned billable outcomes with status/audit records,
  and removed duplicate trail data to improve financial and operational reconciliation.
  my-verify and liveness

#### Provider integrations, ePassport, and new product channels

- **Delivered a configuration-controlled document-verification provider integration** for passport and
  non-core identity-document coverage, building resilient OpenFeign clients, request construction,
  provider-specific error normalization, and behavioral Java mapping of authenticity, image-quality,
  OCR, confidence, and portrait data into common platform results; enabled parallel rollout, legacy
  fallback, and reduced dependence on a single document-verification provider while keeping
  provider-specific payloads out of common audit/response flows.
  my-verify

- **Created a reusable image/NFC provider boundary** with capability-specific contracts, common
  processing results, provider-neutral pipeline mapping, and isolated provider mappers; kept vendor
  semantics, error domains, and result-status interpretation outside core verification logic, creating a
  safe extension point for future third-party integrations without claiming a completed multi-provider
  router.
  currently into my-verify in there own package can be extracted into library whenever required.

- **Delivered end-to-end ePassport/NFC verification as an alternate KYC capture path**, accepting the
  mobile-finalized opaque transaction ID into the backend KYC journey and retrieving the authoritative
  provider result server-side rather than trusting the device verdict; normalized RFID OCR, PACE/BAC
  access, and passive/chip/active-authentication outcomes, with DG2 portrait handling, KYC operation
  persistence, and dedicated downstream events to extend the live product to NFC passports; designed
  and implemented the supporting protected ingress, transaction/journey binding and replay controls,
  private result retrieval, PII-minimized observability, and license/trust-material/deployment
  operations required for a production NFC verification capability.
  **Status correction:** released and live.
  my-verify another support for the already supported cards, nfc passports will also be supported post.

- **Enabled a web-SDK product channel through secure backend gateway APIs**, with HMAC request signing,
  encrypted product-configuration delivery, role-based access, CORS enforcement, and
  configuration-controlled rollout; established a reusable entry surface that routes web/mobile SDK
  requests into the verification/KYC platform.
  idi-mobile

#### Design and discovery work — retain for context, not a default final-resume bullet

- **Turned an ambiguous manual-review requirement into an implementation-ready KYC design and
  handoff**, defining API/SDK contracts, manual-decision states and threshold policy, inbound-verdict
  authentication, Jira/webhook interactions, retry/DLQ, idempotency, and history requirements.
  **Current status:** implementation is in progress as part of a new orchestration platform/service.
  dont say another engineer delivered say currently under work maybe, alng with the orchestration platform
  entire new framework, orchestrator and service

#### Consider before final selection — valid work, but lower-signal or needs a stronger/clearer claim

- Evaluated Philippines/Indonesia verification approaches and document-model options.
  **Likely treatment:** retain for market-expansion/product-integration roles, with the document
  coverage/SDK bullets as the stronger evidence; do not claim a launch or revenue result without direct
  evidence.
  part of pocs, with vendors, open sources models, chatgpt, etc, and other solutions to enable support
  and enter phillipines market.

#### Current one-page exclusions — targeted-version bullet bank

These bullets were intentionally excluded from the current one-page resume. Preserve their wording for
JD-specific variants or later consolidation; related active bullets may already cover part of their scope.

- Built V2 REST APIs with expanded verification features, richer response structures, and multi error
  support; added a compact summary API that cut payload size by ~70% and simplified client and SDK
  integrations.

- Built a backend-managed KYC journey that unified separate verification steps under one client-visible
  status, with idempotent retries, audit history, concurrency-safe updates, and configurable workflows.

- Integrated a new verification vendor, expanding passport and ID coverage while reducing vendor
  dependence.

- Expanded document verification from one Malaysian card to major Malaysian and Indonesian IDs, passports,
  and DLs, adding configuration driven multi side checks, document fraud detection, and configurable
  pass/fail thresholds to match client risk appetites.

- Built configuration-driven document checks that let clients tailor pass/fail criteria, minimum thresholds,
  and stricter validation for selected fields to their risk appetite.

- Integrated third party and in-house device fraud signals into the KYC journey, improving fraud detection
  with client-configurable risk thresholds.

- Expanded the KYC journey with video liveness using consensus based multi frame decisioning and 1:N face
  matching to detect duplicate accounts and support customer blacklists, reducing fraud.

- Reworked billing and failure-path event handling, closing an estimated 20–30% missed-event gap and
  improving financial reconciliation.

- Built end-to-end transaction traceability across verification services, correlating events and publishing
  enriched Kafka records to give customers complete visibility into KYC journey steps through a self service
  portal.

#### Rejected from resume selection — preserve context only

- Made limited POM and Bitbucket YAML configuration updates, including AI-assisted PR-review workflow
  work, while the infrastructure/operations team owned the broader setup. **Decision:** not an
  independent resume bullet, but preserve these terms and scope for a future JD-specific merge if they
  genuinely strengthen a larger ownership-backed point; never add them only for ATS keywords.
  this was done by infra ops only changes were in the bitbucket yamls.

### tiket.com — Software Development Engineer I / Backend Developer

July 2022 – January 2024 · Noida, India

Java, Spring Boot, MongoDB, Redis, Python, Golang, InfluxDB, Telegraf, Grafana, Kapacitor

#### Primary candidate bullets

- **Improved an Agoda-sync microservice’s performance by 300%** through metrics instrumentation,
  asynchronous outbound calls, database-call reduction, flow optimization, and production error
  resolution.

- **Raised InfluxDB uptime from 60% to 99%** by owning monitoring failures, diagnosis, and operational
  fixes; built reusable metrics instrumentation that reduced implementation time from **5 days to
  2 hours**.

- **Lowered MongoDB load by 20%** by replacing a heavy query with application-level joins and smaller
  targeted operations; added Python monitoring/load-reduction automation that improved system
  performance by **30%**.

- **Enhanced a third-party travel-vendor integration** and onboarded **30,000+** products; resolved
  data-validity and cache-invalidation defects that prevented stale content and booking failures.

#### Consider before final selection

- General bug fixing, testing, code reviews, dashboards, logging, monitoring, and cross-team delivery.
  **Treatment:** implied by the stronger bullets above; do not add separately unless a JD specifically
  values one of these.

### JPMorgan Chase & Co. — Software Engineer Intern

January 2022 – June 2022 · Bengaluru, India

- Supported legacy-to-new-system data migration while maintaining continuity and data accuracy.
- Automated Python report-generation workflows and scheduled recurring jobs to improve reporting reliability.

## Education

**Maulana Azad National Institute of Technology, Bhopal** — B.Tech, Computer Science and Engineering,
CGPA 8.3/10 · 2022

**Pragati Vidya Peeth, Gwalior** — CBSE Class XII, 93.6% · 2018

## Leadership — consider for final space

- Co-Head, Training and Placement Cell, MANIT Bhopal.
- Head and Actor, Ae Se Aenak, MANIT Bhopal’s official street-play society.

Likely final-resume treatment: retain leadership only if space remains after current work experience,
skills, and education. With 4+ years of backend experience, it is not a priority.

## Final-resume curation rules

- Use generic, recruiter-readable terms: **object storage** rather than an internal storage-service name;
  **configuration-driven / tenant-configurable** rather than an internal configuration-platform name;
  **customer portal** rather than an internal portal name.
- Keep provider, client, and internal service names out of the public resume. Keep exact internal
  terminology in the context files for interviews.
- Each final bullet must earn its space through at least one of: scale, measurable result, difficult
  architecture, product/market enablement, or unusually strong ownership.
- Combine enabling work with the customer/system result it made possible. Do not create standalone
  bullets for generic configuration, logging, PII toggles, dependency upgrades, or testing unless that
  detail is the core achievement.
- Do not manufacture missing fraud, duplicate-detection, conversion, onboarding, or adoption metrics.
