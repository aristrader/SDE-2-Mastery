---
order: 20
search: false
---

# Solution

## Solution: xss-threat-boundaries - Defense-in-Depth and HttpOnly Boundaries

### 1. Threat Model Evaluation
- **Proposal A Critique (`localStorage` + `'unsafe-inline'` CSP):**
  - Storing session tokens in `localStorage` exposes them directly to JavaScript execution via `window.localStorage`. Any XSS flaw allows immediate token extraction and off-site exfiltration.
  - Using `'unsafe-inline'` in CSP removes script execution origin constraints, rendering CSP ineffective against inline `<script>` or attribute-based payload injection.
- **Proposal B Critique (`HttpOnly` Cookie Immunity Myth):**
  - `HttpOnly` cookies cannot be accessed via `document.cookie`, preventing direct token exfiltration.
  - However, `HttpOnly` does **not** stop an attacker from executing authenticated actions. An injected script runs within the authenticated user's session context; any `fetch()` or `XMLHttpRequest` it initiates will automatically include the browser's ambient `HttpOnly` cookies.
  - Attackers can also modify DOM content, capture user keystrokes (in-page phishing), or exfiltrate sensitive data displayed in the application UI.

### 2. Encoding vs Sanitization
- **Flaws in Proposal C (`<` and `>` character replacement):**
  - Simple bracket replacement is ineffective for attributes (e.g., `<a href="javascript:alert(1)">`), event handlers (e.g., `<img src=x onerror=alert(1)>`), or CSS contexts.
  - In rich HTML contexts, replacing brackets breaks legitimate HTML formatting entirely.
- **Context-Aware Output Encoding:**
  - The primary defense for untrusted data rendered as plain text. It encodes characters based on destination context (HTML Body, HTML Attribute, JavaScript Variable, URL Query Parameter) so the browser treats the input strictly as data rather than executable markup.
- **HTML Sanitization:**
  - Reserved strictly for scenarios where users are deliberately permitted to author raw HTML/Markdown. A sanitizer parses the markup into an AST and strips dangerous elements (`<script>`, `<iframe>`, `<object>`) and attributes (`onerror`, `href="javascript:..."`), preserving only an explicit safelist of structural tags (`<b>`, `<i>`, `<p>`).

### 3. Defense-in-Depth Architecture
- **Credential Storage:** Store session identifiers in `HttpOnly`, `Secure`, and `SameSite=Lax` or `Strict` cookies.
- **Content Rendering:** Use context-aware output encoding by default; apply robust HTML sanitization libraries (e.g., OWASP Java HTML Sanitizer, DOMPurify) only where rich markup is explicitly supported.
- **CSP Layer:** Implement a strict Content Security Policy header (e.g., `script-src 'nonce-...'`, `object-src 'none'`, `base-uri 'none'`, `frame-ancestors 'none'`) to restrict script execution and mitigate impact if an injection flaw occurs.

---

## Solution: cors-preflight-credentials - CORS Preflight and Credentialed Origin Configuration

### 1. Preflight Diagnosis
The request triggers a CORS preflight (`OPTIONS`) request because it does not qualify as a simple request under the Fetch specification:
- The `Content-Type: application/json` is not one of the three simple MIME types (`application/x-www-form-urlencoded`, `multipart/form-data`, `text/plain`).
- The request includes a custom non-safelisted header: `X-Client-Version`.

### 2. Header Specification

#### Preflight Exchange
```http
OPTIONS /v1/orders HTTP/1.1
Host: api.example.com
Origin: https://dashboard.example.com
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type, x-client-version

HTTP/1.1 204 No Content
Access-Control-Allow-Origin: https://dashboard.example.com
Access-Control-Allow-Methods: POST, OPTIONS
Access-Control-Allow-Headers: Content-Type, X-Client-Version
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 86400
```

#### Actual Request & Response
```http
POST /v1/orders HTTP/1.1
Host: api.example.com
Origin: https://dashboard.example.com
Content-Type: application/json
X-Client-Version: 2.4.0
Cookie: session_id=s%3A98f12...

HTTP/1.1 200 OK
Content-Type: application/json
Access-Control-Allow-Origin: https://dashboard.example.com
Access-Control-Allow-Credentials: true

{"orderId": "ord-1029", "status": "CREATED"}
```

### 3. Security Invariant
- When `credentials: 'include'` is set, the browser requires `Access-Control-Allow-Credentials: true` and strictly forbids `Access-Control-Allow-Origin: *`.
- If wildcard credentialed requests were allowed, any malicious third-party site on the internet could initiate cross-origin requests with ambient user credentials and read sensitive authenticated responses.
- Browsers enforce this invariant to ensure the backend explicitly names and trusts each specific origin before granting it access to credentialed response data.

---

## Solution: csrf-cors-sop-diagnosis - SOP, CORS, and CSRF Architecture Boundaries

### 1. Flaw Analysis
- CORS is a browser mechanism that selectively **relaxes** the Same-Origin Policy (SOP) to allow cross-origin script reads; it does **not** restrict standard cross-origin writes or requests.
- CORS does not prevent cross-origin `POST` submissions initiated via standard HTML `<form>` elements. The browser transmits the request and ambient session cookies directly to the target server regardless of CORS settings. CORS headers only control whether the client script can inspect the response.

### 2. Execution Flow Trace
1. An authenticated victim visits `https://attacker.com` while maintaining an active session cookie for `https://api.example.com`.
2. `attacker.com` submits a hidden standard HTML form pointing to `https://api.example.com/api/v1/transfer`.
3. The browser attaches the ambient `Cookie: session_id=...` header and sends the `POST` request to `api.example.com`.
4. The backend server authenticates the request via the valid session cookie and executes the fund transfer.
5. Even though the browser blocks `attacker.com` from reading the HTTP response due to SOP/CORS, the state change on the backend has already succeeded.

### 3. Remediation Strategy
- **Cookie SameSite Attributes:** Configure session cookies with `SameSite=Lax` or `SameSite=Strict` and `Secure` to prevent the browser from attaching cookies to cross-site top-level POST submissions or cross-site embedded requests.
- **Anti-CSRF Tokens:** Implement the Synchronizer Token Pattern. The server generates a cryptographically random, unpredictable token bound to the user session, embeds it in forms or headers, and rejects state-changing requests lacking a valid token.
- **Origin & Referer Validation:** Verify the `Origin` and `Referer` headers on state-changing requests at the gateway/filter level to ensure the request originated from trusted application domains.
- **Enforce Custom Headers or JSON Payloads:** Require JSON content-type or custom request headers (e.g., `X-CSRF-Token`, `X-Requested-With`) on state-changing API endpoints, which cannot be generated by simple HTML forms and force a CORS preflight check.
