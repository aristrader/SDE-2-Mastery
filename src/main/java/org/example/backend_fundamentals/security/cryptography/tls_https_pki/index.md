---
order: 30
---

# TLS, HTTPS, and Public Key Infrastructure (PKI)

## TLS vs HTTPS vs SSL
- **HTTPS** is simply HTTP over a TLS tunnel.
- **SSL (Secure Sockets Layer)** is the deprecated predecessor to TLS. Although people still say "SSL Certificate", the protocol actually used under the hood is TLS (Transport Layer Security).

TLS provides Encryption, Server Authentication, and Data Integrity. It acts as a secure pipe. 
**Gotcha:** TLS does *not* protect against XSS, SQL Injection, or CSRF. If the server application generates malicious output (XSS), TLS will faithfully and securely deliver that malicious output to the browser.

## Symmetric vs Asymmetric Cryptography
- **Symmetric:** The same key is used to encrypt and decrypt (e.g. AES, ChaCha20). It is extremely fast, but suffers from the *Key Distribution Problem* (how do you safely share the key?).
- **Asymmetric:** Uses a Key Pair (Public Key + Private Key). Data encrypted with the Public Key can only be decrypted by the Private Key. It solves the distribution problem but is computationally very slow.

**Why TLS uses both:**
Using Asymmetric Crypto (RSA/ECC) for all traffic would melt the server's CPU. Instead, TLS uses Asymmetric Crypto during the *Handshake* to establish trust and securely generate a Shared Secret. Once the Handshake is complete, it switches to Symmetric Crypto (AES) to encrypt the actual data transfer. 

### Diffie-Hellman Key Exchange
The mechanism used to generate that shared secret is typically Diffie-Hellman (or ECDHE in modern TLS). It allows the browser and server to jointly derive a shared secret over a public, monitored channel without ever transmitting the secret itself. An attacker eavesdropping on the handshake sees the public inputs but mathematically cannot derive the final secret.

## The TLS Handshake & Sessions
A common misconception is that after the TLS handshake exchanges keys, the connection closes and the application just encrypts payloads using that key. 
In reality, the **TLS Tunnel** is the mechanism using the key. Every subsequent HTTP request (which contains sensitive cookies, JWTs, etc.) is sent *through* this open tunnel.

A **TLS Session** exists while the connection exists. It may last seconds, minutes, or hours. If the connection times out and drops, a new Handshake is required to create a new session key.
At massive scale (e.g. Google), maintaining millions of open TLS sessions is normal. The expensive part is the initial Handshake; the ongoing AES encryption is incredibly fast and hardware-accelerated.

**TLS Termination:** In modern architectures, TLS is usually terminated at the Edge / Load Balancer. The traffic behind the Load Balancer to the actual backend application servers is often plaintext HTTP or re-encrypted with internal certs (mTLS).

## Public Key Infrastructure (PKI) & Browser Trust
**PKI is not an encryption algorithm.** It is the trust infrastructure (Certificates, Certificate Authorities, Trust Chains) that allows Asymmetric Crypto to actually verify identity.

When a browser visits `https://amazon.com`, how does it know the server isn't an attacker?
1. The server returns a **Certificate**. This contains the Domain Name, the server's Public Key, and a **Signature from a Certificate Authority (CA)** (like Let's Encrypt or DigiCert).
2. The browser comes pre-installed with the Public Keys of trusted Root CAs.
3. The browser uses the CA's public key to cryptographically verify the signature on the certificate.

**Could an attacker just copy Amazon's certificate?**
Yes, but they do not have Amazon's *Private Key*. A copied certificate is useless if you cannot prove you own the private key associated with the public key inside the cert. The handshake will fail.

**Could an attacker forge a certificate for Amazon?**
No. They would need a trusted CA to sign it. CAs enforce **Domain Ownership Verification** (e.g. asking the applicant to put a specific file at `amazon.com/.well-known/...` or add a DNS TXT record). The attacker does not control the domain, so the CA will refuse to sign the forged certificate.

## Quick recall
**Q. Does TLS protect a website from XSS?**
A. No. TLS secures the transport. If the website's database contains an XSS payload, the server will intentionally send it, and TLS will faithfully deliver it to the browser over a secure pipe.

**Q. Why does TLS use both Asymmetric and Symmetric cryptography?**
A. Asymmetric is too slow for bulk data but solves the key exchange problem. TLS uses Asymmetric during the handshake to establish trust and generate a shared session key, then switches to fast Symmetric (AES) for the actual data transfer.

**Q. What prevents an attacker from simply copying a bank's public TLS certificate to spoof their website?**
A. The attacker doesn't have the bank's Private Key. The TLS handshake requires the server to cryptographically prove it holds the private key matching the public key in the certificate.

**Q. What is TLS Termination?**
A. The practice of decrypting the TLS traffic at the Load Balancer or API Gateway. The traffic is then usually forwarded to backend application servers over plaintext HTTP (or re-encrypted internally).
