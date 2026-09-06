# Weekend TODO

Use this as the next short execution list. Mark Done only after either coding it once or explaining the design out loud for 2 minutes.

| # | Task | Type | Status | Notes |
|---|------|------|--------|-------|
| 1 | Splitwise | LLD | [ ] | Domain model, balances, settlement |
| 2 | Rate Limiter | LLD | [ ] | Token bucket / sliding window; keep concurrency in mind |
| 3 | Elevator | LLD | [ ] | State, scheduling, request assignment |
| 4 | Logger | LLD | [ ] | Levels, appenders/sinks, formatting |
| 5 | Vending Machine | LLD | [ ] | Inventory, state transitions, payment/change strategy |
| 6 | BookMyShow | LLD | [ ] | Catalogue/search boundary, seat lock + expiry, booking, payment status/idempotency, coupons |
| 7 | Coupon / Promotion Rules | LLD | [ ] | Eligibility, discount strategies, redemption, stacking rules |
| 8 | E-commerce Checkout | LLD | [ ] | Catalogue, cart, inventory reservation, coupon, order and payment states |
| 9 | Revisit Spring Chunk 6 | Spring | [ ] | Security, Feign, caching, async MDC |
| 10 | CompletableFuture + ExecutorService | Java concurrency | [ ] | Pool sizing, shutdown, composition, exception handling |

## Suggested Order

1. Splitwise
2. Vending Machine
3. Rate Limiter
4. Elevator
5. Logger
6. BookMyShow
7. Coupon / Promotion Rules
8. E-commerce Checkout
9. Revisit Spring Chunk 6
10. CompletableFuture + ExecutorService
