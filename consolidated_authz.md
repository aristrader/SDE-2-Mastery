# Authentication & Authorization Study Notes

This document consolidates key technical concepts for Authentication and Authorization, specifically tailored for Java Backend systems using Spring Security and JWT.

## 1. Access Control Models

*   **RBAC (Role-Based Access Control)**
    *   **Concept:** Users are assigned *Roles*, and Roles are collections of *Permissions*.
    *   **Hierarchy:** Higher roles can inherit permissions from lower roles (e.g., `ADMIN` inherits from `MANAGER`).
    *   **Pros:** Easy maintenance and scalability. You update a role's permissions instead of individual users.
    *   **Example:** `User (Swapnil) -> Role (ADMIN) -> Permissions (READ_USER, DELETE_USER)`.

*   **ACL (Access Control List)**
    *   **Concept:** Permissions are attached directly to each *Resource*.
    *   **Example:** `Salary.xlsx -> Swapnil (Read/Write), Rahul (Read)`.
    *   **Cons:** Doesn't scale well with millions of resources (e.g., Google Drive, Linux file system permissions).

*   **ABAC (Attribute-Based Access Control)**
    *   **Concept:** Authorization depends on attributes (User, Resource, Action, Environment) and conditions.
    *   **Example:** Allow Manager to approve leave ONLY IF `Manager.Department == Employee.Department`.
    *   **Comparison:** RBAC is role-based; ABAC is context/attribute-based.

## 2. Spring Security Architecture

The core flow is: **Authenticate -> Authorize**. Everything happens in a filter chain before the controller executes.

### The Flow
1.  **Client Request:** HTTP Request arrives.
2.  **Spring Security Filter Chain:** Intercepts the request.
    *   **JWT Filter:** Reads the token, verifies the signature, checks expiry, extracts claims, and creates an `Authentication` object.
3.  **SecurityContext:** Stores the `Authentication` object for the current request.
4.  **Authorization:** Checks permissions (e.g., `@PreAuthorize`).
5.  **Controller:** Executes if authorized; otherwise, returns `403 Forbidden`.

### Key Components
*   **Authentication Object:** Contains the `Principal` (e.g., email), `Authorities` (e.g., `ROLE_ADMIN`, `READ_ORDER`), the `Authenticated` flag, and `Credentials`.
*   **SecurityContext:** Think of it as the current request's security information.
*   **SecurityContextHolder:** Provides global access to the `SecurityContext` (e.g., `SecurityContextHolder.getContext().getAuthentication()`).
*   **AuthenticationManager & UserDetailsService:** Primarily associated with username/password authentication. In modern JWT + Keycloak flows, they are often bypassed as the token is verified locally without querying the application's database.

## 3. Method Security vs. URL Security

*   **URL Security (e.g., `.requestMatchers("/admin/**").hasRole("ADMIN")`):** Protects endpoints at the routing level.
*   **Method Security (e.g., `@PreAuthorize("hasRole('ADMIN')")`):** Protects business logic, regardless of how or from where the method is invoked.
*   **Best Practice:** Combine both for **Defense in Depth**.

### @PreAuthorize Expressions
*   `hasRole('ADMIN')`: Checks for `ROLE_ADMIN`. (Roles represent job responsibilities).
*   `hasAuthority('DELETE_ORDER')`: Checks for the exact string `DELETE_ORDER`. (Authorities represent specific capabilities and are more reusable).
*   `isAuthenticated()`: Checks if the user is logged in (ignores roles/authorities).
*   **Note:** `@PreAuthorize` does *not* read the JWT. It reads the authorities from the `Authentication` object in the `SecurityContext`.

## 4. JWT & Keycloak Integration

*   **Responsibilities:**
    *   **Keycloak (Identity Provider):** Manages users, roles, groups, handles login, and issues JWTs. Keycloak does *not* authorize application requests.
    *   **Spring Security (Resource Server):** Consumes JWTs, verifies signatures, and performs authorization.
*   **Statelessness:** By configuring `SessionCreationPolicy.STATELESS`, no HTTP sessions are created. Every request must send a JWT.
*   **Public Keys (JWK):** Keycloak signs multiple JWTs with the same private key. Spring Security downloads and caches Keycloak's public keys (via `jwk-set-uri`). The `kid` (Key ID) in the JWT header tells Spring which cached public key to use to verify the signature locally, avoiding network calls on every request.

### Misconceptions Corrected
*   **Myth:** Spring Security always authenticates using a database.
    *   **Fact:** Only for username/password login. In JWT flows, it verifies the token locally using public keys.
*   **Myth:** Each user has a separate public/private key pair.
    *   **Fact:** Keys belong to the Identity Provider (Keycloak), not individual users.

## 5. Custom Claims & Business Authorization

*   **Custom Claims:** JWTs can carry custom claims like `tenant_id`, `client_id`, or `organization_id` to avoid additional database calls.
*   **Business Authorization:** Sometimes, `@PreAuthorize` is insufficient because authorization depends on the request body or complex business rules (e.g., requiring specific permissions based on the requested service like OCR vs. Face Match). In these cases, authorization is handled programmatically in the business layer after the JWT is validated.

## 6. Multi-Tenant Authorization

*   **Tenant:** Refers to a customer organization (e.g., Google, Microsoft), not an individual user.
*   **Isolation:** Users must never see another tenant's data.
*   **Implementation:** Tenant IDs are often passed via JWT custom claims. When querying the database, always filter by `WHERE tenant_id = ?`.
*   **Golden Rule:** **Never** trust a `tenant_id` provided in request parameters. Always derive it from the authenticated JWT.

## 7. Defense in Depth

Never rely on a single security check. Use multiple independent layers:
1.  **Spring Security:** JWT signature validation.
2.  **Principal Injection:** Standardized request user context.
3.  **Domain Wrapper:** Extracting claims into a clean domain object (e.g., `VidaUser`).
4.  **Business Authorization:** Validating user capabilities against the requested operation.
5.  **Business Validations:** Checking entity states, configuration validity, etc.

## 8. Business Models & Deployment
*   **B2B (Business-to-Business):** Customers are other businesses (e.g., Stripe, Twilio).
*   **B2C (Business-to-Consumer):** Customers are end-users (e.g., Swiggy, Netflix).
*   **Deployment Models:**
    *   **Multi-Tenant SaaS:** One application instance serves many customers. Data is isolated via `tenant_id`.
    *   **Single-Tenant SaaS:** Vendor hosts separate infrastructure, application, and database for each customer. (Typical for enterprise/banks).
    *   **On-Premise:** Customer hosts and manages the software, servers, and networking. Vendor only supplies the software.

## 9. Security Operations
*   **Audit Logging:** Tracks security-sensitive actions for investigations and compliance (unlike application logs, which help developers debug).
    *   **Should Log:** User ID, Tenant/Client ID, Action, Resource, Timestamp, Decision (Allowed/Denied), and Reason.
    *   **Should NOT Log:** Passwords, JWTs, OTPs, or Sensitive Personal Information.
    *   **MDC (Mapped Diagnostic Context):** Tools like `MDC.put("client.id", clientId)` automatically enrich every log line, facilitating a natural audit trail.
*   **Step-up Authentication:** Requires a user who is already authenticated to provide stronger authentication (e.g., OTP) before performing high-risk operations (e.g., large fund transfer, disabling MFA). This balances security with user experience.

## 10. Additional Misconceptions Corrected
*   **Myth:** SecurityContext is specific to JWT.
    *   **Fact:** SecurityContext is independent of the authentication mechanism (JWT, OAuth, Session, etc., all populate the same `Authentication` object).
*   **Myth:** Spring Security features are "written on SecurityContext."
    *   **Fact:** Spring Security components *read* the `Authentication` stored inside the SecurityContext.
*   **Myth:** Interceptors (e.g., `HandlerInterceptor`) authenticate JWTs.
    *   **Fact:** Authentication happens earlier in the Spring Security filter chain. Interceptors only parse already-authenticated JWTs (e.g., to extract values for MDC logging).
*   **Myth:** URL-based security should contain all authorization logic.
    *   **Fact:** URL-based security only knows the URL. Complex authorization requiring business rules or request body analysis belongs in the Java/business layer.
*   **Myth:** `tenant_id` or `MMID` require special JWT handling.
    *   **Fact:** They are usually just standard custom JWT claims and can be extracted easily.
