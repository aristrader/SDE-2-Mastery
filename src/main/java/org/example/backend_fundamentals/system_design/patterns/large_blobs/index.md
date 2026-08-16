---
order: 80
---

# Handling Large Blobs

Use this pattern for images, documents, audio, and video. Application servers should authorize the operation and store metadata, not proxy multi-gigabyte bytes.

## Upload and download flow

```text
client -> API: create upload intent -> Blob metadata(PENDING) + scoped presigned URL
client -> object storage: direct multipart/resumable upload
object storage -> event/verification worker -> Blob metadata(READY)

viewer -> API authorization -> signed CDN/object URL -> CDN/object storage
```

The presigned URL is short-lived and scoped to a specific object/key, method, size, and content type where the store supports it. Downloads should use a CDN; protected files use signed URLs or signed cookies.

## Consistency and failure handling

- Keep metadata and blob state explicit: `PENDING`, `UPLOADED`, `VERIFIED`, `READY`, `FAILED`, `DELETED`.
- An upload success response from storage does not mean the file is safe to serve. Verify checksums, scan if required, and process/transcode asynchronously before `READY`.
- Use multipart/resumable upload for large or unreliable client connections. Persist the upload session so the client can resume.
- A periodic reconciler finds orphaned metadata and unreferenced objects. Lifecycle rules remove abandoned uploads.
- Never trust a client filename, MIME type, or callback alone for security decisions.

## Interview delivery

Say: "The API creates an authorized upload intent and metadata record; the browser uploads directly to object storage. Storage completion triggers verification, then the CDN serves the ready asset. This removes application-server bandwidth from the hot path."

## Quick recall

**Q. Why use a presigned upload URL?**
A. The client transfers bytes directly to storage while the API keeps authorization and metadata control.

**Q. How do metadata and storage stay consistent?**
A. Use explicit states, storage events plus verification, and reconciliation for missed events or abandoned uploads.
