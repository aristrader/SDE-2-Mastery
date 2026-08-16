---
order: 30
---

# Real-Time Updates

Use this pattern when the server must initiate delivery after the original request has completed: chat messages, notifications, a driver moving on a map, or job progress. A normal request/response API is still best for ordinary commands and reads.

## Choose the delivery mechanism

| Need | Start with | Move up when | Cost |
|---|---|---|---|
| Updates can be seconds late | Polling | Poll volume or latency becomes unacceptable | Wasted requests and delayed visibility |
| Server only pushes events to client | SSE | Client must also send frequent low-latency messages | One-way connection, reconnect handling |
| Bi-directional, low-latency interaction | WebSocket | The product truly needs it | Connection lifecycle, fan-out, backpressure |
| Mobile device may be offline | Push notification plus refresh | User opens the app | Delivery is not a durable source of truth |

Do not select WebSockets by default. Start with polling when updates are infrequent or a few seconds late is acceptable. SSE is simpler for server-to-client streams; WebSockets fit chat, collaboration, and interactive live sessions.

## Backend flow

```text
command -> durable state change -> outbox/event -> delivery service -> connected client
                                                          -> offline push notification
client reconnect/refresh -> GET authoritative state
```

Persist the business state before announcing it. A push, pub/sub message, or WebSocket event can be delayed or lost; the client must recover by fetching authoritative state from the service.

## Fan-out and failure handling

- A delivery service keeps a mapping from user/channel to active connections. For heavy stateful sessions, route the same user or document to the same connection owner with consistent hashing, or keep the mapping in a shared store.
- Use pub/sub to distribute an event from the producing service to the connection owner. It decouples producers from thousands of clients, but it does not itself guarantee a user saw the update.
- Include an event ID or sequence number. Clients deduplicate replays and detect gaps after reconnecting.
- Bound per-connection buffers. Slow clients must be disconnected, dropped to a coarser update, or told to refresh rather than consuming unbounded memory.

## Interview delivery

Say: "The command persists state, emits an event through an outbox, and the delivery tier pushes it to connected clients. The next read remains the source of truth, so reconnects and missed pushes are safe."

Do not spend time on WebSocket frame internals unless asked. Explain protocol choice, connection ownership, fan-out, and recovery.

## Quick recall

**Q. Why is a push event not the source of truth?**
A. Connections and notifications are lossy. Durable state and a refresh API repair missed or duplicated events.

**Q. When is polling sufficient?**
A. When updates are infrequent or seconds of delay are acceptable; it is the simplest operational choice.
