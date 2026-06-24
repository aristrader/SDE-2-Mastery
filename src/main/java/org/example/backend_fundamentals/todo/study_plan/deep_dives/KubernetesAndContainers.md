# Deep Dive: Kubernetes, Containers, and ArgoCD

## Docker vs Kubernetes
* **Docker:** Creates and runs containers (Mental Model: One Taxi). Best for 1 machine, 5 containers.
* **Kubernetes:** Manages containers at scale (Mental Model: Uber Dispatch System). Responsible for routing, replacement, scaling, and management across 100+ machines. 

## Kubernetes Architecture Hierarchy
```text
Cluster (Group of Machines)
    ↓
Nodes (Single Machine / VM)
    ↓
Deployment (Manages desired state, e.g. replicas: 10)
    ↓
ReplicaSet (Internal helper maintaining replica count)
    ↓
Pods (Smallest deployable unit, protective wrapper around container)
    ↓
Containers
```

## Communication & Config Layers
* **Service:** Sits in front of Pods. Provides a stable address, load balancing, and service discovery, solving the problem of temporary pods and changing IPs.
* **Ingress:** Routes external internet traffic to internal Services.
* **ConfigMap:** Stores non-sensitive configuration (e.g., `DB_HOST`, `LOG_LEVEL`).
* **Secret:** Stores sensitive configuration (e.g., Passwords, API Keys, JWT Secrets).
* **Autoscaling:** Automatically increases/decreases pod count based on load.

## ArgoCD (GitOps)
ArgoCD provides GitOps Deployment Automation:
```text
Git Repository → ArgoCD Watches → Kubernetes Syncs
```

## VM vs Container vs Lambda
* **VM:** You manage OS, Runtime, and Application. Pros: Strong Isolation, Full Control. Cons: Heavy, Slower Scaling.
* **Container:** You manage Application and Docker Image (Platform manages the rest). Pros: Lightweight, Fast Startup, Portable. Cons: Operational Complexity.
* **Lambda (Serverless):** You provide the Function only (Cloud handles servers, OS, scaling). Pros: No Server Management, Pay Per Request. Cons: Cold Starts, Execution Limits.

## SDE2 Scope Recommendation
For SDE2, focus deeply on: **Cluster, Node, Pod, Deployment, Service, Autoscaling**. Advanced topics like Operators, Helm internals, containerd, CSI/CNI are considered low ROI.
