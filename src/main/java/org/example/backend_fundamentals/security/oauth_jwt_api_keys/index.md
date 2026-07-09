---
order: 50
---

# Deep Dive: OAuth 2.0, JWT, and API Keys

## Authentication vs Authorization
- **Authentication**: "Who are you?" (e.g., Login with Google). User identity is established.
- **Authorization**: "What are you allowed to do?" (e.g., Can read profile, upload photo). Permission is granted or denied.
- **OAuth primarily solves the authorization problem.** It asks "What is this application allowed to access?" rather than "Who is the user?"

## OAuth Explained Using a Real Example
Scenario: A photo-printing application wants access to files stored in Google Drive.
- **Without OAuth**: User gives Gmail password → App logs into Google → App reads files. Bad design, app knows user's password.
- **With OAuth**: User clicks "Login with Google". Google asks for consent to read photos. User approves. Google returns an **Access Token** instead of the password. App uses the token (`Authorization: Bearer xyz`) to access photos. Password is never shared.

## OAuth Actors
- **Resource Owner**: The owner of the data (e.g., the User).
- **Client**: The application requesting access (e.g., Photo Printing App).
- **Authorization Server**: The server responsible for login, consent, and token issuance (e.g., Google's login infrastructure).
- **Resource Server**: The actual API that stores the protected data (e.g., Google Photos API).

## OAuth Concepts
- **Access Token**: A temporary permission slip (can be a random string or a JWT). Means the user approved access.
- **Scope**: Limits what the application can do (e.g., `read_profile`, `read_photos`). Avoids granting unrestricted access.
- **Refresh Token**: Access Tokens typically expire (e.g., 15 minutes) for security (limits blast radius if leaked). Once expired, a Refresh Token is used to obtain a new Access Token.

## OAuth vs OpenID Connect (OIDC)
- **Misconception**: OAuth = Login.
- **Correction**: OAuth provides *permission*, not *identity*. OIDC is a layer on top of OAuth that adds identity.
- **OIDC Focus**: "Who is the user?" It provides an **ID Token** (usually a JWT) containing identity info (e.g., `sub`, `email`, `name`).
- **Login with Google**: Uses both OAuth (for permissions) and OIDC (for identity).

| OAuth | OIDC |
|---------|---------|
| Authorization | Authentication + Authorization |
| Grants resource access | Identifies user |
| Access Token | Access Token + ID Token |
| Permission focused | Identity focused |

## JWT vs OAuth
- **Misconception**: JWT and OAuth are the same thing.
- **Correction**: JWT is only a token format (JSON Web Token) often used to carry identity or authorization info. OAuth is the authorization framework defining how access is granted.
- **Analogy**: OAuth = Driver's License Authority. JWT = The physical plastic Driver's License Card.

## Authorization Code Flow vs Client Credentials Flow
- **Client Credentials Flow**: Machine-to-Machine Authentication (e.g., Backend → Backend). No human user is involved. Client ID + Secret → Access Token.
- **Authorization Code Flow**: Human user is involved. User logs in, grants consent → Authorization Code → exchanged for Access Token.

## OAuth 1.0 vs OAuth 2.0
- **OAuth 1.0**: Older, complex, required request signing.
- **OAuth 2.0**: Redesign relying on HTTPS and Bearer Tokens, simpler, supports various flows like Authorization Code, Client Credentials, and PKCE.

## API Keys vs OAuth Tokens
- **API Keys**: Represent *Application Identity* (e.g., "I am Application X"). They are simple, usually long-lived, and have no user involvement. Often acceptable in secure, trusted internal environments (e.g., VPC, Kubernetes) where secrets are stored securely (Vault).
- **OAuth Tokens**: Represent *User Delegated Permission* (e.g., "I am acting on behalf of Swapnil"). They use short-lived tokens (Access Tokens) and Refresh Tokens to minimize credential leakage risk.
- **Why not use API Keys for User Authorization?**: API Keys cannot express user consent.
