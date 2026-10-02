---
order: 20
search: false
---

# Design

## Worked estimate: media-heavy social feed

Use this shape when an interviewer asks for Twitter/Instagram-style capacity.

Assumptions:

- 300M monthly active users.
- 50% are daily active users.
- Each daily active user creates 2 posts/day.
- 10% of posts contain media.
- Average media payload is 1 MB.
- Retention is 5 years.

Traffic:

```text
DAU = 300M x 50% = 150M
posts/day = 150M x 2 = 300M/day
average write QPS = 300M / 86,400 ~= 3.5K QPS
peak write QPS ~= 2x average ~= 7K QPS
```

Media storage & replication options:

```text
media posts/day = 300M x 10% = 30M/day
raw media/day = 30M x 1 MB = 30 TB/day
5-year raw media = 30 TB x 365 x 5 ~= 55 PB

Option A (3-way replication): 55 PB × 3 ≈ 165 PB (high storage overhead, simple recovery)
Option B (Erasure coding e.g. 10+4 / 1.4x): 55 PB × 1.4 ≈ 77 PB (saves 88 PB disk footprint, higher CPU)
```

Network bandwidth (Upload Ingress & CDN Offload):

```text
Upload ingress bandwidth:
- Average upload bandwidth = 30 TB / 86,400 s ≈ 347 MB/s (≈ 2.78 Gbps)
- Peak upload bandwidth (2x) ≈ 694 MB/s (≈ 5.56 Gbps)

Read egress & CDN offload:
- Assume 10:1 read-to-write ratio (3B read views/day ≈ 35K read QPS, 70K peak)
- Without CDN: Media egress at peak would exceed 50+ Gbps, overwhelming origin servers.
- With CDN offload: Edge caching (based on 80/20 hot media access) absorbs 90–95% of egress traffic.
- Origin egress drops to ~2.5–5 Gbps, protecting backend origin storage and reducing cloud egress costs.
```

Concurrent work sizing (Little's Law):

```text
Peak media upload arrival rate (λ) = 30M / 86,400 × 2 ≈ 700 uploads/second
Average upload service / transcoding latency (W) ≈ 1.5 seconds
Concurrent in-flight upload jobs (L = λ × W) = 700 × 1.5 = 1,050 concurrent workers
```

Design implications:

- Store media in object storage (e.g., S3/GCS with erasure coding), not the relational database.
- Put media behind a CDN with strong edge caching policies to offload 90%+ egress bandwidth.
- Store post metadata separately from media bytes in partitioned/replicated databases.
- Expect thumbnails/transcoded variants to add extra storage overhead (~20-30%).
- Size async worker pools and ingestion queues using Little's Law ($L = \lambda W \approx 1,050$ concurrent jobs).
- Use direct client-to-object-storage presigned uploads to keep heavy media ingress off core API application servers.

## References & Authoritative Citations

- **AWS Well-Architected Framework**: Storage tiering (S3 Standard with erasure coding vs Glacier lifecycle rules) and CloudFront CDN edge distribution.
- **Google SRE Workbook**: Non-Abstract Large System Design (NALSD) capacity budgeting and concurrency limits.
- **Little's Law ($L = \lambda W$)**: Sizing worker pools and async queuing capacity.
- **System Design Primer**: Social network capacity estimates and CDN offload models.

## Quick recall

**Q. What changes the architecture in this estimate?**
A. PB-scale media pushes you toward object storage, erasure coding, CDN offload, lifecycle policies, presigned upload URLs, and async media processing.

**Q. Does 7K write QPS automatically require sharding?**
A. Not always. It depends on DB capacity, write shape, indexes, batching, and peak factor.

**Q. Why is CDN offload critical for a 30 TB/day media upload platform?**
A. Media read egress is 10x–20x write ingress; CDN edge caching absorbs 90–95% of egress bandwidth, protecting origin storage bandwidth and reducing egress costs.

**Q. How does Little's Law size media processing workers in this scenario?**
A. At peak 700 media uploads/sec with 1.5s latency, $L = 700 \times 1.5 = 1,050$ concurrent processing threads/workers are required.
