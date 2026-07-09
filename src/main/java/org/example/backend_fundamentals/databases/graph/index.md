---
order: 100
---

# Graph Databases

Graph databases model data as nodes and relationships. They fit problems where the important question is not just "which rows match?", but "how are these entities connected?"

Common interview examples:

- Fraud detection across accounts, devices, cards, IPs, and merchants.
- Social graphs such as friends-of-friends and recommendations.
- Dependency graphs such as package, service, or ownership relationships.
- Knowledge graphs where relationships are first-class facts.

Neo4j and Amazon Neptune are common examples.

## Key idea

Relational databases can model relationships with join tables, but deep relationship traversal can become join-heavy and awkward. Graph databases store relationships as first-class edges, so traversal-style queries are the natural path.

## Quick recall

**Q. Is GraphQL a graph database?**

A. No. GraphQL is an API query language. Graph databases are storage engines for relationship-heavy data.

**Q. Why do graph databases fit fraud detection?**

A. Fraud often hides in relationships: shared devices, linked cards, repeated IPs, cycles, and suspicious clusters.
