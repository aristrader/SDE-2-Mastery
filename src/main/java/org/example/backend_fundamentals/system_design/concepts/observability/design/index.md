---
order: 20
search: false
---

# Design

## From symptom to cause

```mermaid
flowchart LR
    SLI[Checkout error-rate SLI breaches] --> Alert[Actionable alert]
    Alert --> Trace[Trace or correlation ID]
    Trace --> Span[Slow dependency span]
    Span --> Metrics[Confirm queue age / latency / saturation]
    Metrics --> Mitigation[Rollback, shed load, or disable optional path]
    Mitigation --> Verify[Verify SLI recovery]
```

Logs carry detailed event context, metrics reveal aggregate change, and traces connect one request across
boundaries. The flow starts with user impact so a noisy infrastructure signal does not become an incident
by itself.
