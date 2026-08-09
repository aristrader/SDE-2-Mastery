---
order: 20
search: false
---

# Design Google Drive System

## Requirements

Support arbitrary files up to 10 GB across desktop, mobile, and web clients. Users upload/download, share files, retain revisions, and see changes made on another device. Files are encrypted in transit and at rest. Metadata operations such as folder listing or starting an upload should normally complete within about 500 ms; transfer time is handled separately by the data plane. The base answer excludes collaborative text editing; arbitrary-file conflicts are preserved rather than automatically merged.

## Derive the design

Start with `client -> API server -> database`. It fails because databases and API servers become the storage and bandwidth bottleneck for multi-GB files. Split responsibilities:

```text
Metadata and permissions -> transactional metadata store
Large file bytes         -> durable object storage
Upload/download bytes    -> direct client-to-storage transfer
Remote change awareness  -> event plus notification
```

## APIs and state

```text
POST /v1/files
{ "name": "resume.docx", "sizeBytes": 734003200, "baseVersion": 5 }

201 Created
{ "fileId": "f_123", "uploadId": "u_456", "status": "UPLOADING", "partUploadAuthorization": "..." }

POST /v1/files/f_123/uploads/u_456/complete
GET  /v1/files/f_123/download
GET  /v1/files/f_123/revisions?limit=20
```

```text
UPLOADING -> READY -> DELETED
     |          |
     -> FAILED  -> new version creates a new immutable FileVersion
```

The API creates state with `POST`. Object-storage completion, not a client request, is authoritative for transition to `READY`.

## Upload path

1. A sync client watches a local file save and submits metadata plus `baseVersion`.
2. API service validates ownership/permission and creates a multipart upload session in `UPLOADING`.
3. Client uploads parts directly to object storage using short-lived scoped authorization and resumes only failed parts.
4. Storage emits a completion event to a durable queue.
5. Worker conditionally writes the immutable `FileVersion`, updates `File.currentVersion`, and emits `FileChanged`.
6. Notification service alerts online devices; offline devices reconcile from retained event/change state.

Use conditional version writes and idempotency so a duplicate completion event cannot create two versions or send an incorrect state transition.

## Sync and conflict path

```text
Remote FileChanged -> sync client fetches metadata -> compare local version/hashes
  -> current: do nothing
  -> stale: direct-download missing blocks -> reconstruct/apply new version
```

For an update, require `baseVersion`. If the server current version differs, preserve the submitted update as a conflicted copy rather than silently overwriting data. This is sufficient for arbitrary binary files. OT/CRDT design belongs to a collaborative-document follow-up.

## Scale and recovery

- Keep API services stateless behind a load balancer; durable state lives in metadata DB, object storage, and queues.
- Replicate metadata and object data; use backups/PITR as well as replication.
- Route a user to a nearby region and asynchronously replicate files and metadata where bounded staleness is acceptable.
- Reconnect notification clients gradually after an outage and run reconciliation as a fallback.
- Use block hashes for changed-block synchronization only when the bandwidth saving justifies its extra client and metadata complexity.

## Quick recall

**Q. Why is an object-storage event needed after direct upload?**
A. The client can disappear after storage accepted the bytes; the storage event reliably drives the server-side completion workflow.

**Q. What is the base conflict strategy?**
A. Compare `baseVersion` with the current version and preserve a conflicted copy on mismatch.

**Q. What does the notification service do?**
A. It tells devices that a file changed. Devices fetch metadata and synchronize separately.
