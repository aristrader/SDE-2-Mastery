# BookMyShow — entity identification and class diagrams

## Goal

Model the smallest useful in-memory movie-ticket system that searches shows, holds seats temporarily,
and confirms a booking only after a payment outcome.

This is the agreed scope after discussing the initial [interviewer prompt](../exercise/problem_statement/) and
[candidate clarifications](../exercise/candidate_discussion/).

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

## Entity identification
Theater
Movie
Customer
Screen
Booking
Seat

## Class diagrams

PaymentService
- payments : Map<int paymentId, Payment payment>
+ handlePaymentCallBack(int paymentId, PaymentStatus paymentStatus)
  -> update the paymentstatus
+ initiatePayment(int amount) -- ideally multiple payment providers so we have resolver and then we resolve and call accordingly using strategy pattern

MovieBookingService
- movies : Map<int movieId, Movie movie>
- screens : Map<int screenId, Screen screen>
- showsQuickSearch : Map<MovieCityDate{title, city, calendarDate}, List<Shows>>
- shows : Map<int showId, Show show>
- bookings : Map<int bookingId, Booking booking>
+ addNewShow(int movieId, LocalDateTime startTime, int screenId, int cityId, int theaterId)
  -> do basic validations that movieId exists, screenId exsits
  -> create a new show, the seats map created using the seatmap in the screen
  -> populate the show in the screen and also the shows map in this file
+ BookingResponse bookSeat(int showId, List<List<int>> seats, PaymentMode paymentMode) // seats is list of row and col representing the seats or we can do directly List of seatIds and traverse the seats like to know which seatid matches the supplied seatids
  -> do basic validation showId and seats exist and are not held or booked
  -> mark all seats as held
  -> initiate a payment request for the amount (all seats cost same)  - paymentService.initiatePayment
+ isBookingConfirmed(int bookingId)
  -> check the status of payment - PaymentService.isPaymentSuccessful(int paymentId)
+ expireBookings() -- cron job calls this and expires the booking that are older than 10 minutes
  -> update the booking status to expired
  -> free up the bookings

BookingResponse
- bookingId : int
- redirectUrl : string (payment gateway url)
- qr : string
- paymentId : int

Payment
- id
- status

PaymentStatus
- INITIATED
- IN_PROGRESS
- PAID
- FAILED

PaymentMode
- CARD
- UPI

Movie
- id : int
- name : string
- rating : BigDecimal
- runtime : int (minutes/seconds)

Seat
- id : int
- status : SeatStatus

SeatStatus
- held
- booked
- available

Show
- id
- movieId : int
- startTime : LocalDateTime
- seats : List<List<Seat>> // the seats get created with the help of the map in the screen

Screen
- id : int
- shows : List<Show>
- seatMap : List<List<boolean>>

Theater
- id : int
- name : string
- screens : List<Screen>

City
- id : int
- name : String
- theaters : List<Theater>

Customer
- id : int
- name : string

Booking
- id : int
- cityId : int
- theaterId : int
- showId : int
- customerId : int
- seats : list<SeatId>
- paymentId : int
- bookingStatus : BookingStatus
- reservationExpiresAt : LocalDateTime

BookingStatus
- INITIATED
- PAID
- CANCELLED
- TIMEOUT/EXPIRED