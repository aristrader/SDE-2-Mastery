---
order: 20
search: false
---

# Design

## Protected downstream call

```mermaid
stateDiagram-v2
    [*] --> Closed
    Closed --> Open: eligible failures cross threshold
    Open --> HalfOpen: recovery wait elapses
    HalfOpen --> Closed: limited probes succeed
    HalfOpen --> Open: probe fails
```

Each allowed call has a bounded timeout. Retrying is only safe when the operation is idempotent or its
outcome can be reconciled. An open circuit fails fast; a bulkhead separately limits the resources this
dependency can consume.
