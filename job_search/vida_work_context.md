# VIDA work context — evidence inventory

Purpose: retain enough technical and business context to build a truthful master résumé later without repeatedly rediscovering the same details.

This is not a final résumé and not a claim that every listed change shipped to production. The task list establishes Swapnil's involvement; the local `my-verify` repository is technical evidence of the design/code present there. Confirm release state, individual ownership, scale, and business outcome before converting an item into a résumé bullet.

## User-confirmed delivery and ownership

- All listed VIDA workstreams except GXS Bank manual review were personally owned and delivered
  end-to-end by Swapnil: design discussions, implementation, testing, release, and associated
  delivery/metrics.
- GXS Bank manual review was personally owned through research/design and is currently under
  implementation as part of the new orchestration platform/service. It must not be described as a
  delivered implementation.
- The two performance-review submissions in `resume_master.md` are the primary source for confirmed outcomes and metrics. Treat repository evidence as technical substantiation, not a replacement for those stated results.

Source root: `/Users/swapnilagarwal/IdeaProjects/my-verify`

## System context

`my-verify` is a Java/Spring verification service with a staged pipeline: preprocessing, parallel verification stages, then post-processing. It supports KYC flows, document OCR/authentication, liveness/face match, fraud evaluation, audit/status storage, and integrations with external document-verification providers.

Primary source: `docs/codebase-context.md`.

## Critical fields

### Technical context

- Critical-field behavior is configured per normalized card group: MyKad, MyKas, MyPR, MyTentera, and Indonesia KTP.
- The configuration carries whether enforcement is enabled, critical OCR/landmark fields, and minimum pass thresholds.
- The utility resolves the current card type to the correct product configuration at runtime. Unknown or missing configuration falls back to a disabled default and logs the configuration problem.
- In the wider OCR flow, a failed critical field can produce an error while a failed non-critical field produces a warning; summary and full responses have different behavior.

### Evidence

- `src/main/java/id/vida/verify/myservice/util/CriticalFieldConfigUtil.java`
- `src/main/java/id/vida/verify/myservice/model/CardCriticalFieldsConfig.java`
- `docs/codebase-context.md` - OCR and critical-field sections.

### Confirm later

- What exact customer/product or compliance requirement prompted this work?
- Did the change add new card types, make config self-service, or change verification decisions for an existing market?
- Was it shipped? What volume, error-rate, onboarding, or manual-review outcome changed?

## KYC operations and status flow

### Technical context

- KYC behavior is available through the v2 request path, which adds request context including the KYC flow type.
- Each applicable verification stage can record operation-level status for a KYC transaction. The system persists KYC case/operation state and recomputes an overall status.
- NFC design work extends the same KYC model with `documentCaptureMode` as a path switch, NFC operation recording after stage execution, and an NFC section in the KYC status response.
- The design evaluated separate flow types versus an alternate NFC path inside existing flows; the local design records the alternate-path approach as selected.

### Evidence

- `docs/codebase-context.md`
- `docs/nfc-kyc-operations-design.md`
- `docs/nfc-kyc-extension-design.md`
- `src/main/resources/db/kyc_flow_ddl_queries.sql`
- `src/main/java/id/vida/verify/myservice/audit/KycStorageFacade.java`

### Confirm later

- “KYC” is broad. Which KYC task was personally owned: operation persistence, status recomputation, a new flow, API changes, or something else?
- Which countries/clients used it, and was it a net-new onboarding capability or an internal reliability change?

## Liveness and FraudShield integration

### FraudShield technical context

- FraudShield is an external fraud-evaluation stage in the verification pipeline.
- It is gated by v2/KYC eligibility, a supplied shield ID, and product-level enablement flags.
- The integration uses a Feign client, handles client failures, retries `IN_PROGRESS` results with configurable attempts/backoff, maps result risk to configurable warning/error thresholds, and persists evaluation metadata.
- When fraud reaches the configured error threshold, all requested KYC operations are upgraded to an error state using a worst-of status update.
- The code separates execution from result persistence and propagates request/MDC context into async work.

### Liveness technical context

- Liveness is a separate verification path. The pipeline supports liveness and face-match results independently; liveness transaction IDs can supply the selfie used by downstream face matching.
- The repository contains image and video liveness stages plus client/event handling, but the exact change owned under the “liveness integration” task is not yet identifiable from the supplied task name alone.

### Evidence

- `src/main/java/id/vida/verify/myservice/service/IdFraudShieldService.java`
- `src/main/java/id/vida/verify/myservice/verify/stage/impl/FraudShieldVerificationStage.java`
- `src/main/java/id/vida/verify/myservice/feign/FraudShieldApiFeign.java`
- `src/main/java/id/vida/verify/myservice/client/LivenessClient.java`
- `src/main/java/id/vida/verify/myservice/verify/stage/impl/LivenessVerificationStage.java`
- `src/main/java/id/vida/verify/myservice/verify/stage/impl/VideoLivenessVerificationStage.java`
- `docs/codebase-context.md` - Face and FraudShield sections.

### Confirm later

- Did the task deliver FraudShield, liveness, or both? Which parts were personally implemented versus integrated/tested/released?
- What upstream service, customer need, fraud/compliance goal, or measurable risk reduction did it support?

## Presigned URL introduction

### Technical context

- The get-transaction read path was changed to return short-lived presigned S3 download URLs instead of proxying Base64 image bytes through `my-verify`.
- The response can provide front, back, and portrait document URLs. URL generation is requested in parallel when documents are present.
- The storage client exposes a dedicated presigned-download endpoint; error handling maps missing documents separately from generic storage failures.
- URLs are treated as bearer credentials and redacted from string/log representations.

### Evidence

- `src/main/java/id/vida/verify/myservice/client/StorageClient.java`
- `src/main/java/id/vida/verify/myservice/service/StorageService.java`
- `src/main/java/id/vida/verify/myservice/service/VerifyApiService.java`
- `src/main/java/id/vida/verify/myservice/verify/documents/PresignedDocumentDTO.java`

### Confirm later

- Was the goal response-size/latency reduction, service-load reduction, mobile/client download reliability, or all of these?
- Which API consumers migrated, and what production metric or infrastructure cost changed?

## 1:N enrollment / duplicate and watchlist checks

### Technical context

- The v2 flow supports 1:N image-search behavior for identity-face duplicate checks and image blacklist/watchlist behavior.
- Duplicate checking is scoped by a country/card allowlist, requests with no prior errors, and required portrait/document fields. For the documented configuration, the duplicate path targets Malaysia MyKad-family cards.
- A detected match can become an error for high-risk profiles or a warning otherwise; the system records explicit duplicate-check results in the v2 response.
- The separate face blacklist endpoint supports searching and enrollment/blacklisting; enrollment uploads an image and returns a face ID.

### Evidence

- `docs/codebase-context.md` - Face: liveness, match, 1:N dedup, blacklist/watchlist.
- `src/main/java/id/vida/verify/myservice/service/FaceVerifyService.java`
- `src/main/java/id/vida/verify/myservice/controller/FaceController.java`

### Confirm later

- Does “1 to N enrollment” mean blacklist enrollment, KYC duplicate prevention, a different provider, or all of them?
- Was this released to a named market/customer, and what policy/risk threshold or operational outcome did it enable?

## Regula image-based document verification

### Technical context

- The integration replaces or reroutes the existing ASG Neo/Microblink document-authentication path for non-Malaysian, non-KTP cards through a configuration-controlled Regula provider.
- The provider maps in-house inputs to Regula requests for document type, authenticity, image quality, OCR text, and images.
- Response mapping turns provider-specific results into a common result: document metadata, authenticity/IQA results, OCR fields and confidence, OCR-source selection, portrait extraction, and normalized failures.
- The implementation has explicit handling for missing/invalid provider responses and separates provider-specific interpretation from common business-response mapping.
- Design material covers OCR source precedence for MRZ versus visual data, field mappings, quality/authenticity decisions, error normalization, downstream billing/events, and observability.

### Evidence

- `src/main/java/id/vida/verify/myservice/regula/service/RegulaProvider.java`
- `src/main/java/id/vida/verify/myservice/regula/service/RegulaRequestMapper.java`
- `src/main/java/id/vida/verify/myservice/regula/service/RegulaImageResponseMapper.java`
- `src/main/java/id/vida/verify/myservice/regula/docs/context.md`
- `src/main/java/id/vida/verify/myservice/regula/docs/design.md`
- `docs/confluence/epassport-nfc-regula-technical-review.md`

### Confirm later

- Was the production scope limited to selected cards/countries, or did it open a particular new market/client?
- Which elements were personally implemented: API client, request model, response mapping, routing, tests, release/hardening, or architecture/design?
- Any throughput, accuracy, provider-cost, reliability, or onboarding metric?

## Regula NFC / ePassport verification

### Technical context

- NFC extends the provider path from optical document processing to passport-chip verification.
- NFC requests use a distinct Regula request shape: document type, status, text, and images; they do not use optical authenticity/IQA result types because chip data is cryptographically verified.
- The documented result model includes access-control and authentication outcomes, with RFID checks such as PA, CA, AA, PACE, BAC, and TA requiring provider-specific semantic mapping.
- The KYC design adds NFC-specific operation recording and response/status behavior while preserving existing image-based flows.
- A detailed technical review addresses zero-trust delivery, secure transaction finalization, private result retrieval, PII-safe observability, operational metrics, trust material, and deployment requirements.

### Evidence

- `src/main/java/id/vida/verify/myservice/regula/service/RegulaNfcResponseMapper.java`
- `src/main/java/id/vida/verify/myservice/regula/service/RegulaRequestMapper.java`
- `docs/nfc-verification-api-contract.md`
- `docs/nfc-kyc-operations-design.md`
- `docs/nfc-mas-events-design.md`
- `docs/confluence/epassport-nfc-regula-technical-review.md`

### Confirm later

- What was built versus researched/designed? Is ePassport NFC already released, in pilot, or future work?
- Which client/country/market need made NFC verification important?
- What portions did Swapnil own end-to-end: design, contract, provider mapping, KYC flow, eventing, tests, deployment readiness, or stakeholder review?

## Provider framework for future third-party integrations

### Technical context

- The service defines provider-agnostic document-verification contracts, separate capability interfaces for image and NFC verification, and a common processing-result model.
- Provider-specific code is isolated in its own package while the pipeline stage remains provider-neutral.
- A resolver/routing layer is designed to select the provider from configuration instead of hard-coded stage branching.
- The design intentionally keeps the provider layer extractable into libraries/modules and preserves a migration path for shadow mode, split capabilities, new providers, and future BPMN/DMN orchestration.
- The approach avoids one stage per provider; it preserves one common stage and moves provider-specific interpretation behind the interface/mapper boundary.

### Evidence

- `src/main/java/id/vida/verify/myservice/verify/externalprovider/`
- `src/main/java/id/vida/verify/myservice/regula/docs/provider-abstraction.md`
- `src/main/java/id/vida/verify/myservice/regula/docs/future-evolution.md`
- `docs/nfc-design-extension.md`
- `docs/nfc-composition-approach.md`

### Confirm later

- Which parts are implemented now versus a documented future architecture?
- Did the framework materially reduce the effort to add Regula/NFC or prepare a real second provider?
- Is there a clear before/after integration time, number of providers/capabilities, or business expansion result?

## GXS Bank manual-review flow

### Research/design context only

- Local material captures the documentation/research plan for a GXS Singapore KYC manual-review flow.
- It separates product decisions, SDK responsibilities, backend decision-engine/Jira/webhook behavior, and API/field contracts.
- Topics explicitly considered include manual-review status returned to the SDK, OCR and IDV result shape, warnings/errors alongside manual review, inbound-verdict authentication, HMAC signing, retries/DLQ, idempotency, status history, threshold bands, and coexistence with critical-field configuration.
- The local evidence does not show a manual-review implementation. Per the user’s description, someone else developed it.

### Evidence

- `docs/confluence/gxs-sg-kyc/_conventions.md`

### Résumé treatment

- Do not present this as implementation ownership.
- Consider it only if the work can be described precisely as cross-functional technical discovery/design that informed a delivered GXS KYC capability. It is likely lower priority than shipped backend integrations unless it demonstrates substantial ownership, stakeholder influence, or a distinctive system-design contribution.

### Confirm later

- What concrete artifact did Swapnil deliver: requirements analysis, decision doc, flow/contract design, feasibility review, stakeholder alignment, or handoff?
- Did the work unblock a GXS launch or materially reduce ambiguity/rework?

## Cross-cutting evidence worth retaining

- Java/Spring backend work with Feign-based external APIs, asynchronous stages, resilience/retry, configuration-driven behavior, audit/status persistence, response contracts, tests, observability, and PII-aware security design.
- Work spans verification/compliance-sensitive flows: document authenticity, OCR, KYC, liveness/face matching, fraud risk, document storage, and ePassport NFC.
- Strongest likely résumé themes: external-provider integration, backend ownership across request-to-result paths, extensible architecture, security/privacy considerations, and market/client enablement. These remain themes, not final bullets.

## Detailed audit addendum

### Critical fields and KYC

- `CardCriticalFieldsConfig` supports enabled/disabled enforcement, OCR and landmark field sets, and minimum passing-field thresholds. OCR/landmark critical failures differ from non-critical warnings; aggregate minimum-field failures have their own error paths.
- The inspected code uses a disabled fallback config for unknown/missing card mapping. This is implementation context, not necessarily the intended product policy.
- KYC state uses `kyc_status` and `kyc_operation_attempts`; NFC is modeled through `document_capture_mode=NFC` while alternate image-flow attempts remain history. The inspected configuration has a seven-day KYC timeout and five-minute batch scheduling.

### Liveness, 1:N, blacklist, and FraudShield

- Face match uses only the last normalized liveness transaction ID when multiple are supplied. Liveness and face-match errors are separate, and face match uses a cropped document face or ID front as its reference.
- 1:N duplicate checks require v2, OCR, enabled configuration, an eligible card/country, no prior errors, portrait, ID number, and front-document ID. The source allowlist includes Malaysian MyKad variants, MyKas, MyPR, MyTentera, MyKid, and Indonesia KTP; passport is excluded.
- Verify-time blacklist search runs before face match and can short-circuit it. The separate blacklist endpoint supports both search and enrollment, although its billing is documented as a remaining TODO.
- FraudShield calls `/api/v1/evaluations` with product context, retries only `IN_PROGRESS`, maps code `8004` separately, and treats unknown risk as fail-open. Inspected defaults are three attempts and 200 ms delay.

### Presigned storage URLs

- Presigned document URLs are produced for v2 transaction retrieval when document inclusion is requested. They cover persisted ID images, liveness/face-match images, portraits, and blacklist-enrollment assets as applicable to storage.
- Storage error mapping detects a missing document from upstream response-body code `1604`, even if upstream returns HTTP 500; blank URLs in successful-looking responses also fail.
- The local sources do not establish production URL TTL, revocation, downstream authorization, or external access-audit policy.

### Regula image and NFC boundaries

- The image provider catches provider/processing/unexpected failures into a sealed outcome and stores image-stripped provider details. Manual Java mapping is an explicit design choice.
- Deferred/not-proven-production image items include configurable OCR-source preference, production environment verification, quality/security scans, and ASG cleanup.
- The implemented NFC path is v2-only: a Regula process call followed by one result fetch, not a polling loop. It requires completed processing, passport classification, RFID containers, and DG2 portrait data. PA is mandatory; CA/AA are optional.
- The ePassport technical review supplies the detailed control model for ingress, replay protection,
  trust-store operations, analytics, licensing, metrics, and deployment. The user has confirmed that
  the Regula/NFC integration is released and live; retain source-level evidence boundaries for any
  particular operational control until its owning service is inspected.
- A sample-value inconsistency exists between the draft technical review and the implemented NFC mapper; use the versioned mapper/code as truth for implementation claims.

### Framework and GXS scope

- The implemented framework has image/NFC capability interfaces and a Regula provider implementing both. Current resolver behavior is simple: return Regula.
- Config/DMN routing, multi-module extraction, shadow mode, split providers, and in-house-provider unification are future designs, not current deliverables.
- GXS evidence is limited to research/documentation conventions and open decisions. It supports a truthful design/discovery contribution only, not a shipped implementation claim.

## Line-by-line audit coverage

Two read-only audits were completed before any résumé trimming:

- **Regula/NFC audit:** read the full 3,720-line NFC/Regula document corpus, including the ePassport technical review, NFC API/KYC/event/design documents, all Regula package design documents, and concrete provider/request/response-mapper code.
- **Verification-feature audit:** read the critical-fields, generic KYC, liveness/face-match, FraudShield, Tijori/presigned-URL, 1:N, blacklist/watchlist, GXS, configuration, DDL, test, and relevant pipeline sources.

The detailed import in `resume_master.md` preserves the strongest line-by-line findings. Before asking a new question about a listed workstream, check this file first and then the source paths named under that workstream.

### High-value distinctions retained from the audits

- Mobile NFC capture is evidence, not the authoritative verification verdict; the server/provider result is the intended trust boundary.
- The current NFC implementation performs one process call plus one result retrieval with images, not a polling loop. It requires passport/RFID data and uses DG2 portrait data for face-match support.
- KYC state is idempotent and concurrency-aware; status is recomputed from the active image/NFC path, while prior attempts remain audit history.
- FraudShield, duplicate checks, and watchlist checks use configurable risk policy. A low-risk match can warn while a high-risk match can error, so outcome is not simply “match equals reject.”
- Presigned URLs are bearer credentials; source code redacts them, but production TTL/revocation/access policy is not established in the local sources.
- Regula image integration is current implementation. Provider extraction, routing by config/DMN, shadow mode, split capabilities, additional providers, and BPMN changes are future architecture.
- The ePassport technical review contains valuable security/operations design, but not proof of production delivery. GXS manual review is research/design only in the local source set.

## Git-history corroboration

The following history confirms that the inspected features were developed as distinct workstreams. Commit history is evidence of repository evolution, not proof of Swapnil's individual authorship; combine it with the supplied task list and review material before using it in the résumé.

- **Critical fields:** history records KTP critical/minimum OCR and landmark configuration, strict KTP portrait enforcement, Malaysian-card OCR/landmark extensions, centralized card-type selection, and critical-field error-code handling (`VENGG-15001`, `VENGG-15008`).
- **KYC:** history records the initial KYC flow, `groupId` status API, operation-level status updates, final outcome/billing/event behavior, requested-operation validation, safe concurrent status writes, duplicate-liveness handling, stale-case cleanup/deadlock prevention, and history-only status summaries (`VENGG-14672`, `VENGG-14735`, `VENGG-14755`, `VENGG-14766`).
- **FraudShield:** history records the external fraud-service integration, verification/post-stage placement, retry/in-progress behavior, separate GeoX configuration, safe error handling, generic fraud-message masking, transaction-not-found handling, threshold persistence, and escalation of requested KYC operations (`VENGG-15116`, `VENGG-15203`, `VENGG-15226`).
- **Liveness and face match:** history records liveness-transaction validation, liveness-result evaluation, use of the liveness image for blacklist/face match, biometric billing correction, validation/error handling, and persistence/context fixes.
- **1:N and blacklist:** history records KTP duplicate-check enablement, duplicate-check recomputation, duplicate face-check hotfixes, and removal of an accidental duplicate invocation (`VENGG-15010`).
- **Presigned URLs:** history records the shift from Base64 responses to presigned S3 URLs in get-transaction, the Tijori endpoint rename, missing-document surfacing, and response-contract cleanup (`VENGG-15404`).
- **Regula image integration:** history records Feign/config/error handling, request construction, response mapping, mapper extraction, ASG/Regula fail-safe switching, IQA circuit breaking, date/card-type normalization, skipping conflicting Paddle OCR work, and hardening/code-review work (`VENGG-1550`, `VENGG-15635`, `VENGG-15636`, `VENGG-15638`, `VENGG-15639`).
- **Regula NFC:** history records NFC pipeline integration, response mapper/audit work, KYC status integration, provider transaction persistence, event publication/testing, and portrait/data-storage corrections.
- **GXS manual review:** history contains documentation updates that closed product decisions, but the audited local sources still do not prove a GXS backend/manual-review implementation.

## Next facts to collect

For each task that may reach the final résumé, capture:

1. Time period and exact role.
2. What Swapnil personally owned versus designed/reviewed/supported.
3. Release state: shipped, pilot, in progress, research only, or superseded.
4. Who benefited: client, country/market, internal team, or platform.
5. Measurable result: traffic, latency, availability, conversion, fraud loss, onboarding time, manual-review volume, cost, or developer effort.
6. One difficult technical decision or trade-off worth discussing in interviews.

## Confirmed outcomes and scope from the impact questionnaire

Source: `job_search/vida_impact_questions.md`, fully read through line 797. The business outcomes below are Swapnil-provided facts unless a `my-verify` source is named. They are suitable for internal résumé selection; client names and service names must not appear in the public résumé.

### Service ownership, scale, and turnaround

- Swapnil is the sole owner of the MyVerify service and has also worked on MyLiveness (liveness/face match), Tijori (image storage and BYOK), the ID-fraud service, and idi-mobile (the entry gateway for web/mobile SDK requests).
- At joining, MyVerify was a service curated for one customer, with reported downtime above 90% and no client onboarding. It now reportedly serves **20+ customers**, operates in **two active regions**, is being prepared for **two more**, and contributes roughly **5% of company revenue**. These are user-provided business facts; the repository does not independently prove customer/revenue/uptime figures.
- The performance turnaround brought a reported 8–10-second average latency to the customer commitment of under two seconds, alongside the already recorded sub-one-second OCR/IDV and sub-two-second liveness/face-match results.
- Clarification (2026-09-04): the earlier 5-TPS figure is from an older performance test. Swapnil confirms a
  later OCR/IDV validation at **20 TPS** with approximately **1.4 s p99**; use the later figure in the active
  résumé while preserving the older raw questionnaire answer as historical context.
- Root causes addressed across the turnaround included incorrect AWS/version/server configuration, high-latency flows, fragmented S3/database image storage, memory leaks, and response/flow issues. Tijori became the common storage path for storing and maintaining verification data.

### Client controls, KYC orchestration, and market enablement

- Malaysian-card work reportedly opened further Malaysia-market opportunities and created early discussion/entry points for Philippines and Thailand. The code supports the Malaysian/KTP card families; commercial expansion is user-provided rather than repository-proven.
- V2 provided richer response structures, feature extensibility, and client support. Critical-field policy gave customers product-level control over hard versus soft rejection and card-acceptance policy, intended to improve conversion and stickiness.
- A company customer-NPS figure of approximately **61** was supplied as product context. It must not be attributed to critical-fields work or used as a personal impact metric without direct evidence.
- KYC changed previously individual verification operations into a backend-managed journey: clients can configure OCR, IDV, liveness, face match, and fraud steps; the platform persists attempts/retries/success-failure transactions and exposes one KYC-journey view. The codebase substantiates persisted operation attempts, effective-path handling, history, and overall-status recomputation.

### Fraud, duplicate prevention, and watchlist context

- FraudShield was Swapnil's backend integration with a third-party fraud service. The SDK/data/model layers collected 1,000+ signals; do **not** attribute that signal collection or model performance to Swapnil's backend work.
- The delivered product purpose was stronger fraud/compliance coverage, including fraud-transaction detection, duplicate-account prevention, fraudulent-document prevention, and client-controlled face/user blacklisting.
- No measured fraud-catch rate, FAR reduction, conversion change, or duplicate-case volume has been provided. Never create a 30–40% (or other approximate) metric for the résumé; keep the final claim qualitative unless a defensible measurement is later available.

### Storage, presigned URLs, BYOK, and PII

- Presigned URLs replaced Base64 document transfer to reduce payload size, provide time-bounded access, shift download load directly to S3, and reduce application-server/cost pressure. The source code confirms short-lived parallel presigned URL retrieval and log redaction.
- Two separate response-size metrics were supplied: the summary endpoint reduced response size by roughly **70%**; the Base64-to-presigned-URL path reportedly changed individual responses from roughly **1–2 MB** to **45–90 KB**. Do not merge these metrics; validate their measurement scope before using either in a final bullet.
- BYOK is a Tijori KMS/envelope-encryption capability designed for regulated-customer key-control/audit requirements. The supplied design uses a shared bucket with `productExternalId`/path isolation, a customer KMS-key mapping held in OSS, and a Vida-managed default key for unconfigured customers.
- User-provided BYOK validation includes multi-key testing in one bucket, compatibility with existing encrypted data, and an annotation-platform integration through Tijori presigned URLs without copying data.
- BYOK status must be precise: delivered through development, QA, and customer sandbox testing; the named regulated-bank customer's production deployment was still pending its production key and auditor approval. Do not claim production rollout or name the bank/key/environments publicly.
- OSS-backed PII controls are independently supported in `my-verify`: product configuration can disable PII storage and audit payloads are conditionally image-filtered.

### Billing, SSP, traceability, and operational support

- SSP is a client-facing portal that surfaces transaction events and details, helping customers investigate and raise issues. Group-level traceability and expanded events improve customer/support visibility.
- The billing rewrite addressed an estimated **20–30%** missed event/error-flow gap. No measured recovered-revenue figure exists; retain only the estimated missed-event gap if needed.
- Audit/logging work improved compliance trail, reduced duplicate records, and strengthened database/audit structures. No specific incident-resolution or support-time metric has been provided.

### Regula image/provider integration and rollout context

- Regula was introduced through a parallel rollout/fallback path, with an intended eventual replacement/more-provider direction. It supported passport-oriented/newer-market capability, including Philippines-oriented work, and reduced dependency on the existing ASG/Microblink path.
- The source code confirms configuration-controlled Regula routing, ASG/Paddle fallback paths, and a provider boundary with image/NFC capability interfaces. Current `ProviderResolver` returns Regula only; configuration/DMN routing, a second provider, and broader plug-and-play routing remain future evolution.
- The checked-in configuration has `EXTERNAL_PROVIDER_REROUTE_TO_REGULA` defaulting to false, while design documents discuss a true/default rollout. Treat exact production routing/rollout state as environment-dependent; do not claim full replacement unless separately verified.
- The major architecture decision is deliberate manual Java mapping plus capability-specific provider interfaces: provider-specific semantics stay in the provider boundary while downstream code receives a common result. MapStruct/JOLT/reflection alternatives were evaluated and rejected because the work is behavioral, not merely structural.

### NFC/ePassport — detailed design and implementation boundary

- User supplied the full July 2026 NFC technical design review. It is a draft-for-engineering-review artifact covering an Android ePassport product line, complete server-side verification, mobile SDK/DocVer wrapper, MyVerify orchestration, an ASG provider adapter, protected Regula Web Service ingress, MySQL/S3 storage, trust material, observability, licensing, and deployment.
- Core trust model: mobile MRZ/NFC capture and `finalizePackage()` provide evidence plus an opaque transaction ID, never the final authenticity verdict. The server-side provider result, retrieved under a transaction/journey binding and normalized at the provider boundary, is authoritative.
- Released end-to-end flow: Verify supplies the mobile SDK the required initialization material; the
  Vida-owned wrapper initializes Regula, captures MRZ/RFID/PACE/BAC evidence, finalizes a package,
  returns a transaction ID, and Verify requests the normalized authoritative outcome through the
  provider boundary.
- The selected Android package set (`api + common + ocrandmrzrfid`) was estimated at **86.34 MiB** before APK/AAB optimization. The design explicitly treats this as a customer-integration/download-size trade-off and avoids coupling consuming SDKs to Regula classes, callbacks, endpoints, or enums.
- Design scope includes PACE/BAC, DG reading, PA, DSC-to-CSCA validation, and AA/CA when available; excludes iOS, non-NFC optical hardware, DG3/DG4 EAC/terminal-authentication work, liveness/face-match/sanctions/account binding, issuing-state PKI operations, and long-term raw-artifact retention.
- The model defines normalized PASS/FAIL/INDETERMINATE/NOT_SUPPORTED/SYSTEM_ERROR semantics, stable failure buckets, versioned field-specific status mapping, and a strict rule not to apply a global zero/non-zero interpretation to Regula status values.
- Security/operations design covers protected endpoint allow-listing, TLS/pinning and optional mTLS, journey/tenant/environment binding, nonce/JTI replay prevention, idempotent finalization, payload/rate/concurrency limits, restricted PII logging, curated PII-minimized analytics, secret handling, trust-store lifecycle, online-license monitoring, dashboards, alerts, artifact TTL, and multi-worker deployment.
- The design calls for online-license access as a distinct external dependency, with license type/capacity/grace-period confirmation before production approval. It also calls for a private result-retrieval route and requires production raw artifacts/debug logging to be disabled by default.
- Current `my-verify` evidence confirms a V2 NFC path, Regula provider/mappers, KYC alternate-path
  status, and NFC event/result handling. The user has separately confirmed release/live status for the
  Regula/NFC integration. The code audit alone does not establish every exact infrastructure/ownership
  detail of the broader topology, so use the technical review and user confirmation together rather
  than treating a single repository module as exhaustive evidence.
- A source conflict remains: the technical review targets provider result retrieval through ASG with polling and no images by default; checked-in MyVerify directly calls Regula once for processing and once for results with images to obtain DG2 portrait data. Current versioned implementation is the source of truth for code claims.

### Web SDK, OSS, research, and GXS

- The web-SDK wrapper unlocked a new web product channel. No adoption/customer metric was provided, so retain it as product enablement rather than quantified impact.
- OSS is the product-configuration platform used for phased rollout, client-specific feature enablement, and customer control of conversion/risk behavior. Redis supports cached dynamic configuration. This is the business explanation behind the technical configuration work.
- Philippines/Indonesia evaluation work improved product/model understanding and contributed to new-market/client discussions. It has no standalone launch/adoption metric; select it only if needed for a market-expansion narrative.
- GXS began with an unclear PRD. Swapnil owned the non-coding technical discovery: converting it into the required design pieces, flows, considerations, and handoff material. The audited repository still contains no GXS-specific implementation; keep it as a design/discovery achievement only.

### Public-resume and evidence rules after reconciliation

- Do not include client names, internal service names, KMS key identifiers, bucket/environment names, sensitive implementation endpoints, or license/secrets details publicly.
- Use percentage metrics supplied by Swapnil only with their exact scope: **90%** storage-cost reduction, **8–10s to <2s** stated latency turnaround, sub-one-second/sub-two-second flow targets, 70% summary-size reduction, 20–30% missed-billing-event gap, 20+ customers/two active regions/two preparing/~5% revenue, and the explicit benchmarking figures already in the review.
- Keep unmeasured claims qualitative: fraud prevention, conversion/stickiness, market expansion, customer support visibility, Web SDK enablement, duplicate/watchlist effect, research outcomes, and documentation/PR-review impact.
- Before asking follow-up questions, inspect this section, the earlier technical inventory, and the raw questionnaire. The only remaining material follow-ups should be questions that cannot be derived from those sources, such as final public wording preferences or a direct measurement needed for a specific chosen bullet.

## Detailed questionnaire appendix — preserve for interview and résumé work

This appendix retains the material detail behind the impact answers. It supplements, rather than replaces, `vida_impact_questions.md`; the latter remains the verbatim source, including the complete NFC design review. It deliberately distinguishes a supported implementation from an intended target architecture.

### 1. MyVerify turnaround and multi-service scope

- Baseline context: MyVerify had been curated for one customer, had no active onboarding, and was reported to experience more than 90% downtime. The turnaround work covered production deployment, critical bug fixing, flow optimization, response standardization, memory-leak remediation, AWS/version/server correction, and storage-path cleanup.
- Result context: the service reportedly moved to 20+ customers, two active regions, two regions in preparation, and about 5% of company revenue. Use this only as the overall platform-growth story—not as proof that any one feature caused all growth.
- Service boundaries: MyVerify owns OCR/IDV/KYC orchestration; MyLiveness handles liveness/face-match calls; Tijori is the image-storage service; the ID-fraud service provides fraud capability; idi-mobile is the web/mobile-SDK entry gateway. Public wording should describe the capabilities, never these service names.
- Storage consolidation: the reason Tijori matters is not merely S3 access. It created one path to store, retrieve, and maintain verification data instead of fragmented database/S3 handling, and it helped address unnecessary storage and application-server work.

### 2. Card coverage, accuracy work, and client integration

- Malaysia work included major card/model coverage and a Microblink upgrade; its stated outcome was improved success/accuracy figures, latency, uptime, and SLA performance. The provided 70% figure refers collectively to internal success/accuracy improvements in model runs, not a named external benchmark. Preserve that ambiguity until final-bullet wording is chosen.
- FAR/FRR exercises were used to evaluate the verification/model flows. No direct decision (for example a particular threshold, release gate, or model-selection choice) was supplied, so do not invent one.
- V2 was a client-facing API evolution: richer/simpler responses, more verification features, easier integration, improved extensibility, and support for a growing client set. It should not be reduced only to an error-handling refactor.
- Critical-field configuration was product/client policy control: customers could decide hard versus soft treatment for card/OCR/landmark failures and customize acceptable card-failure behavior. The repository confirms per-card/per-product configuration with error/warning outcomes and minimum thresholds.

### 3. KYC ownership and user-journey model

- Before the KYC work, verification operations were individually managed. The platform now centrally orchestrates a configurable KYC journey rather than forcing clients to compose and track every component themselves.
- The backend retains per-operation attempt count, retry state, transaction identifiers, success/failure state, required operations, effective image-versus-NFC capture mode, and overall KYC status. Earlier attempts are kept as history when the active path changes.
- This gives clients a single place to see where a user is in the KYC journey and lets the product offer different OCR/IDV/liveness/face-match/fraud combinations. The technical implementation uses KYC status/operation persistence, idempotence, and status recomputation, with `ERROR > REVIEW > VERIFIED` precedence.

### 4. Fraud and biometric-risk work — careful ownership framing

- Backend work: integrate the third-party fraud evaluation into the platform request/KYC lifecycle, propagate request context through asynchronous work, apply product-configured threshold policy, persist evaluations, retry only valid in-progress outcomes, and escalate KYC operations when policy requires.
- Product/data work outside this backend claim: the SDK/data/model side collected 1,000+ fraud signals and produced fraud-model outcomes. In a final résumé, say Swapnil integrated the fraud service and its decisioning into the backend—never that he built the SDK signal collection or fraud model.
- Biometric-risk portfolio also includes multi-frame liveness decisioning, color-print detection, image-manipulation checks, face duplicate search, and blacklist enrollment/search. The unifying business story is a more complete fraud/compliance package that protects clients while retaining configurable warning/error behavior.
- There is no supported numeric fraud/FAR improvement. Never use an estimated fraud percentage merely because the direction is clear.

### 5. Duplicate checks and blacklist behavior

- Duplicate checks require an eligible V2/OCR request, product configuration, permitted card/country, clean upstream status, portrait, ID number, and front-document reference. The platform can upload a portrait on demand when required data is absent.
- Duplicate and watchlist matches are policy/risk decisions rather than automatic rejection: high-risk profile can produce an error; lower-risk profile can produce a warning. The verify-time blacklist runs before face match; a separate API supports direct face search/enrollment.
- Intended business value: prevent duplicate accounts, detect repeat fraudulent identity/document use, and allow clients to blacklist faces/users. No actual detection volume, loss reduction, or market-specific deployment metric is available.

### 6. Presigned retrieval and BYOK architecture

- Transaction retrieval hydrates persisted document IDs and obtains front/back/portrait presigned URLs in parallel. Clients obtain document content from S3 instead of receiving Base64 through MyVerify; URLs are bearer credentials, redacted from logs, and handled as explicit document-retrieval failures if storage cannot provide them.
- The reported 45–90 KB value applies to the presigned-URL response path after removing 1–2 MB Base64 payloads. The separate 70% metric applies to the summary response. Treat both as distinct measurements in all later drafts.
- BYOK uses envelope encryption: data is encrypted with a generated data key; that key is encrypted by the customer-selected AWS KMS key. A shared bucket is isolated using product/region/source/date/file path partitioning; OSS maps a customer product identifier to its KMS key ARN and falls back to a Vida-managed key when no customer key is configured.
- Validation included multiple customer keys in the shared-bucket approach, decryption through presigned Tijori access without copying data into another system, compatibility with existing encrypted data, and an external annotation-platform integration.
- Deployment detail is material: implementation progressed through development, QA, and a regulated-customer sandbox, while the customer's production use required a production key and external compliance approval. This is a strong security/enterprise-readiness story but not a production-launch metric.

### 7. SSP, billing, audit, and operational data

- SSP is not only internal observability: customers use it to inspect transaction details/events and surface issues. Expanded data includes images, scores, warnings, errors, summaries, backside-processing events, and group-level correlation across services.
- The billing change rebuilt error-flow/event coverage, where the previous estimate was 20–30% missed events. It can support “closed a billing-coverage gap” wording, but not a revenue-recovered number.
- Audit/logging work included traceability, more useful database trail/status data, standardized/clearer logs, and reduced duplication. Treat the outcome as compliance/support visibility unless a hard incident or support metric appears later.

### 8. Regula integration and provider-boundary design

- Regula image work has two distinct stories: (1) a config-controlled parallel/fallback integration that expands passport/non-core-card capability and reduces single-vendor dependence, and (2) an internal architecture boundary designed to localize provider-specific complexity.
- The image path maps in-house requests into Regula document/authenticity/IQA/OCR/image processing and maps the provider response back to common document, authenticity, IQA, OCR, confidence/source, portrait, and error results. It keeps provider payloads image-stripped for auditability and handles unsupported cards and independent OCR-only/IDV-only flows.
- Manual Java mappers were chosen after evaluating MapStruct, JOLT, and reflection/config mapping: field movement is simple, but status interpretation, error normalization, conditional result construction, and fallback behavior are behavioral logic.
- Capability-specific image/NFC interfaces prevent invalid routing at compile time and isolate provider code from downstream business mapping. The current implementation has a Regula provider for both; true multi-provider config/DMN routing, shadow testing, split capability merging, and module extraction are future designs, not completed product claims.

### 9. NFC/ePassport technical-design dossier

- **Product boundary:** Android ePassport verification uses a Vida-owned DocVer wrapper around Regula Mobile SDK. The wrapper owns vendor lifecycle/configuration, MRZ/RFID capture, PACE/BAC access, package finalization, transaction-ID mapping, and vendor-error translation; consuming applications see Vida-owned contracts only.
- **Authority boundary:** `finalizePackage()` returns an opaque transaction ID. A successful device capture is evidence collection, not an accepted authenticity decision. The transaction ID must be bound to the KYC journey before server-side retrieval, and the normalized server/provider response decides the result.
- **Component separation:** Verify owns journey/KYC policy and initialization material; the provider boundary owns Regula request/result normalization, retries, and correlation; the protected ingress limits mobile transaction traffic; Regula Web Service retains state/processes RFID evidence; MySQL stores transaction/reprocessing metadata; S3 holds encrypted artifacts; PKD/CSCA material supports passport trust validation; the analytics model receives curated PII-minimized facts rather than raw identity data.
- **Data-flow detail:** the backend supplies protected ingress and encrypted license material to the SDK; DocVer initializes Regula; the mobile flow reads MRZ/RFID/DG evidence and finalizes the package; SDK reports the opaque transaction ID; Verify binds it to the journey; the backend provider path retrieves and normalizes the authoritative result; KYC policy publishes final state and the SDK gets the normalized outcome.
- **Verification semantics:** PACE/BAC are chip-access prerequisites. PA validates signed-object/data-group integrity and trust; CA/AA are capability-dependent authentication checks. The design supports PASS, FAIL, INDETERMINATE, NOT_SUPPORTED, and SYSTEM_ERROR rather than treating every provider status as a simple Boolean.
- **Mapping caution:** Regula has several numeric status domains. The implementation must map by field/schema semantics, preserve unknown values as indeterminate, and never use a global zero/non-zero rule. This is an interview-quality integration decision.
- **Security controls designed:** restricted transaction endpoint allow-list, HTTPS/pinning/optional mTLS, short-lived channel credential, nonce/JTI replay controls, one active transaction per journey, tenant/environment binding, idempotent finalization/reporting, rate/payload/concurrency limits, PII/secret exclusion from normal logs, and private server-side result retrieval.
- **Operations designed:** a curated transaction fact model (correlation IDs, versioning, duration, process attempts, HTTP status, normalized outcome, trust/policy version, and artifact-presence flags); structured logs; latency/reliability/capacity/trust/license/retention metrics; alerts for license expiry, error/latency burn, missing results, trust-chain anomalies, DB/S3/worker issues, and overdue artifact deletion.
- **License/deployment design:** online licensing is separate from document processing; mobile receives an encrypted license through an authenticated backend path while the server license stays in a secret store. Production design requires controlled outbound license access, pinned images, managed/private encrypted MySQL and S3, trust-material promotion/rollback, restricted metrics, and multi-worker resilience. License entitlement model/capacity and network-loss grace period must be confirmed before production approval.
- **Scope limits:** iOS, DG3/DG4/EAC/terminal authentication, liveness/face match, sanctions/AML/account binding, issuing-state PKI operations, and long-term raw RFID/MRZ/DG/portrait retention are explicitly outside this design's scope.
- **Reality check:** the repository proves a narrower current V2 path in which MyVerify's Regula provider performs one process call and one results call with images for DG2 portrait data. The full target topology, ASG-separated polling path, protected ingress, mobile wrapper, and production operational controls remain design/review material unless separately evidenced.

### 10. Web SDK, OSS/Redis, research, and GXS discovery

- The Web SDK wrapper is a product-channel enablement effort with HMAC signing, encrypted config, roles, and CORS; describe its result as a new web SDK product line unless adoption evidence later emerges.
- OSS is the controlled product configuration layer: it enables phased feature rollout, per-customer feature access, and policy changes around conversion and risk without requiring code releases. Redis makes those configuration decisions dynamically available in the serving path.
- Philippines/Indonesia work covered POCs/model investigation and FAR/FRR/product evaluation. It creates useful market-expansion context but has no confirmed public launch metric.
- GXS design work began with an unclear PRD. Swapnil completed the non-coding analysis/design: decomposing the flow, API/SDK contract, decision rules, manual-review state, Jira/webhook/HMAC/retry/DLQ/idempotency/status-history considerations, threshold policy, and handoff concerns. Do not upgrade this to a delivered implementation.

### Remaining evidence state — no immediate follow-up required

- Technical behavior is now fully covered by the local source audit, the raw questionnaire, and this context file.
- The only absent evidence is numerical business measurement for fraud outcomes, duplicate/watchlist outcomes, Web SDK adoption, documentation/PR-review productivity, and individual market launches. These are optional; the final résumé can be strong without them.
- The next correct action is not another broad questionnaire. Build a candidate long-form VIDA experience section from this dossier, then ask only if a specific final bullet would materially improve with one missing number or scope clarification.

### Service-ownership map and latest user corrections — September 1, 2026

This section captures the service labels and scope added as inline notes during long-form résumé review.
Internal service names remain interview context only; public résumé wording stays capability-based.

- **MyVerify:** central verification/KYC orchestration service. It owns KYC transaction details and
  coordinates the liveness, object-storage, model/provider, and fraud services; it also publishes the
  relevant billing/transaction events. The turnaround, V2 API, KYC workflow, policy controls,
  presigned retrieval, document-provider integration, NFC path, and major response/flow cleanup are
  primarily MyVerify work.

- **MyLiveness:** owns liveness, face match, active-liveness, color-captcha, and other in-house
  biometric/fraud-model integrations. Fraud effects must be carried through this service as well as
  MyVerify, including final responses, KYC records, and billing events. Multi-frame liveness,
  image-manipulation, and color-print work belongs primarily here.

- **Object-storage service:** the storage consolidation was not merely an S3 API change. It integrated
  a shared artifact service across MyVerify, MyLiveness, and related flows, replacing duplicate manual
  S3/database storage paths. It owns the BYOK implementation and supports presigned access; MyVerify
  owns the client-facing transaction-retrieval integration.

- **Fraud service:** the third-party fraud metric/decision integration is hosted in a dedicated fraud
  service and integrated into MyVerify and MyLiveness. Swapnil's ownership includes making fraud
  outcomes affect KYC state, final API response, persisted records, billing events, and interactions
  with other fraud modules. Do not claim ownership of the underlying SDK signal collection or model.

- **SDK entry gateway:** the mobile/web-SDK gateway is the entry point for SDK calls and internally
  invokes MyVerify. It contains the web-SDK channel work—HMAC signing, encrypted configuration,
  role-based access, CORS, and controlled rollout—and participates in the KYC integration surface.

- **Legacy document-verification path:** the third-party document-verification SDK upgrade was applied
  in the legacy provider/service called by MyVerify. Its v2.7-to-v3.4 work is a vendor integration,
  not a new in-house model. Supporting work included approximately 150-card validation, larger
  FAR/FRR and OCR-scoring exercises, model-data support, fine tuning, and logic changes from findings.

- **Document models and alternatives:** the OCR-scoring/configuration work covered POCs, testing,
  vendor and open-source evaluation, custom matching logic, configuration tuning, and two routes:
  an earlier custom OCR/matching approach and the later provider-based route. Keep this as
  integration/model-evaluation depth, not a claim of owning model training.

- **Customer portal and billing:** the customer portal itself is a Kafka-event consumer maintained by
  another team. Swapnil owned the event integrations and enrichment in MyVerify and MyLiveness that
  made richer transaction details available to the portal: scores, images, errors, statuses, warnings,
  summaries, and back-side processing visibility. Billing reliability work spans MyVerify and
  MyLiveness; do not claim ownership of the portal UI/service.

- **Provider boundary:** the image/NFC provider boundary currently lives in its own package inside
  MyVerify. It was intentionally structured so it can be extracted into a reusable library if and when
  broader reuse requires it. Do not claim that this extraction or a general multi-provider router has
  already shipped.

- **NFC/ePassport product scope:** the MyVerify NFC path extends document coverage toward NFC passports
  and is released and live. The provider/SDK flow returns a transaction identifier to the backend,
  which retrieves the relevant provider result and uses it in the KYC journey. The technical review
  retains the detailed operational controls and component boundaries for interview use.

- **Manual review/orchestration:** the GXS/manual-review work began as an unclear PRD and was developed
  into an implementation-ready flow, contract, decision policy, webhook/Jira, retry/DLQ, idempotency,
  and status-history design. **Latest status correction:** it is currently under implementation as part
  of a new orchestration platform/service; do not say it was delivered by another engineer.

- **Commercial context to validate before résumé use:** the user reported that the GXS/manual-review work
  supports entry into a new region and has approximately USD 400K in annual revenue potential. This
  figure is not corroborated by the local source set, so retain it for later validation rather than
  presenting it as a résumé metric.

- **User-provided provider and NFC context:** the provider boundary is intended to make third-party and
  eventual in-house integrations easier, with broader card/passport coverage and less vendor dependence.
  Swapnil estimates that the reusable integration approach halved time to integrate a new vendor; retain
  this as a user-provided estimate unless a before/after implementation baseline becomes available.
  The user also described NFC passports as a new product line that helped two critical client pursuits.
  The reusable package, provider mapping, coverage expansion, and server-authoritative result flow are
  evidenced above; the deal outcome requires validation before résumé use.

- **User-provided orchestration aspiration:** manual review is intended to contribute to a centralized,
  backend-driven orchestration layer with a single KYC entry/exit flow and a thinner SDK. Retries,
  idempotency, audit history, Jira routing, verdict ingestion, and status recomputation are documented
  workflow design details. The broader orchestration platform remains under implementation and must not
  be presented as a completed delivery until independently verified.

#### NFC transaction-ID replay protection — verified current state and target design

- **What the current MyVerify code proves:** the V2 NFC request carries a caller-supplied provider
  `transactionId` alongside `groupId` and `partnerTrxId`. `RegulaProvider.process(...)` passes that ID
  directly to the provider's `/process` and `/results` endpoints, then maps the returned result. The
  current provider class does not itself show a lookup that binds the provider transaction ID to the
  KYC journey, customer/product, environment, expiry, or a consumed/one-time-use state.

- **What that does not prove:** this code inspection cannot establish the final security posture of the
  public API, mobile gateway, or provider ingress. The request is authenticated/validated elsewhere,
  and an upstream layer may perform additional controls. But no transaction-binding or replay guard was
  found in the inspected MyVerify NFC provider path, so it must not be claimed as currently implemented
  until that upstream path is verified.

- **What the target technical design requires:** a transaction ID is only an opaque pointer to provider
  evidence, never proof that a passport is valid. Verify must bind it to the authenticated KYC journey,
  customer/product, environment, and expected capture state; the provider boundary must reject an
  unbound, expired, consumed, superseded, cross-tenant, or cross-environment ID before processing or
  retrieving results.

- **New explicit design evidence:**
  `my-verify/nfc/01-epassport-nfc-verification-technical-review-regula.md` says Verify validates the
  reported transaction ID and stores the journey binding; ASG then rejects an unbound, expired,
  consumed, superseded, cross-tenant, or cross-environment ID. It also requires short-lived channel
  credentials, protected ingress, nonce/JTI replay tracking, one active transaction per journey,
  idempotent finalization/reporting, and private server-side result retrieval.

- **Evidence boundary:** that new review is marked **Draft for engineering review**. It documents the
  intended control set, and says the Android wrapper handoff is implemented in `nfc-docver`, but it does
  not by itself prove that Verify/ASG persistence and rejection checks are deployed. Keep “design
  requires” separate from “current code implements” until those services are inspected.

- **Why this stops replay:** if an attacker resends a valid ID, the binding check prevents it from being
  attached to a different user, product, or KYC journey. The backend retrieves the provider result over
  an authenticated server-to-server route and returns a normalized verdict; it does not trust a result
  supplied by the client. A legitimate duplicate request may return the same bound journey's idempotent
  result, but must not create a new accepted verification or substitute another person's result.

- **Required controls from the target design:** short-lived transaction/channel credentials;
  high-entropy IDs; one active provider transaction per journey; nonce/JTI replay tracking; idempotent
  finalize/result handling; state-transition checks; tenant/environment binding; expiry; and
  rate/payload/concurrency limits. These are design requirements, not verified current code controls.

- **Evidence:** current direct NFC calls are in
  `my-verify/src/main/java/id/vida/verify/myservice/regula/service/RegulaProvider.java`; the V2 request
  contract is in `my-verify/docs/nfc-verification-api-contract.md`; the target binding/replay design is
  in `my-verify/nfc/01-epassport-nfc-verification-technical-review-regula.md` (transaction flow and
  security-controls sections), with a parallel technical-review copy under `docs/confluence/`.

### Résumé-consolidation trace — September 1, 2026

This records the cleanup decisions made in `resume_long_form_draft.md`. It is a preservation aid, not a
separate résumé. A consolidation is valid only when its factual detail remains in an active bullet and/or
the raw master/context sources listed below.

- **SDK upgrade / 70% / FAR-FRR:** the active SDK-upgrade bullet retains v2.7→v3.4, 15s→3–4s, at least
  70% internal accuracy, ~150-card validation, and 3,000+/500+ FAR/FRR coverage. The original review
  details remain in `resume_master.md`; model-data support, fine tuning, and logic changes remain below.

- **Portrait, passport date format, and response fixes:** portrait extraction and date-format support are
  retained in the document-coverage bullet; response/error handling and false-passport-rejection logic
  are retained in the V2 API bullet. Original technical detail remains in `resume_master.md` and the
  Regula/provider sections above.

- **OCR scoring and model evaluation:** the active OCR/model-evaluation bullet retains product toggles,
  allowlists, vendor/open-source POCs, custom OCR matching, manual validation, model-data support,
  configuration tuning, and findings-driven logic changes. The two implementation routes—earlier
  Paddle/custom matching and later provider-based integration—remain in the raw note under that bullet,
  in `resume_master.md`, and in the service map above.

- **Production deployment scope:** the active platform-turnaround bullet retains deployment and
  stabilization across verification, liveness, shared storage, and Indonesia OCR/IDV. The original
  deployment inventory remains in `resume_master.md`.

- **Live Regula/ePassport work:** active provider and NFC bullets retain the live integration, opaque
  transaction ID, backend KYC path, server-side authoritative result retrieval, normalized RFID/PACE/
  BAC/PA/CA/AA data, DG2 portrait handling, KYC persistence, events, protected ingress,
  transaction/journey binding and replay controls, private result retrieval, PII-minimized
  observability, and license/trust-material/deployment operations. The fuller technical detail remains
  in the NFC dossier above.

- **POM/YAML and AI-assisted PR-review work:** retained under `Rejected from résumé selection` in the
  long-form draft because infrastructure/operations owned the broader setup. It remains available for a
  genuine JD-specific merge, but must never be included simply for ATS keywords.

- **Cleanup and research status:** the ~5,000-line cleanup/model-service upgrade is now consolidated
  into the active platform-turnaround and V2 API bullets; its raw detail remains in `resume_master.md`.
  Market POC work remains in the draft's `Consider` section, and GXS manual-review design remains in
  its design/discovery section. Their full raw context remains in `resume_master.md` and this file.
