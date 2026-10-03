---
order: 80
---

# JWT Validation, Refresh Tokens, MFA & API Security

This document covers production-grade token validation, stateful refresh token lifecycles with reuse detection, modern MFA architectures (TOTP vs WebAuthn/Passkeys), and HMAC request signing with replay defenses for webhook and SDK-to-server integration.

---

## 1. JWT Claim Validation (Beyond Cryptographic Signatures)

### The Misconception
Developers often assume that if a JWT passes cryptographic signature verification (e.g., RS256/ES256 public key check), the token can be immediately trusted and the payload claims accepted.

### The Reality & Failure Mode
Signature verification only proves two things: the payload was not tampered with in transit, and it was signed by the private key holder. It does **not** guarantee that the token was intended for your specific service, that the issuer is trusted, or that the token is currently active.
* **Attack Scenario (Token Confused Deputy / Cross-Service Replay):** An attacker obtains a valid JWT issued by Auth0 for low-privilege `Service-A`. If high-privilege `Service-B` only verifies the signature against Auth0's JWKS and skips `aud` (audience) validation, the attacker uses the `Service-A` token to execute actions on `Service-B`.

### Mechanism: Mandatory Claim Validations
Beyond signature verification, backends must strictly validate standard RFC 7519 claims:

1. **`iss` (Issuer):** Must exactly match your expected authorization server URL (e.g., `https://auth.company.com/`). Rejects tokens from rogue or untrusted tenants sharing the same identity provider infrastructure.
2. **`aud` (Audience):** Must match your specific service identifier or API resource server URI (e.g., `api://payment-service`). Prevents tokens minted for one client/resource from being replayed against another.
3. **`exp` (Expiration Time):** Current epoch timestamp must be `< exp`. Reject expired tokens immediately. Backends allow an optional small clock skew (e.g., 30–60 seconds) to account for NTP drift across distributed nodes.
4. **`nbf` (Not Before):** Current epoch timestamp must be `>= nbf`. Ensures tokens scheduled for future activation cannot be used prematurely.
5. **`iat` (Issued At):** Identifies when the token was created. Used for auditing, token age policies, and rejecting tokens issued prior to an account password reset event (`iat < last_password_reset_timestamp`).

### Trade-offs & Recovery
* **Clock Skew:** Enforce tight clock drift tolerances (30s maximum) using host-level NTP synchronization. Excessively large clock-skew windows widen the replay vulnerability window for expired tokens.
* **Revocation Gap:** Stateless JWT validation cannot detect mid-flight permission revocation or immediate user suspension without external revocation lists (CRL/Redis blacklists) or keeping access token lifetimes very short (5–15 minutes).

### SDE2 Interview Answer
> "Signature validation proves integrity and authorship, but claim validation enforces authorization context and temporal validity. In production, we enforce a strict pipeline: verify signature using rotated JWKS keys, reject `exp` violations allowing <=30s clock skew, verify `iss` matches our trusted IdP, and verify `aud` contains our resource URI so tokens minted for other internal services cannot be replayed here."

---

## 2. Refresh Token Lifecycle, Rotation & Reuse Detection

### Token Lifetimes & Threat Model
* **Access Token:** Short-lived (5 to 15 minutes). Held in memory or short-lived memory storage. Limits exposure if intercepted.
* **Refresh Token:** Long-lived (days to weeks). Stored securely (e.g., `HttpOnly`, `Secure`, `SameSite=Strict` cookies or OS-level credential vaults). Used exclusively at the authorization server's `/token` endpoint (RFC 6749) to exchange for a new Access/Refresh pair.

### Refresh Token Rotation (RTR) Mechanism
* **Old Static Approach:** A single refresh token lived until expiration. If compromised via cross-site scripting (XSS) or database leakage, an attacker held persistent account access.
* **Modern Rotation Approach (RFC 6749 Section 6 / RFC 6819):** Every time a refresh token (`RT_n`) is presented to mint a new access token, `RT_n` is invalidated immediately, and the auth server issues a brand-new refresh token (`RT_n+1`) along with the new access token.

```
Legitimate Flow:
Client [RT_1] --------> Auth Server (Consumes RT_1, issues [AT_2, RT_2]) --------> Client stores RT_2

Attack & Reuse Detection:
Attacker [Stolen RT_1] -> Auth Server detects RT_1 is ALREADY CONSUMED!
                          Action: Revoke token family (RT_1, RT_2, ...), invalidate all active sessions.
```

### Reuse Detection & Token Families
1. **Token Family Concept:** Each login initiates a "Family ID" or lineage graph in the persistence store (e.g., Redis or SQL):
   ```
   Family-101: RT_1 (Consumed) -> RT_2 (Consumed) -> RT_3 (Active)
   ```
2. **Normal Path:** Client sends `RT_3`. Server marks `RT_3` as `Consumed`, generates `RT_4`, and returns `[AT_4, RT_4]`.
3. **Attack Detection:** If an attacker intercepts `RT_1` and attempts to redeem it *after* the legitimate client has already exchanged `RT_1` for `RT_2`:
   - The auth server observes a request presenting a token with status `Consumed`.
   - This indicates a token theft race condition or exfiltration event.
   - **Remediation:** The auth server immediately revokes the **entire Family-101 session tree**, revokes all associated access tokens, and forces a full re-authentication with MFA.

### Trade-offs & Recovery
* **Network Concurrency / Race Conditions:** On high-latency mobile networks, overlapping requests or retries might send `RT_n` twice before receiving `RT_n+1`. Production auth systems implement a brief "grace period" (e.g., 5–10 seconds) during which multiple presentations of `RT_n` return the existing `RT_n+1` rather than triggering nuclear family revocation.
* **Storage Requirement:** Refresh token state must be persisted (Redis cluster or distributed database) to track family IDs and consumption state, breaking pure statelessness in exchange for revocability.

### SDE2 Interview Answer
> "We implement Refresh Token Rotation with Token Family tracking. Every refresh exchanges the current token for a new pair and marks the old token consumed. If a previously consumed token is presented again, the auth engine treats it as token compromise, immediately invalidating all tokens under that family ID and terminating the user's active session across devices."

---

## 3. Multi-Factor Authentication (MFA) & TOTP Deep Dive

MFA mandates that authentication requires two or more distinct, independent authentication categories:
1. **Something You Know (Knowledge):** Passwords, PINs, security questions.
2. **Something You Have (Possession):** Authenticator apps (TOTP), hardware tokens (YubiKey), mobile devices.
3. **Something You Are (Inherence):** Biometrics (fingerprint, facial geometry, retina scans).

### TOTP: Time-Based One-Time Password (RFC 6238)
* **Under the Hood Mechanism:**
  TOTP is an extension of HMAC-Based One-Time Passwords (HOTP, RFC 4226). It replaces the incremental event counter $C$ with a time-based counter:
  $$T = \lfloor \frac{\text{Current Unix Time} - T_0}{X} \rfloor$$
  where $X$ is the time step (default: 30 seconds), and $T_0$ is the epoch start (0).

  The one-time code is calculated as:
  $$\text{TOTP} = \text{Truncate}(\text{HMAC}(K, T)) \pmod{10^d}$$
  where $K$ is the shared base32 secret provisioned via QR code during MFA enrollment, and $d$ is the configured digit count. RFC 6238's reference profile uses SHA-1, while implementations can support SHA-256 or SHA-512 when both sides agree.

* **Validation & Clock Drift:**
  The server computes TOTP for $T$, $T-1$, and $T+1$ (a $\pm 1$ step window representing $\pm 30\text{s}$) to tolerate reasonable client clock drift. Once a TOTP code is accepted for step $T$, that specific code is marked consumed in a short-lived cache to prevent replay within the same 30-second window.

### Why SMS is Weak (Vulnerabilities & Limitations)
SMS-based verification is considered insecure by NIST SP 800-63B guidelines due to fundamental telecom vulnerabilities:
* **SIM Swapping:** Attackers use social engineering or compromised carrier employees to port the victim's phone number to an attacker-controlled SIM card.
* **SS7 Protocol Interception:** Flaws in global telecom signaling (SS7) allow sophisticated adversaries to reroute SMS messages without physical SIM possession.
* **Phishing Proxies (Reverse Proxies):** Adversary-in-the-middle (AitM) reverse proxy kits (e.g., Evilginx) intercept both the password and the SMS OTP in real time, rendering one-time codes ineffective against phishing.

---

## 4. Passwordless Authentication & WebAuthn / Passkeys

Passwordless authentication removes reusable credentials from the authentication lifecycle, mitigating brute-force, database credential dumps, and credential stuffing.

### Magic Links
* **Mechanism:** The user enters an email; the server mints a high-entropy, short-lived (5–15 min), single-use cryptographic token stored in Redis and sends a URL. Clicking the link validates and burns the token, establishing a session.
* **Limitation:** Relies entirely on the security of the email provider, vulnerable to email account takeover, and subject to email delivery latency or corporate spam filter URL-crawlers burning the single-use token prematurely.

### Passkeys & WebAuthn / FIDO2
Passkeys represent the gold standard in modern authentication, replacing passwords with public-key asymmetric cryptography.

```
+----------------+          Challenge           +---------------------+
|                | <--------------------------- |                     |
|  Client Device |                              |  Relying Party (RP) |
|   (Browser /   |   Biometric Unlocks Key      |      (Backend)      |
|  Hardware Key) | ---------------------------> |                     |
|                |   Signed Challenge + Origin  |                     |
+----------------+ ---------------------------> +---------------------+
```

* **The WebAuthn Trust Boundary:**
  1. **Registration:**
     - The user prompts device biometrics (TouchID, FaceID, Windows Hello).
     - The device's Secure Enclave / TPM generates a unique asymmetric key pair ($SK_{device}, PK_{device}$) strictly scoped to the Relying Party's domain origin (e.g., `id.example.com`).
     - $PK_{device}$ is sent to the backend and stored with the user record. $SK_{device}$ never leaves the hardware secure element.
  2. **Authentication (Challenge-Response):**
     - Relying Party (backend) issues a cryptographically secure random challenge string.
     - The client platform prompts for local biometric verification to unlock $SK_{device}$.
     - The device signs the challenge and client data (including the verified browser `origin`) with $SK_{device}$.
     - The backend verifies the signature using stored $PK_{device}$ and confirms that the signed `origin` matches its domain.

* **Phishing Resistance:**
  Because the browser automatically injects the verified top-level origin into the signed assertion, an attacker operating a replica site (e.g., `id.examp1e.com`) cannot trick the authenticator into signing a challenge for `id.example.com`.

---

## 5. API Security: HMAC Request Signing & Replay Defense

### The Problem
OAuth and bearer tokens authenticate the user's identity, but server-to-server APIs (e.g., AWS APIs, Stripe webhooks, Razorpay payment notifications, KYC verification services) require proving that:
1. The request payload originated from an authentic sender holding a shared secret.
2. The HTTP method, URI, headers, and request body were not tampered with in transit.
3. An eavesdropper cannot capture a legitimate request and replay it later.

### HMAC Signing Mechanism
Rather than transmitting credentials over the wire, the client and server share a pre-shared key (PSK / `secret`). The sender builds a canonical request string and generates a cryptographic hash using HMAC (e.g., HMAC-SHA256):

```
CanonicalPayload = HTTP_Method + "\n" +
                   Request_Path + "\n" +
                   Epoch_Timestamp + "\n" +
                   Nonce + "\n" +
                   SHA256_Hash(Request_Body)

Signature = HexEncode(HMAC-SHA256(PreSharedKey, CanonicalPayload))
```

The sender sends the `Timestamp`, `Nonce`, and `Signature` via HTTP headers:
```http
X-Signature: a3f87b89...
X-Timestamp: 1727954000
X-Nonce: e3b0c442-98fc-1c14-9afe-46364f2dd4cb
```

### Replay Attack Prevention Architecture
An attacker intercepting `POST /api/v1/payments/transfer` with `amount=10000` could resend the exact same signed payload repeatedly if replay defenses are absent.

```
Incoming Request (Signature, Timestamp, Nonce)
  │
  ├── 1. Timestamp Check: |Current_Time - Request_Timestamp| <= 300s?
  │      └── NO  --> Reject (401 / Stale Request Window Expired)
  │      └── YES --> Proceed
  │
  ├── 2. Nonce Uniqueness Check: Nonce exists in Redis cache?
  │      └── YES --> Reject (409 / Duplicate Replay Detected)
  │      └── NO  --> Store Nonce in Redis with TTL = 300s
  │
  ├── 3. Signature Verification: Recompute HMAC-SHA256(Secret, CanonicalPayload)
  │      └── Computed == Header Signature (via Constant-Time Comparison)?
  │            └── NO  --> Reject (401 / Invalid Signature)
  │            └── YES --> Process Request
```

1. **Step 1: Timestamp Drift Window:** The backend rejects any request where `|now - X-Timestamp| > 300 seconds` (5 minutes). This limits the vulnerability window to 5 minutes.
2. **Step 2: Nonce Tracking with TTL:** The backend checks if `X-Nonce` exists in a distributed cache (Redis) using an atomic `SET NX EX 300`. If the key already exists, it is a duplicate replay attempt and rejected immediately.
3. **Step 3: Constant-Time Comparison:** When validating signatures, use constant-time equality comparison (`MessageDigest.isEqual()` in Java) to eliminate side-channel timing attacks.

---

## 6. SDE2 Interview Synthesis

| Mechanism | Primary Threat Mitigated | Core Invariant | Failure / Recovery Mode |
| :--- | :--- | :--- | :--- |
| **JWT Claim Validation** | Confused deputy, expired access, wrong tenant | Signature $\land$ Audience $\land$ Issuer $\land$ Not-Expired | Fast reject; refresh or prompt re-login. |
| **Refresh Token Rotation** | Stolen long-lived refresh tokens | Token is single-use; subsequent use of consumed token triggers family revocation | Revoke all descendant sessions; prompt full re-login. |
| **TOTP (RFC 6238)** | Credential stuffing, static password reuse | Shared seed $K$ evaluated over synchronized time-step window $T \pm 1$ | Consumed OTP cache avoids window replay; out-of-sync prompts recovery codes. |
| **WebAuthn / Passkeys** | Real-time phishing, AitM reverse proxies, credential dumps | Private key never leaves hardware; browser enforces origin binding | User registers backup passkey or falls back to step-up identity proofing. |
| **HMAC Request Signing** | Man-in-the-middle tampering, webhook forgery, replay attacks | Pre-shared key hashes canonical request body + timestamp + nonce | Drift window rejects stale calls; Redis `SET NX` drops duplicate nonces. |

---

## 7. Recommended Authoritative & Independent Learning

### Authoritative RFCs & Standards
* [RFC 6749: The OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749) - Standard token issuance and refresh mechanics.
* [RFC 6238: TOTP: Time-Based One-Time Password Algorithm](https://datatracker.ietf.org/doc/html/rfc6238) - Mathematical specification for time-step HMAC tokens.
* [W3C Web Authentication (WebAuthn) Specification](https://www.w3.org/TR/webauthn-2/) - Asymmetric credential authentication protocol.
* [OWASP REST Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html) - Canonical API authentication and request signing guidelines.

### Accessible Independent Learning Links
* [Auth0 Architecture Guide on Refresh Token Rotation](https://auth0.com/docs/secure/tokens/refresh-tokens/refresh-token-rotation) - Practical explanation of token family reuse detection.
* [Stripe Webhook Signatures Guide](https://docs.stripe.com/webhooks/signatures) - Canonical real-world implementation of HMAC signature verification and timestamp replay mitigation.

---

## Quick recall

**Q: Why is cryptographic signature verification alone insufficient to accept a JWT?**
A: Signatures only verify data integrity and key authorship. The server must independently validate `exp` (expiry), `nbf` (not before), `iss` (expected issuer), and `aud` (intended recipient service) to prevent cross-service confused deputy attacks.

**Q: How does Refresh Token Rotation with Reuse Detection detect token theft?**
A: Each refresh token is strictly single-use. When a token is exchanged, it is marked `Consumed`. If a request arrives presenting a previously consumed refresh token, the server identifies that both the legitimate client and an attacker possess the token family, immediately revoking all descendant tokens and active sessions.

**Q: What is the mathematical basis of TOTP (RFC 6238)?**
A: TOTP calculates $\text{Truncate}(\text{HMAC}(K, T)) \pmod{10^d}$, where $K$ is a pre-shared secret and $T = \lfloor (\text{UnixTime} - T_0) / 30 \rfloor$ is an integer counter advancing every 30 seconds. SHA-1 is the RFC reference profile; SHA-256 or SHA-512 are also possible by agreement.

**Q: Why are SMS OTPs vulnerable to interception while Passkeys/WebAuthn are phishing-resistant?**
A: SMS is vulnerable to SIM swapping, SS7 routing attacks, and reverse-proxy phishing. WebAuthn is hardware-bound and cryptographically binds authentication challenges to the browser-verified domain origin, preventing rogue replica domains from obtaining valid assertion signatures.

**Q: How does an API backend defend against webhook replay attacks using HMAC signing?**
A: The sender includes a timestamp and a unique nonce within the HMAC-signed canonical payload. The receiver rejects requests with timestamps outside an acceptable tolerance window (e.g., 5 minutes) and uses an atomic cache check (e.g., Redis `SET NX EX 300`) to reject repeated nonces.
