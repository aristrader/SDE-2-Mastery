---
order: 10
search: false
---

# Chat System Exercise

## Exercise: chat-system-hld - Design Real-Time Chat

### Goal

Give a complete HLD answer for a Messenger-style system with one-to-one chat, groups of at most 100 members, multiple devices, presence, and offline notification.

### Task

Design the request and message flows. Cover:

- HTTP versus WebSocket responsibilities
- service discovery and chat-server connection ownership
- durable message storage and per-conversation ordering
- sender idempotency and recipient dedupe
- online and offline routing
- per-device catch-up cursor
- small-group inbox fanout
- heartbeat-based presence

### Acceptance criteria

- Explain why a chat server is stateful while API servers are stateless.
- State the message guarantee as at-least-once with `messageId` dedupe.
- Do not use timestamps alone for ordering.
- Make durable history, not push notification, the offline correctness path.
- Bound the group fanout solution to small groups and name the large-group alternative.
- Explain how a client reconnects after its chat server fails.

