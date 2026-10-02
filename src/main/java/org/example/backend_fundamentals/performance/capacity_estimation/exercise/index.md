---
order: 10
search: false
---

# Exercise

## Exercise: kyc-capacity-estimate - Estimate KYC Platform Capacity

Estimate capacity for a KYC verification platform.

Assumptions:

- 1M verifications/day.
- Each verification stores 10 KB metadata.
- Each verification stores 500 KB document/image data.
- Average verification pipeline service/OCR processing latency is 2.0 seconds.
- Data is retained for 7 years.
- Storage has 3 replicas (or erasure coding option).
- Peak traffic is 4x average traffic.

Tasks:

1. Estimate average verification QPS.
2. Estimate peak verification QPS.
3. Estimate average and peak network ingress bandwidth (`bandwidth = QPS × payload size` in MB/s and Mbps).
4. Calculate peak in-flight concurrent verification requests using Little's Law ($L = \lambda \times W$).
5. Estimate raw daily storage (separate metadata DB vs document object storage).
6. Estimate 7-year replicated storage and compare 3-way replication vs erasure coding (e.g., 1.4x overhead).
7. Add 30% overhead for indexes, audit logs, thumbnails, and metadata.
8. State at least 4 architecture implications.

## Acceptance criteria

A good answer:

- labels every unit clearly (QPS, MB/s, Mbps, TB, PB)
- rounds numbers cleanly for back-of-the-envelope speed
- calculates ingress bandwidth (`QPS × 510 KB`) and explains network interface sizing
- sizes concurrent processing workers using Little's Law ($L = \lambda \times W$)
- separates structured metadata (relational/NoSQL) from blob/document data (object storage)
- compares storage efficiency of 3-way replication vs erasure coding for regulatory 7-year archive
- mentions retention/lifecycle policy (hot S3 tier transitioning to Glacier/cold archive)
- connects peak QPS to autoscaling, queues, and downstream verification vendor rate limits

## References & Authoritative Citations

- **AWS Well-Architected Framework**: Storage tiering (compliance retention) and decoupled async processing.
- **Google SRE Workbook**: Non-Abstract Large System Design (NALSD) & Little's Law concurrency sizing.
- **Little's Law ($L = \lambda W$)**: Sizing asynchronous document processing queues and thread pools.
- **System Design Primer**: Back-of-the-envelope calculations for throughput and bandwidth.

## Quick recall

**Q. Why separate metadata and document/image bytes?**
A. Metadata fits DB access patterns; large binary data belongs in object storage.

**Q. Why does peak factor matter?**
A. Systems fail at peak, and vendor/downstream limits are usually peak-sensitive.

**Q. How do you size the KYC OCR worker thread pool at peak?**
A. Using Little's Law: $L = \text{Peak QPS} \times \text{Average OCR Latency } (W)$.

**Q. Why calculate ingress bandwidth in a KYC system?**
A. High payload size (500 KB images) means even modest QPS can saturate API gateway NIC bandwidth if not sized properly or offloaded via presigned S3 upload URLs.
