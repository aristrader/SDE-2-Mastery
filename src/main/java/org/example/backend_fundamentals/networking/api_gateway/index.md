---
order: 20
---

# Consolidated Technical Notes: Service Discovery, Load Balancers, Gateways, and Service Mesh

This document consolidates technical concepts related to service-to-service communication, discovery, and traffic management in distributed systems.

## 1. Service Discovery & Service Registry

**Service Discovery** is the mechanism used to locate services in a distributed system where instances are dynamic, IPs change, and services scale up/down or restart. It answers the question: "Where is Service X currently running?"

A **Service Registry** is the database that stores the mappings between service names and their current instance addresses (e.g., analogous to DNS for microservices).

### Discovery Patterns
* **Client-Side Discovery**: The client queries the Service Registry directly, retrieves the list of instances, applies its own load balancing (e.g., Round Robin, Random), and calls the target instance.
  * *Pros*: Client has full control over routing, retries, and load balancing.
  * *Cons*: Discovery and load balancing logic must be duplicated in every client.
* **Server-Side Discovery**: The client queries a Load Balancer (or proxy) which in turn queries the registry and forwards the request to the appropriate instance.
  * *Pros*: Clients are simpler; they only need to know a single endpoint.
  * *Cons*: The load balancer becomes a critical infrastructure component that must be scaled and monitored.

### Service Registration Patterns
* **Self Registration**: The service instance is responsible for registering itself with the registry on startup and deregistering on shutdown. It typically sends periodic heartbeats to prove it is healthy.
* **Third-Party Registration**: Infrastructure (e.g., Kubernetes) observes the environment and updates the registry automatically based on pod creation, deletion, or failure. Services focus solely on business logic.

## 2. Gateways vs. Load Balancers vs. Service Mesh

These three components handle different aspects of traffic management.

### API Gateway (North-South Traffic)
The API Gateway acts as the "Front Door" for external traffic entering the system (North-South traffic).
* **Responsibilities**: Authentication (JWT, OAuth), Authorization, Rate Limiting, API Routing, and Request Transformation (e.g., REST to gRPC).
* *Note*: Many modern API Gateways (like NGINX, Kong) include built-in load balancing capabilities, which often causes conceptual confusion.

### Load Balancer
A Load Balancer focuses primarily on traffic distribution.
* **Responsibilities**: Answering "Which instance should receive this request?" using algorithms like Round Robin, Least Connections, or Weighted.

### Service Mesh (East-West Traffic)
A Service Mesh manages internal, service-to-service communication (East-West traffic) by offloading networking responsibilities from the application code into the infrastructure. It typically uses a "Sidecar" pattern (e.g., Envoy proxy running alongside the application container).
* **Responsibilities**: Distributed tracing, automatic retries, timeouts, circuit breaking, mutual TLS (mTLS) for security, observability, and advanced traffic routing (e.g., Canary releases).
* **Architecture**: The control plane (e.g., Istio) manages configuration, while the data plane (e.g., Envoy proxies) intercepts and handles all traffic.

## 3. Kubernetes Services

Kubernetes native "Services" automatically provide:
1. **Service Discovery**
2. **Basic Load Balancing**

When you call a Kubernetes Service endpoint, it acts as a stable virtual IP that automatically tracks healthy pods and distributes traffic among them. It does *not* natively provide advanced mesh features like distributed tracing, automatic retries, complex canary routing, or circuit breaking.

## 4. Key Clarifications and Fact Corrections

* **Service Mesh vs. Load Balancer**: A Service Mesh is *not* just a load balancer. Load balancing is merely one capability inside a service mesh. A mesh comprehensively centralizes service-to-service networking concerns, observability, and security.
* **Service Mesh is Optional**: A Service Mesh is not mandatory for microservices. It introduces significant operational complexity (managing hundreds of sidecar proxies and control plane components). Many organizations successfully rely on Kubernetes Services combined with application-level libraries (e.g., Resilience4j, Feign) for retries and circuit breaking. A mesh becomes attractive only when standardizing networking policies across a large number of services and teams becomes critical.
* **Finding Instances**: Service Mesh does not exist primarily to locate pods; Kubernetes already solves that. Service Mesh exists to govern *how* those services communicate once they are found.
* **Single Visible Endpoint**: If a service has multiple pods but exposes only one endpoint, that endpoint is typically backed by a Kubernetes Service that handles discovery and load balancing under the hood.


<ExerciseNav />
