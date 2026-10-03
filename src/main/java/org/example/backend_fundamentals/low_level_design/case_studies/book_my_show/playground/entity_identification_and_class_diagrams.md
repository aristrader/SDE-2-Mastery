# BookMyShow — entity identification and class diagrams

## Goal

Model the smallest useful in-memory movie-ticket system that searches shows, holds seats temporarily,
and confirms a booking only after a payment outcome.

## Requirements

- Theatres contain screens, and screens have fixed uniquely identified seats.
- Shows schedule movies on screens and own independent seat availability.
- Search filters shows by movie title, city, and calendar date.
- A request holds one or more distinct available seats for one show for five minutes.
- A held or booked seat makes the full hold request fail without reserving its other seats.
- Expired holds release all held seats; payment success confirms and payment failure releases them.
- A repeated idempotency key returns its original hold or booking result without another payment attempt.
- Reject unknown IDs, empty/duplicate seat selections, and confirmation after expiry.

## Constraints

- Keep data in memory and use a simulated payment result.
- Use explicit expiry processing; no background worker.
- The base implementation is single-threaded.

## Test scenarios

- Search a movie's shows by city and date.
- Hold multiple available seats.
- Reject an all-or-nothing hold with one unavailable seat.
- Release holds after expiry or payment failure.
- Confirm paid seats and prevent later holds for the same show.
- Return the original result for an idempotent retry.

## Interview follow-ups

- How would concurrent requests reserve the same show-seat safely across instances?
- How would you process large numbers of expiring holds efficiently?
- How would cancellation, refunds, waitlists, notifications, and dynamic pricing fit in?
- How would persistence and payment-timeout recovery change the model?

## Entity identification


## Class diagrams
