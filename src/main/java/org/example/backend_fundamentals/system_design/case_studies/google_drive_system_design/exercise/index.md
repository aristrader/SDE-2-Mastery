---
order: 10
search: false
---

# Google Drive System Design Exercise

## Exercise: file-sync-hld - Design a Cross-Device File System

### Goal

Practice a complete HLD answer for a 10 GB file-storage and synchronization system.

### Task

Design upload, download, remote sync, and conflict handling. Include:

- metadata database versus object storage responsibilities
- direct resumable multipart upload with scoped authorization
- an authoritative object-storage completion event and idempotent worker
- local sync client, notification-driven reconciliation, and offline recovery
- version preconditions and conflicted-copy behavior
- replication, backup/PITR, and multi-region bounded staleness

### Acceptance criteria

- Keep multi-GB bytes off API servers and the metadata database.
- State why a client callback alone cannot finalize upload state.
- Distinguish transfer chunks from video-streaming segments.
- Explain why notification is a change hint, not the synchronization itself.
- Include bandwidth/storage estimation as well as request QPS.
- Keep CRDT/OT and real-time collaboration outside the base answer.
