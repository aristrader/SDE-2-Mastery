---
order: 10
search: false
---

# YouTube / Video Streaming Exercise

## Exercise: youtube-hld - Design an on-demand video platform

### Goal

Give a 35-45 minute HLD answer for a global YouTube-like product. Scope it to video upload, asynchronous processing, and on-demand playback. Do not spend time on recommendations, comments, subscriptions, or live streaming unless asked.

## Timed mock

Set a 45-minute timer. Keep upload and playback as separate paths from the beginning.

| Time | What to produce |
|---|---|
| 0-5 min | Scope, upload/playback requirements, availability target |
| 5-8 min | Upload volume, source storage, transcoding, and delivery estimate |
| 8-13 min | Upload-session/playback APIs and video lifecycle |
| 13-25 min | Direct upload, metadata, processing pipeline, storage, manifest, CDN |
| 25-40 min | Deep dive: reliable processing DAG and global playback/cost |
| 40-45 min | Failure recovery, security, and trade-offs |

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

## Interviewer follow-ups

1. Why should the queue contain a job/object reference instead of the uploaded video bytes?
2. A transcoding worker crashes after writing some variants. How can retry be safe?
3. Why are `UPLOADED` and `READY` different states?
4. What happens if the closest CDN region has no cached segment?

## Self-review

| Signal | Score 0-2 |
|---|---|
| Upload control plane and byte data plane are separate | |
| Processing uses durable state, idempotent jobs, and dependency ordering | |
| Playback uses manifests, segments, adaptive bitrate, and CDN | |
| Global routing and CDN cost are justified by the estimate | |
| Failure, takedown, and authorization boundaries are explicit | |

**Target:** at least `7/10`. Then compare with [Design](/system_design/case_studies/youtube/design/).

## Quick recall

**Q. What should an HLD answer separate first?**
A. The upload-and-processing path from the read and playback path.

**Q. What is the main event after an upload completes?**
A. A durable processing event that starts validation and the transcoding DAG; it is not a direct client-visible ready signal.
