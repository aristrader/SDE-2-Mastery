---
order: 10
search: false
---

# Notification System Exercise

## Exercise: notification-system-hld - Design Multi-Channel Notifications

### Goal

Practice the HLD answer for a notification system that sends push, SMS, and email.

### Task

Design the system with:

- producer services and scheduled jobs
- notification API servers
- user/device/settings/template storage
- per-channel queues
- worker pools
- third-party providers
- retry, dedupe, rate limiting, monitoring, and analytics

### Acceptance criteria

- Explain why the API returns `202`.
- Explain why queues sit after the notification API in the base design.
- Include per-channel queue isolation.
- Include a durable notification log or outbox.
- State the delivery guarantee as at-least-once with idempotency/dedupe.
- Call out queue-depth/consumer-lag monitoring.
- Mention the LLD split: type vs channel vs provider.
