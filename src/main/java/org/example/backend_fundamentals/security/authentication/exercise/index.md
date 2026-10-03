---
order: 10
search: false
---

# Exercises: Authentication Mechanisms & OAuth 2.0

## Exercise: auth-mechanism-tradeoffs - Selecting Between Sessions, JWTs, and API Keys

### Problem
You are designing authentication for three distinct client scenarios in a distributed e-commerce platform:
1. **Internal Admin SPA:** High-privilege backoffice portal hosted on the same domain where immediate account revocation on employee deprovisioning is mandatory.
2. **Third-Party Partner Ingestion:** External partner servers uploading nightly catalog feeds via automated batch jobs.
3. **High-Throughput Mobile App:** Millions of active mobile users querying downstream microservices where cross-service DB session lookups create a critical bottleneck.

### Requirements
1. Recommend the best authentication/identity mechanism (`Stateful Session Cookie`, `Stateless JWT`, `OAuth 2.0 Client Credentials`, or `Static API Key`) for each scenario.
2. Justify each choice based on revocation guarantees, latency, and client trust model.
3. Identify the primary security risk for each selection and its mitigation.

## Exercise: oauth-pkce-defense - Securing Public Clients with Authorization Code + PKCE

### Problem
A single-page React application integrates with a third-party identity provider (IdP) using OAuth 2.0. A developer proposes using the standard Authorization Code grant without PKCE and embedding a hardcoded `client_secret` in the frontend bundle.

### Requirements
1. Explain why embedding a `client_secret` in a public client (SPA or mobile app) fails the OAuth 2.0 security model.
2. Walk through how PKCE (`code_verifier` and `code_challenge` via RFC 7636) eliminates the need for a client secret and prevents authorization code interception attacks.
3. Detail the exact cryptographic handshake sequence between Client, User Agent, Authorization Server, and Resource Server.
