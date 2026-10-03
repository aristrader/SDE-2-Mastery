---
order: 20
search: false
---

# Solutions

## Solution: local-ssl-warning - The Self-Signed Cert

The client rejects the connection because it **cannot cryptographically verify server identity through a trusted root authority**, even though the underlying cryptographic key exchange and encryption algorithms function properly.

---

### 1. Root Cause: The Broken Trust Chain
In PKI, encryption without authentication is vulnerable to active Man-in-the-Middle (MITM) attacks.

A client verifies a certificate by building a cryptographic chain of trust from the Leaf Certificate to an Intermediate CA and ultimately to a Root Certificate stored in the operating system or browser's **Trust Store** (e.g., Apple Keychain, Windows Root Store, Java `cacerts`).

Because the local certificate is **self-signed**, its signature issuer does not exist in the client's pre-installed Root Store. As a result, trust evaluation fails immediately with `NET::ERR_CERT_AUTHORITY_INVALID`.

---

### 2. Difference Between Self-Signed vs Public CA Certificates

| Property | Self-Signed Local Certificate | Public CA Certificate (e.g. Let's Encrypt) |
| :--- | :--- | :--- |
| **Issuer** | Signed by the server's own private key. | Signed by an intermediate CA chained to a trusted root authority. |
| **Trust Store Presence** | Missing from standard OS / browser trust roots. | Pre-installed globally across all standard clients and devices. |
| **Domain Verification** | None; anyone can create a self-signed cert claiming any domain. | Strict Automated Domain Validation (ACME DNS/HTTP-01 challenge). |
| **Revocation Support** | No CRL / OCSP infrastructure available. | Supported via OCSP / CRL distribution endpoints. |

---

### 3. Engineering Solutions for Local Development

1. **Local Root CA Generation (e.g., `mkcert`):**
   - Create a dedicated local development Certificate Authority (CA) using tools like `mkcert`.
   - Install the local CA root certificate into the local machine's and Java's trust store.
   - Issue certificates for `localhost` and `127.0.0.1` signed by this local CA. The browser and HTTP clients will trust it seamlessly without manual browser exceptions.
2. **Explicit Trust Store Import:**
   - Manually export the self-signed public certificate (`.crt` / `.cer`) and import it into the OS/browser trust store (or Java `keytool -importcert -keystore $JAVA_HOME/lib/security/cacerts`) as a trusted root.
3. **Development Exception / Bypass:**
   - For rapid manual testing in a browser, proceed past the warning ("Advanced -> Proceed to localhost (unsafe)"). In programmatic HTTP clients (e.g. `curl -k` or testing frameworks), bypass certificate verification strictly in isolated local test suites (never in production configuration).

---

### 4. Resolving Hostname Mismatches (`ERR_CERT_COMMON_NAME_INVALID`)
Modern TLS clients require the target hostname (`localhost`, `127.0.0.1`, or `*.dev.local`) to be explicitly defined in the X.509 **Subject Alternative Name (SAN)** extension (`subjectAltName = DNS:localhost, IP:127.0.0.1`). Legacy clients relied on the `Common Name (CN)` attribute, which modern browsers and TLS 1.3 clients ignore or reject if SAN is omitted.
