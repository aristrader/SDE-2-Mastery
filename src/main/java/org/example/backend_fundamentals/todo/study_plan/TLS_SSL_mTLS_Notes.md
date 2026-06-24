# System Design Notes: SSL, TLS, HTTPS, Certificates, mTLS

## Context
From a big-picture system design perspective, this topic is not one of the most important areas, but useful background knowledge.
For SDE2 backend interviews, deep cryptography knowledge is usually unnecessary.
The important goal is understanding:
- Why HTTPS exists
- What TLS does
- What certificates are
- What mTLS is
- Why mTLS is common in microservices

## Security as a Non-Functional Requirement
Whenever data moves across a network, an important question is: How do we prevent somebody from reading or modifying the data? TLS is one of the major answers.

## The Problem Before SSL/TLS
Without encryption, traffic travels in plaintext. Anyone intercepting traffic can read it, leading to eavesdropping and Man-in-the-Middle (MITM) attacks.

## SSL (Secure Sockets Layer) vs TLS (Transport Layer Security)
- **SSL**: Developed first (SSL 1.0, 2.0, 3.0). Now obsolete due to security vulnerabilities.
- **TLS**: Modern replacement for SSL.
- People still say "SSL Certificate" because the terminology survived even though the protocol did not. Most "SSL certificates" sold today are actually TLS certificates.
- HTTPS = HTTP over TLS. The "S" in HTTPS refers to secure communication provided by TLS.

## Three Main Things TLS Provides (Crucial for Interviews)
1. **Encryption**: Hide transmitted data from third parties. Attackers cannot understand the encrypted contents.
2. **Authentication**: Verify identity. Prevents impersonation attacks by ensuring the server is who it claims to be (using certificates).
3. **Integrity**: Detect tampering. Receiver knows if the message was changed, and rejects the connection/request.

## High-Level TLS Handshake
*(Packet-level details not needed for SDE2 interviews)*
1. **Client Connects**: Client says "Hello" to the server.
2. **Server Sends Certificate**: Certificate contains domain name, public key, and Certificate Authority signature.
3. **Browser Verifies Certificate**: Checks expiration, domain match, and CA trust. If invalid, shows warning ("Your connection is not private").
4. **Session Key Establishment**: Client and server derive/agree upon a shared session key.
5. **Encrypted Communication**: All future traffic uses the session key (symmetric encryption is faster than public-key operations).

## Certificates and Certificate Authority (CA)
- **Certificate**: Like a passport. Proves server identity ("I am google.com").
- **Certificate Authority (CA)**: Trusted authority (e.g., DigiCert, Let's Encrypt) that verifies domain ownership and issues signed certificates.
- **Trust chain**: Browser trusts CA -> CA trusts Server -> Browser trusts Server.

## HTTPS Interview Explanation
HTTPS uses TLS. During connection setup, the server sends a certificate containing its public key. The client verifies it via a trusted CA. A secure session key is established, and future communication is encrypted. TLS provides Encryption, Authentication, and Integrity.

## Mutual TLS (mTLS)
- **Normal TLS**: Client verifies server.
- **mTLS**: Client verifies server AND Server verifies client. Both sides authenticate using certificates.

### Why Use mTLS?
Ensure trust in both directions. Only approved participants can communicate.

### mTLS in Microservices & Zero Trust
- **Service-to-Service Auth**: Order Service calling Payment Service: Payment Service verifies Order Service's certificate before accepting requests. Only trusted services can connect.
- **Zero Trust Architecture**: "Trust nobody automatically. Verify everything." mTLS supports this because every service must prove its identity.
- **Service Mesh**: mTLS is heavily used in service meshes (Istio, Linkerd). Sidecar proxies handle mTLS transparently, so developers don't manually implement it.

## SDE2-Level Scope Guidance
Remember:
- **HTTP -> HTTPS** = HTTP + TLS
- **TLS provides**: Encryption, Authentication, Integrity.
- **Normal TLS**: Client verifies server.
- **mTLS**: Client verifies server, server verifies client (common in microservices and zero-trust).
- **Topics explicitly marked as optional/advanced**: RSA, ECC, Diffie-Hellman, Cipher suites, Certificate chains, OCSP.
