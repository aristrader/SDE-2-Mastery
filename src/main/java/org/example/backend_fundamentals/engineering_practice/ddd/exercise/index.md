---
order: 10
search: false
---

# Exercises: Domain-Driven Design & Bounded Contexts

Practice decomposing legacy monolith entities and drawing robust bounded context seams for senior backend architecture interviews.

## Exercise: bounded-context-decomposition - Decomposing a God Customer Model

### Scenario
You are modernizing a monolithic retail platform where a central `customers` table has grown to 40+ columns over 6 years. Teams regularly block each other on schema migrations, and a bug in the loyalty points calculation recently corrupted checkout billing addresses.

The current bloated entity contains:
- `id`, `email`, `password_hash`, `mfa_secret`
- `first_name`, `last_name`, `phone_number`
- `billing_street`, `billing_city`, `billing_zip`, `tax_exempt_id`
- `shipping_street`, `shipping_city`, `shipping_zip`, `delivery_instructions`
- `loyalty_tier`, `loyalty_points`, `preferred_category`, `last_campaign_id`
- `open_ticket_count`, `last_support_agent_id`, `risk_score`

### Task
1. Delineate the natural **Bounded Contexts** present in this system.
2. For each context, define its specialized `Customer` model (owned attributes, primary responsibility, and what is explicitly excluded).
3. Specify how contexts reference the customer entity without sharing database tables or ORM classes.

### Acceptance Criteria
- Identify at least 4 distinct bounded contexts.
- No context holds attributes outside its domain responsibility.
- State the identifier strategy used across context boundaries.

---

## Exercise: cross-context-data-sync - Autonomy vs Synchronous Coupling in Order Placement

### Scenario
During checkout, the **Fulfillment Context** needs customer delivery preferences and recipient address, while the **Billing Context** needs tax identification and payment instruments.

A junior engineer proposes:
> *"Whenever Fulfillment or Billing processes an order, they should make a synchronous REST API call to `CustomerService.getCustomer(id)` to ensure they have the latest data."*

### Task
1. Evaluate the availability, latency, and consistency risks of this synchronous dependency during high-load events (e.g., flash sales).
2. Propose a DDD-aligned architectural pattern to keep Fulfillment and Billing autonomous without coupling runtime availability to Customer Service.
3. Explain how to handle the "Address Update Trap": what should happen if a customer edits their profile address while an order is in transit?

### Acceptance Criteria
- Clearly contrast temporal coupling with autonomy via local projections / snapshots.
- Provide a concrete event-driven or snapshot-based design.
- Define whether placed orders should track mutable customer profiles or point-in-time domain snapshots.
