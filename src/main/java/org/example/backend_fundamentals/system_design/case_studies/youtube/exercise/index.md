---
order: 10
search: false
---

# YouTube / Video Streaming Exercise

## Exercise: youtube-hld - Design an on-demand video platform

### Goal

Give a 35-45 minute HLD answer for a global YouTube-like product. Scope it to video upload, asynchronous processing, and on-demand playback. Do not spend time on recommendations, comments, subscriptions, or live streaming unless asked.

### Task

Design the system for 5M DAU, five views per user per day, 10% daily uploaders, 300 MB average upload, common formats/resolutions, and 1 GB maximum file size. Include:

- an API that creates an upload session and returns a pre-signed URL
- direct resumable multipart upload to object storage
- parallel metadata update and a `UPLOADING -> PROCESSING -> READY/FAILED` lifecycle
- a durable asynchronous DAG pipeline for validation, GOP split, encoding, thumbnail/watermark generation, and final output
- transcoded object storage, manifest/segment playback, and CDN delivery
- metadata DB/cache, failure recovery, global upload routing, security, and content takedown

### Acceptance criteria

- Estimate both daily source-video storage and video-delivery/CDN cost pressure.
- Explain why API servers do not proxy multi-gigabyte video bytes.
- Explain why the processing queue holds an object key/job reference rather than the video file.
- Explain why upload completion and video readiness are different states.
- Separate the video data plane from the metadata/authorization control plane.
- Explain how a DAG provides dependency ordering and parallelism without describing heap or worker-thread internals.
- Describe adaptive playback as a manifest plus short CDN-served segments, not a full-file download.
- Give one long-tail CDN-cost optimization and one recoverable versus non-recoverable processing failure.

## Quick recall

**Q. What should an HLD answer separate first?**
A. The upload-and-processing path from the read and playback path.

**Q. What is the main event after an upload completes?**
A. A durable processing event that starts validation and the transcoding DAG; it is not a direct client-visible ready signal.
