---
order: 10
search: false
---

# Exercise

## Exercise: thread-pool-sizing-littles-law - Thread Pool and Connection Pool Sizing

You are designing the backend capacity for a payment gateway order-processing microservice built on Spring Boot and Tomcat with HikariCP database connection pooling.

### Scenario & Constraints

- The service receives a steady-state traffic of $\lambda = 2,500\text{ RPS}$.
- Under normal operating conditions (p50):
  - In-service compute time: $5\text{ ms}$ (CPU time).
  - Downstream database I/O latency: $15\text{ ms}$ (waiting for DB query completion).
  - Total end-to-end response time ($W_{\text{p50}}$): $20\text{ ms}$ ($0.020\text{ s}$).
- During downstream database degradation (e.g. index contention / lock waits), the DB query time increases such that p99 response time reaches $W_{\text{p99}} = 120\text{ ms}$ ($0.120\text{ s}$).
- The application cluster currently has 10 identical service instances sharing traffic equally through an L7 load balancer.
- Each instance runs on a standard 8-vCPU virtual machine with Tomcat `maxThreads` set to 200 and HikariCP `maximumPoolSize` set to 50.

### Tasks

1. **Baseline Concurrency**: Calculate the average number of active in-flight requests ($L$) across the entire cluster and per individual instance under normal conditions ($W = 20\text{ ms}$) using Little's Law ($L = \lambda \times W$).
2. **Degraded Concurrency**: Calculate the required in-flight concurrency per instance if the average response time degrades to $W = 120\text{ ms}$ while throughput remains at $2,500\text{ RPS}$.
3. **Pool Exhaustion Analysis**:
   - Determine what happens to the Tomcat thread pool and HikariCP connection pool under this degraded state.
   - If Tomcat request queue capacity is 100, calculate how many seconds it takes for the queue to fill up and begin dropping requests (503 Service Unavailable / Connection Refused) if arrival rate stays at $2,500\text{ RPS}$.
4. **Remediation & Sizing Strategy**:
   - Provide a mathematically sound configuration for thread pools, connection pools, and connection acquisition timeouts (`connectionTimeout`).
   - Explain why increasing Tomcat `maxThreads` to 2,000 threads per 8-vCPU instance creates thrashing instead of solving the problem.

---

## Exercise: fanout-tail-latency-percentiles - Tail Latency Amplification in Distributed Fan-Out

You are designing an e-commerce Product Detail Page (PDP) aggregator service. To render a single product page, the aggregator issues concurrent non-blocking gRPC calls to $N$ independent downstream microservices (e.g., Inventory, Pricing, Reviews, Recommendations, Seller Info, Shipping Estimates, Promos, Badges, Media, etc.).

### Scenario & Constraints

- Each downstream microservice has an independent latency distribution with:
  - Median latency (p50) = $10\text{ ms}$.
  - 99th percentile latency (p99) = $50\text{ ms}$.
  - 99.9th percentile latency (p99.9) = $800\text{ ms}$ (caused by garbage collection pauses, disk cache misses, or lock contention).
- The aggregator can only finish rendering the page when **all** $N$ service calls have returned. Assume downstream call latencies are independent and identically distributed.

### Tasks

1. **Probability of Tail Degradation**:
   - Derive the mathematical formula for the probability that the aggregator experiences at least one slow request (taking $\ge 50\text{ ms}$, i.e., in the p99 tail) as a function of the fan-out degree $N$.
   - Calculate this probability for $N = 1$, $N = 10$, $N = 20$, and $N = 100$ downstream calls.
2. **Overall Service p99 Latency**:
   - Explain why the aggregator service's effective p99 latency is significantly worse than $50\text{ ms}$ when $N = 20$, even though every single downstream service complies with its $50\text{ ms}$ p99 SLA.
3. **Tail Mitigation Techniques**:
   - Evaluate the trade-offs of the following mitigation patterns:
     - **Hedged Requests (Speculative Retries)**: Sending a duplicate request to a backup replica if no response is received within the p95 threshold.
     - **Tied Requests / Request Cancellation**: Sending dual requests with a cancel signal once one completes.
     - **Graceful Degradation / SLA Budgets**: Returning partial responses when non-critical components (e.g. Recommendations) exceed a strict deadline.

---

## Exercise: queueing-saturation-utilization - Utilization Knee and Backpressure Dynamics

You are reviewing the performance characteristics of an asynchronous event-processing consumer node running in Kubernetes. The node dequeues payment webhooks from a message broker and processes them against a core banking API.

### Scenario & Constraints

- The worker node operates as an $M/M/1$ queueing system (Poisson arrival rate $\lambda$, exponentially distributed service rate $\mu$).
- The node has a maximum service rate of $\mu = 250\text{ events/second}$ (mean service time $T_s = \frac{1}{\mu} = 4\text{ ms}$).
- The incoming webhook arrival rate $\lambda$ varies over the course of a flash sale:
  - Case A: $\lambda = 150\text{ events/sec}$
  - Case B: $\lambda = 200\text{ events/sec}$
  - Case C: $\lambda = 240\text{ events/sec}$
  - Case D: $\lambda = 255\text{ events/sec}$ (overload)

### Tasks

1. **Utilization & Queue Metrics**:
   - For Cases A, B, and C, calculate:
     - Server utilization $\rho = \frac{\lambda}{\mu}$.
     - Average queue length $L_q = \frac{\rho^2}{1 - \rho}$.
     - Average queue waiting time $W_q = \frac{\rho}{\mu(1 - \rho)}$.
     - Total residency time / latency $W = W_q + T_s = \frac{1}{\mu(1 - \rho)}$.
2. **The "Hockey Stick" Knee**:
   - Compare the latency increase from Case A ($\rho = 0.60$) to Case B ($\rho = 0.80$) versus Case B ($\rho = 0.80$) to Case C ($\rho = 0.96$).
   - Explain why a mere $20\%$ increase in throughput from 200 to 240 events/sec causes an exponential surge in latency rather than a linear $20\%$ increase.
3. **Overload Failure & Backpressure**:
   - Describe what mathematically and operationally occurs in Case D ($\lambda = 255\text{ events/sec} > \mu$).
   - Design a robust backpressure and load-shedding architecture (using token-bucket rate limiting, circuit breaking, and Kafka consumer group autoscaling) to keep $\rho \le 0.70$.

---

## Exercise: latency-hierarchy-caching-tradeoff - Hardware Latency Ladder and Cross-Region Optimization

You are auditing an identity verification service running in AWS region `us-east-1` (N. Virginia). The service handles a read traffic of $\lambda = 1,000\text{ RPS}$.

### Scenario & Constraints

- **Existing Architecture**:
  - Step 1: Parse request & validate JWT in CPU memory ($0.1\text{ ms}$).
  - Step 2: Read user profile from local in-region MySQL read replica on NVMe SSD ($3.0\text{ ms}$).
  - Step 3: Read fraud score from in-region Redis cluster in RAM ($0.9\text{ ms}$).
  - Total latency = $0.1 + 3.0 + 0.9 = 4.0\text{ ms}$.
- **New Compliance Requirement**:
  - Step 4 is added: Query a global sanctions database located strictly in `eu-west-1` (Frankfurt, Germany) for regulatory compliance.
  - Cross-region network Round-Trip Time (RTT) between `us-east-1` and `eu-west-1` is $76\text{ ms}$.
  - In-region EU database query execution time is $4\text{ ms}$.

### Tasks

1. **Latency & Concurrency Impact**:
   - Calculate the new total request latency after adding the synchronous cross-region check.
   - Using Little's Law, calculate the active in-flight request concurrency $L$ needed to support $1,000\text{ RPS}$ before and after this architectural change.
   - Explain why a $20\times$ increase in latency causes an immediate $20\times$ increase in open socket connections, memory footprint, and thread consumption.
2. **Architectural Redesign**:
   - Propose a production-ready redesign that brings aggregate p99 latency back under $10\text{ ms}$ while complying with the European sanctions data sovereignty rules.
   - Evaluate:
     - Multi-tier caching (L1 local JVM cache with Caffeine + L2 in-region Redis replica).
     - Cross-region read replication / global table replication (e.g. AWS DynamoDB Global Tables or Aurora Global Database).
     - Asynchronous validation vs synchronous blocking on the critical payment path.

---

## Acceptance Criteria

A rigorous, interview-ready answer must:

1. **Show Precise Mathematical Steps**: Clearly show Little's Law ($L = \lambda W$), binomial tail probability ($1 - (1-p)^N$), and $M/M/1$ queueing formulas with units.
2. **Distinguish Concurrency from Parallelism**: Distinguish active in-flight requests (waiting in I/O) from threads actively computing on CPU cores.
3. **Analyze Failure Modes**: Explain thread pool starvation, connection pool exhaustion, GC pressure, context switching overhead, and cascading failure propagation.
4. **Provide Practical Production Solutions**: Recommend concrete architectural patterns (Hedged requests, bulkhead isolation, HikariCP sizing guidelines, timeout budgets, and distributed caching).

---

## References & Authoritative Citations

- **John D. C. Little (1961)**: *A Proof for the Queuing Formula: $L = \lambda W$* — Operations Research.
- **Jeffrey Dean & Luiz André Barroso (2013)**: *The Tail at Scale* — Communications of the ACM (Google research on tail latency reduction).
- **Brendan Gregg (2020)**: *Systems Performance: Enterprise and the Cloud (2nd Edition)* — Queueing systems and latency hierarchy.
- **HikariCP Wiki (Brett Wooldridge)**: *About Pool Sizing* — Optimal database connection pool formula: $\text{connections} = \text{core\_count} \times 2 + \text{effective\_spindle\_count}$.
- **Martin Kleppmann (2017)**: *Designing Data-Intensive Applications* — Chapter 1: Describing Performance & Percentiles.

---

## Quick recall

**Q. What does Little's Law state in plain English?**
A. The average number of items in a stable system equals the average arrival rate multiplied by the average time an item spends in the system ($L = \lambda W$).

**Q. If response time doubles while incoming request rate remains constant, what happens to thread utilization?**
A. Thread concurrency doubles, requiring twice as many active threads/sockets to prevent queueing.

**Q. Why does a fan-out of 20 services make a 99% SLA feel like an 81% SLA?**
A. Because $0.99^{20} \approx 0.8179$. The probability that *all* 20 succeed without hitting the p99 tail delay is only ~81.8%, meaning ~18.2% of requests experience the slow tail.

**Q. What happens to queue wait time as system utilization $\rho$ approaches 1.0 (100%)?**
A. Queue wait time approaches infinity asymptotically according to $W_q \propto \frac{\rho}{1 - \rho}$.
