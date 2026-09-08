---
order: 50
---

# Designing REST API Endpoints

Endpoint design is the public vocabulary of a backend. A good endpoint lets a new API consumer predict
how the rest of the API behaves. A poor endpoint leaks controller methods, database tables, or one-off
implementation decisions and forces every consumer to memorize exceptions.

The goal is not to make every route look academically RESTful. The goal is a stable contract that models
business concepts clearly, uses HTTP consistently, and stays easy to extend.

This chapter builds an endpoint design from first principles using a transaction-verification API. The
same reasoning applies to users, orders, bookings, tasks, payments, or any other domain.

## 1. Start with the resource, not the controller method

An API should expose the business things a client works with. These are **resources**. In a verification
system, the useful resources might be transactions, documents, images, review cases, and verification
attempts. They are not methods such as `getTransaction`, `createTransaction`, or database tables such as
`transaction_records`.

Before naming a route, ask:

> What business thing is the client trying to create, read, change, or delete?

If the answer is "a transaction", start with `/transactions`. The HTTP method carries the normal action:

```http
GET    /v1/transactions                  # Read a collection
POST   /v1/transactions                  # Create a transaction
GET    /v1/transactions/{transactionId}  # Read one transaction
PATCH  /v1/transactions/{transactionId}  # Change part of it
DELETE /v1/transactions/{transactionId}  # Delete it, if deletion is allowed
```

This is why verb-heavy paths are redundant:

```http
POST /create-transaction                 # Avoid
GET  /get-transaction/{transactionId}    # Avoid
POST /transactions                        # Prefer
GET  /transactions/{transactionId}        # Prefer
```

The method says **what happens**. The path says **what it happens to**.

### Collections and items

A collection and an item are different resources. `/transactions` means the transaction collection;
`/transactions/{transactionId}` means one transaction in it. This pattern makes the API predictable:

| Client intent | Resource-oriented endpoint |
|---|---|
| List transactions | `GET /v1/transactions` |
| Read transaction `txn_98234` | `GET /v1/transactions/txn_98234` |
| Create a transaction | `POST /v1/transactions` |
| Replace a transaction | `PUT /v1/transactions/txn_98234` |
| Change its status | `PATCH /v1/transactions/txn_98234` |
| Delete it | `DELETE /v1/transactions/txn_98234` |

`PUT` usually means the client sends a complete replacement representation. `PATCH` means the client
sends only the fields to change. `PUT` must be idempotent: sending the same request again leaves the
resource in the same state. A `PATCH` can be idempotent, but only if its patch semantics make it so.

## 2. Give resources stable names

Once the resource is clear, name it so that the route is easy to read and stays stable over time.

### Use plural collection nouns

Plural nouns remove an unnecessary special case:

```http
GET /users
GET /users/{userId}
GET /orders
GET /orders/{orderId}
```

Use a singular segment only for a true singleton in an already-known context. A user has one profile, so
`GET /users/{userId}/profile` is natural. `/me` is also reasonable when it always means the authenticated
caller.

### Use lowercase, readable, consistent paths

Use lowercase URI segments. For multiword names, kebab case is a common choice:

```http
/managed-devices
/review-cases
/verification-attempts
```

Do not mix `system-logs`, `system_logs`, and `systemLogs` without a deliberate API-wide convention. The
important rule is consistency. URI paths should represent business language, not Java class names or
database names:

```http
/transactions           # Business term
/transaction_table_rows # Database-shaped API: avoid
```

Renaming a public path is a contract change even if the underlying implementation did not change.

## 3. Decide between a path variable and a query parameter

This is one of the most common API-design decisions. The rule is simple:

> Use the path to identify the resource or required parent scope. Use the query string to shape an
> otherwise valid request.

### Path variables identify where to operate

The value is part of the resource's address. Without it, the request cannot identify the target.

```http
GET /v1/transactions/{transactionId}
PATCH /v1/users/{userId}
GET /v1/projects/{projectId}/tasks
```

`transactionId` identifies one transaction. `projectId` identifies the project whose task collection is
being requested. These values are required by the meaning of the route.

### Query parameters modify the request

The base route remains valid if the parameter is absent. Parameters commonly filter, sort, paginate,
project fields, or ask for related data:

```http
GET /v1/transactions?status=COMPLETED
GET /v1/transactions?sort=-createdAt&limit=50&cursor=eyJpZCI6MTIzfQ
GET /v1/transactions?fields=id,status,createdAt
GET /v1/transactions?include=images,merchant
```

| Question | Put it in | Example |
|---|---|---|
| Which exact resource is this? | Path | `/transactions/{transactionId}` |
| Which parent collection is this under? | Path | `/projects/{projectId}/tasks` |
| Which subset should I return? | Query | `/transactions?status=COMPLETED` |
| In what order and page size? | Query | `/transactions?sort=-createdAt&limit=50` |
| What optional fields or relations should be expanded? | Query | `/transactions/{id}?include=images` |

`GET /transactions?transactionId=...` is not inherently wrong. It can be valid for an optional filter,
an external reference, or a multi-value lookup. When the client means "fetch this transaction by its
canonical ID", `GET /transactions/{transactionId}` communicates that intent better.

### Worked example: transaction details with optional images

Suppose the normal transaction representation is enough for most screens, but one screen needs images.
The transaction must be known; the images are optional output detail:

```http
GET /v1/transactions/{transactionId}
GET /v1/transactions/{transactionId}?include=images
```

The second request does not identify a different transaction. It requests a richer representation of the
same one. A single extensible `include` parameter is usually better than many booleans:

```http
GET /v1/transactions/{transactionId}?include=images,merchant,disputes
```

Avoid an expanding collection of flags such as `includeImages=true`, `includeMerchant=true`, and
`includeDisputes=true`. If images are large, independently addressable, or paginated, make them a
resource too:

```http
GET /v1/transactions/{transactionId}/images
GET /v1/images/{imageId}
```

### Do not put sensitive input in a URL

Paths and query strings are frequently recorded by access logs, proxies, browser history, traces, and
analytics tools. Do not place credentials, tokens, passwords, or sensitive personal data there.

For a complicated search with a large, structured filter, a query string can become unreadable or exceed
practical URL limits. A documented search operation with a request body can be the better exception:

```http
POST /v1/transactions:search
```

This does not mean every read should use `POST`; it is a deliberate choice for complex search criteria.

## 4. Model relationships without creating URL spaghetti

Nested routes are useful when the parent provides useful context for a child **collection**:

```http
GET  /v1/projects/{projectId}/tasks
POST /v1/projects/{projectId}/tasks
GET  /v1/customers/{customerId}/orders
```

The problem begins when every ancestor is repeated for a resource that already has its own globally unique
ID:

```http
DELETE /v1/users/{userId}/projects/{projectId}/tasks/{taskId}  # Too much path history
DELETE /v1/tasks/{taskId}                                      # Usually clearer
```

This is called **shallow routing**. Keep nesting to show collection context; flatten operations on a
specific child when its ID is enough to identify it.

| Need | Good route | Why |
|---|---|---|
| List tasks for project `p1` | `GET /projects/p1/tasks` | The project scopes the collection. |
| Update task `t7` | `PATCH /tasks/t7` | The task ID identifies the item. |
| Delete task `t7` | `DELETE /tasks/t7` | Parent IDs add no identity value. |

### When deeper nesting is correct

Do not flatten mechanically. Keep the parent if the child identifier is unique only within that parent or
if the parent is an essential part of the public identity:

```http
GET /v1/repositories/{owner}/{repository}/issues/{issueNumber}
```

An issue number may be unique only inside a repository. The repository context is therefore not redundant.

### The route is not an authorization rule

`DELETE /tasks/{taskId}` is not less secure than a deeply nested version. The backend reads the caller
from the authentication token, loads the task, and checks whether that caller is allowed to delete it.
Parent IDs in a URL are client-provided values; they do not prove ownership.

## 5. Keep collection operations in the query string

Filtering, sorting, pagination, and projections describe a view of a collection. They should not become
new path shapes for every variation:

```http
GET /v1/products/active        # Avoid: filter hidden in the path
GET /v1/products/sort-by-price # Avoid: ordering hidden in the path

GET /v1/products?status=active&sort=price_asc&limit=20 # Prefer
```

Document allowed filter values, default sort order, maximum page size, cursor format, and invalid
combinations. A client should know whether the server applies filter, sort, paginate, and projection in a
defined order. This is part of the API contract, not an implementation detail.

## 6. Handle actions that are not normal CRUD

Most client operations fit `GET`, `POST`, `PUT`, `PATCH`, and `DELETE` against a resource. Some actions do
not. Capturing a payment or retrying a job is not simply a field update from the client's perspective.

Use a documented custom action sparingly:

```http
POST /v1/payments/{paymentId}:capture
POST /v1/jobs/{jobId}:retry
```

First ask whether the action creates a durable business object. A retry might create an attempt that has
its own status, audit history, and timestamps. In that case, a resource model may be more expressive:

```http
POST /v1/jobs/{jobId}/attempts
```

Neither option is universally right. A command route emphasizes the operation; a subresource emphasizes
the object created by it. Pick the contract that best represents the domain and apply the convention
consistently.

## 7. Version a contract deliberately

Public APIs need a compatibility strategy. URI versioning is a common and practical default:

```http
GET /v1/transactions/{transactionId}
```

It is visible in traffic and easy to route, which is useful for partner integrations and SDKs. Header or
media-type versioning can also work, but one API should not mix strategies without a strong reason.

A new version is for a breaking contract change, not every release. Adding an optional response field is
normally compatible. Removing or renaming a field, changing its meaning, or changing required request
data may require a new version and a deprecation plan for the old one.

## 8. Design the endpoint set, then test its predictability

A small verification API might read as follows:

```http
POST  /v1/transactions
GET   /v1/transactions?status=REVIEW&limit=50
GET   /v1/transactions/{transactionId}
GET   /v1/transactions/{transactionId}?include=images
GET   /v1/transactions/{transactionId}/images
POST  /v1/transactions/{transactionId}:resubmit
GET   /v1/review-cases?assigneeId={userId}&status=OPEN
PATCH /v1/review-cases/{reviewCaseId}
```

Each route answers the same questions consistently:

1. What resource is being addressed?
2. Is this a collection, one item, a related collection, or a deliberate command?
3. Is every path value required to identify that target?
4. Are optional result-shaping choices in the query string?
5. Can a new client infer related routes without learning a special rule?

If the answer to the last question is repeatedly "no", the API probably needs a clearer resource model,
not another special-case endpoint.

## Common mistakes

| Mistake | Better design | Reason |
|---|---|---|
| `GET /get-all-users` | `GET /users` | The HTTP method already says read. |
| `POST /users/{id}/delete` | `DELETE /users/{id}` | Deletion is a standard resource operation. |
| `GET /orders/active` | `GET /orders?status=active` | Active is a collection filter. |
| `/users/{u}/projects/{p}/tasks/{t}` for every task action | `/tasks/{t}` | Flatten globally identifiable items. |
| `?includeImages=true&includeMerchant=true` | `?include=images,merchant` | One extensible expansion convention. |
| `/transaction_table_rows` | `/transactions` | Public APIs model domain concepts, not schema. |

## References

- [Microsoft: REST web API design](https://learn.microsoft.com/en-us/azure/architecture/best-practices/api-design)
- [Google AIP-121: Resource-oriented design](https://google.aip.dev/121)
- [Google AIP-136: Custom methods](https://google.aip.dev/136)

## Quick recall

**Q. How should I fetch transaction details and optionally include images?**

Use `GET /v1/transactions/{transactionId}?include=images`. The ID identifies the transaction; `include`
changes the representation returned.

**Q. When does a value belong in the path?**

When it identifies the exact resource or a required parent collection scope. Use a query parameter when it
filters, sorts, paginates, projects, or expands an otherwise valid request.

**Q. Why avoid deep nesting?**

If `taskId` is globally unique, `/tasks/{taskId}` is enough. Keep nesting for parent-scoped collections
or when the parent is required to identify the child.

**Q. When is a verb acceptable in an endpoint?**

For an actual non-resource command, such as `POST /payments/{paymentId}:capture`. First check whether the
operation should instead create a resource such as `/jobs/{jobId}/attempts`.

**Q. Does every API release need a new version?**

No. Add a new version for breaking contract changes. Additive backward-compatible fields normally do not
need one.
