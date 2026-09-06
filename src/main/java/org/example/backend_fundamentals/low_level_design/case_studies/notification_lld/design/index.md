---
order: 20
search: false
---

# Notification LLD Design

## Responsibilities

| Class | Role |
| --- | --- |
| `NotificationService` | Orchestrates validation, rendering, routing, sending |
| `TemplateRenderer` | Converts request + channel into a message |
| `ChannelRouter` | Finds the right sender for a channel |
| `NotificationSender` | Strategy interface for channel senders |
| `EmailProviderClient` / `SmsProviderClient` / `PushProviderClient` | Vendor adapters |

## Flow

```text
request
  -> validate recipient/channel data
  -> render channel-specific message
  -> find sender by channel
  -> sender calls provider client
  -> collect per-channel SendResult
```

## Boundaries

- `NotificationService` does not know SendGrid/Twilio/Firebase APIs.
- Channel senders do not decide templates.
- Provider clients do not know notification type.
- Each channel returns its own `SendResult`.

## Quick recall

**Q. Why not put email/SMS/push if-else inside the service?**
A. It couples orchestration to every channel and vendor. Strategy + router keeps it replaceable.

**Q. What changes when adding WhatsApp?**
A. Add `WHATSAPP`, a sender, a provider client, and template support. Existing senders stay unchanged.
