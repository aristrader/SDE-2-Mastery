---
order: 20
search: false
---

# Design

## Design Uber / Ride-Sharing

This is the interview blueprint for a ride-sharing backend. Keep the first version focused on ride request, nearby driver discovery, matching, trip lifecycle, and tracking. Payments, fraud, promotions, and support can be follow-ups.

## Clarifying questions

- Are we designing rider booking only, or driver onboarding, payments, pricing, and support too?
- Is this one city, one country, or global multi-region?
- Do riders need real-time driver tracking?
- How often do drivers send location updates?
- Do we optimize by nearest distance, ETA, driver rating, vehicle type, or price?
- Can a driver reject a ride?
- Do we need scheduled rides, pooled rides, surge pricing, or cancellations?

Reasonable scope:

```text
Riders request rides.
Drivers publish live location.
System finds nearby available drivers.
System assigns one driver and tracks trip state.
Rider sees ETA and driver movement.
```

## Functional requirements

- Driver can go online/offline.
- Driver app sends location updates every few seconds.
- Rider requests a ride from pickup to drop-off.
- System finds candidate available drivers near pickup.
- System assigns/reserves a driver.
- Rider and driver can see trip status.
- Trip moves through states: requested, assigned, arrived, started, completed/cancelled.

## Non-functional requirements

- Low-latency matching: rider should get candidates/assignment quickly.
- Fresh driver locations: stale drivers must not be matched.
- High write throughput: driver location updates are frequent.
- High availability: matching should degrade by region, not globally fail.
- Consistency where it matters: one driver should not be assigned to two rides at the same time.
- Regional locality: a Bangalore ride should not depend on a US region hot path.

## Back-of-envelope estimation

Use simple round numbers in an interview:

```text
1M active drivers
location update every 5 seconds
location updates = 1M / 5 = 200K writes/sec

100K ride requests/min at peak
ride requests = ~1.7K/sec
```

Design implication:

| Estimate | Implication |
|----------|-------------|
| Location writes dominate | Store latest active locations in Redis / in-memory spatial index |
| Matching reads need low latency | Query nearby cells, not the full driver table |
| Trip state changes are fewer but critical | Persist trip state in durable DB |
| Region traffic is local | Partition by city/region/geospatial cell |

Do not store every GPS update in the primary relational DB hot path. Keep latest state in a fast store, and stream history asynchronously for analytics/audit if needed.

## APIs

```text
POST /drivers/{driverId}/status
body: { status: "AVAILABLE" | "OFFLINE" }

POST /drivers/{driverId}/location
body: { lat, lon, timestamp }

POST /rides
body: { riderId, pickupLat, pickupLon, dropoffLat, dropoffLon, vehicleType }

POST /rides/{rideId}/accept
body: { driverId }

POST /rides/{rideId}/events
body: { event: "ARRIVED" | "STARTED" | "COMPLETED" | "CANCELLED" }

GET /rides/{rideId}
```

## Data model

Durable tables:

```text
Driver(driver_id, status, vehicle_type, rating)
Rider(rider_id, ...)
Ride(ride_id, rider_id, driver_id, pickup, dropoff, status, created_at)
RideEvent(ride_id, event_type, timestamp, actor_id)
```

Hot location store:

```text
driver:{driverId} -> {lat, lon, geohash/h3_cell, status, lastSeen}
cell:{h3_cell} -> set(driverIds)
```

The cell index can be Geohash, H3, or S2. The key idea is the same: turn nearby search into "look in this cell and neighboring cells", then rank candidates by exact distance/ETA.

## High-level architecture

```text
Driver App -> Location Ingest -> Active Driver Store / Spatial Index
                                      |
Rider App  -> Ride Service -> Matching Service -> Candidate drivers
                                      |
                              Trip Service / DB
                                      |
                              Notification / Push
```

Core components:

- **Location ingest:** accepts frequent driver updates.
- **Active driver store:** keeps only latest searchable driver state.
- **Spatial index:** maps cells to available drivers.
- **Matching service:** fetches candidates, computes ETA/rank, reserves one driver.
- **Trip service:** owns ride state machine.
- **Notification service:** sends assignment/status updates.

## Main flows

### Driver location update

1. Driver sends `lat/lon/timestamp`.
2. Server validates driver is online or on-trip.
3. Compute geospatial cell.
4. Update `driver:{driverId}` latest location.
5. If cell changed, remove driver from old cell set and add to new cell set.
6. Emit async event for analytics/history if required.

### Ride request and matching

1. Rider creates ride request.
2. Matching service computes pickup cell and neighboring cells.
3. Fetch available drivers from those cells.
4. Filter stale drivers using `lastSeen`.
5. Compute distance/ETA for candidates.
6. Rank by ETA, vehicle type, driver status, and business rules.
7. Reserve one driver atomically.
8. Notify driver and rider.

### Trip lifecycle

```text
REQUESTED -> RESERVED -> ASSIGNED -> ARRIVED -> ON_TRIP -> COMPLETED
                      \-> CANCELLED
```

Use explicit state transitions. Avoid "status string changed anywhere" because ride systems become messy quickly.

## Deep dives

### Nearby search

Do not scan all drivers. Use a geospatial index:

```text
pickup lat/lon -> cell -> current + neighbor cells -> candidate drivers
```

Then filter/rank:

```text
candidates -> exact distance -> route ETA -> assignment score
```

Geohash is easy to store and query by prefix. H3/S2 are often better for production grids. Quadtree is useful conceptually, but a huge distributed quadtree is hard to update when drivers move every few seconds.

### Driver reservation

Two ride requests can race for the same driver. The reservation must be atomic:

```text
reserve driver if status == AVAILABLE
set status = RESERVED for rideId
```

This can be done with a DB conditional update, Redis Lua script, or a per-driver ownership service. The key is compare-and-set semantics.

### Stale drivers

Do not match a driver who stopped sending GPS updates. Store `lastSeen` and remove drivers whose update age exceeds the threshold.

```text
if now - lastSeen > 15s:
  remove from available pool
```

TTL can help, but an explicit cleanup/state transition is easier to reason about in interviews.

## Failure modes

| Failure | Handling |
|---------|----------|
| Location updates delayed | Mark stale after threshold; do not match stale drivers |
| Driver reservation race | Atomic compare-and-set on driver status |
| Matching service down | Retry in same region; return graceful failure to rider |
| Notification lost | Trip state remains source of truth; clients poll/refresh ride state |
| Region outage | Route users to nearby healthy region only if data/driver pool can serve them |

## Monitoring

- location update QPS and lag
- active drivers per cell/city
- stale driver count
- ride request rate
- match success rate
- assignment latency p50/p95/p99
- driver acceptance/rejection rate
- cancelled rides by reason
- hot cells / overloaded regions

## Tradeoffs

| Choice | Benefit | Cost |
|--------|---------|------|
| Redis active-driver store | Very low latency | Volatile; needs recovery/refresh path |
| Geohash/H3/S2 cells | Fast candidate retrieval | Boundary and hot-cell issues |
| Route by ETA | Better user experience | Needs routing service and traffic data |
| Strong reservation consistency | Avoids double assignment | Adds coordination in hot path |
| Regional matching | Low latency and simpler failure domains | Cross-region rides/data become harder |

## Quick recall

**Q. What is the hot path?**  
A. Driver location updates and nearby-driver matching.

**Q. Why not store every location update in the DB?**  
A. Location writes are too frequent; keep latest active state in memory and stream history async.

**Q. How do you find nearby drivers?**  
A. Use Geohash/H3/S2 cells to fetch candidates, then compute exact distance/ETA.

**Q. What consistency problem matters most?**  
A. Atomic driver reservation so one driver is not assigned to two rides.

**Q. What makes a driver stale?**  
A. `lastSeen` exceeds a freshness threshold, so remove from available matching pool.
