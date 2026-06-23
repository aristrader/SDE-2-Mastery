# Location Systems: Routing & ETA

## Core Motivation
- **Distance ≠ Travel Time**: A driver physically closer to the passenger might take longer to arrive due to traffic, road restrictions (e.g., one-way roads), or physical barriers (e.g., railway crossings). 
- **Distance**: Easy to compute using Haversine formulas (straight-line distance). Used primarily for **Candidate Discovery**.
- **ETA (Estimated Time of Arrival)**: Hard to compute as it requires knowledge of the road network, traffic, signals, and turns. Used for **Final Ranking** of candidate drivers.

## Modeling Roads As A Graph
Routing engines model the real world as a mathematical graph, not raw latitudes and longitudes.
- **Nodes**: Intersections.
- **Edges**: Road segments connecting intersections.
- **Edge Weights**: Represents travel cost. In modern systems, weight = **Travel Time** rather than just physical distance, as it naturally optimizes for the fastest route instead of the shortest distance.

## Shortest-Path Algorithms
- **Routing Problem**: Finding the shortest path (in terms of time) between the driver and passenger inside the road graph.
- **Dijkstra's Algorithm**: Computes shortest paths in a weighted graph with non-negative edge weights. It expands radially, exploring the currently cheapest node in all directions.
- **A* Search**: An optimization over Dijkstra that introduces destination-awareness (direction). It uses a heuristic (e.g., estimated remaining straight-line/Haversine distance) to guide the search toward the destination, exploring fewer nodes. A* is fundamentally a guided shortest-path algorithm.

## Routing Engine Flow & ETA Computation
### Routing Flow:
1. **Map Coordinates**: Map driver and passenger GPS coordinates to the nearest nodes on the road graph.
2. **Run Routing Algorithm**: Run an algorithm like A* to find the optimal path.
3. **Return Results**: Return the actual route, travel distance, and ETA.

### ETA Sources:
ETA is not a naive `Distance / Speed` calculation. It is computed from predicted travel times on road segments derived from:
- **Historical Traffic**: Patterns observed over long periods (e.g., typical travel time on a specific road segment at 8 AM on Mondays).
- **Real-Time Traffic**: Live updates for anomalies like accidents, construction, road closures, or temporary flooding.
- **ETA = Historical Traffic + Real-Time Traffic**.

### Routing Optimizations:
- Running A* on millions of candidate drivers for millions of requests is computationally expensive.
- **Phased Filtering Pipeline**: Systems progressively narrow down drivers.
    1. Geohash Search → Neighbor Cells (e.g., 200 candidates)
    2. Bounding Box Filter (e.g., 50 candidates)
    3. Haversine Distance (e.g., 20 candidates)
    4. Fast ETA Approximation (e.g., 5 candidates)
    5. Accurate Routing & ETA (Final Ranking)
- **ETA Caching**: ETA estimates for common or frequently requested routes (e.g., Airport to Downtown) may be cached and reused to save computation.

## The Overall System Pipeline
The complete matching pipeline logic:
1. Passenger makes a Ride Request
2. Geohash Search (Find Neighboring Cells)
3. Bounding Box Filter
4. Haversine Distance Calculation (Find Nearby Candidate Drivers)
5. ETA Calculation via Routing Engine
6. Rank Drivers by ETA
7. Offer Ride to Top Drivers (Reserve & Assign)

## Scope in Software Engineering (SDE2) Interviews
- **Focus Areas**: Understanding the road graph (Nodes/Edges), Edge Weights = travel time, conceptual understanding of Dijkstra vs. A*, ETA ranking vs. Haversine filtering, Historical vs. Real-time traffic.
- **Out of Scope (usually)**: Low-level implementation of A*/Dijkstra, advanced routing optimizations (Graph compression, Contraction Hierarchies), distributed Map storage internals, and AI traffic prediction models (these are more relevant for specialized GIS or Google Maps interviews).
