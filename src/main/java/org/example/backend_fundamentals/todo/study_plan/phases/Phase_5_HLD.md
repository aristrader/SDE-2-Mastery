# High-Level Design (HLD) Interview Curriculum (SDE-2 Backend)

> Goal: Maximize interview coverage with the minimum number of representative system design problems.

## 1. Foundation
### HLD Interview Process
- [ ] Requirement Gathering
- [ ] Functional vs Non-functional Requirements
- [ ] API Design
- [ ] Data Model
- [ ] Capacity Estimation (QPS, Storage, Memory, Bandwidth)
- [ ] High-Level Architecture
- [ ] Bottlenecks & Trade-offs
### Core Building Blocks
- [ ] Load Balancer
- [ ] Reverse Proxy / API Gateway
- [ ] CDN
- [ ] Caching
- [ ] SQL vs NoSQL
- [ ] Database Replication
- [ ] Database Sharding / Partitioning
- [ ] Message Queue
- [ ] Object Storage
- [ ] Search Engine
- [ ] Service Discovery
- [ ] Rate Limiter
### Distributed Systems Fundamentals
- [ ] CAP Theorem
- [ ] Eventual Consistency
- [ ] Leader–Follower Replication
- [ ] Quorum Reads/Writes
- [ ] Distributed ID Generation (UUID vs Snowflake vs Sequence)
- [ ] Idempotency
- [ ] Retry
- [ ] Circuit Breaker
- [ ] Distributed Lock
## 2. Core Interview Problems (Recommended Order)
### 1. URL Shortener

#### Variants

- [ ] TinyURL
- [ ] Bit.ly

#### Learn

- [ ] Database Design
- [ ] Base62 Encoding
- [ ] Hashing
- [ ] Caching
- [ ] Read-heavy Scaling
- [ ] Hot Keys
- [ ] Load Balancing
### 2. Rate Limiter

#### Variants

- [ ] API Gateway
- [ ] Login Rate Limiter
- [ ] Payment Rate Limiter

#### Learn

- [ ] Token Bucket
- [ ] Sliding Window
- [ ] Redis
- [ ] Distributed Counters
- [ ] Atomic Operations
- [ ] Horizontal Scaling
### 3. Distributed Cache

#### Variants

- [ ] Redis Cluster
- [ ] Memcached Service

#### Learn

- [ ] Consistent Hashing
- [ ] Replication
- [ ] Cache Invalidation
- [ ] Eviction Policies
- [ ] Hot Keys
- [ ] High Availability
- [ ] 4. Notification System

#### Variants

- [ ] Email
- [ ] SMS
- [ ] Push Notifications
- [ ] Multi-channel Notifications

#### Learn

- [ ] Message Queue
- [ ] Async Processing
- [ ] Retry
- [ ] Dead Letter Queue
- [ ] Worker Scaling
- [ ] Scheduling
- [ ] 5. Chat / Messaging System

#### Variants

- [ ] WhatsApp
- [ ] Slack
- [ ] Microsoft Teams
- [ ] Facebook Messenger

#### Learn

- [ ] WebSockets
- [ ] Presence
- [ ] Pub/Sub
- [ ] Fan-out
- [ ] Ordering
- [ ] Offline Delivery
- [ ] 6. News Feed / Social Timeline

#### Variants

- [ ] Facebook Feed
- [ ] Twitter/X Timeline
- [ ] LinkedIn Feed

#### Learn

- [ ] Fan-out on Write
- [ ] Fan-out on Read
- [ ] Ranking
- [ ] Feed Generation
- [ ] Pagination
- [ ] Caching
- [ ] 7. Ride Sharing / Food Delivery

#### Variants

- [ ] Uber
- [ ] Lyft
- [ ] Ola
- [ ] Swiggy
- [ ] Zomato

#### Learn

- [ ] Geospatial Indexing
- [ ] Matching
- [ ] ETA
- [ ] Real-time Location Updates
- [ ] Dispatch
- [ ] Pub/Sub
- [ ] 8. Search System

#### Variants

- [ ] Product Search
- [ ] Document Search
- [ ] Full-text Search

#### Learn

- [ ] Elasticsearch
- [ ] Inverted Index
- [ ] Ranking
- [ ] Index Pipeline
- [ ] Sharding
- [ ] Event-driven Indexing
- [ ] 9. Object Storage & Content Delivery

#### Variants

- [ ] Dropbox
- [ ] Google Drive
- [ ] Amazon S3
- [ ] YouTube
- [ ] Netflix

#### Learn

- [ ] Object Storage
- [ ] Blob Storage
- [ ] Metadata Service
- [ ] Chunking
- [ ] Deduplication
- [ ] Upload Pipeline
- [ ] CDN
- [ ] Transcoding (Video Variant)
- [ ] 10. Payment System

#### Variants

- [ ] Stripe
- [ ] Razorpay
- [ ] PayPal

#### Learn

- [ ] Idempotency
- [ ] Ledger
- [ ] Transactions
- [ ] Double Spending Prevention
- [ ] Reconciliation
- [ ] Event-driven Processing
- [ ] Reliability
- [ ] 11. E-commerce Platform

#### Variants

- [ ] Amazon
- [ ] Flipkart

#### Learn

- [ ] Inventory
- [ ] Shopping Cart
- [ ] Checkout
- [ ] Orders
- [ ] Search
- [ ] Recommendation
- [ ] Event-driven Architecture
- [ ] Microservices
- [ ] 12. Web Crawler

#### Variants

- [ ] Google Search Crawler

#### Learn

- [ ] URL Frontier
- [ ] Distributed Queue
- [ ] Scheduling
- [ ] Deduplication
- [ ] Worker Coordination
- [ ] Politeness
## 3. Additional Practice (If Time Permits)
- [ ] 13. Distributed Job Scheduler

#### Variants

- [ ] Cron Service
- [ ] Workflow Engine
- [ ] Quartz Scheduler
- [ ] ZooKeeper / etcd-based Coordination

#### Learn

- [ ] Scheduling
- [ ] Leader Election
- [ ] Distributed Lock
- [ ] Retry
- [ ] Queue
- [ ] Coordination
- [ ] 14. Analytics Pipeline

#### Variants

- [ ] Clickstream Analytics
- [ ] Metrics Collection
- [ ] Event Analytics

#### Learn

- [ ] Kafka
- [ ] Stream Processing
- [ ] Batch vs Streaming
- [ ] Aggregation
- [ ] OLAP
- [ ] 15. Distributed Key-Value Store

#### Variants

- [ ] DynamoDB
- [ ] Cassandra

#### Learn

- [ ] Partitioning
- [ ] Replication
- [ ] Consistent Hashing
- [ ] Quorum
- [ ] Anti-Entropy
- [ ] 16. Recommendation System

#### Variants

- [ ] Netflix
- [ ] YouTube
- [ ] Amazon Recommendations

#### Learn

- [ ] Offline Pipeline
- [ ] Online Serving
- [ ] Feature Store
- [ ] Ranking
- [ ] Caching
- [ ] 17. Ad Serving System

#### Variants

- [ ] Google Ads
- [ ] Meta Ads

#### Learn

- [ ] Low-latency Serving
- [ ] Targeting
- [ ] Budget Pacing
- [ ] Ranking
- [ ] Distributed Counters
- [ ] 18. Logging / Metrics Platform

#### Variants

- [ ] Datadog
- [ ] ELK Stack
- [ ] Splunk
- [ ] CloudWatch

#### Learn

- [ ] Log Ingestion
- [ ] Time-series Storage
- [ ] Aggregation
- [ ] Streaming Pipeline
- [ ] Indexing
- [ ] Retention
## Concept Coverage Matrix
| Concept | Covered By |
|---|---|
| Capacity Estimation | Foundation (applies to every problem) |
| API Design | Foundation |
| Database Design | URL Shortener, Payments, E-commerce |
| Caching | URL Shortener, Distributed Cache, Feed, Search |
| Load Balancing | URL Shortener, Chat, E-commerce |
| CDN | Object Storage & Content Delivery |
| Object Storage | Object Storage & Content Delivery |
| Search | Search System, E-commerce |
| Sharding | Search, Distributed Cache, KV Store |
| Replication | Distributed Cache, KV Store |
| Consistent Hashing | Distributed Cache, KV Store |
| Messaging | Notifications, Chat, Analytics |
| Pub/Sub | Chat, Ride Sharing |
| Event-driven Architecture | Notifications, Payments, E-commerce |
| WebSockets | Chat |
| Geospatial Indexing | Ride Sharing |
| Rate Limiting | Rate Limiter |
| Distributed Locks | Job Scheduler |
| Leader Election | Job Scheduler |
| Idempotency | Payments, Notifications |
| Retry & DLQ | Notifications, Payments |
| Distributed ID Generation | Foundation |
| CAP & Quorum | Foundation, KV Store |
| Stream Processing | Analytics |
| Recommendation Systems | Recommendation, E-commerce |
| Logging & Observability | Logging Platform |
