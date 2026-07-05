---
order: 30
---

# Client ↔ Server Communication Patterns

## Push vs Pull (The Foundation)

### Pull-Based Communication
The client repeatedly asks the server for updates. The server responds either immediately (short polling) or holds the connection until data is available (long polling).
- **Examples**: Refreshing an inbox, polling order status.
- **Techniques**: Short Polling, Long Polling.

### Push-Based Communication
The client subscribes once, and the server pushes updates as they occur.
- **Examples**: WhatsApp messages, live sports scores, trading dashboards.
- **Techniques**: Server-Sent Events (SSE), WebSockets, Webhooks (server-to-server).

---

## Short Polling vs Long Polling

### Short Polling
Client repeatedly sends HTTP requests at fixed intervals (e.g., every 5 seconds).
- **Pros**: Extremely simple to implement, works everywhere.
- **Cons**: Massive waste of resources (empty responses), high latency (up to the polling interval). 

### Long Polling
Client sends an HTTP request. If the server has no new data, it **holds the connection open** rather than responding immediately. Once data arrives, the server responds, and the client immediately opens a new long-polling request.
- **Why it's better**: Eliminates useless empty requests. Latency is minimized because the server pushes data over the open connection the moment it's available.
- **Mental Model**: Fake push built on top of HTTP.

---

## Server-Sent Events (SSE)

One-way streaming from Server → Client over a single, long-lived HTTP connection.
- **Protocol**: Standard HTTP.
- **Direction**: Unidirectional (Server → Client).
- **Use Cases**: Live score updates, stock tickers, notifications feeds, live dashboards.

If the client needs to send data back, it uses standard, separate HTTP requests.

---

## WebSockets

Full duplex, bidirectional communication over a long-lived TCP connection.
- **Protocol**: WebSocket (ws:// or wss://), initiated via an HTTP Upgrade handshake.
- **Direction**: Bidirectional (Server ↔ Client). Both sides can push data independently.
- **Use Cases**: Chat applications (WhatsApp, Discord), multiplayer games, live collaboration tools, trading platforms.

### SSE vs WebSockets
| Feature | SSE | WebSocket |
|----------|----------|----------|
| **Direction** | Server → Client | Both directions |
| **Protocol** | HTTP | WebSocket |
| **Complexity** | Simple | More complex |
| **Best fit** | Live dashboards, feeds | Chat apps, games, trading |

---

## Webhooks

Webhooks are **server-to-server push** notifications. Instead of a consumer service polling a producer service for state changes, the producer makes an HTTP POST request to the consumer's registered callback URL.

- **Polling vs Webhooks**: Polling is "Are we there yet?" (Pull). Webhooks are "I'll call you when we get there" (Push).
- **WebSockets vs Webhooks**: WebSockets are for Client ↔ Server (browser to backend) real-time streaming. Webhooks are for Server ↔ Server event notifications (e.g., Stripe payment success).

---

## Protocols and HTTPS Details

### Are Webhooks Just HTTPS Calls?
Yes, in practice, almost always. The defining idea of a webhook is not a special transport protocol, but the architectural pattern: Producer notifies Consumer (Push) instead of Consumer asking Producer (Pull/Polling).

### Is SSE a Protocol?
Not really. SSE is a streaming mechanism built directly on top of HTTP. The client requests with standard HTTP, the server responds with `Content-Type: text/event-stream`, and the connection stays open to stream events.

### Is WebSocket a Protocol?
Yes. The connection starts as HTTP (`Upgrade: websocket`). The server responds with `101 Switching Protocols`, and the connection becomes a separate WebSocket protocol connection, allowing bidirectional communication.

---

## Quick recall

**Q. Short polling vs Long polling?**
A. Short polling repeatedly asks and gets empty responses. Long polling asks once, and the server holds the connection open until data is ready, returning immediately when it is.

**Q. When to choose SSE over WebSockets?**
A. Choose SSE when communication is strictly one-way (Server → Client) like a live dashboard. Choose WebSockets for bidirectional real-time communication like a chat app.

**Q. WebSocket vs Webhook?**
A. WebSockets are long-lived bidirectional connections typically between a browser and a server. Webhooks are one-off HTTP POST requests from one server to another triggered by an event.

**Q. Is long polling push or pull?**
A. It is fundamentally pull (client initiates the request), but acts as a "fake push" because the server holds the response until data is ready.
