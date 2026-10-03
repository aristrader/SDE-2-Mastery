---
order: 10
search: false
---

# Exercise

## Exercise: xss-threat-boundaries - Defense-in-Depth and HttpOnly Boundaries

### Context
A fintech web application is redesigning its authentication and user profile subsystem. During an architecture review, the team debates the following proposals:

1. **Proposal A:** "We should store our session JWTs in `localStorage` for easy access by our Single Page App (SPA). If we need XSS protection, we can set up a basic Content Security Policy (CSP) with `'unsafe-inline'`."
2. **Proposal B:** "If we migrate the JWT to an `HttpOnly`, `Secure`, `SameSite=Strict` cookie, our application becomes 100% immune to all impacts of XSS vulnerabilities because the attacker's script cannot extract the token value via `document.cookie`."
3. **Proposal C:** "For user profile bios where markdown/rich HTML formatting is allowed, we will simply replace `<` with `&lt;` and `>` with `&gt;` before storing and rendering with `element.innerHTML`."

### Tasks
1. **Threat Model Evaluation:** Critique Proposal A and Proposal B. Specifically, what can an attacker still achieve if they find an XSS vulnerability while session tokens are stored in `HttpOnly` cookies?
2. **Encoding vs Sanitization:** Explain why Proposal C is fundamentally flawed for both plain text and rich HTML contexts. Contrast context-aware output encoding with HTML sanitization.
3. **Defense-in-Depth Architecture:** Outline the recommended defense-in-depth design for credential storage, content rendering, and CSP policy headers.

---

## Exercise: cors-preflight-credentials - CORS Preflight and Credentialed Origin Configuration

### Context
A frontend SPA hosted on `https://dashboard.example.com` makes an authenticated API request to `https://api.example.com/v1/orders`.
The frontend code uses `fetch` with JSON payload and includes session cookies:

```javascript
fetch('https://api.example.com/v1/orders', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-Client-Version': '2.4.0'
  },
  credentials: 'include',
  body: JSON.stringify({ itemId: 'sku-998', quantity: 2 })
});
```

During testing, the browser console reports two errors:
1. `CORS preflight channel did not succeed (OPTIONS status 404 / missing headers).`
2. `The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.`

### Tasks
1. **Preflight Diagnosis:** Explain why this specific request triggers a preflight `OPTIONS` request instead of being sent as a simple request.
2. **Header Specification:** Provide the exact sequence of HTTP headers (both Request and Response) for the preflight `OPTIONS` exchange and the subsequent actual `POST` request.
3. **Security Invariant:** Explain the technical security rationale behind the browser's invariant prohibiting `Access-Control-Allow-Origin: *` when credentials are included.

---

## Exercise: csrf-cors-sop-diagnosis - SOP, CORS, and CSRF Architecture Boundaries

### Context
A backend engineer is implementing a fund transfer endpoint: `POST /api/v1/transfer` accepting form data (`account_to`, `amount`).
The service uses cookie-based session authentication (`Cookie: session_id=...`).

The engineer decides to remove the existing anti-CSRF token verification filter, providing this justification:
> *"Our backend enables CORS and strictly configures `Access-Control-Allow-Origin: https://app.example.com`. Because malicious origin `https://attacker.com` is not in our CORS allowlist, the browser will block attacker.com from submitting requests to `/api/v1/transfer`, completely eliminating the CSRF threat without needing CSRF tokens or custom headers."*

### Tasks
1. **Flaw Analysis:** Identify the core conceptual mistake in using CORS as a defense against CSRF attacks on simple endpoints.
2. **Execution Flow Trace:** Step through what happens in the user's browser and on the backend server when an authenticated victim visits `https://attacker.com` containing a hidden `<form action="https://api.example.com/api/v1/transfer" method="POST">`. Does the server execute the fund transfer?
3. **Remediation Strategy:** Provide a complete mitigation plan combining cookie attributes, request headers, and token patterns to secure the endpoint against CSRF.
