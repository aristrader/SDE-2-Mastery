---
order: 10
---

# Authentication: OAuth 2.0, JWT, Sessions, and API Keys

## Identity Representation: JWT vs API Keys
A fundamental point of confusion is why API keys exist when JWTs are so prevalent. The distinction is about **who is being represented**.

- **JWT (JSON Web Token):** A compact, signed claims format (RFC 7519) representing the identity of a **User** or a **Workload** (e.g., Alice logged into a client app, or a service calling another service via M2M tokens).
- **API Key:** A long-lived, static **bearer client secret** representing an **Application** or **System** (e.g., your backend calling Google AI Studio or Stripe).

API Keys are much simpler operationally for machine-to-machine integrations. You copy the key, paste it into a header, and you're done. There's no login flow, no token expiry, and no refresh handling. The API Gateway identifies the tenant, bills them, and enforces rate limits entirely via this key. 

**Gotcha:** API Keys are Bearer tokens. If an attacker steals the key, they immediately gain full access to the application's quota and permissions.

## Stateful Sessions vs Stateless Tokens (JWTs)

| Dimension | Stateful Sessions | Stateless JWTs |
| :--- | :--- | :--- |
| **Storage** | Server-side (Redis, Database, Memory) | Client-side (Browser memory, HttpOnly cookie, Mobile Secure Storage) |
| **Verification** | DB/cache lookup per request | Cryptographic signature verification using configured key (often public for asymmetric algorithms) |
| **Revocation** | Instant (delete or invalidate session key in store) | Hard before expiry (requires blacklists or token rotation) |
| **Scalability** | Requires centralized session store across nodes | Decoupled and easily scalable across microservices |
| **Best Fit** | Server-rendered web apps, high-security admin tools | Distributed microservices, mobile apps, cross-domain APIs |

## OAuth 2.0 vs Authentication (OIDC)
Another massive misconception is treating OAuth 2.0 and JWT as the same thing.
- **OAuth 2.0** is an **Authorization Framework** (RFC 6749). It dictates *how* tokens are acquired, requested, and exchanged. OAuth access tokens may be structured (signed JWTs) or opaque reference strings.
- **OpenID Connect (OIDC)** adds an **Authentication Layer** on top of OAuth 2.0, introducing the `id_token` to prove who the user is.
- **JWT** is a **Token Format** (RFC 7519). It dictates *what* the token looks like.

**Analogy:** OAuth 2.0 is the shipping company (FedEx). JWT is the cardboard box. OIDC is the identity badge inside the box. You can use OAuth to deliver an opaque token, or you can use a JWT without ever touching OAuth.

### The Problem OAuth Solves
OAuth exists to solve one specific problem: **How can Application A access resources on behalf of User B without knowing User B's password?**
Example: Canva wants to access your Google Drive. 
Without OAuth, you would have to give Canva your Google password. With OAuth, Google authenticates you, issues a scoped access token to Canva, and Canva uses that token. Canva never sees your password.

## OAuth 2.0 Core Flows

### 1. Authorization Code Flow with PKCE (RFC 7636, RFC 9700)
Used for Single-Page Applications (SPAs), mobile apps, and public clients that cannot securely store a `client_secret`.
- **PKCE (Proof Key for Code Exchange):** Binds the authorization code to a per-request secret (`code_verifier`). The client sends its SHA-256 hash (`code_challenge`) during the initial authorization request.
- On token exchange, the client sends the plaintext `code_verifier`. The authorization server verifies it matches the original challenge before issuing tokens, preventing authorization code interception attacks even if the redirect URI is observed.
- **Scope & Complementary Controls:** PKCE does not replace strict exact redirect URI validation, CSRF state parameters (`state`), OIDC `nonce` (for replay defense), or TLS transport security; it complements them specifically to protect the code exchange step.

### 2. Client Credentials Flow (M2M)
Used when Service A communicates directly with Service B without user context.
1. Service A authenticates with the Authorization Server using its `Client ID` and `Client Secret`.
2. The AS returns a short-lived access token (which may be a signed JWT or an opaque token).
3. Service A includes this token in the `Authorization: Bearer` header to call Service B.

## Access Tokens vs Refresh Tokens

Why do Refresh tokens exist? If an Access Token expires, why not just issue another one, or make the Access Token last forever?
- If an Access Token lasts 30 days and is stolen (e.g. via XSS stealing it from `localStorage`), the attacker has 30 days of free reign.
- If an Access Token lasts 15 minutes, the blast radius is heavily contained, but the user would have to re-login every 15 minutes without a refresh mechanism.

The **Refresh Token** solves this by splitting the problem:
1. **Access Token:** Short-lived (e.g. 5–15 min). Used for every API request. Transmitted constantly. Often stateless (JWT).
2. **Refresh Token:** Long-lived (e.g. 7–30 days). Used *only* to exchange for a new Access Token. Transmitted rarely. Often stateful (stored in Redis for instant revocation).

### Token Storage Tradeoffs & Platform Defense
- **Web Applications:**
  - `HttpOnly`, `Secure`, `SameSite` cookies prevent JavaScript from accessing tokens, protecting them from XSS exfiltration. However, cookies are automatically attached by the browser, requiring CSRF defenses (`SameSite=Strict`/`Lax`, anti-CSRF tokens, or custom headers) for state-changing requests.
  - In-memory storage avoids CSRF and XSS persistence across sessions, but loses state on page refresh. Web storage (`localStorage`) is vulnerable to XSS exfiltration.
- **Mobile Applications:**
  - Native mobile apps typically use OS-protected secure storage (iOS Keychain; Android Keystore-backed storage where available). Hybrid/WebView clients need browser-style token, CSRF, and XSS analysis.

## Quick recall
**Q. What is the fundamental difference in what a JWT represents vs an API Key?**
A. A JWT is a signed claims format representing a user or workload identity. An API Key is a static bearer client secret representing an application/system identity.

**Q. Why is PKCE required for Single-Page Applications (SPAs) and mobile apps, and what does it protect?**
A. Public clients cannot protect a `client_secret`. PKCE binds the authorization code to a per-request `code_verifier` to prevent code interception attacks. It complements (rather than replaces) exact redirect URI validation, `state`, OIDC `nonce`, and TLS.

**Q. How do stateless JWTs and stateful sessions trade off revocation vs scalability?**
A. Stateful sessions allow instant revocation but require a centralized session store lookup per request. JWTs scale seamlessly with cryptographic signature verification using a configured key (often public for asymmetric algorithms) without database checks, but cannot be revoked immediately before TTL expiry without a blacklist.

**Q. What are the tradeoffs between HttpOnly cookies and mobile secure storage for token protection?**
A. `HttpOnly` + `Secure` + `SameSite` cookies shield web tokens from JavaScript XSS exfiltration but require CSRF mitigations. Native apps typically use OS-protected storage; hybrid/WebView clients retain browser-style XSS and CSRF considerations.

## Sources
- **RFC 7519:** JSON Web Token (JWT) (`https://datatracker.ietf.org/doc/html/rfc7519`)
- **RFC 6749:** The OAuth 2.0 Authorization Framework (`https://datatracker.ietf.org/doc/html/rfc6749`)
- **RFC 7636:** Proof Key for Code Exchange by OAuth Public Clients (PKCE) (`https://datatracker.ietf.org/doc/html/rfc7636`)
- **RFC 9700:** Best Current Practice for OAuth 2.0 Security (`https://datatracker.ietf.org/doc/html/rfc9700`)
- **OpenID Connect Core 1.0:** Identity layer on top of OAuth 2.0 (`https://openid.net/specs/openid-connect-core-1_0.html`)
- **OWASP OAuth 2.0 Cheat Sheet:** (`https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Cheat_Sheet.html`)
- **OWASP REST Security Cheat Sheet:** (`https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html`)
- **Spring Security Reference Documentation:** OAuth 2.0 Client & Resource Server (`https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html`)
