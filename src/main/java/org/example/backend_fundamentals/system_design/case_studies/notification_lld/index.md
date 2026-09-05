---
order: 60
---

# Notification LLD

Low-level design for a notification system that can send Email, SMS, and Push/App notifications through replaceable third-party providers.

For the high-level architecture, queues, workers, retries, settings, and monitoring, see `system_design/case_studies/notification_system/`.

## Core idea

Keep these concepts separate:

| Concept | Meaning | Example |
| --- | --- | --- |
| Notification type | Why we are notifying | OTP, payment success, KYC approved |
| Channel | How the user receives it | Email, SMS, Push |
| Provider | Vendor/integration that sends it | SendGrid, Twilio, Firebase |

## Design shape

```text
NotificationService
  -> TemplateRenderer
  -> ChannelRouter
  -> NotificationSender
  -> ProviderClient
```

Patterns:

- **Strategy:** `NotificationSender` per channel.
- **Adapter:** provider clients hide vendor APIs.
- **Registry:** `ChannelRouter` maps channel to sender.
- **Facade:** `NotificationService` is the simple entry point.

## Run

```bash
mvn -q exec:java -Dexec.mainClass="org.example.backend_fundamentals.system_design.case_studies.notification_lld.playground.NotificationLldRun"
```

## Add only if asked

- Retry transient provider failures.
- Fallback provider: Twilio fails, use another SMS provider.
- Idempotency: avoid sending the same OTP/payment message twice.
- User preferences: some users disable SMS/email.
- Rate limiting: prevent spam/abuse.
- Async queue: enqueue notification job instead of sending synchronously.
- Audit log: store request/result for compliance/debugging.
- Priority: OTP > marketing.

## Quick recall

**Q. What is the main split?**
A. Type is business intent, channel is delivery method, provider is vendor integration.

**Q. Where does vendor code live?**
A. Behind provider-client adapters, not in `NotificationService`.

**Q. Which class coordinates sending?**
A. `NotificationService`.
