---
order: 100
---

# Proximity Services

Use this pattern when a request means "find entities near this latitude/longitude": drivers near a rider, restaurants near a customer, or stores near a delivery address. For a few hundred or roughly a thousand items, scan and calculate distance; a distributed spatial index is unnecessary overhead.

## Candidate funnel

```text
query point -> cell and neighboring cells -> cheap eligibility filter
            -> exact distance -> route ETA / ranking for a bounded candidate set
```

Geohash, H3, S2, PostGIS, Redis GEO, and Elasticsearch geo queries are implementation choices. The essential idea is to reduce the search space with a spatial cell/index before performing exact or expensive computations.

## Practical choices

| Need | Suitable approach | Important caveat |
|---|---|---|
| Small, mostly static set | In-process scan or database query | Keep it simple; measure first |
| Moderate durable data | Database spatial index, for example PostGIS | Query/update rate must fit the database path |
| Very frequent latest-location updates | Redis/in-memory geo index plus durable async history | Index is rebuildable and must expire stale entities |
| Global/local operation | Region/city ownership plus local index | A region is not the same thing as a spatial cell |

Always search neighbouring cells. Two entities can be physically adjacent yet placed in different cells by a grid boundary. Filter stale locations before ranking; exact distance is not useful if the driver stopped updating minutes ago.

## Quick recall

**Q. Why not scan every driver/location?**
A. It makes each request proportional to the global entity count. A spatial index narrows it to a small local candidate set.

**Q. What comes after the cell lookup?**
A. Cheap availability filters, then exact distance, then expensive route/ETA ranking for only the best candidates.
