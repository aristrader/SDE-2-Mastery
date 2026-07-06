---
order: 40
---

# DNS — Resolution Flow, Caching, and Traffic Steering

## How it works

### What DNS does — and does NOT do

DNS converts human-readable domain names into IP addresses (`google.com` → `142.250.x.x`). DNS resolution only answers *"what IP address should I connect to?"* It does **not** send the webpage, establish TCP connections, perform TLS handshakes, or serve application data — all of that happens *after* DNS resolution completes:

```text
DNS → TCP → TLS → HTTP
```

(Why DNS runs over UDP is covered in `networking/tcp_vs_udp/TcpVsUdp.md`.)

### The players

| Player | Role |
|--------|------|
| **Client** | The user's machine/browser — needs an IP before it can connect |
| **DNS resolver (recursive resolver)** | Performs the whole lookup *on behalf of* the client. Examples: Google DNS `8.8.8.8`, Cloudflare `1.1.1.1`, ISP DNS. The client never directly contacts root/TLD/authoritative servers |
| **Root server** | Does NOT know website IPs. Only knows where the TLD servers are ("ask the `.com` servers") |
| **TLD server** (`.com`, `.org`, `.net`, `.in`, `.io`) | Does NOT know the final IP either. Knows which authoritative server owns the domain ("ask Google's DNS") |
| **Authoritative server** | The final source of truth — holds the actual DNS records and returns the IP |

### The hierarchy

```text
Root
 ├── .com ── google.com ── actual DNS records
 ├── .org          amazon.com
 ├── .net          netflix.com
 └── .in
```

The hierarchy exists so that **no single server needs to store information about every domain on Earth.**

### Complete resolution flow (nothing cached)

1. **Browser cache** — "do I already know google.com's IP?" If yes, lookup ends here.
2. **OS DNS cache** — if found, lookup ends.
3. **Browser asks the resolver** — resolver doesn't know yet.
4. **Resolver asks a root server** → "ask the `.com` TLD servers." (No IP yet.)
5. **Resolver asks the `.com` TLD server** → "ask Google's authoritative server." (Still no IP.)
6. **Resolver asks the authoritative server** → `142.250.x.x` — the actual record.
7. **Resolver returns the IP to the client** and caches it. Only now does the browser start TCP → TLS → HTTP.

### Caching and TTL

Without caching every lookup would walk resolver → root → TLD → authoritative — slow and expensive. DNS caches at multiple layers: **browser → OS → resolver**. Because of this, most lookups never reach root servers.

**TTL (Time To Live)** defines how long a DNS answer may be cached. `TTL = 300` means "cache for 5 minutes, then ask again."

**Why TTL exists:** if the IP behind a domain changes (`142.250.195.78` → `142.250.195.90`) and clients cached forever, traffic would keep hitting the old server. TTL prevents stale routing.

**TTL trade-off:**

| High TTL | Low TTL |
|----------|---------|
| Fewer DNS queries | More DNS queries |
| Better cache hit rate | Lower cache hit rate |
| Lower DNS load | Higher DNS load |
| Slower failover | Faster failover |
| Slower migrations | Faster migrations |

Common interview point: **before a migration or failover, teams reduce TTL** so caches expire quickly and traffic moves to the new destination faster.

### DNS-based traffic steering

DNS is not only name resolution — large systems use it to decide *where* traffic should go. The same domain can return different IPs: a user in Bangalore gets a Bangalore IP for `netflix.com`, a user in London gets a London IP.

**CDNs:** with servers in Bangalore, Singapore, Tokyo, London, New York, the authoritative DNS returns different IPs depending on user location — routing users to nearby infrastructure. DNS becomes the first layer of load balancing.

**DNS-based load balancing:** the authoritative server can return Server A, B, or C based on geography, latency, traffic conditions, or health checks. DNS is a traffic-control mechanism, not just a phonebook.

**DNS-based failover:** if a server becomes unavailable, DNS can be updated to return a backup server's IP.

### How DNS determines user location — important nuance

The authoritative DNS server often sees the **resolver's IP address, not the end user's IP**. If you use Cloudflare's resolver, Netflix's DNS primarily sees the Cloudflare resolver IP, not your exact IP. This is one reason DNS-based geolocation is approximate.

To improve this, modern DNS often uses **EDNS Client Subnet (ECS)**, which allows the resolver to forward a partial subnet of the client's IP to the authoritative server for better geolocation accuracy.

## Gotchas / Trick questions

1. **"Root servers know website IPs."** No — they only know `TLD → TLD server locations` (`.com → TLD servers`). They are the first directory lookup.
2. **"TLD servers know website IPs."** No — they only know `google.com → authoritative server`. The authoritative server holds the actual records.
3. **"DNS lookup sends webpage data."** No — DNS only provides domain → IP. The webpage is fetched afterwards via TCP → TLS → HTTP.
4. **"Every website must run its own authoritative DNS servers."** Not necessarily. Every domain must *have* authoritative servers, but a DNS provider may run them on the company's behalf — ownership and operation are separate concerns.
5. **"Root servers seem unnecessary — why not let resolvers know all TLD servers directly?"** The root layer is a universal, globally consistent starting point. Without it, every resolver would independently maintain the TLD mapping.
6. **"Root servers must be overloaded since every lookup starts there."** Not in practice — resolvers cache TLD information, authoritative information, and final records for long periods, so most lookups never reach the root.
7. **Do root and TLD servers have fixed IPs?** Yes — resolvers rely on stable root-server information, and TLD servers also keep stable addresses.
8. **Where are root-server addresses stored?** Not in browsers — resolvers ship with a **root-hints file** containing root-server information.

## Performance characteristics

### Why DNS scales

The hierarchy distributes responsibility — no layer stores the entire internet:

| Layer | Responsibility |
|-------|---------------|
| Root | Knows TLD locations |
| TLD | Knows authoritative servers |
| Authoritative | Knows actual records |

### Why caching is critical

Without caching: every lookup → root → TLD → authoritative. With caching: most lookups end at a resolver cache hit — dramatically lower latency and infrastructure load.

## Good to know

### Mental model

```text
Root          → Reception desk  ("ask .com")
TLD           → Department      ("ask Google's DNS")
Authoritative → Actual owner    ("here is the IP")
```

This single mental model is often enough to reconstruct the entire DNS lookup process during interviews.

### DNS is more than a phonebook

Beyond name → IP mapping, DNS participates in CDN routing, traffic steering, load balancing, and failover — which is why it appears constantly in system design discussions.

## Quick recall

**Q. What is the job of a DNS resolver?**
A. Find the IP on behalf of the client by querying root, TLD, and authoritative servers.

**Q. Does a root server know google.com's IP? Does a TLD server?**
A. No and no. Root knows where `.com` TLD servers are; TLD knows which authoritative server owns google.com.

**Q. Which server holds the final DNS records?**
A. The authoritative DNS server.

**Q. What does TTL control?**
A. How long DNS responses may be cached before revalidation.

**Q. Why reduce TTL before a migration?**
A. So cached records expire quickly and traffic moves to the new destination faster.

**Q. Does DNS send webpage data?**
A. No — DNS only resolves names to IPs; TCP/TLS/HTTP happen afterward.

**Q. How do CDNs use DNS?**
A. They return different IPs by user location/conditions, steering users to nearby infrastructure — though geolocation is approximate because the authoritative server usually sees the resolver's IP, not the user's.

**Q. How does DNS provide load balancing and failover?**
A. Different IPs for the same domain (by geography, latency, health), and record updates that point traffic at backup servers.


