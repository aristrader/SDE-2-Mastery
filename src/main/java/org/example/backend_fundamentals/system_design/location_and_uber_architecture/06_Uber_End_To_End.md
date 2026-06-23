# Location Systems Roadmap — Part 6: Uber End-To-End

This document outlines the complete end-to-end ride lifecycle, scaling techniques, and clarification of common misconceptions regarding regional matching.

## End-To-End Ride Lifecycle

### 1. Driver Location Updates
- Drivers continually send location updates (latitude, longitude, timestamp) every few seconds.
- The **Location Service** processes these events and updates a **Redis** store with: `driverId`, `latitude`, `longitude`, `geohash`, `lastSeen`, and `status`.
- This provides a live view of available drivers in the system.

### 2. Passenger Ride Request
- The passenger requests a ride by providing pickup and destination locations.
- The backend computes the **Geohash** for the passenger's pickup location.

### 3. Candidate Discovery
- The system queries the passenger's current Geohash cell and its 8 neighbor cells to find nearby drivers, resolving the boundary problem.
- This narrows the search space but usually returns too many candidates.

### 4. Bounding Box & Haversine Filtering
- A **Bounding Box** is created around the passenger to filter out irrelevant candidates.
- The system then calculates actual straight-line distances using the **Haversine formula** to further refine the candidate list.

### 5. ETA Calculation
- *Correction:* The closest driver by straight-line distance is not necessarily the best driver. Final driver selection requires ETA calculations because distance does not equal driving time.
- A **Routing Engine** uses a road graph (intersections as nodes, roads as edges, travel time as weights) and an algorithm like **A\*** to compute the actual ETA for the candidate drivers.

### 6. Driver Ranking
- Drivers are ranked primarily by ETA. Additional business logic factors such as acceptance/cancellation rates and surge pricing may also be considered.
- A subset of top drivers (e.g., Top 5) is selected.

### 7. Driver Reservation & Offer
- **Concurrency Protection:** The system requires an atomic state transition (`AVAILABLE` → `RESERVED`) to prevent multiple ride requests from assigning the same driver.
- Ride offers are sent to the top drivers. If a driver accepts, they win the ride, and other reservations are released. If they reject or time out, the system falls back.

### 8. Ride Assignment
- Once accepted, the driver's state transitions from `RESERVED` → `ASSIGNED`.
- A ride entity is created containing `RideId`, `PassengerId`, and `DriverId`.

### 9. Trip Progress & Completion
- **Travel to Pickup:** The driver continues sending location updates. The passenger receives real-time location and ETA updates.
- **Trip Starts:** When the passenger is picked up, the driver's state becomes `ON_TRIP`. The matching service no longer considers this driver for new rides.
- **During Trip:** Location updates continue for passenger tracking, ETA updates, fraud detection, and trip monitoring.
- **Trip Ends:** Upon reaching the destination, the driver's state transitions back to `AVAILABLE` (or `OFFLINE` if they log out).

### Offline Driver Handling
- The system monitors the `lastSeen` timestamp of drivers.
- If a driver misses updates for a threshold (e.g., 30 seconds), their status becomes `STALE`, and they are removed from the active matching pool.

## System Scaling: Clusters and Regional Matching

Scaling the system beyond a single city requires handling massive concurrent updates and ride requests.

### Redis Cluster vs. Matching Cluster
- **Redis Cluster:** Relates to infrastructure scaling. Multiple Redis nodes work together to store more data, handle higher traffic, and provide redundancy.
- **Matching Cluster:** Relates to application-level workload partitioning (e.g., Bangalore Matching Service, Mumbai Matching Service).

### Logical vs. Physical Partitioning
- *Correction:* Regional matching clusters (e.g., "Bangalore Cluster") are **logical partitions**, not necessarily physical servers deployed in those specific cities.
- Multiple logical matching services can run in a single datacenter (e.g., AWS Mumbai Region). Traffic is routed conceptually (e.g., if city == Bangalore, route to Bangalore Matching Service). 

### Why Regional Matching Exists
- *Correction:* Regional matching does **not** exist to improve geospatial search; Geohash already solves spatial indexing and search space reduction.
- **Geohash** handles **Data Partitioning** for location search.
- **Regional Matching** handles **Workload Partitioning** for ride assignments. It reduces the load of state management, driver reservation, assignments, and offer dispatching by localizing the workload to a region.
