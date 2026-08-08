---
order: 20
search: false
---

# Design YouTube / Video Streaming

## Problem and requirements

Design an on-demand video-sharing platform with fast uploads, smooth global playback, adaptive quality, high availability, and controlled infrastructure cost. Support browser, mobile, and smart-TV clients; accept common formats up to 1 GB; use existing object storage and CDN infrastructure.

### Functional requirements

- Upload a video and its metadata.
- Process it into playable variants and report readiness.
- Play a video progressively, change quality as network conditions change, and resume failed uploads.
- Enforce upload and playback authorization; support takedown or blocking.

### Non-functional requirements

- Keep large video bytes off the synchronous API path.
- Serve playback globally with low startup latency and high availability.
- Tolerate processing failures without losing uploaded source files.
- Keep CDN and transcoding cost proportional to content demand where possible.

## Derive the architecture from the naive design

Start with `client -> API service -> storage`. It works functionally, but passing multi-GB uploads and downloads through the API tier turns application servers into the bandwidth bottleneck. The design evolves for a reason:

```text
Large video bytes -> object storage and CDN, not API servers
Unreliable large uploads -> direct multipart/resumable upload
CPU-heavy conversion -> upload-complete event plus async workers
Different devices and networks -> multiple encoded bitrates plus short segments
Global hot playback -> CDN edges; origin storage only on misses
```

This reasoning is more valuable than listing components. Metadata, authorization, status, and signed URLs remain in the API/control plane; source video, processed segments, and playback remain in the data plane.

## API and state

```text
POST /v1/videos
{ "title": "Kafka guide", "contentType": "video/mp4", "sizeBytes": 734003200 }

201 Created
{ "videoId": "v_123", "status": "UPLOADING", "uploadUrl": "short-lived signed URL" }

PUT <uploadUrl>
Content-Range: bytes 0-8388607/734003200

GET /v1/videos/v_123
{ "status": "PROCESSING" | "READY" | "FAILED", "playbackUrl": ".../master.m3u8" }
```

`POST /v1/videos` creates metadata and returns scoped upload authorization. The client uploads chunks directly to object storage and can retry only failed chunks. The API's status is authoritative; receiving the last byte does not mean the video is playable.

```text
UPLOADING -> PROCESSING -> READY
                     \-> FAILED
READY -> TAKEN_DOWN
```

## Upload and metadata flow

1. Client calls the API service with initial metadata.
2. API service authenticates the creator, creates video `v_123` with `UPLOADING`, and returns a pre-signed multipart-upload URL or session.
3. Client uploads GOP-aligned chunks directly to a nearby object-storage upload endpoint. The client resumes from confirmed chunks after a network failure.
4. In parallel, the client/API service persists title, owner, size, format, and other metadata in the metadata DB and updates cache.
5. Object storage emits an upload-complete event. The processing pipeline changes status to `PROCESSING`.

GOP means a small independently playable group of frames, typically a few seconds. Splitting on GOP boundaries allows independent work and clean adaptive-streaming segments. Older clients that cannot split a video can upload the whole file and let the server-side preprocessor split it.

### End-to-end flow to say out loud

```text
Upload control: client -> API gateway -> Video Service -> metadata DB/cache -> signed upload URL
Upload bytes: client -> nearby object storage -> completion event -> processing queue and workers
Playback: client -> Video Service for authorization/manifest -> CDN -> transcoded origin only on a miss
```

The API gateway and Video Service serve small control-plane requests, not video bytes. A queue entry is a compact reference such as `{ videoId, sourceObjectKey }`; workers read the source from object storage. This division prevents network-heavy video traffic from becoming an application-server or queue bottleneck.

## Transcoding and processing DAG

![Video processing pipeline](../assets/video-processing-pipeline.svg)

Raw files are too large, browsers support different formats, and viewers have different bandwidth. Transcoding therefore produces multiple representations. A container (`.mp4`, `.mov`) holds video, audio, and metadata; a codec (for example H.264, VP9, or HEVC) compresses/decompresses the media. Higher bitrate generally gives better quality but requires more bandwidth and processing.

A DAG expresses only the task dependencies. For one upload, inspection and media demuxing happen first; video encoding, audio encoding, thumbnail generation, and watermarking can then run in parallel. A worker should not wait synchronously for another component's network call when a durable event can express the dependency.

| Pipeline component | HLD responsibility |
| --- | --- |
| Preprocessor | Inspect input, split into GOP chunks, construct the processing DAG, and persist intermediate chunks/metadata for retry. |
| DAG scheduler | Release tasks whose dependencies have completed. |
| Resource manager | Select runnable work by priority and compatible available worker capacity; track running task-to-worker assignments. |
| Task workers | Perform CPU/GPU-heavy encoding, thumbnail, inspection, audio, or watermark tasks. |
| Temporary storage | Retain retryable GOPs and intermediate state until processing finishes. |
| Output storage | Store final renditions, thumbnails, segments, and playback manifests. |

At normal SDE-2 HLD depth, say "queue-backed workers with dependency-aware scheduling." Explain the task queue, worker-capacity queue, and running-assignment tracking only if the interviewer drills into transcoding scheduling; exact priority-queue implementation is LLD-level detail.

## Publish flow and playback

1. Completed tasks write renditions, segments, thumbnails, and a manifest to transcoded object storage.
2. A completion event is consumed by handlers that verify required outputs, mark metadata `READY`, update cache, and make the content eligible for CDN distribution.
3. Client calls `GET /v1/videos/{id}`. API service checks authorization, reads cache/metadata, and returns a playback or manifest URL.
4. The player requests the manifest and only a few segments at a time from the nearest CDN edge.
5. The player selects or switches bitrate/resolution according to measured network conditions. This is adaptive bitrate streaming; HLS and MPEG-DASH are common HTTP-based protocol families.
6. On a CDN miss, the edge fetches from transcoded origin storage, then caches according to policy.

The CDN carries video bytes. API servers carry metadata, authorization, feed/recommendation requests, and playback URL generation. Do not proxy every segment through API servers.

The player does not need the full file before starting playback. It fetches only the next few segments and can seek directly to later segments. Pre-creating short segments for each rendition, rather than dynamically requesting arbitrary multi-minute ranges, makes adaptive playback practical.

## Scale, reliability, and recovery

| Failure | Handling |
| --- | --- |
| Chunk upload fails | Resume and retry the failed chunk; keep completed chunks. |
| One transcode task fails transiently | Retry with bounded attempts; retain inputs in temporary storage. |
| Input is malformed or policy-rejected | Stop dependent tasks, mark `FAILED` or `TAKEN_DOWN`, and return a clear status. |
| Worker fails | Reschedule its unacknowledged/running task on another worker. |
| Queue or scheduler replica fails | Fail over to a replica; keep durable task state and idempotent task outputs. |
| API instance fails | Load balancer sends the request to another stateless instance. |
| Cache node fails | Read another replica or metadata DB, then repopulate cache. |
| Metadata writer fails | Promote a replicated writer; serve reads from healthy replicas as appropriate. |

Make publish idempotent: task retries may produce the same rendition more than once, but the completion handler must not incorrectly expose a partially processed video. Keep serving the last known metadata state until the new state is complete.

## Global performance and cost

- Route uploads to geographically close upload endpoints; CDN infrastructure can also provide these ingress locations.
- Use multipart, resumable, parallel GOP-aligned upload to reduce restart cost for large files.
- Keep popular, regionally popular content warm in CDN; long-tail content can be served from high-capacity origin or encoded on demand when product requirements permit.
- Do not distribute every rendition to every region. Use observed audience geography and access patterns.
- Building a private CDN or partnering with ISPs is a late-stage option for a massive platform, not the default interview answer.

## Design decisions to defend

| Question | Strong interview answer |
| --- | --- |
| Why eventual consistency? | The product tolerates a bounded processing/publishing delay. It is not because a new upload is assumed to be low traffic. |
| Why `POST /videos`? | It creates a video resource and server-side upload state; `GET` should not create state. |
| Why object storage instead of a database blob? | Video bytes are large, streaming-oriented objects; the metadata DB stores the small, queryable control record. Database selection still depends on metadata access patterns, not merely relationship count. |
| What does cache store? | Hot metadata, readiness state, and playback-related information. CDN/object storage handle multi-GB media bytes. |
| What level of streaming protocol detail is needed? | Explain manifest plus segmented adaptive playback and name HLS/DASH. Codec math and protocol packet internals are out of scope. |

## Security and policy

- Authenticate the creator and issue a short-lived, object-scoped pre-signed upload URL.
- Enforce authorization for private or paid playback; use signed playback URLs/cookies where needed.
- Protect content with DRM, encryption, and/or visible watermarking when the product requires it.
- Run moderation and copyright checks during or after processing, and support user reports. A takedown must revoke playback eligibility and purge/invalidate CDN availability according to policy.

## Follow-up boundaries

Live streaming shares ingest, encoding, and CDN delivery, but it has stricter end-to-end latency and different failure handling: waiting for large retries is often unacceptable, and chunks arrive continuously. Keep it out of the base on-demand answer unless asked.

## Quick recall

**Q. Why process uploaded video asynchronously?**
A. Transcoding and segment creation are expensive and should not hold the upload request open.

**Q. Why use a DAG for processing?**
A. It captures dependencies while allowing independent tasks such as video encode, audio encode, thumbnailing, and watermarking to run in parallel.

**Q. What separates a CDN from the metadata API tier?**
A. CDN serves large video segments; API servers serve small control-plane metadata and authorization responses.

**Q. What is adaptive bitrate streaming?**
A. The player chooses among pre-encoded variants as network conditions change, balancing quality and uninterrupted playback.
