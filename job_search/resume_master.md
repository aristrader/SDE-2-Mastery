# Resume master — working draft

Use this local file as the source of truth while we build the full career inventory. The Google Doc copy remains untouched until the final résumé is ready.

## Contact

- Name: Swapnil Agarwal
- Phone: +91 7773054360
- Email: swapnilagarwal2000@gmail.com
- LinkedIn: agarwal-swapnil
- GitHub: swapnil78945
- Other: aristrader

## Current résumé baseline

### Education

**Maulana Azad National Institute of Technology** — Bhopal, Madhya Pradesh, India  
B.Tech, Computer Science and Engineering; CGPA: 8.3/10 — 2022

**Pragati Vidya Peeth** — Gwalior, Madhya Pradesh, India  
CBSE, 12th Standard; Percentage: 93.6% — 2018

### Professional experience

#### tiket.com — Software Development Engineer I / Backend Developer

July 2022 – Present · Noida

**Cross-Functional IT Team**  
Technology: Java, MongoDB, Python, Golang, InfluxDB, Telegraf, Grafana, Kapacitor

- Optimized Agoda sync microservice efficiency via metrics integration, flow optimization, reduced DB calls, error resolution, and asynchronous outbound calls; improved performance by 300%.
- Took complete ownership of InfluxDB issues and increased uptime from 60% to 99% through monitoring and issue resolution.
- Integrated utility repositories to enable metric transmission during database calls, reducing metric implementation time from 5 days to 2 hours without structural changes.
- Implemented application-level joins for a heavy query, breaking it into smaller parts and reducing MongoDB load by 20%.
- Developed Python monitoring and load-reduction scripts, improving the system by 30%.

**Vendor Integration and Enhancement Team**  
Technology: Java, MongoDB, Redis, Spring Boot

- Enhanced Viator API integration and onboarded 30,000+ products with key questionnaires.
- Resolved a BMG validity bug and product-update cache issue, preventing stale pages and booking failures.
- Led bug fixes, enhancements, debugging, testing, cross-team collaboration, code reviews, logging/dashboard creation, test-coverage expansion, and monitoring improvements.

#### JPMorgan — Software Engineer Intern

January 2022 – June 2022 · Bangalore

**Migration Team**  
Technology: Python, Schedulers

- Supported data migration from legacy to new systems while maintaining continuity and accuracy.
- Designed Python scripts to automate diverse report generation.
- Scheduled multiple report-generation jobs to improve report delivery.

### Projects

#### Filtering Disaster-Related Tweets via Twitter API with Machine Learning

Technology: Python, Machine Learning, Twitter API

- Built a machine-learning model to classify disaster-related tweets.
- Trained the model using tweet content and hashtags; achieved over 90% accuracy.

### Technical skills

- Languages, frameworks, and tools: C++, Java, Python, Golang, Spring Boot, Kafka, Redis, Git, GitHub, REST APIs, Microservices, Telemetry, CI/CD.
- Databases: SQL, MongoDB, NoSQL, PostgreSQL.
- Coursework: DBMS, Operating Systems, OOP.
- DSA: 500+ problems solved.

### Positions of responsibility

- Co-Head, Training and Placement Cell, MANIT Bhopal.
- Head and Actor, Ae Se Aenak, MANIT Bhopal's official street-play society.

## VIDA — performance-review material

Paste both performance-review submissions below. Keep original wording, project names, dates, metrics, scope, and manager feedback where useful.

### Review 1

Performance:
Production Deployment & Performance Optimization of Malaysia Service :
Led prod deployment, fixed critical bugs, optimized flows & reduced DB storage by 90% (image storage issue).
Fixed memory leaks, Improved latency via flow optimizations, AWS fixes & version upgrades. Enhanced stability and Standardized responses.
Got the average latencies under 1 second for ocr/idv and under 2 for liveness & faceMatch.
Results of Performance Testing: Video Liveness/FaceMatch → 5 TPS closed, ~2870 ms latency, OCR/IDV → 5 TPS closed, 99 percentile : ~1418 ms latency , ASG Calls → 3-4s latency per call.

Malaysia Service Integrations and improvements :  
Integrated OCR & IDV models (MyKad models, image liveness, face match, KTP models). Allowing for in-house handling of major Malaysian cards, and KTP cards.
FAR/FRR analysis on 3000+ cards for improved accuracy.
Optimized flows: stopped unnecessary API calls (liveness facematch flows), refined V2 model’s response, integrated Microblink KTP forgery, etc.

ASG-Neo Stability, Microblink Upgrade & Market Expansion :
Fixed critical ASG-Neo issues and upgrading microblink v3.4 from v2.7. Increasing accuracy by at least 70% and reducing latency to about 1/4th (used to be around 15 sec now 3-4 sec).
Extensive testing on around 150 cards → improved latency, success rates, FAR/FRR accuracy. Optimized PH cards & passport processing, expanding market reach.

IDI-Main & Liveness changes (Tijori, liveness db and session id) :
Collaborated with mithilesh on IDI-Main & IDI-Liveness updates, integrating liveness DB & Tijori.
Standardized responses, refactored the service, and fixed request validation & score-related issues. Resolved Kafka MAS billing event issues, improving system reliability.

Billing Optimization & Audit Trail Enhancements :
Rewrote billing logic, Added new billing events for improved tracking & coverage.
Earlier - 20-30% missed events & error flows. Leading to higher revenue capture.
Resolved audit trail issues.

Summary Endpoint for Easier Client Integration:
Simplified response structure for easy integration and understanding of the response for the clients. Improving usability and expanding adoption. Response size decreased by around 70%

Paddle OCR Integration:
Integrated Paddle OCR to generate scores for Malaysian cards and passports. Implemented toggles & product ID whitelisting for precise client control. Also helped buy us time for in-house model developments and helped create the data set for the same.
FAR/FRR analysis on 500+ cards, built full scoring flows (Malay cards, passports - YTL).

Major Deployments and ownerships:
My-verify
Deployed My-Liveness & My-Tijori, completing Geo-X ecosystem.
Deployed Doc-Verify (Indonesia) for IDV & OCR services.

I rate myself as Outstanding because I have consistently delivered high-impact improvements across key areas, including production deployments, performance optimization, and system stability. I’ve taken initiative in resolving critical issues, optimizing flows, and enhancing reliability, ensuring smoother integrations and better scalability. My contributions have improved efficiency and reduced failures. Additionally, I am always eager to learn and improve, continuously researching better practices, exploring edge cases, and identifying potential issues to enhance system reliability. I take initiative in optimizing processes, refining flows, and proactively addressing challenges to ensure long-term stability and efficiency.

### Review 2

Performance:
1. Platform & Product Capability Enhancements

Integrated all Malay cards: Integrated new models for MyTentera, MyKas, and MyPR, expanding Malaysia verification from MyKad-only to full Malaysian card coverage.
Enabled Backside Card Verification Support: Integrated new model capabilities enabling front–back card verification while maintaining backward compatibility with existing integrations.
Designed and Introduced MyVerify V2 APIs: Launched MyVerify V2 APIs with improved error handling, spoof/IQA responses, face mismatch detection, and simpler integrations for clients and SDK.
Integrated OSS and Redis into GeoX - Integrated OSS configuration with Redis caching to enable dynamic feature control and safe rollout of new capabilities.
Enabled Web SDK Integration for GeoX: Built idi-mobile wrapper APIs with HMAC signing, encrypted configs, role-based access, and CORS for Web SDK support.
Introduced Configurable Critical Field Validation: Implemented configuration-driven OCR and landmark validation, allowing failures to be treated as errors or warnings.
Improved Cross-Service Transaction Traceability for Liveness Stack: Implemented groupId propagation across liveness services (idi-mobile, liveness, image-quality), enabling end-to-end transaction tracking within SSP.
Enabled PNG support and configurable KTP portrait extraction while maintaining backward compatibility with existing integrations.

2. Security, Fraud Prevention & Compliance

Implemented Customer-Managed Encryption (BYOK): Clients can encrypt stored images using their own AWS KMS keys with full key ownership and revocation control.
Integrated FraudShield for Liveness Fraud Detection: Helps to analyze SDK-originated liveness requests and block suspicious transactions.
Added Color Print Fraud Detection: Introduced color print detection for Malaysian identity cards to identify printed card reproductions.
Enabled Image Manipulation Detection for Liveness: Added image manipulation checks for liveness calls via GeoX to detect tampered submissions.
Enabled Configurable PII Storage Controls: Implemented OSS-driven configuration allowing clients to disable storage of images and SSP transaction data.

3. System Reliability, Observability & Infrastructure

Tijori Integration and SSP Improvements (GeoX): Integrated Tijori storage with GeoX and added events enabling SSP visibility into images, scores, warnings, errors, and transaction summaries.
Improved Error Handling Framework (Tech Debt Clearance): Redesigned exception and error propagation to support multiple errors per verification operation, improving API clarity and reliability.
SSP Improvements (2.0) -  Introduced backside card verification events and expanded the data sent to SSP, enabling full visibility into backside processing stages.
Upgraded Platform Dependencies: Upgraded GeoX service to Spring 3.3.5 and updated AWS and related dependencies for improved stability and security.
Resolved Redis and Sentinel configuration issues within Tijori.
Improved Code Quality and Maintainability: Resolved SonarQube, SpotBugs, and PMD issues while optimizing code paths and removing unused code.
Improved Logging and Debugging Capabilities: Fixed fragmented Tomcat and Spring Boot logs, improving log readability and operational debugging.

4. Research, Experimentation & Market Expansion

Evaluated Philippines Card Verification Approaches: Conducted multiple POCs using LLM extraction, Microblink, and KTP models to assess Philippines ID verification feasibility.
Evaluated Indonesia Driving License Support: Conducted Microblink-based POC to evaluate support for Indonesia driving license verification.
Improved Microblink Verification Reliability: Investigated Microblink-related issues and tested multiple versions to improve passport and card verification reliability.

5. Verification Accuracy & Conversion Improvements

Improved Video Liveness Decisioning: Replaced max-score evaluation with consensus-based multi-frame decisioning, improving liveness conversion while maintaining fraud detection effectiveness.
Improved Card Verification Accuracy: Enhanced ASG and passport verification with additional date formats and flow fixes, improving OCR accuracy and stability.
Reduced Passport Rejection in Summary Endpoint: Excluded unreliable landmark validations from summary endpoint logic, reducing false passport rejections caused by Mircoblink limitations.

6. Documentation & Developer Enablement

Revamped Product Documentation and Knowledge Base:
Led a comprehensive overhaul of GitBook and Confluence documentation for the Malaysia service. Achieved complete API and workflow documentation coverage, added detailed error cases and examples, and reorganized documentation across OCR, IDV, liveness, and facematch flows to provide a structured one-stop reference for developers and integrators.

7. Operational Ownership & Delivery Support

Operational Ownership and Delivery Support : Proactively supported testing, monitoring, alert handling, and issue resolution to ensure smooth feature delivery and stable production operations.
Improved Development Workflow Efficiency: Enabled ChatGPT-based automated PR review for the Malaysia service, improving code review efficiency and development productivity.

## VIDA — work after the reviews

### Imported technical context from `my-verify` — pending validation

This is an append-only inventory from the local technical documents and code. It is deliberately broader than a final résumé. It preserves technical scope and source paths; confirm ownership, release state, market/client, and measurable outcomes before converting any item into final résumé bullets.

#### Critical fields

- Worked in the per-card critical-field configuration path for MyKad, MyKas, MyPR, MyTentera, and Indonesia KTP.
- The runtime flow resolves a normalized card type to product-specific configuration containing enforcement, critical OCR/landmark fields, and minimum thresholds.
- The wider OCR path distinguishes critical failures (error) from non-critical failures (warning), so this work is part of the verification-decision policy rather than only extraction/display logic.
- OCR minimum-field and landmark minimum-field threshold checks are separately supported; summary responses intentionally suppress some detailed critical-field errors.
- Current source uses a safe disabled default for unknown/missing card configuration and logs the configuration issue.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/util/CriticalFieldConfigUtil.java`; `my-verify/docs/codebase-context.md`.

#### KYC operations and KYC status

- The v2 verification path carries KYC-flow context and records stage/operation-level status for KYC transactions.
- KYC state is persisted per group/transaction/operation and recomputed into an overall status.
- NFC work extends this model with `documentCaptureMode`, NFC operation recording after execution, and an NFC section in the KYC status response.
- The documented design selected NFC as an alternate capture path within existing KYC flows rather than a separate flow family.
- Image-to-NFC switching is explicitly designed as supported; the alternate path keeps historical OCR/IDV attempts visible.
- Evidence: `my-verify/docs/codebase-context.md`; `my-verify/docs/nfc-kyc-operations-design.md`; `my-verify/docs/nfc-kyc-extension-design.md`.

#### Liveness and FraudShield

- The verification pipeline has separate liveness and face-match stages; liveness transaction data can provide the selfie used by downstream face matching, while liveness and face-match outcomes remain independently representable.
- When multiple liveness transaction IDs exist, face match uses the last one. Its reference image is the cropped document face, falling back to the ID-front image rather than the liveness selfie.
- FraudShield is integrated as an asynchronous post-verification fraud-evaluation stage, guarded by v2/KYC eligibility, a shield ID, and product-level enablement.
- The FraudShield path calls an external API through Feign, retries `IN_PROGRESS` results using configurable attempts/backoff, applies configurable warning/error risk thresholds, and maps failure modes to service errors.
- An error-threshold breach upgrades all requested KYC operations to `ERROR` using a worst-of status update; successful evaluations persist submission/risk/threshold metadata.
- Request/MDC context is explicitly propagated into the asynchronous fraud-evaluation work.
- The default retry policy is three attempts with a 200 ms delay; transaction-not-found is handled separately and unknown risk fails open.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/service/IdFraudShieldService.java`; `my-verify/src/main/java/id/vida/verify/myservice/verify/stage/impl/FraudShieldVerificationStage.java`; `my-verify/docs/codebase-context.md`.

#### Presigned document URLs

- Introduced/maintained a get-transaction response path that returns short-lived presigned S3 URLs instead of proxying Base64 image bytes through the verification service.
- The service requests URLs for front, back, and portrait documents concurrently when available.
- The design protects the URL as a bearer credential by redacting it from string/log representations and maps missing documents separately from generic storage failures.
- The path is exposed through v2 transaction retrieval when documents are requested; a storage-specific missing-document error is mapped even when the upstream storage service returns an unexpected HTTP status.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/client/StorageClient.java`; `my-verify/src/main/java/id/vida/verify/myservice/service/StorageService.java`; `my-verify/src/main/java/id/vida/verify/myservice/service/VerifyApiService.java`; `my-verify/src/main/java/id/vida/verify/myservice/verify/documents/PresignedDocumentDTO.java`.

#### 1:N enrollment, duplicate checks, and blacklist/watchlist flow

- The v2 verification flow supports 1:N identity-face duplicate checking and image blacklist/watchlist behavior.
- Duplicate checks are scoped by configured country/card eligibility, an OCR request, clean upstream verification state, and required portrait/document fields; the documented allowlist includes Malaysian MyKad variants, MyKas, MyPR, MyTentera, MyKid, and Indonesia KTP, but not passports.
- Matched results are treated as an error for high-risk profiles and a warning otherwise; the v2 response can expose duplicate/blacklist check results.
- The dedicated face-blacklist endpoint supports both search and enrollment/blacklisting, uploading an image for enrollment and returning a face ID.
- Evidence: `my-verify/docs/codebase-context.md`; `my-verify/src/main/java/id/vida/verify/myservice/service/FaceVerifyService.java`; `my-verify/src/main/java/id/vida/verify/myservice/controller/FaceController.java`.

#### Regula image-based document-verification integration

- Completed image-based Regula integration replaces/reroutes the ASG Neo/Microblink document-authentication path for supported non-Malaysian, non-KTP cards through configuration-controlled provider routing.
- Implemented/covered Feign client integration, DTOs, configuration, request construction, common result model, provider response parsing, provider interface, stage/result mapping, billing/response integration, configuration, and verification.
- Request mapping asks Regula for document type, authenticity, image quality, OCR text, and images. Response mapping converts provider-specific containers into common document metadata, authenticity/IQA outcomes, OCR fields/confidence/source details, portrait data, and normalized failures.
- The design handles MRZ versus visual OCR-source selection, image-quality/authenticity decision mapping, unsupported cards, image stripping for audit-safe payloads, independent OCR-only/IDV-only requests, and focused code-review hardening.
- Manual Java mapping was deliberately retained after evaluating MapStruct, JOLT, and reflection/config-driven alternatives; provider request/response details are retained in image-stripped form for auditability.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/regula/`; `my-verify/src/main/java/id/vida/verify/myservice/regula/docs/context.md`; `my-verify/src/main/java/id/vida/verify/myservice/regula/docs/design.md`; `my-verify/docs/confluence/epassport-nfc-regula-technical-review.md`.

#### Regula NFC / ePassport verification

- Extended document verification to an NFC/ePassport input mode: the SDK sends a Regula transaction ID after chip read/finalization; the server performs one provider process call and retrieves the result, then returns normalized chip authentication and OCR data.
- NFC uses a dedicated v2 operation and payload section, separate from image verification. Its response models access control (PACE/BAC), authentication (PA/CA/AA), and RFID-derived OCR data instead of front/back image results.
- The integration uses a distinct NFC request shape and a dedicated mapper because chip data is cryptographically verified rather than evaluated through optical authenticity/IQA checks.
- The mapper requires completed processing, a passport card type, RFID status/text/images, and an RFID DG2 portrait; PA is mandatory while CA/AA are optional. Current semantics map `0` to fail, `1` to pass, and `2`/not-done to omission.
- NFC is recorded as a KYC result operation and emits separate MAS OCR, access-control, and authentication events.
- Technical design includes zero-trust finalization/result-retrieval boundaries, private provider-result access, PII-safe logs/analytics, correlation/transaction observability, trust-material management, Prometheus metrics, deployment sizing, and license/secret controls.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/regula/service/RegulaNfcResponseMapper.java`; `my-verify/docs/nfc-verification-api-contract.md`; `my-verify/docs/nfc-kyc-operations-design.md`; `my-verify/docs/nfc-mas-events-design.md`; `my-verify/docs/confluence/epassport-nfc-regula-technical-review.md`.

#### Reusable third-party document-verification framework

- Built/designed a provider-agnostic boundary around external document verification: capability-specific image/NFC interfaces, request types, common processing results, provider-specific mappers, and provider-neutral result mapping.
- The same `DocumentVerificationStage` remains the pipeline entry point while a resolver selects the appropriate provider/mode. Image and NFC capability types provide compile-time protection against routing a request to an unsupported provider.
- Provider code is isolated from common contracts and pipeline/business mapping, preserving a path to add providers without changing the stage or result mapper.
- The architecture deliberately supports later configuration/DMN routing by country/card/client, shadow-mode comparison, split OCR/IDV providers, library extraction, and future orchestration without prematurely implementing those features.
- A composition-based alternative was also evaluated and retained as a design reference; the documented interface/capability approach is the active direction.
- Today the resolver returns Regula; config/DMN routing, shadow mode, split capabilities, additional providers, and module extraction are documented future evolution rather than delivered functionality.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/verify/externalprovider/`; `my-verify/src/main/java/id/vida/verify/myservice/regula/docs/provider-abstraction.md`; `my-verify/docs/nfc-design-extension.md`; `my-verify/docs/nfc-composition-approach.md`; `my-verify/src/main/java/id/vida/verify/myservice/regula/docs/future-evolution.md`.

#### GXS Bank Singapore manual-review flow — research/design contribution only

- Prepared the local documentation/research structure for GXS Singapore KYC manual review, separating product decisions, SDK responsibilities, backend decision-engine/Jira/webhook behavior, and API/field contracts.
- Considered SDK manual-review status behavior, OCR/IDV response shapes, warning/error coexistence, HMAC/inbound-verdict authentication, retries/DLQ, idempotency, status history, threshold bands, and interaction with critical-field configuration.
- Do not claim implementation ownership unless later evidence supports it. This can be retained as a cross-functional discovery/design contribution only if it materially informed the delivered capability.
- Evidence: `my-verify/docs/confluence/gxs-sg-kyc/_conventions.md`.

#### Evidence still needed before résumé selection

- Exact personal ownership for broad task labels, especially KYC, liveness, and 1:N enrollment.
- Whether each item shipped, is in pilot, is in progress, or is design/research only.
- Client/country/market enabled and a measurable outcome: traffic, latency, conversion, fraud reduction, manual-review reduction, cost, or developer/onboarding effort.
- One notable technical trade-off for each final selected project.

### Line-by-line technical audit addendum — preserve for later selection

#### Verification platform and KYC lifecycle

- The v2 path initializes product, request-origin, and KYC-flow context before invoking the shared verification service. The service then hydrates existing document IDs, validates, creates/audits KYC state, runs preprocessing and verification/post stages, persists document IDs, runs post-checks, recomputes KYC status, and completes auditing.
- KYC state is designed around one status record per `groupId` and idempotent operation-attempt records per group/transaction/operation. Existing operation rows can only worsen through `VERIFIED -> REVIEW -> ERROR`; concurrent status escalation uses persistence locking.
- Overall KYC status derives required operations from both flow type and the active document path. Missing required work remains `IN_PROGRESS`; precedence is `ERROR > REVIEW > VERIFIED`; stale in-progress cases time out separately.
- A stable `groupId` cannot be reused with a different product or KYC flow. The active document path can switch between image and NFC, while previous-path attempts remain available as historical summaries.
- Existing generic `REVIEW` status should not be confused with a future GXS manual-review verdict.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/service/VerifyApiService.java`; `my-verify/src/main/java/id/vida/verify/myservice/audit/KycStorageFacade.java`; `my-verify/src/main/java/id/vida/verify/myservice/service/KycStatusTimeoutScheduler.java`; `my-verify/src/main/resources/db/kyc_flow_ddl_queries.sql`.

#### Critical-field and OCR policy details

- Critical-field configuration is product-scoped and controls OCR/landmark enforcement, field lists, and pass-count thresholds by card group.
- KTP portrait extraction is separately gated by product configuration, KTP classification, OCR plus IDV operations, and usable OCR output; a missing required portrait becomes an OCR error.
- OCR scoring/configuration also includes provider score inclusion, face-match score override, duplicate-check controls/risk profile, landmark settings, fraud thresholds, and material-check settings.
- Paddle OCR is not the source of truth for OCR values in the documented flow; it backfills similarity-derived scores into ASG output under restrictive product/card conditions.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/model/OssProductConfigDTO.java`; `my-verify/src/main/java/id/vida/verify/myservice/util/CriticalFieldConfigUtil.java`; `my-verify/src/main/java/id/vida/verify/myservice/verify/vida/ocr/`; `my-verify/docs/codebase-context.md`.

#### Liveness and face-match details

- Image liveness and image face match are independent operations. Liveness can enable image-manipulation checking through product configuration.
- When face match is called with liveness transaction IDs, each ID is reflected in KYC liveness-attempt tracking; earlier IDs are set to error and the last ID receives the latest computed status.
- The local integration test suite covers matching/invalid liveness, non-matching face match, and liveness-only success/failure cases.
- Known technical limitation: face match using only a prior liveness transaction does not create a new face-liveness row under the face-match transaction; the selfie remains linked to the earlier liveness transaction.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/verify/stage/impl/LivenessVerificationStage.java`; `my-verify/src/main/java/id/vida/verify/myservice/verify/stage/impl/FaceMatchVerificationStage.java`; `my-verify/src/test/java/id/vida/verify/myservice/MyServiceApplicationIntegrationTest.java`; `my-verify/docs/tech-debt-todo.md`.

#### Fraud, 1:N, and watchlist policy details

- A successful FraudShield evaluation is persisted with transaction/group/submit ID, detected risk, and the configured thresholds. Transaction retrieval can reconstruct stored risk if the upstream evaluation is no longer found.
- 1:N duplicate-check eligibility requires v2, OCR, a product feature flag, eligible card/country, no aggregate errors, a portrait, OCR ID number, and a front-card storage ID. A portrait can be uploaded on demand if the document ID is absent.
- Returned duplicate matches include face/ID references and enrollment-skip reason. A null storage document ID or score in a returned match is treated as an internal error; duplicate errors are attributed to OCR and can only worsen KYC state.
- Verify-time blacklist search executes before face match. Its internal defaults are documented as threshold `0.81` and maximum three results; a non-empty result may error or warn based on risk profile.
- The separate blacklist API validates image payloads, supports search/enrollment, uploads the enrollment image to Tijori, and returns a face ID. Its billing semantics remain a question rather than a completed business claim.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/service/FraudShieldTransactionService.java`; `my-verify/src/main/java/id/vida/verify/myservice/service/FaceVerifyService.java`; `my-verify/src/main/java/id/vida/verify/myservice/service/FaceWatchlistService.java`; `my-verify/src/main/java/id/vida/verify/myservice/helper/FaceVerifyHelper.java`; `my-verify/src/main/resources/application.properties`.

#### Document storage and presigned URL details

- The service uploads and tracks document assets asynchronously, including ID images, liveness/face-match assets, portrait, and relevant verification images. Existing document IDs are hydrated before validation and can suppress duplicate uploads.
- Persisted transaction document IDs are keyed by transaction/image type. Storage persistence is soft-fail: a persistence error becomes an aggregate internal error but does not necessarily stop verification.
- `GET /transaction` can obtain front/back/portrait presigned URLs in parallel. Missing persisted DB rows are omitted, but a storage fetch failure for a present row fails the request.
- A known gap is that video-liveness frame IDs are uploaded during verification but are not persisted in the document-ID table for later retrieval.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/service/VerifyApiService.java`; `my-verify/src/main/java/id/vida/verify/myservice/audit/documents/TransactionDocumentService.java`; `my-verify/src/main/java/id/vida/verify/myservice/service/StorageService.java`; `my-verify/docs/tech-debt-todo.md`.

#### Regula/ePassport trust and operational architecture

- The technical review treats mobile NFC capture as untrusted evidence: a server-authoritative provider result must determine the verification verdict. A transaction ID binds the mobile capture to the server-side verification journey.
- The reviewed architecture separates provider transaction state, encrypted artifact storage, KYC policy/orchestration, and a PII-minimized analytics fact model. It explicitly avoids using raw document identity data or provider tables as general analytics sources.
- Operational topics fully documented for later delivery include constrained ingress, private result retrieval, replay/idempotency controls, tenant/journey binding, rate/concurrency limits, trust-store management, TLS/pinning/mTLS decisions, online-license dependency, secret management, metrics, alerts, and retention/deletion controls.
- These operational controls are high-quality system-design evidence but remain research/design unless release evidence confirms implementation.
- Evidence: `my-verify/docs/confluence/epassport-nfc-regula-technical-review.md`.

#### Regula/NFC implementation precision and boundaries

- Current NFC code is v2-only and standalone. It processes a provider transaction once, retrieves one result with images to obtain RFID DG2 portrait data for face-match use, and does not implement polling.
- The NFC request asks for provider document type, status, text, and images. The mapper validates processing completion, passport support, status/text/image containers, and RFID portrait data. RFID OCR uses RFID-sourced values, normalized dates, and no confidence/threshold field.
- PACE/BAC are prerequisites to chip access rather than authenticity checks; PA is mandatory, CA/AA are optional when unsupported/not performed. The current mapper interprets `0` as fail, `1` as pass, and `2`/not-done as omitted.
- Current code does not validate the NFC transaction ID UUID format or compare returned result identity to the requested ID. These are documented security-hardening opportunities, not delivered claims.
- The technical-review draft conflicts with portions of current implementation (for example polling behavior, image retrieval, output types, and sample numeric semantics). Use versioned implementation code as the truth for current behavior; preserve the draft as design/research context only.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/regula/service/RegulaProvider.java`; `my-verify/src/main/java/id/vida/verify/myservice/regula/service/RegulaRequestMapper.java`; `my-verify/src/main/java/id/vida/verify/myservice/regula/service/RegulaNfcResponseMapper.java`; `my-verify/docs/confluence/epassport-nfc-regula-technical-review.md`.

#### Provider-framework decision record

- Capability-specific interfaces were chosen over the evaluated composition flow because they retain compile-time routing safety and lower indirection for the current scope.
- Current state: Regula implements image and NFC provider interfaces; the resolver returns Regula; common results keep provider-specific interpretation out of downstream business mapping.
- Future-only: additional providers, country/card/client configuration routing, DMN, shadow comparison, split-capability result merging, library extraction, Spark overrides, BPMN stage removal, and in-house-provider unification.
- Evidence: `my-verify/src/main/java/id/vida/verify/myservice/verify/externalprovider/`; `my-verify/docs/nfc-design-extension.md`; `my-verify/docs/nfc-composition-approach.md`; `my-verify/src/main/java/id/vida/verify/myservice/regula/docs/future-evolution.md`.

#### GXS manual-review scope boundary

- The local GXS source is a documentation/research convention record. It captures decisions and unanswered questions around SDK manual-review attributes, warning/error precedence, Jira/webhook/HMAC/retry/DLQ/status history, threshold bands, contract shape, and critical-field coexistence.
- No audited source establishes a GXS-specific implementation, Jira integration, webhook receiver, HMAC flow, or manual-review status subsystem. Treat it as technical discovery/design support unless personal deliverables and launch impact are later supplied.
- Evidence: `my-verify/docs/confluence/gxs-sg-kyc/_conventions.md`.

## VIDA — synthesized achievement inventory (long master)

Use this as the working inventory for the eventual VIDA experience section. It is intentionally longer than any final résumé. Review text remains above as the verbatim source; this section combines that source with the audited technical context. All items below are user-confirmed end-to-end shipped work unless explicitly marked as research/design.

**Vida — Software Development Engineer I → Software Development Engineer II**
January 2024 – Present · Java, Spring Boot, Redis, Kafka, MySQL, AWS, Feign

### Production delivery, performance, and platform reliability

- Led production deployment and stabilization of the Malaysia verification service; resolved critical defects, memory leaks, AWS/version issues, and inefficient flows, while reducing image-storage database usage by **90%**.
- Brought average OCR/IDV latency below **1 second** and liveness/face-match latency below **2 seconds** through flow and infrastructure optimization; closed 5-TPS performance tests at approximately **1,418 ms p99** for OCR/IDV and **2,870 ms** for video-liveness/face-match.
- Upgraded Microblink from v2.7 to v3.4 and stabilized the ASG-Neo path, improving verification accuracy by at least **70%** and reducing latency from about **15 seconds to 3–4 seconds**; tested roughly **150** cards and improved Philippines-card/passport handling.
- Integrated OCR/IDV models for MyKad, image liveness, face match, and Indonesia KTP, enabling in-house handling for major Malaysian identity cards and KTP verification.
- Expanded Malaysian verification from MyKad-only coverage to MyTentera, MyKas, and MyPR; enabled front-and-back card verification without breaking existing client integrations.
- Designed and launched MyVerify V2 APIs with clearer error handling, spoof/IQA results, face-mismatch detection, and a simpler integration contract for clients and SDKs.
- Reworked error propagation to represent multiple errors per verification operation, improving API consistency and operational diagnosis.
- Upgraded GeoX to Spring Boot 3.3.5 and refreshed AWS-related dependencies; resolved Redis/Sentinel, fragmented application-log, SonarQube, SpotBugs, and PMD issues while removing dead code and improving maintainability.

### Verification accuracy, fraud prevention, and KYC controls

- Performed FAR/FRR analysis across **3,000+** cards for Malaysian/KTP model improvements and **500+** cards for Paddle OCR scoring flows covering Malaysian cards, passports, and YTL use cases.
- Replaced max-score video-liveness evaluation with consensus-based multi-frame decisioning to improve conversion while retaining fraud-detection effectiveness.
- Integrated FraudShield into the KYC verification pipeline to evaluate SDK-originated liveness fraud; implemented configurable risk thresholds, asynchronous execution with context propagation, retry handling, result persistence, and KYC-error escalation for high-risk outcomes.
- Added color-print fraud detection for Malaysian IDs and image-manipulation detection for GeoX liveness requests to identify physical reproductions and tampered submissions.
- Implemented configuration-driven critical OCR and landmark-field validation for Malaysian cards and KTP, allowing per-product treatment as warning or error and supporting minimum-passing-field policy.
- Built 1:N face enrollment, duplicate-detection, and blacklist/watchlist flows with eligibility controls, configurable warning/error policy, result reporting, and dedicated enrollment/search APIs.
- Implemented KYC operation-level status persistence and overall-status recomputation across verification stages, including idempotent/concurrency-aware updates, stale-case handling, and KYC API/status reporting.
- Added PNG support and configuration-controlled KTP portrait extraction while retaining backward compatibility; improved ASG/passport accuracy with date-format and flow fixes, and reduced false passport rejections by excluding unreliable landmark signals from the summary response.

### Integrations, storage, security, and observability

- Integrated OSS configuration with Redis caching for dynamic feature control and safer rollout of verification capabilities.
- Built idi-mobile wrapper APIs for GeoX Web SDK integration, including HMAC signing, encrypted configuration, role-based access, and CORS controls.
- Implemented customer-managed encryption (BYOK), enabling clients to protect stored verification images with their own AWS KMS keys and retain key revocation control.
- Added configuration-driven PII-storage controls so clients can disable image and SSP transaction-data persistence when required.
- Integrated Tijori storage into GeoX and expanded SSP/Kafka events to expose verification images, scores, warnings, errors, transaction summaries, and backside-card stages; propagated `groupId` across liveness services for end-to-end traceability.
- Replaced Base64 image payloads in transaction retrieval with concurrent presigned S3 URL generation for stored front, back, and portrait documents; protected URLs as bearer credentials through log redaction and explicit missing-document handling.
- Rewrote billing logic and added coverage-tracking events, addressing error-flow gaps that previously missed approximately **20–30%** of billing events; also repaired audit-trail issues.
- Simplified the summary endpoint and standardized responses, reducing response size by roughly **70%** and making client integration easier.

### External-provider architecture and new verification capabilities

- Designed and delivered a configuration-controlled Regula image-verification integration for supported non-Malaysian/non-KTP documents, including Feign client integration, request construction, provider-response parsing, normalized common results, error handling, tests, and release hardening.
- Mapped provider-specific document, authenticity, image-quality, OCR, confidence, and portrait data into the platform response model; handled OCR-source selection, unsupported-card behavior, independent OCR/IDV requests, and PII-safe image-stripped provider audit data.
- Created a provider-agnostic document-verification boundary with capability-specific image/NFC contracts, common processing results, provider-neutral pipeline mapping, and isolated provider mappers—making later provider addition possible without changing core verification stages.
- Delivered Regula ePassport/NFC verification as a dedicated V2/KYC path: processed mobile-originated provider transactions server-side, mapped PACE/BAC and PA/CA/AA chip-authentication outcomes, normalized RFID OCR/DG2 portrait data, persisted KYC operations, and emitted NFC-specific MAS events.
- Designed the security and operational model around server-authoritative NFC verdicts, PII-safe observability, private provider-result handling, transaction correlation, trust-material management, and deployment/secret/license controls. Use as interview design depth; select final résumé wording only after choosing the targeted role.

### Product enablement, research, documentation, and operations

- Integrated Paddle OCR scoring for Malaysian cards and passports with product-ID whitelisting/toggles, supporting client-specific controls while providing data for in-house-model development.
- Evaluated Philippines identity-verification approaches using LLM extraction, Microblink, and KTP models; also evaluated Indonesia driving-license support and Microblink versions for passport/card reliability.
- Deployed My-Liveness and My-Tijori to complete the GeoX ecosystem, and deployed Doc-Verify for Indonesia OCR/IDV services.
- Led a full GitBook/Confluence documentation overhaul for the Malaysia service, covering OCR, IDV, liveness, face match, API workflows, errors, and examples in a single structured reference.
- Owned testing, monitoring, alert response, production issue resolution, and delivery support; introduced ChatGPT-assisted PR review to improve review efficiency.
- **GXS Bank Singapore manual review — research/design only:** contributed technical discovery and contract/flow design across SDK status, backend decisioning, Jira/webhook behavior, HMAC, retry/DLQ, idempotency, status history, threshold bands, and critical-field interactions. Do not describe as implementation ownership.

### Metrics and proof bank for final selection

- **90%** reduction in image-storage database usage.
- OCR/IDV average latency below **1 s**; liveness/face-match below **2 s**.
- 5 TPS performance validation: OCR/IDV about **1,418 ms p99**; video-liveness/face-match about **2,870 ms**.
- Microblink upgrade: at least **70%** accuracy improvement; approximately **15 s → 3–4 s** latency.
- FAR/FRR analyses: **3,000+** cards and **500+** cards across distinct workstreams.
- Billing error-flow/event gap: approximately **20–30%** before rewrite and event-coverage work.
- Summary API response-size reduction: approximately **70%**.

<!-- Paste rough notes, projects, incidents, launches, migrations, ownership, metrics, stack, and collaborations here. -->

## NOTE for gpt - let me know in case these review pointers are not enough or something, or in case you need more context on tasks or something like that since these were for internal review there hold a lot of service names and other things. Let me know in case you need documents or other details to get more details on what i do what we have done, etc.

## Questions / facts to confirm later

- Exact tiket.com end date and final designation. jan 2024 , final designation sde 1 only
- VIDA joining date, current designation, location/work mode, and core stack. - 29th jan 2024 joining date, current designation sde 2, prmoted in april 2026, joined as sde 1, core stack - java, spring, redis, kafka, mysql, 
- Correct public URLs for LinkedIn, GitHub, and any portfolio. - already exisiting in the google doc from where you would have pulled the data for this page -
  https://docs.google.com/document/d/1WlWwkTsti1u-X8fi-f44vrKxsV78yamM/edit, linkedin - https://www.linkedin.com/in/agarwal-swapnil/, gfg - https://www.geeksforgeeks.org/profile/swapnil78945, leetcode - https://www.google.com/url?q=https://leetcode.com/aristrader/&sa=D&source=docs&ust=1788093166258068&usg=AOvVaw1rSy0PuvOXQ7VMBYDZLy0z
