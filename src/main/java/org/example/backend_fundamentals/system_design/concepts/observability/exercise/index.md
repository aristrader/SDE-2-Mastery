---
order: 10
search: false
---

# Exercise

## Exercise: observability-signal - Diagnose a degraded request path

An alert reports that 8% of checkout requests fail over ten minutes. A dashboard also shows higher database
latency and a growing asynchronous queue.

1. State the first SLI question you would verify.
2. Name one log field and one trace field needed to connect a failed request across services.
3. Name two metrics that distinguish a database bottleneck from a consumer backlog.
4. Give one safe mitigation before root-cause analysis is complete.

## Answer shape

Begin with user impact and the affected operation. Use a trace/correlation ID to narrow the path, then use
queue age, consumer rate, database latency, and connection saturation to test the hypothesis. Mitigate
first, then prove recovery with the same SLI.

## Quick recall

**Q. Why are metrics alone not enough for one failed request?**
A. They show aggregate behaviour; a trace or correlated logs reveal that request's causal path.
