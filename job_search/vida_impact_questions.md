# VIDA impact questions

Purpose: capture the outcomes needed to choose final resume bullets. Answer briefly in this file or in chat. “Don’t know” is a valid answer.

Already confirmed: Swapnil owned and shipped every listed VIDA workstream end-to-end except GXS Bank manual review, which was research/design only. Do not repeat ownership or release state.

## 1. Malaysia service deployment and performance

Known: database image-storage use fell 90%; OCR/IDV averaged under 1s; liveness/face match under 2s; performance testing reached 5 TPS.

- Which client(s), product(s), or market relied on this deployment? Was it a launch, migration, or rescue of an existing production service? - i was deployed in the my-verify service which was a service not live in production specifically curated for 1 customer at that point in time, now the service has been expanded into a service currently serving 2 active reagions and being prepared for 2 more in parallel, when i joined the down time of the service was more than 90% of time and no client on boarding, now it servers more than 20 customers and draws about 5% overall company revenue. iam the compelte and sole owner of this service apart from this i have served in the liveness service (hadnles livenesss and facematch calls, tijori service which is the image storage service and the BYOK part was done for it only. apart from this i had worked in id fraud service creation and integration, and also the idi-mobile which is the service that acts as a entry gate for the web and mobile sdk calls.)
- Did the latency/storage work improve a customer-visible SLA, reduce infrastructure cost, or resolve a production incident? Which outcome matters most? - storage reduced the costs of storage by 90% for this one, and the latency promised to customers was under 2 s which when i joined was on a average of 8-10s
- What were the most consequential technical changes behind the result (for example: an image-storage design change, flow change, AWS fix, or version upgrade)?  - i have found multiple issues in the service including but not limited to aws versioning incorrect servers being hit, high latency, s3 storages, db storages , etc, etc. the tiori service helped streamlined a lot of this being the single point of storage and also providing a single way of storing and maintaining the data.

## 2. Malaysia card models, Microblink, and accuracy

Known: MyKad/MyTentera/MyKas/MyPR and KTP work; Microblink v2.7 → v3.4; at least 70% accuracy improvement; about 15s → 3–4s latency; FAR/FRR analysis on 3,000+ and 500+ cards.

- Did full Malaysian-card coverage or KTP support onboard/retain a specific client or enable a new market? Name it if resume-safe. - yes we tapped newer markets of malaysia using this and also gave us a entry point or discusssion points in phil and thai
- What exactly does the 70% accuracy improvement represent: success rate, a particular model metric, or an internal benchmark? - improved successs rates, accurancy numbers on the model runs, and the latency improvements, up times and SLA's
- Which customer/business problem did the FAR/FRR analysis change—model selection, thresholds, release approval, or something else?

## 3. MyVerify V2, critical fields, and KYC status

Known: V2 improved error/spoof/IQA/face-mismatch responses; critical-field policy is configurable; KYC status is persisted and recomputed across operations.

- Who consumed V2 first—mobile SDK, web SDK, internal services, or named clients—and what integration/onboarding issue did it solve? - newer better response structures, more features, better extensiblity, higher supports , more clients
- Did configurable critical fields reduce false rejects, enable a compliance policy, or remove release work for customer-specific rules? Any before/after or concrete example? - help give more options to clients in terms of the controls they had, the hard soft rejections and customisation in terms of card failures acceptance etc, helped improve the conversion number for clients and also helped improve client stickiness, the nps of our customers is around 61.
- Did KYC status enable a client journey, asynchronous follow-up, compliance/audit visibility, or support/debugging improvement? What made it worth building? - earlier each operation was individual it helped bind it all under the kyc flow, maintained details on backend like retry counts, the attempt no, success and failure transaction if the kyc is verified or not, so the orchestraction and the kyc management was taken control by us instead of client and we gave them different options to customise as to what all steps they need in there kyc , options inlcueded ocr, idv, liveness, facematch, fraud, easier integrations for clients, one stop solution to see where the user is at in there kyc journey, etc

## 4. Liveness, FraudShield, image manipulation, and color-print detection

Known: multi-frame consensus decisioning; FraudShield risk evaluation with thresholds/retries/status escalation; image-manipulation and color-print checks.

- Which fraud or compliance scenario triggered this work, and who needed it (customer, country, product, or internal risk team)? - this helped improve the security and complieance and offered a better and complete product to the clients, the fraud one had signal collections done at the sdk, collecting 1000+ parameters (although this sort of comes under the sdk and data team work on the models my part was backend integration and a backend integration with a third party for more fraud related signal collection and security) , helped reduce and catch the fraud transactions, bring down the FAR signifivcantyl (we can generate a random number say 30 or 40% to quanitfy (similarly for other places you can ask me also and if i dont have number we can do a rough estimation)), iproved revenue and busineed and made the product more compelte and packaged.
- Did these checks have an observed fraud catch rate, reduction in false accepts, conversion change, or rollout volume? Approximate is fine. - i think already answered above if not let me know otherwise
- What was the hardest design decision: retry/fail-open policy, error versus warning thresholds, preserving conversion, or integration with KYC status? - is this detail relevant for creating the interview point? we have done it all though in this.

## 5. 1:N duplicate checks and blacklist enrollment

Known: V2 face-duplicate and watchlist checks; high-risk matches error and lower-risk matches warn; separate enrollment/search API.

- What business problem did this solve: duplicate-account prevention, fraud, sanctions/watchlist policy, or something else? - yes hepled prevent duplciate accounts, helped reduce fraud when people tried to submit fraud documents, etc, and also helped give clients the options to blacklist users and faces to reduce the fraud impact.
- Which market/client/card types used it first, and was it used at onboarding or for an existing population? - we can say all for simplicity
- Do you know any scale or outcome: duplicate cases caught, manual review avoided, fraud prevented, or launch dependency? - i dont have the numbers as such. (maybe we approximate i guess) you can find my company details on internet as well maybe they have made some claims online regarding these things.

## 6. Tijori storage, presigned URLs, BYOK, and PII controls

Known: presigned S3 URLs replaced Base64 transaction payloads; summary response shrank about 70%; BYOK and configurable PII-storage controls were delivered.

- Which API/client pain did presigned URLs solve: large response payloads, mobile reliability, service load, download latency, or cost? - reduced payloads, provided ttl, , reduced load at our servers since all load directed to the s3 directly, cost minimisation in terms of servers, etc.
- Was the 70% response-size reduction from the summary endpoint, presigned URLs, or both? Keep the final resume claim precise. - 70% response reduction and each of usage in summary, presigned url removed the base 64 and hence reduced response sizes from the 1-2 mb to a few kbs of upto 45 to 90kb only
- Did BYOK/PII controls unlock a regulated customer, a security requirement, or a contractual/compliance approval? - i will provide you the summary here let me know if more details are required -BYOK (Bring Your Own Key) is a KMS encryption solution that unlocks Vida's ability to serve regulated customers like Ryt Bank (Malaysian bank) by enabling them to use their own AWS KMS keys for S3 data storage in Tijori, addressing a core regulatory audit requirement where auditors demand client control of cryptographic keys for hosted data; the architecture uses a single shared bucket with data isolation via productExternalId and folder structure (/<region>/<productId>/<documentSource>/<year>/<month>/<day>/<fileIdentifier>), implementing envelope encryption where data is encrypted with a generated key, which is then encrypted by KMS; configuration maps each client's productExternalId to their specific KMS key ARN in OSS, with default Vida-managed keys for non-configured clients; extensive testing validated multi-key encryption in single buckets and successful Encord annotation platform integration via Tijori presigned URL APIs that handle decryption without copying data; the solution maintains backward compatibility with existing encrypted data and was deployed across dev (test-byok), QA (vida-qa-tijori-sse), and sandbox (vida-sandbox-tijori-3) environments; Ryt Bank completed sandbox testing with their client KMS key (mrk-3559bb3d8340440f8251fc9aadc89ac0), and production deployment is pending their production key and compliance auditor approval following the agreement reached at an in-person meeting in Kuala Lumpur with a ~1-week timeline from key receipt.


## 7. Tijori/SSP, billing, traceability, and observability

Known: SSP events and `groupId` traceability; billing rewrite addressed an estimated 20–30% missed event/error-flow gap; audit trails and logs improved.

- Did the billing fix produce a measured revenue recovery, or should we only state the previously missed-event percentage? - i dont have measured revenue recovery maybe just 20-30% should suffive rigth?
- Who used the SSP/traceability improvements—support, operations, finance, customers, or internal analytics—and what did it make faster/easier? - ssp is our portal for the clients to see the events the details what happened in the transactions ,etc, etc. gives visibliity clients can raise issues if any , etc.
- Was there a production incident or recurring support problem that the new logging/audit trail resolved? - better compliance and reduced duplications better trail, better db tables essentially.

## 8. Regula image integration and provider framework

Known: configuration-controlled Regula image verification, normalized provider mappings, and image/NFC capability contracts behind a provider-neutral pipeline.

- Which country/cards/customer use case required Regula, and did it enable a new market or reduce a dependence on ASG/Microblink? - enabled newer markets, for passports, phliipnes, etc, reduce dependency allowed more 3rd party vendors.
- Was this a replacement, fallback, parallel rollout, or net-new provider capability? What was the rollout strategy? - parallel rollout eventually net replavecment also newer capability of nfc has been added right, also the framework for easy 3rd party plug and play, please go through the my-verify docs properly cover the details prperly you will find all answers there.
- What was the key architectural trade-off you personally decided: manual mapping, capability-specific interfaces, configuration switching, data handling, or error normalization?

## 9. Regula NFC / ePassport

Known: V2 NFC path; chip authentication/OCR/DG2 portrait mapping; KYC status and MAS event integration; server-authoritative verification design.

- Was NFC/ePassport live for a particular client/country, pilot, or all eligible consumers? What product need drove it? - newere product line and increased revenue and more the clients that can be tapped better product finish
- What is the business value: stronger identity assurance, faster onboarding, compliance, passport coverage, or a specific launch requirement? - i am not sure what you want to ask in this question nfc passport verification i think all you questions should be self answered no?
- Which of these should be the interview story: untrusted mobile capture/server-authoritative result, alternate image/NFC KYC paths, RFID-status mapping, or privacy-safe provider integration? - full no trust model - pasted it below -  
  1 Review summary
  2 Overview
  3 Glossary
  4 Scope
  4.1 In scope
  4.2 Out of scope
  5 Architecture
  5.1 Trust and decision boundaries
  5.2 Data Flow
  5.2.1 Result semantics
  5.2.2 Zero-trust delivery contract
  5.2.2.1 Runtime Topology
  5.2.2.2 End-to-end customer and service journey
  5.2.2.3 Wrapper SDK interface for Mobile
  5.2.2.4 MyVerify-to-Regula Webservice contract
  5.2.2.4.1 Reprocess Transaction
  5.2.2.4.2 Transaction Result
  5.2.2.4.3 Java Sample
  5.3 SDK Side
  5.3.1 Responsibilities
  5.3.2 Selected package and size review
  5.3.3 Normalized failure buckets
  5.3.4 Security of the direct finalizePackage() channel
  5.4 Server Side (Web Service)
  5.4.1 Database
  5.4.1.1 Holistics transaction fact
  5.4.2 Observability
  5.4.2.1 Logs
  5.4.2.2 Metrics
  5.4.3 Online license
  5.4.3.1 Runtime mechanism and failure behavior
  5.5 Deployment
  6 References
  Document control
  Value
  Status
  Draft for engineering review
  Repository
  nfc-docver
  Audience
  Mobile, Backend, Platform/SRE, Data, Security, QA
  Prepared
  16 July 2026
  Evidence base
  Repository configuration, Regula tests in local and dev environment, service-context diagram, Regula official documentation
  Proposed decision  
  Adopt complete server-side verification with the selected Regula mobile package api + common + ocrandmrzrfid (approximately 86.34 MiB of dependency footprint). Verify Service supplies the VIDA-hosted DocReader ingress URL and license binary to VIDA SDK, which initializes Regula through the DocVer wrapper. finalizePackage() returns only an opaque transactionId. VIDA SDK reports that ID to Verify; Verify asks ASG to independently retrieve and normalize the Regula server result before returning the verification result to VIDA SDK.
  Review summary

The design separates evidence acquisition from the trust decision. The Android-side VIDA DocVer wrapper embeds Regula Mobile SDK for MRZ/NFC capture, PACE/BAC access, RFID reading, and server-package finalization. finalizePackage() returns a transactionId, not an authenticity verdict. Verify Service carries the KYC journey, while ASG owns the provider-specific Regula adapter, transaction binding, authoritative result retrieval, and normalized result mapping.
The selected mobile dependency set is api + common + ocrandmrzrfid, estimated at approximately 86.34 MiB before final APK/AAB optimization. This keeps OCR/MRZ/RFID capture on the device while moving the authoritative authenticity decision to the Web Service. The footprint remains a material client-integration concern and must be measured as a per-device Play/App Bundle download, not only as raw AAR size.
Regula licensing is online. Verify Service supplies the encrypted Regula license binary to VIDA SDK as part of the initialization response. VIDA SDK supplies the binary to the DocVer wrapper, which feeds it into Regula SDK during initialization without exposing it to the integrating application. The backend Web Service keeps its server license in the approved secret store. Runtime entitlement still requires outbound HTTPS access to Regula's main and backup licensing services. The license model (transaction-based or instance/worker-based) changes outage behavior and scaling cost, so it must be confirmed before capacity approval.
Local testing demonstrated the complete Web Service path: transaction start, encrypted process calls, result retrieval, MySQL transaction persistence, S3 artifact persistence, and detailed DEBUG traces. Successful samples returned CoreLibResultCode=0, ProcessingFinished=1, RFID overall=0, PA=0, CA=0, and AA=1; detailed RFID evidence showed AA was performed and its signature was valid. Because several Regula status domains use different numeric conventions, the integration must map by field semantics rather than assume that zero always means success.
Overview

This Technical Design Review defines the target integration for Android ePassport verification in nfc-docver. It covers the VIDA SDK wrapper, Regula Mobile SDK, Verify Service, ASG, mobile-license delivery from the VIDA backend, a VIDA-hosted Regula Web Service, server-side verification, transaction persistence, observability, analytics, and deployment. It uses a zero-trust-to-mobile model: a successful NFC read is evidence collection, not an accepted authenticity verdict.
Verify Service starts the VIDA journey and returns the environment-specific DocReader ingress URL and encrypted license binary to VIDA SDK. VIDA SDK supplies both values to the DocVer wrapper, which initializes Regula SDK and configures complete server-side verification. Regula SDK communicates directly with the protected VIDA ingress for the Regula transaction protocol. After finalizePackage() returns the opaque transactionId, VIDA SDK submits it to Verify; Verify requests the authoritative outcome through ASG, and the normalized result is returned to VIDA SDK.
Glossary

Term
Definition
AA
Active Authentication; chip challenge-response using the DG15 public key.
BAC
Basic Access Control; legacy MRZ-derived access protocol.
CA
Chip Authentication; stronger chip-authentication/key-agreement mechanism.
CSCA
Country Signing Certification Authority; trust anchor for a passport DSC.
DG
ICAO Logical Data Structure data group, for example DG1 (MRZ), DG2 (portrait), DG15 (AA key).
DSC
Document Signer Certificate used to sign EF.SOD.
EF.SOD
Signed Object Document containing data-group hashes and the document signer signature.
Holistics
Analytics/BI platform receiving a curated, PII-minimized transaction fact table.
ICAO PKD
ICAO Public Key Directory distributing eMRTD trust material.
MRZ
Machine Readable Zone; passport number, date of birth, and expiry help derive NFC access keys.
PA
Passive Authentication; validates DG hashes and EF.SOD signature/trust path.
PACE
Password Authenticated Connection Establishment; preferred chip access protocol when supported.
Regula SDK
On-device Document Reader component used for OCR/RFID capture and server-package handling.
Regula Web Service
VIDA-hosted server component that stores transaction state and reprocesses evidence.
OL
Regula Online License; requires Internet connectivity to Regula's licensing service.
SSV
Complete Server-Side Verification; Regula's zero-trust-to-mobile flow.
Scope

In scope

Android ePassport MRZ-to-NFC journey and RFID evidence collection.
Regula Mobile SDK integration through a VIDA-owned DocVer abstraction layer that limits vendor coupling.
Verify Service orchestration and ASG-owned Regula backend adapter/result normalization.
Mobile-license download by VIDA SDK, secure handoff to the wrapper, server-license secret handling, outbound licensing connectivity, renewal/heartbeat behavior, and license monitoring.
Protected direct Regula SDK-to-Web-Service transaction channel, transaction binding, replay protection, and authoritative result retrieval.
Selected api + common + ocrandmrzrfid package and its mobile delivery footprint.
Complete server-side verification through a VIDA-hosted Regula Web Service.
PACE/BAC, DG reading, PA, DSC-to-CSCA validation, and AA/CA when available.
MySQL transaction metadata, S3-compatible artifact storage, and PKD/CSCA material.
Normalized outcomes/errors, transaction analytics, logs, metrics, dashboards, alerts, retention, and deployment controls.
Out of scope

iOS implementation details and non-NFC optical authenticity hardware.
DG3/DG4 access requiring EAC/Terminal Authentication.
Face matching, liveness, sanctions/AML, or customer identity-account binding.
Issuing-state CSCA/DSC operations and final business acceptance policy.
Long-term retention of raw RFID sessions, MRZ, DG content, or portrait images.
Architecture

The service context supplied for this review is retained below. It shows online-license dependencies on the mobile and server components, the VIDA SDK wrapper around Regula SDK, the VIDA-managed Regula Web Service, database and S3 storage, the CSCA directory, Grafana, Holistics, and external ICAO/BSI trust-material sources.

Open image-20260731-100416.png
image-20260731-100416.png

Component
Responsibility
VIDA SDK
Starts the client journey, receives the license binary and DocReader ingress URL from Verify, initializes DocVer, receives transactionId from finalizePackage(), reports it to Verify, and consumes only the normalized VIDA result.
VIDA DocVer wrapper
VIDA-owned abstraction over Regula SDK. Feeds the VIDA-supplied license binary and URL into Regula SDK initialization; owns vendor configuration, lifecycle, package finalization, transactionId mapping, and error translation.
Regula Mobile SDK
Performs MRZ/RFID acquisition, PACE/BAC, live server-side session calls, encrypted package finalization, and returns transactionId to the wrapper.
Verify Service
Owns KYC journey orchestration and policy; supplies mobile initialization material, binds the reported transactionId to the journey, asks ASG for the authoritative outcome, and returns it to VIDA SDK.
ASG
Authoritative Source Gateway and provider boundary; owns the Regula backend adapter, transaction correlation, /process and /results calls, normalized result mapping, retries, and provider observability.
Protected VIDA ingress
Only public route to required Regula transaction endpoints; terminates TLS, authenticates the mobile session, enforces path/payload/rate/replay controls, and keeps Regula Web Service private.
Regula Web Service
Creates transaction state, stores live RFID session material, reprocesses finalized packages, performs PA/AA/CA checks, and exposes results to ASG.
MySQL
Stores vendor transaction/reprocessing metadata and migration state; it is not the raw-document store.
S3
Stores encrypted requests/responses, metadata, session keys/challenges, processing results, and optional SDK error artifacts.
PKD / CSCA directory
Mounted or synchronized trust material from ICAO, BSI, and approved sources for PA chain validation.
Observability / analytics
Prometheus + Grafana for operations; curated fact table to Holistics for volume, outcome, latency, and debugging slices.
Regula licensing service
External entitlement service reached over outbound HTTPS 443 by the online-licensed components; distinct from VIDA's DocReader processing URL.
Trust and decision boundaries

The device is untrusted for the final authenticity decision. Mobile outcomes are capture and transport evidence only.
The Regula Web Service result is authoritative only after ASG independently retrieves it for a transaction bound to the VIDA journey; a mobile capture result or transactionId alone is not a verdict.
Verify owns KYC orchestration and policy. ASG owns the server-side provider adapter and normalized authoritative-source contract. Regula owns the transaction protocol and verification engine.
Consuming VIDA components depend on VIDA interfaces and normalized models, not Regula classes, callbacks, endpoints, enums, or response shapes. Mobile vendor types stay in DocVer; backend vendor APIs and mappings stay in ASG.
The analytics layer receives only curated, PII-minimized transaction facts; it does not query or replicate raw Regula artifacts.
The DocReader URL supplied to VIDA SDK is a protected VIDA ingress URL, never the private Regula service address. Regula licensing URLs remain a separate entitlement path.
Data Flow

#
Stage
Route
Data / operation
Handling
1
Request initialization data
VIDA SDK -> Verify
Initialization request

2
Supply initialization
Verify -> VIDA SDK
Protected DocReader ingress URL, license binary, short-lived channel credential
Environment controlled; no app-selected URL
3
Initialize Regula
VIDA SDK -> DocVer -> Regula SDK
Feed license binary and backend-processing config during SDK initialization
Wrapper contains all vendor-specific initialization
4
Capture NFC evidence
ePassport <-> Regula SDK
MRZ, PACE/BAC, EF.SOD, DG1/DG2
Minimum data; secure state in memory
5
Finalize package
Regula SDK -> protected ingress -> Regula WS
Start transaction, live encrypted process calls, finalizePackage()
Only required transaction paths; no raw service exposure
6
Return transactionId
Regula WS -> Regula SDK -> DocVer -> VIDA SDK
finalizePackage() callback returns opaque transactionId
Capture completion only; not verification verdict
7
Report transaction
VIDA SDK -> Verify
transactionId
Verify accepts the transaction
8
Request outcome
Verify -> ASG
Provider-neutral authoritative-result request
Verify remains unaware of Regula endpoints/enums
9
Retrieve and normalize
ASG <-> Regula WS
POST /process; poll GET /results?withImages=false;
map PA/AA/CA/RFID
Validate transaction binding; idempotent retries
10
Return VIDA result
ASG -> Verify -> VIDA SDK
PASS/FAIL/INDETERMINATE + stable error bucket
Backend result only; raw vendor details restricted

Result semantics

Outcome
Meaning
PASS
All checks required by the active policy passed.
FAIL
A genuine authenticity check failed, such as DG hash, SOD signature, or trusted-chain validation.
INDETERMINATE
No authoritative result due to processing, dependency, trust freshness, or unsupported-policy conditions.
NOT_SUPPORTED
A capability such as AA/CA is absent; policy decides whether the journey can still pass.
SYSTEM_ERROR
SDK/Web Service/network/storage/database failure; retry or operational handling, not a fraud verdict.
Zero-trust delivery contract

Runtime Topology

Open Service context diagram-Page-18.drawio (1)-20260727-053006.png
Service context diagram-Page-18.drawio (1)-20260727-053006.png
End-to-end customer and service journey

Verify creates an initialization envelope to Vida SDK. The provider values are opaque to Verify policy code: protected DocReader ingress URL, Regula license bytes.
VIDA SDK constructs VidaDocVerificationConfiguration and initializes the DocVer wrapper. The wrapper validates the required license and server URL, initializes Regula, and keeps the license out of logs and public application callbacks.
The wrapper runs the OCR_NFC flow. MRZ capture supplies passport number, birth date and expiry to PACE/BAC access; the NFC profile requests DG1, DG2, DG14 and DG15 plus the evidence needed by server-side verification.
During the live chip read, Regula Mobile SDK—not Verify or ASG application code—executes Regula's stateful transaction protocol through the protected VIDA ingress. The ingress forwards only the required transaction paths to the private Regula Web Service.
The wrapper calls finalizePackage(). Regula returns an opaque transactionId. Wrapper outcome PASS at this stage means capture/package finalization succeeded; PA, AA, CA and KYC remain pending.
VIDA SDK reports transactionId to Verify. Verify validates the reported data, stores the binding, and requests authoritative verification from ASG using a provider-neutral internal contract.
ASG rejects an unbound, expired, consumed, superseded, cross-tenant or cross-environment transaction. For a valid binding, the Regula adapter invokes POST /api/v2/transaction/{id}/process and polls GET /api/v2/transaction/{id}/results?withImages=false.
Regula Web Service reprocesses the transaction against the promoted CSCA/PKD material and returns PA, AA, CA, PACE/BAC, overall, completion and engine metadata. Raw vendor results remain inside the ASG/provider boundary.
ASG maps field-specific Regula status domains into the VIDA model. It preserves PASS, FAIL, NOT_SUPPORTED, NOT_PERFORMED and INDETERMINATE per check, together with trust-store, policy and engine versions.
Verify applies KYC policy to the normalized ASG result and publishes the final transaction state. VIDA SDK receives the existing journey completion response; the wrapper does not retrieve Regula results directly.
All components emit correlation, attempt, transaction, stage, status and latency metadata only. MRZ, DG bytes, face images, EF.SOD, licenses, authorization headers and raw Regula responses are excluded from standard logs and analytics.
Wrapper SDK interface for Mobile

The following is the current Android handoff contract implemented in nfc-docver. The parent VIDA SDK supplies environment configuration once; neither the Regula license nor server URL is accepted as a per-transaction caller parameter.


SDK Wrapper
MyVerify-to-Regula Webservice contract

These endpoints are private service-to-service APIs called only by My-Verify. REGULA_BASE_URL resolves to the private Regula Web Service.
Reprocess Transaction



Request
POST semantics. Reprocess the finalized transaction already stored by complete server-side verification. processParam is required by the Regula 9.6.939 OpenAPI schema. Result type 33 requests Status and 36 requests Text; extend the allow-list only when a consumer has an approved need. Optional useCache=true may reuse stored processed values, but it is decided this explicitly rather than rely on the default false. Do not include image result types in the default path.


Response
POST response handling.  HTTP 200 means Regula accepted/completed the reprocessing call and returned transaction storage metadata; it is not the verification verdict. My-Verify must still call /results and map the returned Status/Text containers. Treat 400 as an invalid request/reference and 403 as a license failure per the pinned OpenAPI. For timeouts or 5xx responses, first inspect /results before retrying POST, then retry with bounded idempotent controls to avoid duplicate work.
Transaction Result



Request
GET semantics. Retrieve the authoritative ProcessResponse for the specified transaction. withImages=false prevents base64 images or image URLs from being returned in the response, while Status and Text containers remain available. Poll with bounded exponential backoff until ProcessingFinished indicates a terminal state according to the pinned ProcessingStatus enum; do not infer completion or success solely from HTTP 200.


Response
Raw status caution  The sample mirrors values observed in local testing and is not a universal enum legend. Regula uses multiple numeric status domains; for example, the observed AA=1 was accompanied by detailed evidence showing AA performed and passed. The versioned My-Verify mapper must interpret each field with the pinned Regula enum/schema, preserve unknown values as INDETERMINATE, and never apply a global zero/non-zero rule.
Java Sample



SampleMain.java


RegulaApi.java


RegulaTransactionService.java


RegulaModels.java
SDK Side

The VIDA DocVer wrapper is the stable abstraction boundary for consuming SDKs. It receives the license binary and protected DocReader ingress URL supplied by VIDA SDK and feeds both into Regula SDK initialization. It applies the RFID/server-side profile, supplies MRZ access input, starts the reader, and calls finalizePackage(). The callback is mapped to a VIDA capture response containing the opaque transactionId. The wrapper does not treat that callback as an authenticity result; Verify and ASG retrieve the authoritative server outcome separately.
Responsibilities

Validate MRZ fields/check digits before NFC and never log the values.
Provide bounded timeouts, cancellation, retryability, antenna guidance, and lifecycle/background handling.
Enable PACE/BAC and required DGs; collect EF.SOD plus DG/security evidence needed for server reprocessing.
Accept the DocReader base URL only from the parent VIDA SDK configuration, validate HTTPS and the environment allow-list, and pass it to Regula through the wrapper. Do not expose a user-editable or document-controlled endpoint.
Accept the mobile license only from the parent VIDA SDK after authenticated download from the VIDA backend. Validate presence, expected metadata/integrity, and applicability before Regula initialization; keep the license bytes out of logs, analytics, crash reports, and public APIs.
Expose VIDA-owned request, result, error, lifecycle, and callback interfaces. Translate vendor-specific objects inside the wrapper so upgrades or a future provider replacement are localized to the adapter and conformance tests.
Keep the VIDA DocReader URL separate from the Regula online-license URL; the former carries document-processing traffic and the latter is an entitlement dependency.
Create one correlation ID per journey and preserve Regula transaction ID/tag for cross-tier tracing.
Treat server result as authoritative in SSV mode; do not merge a local PASS over a server FAIL/INDETERMINATE.
Preserve raw Regula code/name/message in a restricted diagnostic object while publishing stable VIDA buckets.
Compile out debugSaveRFIDSession/debugSaveLogs for production; do not leave them remotely activatable without a governed diagnostic mode.
Selected package and size review

The selected mobile package is api + common + ocrandmrzrfid. It supports OCR/MRZ and RFID capture while the authoritative authenticity decision remains in the VIDA-hosted Web Service. The estimate below is the Android dependency/AAR footprint measured from Regula/Maven artifacts, not the final customer download size.
Component / comparison
Approximate size
Interpretation
Regula api + common and direct base dependencies
~3.72 MiB
Mandatory control/API layer
Regula ocrandmrzrfid core
~82.62 MiB
OCR, MRZ and RFID processing engine/native assets
Selected dependency total
~86.34 MiB
Decision baseline; excludes VIDA wrapper/app code
Regula database downloaded at runtime
~8.3 MiB
Can be omitted from initial bundle, but does not reduce the core AAR
Comparable fullauthrfid total
~97.66 MiB
Selected option saves roughly 11.3 MiB versus fullauthrfid
Size concern for review  Approximately 86.34 MiB is still material for customers with small host applications. Backend verification and runtime database download do not remove the ocrandmrzrfid core from the app. Approval should therefore use measured Play/App Bundle per-device download and installed-size results, not the raw dependency total alone.
Normalized failure buckets

VIDA bucket
Examples
Handling
OCR_CAPTURE_FAILED
Timeout, cancel, invalid image, Regula SDK exception
Usually retryable
OCR_VERIFICATION_FAILED
Data read, quality, blur, glare, template, portrait, authenticity, MRZ detection
Policy/retry depends on subtype
NFC_READ_FAILED
Chip timeout, reader exception, missing RFID payload
Usually retryable
NFC_ACCESS_CONTROL_FAILED
PACE/BAC failed
Retry with validated MRZ/access data
NFC_PASSIVE_AUTH_FAILED
DG hash, SOD signature, DSC/CSCA trust, revocation policy
Non-retryable unless trust/config issue
NFC_ACTIVE_AUTH_FAILED
AA not performed when required or AA signature failed
Policy/capability dependent
NFC_BACKEND_VERIFICATION_FAILED
Package finalization, /process, /results, PA/CA/AA/RFID/overall status
Transport errors retryable; verification failures not
Important numeric-semantic rule  Local tests returned PA=0, CA=0, RFID overall=0 and AA=1 while detailed AA logs showed performed=true, passed=true, and a valid signature. Map each Regula field using its documented enum/domain; never apply one global zero/non-zero rule.
Security of the direct finalizePackage() channel

Control
Required design
Transport
TLS 1.2+ with normal hostname validation; enable Regula-supported certificate pinning with primary and backup pins. Prefer mTLS when mobile certificate issuance, rotation and revocation are operationally supportable.
Endpoint allow-list
Expose only POST /api/v2/transaction/start, required encrypted /process calls, and finalization on /api/v2/transaction/{id}. Keep health, metrics, demo, generic /api/process, result retrieval and administration on private routes.
Replay and abuse
Use short expiry, nonce/jti replay tracking, one active transaction per journey, idempotent finalization/reporting, payload-size and rate limits, concurrency quotas and anomaly alerts. Reject cross-tenant or cross-journey transaction substitution.
Result authority
Never accept the mobile result as the final verdict. ASG retrieves /process and /results over a private service-to-service route and Verify returns only the normalized ASG result to VIDA SDK.
Sensitive data
Do not log authorization headers, license bytes, MRZ, DG content, encrypted package bodies or full transaction responses. Retain only correlation IDs, transactionId, endpoint stage, status, size and latency under approved retention.
Server Side (Web Service)

Regula Web Service runs inside VIDA infrastructure with a database for transaction state and an S3-compatible object store for processing artifacts. Complete SSV requires the transaction start endpoint to be reachable from the client. The Web Service should sit behind VIDA-managed ingress because the service itself is not the product authorization boundary.
Database

The vendor schema is migration-managed and may change with the Regula image. The table inventory below is deliberately limited to what was observed in the referenced local tests or is a standard migration inference; it is not a contractual public API. Production integrations should not write to vendor tables.
Table
Confidence
Inferred function
transaction
Observed
Primary SSV transaction/session metadata: ID, state, lifecycle timestamps, and references needed to coordinate upload, process, finalize, and result retrieval.
reproc_transaction
Observed
Server-side reprocessing record associated with a transaction; tracks the backend processing attempt/state and artifact/result references.
alembic_version
Inferred
Schema migration version used by the SQLAlchemy/Alembic startup process observed in local service logs.
Processing audit table
Vendor-managed name; verify per image
When service.processing.results.audit=true, Regula documents replication of transaction data into a separate database table. Treat its exact name/schema as version-specific and discover it during deployment validation.
Schema control  Run a read-only schema inventory after every pinned Regula image upgrade, diff it against the approved baseline, and keep analytics decoupled through a VIDA-owned view/ETL. Do not couple Holistics directly to vendor migration tables.
Holistics transaction fact

Recommended VIDA-owned table: fact_epassport_verification_transaction. Populate it from the application/service result boundary or a controlled ETL, not by exposing raw vendor tables. One row represents one user verification journey; repeated Regula processing attempts are summarized and can optionally have a child attempt fact.
Field
Type
Purpose
PII note
transaction_id
string/UUID
Primary technical identifier; Regula transaction ID or stable VIDA mapping
No
correlation_id
string/UUID
Cross-tier trace key generated by VIDA
No
tag
string
Regula journey tag; unique and sanitized
No
tenant
string
Client/product grouping
No
environment
enum
dev / staging / prod
No
started_at, completed_at
timestamp UTC
Journey timing
No
duration_ms
integer
End-to-end server verification latency
No
process_attempt_count
integer
Number of backend processing attempts
No
http_start_status, http_process_status, http_results_status
integer
Endpoint response status for debugging
No
processing_finished
boolean/int
Raw Regula completion indicator
No
core_result_code
integer
Raw CoreLibResultCode
No
vida_outcome
enum
PASS / FAIL / INDETERMINATE / SYSTEM_ERROR
No
rfid_overall_status
integer
Raw Regula RFID overall status
No
pa_status, ca_status, aa_status
integer
Raw per-check statuses; interpreted by versioned mapper
No
pace_status, bac_status, ta_status
integer
Access/security mechanism statuses when present
No
normalized_error_bucket
string
Stable VIDA failure category
No
normalized_error_subtype
string
Granular VIDA subtype
No
recoverable
boolean
Whether caller can retry
No
issuer_country_code
ISO-3/nullable
Coarse operational slice; only if approved
Potentially identifying in small cohorts
document_type
enum/nullable
Passport/eMRTD type, not document number
Low
sdk_version, core_version, webservice_version
string
Release correlation and regression analysis
No
trust_store_version, policy_version
string
Reproducibility of trust decision
No
worker_instance, region
string
Operational routing and incident analysis
No
error_fingerprint
string hash
Groups repeatable failures without raw messages
No
artifact_evidence_available
boolean
Whether restricted evidence exists within TTL
No
created_at, updated_at
timestamp UTC
Warehouse lineage
No
Explicitly excluded from Holistics: passport number, DOB, expiry, MRZ, name, DG bytes/hashes, portrait, EF.SOD, certificate bodies, raw request/response JSON, authorization data, object keys containing identity data, and raw exception text that may echo inputs.
Observability

Logs

Stream/event
Useful content
Monitoring/debug use
Access log
HTTP timestamp, method/path, response status, size, client/user-agent fields per configured format
Traffic, 4xx/5xx, endpoint sequence, missing /process or /results, availability
Application log
Startup/config, health, license/scenarios, DB migration/connectivity, S3 persistence, core initialization and processing traces, warnings/errors
Dependency failures, bad config, worker/core failures, license problems, artifact persistence
Processing result log
Input request and optionally result JSON; audit can replicate transaction data to DB
Deep result debugging and vendor escalation; highly sensitive and short-lived
SDK error log
Regula SDK/core error evidence stored at configured processing location
Root-cause analysis for core/RFID processing failures
Regula core/RFID logs
PKD loading, CSCA/DS counts/country list, DG/SOD/AA processing detail, LibProcMgr traces
Trust-material coverage, detailed cryptographic outcome, AA subreason investigation
VIDA normalized event
Correlation/transaction IDs, versions, endpoint stage, normalized outcome/error, latency
End-to-end support without exposing raw identity evidence
Observed event sequence from local tests
transaction_start: POST /api/v2/transaction/start -> transaction ID.
encrypted_process: one or more POST /api/v2/transaction/{id}/process?encrypted=true calls.
transaction_finalize/save: package and metadata persisted; transaction state updated.
result_retrieval: GET /api/v2/transaction/{id}/results?withImages=false.
artifact_persisted: raw-request.bin, request.bson, core-process-response.bin, core-save-response.bin, metadata.json, and cryptographic challenge/key request artifacts when applicable.
verification_summary: CoreLibResultCode, ProcessingFinished, Status.rfid, and detailsRFID PA/CA/AA/PACE/BAC/TA/overallStatus.
trust_material_loaded: unique CSCA/DS counts, countries, and loaded file names at DEBUG level.
failure events: HTTP non-2xx, missing response.json/result, SDK exception code/name/message, DB/S3/PKD/license/core errors.
Monitoring obtainable from logs
Monitoring view
Derivation
Funnel completion
Start -> process -> finalize -> result; identify the stage where journeys drop.
HTTP reliability
2xx/4xx/5xx and timeout rates by endpoint/version/region.
Verification outcomes
PA/CA/AA/RFID/overall distribution after semantic normalization.
Trust coverage
PKD load counts, country coverage, missing CSCA/DS, chain failures.
Persistence health
DB transaction/reprocessing writes, S3 save/read/delete success and latency.
License/core health
License validity/scenario availability and core initialization/processing errors.
Incident debugging
Join SDK, ingress, access, app, DB fact, and restricted artifact evidence by correlation/transaction ID.
Data-governance control
Alert when DEBUG, saveResult, audit, or sdkErrorLog is unexpectedly enabled in production.
Log security and retention
Prefer JSON format in production and inject correlation_id, transaction_id, environment, service_version, and worker_instance at the VIDA boundary.
Do not log MRZ, passport number, DOB, names, DGs, portrait, EF.SOD, certificate body, encrypted package, license, secrets, or authorization headers.
Keep access/application logs according to the platform policy; Regula file logging rotates daily and retains 30 days by default when used, but centralized collection should own final retention.
Processing results and SDK error artifacts require restricted access, encryption, case-based enablement, and a short automatic TTL. They are not normal analytics sources.
Metrics

Regula Web Service can expose Prometheus metrics at /metrics when service.webServer.metrics.enabled=true. The current local config does not enable this block; enable it in the shared environment and restrict the endpoint to the monitoring network.
Regula-exposed metric
Type
Use
license_valid_hours
Gauge
Remaining license validity in hours.
core_request_queue_time_histogram
Histogram
Time a request waits for the Regula Core.
gunicorn_queue_time (+ _count/_sum)
Summary
HTTP worker queue latency and queued request count/time.
gunicorn_request_time_histogram (+ _bucket/_count/_sum)
Histogram
Gunicorn request-time distribution.
flask_http_request_total
Counter
Total Flask HTTP requests; filter by response status/route labels where exposed.
flask_http_request_duration_seconds_*
Histogram
Flask request execution duration buckets/count/sum.
Dashboard area
Recommended derived/platform metrics
Traffic
Request rate by route, environment, tenant; SSV journeys started/completed.
Reliability
2xx/4xx/5xx ratio, timeout/cancel/system-error rate, missing-result rate.
Latency
p50/p90/p95/p99 for start/process/results and end-to-end; Core queue and Gunicorn queue separately.
Capacity
Active workers, queue depth, queue wait, CPU, memory, OOM/restarts, saturation; one document request per worker at a time.
Dependencies
DB connection/pool/errors/latency; S3 operation errors/latency/capacity; ingress/network/TLS errors.
Verification
PASS/FAIL/INDETERMINATE, PA/CA/AA outcome, PACE/BAC use, normalized error bucket.
Trust material
PKD/CSCA/DS count, trust-store age/version, load/update failures, expiring trust anchors.
License
license_valid_hours and licensed scenario/capacity availability.
Data governance
Artifact count/bytes/age, deletion lag, diagnostic capture state.
Recommended alerts
license_valid_hours below 30 days (warning) and below 7 days (critical), adjusted to VIDA license process.
p95 end-to-end latency or Core/Gunicorn queue time breaches SLO for two burn-rate windows.
5xx/system-error/missing-result rate exceeds baseline; alert separately by /start, /process, and /results.
PA or trusted-chain failures spike by issuer country, trust-store version, or Web Service version.
DB/S3 dependency errors, worker restarts/OOM, trust material not loaded, or active worker capacity exhausted.
Processing-result/SDK-error persistence remains enabled outside an approved diagnostic window, or retention deletion is overdue.
Online license

The solution uses a Regula Online License (OL). On mobile, VIDA SDK obtains the encrypted regula.license file from an authenticated VIDA backend endpoint and supplies the file in memory or through a VIDA-controlled protected cache when offline startup is required, to the DocVer wrapper layer. The wrapper feeds the license file into Regula SDK during Regula SDK initialization; the integrating application never receives a license URL, file path, or license contents. On the server, the license remains a runtime secret mounted from the approved secret store. In both cases, the file alone is not sufficient: entitlement validation requires Internet connectivity to Regula's licensing service over HTTPS port 443.
Purpose
FQDN / URL
Protocol
Operational note
Main licensing service
lic.regulaforensics.com
HTTPS 443
3.33.212.24 and 15.197.254.180 in current Regula documentation
Backup licensing service
lic2.regulaforensics.com
HTTPS 443
34.96.77.73 in current Regula documentation
Main health check
https://lic.regulaforensics.com/healthcheck
HTTPS GET
Use from the backend network/diagnostic runbook
Backup health check
https://lic2.regulaforensics.com/healthcheck
HTTPS GET
Use when the main service is unavailable
Backend network requirement  
Open controlled outbound HTTPS 443 from every DocReader worker/pod to both licensing FQDNs. Prefer FQDN-based egress policy because published IP addresses can change; do not expose DocReader inbound to Regula. If the mobile SDK also uses OL, confirm equivalent device-side Internet behavior separately.
Runtime mechanism and failure behavior

Area
Required understanding
Transaction-based OL
The Web Service contacts the licensing service for every /api/process use. Loss of Internet/licensing connectivity stops processing immediately.
Instance/worker-based OL
Each worker registers at startup and sends an hourly heartbeat. Internet loss permits up to 72 hours of continued operation, then results stop until validation succeeds again.
Scaling implication
For instance-based OL, each active worker counts as a licensed instance. Autoscaling and active/passive topology must stay within entitlement.
Automatic renewal
Eligible OL licenses renew online without application recompilation, provided licensing connectivity and entitlement remain valid.
Data boundary
Regula states that the licensing service manages license keys only; customer-hosted personal document data is not sent to it.
Confirm with Regula whether VIDA's license is transaction-based or instance/worker-based and record the transaction/worker limit.
For mobile, expose an authenticated VIDA backend license-download endpoint with authorization, TLS, integrity/version metadata, failure-safe caching, revocation/rotation behavior, and audit events. VIDA SDK supplies the downloaded license file to the wrapper, which feeds it into Regula SDK during SDK initialization.
For server, mount regula.license from the approved secret store. For both platforms, never commit, log, copy to analytics, expose through public APIs, or include the file in support bundles.
Probe DocReader /api/healthz for license status and alert on license_valid_hours, licensing timeouts, rejected entitlement, and scenario unavailability.
Test main-to-backup licensing failover, DNS/proxy/TLS behavior, startup registration, and the applicable Internet-loss grace period in dev before production approval.
Ensure proxy/egress configuration does not perform unsupported TLS interception and that the health-check URLs are included in the operational runbook.
Deployment

Layer
Production design
Mobile
VIDA DocVer + supplied Regula API/Core; release build disables debug/session capture; App Bundle ABI delivery.
Ingress
Private or tightly controlled public endpoint, TLS 1.2+, client authorization, rate/payload limits, WAF/abuse controls, correlation headers.
Regula workers
Pinned image digest; rolling/canary rollout; start at >=1 vCPU and 3.5 GB RAM per worker; scale on queue and latency, not CPU alone.
Database
Managed MySQL 8+; encrypted, backed up, private subnet, least privilege; vendor migrations tested before rollout.
Object storage
Private S3 bucket, encryption/KMS, lifecycle TTL, object access audit, no public access; separate transaction/processing prefixes.
Trust material
Versioned ICAO/BSI/approved CSCA inputs; signed-source validation, promotion/rollback, read-only mount or controlled sync.
Secrets/license
Secrets manager/workload identity; never commit DB/S3 credentials; license expiry and scenario availability monitored.
Observability
Prometheus scrapes restricted /metrics; centralized structured logs; Grafana dashboards/alerts; Holistics reads VIDA-owned fact model.
Resilience
At least two workers across failure domains for production; health/readiness checks, bounded timeouts, idempotent journey handling, DB/S3 backup/restore drills.
Environment profile
Environment
Profile
Development
Demo UI allowed; DEBUG and artifacts enabled for test cases; dev MySQL/S3; synthetic/test passports only.
Staging
Production-like auth/network/metrics; pinned images; controlled artifact capture; golden corpus and load tests.
Production
Demo/CORS wildcard disabled; INFO/JSON logs; raw artifacts off by default; managed DB/S3; short TTL; multi-worker; strict secrets and audit.
References

Regula, Complete Server-Side Verification: Server-Side Verification setup for Document Reader Web Service - Regula - Developer Documentation
Regula document reader API
Regula Document Reader Web API
Regula, Web Service logging: Configuration of logging for Document Reader Web Service - Regula - Developer Documentation
Regula, Monitoring and Prometheus metrics: Document Reader SDK Web API Monitoring and Metrics Collection - Regula - Developer Documentation
Regula, Web Service storage: Data Storage setup for Document Reader Web Service - Regula - Developer Documentation
Regula, Web Service getting started/worker model: Getting started with Document Reader SDK web service - Developer Documentation
Regula, SDK transactions/tag/tenant/environment: Transactions - Developer Documentation

## 10. Web SDK, configuration, and developer enablement

Known: Web SDK wrapper with HMAC/encrypted config/RBAC/CORS; OSS + Redis feature control; full Malaysia documentation overhaul; ChatGPT-assisted PR review.

- Did Web SDK support unlock a particular client/channel or remove an integration blocker? What adoption/result did it have? - unlocked a newer product line that is web sdk, i am not sure if these questions are relevant arent these self answerable?
- Did OSS/Redis configuration reduce deployment risk, rollout time, or the need for code changes? Give one concrete example if possible. - oss is a platform that hold product configs no ksome features it is for pahse roll out, some provide new featuers to specific clients, some provide the clients with options to change coversions and risks.
- Did documentation or PR-review automation have a measurable effect (fewer support questions, faster onboarding/reviews, better release quality), or should we frame it as developer enablement only?

## 11. Research and GXS manual review

Known: Philippines/Indonesia verification POCs; GXS manual-review research/design, not implementation.

- Did either Philippines or Indonesia POC result in a product decision, pilot, client conversation, or launch path? If not, we may exclude it from the final one-page resume. - all these enabled improved, productm far frr, newer markets, clients, etx
- For GXS, what concrete artifact did you own—architecture/decision document, API contract, stakeholder workshops, or handoff—and did it unblock a launch/design decision? - i was given an unclear prd resolving that creating all the peices how it will all look what all considerations etc, it is all covered in the docs in the my-verify please check, except coding all parts covered.

## Final selection preferences

- Which three VIDA projects would you most enjoy explaining in a 45-minute backend/system-design interview? - i can explain all and i think i have worked on a lot of interesting projects as you can see from my reviews.
- Are there company/client names, revenue figures, security details, or internal technology names that should not appear publicly? - client name shouldn't. revenue and all like we have give in percentage can occur, security and internal technology no specific restriction we can i believe just dont use service names.
- Do you want the final resume to lean more toward platform/backend scale, fintech/compliance, external integrations, or global-product engineering? - i didn't understnad this.
