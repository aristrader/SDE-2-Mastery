---
order: 10
search: false
---

# Notification LLD Exercise

## Exercise: notification-lld - Add A Channel

### Goal

Extend the Notification LLD without changing existing channel senders.

### Task

Add a `WHATSAPP` channel.

### Acceptance criteria

- Add a channel enum value.
- Add a provider-client interface or implementation.
- Add a `NotificationSender` implementation.
- Register it in `ChannelRouter`.
- Keep `NotificationService` unchanged.
