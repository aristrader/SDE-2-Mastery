---
order: 20
search: false
---

# Solutions: Domain-Driven Design & Bounded Contexts

## Solution: bounded-context-decomposition - Decomposing a God Customer Model

### 1. Bounded Context Seams & Specialized Models

| Bounded Context | Responsibility | Owned Attributes | Explicitly Excluded |
| :--- | :--- | :--- | :--- |
| **Identity & Access (IAM)** | Authentication, credentials, and account security | `user_id`, `email`, `password_hash`, `mfa_secret` | Addresses, loyalty metrics, support tickets |
| **Billing & Payments** | Tax computation, invoicing, and payment processing | `customer_id`, `billing_street`, `billing_city`, `billing_zip`, `tax_exempt_id` | Shipping instructions, password hash, loyalty points |
| **Fulfillment & Logistics** | Physical package routing, shipping, and carrier dispatch | `customer_id`, `recipient_name`, `phone_number`, `shipping_street`, `shipping_city`, `shipping_zip`, `delivery_instructions` | Tax exemptions, MFA secrets, account passwords |
| **Loyalty & Promotions** | Rewards accrual, campaigns, and tier management | `customer_id`, `loyalty_tier`, `loyalty_points`, `preferred_category`, `last_campaign_id` | Billing details, shipping addresses, auth credentials |
| **Customer Support / Trust** | Incident resolution, agent routing, and fraud risk | `customer_id`, `open_ticket_count`, `last_support_agent_id`, `risk_score` | Passwords, raw credit cards, tax IDs |

### 2. Cross-Context Referencing Strategy
- **Shared Identifier via Value Object**: Contexts reference the customer using an immutable, opaque identifier (`CustomerId` UUID).
- **Zero Shared Tables / ORMs**: Each context owns its private datastore and schema. No foreign key constraints or ORM entity sharing across context boundaries.

---

## Solution: cross-context-data-sync - Autonomy vs Synchronous Coupling in Order Placement

### 1. Risks of Synchronous REST Dependency
- **Availability Degradation**: Availability multiplies across dependencies ($A_{total} = A_{order} \times A_{billing} \times A_{customer} \times \dots$). If `CustomerService` slows down or fails during peak traffic (flash sales), all checkouts fail.
- **Latency Amplification**: Synchronous HTTP hops add network I/O and thread blocking to the critical checkout path.
- **Temporal Coupling**: Fulfillment and Billing cannot make progress if `CustomerService` undergoes maintenance or experiences a network partition.

### 2. Autonomous DDD Architecture
- **Event-Carried State Transfer & Order Snapshots**:
  - When checkout initiates, an immutable **point-in-time snapshot** (`DeliveryAddress`, `BillingDetails`) is captured directly into the `OrderPlaced` event and embedded within the `Order` aggregate.
  - Billing and Fulfillment process orders strictly from data embedded in the order payload without querying `CustomerService` at runtime.
- **Asynchronous Local Projections**: If contexts need customer profile data outside active checkout, they consume domain events (`CustomerAddressChanged`, `TaxIdUpdated`) asynchronously to maintain local read projections.
### 3. The "Address Update Trap"
- **Profile vs. Snapshot**: Profile address edits represent *future* intent for subsequent checkouts. Existing in-flight orders must remain bound to the immutable `OrderSnapshot` created at checkout.
- **Explicit Domain Flow for Modifications**: Changing an active delivery destination cannot be a side-effect of a profile edit. It requires an explicit business command (e.g., `RerouteOrder` or `UpdateOrderDeliveryAddress`), allowing Fulfillment to validate fulfillment state (e.g., rejecting if the parcel is already dispatched or recalculating shipping fees).

