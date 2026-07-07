---
order: 10
search: false
---

# Practice

## Exercise: local-ssl-warning - The Self-Signed Cert

### Goal
Understand the role of Certificate Authorities (CAs) in the PKI trust chain.

### Task
You are developing a backend API locally on `https://localhost:8080`. To support HTTPS, you generate an SSL certificate yourself using OpenSSL and configure your Spring Boot server to use it.
When you open `https://localhost:8080` in Chrome, you are greeted with a massive red warning screen: "Your connection is not private. NET::ERR_CERT_AUTHORITY_INVALID".

### Checks
- The encryption (TLS tunnel) works perfectly fine. Why is the browser still rejecting the connection?
- How is this certificate fundamentally different from the one running on your production server (e.g., signed by Let's Encrypt)?
- Name two ways a developer can resolve this warning on their local machine.
