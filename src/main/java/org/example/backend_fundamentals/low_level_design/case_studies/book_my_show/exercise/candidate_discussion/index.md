---
search: false
---

# Candidate discussion — BookMyShow

Start by proposing a bounded first scope:

> “I’ll model one city with theatres, screens, shows, and fixed seat layouts. A customer can search shows,
> hold multiple available seats for five minutes, then complete one simulated payment to confirm a booking.
> A failed or expired hold releases its seats.”

Then confirm the decisions that change the data model and reservation flow.

## Questions to ask

1. Must users search by movie, city, date, theatre, or all of these filters?
2. Does one screen host many shows at different times with its own independent seat availability?
3. Can one booking contain multiple seats? Are seat types and different prices required?
4. Are seats held before payment? How long does a hold last and how is expiration processed?
5. What happens if two users attempt to hold the same seat simultaneously?
6. Which payment outcomes exist: success and failure only, or pending/refund too?
7. Should a repeated booking request with the same key return its original booking?
8. Are cancellation, refunds, waitlists, dynamic pricing, notifications, persistence, and multi-city scaling part
   of this round?

## Agreed scope for this exercise

- One in-memory system contains theatres, screens, fixed screen seat layouts, movies, and shows.
- A caller can search shows by movie title, city, and date.
- One booking request may hold multiple seats for one show.
- Each show has independent seat state: available, held, or booked.
- Holding seats creates a five-minute hold. A hold cannot include a held or booked seat.
- An explicit expiry check releases all seats in an expired hold.
- A simulated payment outcome either confirms the booking or releases its held seats.
- A booking idempotency key returns an existing booking for a retry without creating another hold or payment.
- Concurrent seat claims, cancellations/refunds, notifications, persistence, dynamic pricing, and waitlists are
  follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
