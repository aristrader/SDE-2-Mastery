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

Media storage:

```text
media posts/day = 300M x 10% = 30M/day
raw media/day = 30M x 1 MB = 30 TB/day
5-year raw media = 30 TB x 365 x 5 ~= 55 PB
3 replicas ~= 165 PB before indexes, thumbnails, logs, backups
```

Design implications:

- Store media in object storage, not the relational database.
- Put media behind a CDN.
- Store post metadata separately from media bytes.
- Expect thumbnails/transcoded variants to add extra storage.
- Use queues/workers for media processing.
- Cache hot feed reads; storage estimates mostly affect the media pipeline and retention policy.

## Quick recall

**Q. What changes the architecture in this estimate?**  
A. PB-scale media pushes you toward object storage, CDN, lifecycle policies, and async media processing.

**Q. Does 7K write QPS automatically require sharding?**  
A. Not always. It depends on DB capacity, write shape, indexes, batching, and peak factor.
