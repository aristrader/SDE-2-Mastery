# API Technologies — REST vs GraphQL vs gRPC Comparison

## REST vs GraphQL vs gRPC (Interview Version)

When asked to compare API technologies in an SDE2/System Design interview, you are expected to contrast them across coupling, performance, and best use-cases. 

### Architecture Characteristics

| Type | Coupling | Chattiness | Performance | Complexity | Caching | Codegen | Discoverability | Versioning |
|---|---|---|---|---|---|---|---|---|
| **REST** | Low | High | Good | Medium | Great | Bad | Good | Easy |
| **GraphQL** | Medium | Low | Good | High | Custom | Good | Good | Custom |
| **gRPC** | High | Medium | Great | Low | Custom | Great | Bad | Hard |

### Practical Trade-offs

| Feature | REST | GraphQL | gRPC |
|---|---|---|---|
| **Learning Curve** | Easy | Medium | Medium |
| **Human Readable** | Yes | Yes | No |
| **Browser Support** | Excellent | Excellent | Limited |
| **Performance** | Good | Good | Excellent |
| **Over-fetching** | Yes | No | No |
| **Streaming** | No | Limited | Excellent |
| **Public APIs** | Excellent | Good | Poor |
| **Internal Microservices** | Good | Rare | Excellent |

## When to choose which?

Interviewers expect you to know *why* you pick a specific technology.

### When would you choose REST?
- **Public APIs**: It has the broadest ecosystem support and is universally understood by external partners.
- **Simplicity**: Best when the domain naturally maps to resources (CRUD operations).
- **Caching**: Easy to cache at the HTTP layer (CDN, reverse proxies) because of standard HTTP GET semantics.

### When would you choose GraphQL?
- **Mobile clients**: Bandwidth and battery optimization by fetching exactly what's needed in one request.
- **Complex frontend requirements**: When the UI aggregates data across multiple disparate domain entities.
- **Avoid over-fetching/under-fetching**: Eliminates the "multiple network calls" penalty of REST for nested relationships.

### When would you choose gRPC?
- **Service-to-service communication**: High throughput and low latency for internal microservices.
- **Streaming requirements**: Built-in support for continuous streams (e.g., chat apps, IoT telemetry).
- **Strong contracts**: Code generation across polyglot environments with strict `.proto` definitions.

## Common Real-World Architecture

A standard hybrid approach used in many large-scale systems:

```text
       Mobile / Web Client
               │
               ▼
          API Gateway
               │
   (REST or GraphQL over HTTP/1.1)
               │
    ┌──────────┴──────────┐
    ▼                     ▼
 UserSvc               OrderSvc
    │                     │
    └─────── gRPC ────────┘
```

**Why this works:**
- **External clients** use REST/GraphQL for ease of integration, browser compatibility, and caching.
- **Internal microservices** use gRPC (over HTTP/2 with Protobuf) for high-speed, binary, low-latency communication.

## gRPC Deep Dive: Why it's fast & Streaming capabilities

### Why gRPC is Fast
1. **Binary Protocol**: Protobuf creates significantly smaller payloads than JSON.
2. **HTTP/2**: Supports multiplexing (multiple requests over a single TCP connection), header compression, and streaming.
3. **Generated Code**: Deserialization is natively compiled; no reflection-heavy JSON parsing is required.

### Streaming Types (A huge advantage over REST)
REST is strictly Request → Response → Done. gRPC supports continuous streams:

1. **Unary**: 1 Request → 1 Response (Most common).
2. **Server Streaming**: 1 Request → Many Responses (e.g., getting real-time stock updates).
3. **Client Streaming**: Many Requests → 1 Response (e.g., uploading large file chunks).
4. **Bidirectional Streaming**: Many Requests ↔ Many Responses (e.g., WhatsApp chat, multiplayer gaming).

## Quick recall

**Q. Why do most public APIs still use REST?**
A. Broadest ecosystem support, easiest to integrate for external clients, natively supported by all browsers, and excellent HTTP-level caching.

**Q. What problem does GraphQL solve over REST?**
A. It eliminates over-fetching (getting unnecessary fields) and under-fetching (requiring multiple round trips to get related data).

**Q. Why is gRPC faster than REST?**
A. Uses compact binary payloads (Protobuf) instead of text (JSON), runs on HTTP/2 (multiplexing, header compression), and uses generated code without reflection.

**Q. What are the 4 streaming types in gRPC?**
A. Unary, Server streaming, Client streaming, and Bidirectional streaming.

**Q. Describe a common hybrid API architecture.**
A. REST or GraphQL exposed via API Gateway to external clients, while internal microservices communicate with each other using gRPC.
