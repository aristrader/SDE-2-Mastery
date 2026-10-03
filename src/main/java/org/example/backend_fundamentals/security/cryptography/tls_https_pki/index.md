---
order: 30
---

# TLS, HTTPS, and Public Key Infrastructure (PKI)

## 1. TLS vs HTTPS vs SSL

- **HTTPS** is HTTP communication carried inside an encrypted Transport Layer Security (TLS) tunnel (typically over TCP port 443).
- **SSL (Secure Sockets Layer)** is the deprecated historical predecessor of TLS. Modern systems run TLS 1.2 or TLS 1.3. While "SSL Certificate" remains common colloquial phrasing, the underlying standard is TLS.
- **Core Guarantees:** TLS provides **Encryption** (confidentiality against eavesdroppers), **Server Authentication** (verifying peer identity), and **Data Integrity** (detecting tampering via cryptographic MACs/AEAD).

| Concept | Scope / Responsibility | What it does NOT protect against |
| :--- | :--- | :--- |
| **TLS / HTTPS** | Secures the transport pipeline between client and server. | Application-layer vulnerabilities like XSS, SQL injection, CSRF, or malicious server payloads. |
| **Application Logic** | Sanitizes user input, authorizes requests, and renders safe responses. | Network sniffing and in-transit packet tampering across public networks. |

> TLS acts as a secure pipe. If the server application generates an XSS payload or serves vulnerable content, TLS will faithfully and securely deliver that malicious output directly to the client browser.

---

## 2. Hybrid Cryptography: Symmetric vs Asymmetric

TLS uses **hybrid cryptography** to balance security and server throughput:

| Cryptographic System | Key Model | Performance | TLS Usage |
| :--- | :--- | :--- | :--- |
| **Asymmetric (Public-Key)** | Key pair: Public Key (encrypt/verify) + Private Key (decrypt/sign). Examples: RSA, ECDSA, Ed25519. | Computationally intensive; high CPU overhead for bulk data. | Used during the **handshake** for server authentication (digital signatures) and key exchange (ECDHE). |
| **Symmetric** | Single shared secret for both encryption and decryption. Examples: AES-GCM, ChaCha20-Poly1305. | Hardware-accelerated (AES-NI), high throughput, low latency. | Used for **bulk application data** transmission once the session key is derived. |

### Why TLS Uses Both
Running asymmetric encryption over every HTTP payload would exhaust server CPU capacity. Instead, TLS uses asymmetric cryptography and Diffie-Hellman during the initial handshake to authenticate identity and establish a shared secret. Once established, both peers derive symmetric encryption keys (AEAD) to encrypt application traffic at gigabit speeds.

---

## 3. The TLS 1.3 Handshake

Modern TLS 1.3 ([RFC 8446](https://datatracker.ietf.org/doc/html/rfc8446)) optimizes connection establishment to a **1-RTT (Round Trip Time)** exchange and removes legacy, insecure cryptographic primitives.

```
Client                                                  Server
  |                                                       |
  | -------- 1. ClientHello (ECDHE Share, Ciphers, SNI) -> |
  |                                                       |
  | <-- 2. ServerHello (ECDHE Share, Selected Cipher) --- |
  | <-- EncryptedExtensions (ALPN, etc.) ---------------- |
  | <-- Certificate (Chain to Root CA) ------------------ |
  | <-- CertificateVerify (Signature over Transcript) --- |
  | <-- Finished (HMAC over Entire Handshake) ----------- |
  |                                                       |
  | (Client verifies cert chain, SAN, signature & HMAC)   |
  |                                                       |
  | -------- 3. Finished (HMAC over Handshake) ---------> |
  | <======= 4. Encrypted Application Data (HTTP/1.1, HTTP/2) ===> |
```

### Handshake Sequence (Step-by-Step)
1. **ClientHello:** The client sends supported TLS versions, supported AEAD cipher suites, the target hostname via **Server Name Indication (SNI)**, and its **Key Share** (the client's ephemeral ECDHE public share $g^x$).
2. **ServerHello:** The server selects the cipher suite, responds with its own **Key Share** (ephemeral ECDHE public share $g^y$). At this exact point, both sides combine keys to compute the shared master secret. All subsequent handshake messages from the server are encrypted.
3. **Encrypted Handshake Extensions:**
   - `EncryptedExtensions`: Protocol negotiation parameters (e.g., ALPN for HTTP/2 or HTTP/3).
   - `Certificate`: The server's X.509 public certificate chain (Leaf cert -> Intermediate CAs).
   - `CertificateVerify`: The server creates a digital signature using its private key over a TLS-defined context string concatenated with the cryptographic hash of all preceding handshake messages.
   - `Finished`: A Message Authentication Code (HMAC) over the complete handshake transcript, proving key derivation consistency and integrity.
4. **Client Verification & Finished:** The client validates the certificate chain and hostname, verifies `CertificateVerify` using the certificate's public key, checks the `Finished` MAC, and sends its own `Finished` message. Application data flows immediately. HTTP/3 uses TLS 1.3 over QUIC rather than this TCP connection shape.

### Key Changes in TLS 1.3 vs Legacy TLS 1.2
- **No RSA Key Transport:** In TLS 1.2, a client could encrypt a premaster secret with the server's static RSA public key. TLS 1.3 completely removes RSA key exchange to guarantee Ephemeral Diffie-Hellman (ECDHE) key derivation and forward secrecy.
- **Mandatory Forward Secrecy:** Only Diffie-Hellman key exchange algorithms (DHE/ECDHE) are supported.
- **Reduced Latency:** Reduced standard full handshake from 2-RTT to 1-RTT by combining the key share proposal directly in `ClientHello`.

---

## 4. Public Key Infrastructure (PKI) & Certificate Validation

PKI is the trust infrastructure (X.509 Certificates, Certificate Authorities, Trust Stores) that binds a public key to a verified domain identity.

```
+-------------------------------------------------------------+
|                     Root CA (e.g. DigiCert)                 |
|             (Self-signed, pre-installed in OS / Browser)     |
+-------------------------------------------------------------+
                              |
                     Signs with Private Key
                              v
+-------------------------------------------------------------+
|                    Intermediate CA (Issuer)                 |
|               (Delegated signing authority)                 |
+-------------------------------------------------------------+
                              |
                     Signs with Private Key
                              v
+-------------------------------------------------------------+
|                 Server Leaf Certificate (amazon.com)        |
|  - Domain / SAN: amazon.com, *.amazon.com                   |
|  - Server Public Key (RSA / ECDSA)                          |
|  - Validity Window (Not Before / Not After)                 |
|  - Signature: Signed by Intermediate CA Private Key         |
+-------------------------------------------------------------+
```

### Client Validation Checklist
When a browser or HTTP client connects to `https://amazon.com`, it performs strict sequential checks:
1. **Trust Chain Path Building:** Validates that the Leaf Certificate was signed by an Intermediate CA, which was signed by a trusted Root CA found in the client's pre-installed OS/browser trust store (e.g., Apple Keychain, Windows Root Store, Mozilla NSS).
2. **Subject Alternative Name (SAN) Match:** Verifies that the requested domain name matches an entry in the certificate's `Subject Alternative Name` (SAN) extension (legacy `Common Name (CN)` is deprecated).
3. **Validity Period:** Confirms that the current system timestamp falls between the certificate's `Not Before` and `Not After` dates.
4. **Revocation Status:** May check whether the certificate was revoked before expiration via **OCSP (Online Certificate Status Protocol)** or CRL (Certificate Revocation List), depending on client and platform policy. Production services can use **OCSP Stapling** to attach a time-stamped CA response to the TLS handshake, avoiding client-side OCSP DNS lookups.
5. **Cryptographic Proof of Ownership:** The client verifies the `CertificateVerify` signature using the public key extracted from the certificate. A copied certificate without the corresponding private key will fail this check.

---

## 5. Connections, Sessions, and Resumption

| Concept | Definition | Lifecycle | Failure & Security Trade-offs |
| :--- | :--- | :--- | :--- |
| **TCP Connection** | The underlying L4 network transport socket. | Milliseconds to hours. Dropped on network disconnect or timeout. | Dropping the connection requires a new TCP 3-way handshake. |
| **TLS Session (Resumption)** | Cached cryptographic parameters (session tickets / Pre-Shared Keys - PSK). | Configured TTL (e.g., 24 hours). Persists across multiple TCP reconnects. | Usually avoids certificate transmission; a server can combine PSK resumption with fresh ECDHE to retain forward secrecy. |
| **0-RTT Early Data** | Client transmits encrypted application data in the very first `ClientHello` flight using a resumed PSK. | Valid during session resumption windows. | **Replay Attacks:** 0-RTT data can be intercepted and replayed by network adversaries. **Forward Secrecy:** 0-RTT lacks 1-RTT forward secrecy. Must only be used for idempotent requests (e.g., safe `GET` queries). |

---

## 6. TLS Termination Architecture

In backend production systems, TLS is rarely terminated directly on every individual microservice container.

```
Internet (Public)            Private Cloud / VPC
+---------+  HTTPS (Port 443)  +------------------+  HTTP / mTLS  +---------------------+
| Client  | -----------------> | Load Balancer /  | ------------> | Microservice Pods / |
| Browser |                    | Reverse Proxy    |               | App Containers      |
+---------+                    +------------------+               +---------------------+
                                (Terminates TLS,
                                 Manages Certs)
```

- **Edge / Reverse Proxy Termination:** Cloud Load Balancers (AWS ALB, Cloudflare, NGINX) terminate public TLS, offloading crypto computation and certificate management.
- **Zero-Trust Internal Traffic:** Inside the private VPC, internal services either communicate over private HTTP networks or utilize **mTLS (Mutual TLS)** via a Service Mesh (e.g., Istio, Linkerd) where both client and server authenticate each other using internal private CAs.

---

## 7. Common Failures and Operational Recovery

| Error / Failure Mode | Root Cause | Engineering Resolution |
| :--- | :--- | :--- |
| `NET::ERR_CERT_AUTHORITY_INVALID` | Self-signed certificate or missing intermediate certificate bundle in the server config. | Install signed cert from trusted CA, configure full certificate chain (`fullchain.pem`), or import custom internal root into client trust store. |
| `NET::ERR_CERT_COMMON_NAME_INVALID` | Request hostname does not match the SAN entries on the certificate. | Reissue certificate with correct domain and wildcard SAN entries (e.g., `api.example.com`). |
| `NET::ERR_CERT_DATE_INVALID` | Certificate has passed its expiration date or client clock is skewed. | Automate certificate renewal via ACME / Let's Encrypt / AWS Certificate Manager (ACM). Check client NTP synchronization. |
| Handshake Timeout / Cipher Mismatch | Incompatible TLS versions or cipher suites between client and legacy server. | Update server cipher suite configuration and support modern TLS 1.2 / TLS 1.3 standards. |

---

## 8. Authoritative References & Standards

- [RFC 8446: The Transport Layer Security (TLS) Protocol Version 1.3](https://datatracker.ietf.org/doc/html/rfc8446)
- [MDN Web Docs: Transport Layer Security (TLS)](https://developer.mozilla.org/en-US/docs/Web/Security/Transport_Layer_Security)
- [OWASP Transport Layer Protection Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Transport_Layer_Protection_Cheat_Sheet.html)
- [Cloudflare: What happens in a TLS handshake?](https://www.cloudflare.com/learning/ssl/what-happens-in-a-tls-handshake/)

---

## Quick recall

**Q. Does TLS protect a website from XSS?**
A. No. TLS secures the transport. If the website's database contains an XSS payload, the server will intentionally send it, and TLS will faithfully deliver it to the browser over a secure pipe.

**Q. Why does TLS use both Asymmetric and Symmetric cryptography?**
A. Asymmetric is too slow for bulk data but solves the key exchange problem. TLS uses Asymmetric during the handshake to establish trust and generate a shared session key, then switches to fast Symmetric (AES) for the actual data transfer.

**Q. What prevents an attacker from simply copying a bank's public TLS certificate to spoof their website?**
A. The attacker doesn't have the bank's Private Key. The TLS handshake requires the server to cryptographically prove it holds the private key matching the public key in the certificate via the `CertificateVerify` signature.

**Q. What is TLS Termination?**
A. The practice of decrypting the TLS traffic at the Load Balancer or API Gateway. The traffic is then usually forwarded to backend application servers over plaintext HTTP (or re-encrypted internally).

**Q. Why did TLS 1.3 eliminate RSA key transport?**
A. RSA key transport lacks Forward Secrecy. If a server's private key were compromised in the future, all historically recorded encrypted traffic could be decrypted. TLS 1.3 mandates ephemeral Diffie-Hellman (ECDHE).

**Q. What are the security risks of TLS 1.3 0-RTT Early Data?**
A. 0-RTT data is susceptible to replay attacks by network eavesdroppers and does not provide forward secrecy. It must be restricted to idempotent, non-state-mutating HTTP requests.
