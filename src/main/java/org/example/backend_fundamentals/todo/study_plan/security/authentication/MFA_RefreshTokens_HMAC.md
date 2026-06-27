# JWT Validation, Refresh Tokens, MFA & API Security

This document captures the final tier of security topics. It moves beyond standard OAuth and JWT mechanics into proper validation, token lifecycles, advanced authentication, and securing API requests.

---

## 1. JWT Validation (Beyond Signatures)

### The Misconception
Developers often assume that if a JWT's cryptographic signature is valid, the token should be immediately accepted.

### The Reality
Signature verification only proves that the token wasn't modified and that it was issued by the expected signer. It does **not** prove that the token is actually valid for *this specific service* at *this specific moment*.
The backend must manually validate the following standard claims:

- **`exp` (Expiration Time):** Has the token expired? Reject if the current time is past `exp`.
- **`nbf` (Not Before):** Is the token valid yet? Reject if the current time is before `nbf`.
- **`iss` (Issuer):** Who issued this token? If your backend expects `https://accounts.google.com` and the token says `UnknownIssuer`, reject it immediately.
- **`aud` (Audience):** Was this token meant for this service? A token issued for Service A should not automatically work to access sensitive data in Service B.
- **`iat` (Issued At):** Useful for freshness checks and debugging.

*Interview Summary:* "Signature verification proves the token wasn't tampered with. Claim validation proves the token is actually valid for this service at this point in time."

---

## 2. Refresh Token Security

### Token Lifetimes
- **Access Token:** Short-lived (e.g., 5, 15, or 30 minutes). If stolen, the attacker only has a small window of access.
- **Refresh Token:** Long-lived (e.g., days, weeks, or months). Used to securely request new Access Tokens when the old one expires.

### Refresh Token Rotation
*Old Approach:* A single Refresh Token was issued and used forever. If an attacker stole it, they had permanent access to the user's account until it was manually revoked.
*Modern Approach (Rotation):* Every time a Refresh Token is used to get a new Access Token, the server issues a **new** Refresh Token and invalidates the old one. 

### Reuse Detection
What happens if an attacker steals `RT1`? 
If the legitimate user already consumed `RT1` to get a new session, `RT1` is invalidated. If the attacker later tries to use `RT1`, the backend immediately notices that an already-consumed Refresh Token is being reused. 
This strongly indicates token theft. The backend should respond by revoking the entire session chain and forcing the user to log in again.

---

## 3. Multi-Factor Authentication (MFA)

MFA requires combining multiple *independent* factors. The three standard categories are:
1. **Something You Know:** Password, PIN.
2. **Something You Have:** Smartphone, Hardware Security Key (YubiKey).
3. **Something You Are:** Fingerprint, Face ID.

### TOTP (Time-Based One-Time Password)
Examples: Google Authenticator, Microsoft Authenticator.
Mechanics: Both the server and the app share a secret seed. Combined with the current time, they generate a matching 6-digit code that changes every ~30 seconds.

### Why SMS is Weak
SMS should only be used as a fallback, not as a primary MFA factor. It is highly vulnerable to:
- **SIM Swapping:** Attackers trick the telecom provider into transferring the victim's phone number to their own SIM card.
- **Interception & Social Engineering.**

---

## 4. Passwordless Authentication

Passwordless authentication eliminates the password entirely, meaning there is nothing reusable for attackers to steal or phish.

- **Magic Links:** User enters their email. The server emails a secure, one-time link. The user clicks it and is logged in.
- **Passkeys (WebAuthn):** Becoming the industry standard. It replaces passwords with Public/Private Key cryptography. The Private Key never leaves the user's device (phone/laptop). The user authenticates locally using biometrics (fingerprint/face), and the device uses the Private Key to sign a cryptographic challenge sent by the server. 

---

## 5. API Security: HMAC Request Signing

### The Problem
OAuth and PKCE secure the *user's* login flow. But how do you secure server-to-server or SDK-to-server requests (e.g., submitting KYC data or a webhook)? How do you guarantee the request genuinely came from your SDK and wasn't modified in transit?

### The Solution: HMAC Signatures
This is completely unrelated to JWTs. Instead of just sending a raw JSON body, the SDK computes an HMAC signature over the entire request:
```
HMAC(HTTP Method + URL + Body + Timestamp + Secret)
```
The SDK sends this Signature along with the request. The backend recomputes the HMAC using the same Secret. If they match, the backend knows:
1. The request was not modified.
2. The sender actually possesses the shared secret.
*(Real-world examples: AWS Signature Version 4, Stripe Webhooks, Razorpay Webhooks).*

### Replay Attack Protection
*The Attack:* An attacker intercepts a valid request (e.g., "Transfer ₹10000") and replays it the next day. The HMAC signature will still be perfectly valid because the body hasn't changed.
*The Fix:* Include a **Timestamp** and a **Nonce** (a random, one-time string) in the HMAC payload. The backend rejects any requests with timestamps older than a few minutes, and tracks Nonces to reject duplicates.

---

## Quick recall

**Q: Is signature verification enough to accept a JWT?**
A: No, you must also validate claims like `exp` (expiry), `iss` (issuer), and `aud` (audience) to ensure the token is valid for your specific service right now.

**Q: How does Refresh Token Rotation work?**
A: Every time a Refresh Token is used, it is invalidated and a new one is issued. If an old, consumed Refresh Token is ever reused, it indicates theft, and the session should be revoked.

**Q: Why is SMS considered a weak MFA factor?**
A: It is highly vulnerable to SIM swapping and interception.

**Q: What is a Passkey?**
A: A password replacement that uses Public/Private Key cryptography. The Private Key never leaves the device, making it highly resistant to phishing.

**Q: How does HMAC Request Signing protect API calls?**
A: It computes a hash over the request body and metadata using a shared secret. It proves the sender has the secret and the payload wasn't tampered with.

**Q: How do you prevent a Replay Attack on an API?**
A: Include a Timestamp and a Nonce in the request payload and signature. The backend rejects stale timestamps and duplicate nonces.
