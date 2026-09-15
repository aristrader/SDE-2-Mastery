---
order: 10
search: false
---

# Final exercise — Notification LLD

## Exercise: notification-lld - Add A Channel

### Goal

Extend the Notification LLD without changing existing channel senders.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Scenario

An OTP request already sends Email, SMS, or Push through a channel-specific sender. Product now asks for
WhatsApp delivery to the same phone number. The caller should add `WHATSAPP` to the request's channel
list; it must not select a provider or call a WhatsApp-specific service directly.

### Task

Add a `WHATSAPP` channel.

### Constraints

- Keep the existing synchronous, in-memory design. Do not add a queue, retry system, database, or real
  HTTP client.
- Reuse `Recipient.phoneNumber` as the WhatsApp destination for this exercise.
- Follow the existing separation: the sender validates the channel endpoint; the provider client owns
  the vendor call; `NotificationService` only orchestrates.

### Acceptance criteria

- Add `WHATSAPP` to `Channel`.
- Add a `WhatsAppProviderClient` boundary and a console implementation that returns a successful
  `SendResult` for the new channel.
- Add `WhatsAppNotificationSender` that returns a failure result when the request has no phone number.
- Register the sender in `ChannelRouter` and include `WHATSAPP` in the runner's request.
- Keep `NotificationService`, the existing senders, and the `NotificationSender` interface unchanged.

### Expected behavior

For a request containing `EMAIL` and `WHATSAPP`, `NotificationService.send` returns two results in that
order. If the recipient has no phone number, the Email result can still succeed while the WhatsApp result
reports a channel-specific failure.

### What this tests

- Whether the variation is modeled at the channel boundary rather than as a new `if/else` in the service.
- Whether a provider dependency stays behind the sender/provider-client seam.
- Whether one channel's invalid destination is represented as its own `SendResult`.

For the design reasoning behind the extension, see [Notification LLD Design](../design/).
