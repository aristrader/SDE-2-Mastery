---
order: 80
---

# Chat System

## Interview scope

Design a Messenger-style text-chat system for mobile and web. This deliberately covers one-to-one chat,
groups of at most 100 members, multiple devices, presence, durable history, and offline notification. It
does not cover media processing, end-to-end encryption, search, or large public channels.

| Clarification | Agreed answer | Design consequence |
| --- | --- | --- |
| What can users send? | Text only | No object storage, upload pipeline, or CDN in the base answer. |
| Which ordering matters? | Per conversation/channel | Each conversation needs one durable append order; global total order is unnecessary. |
| What delivery promise is useful? | At-least-once, durable after acceptance | Sender retries need idempotency and devices deduplicate a durable message ID. |
| How large are groups? | At most 100 members | Fan out lightweight inbox references per member; this is not a public-channel design. |
| What happens on a second device or reconnect? | It must catch up fully | Keep durable history and a cursor per device-conversation, not one cursor per account. |

The scale assumption is **50M DAU**, but the useful estimate is concurrent WebSocket connections rather than
raw DAU. That number determines chat-server connection capacity and regional fleet size; exact message QPS
is only worth calculating if it changes a storage partition or fanout decision.

## Start with one durable message

The smallest correct path is not “send a WebSocket frame to the other user.” A frame can be lost and either
chat server can fail. Instead, the sender's chat server authenticates and authorises the conversation,
deduplicates `clientMessageId`, appends the message in the conversation's durable order, and records the
recipient-delivery work with that write. Only then does it acknowledge the sender.

Live WebSocket push makes the message timely. Durable history plus a per-device, per-conversation cursor
makes it correct when the recipient is offline, the connection owner changes, or delivery is replayed.

## Interview delivery

1. Agree the narrow scope above and name ordering, durability, and multi-device sync as the cruxes.
2. Establish HTTP for account APIs and persistent WebSockets for active chat.
3. Trace one accepted message through durable append, recipient routing, live delivery, and offline catch-up.
4. Deep dive on conversation ordering/idempotency, connection ownership/reconnect, and bounded small-group fanout.
5. Close with heartbeat-based presence, retries, deduplication, and explicit extensions.

## Architecture follows the path

![Chat system architecture](./assets/chat-system-architecture.svg)

Connection routing returns a healthy, nearby chat-server endpoint with connection capacity. The client keeps
that WebSocket until it disconnects; normal HTTP load balancing alone cannot route a server-originated
message to the connection's owner. The connection registry maps a user/device to that owner so delivery
workers know which chat server can perform the final live push.

## Transport choice

| Approach | Interview position |
| --- | --- |
| Polling | Easy but repeatedly asks when no message exists; wasteful at scale |
| Long polling | Better than polling, but reconnect churn and connection ownership make routing awkward |
| WebSocket | Strong default: client-initiated upgrade, persistent, bidirectional, uses ports 80/443 |

Use WebSocket for message send and receive to simplify the client and server protocol. Keep signup, login, profiles, search, and settings as ordinary HTTP APIs.

## Data and storage

Use relational storage for account/profile/settings/friend data. Use a horizontally scalable wide-column or
key-value message store for message history: writes are heavy, recent history is hot, and the main read shape is
a conversation range for history or cursor catch-up.

| Data | Key / access shape |
| --- | --- |
| User/profile/settings | `userId` |
| Connection registry | `userId -> connected chat-server/device IDs` |
| 1:1 message | `(conversationId, sequence)` plus globally unique `messageId` |
| Group message | `(channelId, sequence)`; partition by `channelId` |
| Per-device cursor | `(userId, deviceId, conversationId) -> lastAppliedSequence` |
| Presence | `userId -> online, lastActiveAt, heartbeat expiry` |

For a conversation, the write owner must assign the durable append order. Route a conversation to one
partition/leader and allocate a per-conversation `sequence`, or use a store with a conditional append
position. `messageId` is globally unique for identity and deduplication, but is not by itself a strict canonical
order when two chat servers append concurrently. Do not order solely by `created_at`: simultaneous writes
can share a timestamp or arrive out of order.

## One-to-one message flow

![Message state and sync flow](./assets/chat-message-flow.svg)

1. Sender sends `SEND_MESSAGE(clientMessageId, conversationId, body)` to its connection-owning chat server.
2. Server authenticates membership, returns the existing result for a retried `clientMessageId`, and routes a
   new command to the conversation's append owner if necessary.
3. The append owner writes the ordered message and recipient-delivery record in one durable transaction or
   records an outbox event with the message.
4. Only after that write does the sender receive `ACK(clientMessageId, messageId)`.
5. A delivery worker finds each recipient device's current chat-server owner and pushes to active WebSockets.
6. Offline devices receive a notification wake-up; all devices use durable history to catch up after cursor.
7. Receipt/read state is separate from message acceptance. Delivery can replay after failure, so devices
   deduplicate by `messageId`.

Treat the recipient sync queue as a durable per-recipient delivery stream/inbox, not necessarily a physical
Kafka topic for every user. Its job is to decouple persistence from live delivery and to give devices a
catch-up source. The durable message record/outbox is still the acceptance authority; the stream can replay.

## Multiple devices and synchronization

Each device tracks a cursor for each conversation, such as `lastAppliedSequence`. On connection or reconnect,
it asks for messages after that sequence for the conversations it belongs to. A message is new when its
conversation sequence is greater than that device's stored cursor for the conversation.

Never use one cursor for the whole account: a phone and laptop can be at different points, and each
conversation has its own order. The server can fan out live events to all connected devices, while a
reconnecting device pulls missed durable history. This gives at-least-once delivery; `messageId` makes replay
safe.

## Small group chat

For a small capped group, fan out one message to each member's sync stream/inbox. This makes read and device sync simple because every recipient consumes its own inbox.

```text
group message -> persist once by channelId -> fan out references to group members' inboxes
```

This is suitable only because group size is bounded. For large communities, per-member copies make one send far too expensive; use a channel log plus read-time pull or a hybrid approach. State that boundary clearly rather than pretending the small-group solution scales indefinitely.

## Presence

Presence is soft state, not proof that a user will read a message.

1. On WebSocket connect, store `online=true` and `lastActiveAt` with a TTL/expiry in the presence store.
2. Client sends periodic heartbeats; each heartbeat extends the expiry.
3. Explicit logout marks offline immediately.
4. A missing heartbeat beyond the grace window marks offline, avoiding UI flapping on short network drops.
5. Presence service publishes changes to interested friends over WebSocket.

For small friend lists, publish status changes to subscribers. For very large groups, do not fan out every online/offline change; fetch presence when a user opens the group or refreshes the member list.

## Failure modes and guarantees

| Failure | Handling |
| --- | --- |
| Chat server dies | Service discovery stops assigning it; clients reconnect to a new server and sync durable history after each cursor |
| Append succeeds but ACK is lost | Sender retries the same client message ID and receives the already-created message result |
| Delivery worker, push, or stream fails | Outbox/stream can replay; recipient deduplicates `messageId` or catches up after its cursor |
| Connection registry is stale | Final push fails, worker re-reads the current owner; reconnect/history sync closes the gap |
| Presence disconnect flaps | Heartbeat grace window instead of immediate offline state |
| Notification provider fails | Notification retries/DLQ; message history is still available when app opens |
| Group grows beyond bound | Move from per-recipient inbox copies to a channel log/read-time strategy |

Use **at-least-once delivery with per-conversation ordering** as the base answer. Global total ordering across
all chats is unnecessary and expensive.

## What to leave as extensions

- Media upload, object storage, compression, thumbnails, and CDN delivery.
- End-to-end encryption and key management.
- Full-text message search.
- Large public channels/Discord-scale fanout.
- Client-side cache and geo-edge acceleration.

Mention them only if time remains. The core interview design is WebSocket connection ownership, durable ordered messages, reconnect sync, small-group fanout, and presence.

## Further reading

- [Hello Interview: requirements gathering](https://www.hellointerview.com/blog/system-design-requirements)
- [Hello Interview: real-time updates](https://www.hellointerview.com/learn/system-design/patterns/realtime-updates)
- [RFC 6455: the WebSocket protocol](https://www.rfc-editor.org/rfc/rfc6455)

## Quick recall

**Q. Why WebSocket rather than HTTP polling?**  
A. It gives a persistent bidirectional channel, so servers can push messages without repeated empty requests.

**Q. Why are chat servers stateful?**  
A. Each active WebSocket connection is owned by a specific server until disconnect; API servers remain stateless.

**Q. What does service discovery return?**  
A. A healthy chat-server endpoint selected using location and connection capacity.

**Q. How is message order defined?**  
A. By the per-conversation sequence assigned by its append owner; `messageId` identifies and deduplicates it.

**Q. How does a second device catch up?**  
A. It reads durable messages after its own stored sequence for each conversation.

**Q. Why is per-member group fanout acceptable here?**  
A. The group is capped at 100; it would not be acceptable for a huge public channel.
