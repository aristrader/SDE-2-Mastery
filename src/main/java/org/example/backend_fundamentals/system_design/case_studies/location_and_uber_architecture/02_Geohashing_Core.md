# Geohashing & Spatial Search Systems

## 1. What Problem Geohash Solves

Coordinates (latitude and longitude) are excellent for defining exact locations but are inefficient for locality-based querying at scale. Traditional bounding box queries (e.g., `WHERE lat BETWEEN X AND Y AND lon BETWEEN A AND B`) can suffer from false positives and rely on multi-column indexes that are not naturally hierarchical. 

Geohash solves this by converting a two-dimensional `(latitude, longitude)` pair into a single, hierarchical string identifier. 
- **Desired Property:** Nearby locations share similar string prefixes. For example, two drivers at `tdr1v1` and `tdr1v2` share the `tdr1v` prefix, indicating they are in the same geographic region.
- Geohash translates geographic locality into string similarity, allowing B+ Tree databases to cluster nearby locations together on disk.

### Important Facts
- **Geohash does not replace coordinates:** It acts as an additional index key. Systems must still store exact latitude and longitude for precise distance calculations and mapping.
- **Geohash discovers candidates, not the exact nearest neighbor:** Geohash reduces a massive dataset (e.g., 10 million drivers) to a small candidate pool (e.g., 200 drivers). Exact distance calculations (like Haversine) and ETA routing determine the true nearest/fastest driver.

## 2. How Geohash is Generated

Geohash recursively divides the Earth's surface into smaller geographic grids (cells). Each cell is assigned a unique alphanumeric string. 
- The longer the Geohash string, the smaller the geographic region, which corresponds to higher precision.
- The shorter the string, the larger the region and the lower the precision.
- **Hierarchy:** Recursive subdivision naturally creates a hierarchy. For example, `tdr` is a large area; `tdr1` is a subdivision of `tdr`; `tdr1v` is a subdivision of `tdr1`.

## 3. Precision Levels and Tradeoffs

The length of a Geohash dictates its precision:
- 1 character corresponds to thousands of kilometers.
- 8 characters correspond to approximately 40 meters.

**The Precision Tradeoff:**
- **Too coarse (short string):** A single cell covers too large an area, resulting in too many candidates to filter through, degrading performance.
- **Too fine (long string):** A search radius will span across an excessive number of tiny cells, requiring too many neighboring cell lookups. 
- **Dynamic Precision:** The optimal precision depends on search radius and object density. Systems can dynamically expand search areas by dropping characters from the end of the Geohash (e.g., expanding from `tdr1v2` to `tdr1v`).

## 4. Searching By Prefix

Because strings are ordered in databases, rows sharing prefixes are clustered together. 
- Querying with `WHERE geohash LIKE 'tdr1v%'` does **not** trigger a full table scan. Instead, when properly indexed, it executes as a highly efficient index range scan.
- This efficiently isolates the subset of database rows that correspond to a specific geographic area.

## 5. The Boundary Problem and Neighbor Cell Search

The most significant limitation of Geohash is the **Boundary Problem**. Geohash divides continuous space into discrete rectangular buckets. Two physically close objects (e.g., 5 meters apart) may fall on opposite sides of a Geohash cell boundary. In this scenario, they will have completely different Geohash prefixes despite their proximity.

### The Solution: Neighbor Cell Search
Searching only the user's current Geohash cell is insufficient and will miss nearby candidates across the boundary. To mitigate this:
1. Generate the Geohash for the user's location.
2. Use a Geohash library to calculate the 8 neighboring cells (North, South, East, West, NE, NW, SE, SW).
3. Query candidates across all 9 cells (e.g., `WHERE geohash IN (current, n, s, e, w, ne, nw, se, sw)`).

### Limitations of Neighbor Search
Neighbor search reduces boundary issues but does not perfectly solve spatial querying:
- Geohash cells are rectangular, while real-world search radiuses are circular. This inherent mismatch leads to false positives that must be filtered out.
- If the precision is too fine relative to the search radius, 9 cells will not cover the desired area.

## 6. Geohash in Production

### System Flow
1. **Location Updates:** A driver's phone sends `(lat, lon)`. The backend recomputes the Geohash and updates the data store.
2. **Candidate Retrieval:** A user requests a ride from a specific location. The backend computes the user's Geohash, determines the neighboring cells, and fetches candidate drivers using an `IN` query.
3. **Filtering:** The system calculates exact distances (Haversine) for the returned candidates, computes routing/ETAs, and ranks the best drivers.

### Storage and Caching
Because active driver locations update frequently, they are considered "hot data."
- **Redis:** Systems frequently use Redis as a memory-first store to bucket active drivers by Geohash, allowing low-latency, high-throughput reads and writes.
- **Persistent DB Schema:** A persistent database (like MySQL or PostgreSQL) might store the driver profile alongside location data (`driver_id`, `latitude`, `longitude`, `geohash`, `status`, `last_updated_at`).

### Sharding and Hotspots
Geohash prefixes can be used as sharding keys (e.g., `td*` on Server A, `te*` on Server B) to maintain spatial data locality—drivers physically near each other reside on the same shard. 
- **Limitation (Hotspots):** Because Geohash assumes uniform cell sizes, highly dense urban areas will receive massive traffic, while rural areas receive very little. This uneven data distribution leads to overloaded partitions (hotspots). This limitation is a key reason modern systems sometimes prefer adaptive structures like Quadtrees or libraries like H3/S2.
