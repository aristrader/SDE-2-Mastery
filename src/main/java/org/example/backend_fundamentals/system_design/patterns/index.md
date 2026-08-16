---
order: 30
---

# Patterns

System-design questions are rarely blank-canvas problems. First identify the pressure, then choose the smallest pattern that addresses it and explain the cost it adds. Most real designs combine two or three patterns.

| Pressure in the prompt | Start here | Typical examples |
|---|---|---|
| Users must see changes without repeatedly asking | [Real-time updates](/system_design/patterns/realtime_updates/) | Chat, notifications, live comments |
| Work takes too long for an HTTP request | [Long-running tasks](/system_design/patterns/long_running_tasks/) | Transcoding, exports, bulk jobs |
| Two requests can claim the same resource | [Contention](/system_design/patterns/contention/) | Tickets, inventory, driver assignment |
| Same durable data is requested repeatedly | [Scaling reads](/system_design/patterns/scaling_reads/) | News feeds, product pages, timelines |
| One store cannot absorb write volume | [Scaling writes](/system_design/patterns/scaling_writes/) | Events, feeds, counters, telemetry |
| Users upload or download large files | [Large blobs](/system_design/patterns/large_blobs/) | Video, documents, images |
| A business flow spans time or services | [Multi-step processes](/system_design/patterns/multi_step_processes/) | Payments, onboarding, ride offers |
| Query means "near this location" | [Proximity services](/system_design/patterns/proximity_services/) | Ride-sharing, local delivery |

Use the [scaling-pattern mental map](/system_design/concepts/scaling_patterns/) after drawing the simple architecture. It helps choose the next deep dive without turning a design interview into a list of technologies.

## Pattern composition

```text
video platform
  -> large blobs for upload/download
  -> long-running tasks for transcoding
  -> real-time updates for processing status
  -> multi-step process only if retries/timeouts across stages need durable coordination
```

Start simple. A pattern is justified by a concrete latency, throughput, correctness, or failure requirement, not because it is fashionable.

## Quick recall

**Q. How many patterns should I lead with in an interview?**
A. Usually one core path and two focused deep dives. Add others only when a requirement exposes their pressure.

**Q. What makes an answer sound experienced?**
A. Name the pressure, describe the data/control flow, state a trade-off, and explain failure recovery.
