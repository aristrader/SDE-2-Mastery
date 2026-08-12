# Part 12 — API Design

> **Sprint allocation:** Week 6 (shared). **Budget: ~3-4 hrs.**

## 12 API Design — topic inventory

| # | Topic | Tags | Tier | Time | Done | Partial | Grilling | Visit Again | Notes | Resources |
|---|-------|------|------|------|------|---|---|-------------|-------|-----------|
| 1 | RESTful resource modeling | 🔴 💼 🎯 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | ~1 hr (ChatGPT) — includes HTTP verbs, status codes, over-fetching | 📖 `api_design/api_technologies_summary/index.md` |
| 2 | REST vs GraphQL vs gRPC comparison & architecture | 🔴 💼 🎯 | MP | 1 hr | [x] | [ ] | [ ] | [ ] | ~1 hr (ChatGPT) — includes streaming types, trade-offs | 📖 `api_design/api_technologies_comparison/index.md` |
| 3 | Versioning — URI, header, content negotiation | 🔴 💼 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 4 | Pagination — offset, cursor, keyset | 🔴 💼 🎯 | MP | 1.5 hrs | [x] | [ ] | [ ] | [ ] | Covered offset vs cursor vs keyset, concurrent-write bugs, stable sort keys, opaque cursor design, snapshot pagination, and interview traps | 📖 `databases/pagination/index.md` · 💻 Warm-up: implement keyset pagination — `WHERE id > :lastSeenId ORDER BY id LIMIT 20` + response includes `nextCursor` (30 min) |
| 5 | Filtering, sorting, projections | 🔴 💼 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 6 | Idempotency — Idempotency-Key header, design | 🔴 💼 🎯 | D | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  | 💻 Warm-up: middleware that reads `Idempotency-Key` header + stores `(key, response)` in Redis with 24h TTL + serves cached response on retry (20 min) |
| 7 | Error response design (RFC 7807 Problem Details) | 🔴 💼 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  | 💻 Warm-up: @RestControllerAdvice mapping a DomainException to ProblemDetail with type/title/status/detail/instance fields (15 min) |
| 8 | Async / long-running APIs — `202 Accepted`, status endpoint, and callbacks | 🔴 💼 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 9 | Authentication header design | 🟠 💼 🎯 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 10 | OpenAPI spec & code-gen | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 11 | Bulk operations | 🟠 💼 | MP | 1.5 hrs | [ ] | [ ] | [ ] | [ ] |  |  |
| 12 | HATEOAS — what it is, why most APIs skip it | 🟠 💼 | M | 45 min | [ ] | [ ] | [ ] | [ ] |  |  |
| 13 | API deprecation strategy | 🟡 | M | 1 hr | [ ] | [ ] | [ ] | [ ] |  |  |

## Time summary

| Scope | Hours (zero baseline) | Weeks @ 10–12 hrs/wk | Actual time |
|-------|-----------------------|----------------------|-------------|
| 🔴 MUST only (Sprint priority — 3-month plan) | ~11.5 hrs | ~1.05 wk | |
| 🔴 + 🟠 HIGH (must + high — senior coverage) | ~16.75 hrs | ~1.52 wk | |
| Full Part (all items including 🟡) | ~17.75 hrs | ~1.61 wk | ~2.0 hrs so far |

## Frequently asked

1. **Q:** Cursor vs offset pagination — when does each fit?
   - **Why asked:** Senior REST canonical. Offset: `?page=5&size=20` — simple, supports random page jumps. Breaks under writes (page N may have repeats / skips if rows added/removed). Cursor / keyset: `?after=<opaque>` — stable across writes, only forward/backward, no random jumps. Use keyset for infinite-scroll feeds, offset for paginated tables where users jump pages.
2. **Q:** Walk through your idempotency design for POST /verifications.
   - **Why asked:** KYC-canonical. Client generates `Idempotency-Key` UUID. Server stores `(key, request_hash, response, expires_at)` in Redis with 24h TTL. On retry: same key + same hash → return cached response. Same key + different hash → 412 Precondition Failed. Different key → process new request.
3. **Q:** RFC 7807 Problem Details — what fields, why use it?
   - **Why asked:** Modern error response standard. Fields: `type` (URI identifying error type), `title` (human-readable summary), `status` (HTTP status), `detail` (instance-specific message), `instance` (URI of the specific error occurrence). Use it: machine-readable + human-readable + standardized.
4. **Q:** REST versioning — pick a strategy, defend it.
   - **Why asked:** Architecture decision. URI versioning (`/v1/users`): visible, easy to route, easy to deprecate. Header versioning (`Accept: application/vnd.app.v1+json`): "pure" REST, lets URI stay stable, harder to debug. Most APIs pick URI versioning for simplicity. Defend per your stack: KYC SDK probably prefers URI versioning (easier for partner banks).
5. **Q:** Design an async long-running API for KYC verification.
   - **Why asked:** Your platform pattern. Client POST /verifications → 202 Accepted + Location: /verifications/{id} + status URL. Polling: GET /verifications/{id}/status. Statuses: PENDING / IN_PROGRESS / VERIFIED / REVIEW / ERROR / FAILED. Webhook on completion as alternative to polling.
6. **Q:** Bulk operations — POST /users with array vs N parallel POST /users — what are the trade-offs?
   - **Why asked:** Practical design. Bulk: fewer round-trips, atomic if all-or-nothing, harder to retry partial failures, request size limits. N parallel: clean per-request error handling, retryable per item, but N times the HTTP overhead. Common pattern: bulk endpoint that returns per-item status (some succeeded, some failed).
7. **Q:** Filtering + projections — design `GET /transactions?status=COMPLETED&fields=id,amount`.
   - **Why asked:** Practical query design. Filtering: `?status=...` for equality, `?amount[gt]=100` for ranges, or full GraphQL-style. Projections: `?fields=id,amount` selects only those fields. Sparse fieldsets reduce bandwidth. Document precedence: filter → sort → paginate → project.

## Trick questions / gotchas

1. **Q:** Your idempotency cache is in Redis with TTL=24h. What happens if Redis fails between two retries?
   - **Gotcha:** Without idempotency cache, retry processes again → duplicate effect. Mitigations: (1) write idempotency key to DB transactionally with the operation, (2) check DB for prior key on retry, (3) fallback to "best effort dedup" with shorter window using DB.
2. **Q:** You return 500 with no body. Why is this hostile to clients?
   - **Gotcha:** Clients can't distinguish transient (retry-safe) from permanent (don't retry) failures, and can't show useful error messages. Always return RFC 7807 Problem Details with type, title, status, detail. Include correlationId for support requests.
3. **Q:** Why is offset pagination broken for "concurrently-modified" data sets?
   - **Gotcha:** If rows are added/removed between page fetches, you get duplicates or missing rows. E.g., page=1&size=10 returns rows 1-10; new row inserted at start; page=2&size=10 now starts from the OLD row 11, which is now row 12 — you missed the new row 10. Keyset pagination uses stable cursor (last-seen ID), immune to insertions.
4. **Q:** Why is HATEOAS rarely implemented in practice?
   - **Gotcha:** Promise: client discovers state transitions via links → loose coupling. Reality: clients still hardcode endpoint shapes; the discovery layer doesn't save real work; mobile SDKs especially can't dynamically follow links efficiently. Most APIs document their endpoints out-of-band (OpenAPI) and skip HATEOAS entirely.

## Mastery candidates (top 3–5 from this Part — suggestions, not commitments)

- **Idempotency-Key end-to-end design** (~2.5 hrs) — directly your KYC platform. Storage choices (Redis vs DB), TTL, request-hash matching, conflict handling.
- **Async long-running API for KYC** (~2 hrs) — your platform's exact pattern. 202 + status URL + webhook. Walk it through end-to-end.
- **Pagination cursor design** (~2 hrs) — cursor-based pagination for an unbounded list (e.g., transaction history). Encoding the cursor opaquely (Base64 of `(timestamp, id)`), security considerations.
- **API versioning + deprecation strategy** (~2 hrs) — pick strategy, document migration path for partner banks. Sunset header standards.

## Hands-on exercises (Practice + Advanced)

Warm-up API design exercises are listed inline in the topic-table Resources column (counted in main Time summary). Longer exercises below are tracked separately.

### Practice — mid-level (~30-60 min each)

1. **Idempotency-Key middleware** (~60 min) — Spring filter that intercepts POST with Idempotency-Key header. Stores `(key, request_hash, response)` in Redis with 24h TTL. On retry: matched key + hash → replay cached response. Mismatched hash → 412. Add unit tests covering all paths.
2. **RFC 7807 Problem Details across error types** (~45 min) — define a domain exception hierarchy (ValidationError, NotFoundError, ConflictError). Map each to ProblemDetail with `type` URIs pointing to your documentation. Add `correlationId` for support traceability.
3. **Cursor pagination implementation** (~45 min) — endpoint `GET /events?cursor=<base64>&limit=20`. Cursor encodes `(timestamp, id)`. Validate cursor monotonicity. Return `nextCursor` in response. Test with concurrent inserts to verify stability.

### Advanced — senior-grade depth (~60+ min each)

4. **Design + implement an async long-running API** (~90 min) — POST /verifications returns 202 + Location. GET /verifications/{id}/status returns current state. Webhook delivery on completion. Idempotent retries via Idempotency-Key. Full happy + error paths.
5. **API versioning migration walkthrough** (~60 min) — document migration from v1 → v2 for a partner SDK. Include: backward-compatible additive changes, breaking changes via new versions, deprecation headers, sunset timeline, communication plan to partners.

### Hands-on time summary (Practice + Advanced only)

| Tier | Total time | Weeks @ 10–12 hrs/wk | Actual time |
|------|------------|----------------------|-------------|
| Practice (mid-level) | ~2.5 hrs | ~0.23 wk | |
| Advanced (senior-grade) | ~2.5 hrs | ~0.23 wk | |
| **Combined hands-on (Practice + Advanced)** | **~5 hrs** | **~0.45 wk** | |

> Warm-up exercises (counted in main Time summary above) total ~65 min for Part 12 across 3 in-table warm-ups.

## Quick recall

**Q. Offset vs cursor pagination — when each fits?**
A. Offset: supports random page jumps, breaks under concurrent writes. Cursor: stable under writes, only forward-only navigation. Use cursor for feeds, offset for static result sets.

**Q. Idempotency-Key three rules.**
A. (1) Client generates, server caches. (2) Match cached response on same key + same request hash. (3) Different hash with same key = 412 (client error).

**Q. RFC 7807 fields?**
A. type (URI), title (human summary), status (HTTP code), detail (specific message), instance (URI of error occurrence). Plus extensions for traceability.

**Q. Async / long-running API pattern?**
A. POST returns 202 + Location: /resource/{id}. GET /resource/{id}/status returns current state. Optional webhook for completion notification.

**Q. Which HTTP status codes for what?**
A. 200 OK, 201 Created (with Location), 202 Accepted (async), 204 No Content, 400 Bad Request (client validation), 401 Unauthorized (no auth), 403 Forbidden (auth but no perm), 404 Not Found, 409 Conflict (e.g., duplicate), 412 Precondition Failed (idempotency-key mismatch), 422 Unprocessable Entity (semantic validation), 429 Too Many Requests (rate limit), 500 Server Error, 503 Service Unavailable.

**Q. Why is HATEOAS usually skipped?**
A. Real clients hardcode endpoint shapes anyway. The discovery layer adds complexity without removing the need for out-of-band API documentation (OpenAPI).
