---
search: false
---

# Candidate discussion — Notification extension

Start with the existing channel/provider separation rather than redesigning the whole system:

> “I’ll add WhatsApp as another delivery channel. The caller will request a channel, while the existing
> routing and provider boundary choose how that channel is sent.”

## Questions to ask

1. Is WhatsApp an additional channel on one request, or a replacement for an existing channel?
2. Does the caller choose a provider, or should provider selection remain hidden behind the sender?
3. Can the existing phone number serve as the WhatsApp destination?
4. If one requested channel has no usable destination, should other channels still be attempted?
5. Are queues, retries, persistence, real HTTP calls, and provider fallback in scope for this extension?

## Agreed scope for this exercise

- Add WhatsApp as a channel while preserving the existing `NotificationService` and sender interface.
- Reuse the recipient phone number as the WhatsApp destination.
- Return one channel-specific result per requested channel; an invalid WhatsApp destination must not stop
  Email from succeeding.
- Keep the extension synchronous and in memory with a console provider client.
- Queues, retries, persistence, real vendor clients, and fallback providers are follow-ups.

The final [exercise](../) records the implementation boundaries and expected behavior.
