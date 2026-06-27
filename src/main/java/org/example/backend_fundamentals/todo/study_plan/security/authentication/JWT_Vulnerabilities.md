# JWT: Mechanics, Architectures, and Vulnerabilities

This document explores the mechanics of JSON Web Tokens (JWT), the difference between JWS and JWE, architectural choices (HS256 vs RS256), and classic vulnerabilities that arise from poor implementations.

---

## 1. OAuth Trust vs. SDK Trust

Before diving into JWTs, it's critical to separate two security questions that are often confused:

*Question 1: "Is this the same client that started the OAuth flow?"*
**Solved by:** PKCE. It guarantees that the same app instance that started the flow finishes it.

*Question 2: "Is this request genuinely from my trustworthy app?"*
**Solved by:** Device Attestation (Play Integrity, App Attest) and HMAC Request Signing. 
OAuth does NOT answer whether an application is a scam or if an SDK request was forged via Postman. OAuth assumes the application has already been registered and only ensures the OAuth protocol is followed.

---

## 2. JWT Mechanics: JWS vs JWE

A standard JWT has three parts: `Header . Payload . Signature`. 

### The Misconception
Many developers assume the payload inside a JWT is encrypted and safe from prying eyes.
**The Reality:** The payload is NOT encrypted. It is only Base64 encoded. Anyone who intercepts the JWT can decode and read the payload. However, because the Signature is computed over the `Header + Payload`, anyone who attempts to *modify* the payload will invalidate the signature. 

### The "Envelope" Analogy
**JWS (JSON Web Signature):** Think of this as a transparent envelope. Anyone who intercepts the envelope can read the letter inside (`Salary = ₹30L`), but because the sender signed it, nobody can secretly erase the number and change it to `₹50L`. It guarantees *Integrity* and *Authenticity*, but not *Confidentiality*.

**JWE (JSON Web Encryption):** Think of this as a locked box. The payload itself is encrypted. Only the intended recipient can unlock it and read the contents. It guarantees *Confidentiality*.

### Why do most backend systems only use JWS?
*Interview Question:* "If HTTPS already encrypts our traffic, do we still need JWE?"
*Answer:* Usually no. Most companies intentionally avoid putting highly sensitive business data (like medical records or salaries) inside a JWT. A standard JWT contains only identity and authorization claims (`sub`, `role`, `exp`). Sensitive data is fetched from the database when required.
Because HTTPS provides encryption in transit, and JWS provides integrity, JWE is rarely needed. JWE introduces significant complexity and key management overhead. 

*Exceptions:* JWE is useful in microservices where an edge gateway passes a token through an Order Service and a Payment Service, but the token contains a payload that only the Payroll Service is authorized to read.

---

## 3. HS256 vs RS256 (Symmetric vs Asymmetric)

### The Architecture Problem with HS256
In HS256 (HMAC with SHA-256), the *exact same shared secret* is used to both create the signature and verify the signature. 

Imagine an architecture with an API Gateway and 20 microservices (User, Payment, Order, Notification, etc.). To verify incoming JWTs, every single one of those 20 microservices must have a copy of the `JWT_SECRET`.
**The Vulnerability:** If the Notification Service gets compromised, the attacker steals the `JWT_SECRET`. Because HS256 uses the same secret for verification and signing, the attacker can now mint perfectly valid JWTs (e.g., `{"role": "ADMIN"}`) and send them to the Payment Service. The Payment Service will accept them because the signature is mathematically valid.

### The RS256 Solution
RS256 uses an asymmetric key pair:
- **Private Key:** Used only to *Sign* the JWT.
- **Public Key:** Used only to *Verify* the JWT.

In modern architectures, only the Identity Provider (Google, Auth0) holds the Private Key. It is the only entity that can create JWTs.
The 20 microservices only receive the Public Key. They can verify the token, but they cannot create one. If a microservice is hacked, the attacker only steals a Public Key. They cannot forge new tokens, severely limiting the blast radius.

---

## 4. Classic JWT Vulnerabilities

Both of these classic attacks share the exact same root cause: **The backend server blindly trusted the attacker-controlled JWT Header instead of enforcing its own security policy.**

### Vulnerability 1: The `alg=none` Attack
A normal JWT header looks like `{"alg": "RS256"}`. 
**The Attack:** An attacker modifies the payload (e.g., `role=ADMIN`), completely deletes the Signature section of the token, and changes the header to `{"alg": "none"}`.
**The Exploit:** Poorly written JWT libraries used to read the header, see `alg=none`, and say, "Ah, the algorithm is none, so I don't need to verify a signature!" The token is accepted, and the attacker becomes an Admin.
**The Fix:** The backend must explicitly configure which algorithms it accepts (e.g., `Allowed Algorithms: ["RS256"]`). If a token arrives with `alg=none`, it should be instantly rejected.

### Vulnerability 2: Algorithm Confusion Attack
**The Scenario:** Your server expects RS256 and has securely stored a Public Key to verify tokens.
**The Attack:** An attacker knows your Public Key (because Public Keys are often exposed via a `/jwks` endpoint). The attacker creates a forged token, sets the header to `{"alg": "HS256"}`, and signs the token using HMAC-SHA256, but uses your *RSA Public Key* as the HMAC secret!
**The Exploit:** The buggy JWT library reads the header (`alg=HS256`). Instead of rejecting the token, it switches to HMAC mode. It grabs the configured key (which happens to be the RSA Public Key) and uses it as the HMAC secret to verify the signature. Because the attacker used the same Public Key to sign it, the math works out perfectly. The token is accepted.
**The Fix:** Again, do not trust the JWT header. If the server expects RS256, and a token arrives with `HS256`, reject it immediately before attempting any cryptographic verification.

---

## Quick recall

**Q: Can anyone read the payload of a standard JWT?**
A: Yes. A standard JWT (JWS) payload is only Base64 encoded, not encrypted. It provides integrity, not confidentiality.

**Q: What is the main architectural flaw of using HS256 across multiple microservices?**
A: HS256 uses a symmetric key. Every microservice that needs to verify a token must have the secret. If one service is compromised, the attacker can forge valid tokens for all other services.

**Q: How does RS256 solve the shared secret problem?**
A: By using asymmetric keys. Only the authorization server holds the Private Key to mint tokens. Microservices only hold the Public Key to verify them, meaning a compromised microservice cannot forge tokens.

**Q: What is the root cause of the `alg=none` and Algorithm Confusion attacks?**
A: The backend blindly trusts the algorithm specified in the attacker-controlled JWT header, rather than strictly enforcing a pre-configured list of allowed algorithms.
