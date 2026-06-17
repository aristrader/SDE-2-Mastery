# CDN — Edge Caching, DNS Routing, Push vs Pull

## How it works

### Core idea

A CDN (Content Delivery Network) is a **geographically distributed cache** that stores copies of content closer to users. Instead of every request traveling to the origin server, users are served from nearby CDN nodes whenever possible.

Typical CDN content: images, videos, CSS, JavaScript, software downloads, other static assets.

### Origin server

The origin is the **source of truth** — an application server, object storage (e.g. S3), or file server. CDN nodes only store copies; missing content is fetched from the origin.

### Cache hit vs cache miss

- **Hit:** content exists in the CDN node → served directly, no request reaches the origin. Benefits: lower latency, lower backend load, reduced bandwidth.
- **Miss:** the CDN fetches from origin, stores a copy locally, returns it — future requests become hits.

### DNS-based routing

One of the most important CDN concepts. When a user requests `example.com`, DNS does not return the same IP globally — the CDN provider's DNS infrastructure estimates the user's location and returns a **nearby CDN node's IP**:

| User location | DNS returns |
|---------------|-------------|
| India | Mumbai CDN node |
| UK | London CDN node |
| USA | New York CDN node |

(Resolution mechanics in `networking/dns/DnsResolution.md`.)

### PoP (Point of Presence)

A PoP is simply a CDN location (Mumbai PoP, London PoP, Singapore PoP), usually containing multiple cache servers.

### Pull CDN

The most common model — content is fetched **only when requested**: user request → cache check → miss → fetch from origin → store locally. Advantages: no preloading needed; storage used only for requested content.

### Push CDN

Content is **proactively distributed** to CDN nodes before users request it — e.g. large software updates, game patches, OS releases. The content is already at CDN locations when users begin downloading.

## Gotchas / Trick questions

1. **"Aren't CDN and Redis basically the same thing?"** They're both caches, but they solve different problems:

   | Redis | CDN |
   |-------|-----|
   | Close to backend services | Close to end users |
   | Caches application data | Caches static content |
   | Reduces database load | Reduces network latency and origin load |

2. **"Do Redis and CDN both have hits and misses?"** Yes — the caching concept is identical (hit → serve; miss → fetch from source and cache). The difference is *where* the cache sits and *what* it stores.
3. **"CDN drawbacks mention cost and complexity — isn't it actually reducing complexity vs building global infra ourselves?"** Good observation: the stated drawbacks are relative to *not using a CDN at all*. Cost = paying providers for bandwidth/distribution; complexity = cache invalidation, asset versioning, cache-control policies. Compared to building worldwide infrastructure yourself, a CDN reduces complexity dramatically.
4. **"Do I need to modify application logic to use a CDN?"** Not usually — core business logic is unchanged. Typical additions: asset versioning, cache-control headers, CDN configuration. The CDN is an optimization layer around the application.
5. **"Does Netflix use pull CDN?"** Generally yes — content is fetched and cached on demand; popular content naturally becomes cached near users without preloading everything everywhere.
6. **"Is live streaming push CDN?"** Not exactly — live streams are delivered as small video segments continuously distributed through CDN infrastructure in near real-time. Think of it as a specialized streaming architecture rather than pure push or pull.
7. **"Do governments restrict CDN distribution?"** Sometimes — data sovereignty requirements, regional regulations, content restrictions, licensing. CDN providers may need regional controls or local infrastructure to comply.
8. **"How does the CDN know which node is nearest to me?"** DNS-based geolocation — the CDN's DNS estimates location from the DNS request and returns a nearby node's IP. For SDE2 interviews you generally don't need to go deeper.

## Performance characteristics

**Benefits:** lower latency (nearby nodes), reduced origin load (many requests never reach it), reduced bandwidth consumption, better user experience for static assets.

**Trade-offs:** cost (global CDN infrastructure isn't free) and cache-invalidation complexity (updated content may persist in CDN caches until invalidated or refreshed).

## Good to know

### Push CDN in the wild

Major OS updates and large game patches are distributed to CDN locations *before* release so millions of users can immediately download them.

### Interview depth guidance

For most SDE2 interviews this is sufficient: why CDN exists · origin server · hit vs miss · CDN vs Redis · DNS routing to the nearest node. Deep CDN internals are only expected for infrastructure-heavy roles.

## Quick recall

**Q. What problem does a CDN solve?**
A. Serving content from geographically nearby locations to reduce latency and origin load.

**Q. What is an origin server?**
A. The source of truth that stores the original content.

**Q. What happens on a CDN cache miss?**
A. The CDN fetches from origin, stores a local copy, and returns it.

**Q. How does a CDN route users to nearby nodes?**
A. DNS-based geolocation — location-aware DNS responses.

**Q. Redis vs CDN?**
A. Redis caches application data near backend services; CDNs cache static content near users.

**Q. Pull vs push CDN?**
A. Pull fetches on demand (most common); push proactively distributes content before requests (OS updates, game patches).

**Q. What is a PoP?**
A. A Point of Presence — a CDN location containing cache servers.
