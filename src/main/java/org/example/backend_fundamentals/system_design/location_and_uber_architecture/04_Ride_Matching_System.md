# Location Systems Roadmap — Part 4: Nearby Driver System

This section covers the transition from spatial indexing theory to practical implementation: how ride-sharing systems like Uber find nearby drivers and assign rides to passengers.

## Driver Location Updates

Drivers continuously send location updates (heartbeats) to the backend containing `driverId`, `latitude`, `longitude`, and a `timestamp`. 

Updates typically happen every 3–10 seconds. This frequency balances the need for location freshness with the cost of handling millions of writes per second.

### Active Driver Storage
Because searches happen constantly and require extremely fast lookups, the current state of driver locations is stored in an in-memory datastore like Redis. Databases are reserved for history, analytics, and auditing, often populated asynchronously via event streams (e.g., Kafka).

**Misconception**: Redis stores history.
**Correction**: Redis generally stores only the latest state. Previous locations are overwritten with each new update.

## Driver State Management

Matching systems do not search against every driver using the app. They only search against **AVAILABLE** drivers.

**Misconception**: All drivers using the app are searchable.
**Correction**: An `ON_TRIP` driver might still send GPS updates, but they should not appear in matching searches. Different subsystems care about different states (e.g., the tracking and ETA systems care about `ON_TRIP` drivers, while the matching service cares about `AVAILABLE` drivers).

Many production systems model this using explicit state machines rather than complex if-else chains:
`OFFLINE` → `AVAILABLE` → `RESERVED` → `ASSIGNED` → `ON_TRIP`

### Stale Driver Detection
If a driver app stops sending updates, the system needs to detect and remove them from the active pool.
Rather than relying purely on Redis TTL (which silently expires keys), a common production pattern is to store a `lastSeen` timestamp. A background worker periodically checks if `now - lastSeen > threshold` and explicitly marks the driver as stale and removes them from the matching pool.

## Real-Time Location Indexing and Candidate Retrieval

Drivers continuously move, leading to millions of location updates. Using a distributed Quadtree for this is difficult because moving a node requires deletion, rebalancing, and cross-machine coordination. Instead, systems typically use **Geohash**, **H3**, or **S2**, where an update simply requires recomputing the hash and updating the location.

**Misconception**: Geohash finds the nearest driver.
**Correction**: Geohash only finds *potentially* nearby drivers. It acts as a fast, coarse filter before more precise filtering is applied.

### The Retrieval Flow
1. **Geohash Filtering**: Compute the passenger's geohash and retrieve drivers in the **Current Cell + Neighbor Cells**.
   *Note: We must search neighbor cells because two nearby points can belong to different geohash cells (the boundary problem).*
2. **Bounding Box**: Create a bounding box around the passenger to filter down the candidate set (e.g., 500 candidates → 100 candidates).
3. **Haversine Distance**: Compute the actual distance between the passenger and remaining drivers to eliminate false positives and finalize the nearby driver list (e.g., 100 candidates → 35 candidates).

## Ride Matching and Dispatch

Once nearby candidates are retrieved, the system must decide which driver gets the ride.

If the initial search radius (e.g., 2 km) yields 0 drivers, the system does not fail immediately. It uses **Progressive Search Radius Expansion** (e.g., 2 km → 5 km → 10 km). Starting with a huge radius is inefficient and leads to a poor customer experience.

### Candidate Ranking
**Misconception**: Pick the geographically nearest driver.
**Correction**: Distance ≠ Travel Time. The nearest driver is not always the best driver. A driver 500m away might have an ETA of 8 minutes, while a driver 700m away might have an ETA of 3 minutes. Drivers should be ranked primarily by **ETA**, along with factors like availability, cancellation/acceptance rates, and surge zones.

### Concurrency and Reservation
To minimize passenger wait times, offers might be dispatched in parallel to multiple highly-ranked drivers (e.g., top 5), where the first acceptance wins and remaining offers are cancelled.

**Concurrency Problem**: If two passengers request rides simultaneously, they might both be matched with the same driver.
**Solution**: Driver assignment must use atomic state transitions (e.g., `AVAILABLE` → `RESERVED`) or distributed locks to ensure a driver can only be reserved once. 

Reservations must include a timeout (e.g., 10 seconds). If a driver does not respond, their state transitions back to `AVAILABLE` and the system tries the next candidate.

## Scaling the Matching Service
To handle millions of drivers and ride requests, a single matching server is insufficient. Systems scale using **City-based Partitioning** (e.g., Bangalore Cluster, Mumbai Cluster). Ride requests are routed to the specific regional cluster to significantly reduce the search space.
