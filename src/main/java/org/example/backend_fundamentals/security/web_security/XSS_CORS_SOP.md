# Web Security: XSS, SOP, and CORS

## Attack Models
A crucial realization when discussing web security is that different security mechanisms solve entirely different attack classes. A common mistake is treating "Security" as one monolithic wall.
Security models are based on the attacker's capabilities:
1. **Level 1 - Network Attacker (Public WiFi, ISP, Sniffer):** Can see and modify packets in transit. Protected against by **TLS/HTTPS**.
2. **Level 2 - XSS Attacker:** Can execute JavaScript within the victim's browser session. Protected against by **Content Security Policy (CSP), Output Encoding/Sanitization, and HttpOnly cookies**.
3. **Level 3 - Browser Storage Theft:** Attacker obtains the physical files for `localStorage` or `sessionStorage` (e.g., via malware). Tokens stored here are compromised.
4. **Level 4 - Full Device Compromise:** Attacker owns the OS, memory, and browser. **Game Over**. Most web security controls (like CORS or HttpOnly) are completely irrelevant if the attacker controls the OS.

## XSS (Cross Site Scripting)
In XSS, the attacker doesn't inject JavaScript into *their* browser; they trick *your* website into serving their malicious script to a *victim's* browser. 
TLS does not stop XSS because TLS only guarantees the traffic wasn't modified in transit; it does not guarantee the server generated safe content.

**Stored XSS Flow:**
1. Attacker submits a comment containing `<script>stealToken()</script>`.
2. The server stores this in the database.
3. A victim views the comments page.
4. The server returns the HTML, faithfully including the script.
5. The victim's browser executes the script, stealing their tokens or session.

**Reflected XSS Flow:**
1. Attacker crafts a malicious link: `https://example.com/search?q=<script>evil()</script>`
2. Victim clicks the link.
3. The server takes the `q` parameter and echoes it directly into the HTML response: `Results for <script>evil()</script>`.
4. The browser executes the script.

**Prevention:** Never trust user input. Use **Input Validation** and, critically, **Output Encoding** (e.g., converting `<` to `&lt;` before rendering) so the browser treats the payload as text, not executable code.

## HttpOnly Cookies
Because XSS attacks rely on executing JavaScript (like `document.cookie` or `localStorage.token`), the best defense for long-lived credentials (like Refresh Tokens or Session IDs) is to store them in an **HttpOnly Cookie**.
- **JavaScript Cannot Read It:** If an attacker executes XSS, their script cannot extract the token.
- **The Browser Can Still Send It:** When the browser makes a request (like a `POST /refresh`), the browser itself automatically attaches the HttpOnly cookie.

## Same-Origin Policy (SOP)
SOP is a fundamental browser isolation rule: **One origin cannot freely read another origin's data.**
An "Origin" is defined as `Protocol + Domain + Port`. `https://amazon.com` and `https://api.amazon.com` are *different* origins.
Without SOP, if you logged into `bank.com` and then visited `evil.com`, malicious JavaScript on `evil.com` could silently call `fetch("https://bank.com/account")` and read your balance, because your browser would automatically attach your bank session cookies to the request. SOP blocks `evil.com` from reading that response.

## CORS (Cross-Origin Resource Sharing)
CORS is a controlled exception to the Same-Origin Policy.
Imagine your frontend (`app.company.com`) needs to call your backend (`api.company.com`). SOP blocks this by default because they are different origins.
To allow it, the backend responds with a CORS header:
`Access-Control-Allow-Origin: https://app.company.com`
The browser sees this and permits the frontend JavaScript to read the response.

**Massive Gotcha:** CORS does *not* usually prevent the request from being sent. The request reaches the server, the server processes it, and the server returns a response. **What CORS blocks is the browser's JavaScript from reading that response.**
CORS does not protect the server; it protects the *User's Browser Session*.

**Backend-to-Backend Calls:**
If your server calls the Stripe API, CORS is completely irrelevant. CORS is strictly a browser-enforced security mechanism. Server-to-server calls do not use browsers, so they do not care about CORS.

## Quick recall
**Q. Does TLS prevent XSS attacks?**
A. No. TLS secures the transport, ensuring the payload wasn't modified in transit. In XSS, the server *itself* is serving the malicious payload, which TLS faithfully delivers.

**Q. If JavaScript cannot read an HttpOnly cookie, how is the cookie actually used?**
A. The browser itself automatically attaches the HttpOnly cookie to outgoing HTTP requests (e.g., in the `Cookie:` header) matching that domain. JavaScript never touches the value.

**Q. If a request violates CORS, does the request still reach the backend server?**
A. Yes, usually. The request is sent, processed, and the server returns a response. CORS is a browser mechanism that blocks the *client-side JavaScript* from reading that response.

**Q. Is CORS used to secure backend-to-backend API calls?**
A. No. CORS is strictly enforced by web browsers to prevent cross-origin reads. Server-to-server communication bypasses browsers entirely.
