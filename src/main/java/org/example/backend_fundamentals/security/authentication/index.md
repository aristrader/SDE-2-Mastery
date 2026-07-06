---
order: 10
---

# Authentication: OAuth, JWT, and API Keys

## Identity Representation: JWT vs API Keys
A fundamental point of confusion is why API keys exist when JWTs are so prevalent. The distinction is about **who is being represented**.

- **JWT (JSON Web Token):** Represents the identity of a **User** (e.g., Alice logging in via a browser or mobile app).
- **API Key:** Represents the identity of an **Application** or **System** (e.g., your backend calling Google AI Studio or Stripe).

API Keys are much simpler operationally for machine-to-machine integrations. You copy the key, paste it into a header, and you're done. There's no login flow, no token expiry, and no refresh handling. The API Gateway identifies the tenant, bills them, and enforces rate limits entirely via this key. 
**Gotcha:** API Keys are Bearer tokens. If an attacker steals the key, they immediately gain full access to the application's quota and permissions.

## OAuth 2.0 vs JWT
Another massive misconception is treating OAuth 2.0 and JWT as the same thing.
- **OAuth 2.0** is an **Authorization Framework**. It dictates *how* tokens are acquired, requested, and exchanged.
- **JWT** is a **Token Format**. It dictates *what* the token looks like.

**Analogy:** OAuth 2.0 is the shipping company (FedEx). JWT is the cardboard box. You can use OAuth to deliver an opaque token, or you can use a JWT without ever touching OAuth.

### The Problem OAuth Solves
OAuth exists to solve one specific problem: **How can Application A access resources on behalf of User B without knowing User B's password?**
Example: Canva wants to access your Google Drive. 
Without OAuth, you would have to give Canva your Google password. With OAuth, Google authenticates you, issues a scoped access token to Canva, and Canva uses that token. Canva never sees your password.

## Access Tokens vs Refresh Tokens

Why do Refresh tokens exist? If an Access Token expires, why not just issue another one, or make the Access Token last forever?
If an Access Token lasts 30 days and is stolen (e.g. via XSS stealing it from `localStorage`), the attacker has 30 days of free reign.
If an Access Token lasts 15 minutes, the damage is heavily contained, but the user would have to log in every 15 minutes.

The **Refresh Token** solves this by splitting the problem:
1. **Access Token:** Short-lived (e.g. 15 min). Used for every single API request. Transmitted constantly. Often stateless (JWT).
2. **Refresh Token:** Long-lived (e.g. 30 days). Used *only* to get a new Access Token. Transmitted rarely. Often stateful (stored in Redis for easy revocation).

### Why aren't Refresh Tokens stolen just as easily?
Because of **where** they are stored. Access Tokens are frequently kept in JavaScript memory or `localStorage` to attach to `Authorization: Bearer` headers. Refresh Tokens are usually stored in `HttpOnly` cookies, meaning JavaScript literally cannot read them. This drastically reduces the attack surface for XSS.

## Machine-to-Machine OAuth (Client Credentials)
When Service A needs to talk to Service B, there is no user to click "Login".
Instead, we use the **Client Credentials Flow**:
1. Service A authenticates with the Authorization Server (e.g. Keycloak) using its `Client ID` and `Client Secret`.
2. The AS returns a JWT.
3. Service A uses that JWT to call Service B.

Why use this instead of a static API Key forever? Because OAuth adds short-lived tokens, granular scopes, centralized role management, and easy revocation. A leaked Client Secret only matters if the attacker can reach the AS; a leaked static API Key is an immediate compromise.

## Quick recall
**Q. What is the fundamental difference in what a JWT represents vs an API Key?**
A. A JWT represents a User's identity and session. An API Key represents an Application's identity.

**Q. Is OAuth just JWTs with expiry times?**
A. No. OAuth is the authorization framework (the delivery mechanism). JWT is just the token format (the package). They are completely independent concepts.

**Q. Why do we need a Refresh Token? Why not just use a long-lived Access Token?**
A. A long-lived Access Token is disastrous if stolen. A Refresh Token allows the Access Token to be short-lived (reducing blast radius) while maintaining a long-lived user session, and is typically stored more securely (HttpOnly cookies).

**Q. What OAuth flow is used for Service-to-Service communication?**
A. Client Credentials flow. Service A exchanges its Client ID + Secret for an Access Token directly with the Authorization Server, with no user interaction.


