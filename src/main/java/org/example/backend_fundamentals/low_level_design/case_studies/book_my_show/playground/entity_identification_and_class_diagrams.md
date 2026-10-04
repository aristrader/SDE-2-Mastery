# BookMyShow — entity identification and class diagrams

## Goal

Model the smallest useful in-memory movie-ticket system that searches shows, holds seats temporarily,
and confirms a booking only after a payment outcome.

This is the agreed scope after discussing the initial [interviewer prompt](../exercise/problem_statement/) and
[candidate clarifications](../exercise/candidate_discussion/).

## Requirements

- A theatre belongs to a city and contains one or more screens.
- A screen has a fixed set of uniquely identified seats. A show schedules one movie on one screen at a start time.
- A caller can search shows by movie title, city, and calendar date.
- One booking request selects one show and one or more distinct seat IDs.
- Each show tracks each seat as available, held, or booked independently of other shows on the same screen.
- An available requested seat may be held for five minutes. Reject the full request if any selected seat is held
  or booked.
- An expiry operation releases every seat in a hold whose expiry time has passed.
- Payment success converts the entire valid hold to a confirmed booking. Payment failure releases the hold.
- A repeated request with the same idempotency key returns the existing booking or hold result without a new
  payment attempt.
- Reject unknown show or seat IDs, empty or duplicate seat selections, and confirmation after hold expiry.

## Constraints

- Keep all data in memory.
- Use a simulated payment outcome and an explicit expiry check; do not create background workers.
- The base implementation is single-threaded. Concurrent claims on the same show are a follow-up.

## Test scenarios

- Find shows for a movie in a city on a given date.
- Hold multiple available seats for one show.
- Reject a hold when one requested seat is already held or booked, leaving all other requested seats unchanged.
- Release all held seats after payment failure or explicit expiry processing.
- Confirm a paid booking and prevent those seats from being held again for that show.
- Return the original result for a repeated idempotency key.

## Interview follow-ups

- How would you protect a show-seat hold against concurrent requests across JVM instances?
- How would you process millions of expiring holds efficiently rather than scanning every hold?
- How would cancellation, refunds, waitlists, notifications, and dynamic pricing change the model?
- How would you persist holds, bookings, and payment attempts and recover after a payment timeout?
- How would search scale separately from the transactional seat-reservation boundary?

## Entity identification


## Class diagrams
