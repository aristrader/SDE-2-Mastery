# Proxies, DNS Blocking, VPNs, and National Firewalls

## How it works

### Proxy fundamentals

A proxy is a middleman that receives a request and forwards it elsewhere:

```text
Client ─► Proxy ─► Server
```

The key questions: **who is the proxy representing, and who is being hidden?** That gives the two major types.

### Forward proxy — hides clients

Represents the **client**; the destination server sees the proxy, not the original client.

```text
Employee laptop ─► Corporate proxy ─► Google
```

Typical uses: corporate internet filtering, website access restrictions, privacy, VPN-like behavior.

### Reverse proxy — hides servers

Represents the **server**; users communicate with the reverse proxy and never directly reach backend servers.

```text
User ─► Reverse proxy ─► [Server A | B | C | D]
```

### Forward Proxy vs Reverse Proxy

| Question | Forward Proxy | Reverse Proxy |
|-----------|-----------|-----------|
| Represents | Client | Server |
| Hides | Client | Server |
| Used by | User organization | Website owner |
| Main purpose | Privacy, filtering, geo access | Security, caching, load balancing |
| Example | VPN, corporate proxy | Nginx, HAProxy, CDN edge nodes |

### Why reverse proxies exist

- **Security** — backend servers stay private, not directly reachable from the internet.
- **Load balancing** — distribute requests across servers (req1 → A, req2 → B, ...).
- **Caching** — frequently requested content served from the proxy, reducing backend load.
- **SSL/TLS termination** — the proxy handles HTTPS and certificates; backends don't perform TLS processing themselves.

### Reverse proxy vs load balancer vs CDN

Many modern **load balancers are effectively reverse proxies** — they receive the request and forward it to a backend, which is reverse-proxy behavior. **CDN edge servers also behave like reverse proxies**: receive request → check cache → forward misses to origin.

### Gateway vs reverse proxy

A reverse proxy is the **narrower** concept — its primary job is hiding backend servers and controlling traffic to them. A gateway is **broader**: it may add API management, authentication, policy enforcement, protocol translation.

```text
Reverse proxy ⊂ Gateway
```

A reverse proxy can be viewed as a specialized type of gateway.

## DNS resolution and blocking

The browser can't communicate using a domain name — DNS translates `google.com` → `142.x.x.x`. Most users automatically use their ISP's DNS resolver (laptop → Airtel DNS → returns IP). (Full resolution flow: `networking/dns/DnsResolution.md`.)

### DNS blocking

For a blocked site, the ISP's resolver responds with **"not found," "blocked," or a fake IP** — the user never gets the correct IP, so the connection never starts.

### Why changing DNS sometimes works

Pointing the laptop at an external resolver (e.g. Google DNS) may return the real IP, bypassing the block — but **only when blocking exists solely at the DNS layer**.

### IP blocking

The ISP/government can drop packets destined for specific IPs. Even with correct DNS (`badsite.com → 123.45.67.89`), packets to that IP are dropped — changing DNS no longer helps.

## VPN internals

### Without VPN

```text
You ─► ISP ─► Website
```

The ISP sees `destination IP = website` and can apply blocking.

### With VPN

```text
You ─► encrypted tunnel ─► VPN server ─► Website
```

The ISP sees only `destination IP = VPN server`; the final destination is hidden inside the encrypted tunnel.

- **Bypasses DNS blocking:** DNS queries travel *inside* the tunnel to the VPN's DNS — the ISP cannot inspect or modify them.
- **Bypasses IP blocking:** the ISP only sees the VPN server's IP; the VPN server connects to the blocked site *outside* the ISP's network.

**Obfuscated VPNs:** To bypass Deep Packet Inspection (DPI), VPNs disguise their traffic as normal HTTPS traffic (e.g., "Looks like somebody opening Gmail" instead of "Looks like a VPN"). This creates a constant cat-and-mouse game between firewalls and VPN providers.

## National firewall architecture

### Traditional ISP-based blocking

Each ISP (Airtel/Jio/BSNL) independently implements blocking rules between users and the internet.

### National firewall model

```text
Users → ISPs → national filtering infrastructure → global internet
```

Most international traffic passes through centralized filtering — one highly controlled national checkpoint instead of many separate ones.

### Capabilities

| Capability | What it does |
|-----------|--------------|
| DNS filtering | Return fake or invalid DNS responses |
| IP blocking | Drop traffic to specific destinations |
| Deep packet inspection (DPI) | Inspect packet metadata and protocol characteristics — can detect VPN traffic, certain applications, suspicious patterns |
| TLS/SNI filtering | Inspect the Server Name Indication (SNI) in the unencrypted TLS handshake to see the requested domain (e.g., `facebook.com`) and terminate the connection |
| VPN detection | Identify and block common VPN protocols (OpenVPN, WireGuard) |
| Active probing | The firewall itself connects to a suspected VPN server to test whether it runs VPN software; if confirmed, it's blocked |

*Note: Because of such strict restrictions, services commonly used globally (Google, YouTube, Facebook) are often unavailable, leading to local alternatives developing (e.g., China's internet ecosystem).*

## Gotchas / Trick questions

1. **"Reverse proxy and gateway are basically the same."** Not exactly — reverse proxy is narrower (protect/abstract backends); gateway is broader (API management, auth, policy, protocol translation).
2. **"The primary purpose of a reverse proxy is load balancing."** Not quite — the core purpose is acting as the intermediary in front of backend servers; load balancing is one capability built on that position.
3. **"Changing DNS always bypasses website blocking."** False — it only defeats DNS-level blocking. With IP blocking, a DNS change is ineffective.
4. **"DNS is a global system — how can my ISP block it?"** Your device typically queries the *ISP's* resolver, which the ISP controls. The block happens at the resolver level.
5. **"A VPN magically makes blocked websites accessible."** The VPN doesn't remove the block — the ISP sees only the VPN, and the VPN sees the website. The ISP never sees the final destination.
6. **"If VPN traffic is encrypted, it can never be blocked."** False — VPNs themselves are detected and blocked via known VPN IP lists, DPI, and active probing.
7. **"Servers behind a reverse proxy are hidden because they're backups."** No — they may be primary, active, load-balanced servers. They're hidden because the proxy sits in front of them, not because they're failovers.

## Good to know

### The blocking escalation ladder

```text
DNS blocking → IP blocking → DPI → VPN detection → national-scale filtering
```

Each layer is progressively harder to bypass. Many countries restrict through ISPs; some run centralized systems resembling national firewalls.

### Why this is mostly beyond SDE2

For a typical SDE2 system design interview, this is sufficient: *DNS resolves domain → IP; VPN creates an encrypted tunnel; reverse proxy sits in front of servers.* Deep DPI/national-firewall/VPN-detection knowledge is networking/security-engineering territory — but understanding it sharpens intuition around DNS, TLS, proxies, gateways, CDNs, and traffic flow.

## Quick recall

**Q. Forward proxy vs reverse proxy?**
A. Forward proxy hides clients; reverse proxy hides servers.

**Q. Why use a reverse proxy?**
A. Security, load balancing, caching, SSL termination, request routing.

**Q. Why does changing DNS sometimes bypass blocking — and sometimes not?**
A. It defeats DNS-resolver-level blocks only; IP-level blocking still drops the packets.

**Q. What does a VPN hide from the ISP?**
A. The final destination website and the DNS queries (both travel inside the encrypted tunnel).

**Q. Can VPNs themselves be blocked?**
A. Yes — VPN IP blacklists, deep packet inspection, and active probing.

**Q. Reverse proxy vs gateway?**
A. Reverse proxy ⊂ gateway — the gateway adds API management, auth, policy enforcement, protocol translation.
