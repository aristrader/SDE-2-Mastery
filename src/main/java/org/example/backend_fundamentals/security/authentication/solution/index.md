---
order: 20
search: false
---

# Solutions: Authentication Mechanisms & OAuth 2.0

## Solution: auth-mechanism-tradeoffs - Selecting Between Sessions, JWTs, and API Keys

### 1. Scenario Recommendations & Justifications

| Scenario | Recommended Mechanism | Primary Justification |
| :--- | :--- | :--- |
| **1. Admin SPA (Same Domain)** | **Stateful Session Cookie (`HttpOnly`, `Secure`, `SameSite=Strict`)** | Provides instant server-side revocation upon admin deprovisioning via Redis/DB store; immune to JS token exfiltration (XSS). Requires anti-CSRF defenses. |
| **2. Partner Ingestion (Batch M2M)** | **OAuth 2.0 Client Credentials (Default / Preferred) or Static API Key (Constrained/Legacy)** | Client credentials flow provides automated token lifecycle, short-lived scoped access tokens, and centralized revocation without shared long-lived secrets. Static API keys are bearer client secrets suitable only when constrained by legacy systems or simple integration requirements. |
| **3. High-Throughput Mobile App** | **Short-Lived JWT + Stateful Refresh Token** | Downstream microservices validate signed JWTs locally with the configured verification key (often a public key for asymmetric algorithms), avoiding centralized DB lookups; refresh tokens in native-app OS secure storage (Keychain / KeyStore) enable session management and revocation. |

### 2. Security Risks & Mitigations

1. **Admin Session Cookies:**
   - *Risk:* Cross-Site Request Forgery (CSRF).
   - *Mitigation:* `SameSite=Strict` cookie attribute and anti-CSRF synchronizer tokens (or custom request headers) for all mutating HTTP methods.
2. **Partner Ingestion (Client Credentials vs API Keys):**
   - *Risk:* Client secret or static API key leakage grants direct bearer access.
   - *Mitigation:* For Client Credentials, use short-lived access tokens, mTLS / private key JWT client authentication, and scoped permissions. For static API keys, enforce IP allowlisting, key rotation schedules, and strict gateway rate limits.
3. **Mobile JWTs:**
   - *Risk:* Inability to revoke compromised access tokens immediately before expiration.
   - *Mitigation:* Short lifespans (5–15 minutes), refresh tokens stored in OS secure storage (iOS Keychain / Android KeyStore), server-tracked refresh token rotation, and revocation lists.

---

## Solution: oauth-pkce-defense - Securing Public Clients with Authorization Code + PKCE

### 1. The Vulnerability of Public Client Secrets
SPAs and mobile applications are **public clients**; their source code, assets, and runtime memory are entirely accessible to end users and attackers. A `client_secret` hardcoded in frontend JavaScript or packaged in a mobile binary can be extracted trivially through browser DevTools or APK decompilation, rendering client authentication ineffective.

### 2. How PKCE Mitigates Code Interception
PKCE (RFC 7636, RFC 9700) binds the authorization code to a per-request dynamically generated secret:
1. **Code Verifier:** A cryptographically random high-entropy string generated on the client for each authorization attempt.
2. **Code Challenge:** A SHA-256 hash of the verifier (`BASE64URL(SHA256(code_verifier))`), sent with `code_challenge_method=S256` during the initial authorization request.
3. **Verification:** When exchanging the authorization code, the client transmits the plaintext `code_verifier`. The Authorization Server computes the SHA-256 hash and validates it against the stored `code_challenge`. Even if an attacker intercepts the authorization code in the redirect URI, they cannot exchange it without the original `code_verifier`.
4. **Scope of Defense:** PKCE protects the code exchange step for public clients. It does not replace exact redirect URI validation, CSRF state tokens (`state`), OIDC `nonce` replay validation, or TLS transport encryption.

### 3. Step-by-Step PKCE Handshake Sequence

1. **Client Setup:** Client generates a high-entropy `code_verifier` and computes `code_challenge = BASE64URL(SHA256(code_verifier))`.
2. **Authorization Request:** Client directs the user agent to the Authorization Server:
   `GET /authorize?response_type=code&client_id=spa_app&redirect_uri=https://app.com/callback&scope=openid profile&code_challenge=xyz&code_challenge_method=S256&state=xyzState`
3. **User Authentication & Consent:** User logs in and grants requested scopes; the Authorization Server redirects to the callback with an `authorization_code` and `state`.
4. **Token Exchange:** Client POSTs directly to the token endpoint:
   `POST /token` with `grant_type=authorization_code`, `code=AUTH_CODE`, `client_id=spa_app`, and `code_verifier=ORIGINAL_VERIFIER`.
5. **Challenge Validation & Token Delivery:** The Authorization Server verifies `SHA256(code_verifier) == code_challenge`. Upon match, it returns an `access_token` (and optional `id_token` / `refresh_token`).
6. **Resource Access:** Client accesses protected APIs passing `Authorization: Bearer <access_token>`.
