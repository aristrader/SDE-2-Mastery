---
order: 20
search: false
---

# Solutions

## Solution: local-ssl-warning - The Self-Signed Cert

The browser rejects the connection because it **does not trust the identity** of the server, even though the communication channel itself is securely encrypted.

### The Missing Trust Chain
Your local certificate is "self-signed". This means it was not digitally signed by a recognized Certificate Authority (CA) like Let's Encrypt, DigiCert, or GlobalSign. 
Browsers come pre-installed with a "Root Store"—a list of public keys belonging to trusted CAs. When connecting to your production server, Chrome uses the pre-installed Let's Encrypt public key to verify the signature on your server's certificate. For your local server, the signature is meaningless because the signer is unknown.

### How to resolve it locally
1. **Bypass the warning:** Click "Advanced" -> "Proceed to localhost (unsafe)". The browser will establish the TLS tunnel, but mark the connection as insecure in the URL bar.
2. **Add to Root Store:** You can manually import your self-signed certificate into your operating system's or browser's trusted Root Certificate Store. Once imported, the browser will treat your self-signed cert as a valid CA and the red warning will disappear.
