---
order: 10
search: false
---

# Practice

## Exercise: local-ssl-warning - The Self-Signed Cert

### Goal
Understand the role of Certificate Authorities (CAs), trust stores, and validation constraints in the Public Key Infrastructure (PKI) model.

### Task
You are developing a backend API locally on `https://localhost:8080`. To support HTTPS during local testing, you generate a self-signed certificate using OpenSSL and configure your Spring Boot server with the resulting keystore.

When navigating to `https://localhost:8080` in Google Chrome or testing with standard HTTP clients, the request fails with a security warning screen:
`NET::ERR_CERT_AUTHORITY_INVALID`.

### Checks
1. The symmetric encryption (TLS 1.3 tunnel) is mathematically functional. Why does the browser or HTTP client hard-fail and reject the connection?
2. How is this self-signed certificate fundamentally different in the PKI trust chain from a production certificate issued by Let's Encrypt or DigiCert?
3. Name two concrete, industry-standard ways a backend developer can resolve this warning during local development without disabling security in production.
4. If an engineer encounters `NET::ERR_CERT_COMMON_NAME_INVALID` after generating a custom certificate for `localhost`, what specific X.509 extension was omitted or misconfigured?
