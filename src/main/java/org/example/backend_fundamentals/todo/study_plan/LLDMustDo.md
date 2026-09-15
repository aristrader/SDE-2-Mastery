# LLD must do

Use this as the focused interview-practice list. Mark Done only after coding it once and explaining the design out loud for 2 minutes.

| # | Task | Type | Status | Notes |
|---|------|------|--------|-------|
| 1 | Parking Lot | LLD | [x] | Allocation, pricing, ticket lifecycle, and concurrency extension |
| 2 | Logger | LLD | [x] | Levels, sinks, formatting, asynchronous producer-consumer flow |
| 3 | Tic-Tac-Toe | LLD | [ ] | Java warm-up: board state, coordinates, validation, and win rules |
| 4 | Vending Machine | LLD | [ ] | Inventory, state transitions, payment/change strategy |
| 5 | Splitwise | LLD | [ ] | Domain model, balances, settlement |
| 6 | LRU Cache | LLD | [ ] | HashMap + doubly linked list; eviction and O(1) operations |
| 7 | Rate Limiter | LLD | [ ] | [Video](https://www.youtube.com/watch?v=7y0KWxaUn-E&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=4) · Token bucket / sliding window; keep concurrency in mind |
| 8 | Elevator | LLD | [ ] | State, scheduling, request assignment |
| 9 | Coupon / Promotion Rules | LLD | [ ] | Eligibility, discount strategies, redemption, stacking rules |
| 10 | E-commerce Checkout | LLD | [ ] | Catalogue, cart, inventory reservation, coupon, order and payment states |
| 11 | BookMyShow | LLD | [ ] | [Video](https://www.youtube.com/watch?v=dC-lz_GGTL4&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=9) · Catalogue/search boundary, seat hold + expiry, booking, payment status/idempotency, coupons |
| 12 | File System | LLD | [ ] | Low priority: folders/files, paths, tree hierarchy, and Composite-style modelling |
| 13 | Pub-Sub | LLD | [ ] | Low priority: topics, subscribers, fan-out delivery, and ordering boundaries |
| 14 | Snake & Ladder | LLD | [ ] | [Video](https://www.youtube.com/watch?v=L7hvkK188Ek&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=15) · Low priority, watch/read only: board state, dice, and player movement |
| 15 | Chess | LLD | [ ] | [Video](https://www.youtube.com/watch?v=Fx6Z9Jfvk0o&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=12) · Low priority, watch/read only: piece movement, rule validation, and extensible move strategies |

## Suggested Order

1. Parking Lot — completed
2. Logger — completed
3. Tic-Tac-Toe
4. Vending Machine
5. Splitwise
6. LRU Cache
7. Rate Limiter
8. Elevator
9. Coupon / Promotion Rules
10. E-commerce Checkout
11. BookMyShow
12. File System — low priority
13. Pub-Sub — low priority
14. Snake & Ladder — low priority, watch/read only
15. Chess — low priority, watch/read only

## Variants and company-specific extras

These are useful references, but are not separate must-do implementations.

| Problem | Video | Closest current problem / reason |
|---|---|---|
| Connect Four | [Video](https://www.youtube.com/watch?v=9UI4ikKP3Ws) | Tic-Tac-Toe: board state, turn validation, and row/column/diagonal win checks; Connect Four adds gravity-based column placement |
| Car Rental System | [Video](https://www.youtube.com/watch?v=H68JE5U7Qvw&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=13) | Parking Lot and BookMyShow: availability, reservation, and lifecycle rules |
| FlipMed | [Video](https://www.youtube.com/watch?v=eh4e1VJ05Gc&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=10) | Company-specific workflow; no confirmed parent overlap from the title alone |
| Customer Issue Resolution System | [Video](https://www.youtube.com/watch?v=vHwOyuaEtIk&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=8) | Company-specific case/ticket workflow; no direct must-do equivalent |
| Uber / Ride Sharing | [Video](https://www.youtube.com/watch?v=fWWY6He81nk&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=7) | Separate ride-matching and geospatial domain; broader than the current list |
| ATM | [Video](https://www.youtube.com/watch?v=GSuaqBR0Wpc&list=PLYPO3T7Sl63u7uLLpiKCMXnRjeFIhUAvk&index=6) | Vending Machine: state transitions, validation, inventory, and dispensing |
