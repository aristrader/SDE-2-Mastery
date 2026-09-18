---
order: 110
---

# Spatial Indexing for Nearby Search

## The engineering question

How can a backend find nearby drivers, stores, or couriers without calculating a distance to every active point on
Earth? Coordinates identify a point; an index discovers a small candidate set. Exact distance and ETA then decide
which candidate is actually useful.

```text
location -> spatial cell / range index -> nearby candidates -> exact filter -> route ETA -> decision
```

This page explains the spatial part of that pipeline. For the complete ride request, reservation, and timeout flow,
see [Uber / Ride-Sharing](/system_design/case_studies/location_and_uber_architecture/).

## Coordinates and distance are different things

Latitude and longitude are angular coordinates, not linear distances. One degree of latitude is roughly 111 km, but
one degree of longitude varies with latitude because longitude lines converge toward the poles. Comparing raw
coordinates therefore does not answer “which driver is closest?” and scanning all points is `O(N)`.

| Measure | Good for | Boundary |
| --- | --- | --- |
| Euclidean distance | Small, planar/local approximations | It ignores Earth curvature over larger areas. |
| Great-circle/Haversine distance | Exact geographic filtering after candidate discovery | Still does not describe road travel time. |
| Road distance / ETA | Final dispatch ranking | Requires a routing graph, traffic, and more expensive computation. |

Road routing models intersections as graph nodes and road segments as weighted edges. The useful HLD point is that
the weight is usually predicted travel time, not physical length. Dijkstra or destination-guided A* can find a route;
their implementation is outside a normal backend HLD answer.

## Start with a bounding box

For a radius search, calculate a rectangle that contains the target circle, then query only coordinates inside that
rectangle. A composite latitude/longitude index can make this a useful first pass:

```text
WHERE latitude  BETWEEN minLat AND maxLat
  AND longitude BETWEEN minLon AND maxLon
```

The rectangle includes corner points outside the desired circle, so exact distance must filter false positives. This
can work at modest scale, but dense areas and nearest-neighbour queries still return too many rows. A B-tree is good
at ordered one-dimensional ranges; a spatial index is better at geometric locality.

## Cell indexes: candidates, not answers

A cell index maps `(latitude, longitude)` to a discrete area. The service stores both the cell identifier and exact
coordinates:

```text
driverId -> { cellId, latitude, longitude, availability, lastSeen }
cellId   -> active driver IDs
```

The cell lets the service retrieve a region cheaply; exact coordinates remove false positives afterward. Every cell
system has a boundary: two drivers a few metres apart can belong to different cells. A correct nearby query includes
the pickup cell and enough neighbouring cells/rings to cover the requested radius.

## Geohash

Geohash recursively divides the world into rectangular cells and encodes the result as a string. Longer strings mean
smaller cells, so nearby points often share a prefix such as `tdr1v`. A normal ordered key/index can retrieve one
prefix range efficiently, which makes geohash straightforward to store and shard. One character covers thousands of
kilometres; around eight characters is on the order of tens of metres, with practical dimensions varying by latitude.

| Precision choice | Consequence |
| --- | --- |
| Too coarse | A dense cell returns too many candidates for exact filtering. |
| Too fine | One search radius crosses many cells, increasing lookup fan-out. |
| Right-sized or adaptive | Candidate count and neighbour fan-out stay bounded for the local density and radius. |

For a simple geohash query, search the containing cell plus its eight neighbours. Nine cells are not universal: a
large radius or overly fine precision can require additional rings. The result remains a rectangular approximation,
so a bounding-box and exact-distance pass still matter.

## H3, S2, and quadtrees

| Structure | How it partitions | Interview use | Important boundary |
| --- | --- | --- | --- |
| H3 | Hierarchical global mostly-hexagonal cells | Practical candidate buckets and neighbour traversal | Still has boundaries and pentagon exceptions. |
| S2 | Hierarchical quadrilateral cells projected from a cube onto the sphere | Global/spherical indexing | It changes the grid implementation, not the candidate-to-ETA pipeline. |
| Quadtree | Adaptive recursive rectangles | Explains density-aware local range/nearest queries | A mutable distributed global tree is difficult when points move frequently. |
| R-tree / KD-tree | Spatial/tree structures | GIS or in-process/local spatial workloads | Choose only when their query/update model fits; they are not required for a ride HLD. |

H3 maps a point to a cell at a chosen resolution and can produce cells within a grid distance. It is hierarchical and
useful for aggregation at different resolutions, but a cell identifier is still only a coarse location filter. The
[H3 introduction](https://h3geo.org/docs/) and [grid traversal reference](https://h3geo.org/docs/api/traversal/)
are the source for those operations.

Compared with geohash rectangles, H3's mostly hexagonal cells make neighbour relationships more uniform. That is a
reason to choose it at scale, not a different ride-matching architecture: boundary expansion, exact filtering, ETA,
and durable assignment still follow the same pipeline.

Quadtrees split a rectangle into north-west, north-east, south-west, and south-east children only when a leaf is too
dense. A range query checks whether a node's rectangle intersects the query rectangle; non-intersecting subtrees are
skipped. This makes the mechanism excellent to explain, but moving drivers require delete/reinsert and potentially
cross-node rebalancing. Average lookup/insertion is commonly `O(log n)` but poor overlap/distribution can degrade it.
Quadtrees remain useful for local simulations, game collision queries, and GIS; for high-frequency globally
distributed live locations, fixed-cell keys are usually simpler.

## A production nearby-driver lookup

1. A driver location update—often every few seconds, such as a 3–10-second product policy—derives a cell and stores
   the latest `{ point, cell, status, lastSeen }` in a fast live index. The old cell membership is removed when the
   cell changes.
2. A rider pickup derives its cell and enough neighbouring cells to cover the initial radius.
3. The matcher filters out drivers that are not `AVAILABLE` or whose `lastSeen` is beyond the freshness threshold.
4. It applies a bounding-box and exact geographic-distance filter, then keeps a bounded top set.
5. A routing service calculates ETA for that small set; ETA, vehicle type, and stated business rules rank candidates.
6. If the set is empty, expand one ring/radius at a time until the request deadline. Do not begin with a huge global
   query.

Current locations are hot, replaceable state. An in-memory geospatial store is suitable for the live index; an event
stream can retain history asynchronously for analytics, fraud, and replay. The location index is not a durable record
of which driver was assigned to which ride.

For ETA, map the candidate and pickup coordinates to nearby road-graph nodes, then compute travel-time paths. Road
segment weights combine historical patterns with current traffic, closures, and incidents; ETA is not simply
`distance / speed`. Run accurate routing only on the bounded top set. Short-lived caching can help repeated route
estimates, but prices and traffic-sensitive results need an explicit freshness policy.

## Density, sharding, and freshness

Cell prefixes can preserve locality, but a downtown cell can become a hot partition while rural cells are nearly idle.
Mitigations are finer resolution in dense areas, city/region ownership for match workers, candidate caps before ETA,
and incremental radius expansion. Geographic cells answer *where are candidates*; city/region partitions answer *who
owns this local matching workload*. They are complementary, not competing, partitioning choices.

A “city matching cluster” is a logical ownership boundary, not a promise that every process runs physically inside
that city. It keeps driver reservation, offer dispatch, and request deadlines local to the same operational workload;
the underlying cloud region can host several such logical partitions.

Location freshness is an application rule. A TTL can remove abandoned keys, but a stored `lastSeen` timestamp lets the
matcher explain why a driver is excluded and supports a cleanup/reconciliation process. `ON_TRIP` drivers may still
update their location for rider tracking but must not stay in the available-driver candidate set.

## Interview traps

- **“Geohash finds the nearest driver.”** No. It finds geographically plausible candidates; exact filtering and ETA
  rank them.
- **“H3 removes boundaries.”** No grid removes boundaries. Search neighbouring cells or rings.
- **“Use every GPS update as a database row.”** Keep latest live state in the hot path; retain history asynchronously.
- **“The closest point has the best ETA.”** Road topology and traffic make physical distance only a proxy.
- **“City partitioning replaces spatial indexing.”** City ownership localizes work; spatial cells narrow nearby search.
- **“A quadtree is automatically best because it is adaptive.”** Its distributed update/rebalancing cost can dominate
  for fast-moving global points.

## Quick recall

**Q. Why keep exact coordinates after computing a cell ID?**

A. The cell is a coarse candidate bucket; exact coordinates remove its false positives.

**Q. Why search neighbouring cells?**

A. Nearby points can lie on opposite sides of a discrete cell boundary.

**Q. What decides the final driver ranking?**

A. ETA for a bounded candidate set, not raw latitude/longitude distance alone.

**Q. Why is a bounding box not the whole solution?**

A. It contains points outside the target circle and becomes expensive in dense or broad searches.

**Q. Why not maintain one global distributed quadtree for drivers?**

A. Frequent movement causes coordinated delete/insert/rebalancing; cell keys are simpler to distribute.

**Q. What is the live location store's contract?**

A. It holds current, fresh, eligible candidates and is rebuildable; durable rides and history live elsewhere.
