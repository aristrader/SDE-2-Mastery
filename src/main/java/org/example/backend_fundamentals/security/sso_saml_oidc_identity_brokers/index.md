---
order: 90
---

# Deep Dive: SSO, SAML, OAuth2, OIDC, Identity Providers, Identity Brokers

## Single Sign-On (SSO) Overview

Single Sign-On (SSO) is an authentication process where a user can access multiple applications or websites using a single set of login credentials. Instead of logging into every application separately, the user authenticates once and gains access to multiple trusted applications.

Key idea: SSO is primarily solving authentication ("Who are you?") once instead of repeatedly for every application.

### Components of SSO

#### Identity Provider (IdP)
The system responsible for authenticating users.
- **Responsibilities:** Store user identities, verify credentials (passwords, MFA), generate identity assertions/tokens, and tell applications who the user is.
- **Examples:** Google, Okta, Azure AD, Auth0, OneLogin.
- **Mental Model:** IdP answers: "Who are you?"

#### Service Provider (SP)
The application that provides the actual service.
- **Responsibilities:** Provide functionality, trust the IdP, and consume identity information from the IdP.
- **Examples:** Jira, Slack, GitHub, Zoom.
- **Mental Model:** "I trust Okta. If Okta says this user is Swapnil, I believe it." Application never validates the password itself.

#### Identity Broker
An intermediary between multiple Identity Providers and multiple Applications.
- **Purpose:** Applications integrate once with the broker. The broker integrates with multiple identity providers. It acts like an API Gateway for identity.
- **Protocol Translation:** A broker can perform translation between different protocols (e.g., SAML to OIDC), allowing systems using different protocols to interoperate.
- **Examples:** Keycloak, Auth0, Okta, Azure AD B2B Federation.

---

## Authentication vs Authorization

- **Authentication:** Verifying identity ("Swapnil is verified"). Example: Google verifying credentials.
- **Authorization:** Determining permissions ("Can Swapnil access Production?"). Example: Jira deciding access based on identity information (Role, Department).

---

## SAML (Security Assertion Markup Language)

An older, open standard using XML for exchanging authentication and identity information between systems. Primarily used for Identity Federation and Enterprise SSO.

- **SAML Assertion:** A digitally signed statement made by the Identity Provider (e.g., User is Swapnil, Email is ..., Role is ...). The SP verifies the signature and trusts the statement.
- **Identity Federation:** One organization trusting another's identity system (e.g., Company A trusts Company B's IdP).
- **Why Enterprises Still Use SAML:** Legacy systems (SAP, Oracle, Workday) already support it, it matches traditional browser-based corporate applications, and provides mature federation, compliance, and governance.

---

## OIDC (OpenID Connect) vs OAuth 2.0 vs SAML

### OAuth 2.0
- **Purpose:** Authorization.
- **Question:** "What can this application access?" (e.g., Can Canva access your Google Photos?)
- **Note:** OAuth2 by itself is not primarily about login.

### OIDC (OpenID Connect)
- **Purpose:** Authentication (layered on top of OAuth 2.0).
- **Question:** "Who is the user?"
- **Provides:** ID Token (JWT).
- **Example:** "Login with Google".

### SAML vs OIDC
| Feature | SAML | OIDC |
|---|---|---|
| Data Format | XML | JSON |
| Token Type | Assertion | JWT |
| Mobile/REST Friendly | No | Yes |
| Focus | Older, Enterprise Apps | Modern, Web/Mobile/API |

---

## Session Cookie vs JWT

- **SAML World:** Usually relies on a Session Cookie where the server maintains the session.
- **OIDC World:** Usually relies on a JWT Token, often stateless, more suitable for APIs and modern architectures.

---

## Final Interview Takeaways
For SDE2 interviews, the highest ROI concepts are:
1. Authentication vs Authorization.
2. What SSO is.
3. What an Identity Provider (IdP) and Service Provider (SP) are.
4. OAuth2 vs OIDC.
5. High-level understanding of SAML.
6. Why "Continue with Google" works (typically OIDC).
7. Why enterprises use Okta, Azure AD, Google Workspace (typically SAML or OIDC depending on setup).
