---
order: 10
search: false
---

# Notification System Exercise

## Exercise: notification-system-hld - Design Multi-Channel Notifications

### Goal

Practice the HLD answer for a notification system that sends push, SMS, and email.

## Timed mock

Use a 45-minute timer. Spend the first five minutes clarifying the acceptance/delivery boundary rather than drawing a
queue immediately.

| Time | Produce |
| --- | --- |
| 0–5 min | Scope, non-goals, channel/type policy, and what `202` means |
| 5–10 min | Volume, burst/provider-limit calculation, API, and status model |
| 10–20 min | Intent/delivery/attempt records plus the durable acceptance path |
| 20–32 min | Channel/priority queues, workers, and provider adapters |
| 32–42 min | Retry ambiguity, expiry, stale tokens, preferences, and overload policy |
| 42–45 min | Trade-offs, metrics, and follow-up scope |

### Task

Design the system with:

- producer services and scheduled jobs
- notification API servers
- user/device/settings/template storage
- per-channel queues
- worker pools
- third-party providers
- retry, dedupe, rate limiting, monitoring, and analytics

Begin by defining what the API acceptance response guarantees. Then derive the durable intent/outbox boundary, channel
queues, and retry policy from the fact that provider acceptance, device delivery, and user open are different states.

### Acceptance criteria

- Explain why the API returns `202`.
- Explain why queues sit after the notification API in the base design.
- Include per-channel queue isolation.
- Include a durable notification log or outbox.
- State the delivery guarantee as at-least-once with idempotency/dedupe.
- Distinguish `SENT_TO_PROVIDER` from `DELIVERED` and state when the latter is observable.
- Give different retry/expiry behavior for an OTP and a stale state-refresh notification.
- Call out queue-depth/consumer-lag monitoring.
- Mention the LLD split: type vs channel vs provider.
- Explain why bulk-audience expansion, quiet hours/digests, and an in-app inbox are separate follow-ups rather than
  silently assuming they are solved by the base delivery pipeline.
