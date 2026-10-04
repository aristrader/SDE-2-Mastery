---
order: 110
title: Deployments
---

# Deployments Hub

Modern backend deployment spans the entire lifecycle of packaging, isolating, orchestrating, and safely releasing distributed services with zero downtime.

---

## 🗺️ Recommended Learning Path

Follow the deployment progression from OS-level isolation primitives up to declarative orchestration and progressive delivery:

1. **[OS, Virtualization, and Containers](./os_virtualization_containers/index.md)**
   - Hardware virtualization, Type-1 vs Type-2 hypervisors, VM isolation boundaries, and failure domains.
2. **[OS, Containers & Docker Fundamentals](./os_containers_docker/index.md)**
   - Process vs thread memory models, Linux isolation primitives (`namespaces`, `cgroups`), Docker image layer caching, and storage/network drivers.
3. **[Kubernetes, Containers, and Argo CD (GitOps)](./kubernetes_and_containers/index.md)**
   - Container orchestration, Pod lifecycles, Service/Ingress traffic routing, declarative reconciliation loops, and GitOps sync strategies.
4. **[Release & Deployment Strategies](./release_and_deployment_strategies/index.md)**
   - Progressive rollout patterns (Rolling, Blue-Green, Canary), backward/forward database schema migrations (Expand/Contract), graceful connection draining, and automated rollback triggers.

---

## 🎯 Core Interview Focus Areas

| Concept | Key Architectural Question | Core Trade-off / Mechanism |
| :--- | :--- | :--- |
| **Isolation Boundaries** | VM vs Container vs Serverless | Kernel sharing density vs strict hypervisor hardware boundary. |
| **Resource Governance** | CPU/Memory limits under load | Soft throttles (CPU cgroup CFS quota) vs hard termination (OOMKill on Memory). |
| **Zero-Downtime Rollouts** | Managing live traffic during cutover | Dual-version compatibility, `maxSurge`/`maxUnavailable` capacity headroom, LB connection draining. |
| **Stateful Deployments** | Schema evolution without locks | Expand-Contract (two-phase migrations) with backward-compatible application releases. |

---

## Quick recall

1. **Why do containers start significantly faster than virtual machines?**
   - Containers share the host OS kernel and only isolate user-space processes via namespaces and cgroups, avoiding full guest OS kernel boot and hardware emulation.
2. **What is the primary risk during a Rolling Deployment, and how is it mitigated?**
   - Mixed-version coexistence where old and new pods serve traffic concurrently; mitigated by backward-compatible APIs/payloads and expand-contract database schemas.
3. **What is the difference between CPU throttling and Memory OOMKill in containers?**
   - CPU is a compressible resource (exceeding limits results in throttling/latency degradation), whereas memory is non-compressible (exceeding hard limits results in OS `SIGKILL`/OOMKilled).
