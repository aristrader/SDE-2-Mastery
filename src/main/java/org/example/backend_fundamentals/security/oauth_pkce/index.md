---
order: 60
---

# OAuth 2.0 & PKCE Deep Dive

This document captures a deep-dive discussion into the mechanics of OAuth 2.0 and PKCE. It is designed to be read from scratch, explicitly covering the fundamental "whys", common misconceptions, and the exact security boundaries of the protocols.

---

## 1. Why does the Authorization Code actually exist?

### The Misconception
When first learning OAuth, it is very common to assume that the Authorization Code exists because returning a JWT directly to the browser would create poor UX (since JWTs expire), and that the Authorization Code behaves somewhat like a long-lived API key used to repeatedly fetch tokens based on login frequency.

### The Reality
The Authorization Code does **not** exist because of token expiry or UX. It exists for a very specific security reason: **to prevent the Access Token from ever being exposed to the browser.**

If Google returned an Access Token directly to the frontend (the "Implicit Flow"), that token would sit in the browser where it could potentially be stolen via cross-site scripting (XSS) or browser history logging. 

Instead, Google returns a temporary, one-time `Authorization Code`. By itself, this code is completely useless. Only the legitimate backend—which possesses the highly confidential `client_secret`—can exchange that code with Google for an actual Access Token. If an attacker intercepts the Authorization Code in the browser, they can do nothing with it because they do not have your backend's `client_secret`.

---

## 2. The Two Separate Conversations of OAuth

A major point of confusion is whether the backend also authenticates with Google, and where the browser fits into the flow. The key to understanding OAuth is realizing that two completely different conversations are happening.

**Conversation 1: The Browser Interaction**
```
Browser  →  Google  →  User logs in  →  User consents
```
This conversation is entirely about user interaction. The browser is directed to Google, the user enters their password, and they click "Allow". Google then redirects the browser back to your backend, carrying the Authorization Code.

**Conversation 2: The Backend Token Exchange**
```
Your Backend  →  Google  →  Exchange Authorization Code  →  Receive Access Token
```
This conversation is entirely server-to-server. The backend takes the code it received from the browser and securely trades it with Google for the actual tokens.

**Summary:** The Authorization Code flows *through* the browser. The browser merely transports it, but the backend eventually receives and consumes it.

---

## 3. Why involve the Browser at all?

### The Struggle
If the backend is doing the secure token exchange anyway, why can't the backend simply communicate server-to-server with Google to log the user in? Why do we need the browser to redirect the user to Google?

### The Reason: Verifiable Consent
Google needs absolute proof that:
1. The actual user logged in.
2. The user explicitly clicked "Allow".

If your backend called Google directly via an API to log the user in, there would be no trusted user interaction. Your backend could simply fake the consent and say "Yes, the user approved this." 
Because Google personally owns the login page and the consent screen displayed in the browser, Google can guarantee that the human user actually consented. The browser provides this necessary, trusted boundary.

---

## 4. The Consent Screen and Client IDs

### The Confusion
On the Google consent screen, it says *"Spotify wants access to your account."* But the `client_id` used to trigger this flow is public! What stops an attacker at `evil.com` from using Spotify's `client_id` to trick a user?

### How Google prevents this
During the initial OAuth registration, Spotify permanently registers three things with Google:
```
Client ID  →  Application Metadata (Logo, Name)  →  Allowed Redirect URIs
```

When a request arrives at Google with `client_id = spotify123`, Google uses that ID simply as a lookup key in its database to display the word "Spotify" and the Spotify logo. Google never assumes the `client_id` is a secret.

If `evil.com` attempts to use Spotify's `client_id`, Google will indeed show "Spotify wants access". However, when the user clicks "Allow", Google will **strictly refuse** to redirect the Authorization Code to `evil.com`. Google will only redirect the code back to one of Spotify's pre-registered `Redirect URIs`. The attacker never receives the code.

---

## 5. Session Linking and CSRF Prevention

### The Problem
When the Authorization Code finally arrives at the backend's callback URL, how does the backend know which browser session it belongs to?

### The Solution: The `state` parameter
Before redirecting the user to Google, the backend generates a random `state` string and saves it in the user's session.
```
state  →  Browser Session (Original Login Request)
```
The browser sends this `state` to Google. When Google redirects the user back, it returns the Authorization Code **plus the exact same `state`**. 

The backend looks at the returned `state`, matches it to the session in its database, and now securely knows exactly which user initiated the flow. This prevents Cross-Site Request Forgery (CSRF) and securely links the callback to the original login session.

---

## 6. PKCE: Protecting Mobile Apps

### The Mobile Secret Problem
In traditional Web OAuth, the backend proves its identity during the token exchange by sending the `client_secret`. 
However, mobile apps cannot safely store a `client_secret`. If you embed a secret inside an APK or iOS app, it can easily be reverse-engineered and extracted by attackers. Once extracted, the secret is compromised for every single user of your app.

**Design Question:** How can Google authenticate a Mobile App during the token exchange without using a static Client Secret?
- *Option 1:* Ask the user for their password again. (Rejected: Terrible UX).
- *Option 2:* Ask the backend every time. (Rejected: OAuth is designed to work even for serverless or local apps without requiring a custom backend).

### The Solution: A Temporary, One-Time Secret (PKCE)
Proof Key for Code Exchange (PKCE) solves this by having the mobile app generate a temporary, one-time secret for *every single login flow*.

1. **code_verifier:** Before opening Google, the app generates a random, long, unpredictable string. This acts as our temporary client secret.
2. **code_challenge:** The app cannot send the verifier directly in the initial URL (because it could be intercepted or logged). Instead, the app computes a hash: `code_challenge = SHA256(code_verifier)`.
3. **The Request:** The app sends the `client_id` and the `code_challenge` to Google. Google stores the challenge.
4. **The Exchange:** When the app receives the Authorization Code, it sends the code along with the raw `code_verifier` to the `/token` endpoint. 
5. **The Verification:** Google hashes the raw verifier it just received. If `SHA256(verifier) == stored_challenge`, Google knows it is talking to the exact same app instance that started the flow, and issues the Access Token.

---

## 7. The Exact Attack PKCE Prevents

### The Struggle
A major point of confusion when learning PKCE is this thought: *"If an attacker starts the OAuth flow from the beginning, they just generate their own `code_verifier` anyway. Why is PKCE useful at all?"*

### The Clarification
PKCE is **not** meant to prevent someone from starting a new OAuth flow. Google is perfectly happy if another application wants to start a login flow. That is not an attack.

PKCE protects **the continuation of an existing OAuth flow**. It specifically prevents **Authorization Code Interception**.

Imagine this flow:
1. Your Legitimate App starts the flow.
2. Google issues the Authorization Code and redirects back to `myapp://callback`.
3. An attacker's app on the same phone has also registered `myapp://callback` (a technique known as URI Hijacking) and intercepts the Authorization Code.
4. The attacker attempts to exchange the stolen Authorization Code at the `/token` endpoint.

Without PKCE, the attacker succeeds. 
With PKCE, Google asks the attacker for the `code_verifier`. Because the attacker only stole the Authorization Code and did not generate the original `code_verifier`, the token exchange fails. PKCE ensures that only the app instance that started the flow can finish it.

---

## 8. Mobile Browser Flow

### The Confusion
Does the browser disappear in Mobile OAuth? 

### The Reality
No, the browser still exists. The mobile app opens the system browser (or a Custom Tab) to navigate to Google. The user logs in and consents within the browser. 

Instead of redirecting to a web URL like `https://spotify.com/callback`, Google redirects to a custom URI scheme like `myapp://callback` (or a Universal Link). The mobile Operating System detects this URI, closes the browser, and reopens your app, handing it the Authorization Code.

---

## 9. Frontend Trust vs SDK Trust

### Frontend Claims
Even if your frontend SDK says "Login Successful", the backend should never blindly trust this. The backend must independently validate the tokens and signatures.

### Two Completely Different Security Problems
It is easy to confuse OAuth trust with SDK trust. They solve two different problems:

1. **OAuth/PKCE Problem:** Can Google trust that the same client is finishing the OAuth flow? (Solved by PKCE).
2. **SDK Request Problem:** Can your backend trust that an API request (e.g., submitting KYC data) is actually coming from your genuine SDK and not an attacker with Postman? 

OAuth does **not** solve the SDK Request problem. Ensuring a request genuinely comes from your app is solved by completely different mechanisms such as Play Integrity, App Attest, Device Attestation, and HMAC Request Signing.

---

## Quick recall

**Q: Why does the Authorization Code flow exist instead of returning tokens directly?**
A: To keep the Access Token out of the browser. The Auth Code is useless if intercepted because it requires the `client_secret` (or PKCE verifier) to exchange it.

**Q: Why is the browser required in OAuth?**
A: So the Identity Provider (Google) can guarantee the user actually logged in and consented, preventing the backend from faking user approval.

**Q: How do we prevent CSRF in OAuth?**
A: Using the `state` parameter. The backend sends a random string and verifies it matches when the redirect returns.

**Q: What specific attack does PKCE prevent?**
A: Authorization Code Interception. If a malicious app steals the Auth Code during the mobile redirect, it cannot exchange it for an Access Token without the `code_verifier`.

**Q: Does PKCE prevent network sniffing?**
A: No, HTTPS already protects the transport layer. PKCE protects against interception at the OS level (e.g., custom URI scheme hijacking).
