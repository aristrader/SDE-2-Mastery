---
order: 20
search: false
---

# Design notes — BookMyShow

Use this page after modelling the exercise. The central distinction is between a screen's reusable seat layout
and one show's mutable availability for those seats.

The critical state transition is `AVAILABLE → HELD → BOOKED`. Payment failure or expiry transitions a hold
back to available. Every selected seat must transition together, or none of them should.

## Quick recall

- Layout is per screen; availability is per show.
- A hold has an owner and expiry time.
- Payment success confirms; failure and expiry release.
