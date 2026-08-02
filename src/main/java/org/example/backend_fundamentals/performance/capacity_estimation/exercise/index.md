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
- Data is retained for 7 years.
- Storage has 3 replicas.
- Peak traffic is 4x average traffic.

Tasks:

1. Estimate average verification QPS.
2. Estimate peak verification QPS.
3. Estimate raw daily storage.
4. Estimate 7-year replicated storage.
5. Add 30% overhead for indexes, audit logs, thumbnails, and metadata.
6. State at least 4 architecture implications.

## Acceptance criteria

A good answer:

- labels every unit
- rounds numbers cleanly
- separates metadata from media/blob data
- mentions object storage for images/documents
- mentions retention/lifecycle policy
- connects peak QPS to autoscaling, queues, and downstream vendor limits

## Quick recall

**Q. Why separate metadata and document/image bytes?**  
A. Metadata fits DB access patterns; large binary data belongs in object storage.

**Q. Why does peak factor matter?**  
A. Systems fail at peak, and vendor/downstream limits are usually peak-sensitive.
