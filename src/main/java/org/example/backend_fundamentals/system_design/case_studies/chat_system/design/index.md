---
order: 20
search: false
---

# Design a Chat System

## Problem

Design a text chat system like Messenger for 50M DAU. Support one-to-one chat, groups of at most 100 users, online presence, web/mobile clients, multiple devices, indefinite history, and push notifications for offline recipients.

## Agreements that shape the design

| Question | Agreed answer | Decision it unlocks |
| --- | --- | --- |
| Conversation types? | One-to-one and groups capped at 100 | Per-recipient inbox fanout is affordable; public-channel fanout is excluded. |
| Which delivery guarantee? | At-least-once after durable acceptance | Sender retry uses `clientMessageId`; device dedupe uses `messageId`. |
| Which ordering? | Per conversation, not global | Route each conversation to one append partition/owner. |
| Multiple devices and offline use? | Yes, with retained history | Maintain a cursor per user-device-conversation and pull missed history on reconnect. |
| Media, E2EE, search? | Out of scope | Keep the answer focused on real-time text delivery and recovery. |

## Requirements

### Functional

- Send and receive messages in one-to-one and small group conversations.
- Persist history and synchronize missed messages across devices.
- Show online/offline presence.
- Push-notify offline recipients.

### Non-functional

- Low-latency live delivery.
- Durable messages; no loss after accepted send.
- Ordered messages within a conversation.
- Horizontally scalable connection and storage tiers.
- Tolerate reconnects, retries, and duplicate delivery.

## APIs and WebSocket events

HTTP is suitable for authentication and profile APIs. Use WebSocket after login for real-time events.

```text
WS client -> server: SEND_MESSAGE { clientMessageId, conversationId, body }
WS server -> client: MESSAGE { messageId, conversationId, sequence, senderId, body }
WS server -> client: ACK { clientMessageId, messageId }
WS client -> server: SYNC { conversationId, afterSequence }
WS client -> server: HEARTBEAT
```

`clientMessageId` makes sender retries idempotent. `messageId` identifies a durable message for deduplication;
`sequence` defines its order inside one conversation.

## Architecture

![Chat system architecture](../assets/chat-system-architecture.svg)

| Component | Responsibility |
| --- | --- |
| API servers | Login, signup, profile, settings; stateless HTTP |
| Service discovery | Select a healthy chat server by region and connection capacity |
| Chat servers | Own WebSockets, authenticate events, route live messages |
| Message store | Durable ordered history, partitioned by conversation/channel |
| Sync stream/inbox | Decouple durable write from recipient/device delivery |
| Presence servers/store | Heartbeats, online state, presence events |
| Notification system | Push notification for offline users |

## Message storage

Store normal relational data such as profiles/settings/friends in a relational database. Put chat history in a distributed key-value or wide-column store, partitioned by conversation/channel. It supports high write volume, low-latency recent reads, and scalable historical storage.

```text
message(
  conversation_id,
  sequence,
  message_id,
  sender_id,
  body,
  created_at,
  primary key(conversation_id, sequence)
)
```

For one-to-one conversations, derive a stable `conversationId` from the two user IDs. For groups,
`conversationId` is the channel/group ID and the partition key. Route that partition to one append owner, or
use a conditional append position, so concurrent writers do not invent conflicting local orders.

## Send and receive flow

1. Sender connects to a chat server through WebSocket selected by service discovery.
2. Sender emits `SEND_MESSAGE` with `clientMessageId`.
3. Chat server checks membership and deduplicates sender retry.
4. Server routes the command to the conversation append owner, which assigns the next durable order and
   writes the message with a recipient-delivery record/outbox event.
5. Server acknowledges the sender only after that durable write.
6. Connected recipient devices receive live WebSocket pushes from their chat-server owner.
7. Offline recipients receive a push notification and synchronize history when they open/reconnect.
8. Recipients ACK/read separately; delivery retries may replay, so dedupe by `messageId`.

## Ordering and delivery semantics

Require ordering **within one conversation**, not system-wide. A per-conversation sequence is the simplest
option. A time-sortable globally unique ID is useful for identity but does not alone create a strict order if
two chat servers append concurrently; the storage partition and append ownership must agree.

The correct delivery claim is at-least-once. A server can persist a message, crash before ACK, and receive a sender retry. Store `(senderId, clientMessageId) -> messageId` to return the first result, and let recipients ignore repeated `messageId`s.

## Multiple devices

Track a cursor per user-device-conversation. On reconnect, each device requests messages after that
conversation's cursor; then it advances the cursor only after durable local handling.

```text
(userId, deviceId, conversationId) -> lastAppliedSequence
```

Live push is an optimization. The durable message store and cursor are the correctness path.

## Group chat

For the stated maximum of 100 members, persist one channel message and fan out lightweight references to member inboxes. This is deliberately a small-group design.

For large groups, explain the change: store one channel log and have clients pull recent channel messages, or use a hybrid. Do not make a copy per member for a 100,000-member channel.

## Presence flow

1. WebSocket connect writes presence with a heartbeat expiry.
2. Heartbeats refresh it periodically.
3. Logout clears it immediately.
4. Missed heartbeat beyond a grace interval changes status to offline.
5. Presence events are pushed to subscribed friends; large groups fetch on demand.

Presence is approximate. A user can be online but inactive, or disconnect immediately after an update.

## Failure handling

| Failure | Design response |
| --- | --- |
| Chat server unavailable | Client reconnects using service discovery and syncs from cursor |
| ACK lost after durable append | Sender retry with `clientMessageId` returns the original message result |
| Delivery event delayed/replayed | Outbox retries it; recipient deduplicates by `messageId` |
| Recipient sees duplicate | Ignore already-processed `messageId` |
| Push notification missing | Reconnect/history sync remains correct |
| Short network loss | Heartbeat grace period avoids false offline state |
| Storage latency/outage | Do not ACK accepted send before a durable write; surface retryable error |

## Quick recall

**Q. What is the main state on a chat server?**  
A. The active persistent WebSocket connections it owns.

**Q. What owns correctness when a user is offline?**  
A. Durable message history plus a per-device sync cursor, not the push notification.

**Q. What ordering should the design promise?**  
A. Per-conversation/channel ordering.

**Q. What does a heartbeat solve?**  
A. It avoids marking a user offline on every brief network interruption.
