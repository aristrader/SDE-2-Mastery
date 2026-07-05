---
order: 40
---

# Rate Limiting

Rate limiting is the process of preventing the frequency of an operation from exceeding a defined limit (e.g., 100 requests/minute, 5 login attempts/minute). 

## Purposes
- **DoS Protection**: Prevents resource starvation caused by Denial of Service attacks by rejecting excess attack traffic.
- **Cost Control**: Creates a virtual cap on resource growth to prevent uncontrolled auto-scaling and large infrastructure bills during traffic spikes.
- **Security**: Mitigates common attacks such as brute force login attempts and OTP SMS abuse.
- **Data Flow Control**: Controls data ingestion speed for APIs processing massive amounts of information.

## Locations for Rate Limiting
- **API Gateway**: The most common location (e.g., Kong, NGINX, AWS API Gateway, Envoy). It effectively rejects excess requests before they reach the backend services.
- **Load Balancer**: Can enforce limits before routing requests to backend services.
- **Application Layer**: Enforces business-specific rules within the application (e.g., a filter in Spring Boot before reaching the controller).
- **Database Layer**: Generally avoided due to performance overhead and expense; it is highly preferable to reject requests before they access the database.

## Algorithms

### Leaky Bucket
Requests enter a queue and are processed at a constant, steady rate (FIFO). If the queue is full, new requests are discarded.
- **Advantages**: Smooths out traffic, preventing bursts from overwhelming downstream systems.
- **Disadvantages**: Delays even legitimate bursts of traffic.

### Token Bucket
A bucket holds up to a maximum capacity of tokens and refills at a constant rate. Each request consumes one token. If no tokens are available, the request is rejected.
- **Advantages**: Allows short bursts of traffic while still preventing long-term sustained abuse.
- **Note**: This is the most commonly preferred algorithm for production APIs.

### Fixed Window
A counter is maintained for a fixed time window (e.g., per minute). Each request increments the counter. If the counter exceeds the threshold, requests are rejected until the next window begins.
- **Limitation (Boundary Problem)**: Traffic can spike severely around window boundaries. For instance, allowing 100 requests at the very end of one window and 100 requests at the very start of the next can result in 200 requests processing almost instantly.

### Sliding Log
Stores exact timestamps of every request. When a new request arrives, expired timestamps are removed, and the remaining requests are counted against the limit.
- **Advantages**: Highly accurate based on exact timestamps.
- **Disadvantages**: Extremely memory-intensive, especially for large systems storing millions of timestamps.

### Sliding Window
A hybrid approach combining Fixed Window and Sliding Log concepts. It uses a weighted contribution from the previous time window to proportionally smooth out traffic bursts.
- **Advantages**: Offers better accuracy than a Fixed Window while maintaining much lower memory usage than a Sliding Log.

## Distributed Rate Limiting
In distributed systems with multiple server nodes, allowing each server to independently enforce limits based on a local counter does not enforce a global limit, as users can bypass the limit by distributing their requests across multiple servers. A shared counter mechanism is required.

### Centralized Datastore (Production Standard)
An in-memory, high-speed datastore like Redis is used to store shared counters (e.g., `user:123 -> 57 requests`). All server nodes read and update this centralized counter. 

### Overcoming Race Conditions
A naive read-modify-write approach to update the shared counter is unsafe because concurrent requests can overwrite each other's updates, leading to lost increments and weakened rate limiting.
- **Solution**: Rely on atomic operations provided by the datastore (e.g., the `INCR` command in Redis). Atomic increments ensure correctness without the bottleneck of distributed locks, providing both high performance and accurate counting.

### Sticky Sessions (Alternative)
Routes a user's requests to the exact same server consistently, allowing that node to track all requests for that user.
- **Disadvantages**: Simple to implement but suffers from reduced fault tolerance (rate limit state is lost if the node fails) and complicates scaling.

## Common Keys for Rate Limiting
- **User ID** (`user:123`): The most common dimension for rate limiting.
- **IP Address** (`ip:1.2.3.4`): Useful for limiting anonymous, unauthenticated traffic.
- **API Key** (`api_key:abc`): Commonly used for SaaS platforms and B2B integrations.
- **Organization** (`company:xyz`): Used for tenant-level limits in enterprise APIs.

## Rate Limiting vs Throttling
- **Rate Limiting**: Outright rejects requests when the defined limit is exceeded.
- **Throttling**: Slows requests down (e.g., via queuing or delaying) instead of outright rejecting them.
