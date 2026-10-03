---
order: 80
---

# Performance Engineering

Performance, Latency, Throughput, Threading, and Capacity Planning.

Performance engineering in backend systems is the study of how software interacts with hardware constraints (CPU, memory, disk, network) under varying load. Rather than memorizing arbitrary benchmarks, senior engineers use fundamental laws of capacity, queueing theory, and latency percentiles to reason about scale, predict bottlenecks, and design resilient architectures.

---

## Curriculum Roadmap & Study Guide

For a first-time reader or interview candidate, navigate the performance modules in this recommended sequence:

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Latency, Throughput & Little's Law                      │
│    - Latency Hierarchy (RAM vs SSD vs Network RTT)          │
│    - Concurrency vs Throughput vs Latency                   │
│    - Little's Law: L = λ × W                                │
│    - Tail Percentiles (p50, p95, p99) & Fan-out Amplification│
│    - Queueing Saturation & Hockey-Stick Latency Knee        │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Back-of-the-Envelope Capacity Estimation                │
│    - QPS & Peak Traffic Sizing (Traffic Multiplication)     │
│    - Storage Growth, Retention & Replication Sizing         │
│    - Network Bandwidth & Ingress/Egress Saturation          │
│    - Memory Sizing (Hot vs Cold Caching Strategy)           │
│    - Hardware Limits: Network Cards, Disk IOPS, Memory Bandwidth
└─────────────────────────────────────────────────────────────┘
```

### Module 1: [Latency, Throughput & Little's Law](./latency_throughput_littles_law/index.md)
Master the mechanical laws of request execution:
- **The Latency Hierarchy**: Order-of-magnitude reasoning across registers, caches, RAM, NVMe SSDs, intra-datacenter networks, and cross-region WANs.
- **CPU Cores & Context Switching**: Thread execution models, I/O wait states, thread pool thrashing, and OS scheduler overhead.
- **Little's Law ($L = \lambda \times W$)**: The invariant relationship linking active in-flight concurrency ($L$), arrival rate/throughput ($\lambda$), and residence/latency time ($W$).
- **Tail Latency & Fan-out Degradation**: Why average latency is misleading, how tail percentiles compound in microservice architectures, and mitigation patterns (hedged requests, timeouts).
- **Queueing Theory & Utilization Knee**: Why systems collapse non-linearly as utilization exceeds 75–80%.

### Module 2: [Back-of-the-Envelope Capacity Estimation](./capacity_estimation/index.md)
Master quantitative sizing for system design interviews:
- **Throughput Sizing**: Converting Daily Active Users (DAU) and Read/Write ratios into average and peak QPS.
- **Storage Sizing**: Estimating raw record storage, 5–10 year retention requirements, 3-way replication vs erasure coding overhead, and indexing buffers.
- **Bandwidth Sizing**: Calculating ingress/egress requirements in MB/s and Gbps to avoid NIC saturation.
- **Cache & Memory Sizing**: Applying the 80/20 Pareto rule to size Redis clusters and database buffer pools.

---

## Core Performance Principles

| Concept | Key Formula / Rule | Primary Interview Implication |
| :--- | :--- | :--- |
| **Little's Law** | $L = \lambda \times W$ | Sizing thread pools, database connection pools, and queue lengths under latency degradation. |
| **Tail Latency Amplification** | $P(\text{tail}) = 1 - (1 - p)^N$ | A service calling 20 microservices with p99 = 50ms will expose ~18% of aggregate requests to the slow tail. |
| **Queueing Utilization Knee** | $W_q \propto \frac{\rho}{1 - \rho}$ | Latency explodes asymptotically as server utilization $\rho \to 1.0$; always design for headroom ($\rho \le 0.70$). |
| **Amdahl's Law** | $S_{\text{latency}}(s) = \frac{1}{(1 - p) + \frac{p}{s}}$ | Parallelizing code only speeds up the parallelizable portion $p$; serial bottlenecks (locks, single-threaded writes) limit maximum speedup. |
| **Universal Scalability Law (USL)** | $X(N) = \frac{\gamma N}{1 + \sigma(N-1) + \kappa N(N-1)}$ | Accounts for both contention ($\sigma$) and coherency/crosstalk overhead ($\kappa$), explaining why scaling out nodes eventually reduces total throughput. |

---

## References & Authoritative Citations

- **Brendan Gregg (2020)**: *Systems Performance: Enterprise and the Cloud (2nd Edition)* — The gold standard for OS, CPU, memory, disk, and network performance analysis.
- **Jeffrey Dean & Luiz André Barroso (2013)**: *The Tail at Scale* (Communications of the ACM, Vol. 56 No. 2) — Authoritative paper on tail latency, fan-out amplification, and hedged requests.
- **Martin Kleppmann (2017)**: *Designing Data-Intensive Applications (DDIA)*, Chapter 1 (Reliability, Scalability, Maintainability) — Throughput, percentiles, and SLA guarantees.
- **Google Site Reliability Engineering (SRE)**: *Site Reliability Engineering Handbook* & *The SRE Workbook* (O'Reilly) — Service Level Indicators (SLIs), Service Level Objectives (SLOs), and Non-Abstract Large System Design (NALSD).
- **John D. C. Little (1961)**: *A Proof for the Queuing Formula: $L = \lambda W$* (Operations Research, 9(3):383–387).
- **Neil J. Gunther (2007)**: *Guerrilla Capacity Planning: A Tactical Approach to Planning for Highly Scalable Applications and Services* — Universal Scalability Law (USL).
