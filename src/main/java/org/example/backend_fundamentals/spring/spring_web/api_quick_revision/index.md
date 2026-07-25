---
order: 60
---

# Spring API Quick Revision

Use this as the pre-implementation checklist before writing a Spring REST API in an interview or practice project. It links to the deeper pages instead of duplicating them.

## Controller shape

```java
@RestController
@RequestMapping("/parking")
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<TicketResponse> enter(@Valid @RequestBody EnterRequest request) {
        Vehicle vehicle = new Vehicle(request.getRegistrationNumber(), request.getVehicleType());
        Ticket ticket = parkingService.enterVehicle(vehicle, request.getEntryGateId());
        return ResponseEntity.ok(TicketResponse.from(ticket));
    }
}
```

- `@RestController` = `@Controller` + `@ResponseBody`.
- Controller methods should be `public`.
- Keep controller thin: validate request, map request to service input, map service output to response.
- Prefer constructor injection through `final` fields and `@RequiredArgsConstructor`.

Read next: [Spring REST](../rest/)

## URL and parameter choices

| Case | Use | Example |
|---|---|---|
| Identify one resource | `@PathVariable` | `/tickets/{ticketId}/quote` |
| Filter or query | `@RequestParam` | `/availability?vehicleType=CAR` |
| Structured command data | `@RequestBody` | `POST /entry` with JSON body |
| Client metadata | `@RequestHeader` | `X-Correlation-Id` |

Resource identity comes before action/sub-resource:

```text
/tickets/{ticketId}/quote
/tickets/{ticketId}/exit
```

Avoid `/tickets/quote/{ticketId}` because it puts the action before the resource identity.

Read next: [Spring MVC](../mvc/)

## HTTP method choices

| Method | Use when | Parking example |
|---|---|---|
| `GET` | Read only | `GET /parking/availability?vehicleType=CAR` |
| `POST` | Create or perform a command | `POST /parking/entry`, `POST /parking/tickets/{ticketId}/exit` |
| `PUT` | Replace/set a known resource completely | `PUT /tickets/{ticketId}` |
| `PATCH` | Partially update known resource | `PATCH /tickets/{ticketId}` |
| `DELETE` | Remove resource | `DELETE /tickets/{ticketId}` |

Read next: [HTTP Fundamentals](../../../networking/http_basics/)

## Request validation

```java
public class EnterRequest {
    @NotBlank
    private String registrationNumber;

    @NotNull
    private VehicleType vehicleType;
}
```

```java
@PostMapping("/entry")
public ResponseEntity<TicketResponse> enter(@Valid @RequestBody EnterRequest request) {
    ...
}
```

- `@NotNull`: value must not be `null`; good for enums and object fields.
- `@NotBlank`: string must not be `null`, empty, or spaces only.
- `@Valid` on `@RequestBody` triggers validation after Jackson deserialization.
- Domain constructors should still validate invariants; controller validation only protects API input.

## Exception handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_FAILED", "Invalid request"));
    }
}
```

Use custom exceptions when the error has domain meaning or needs different handling:

| Situation | Exception style | HTTP status |
|---|---|---|
| Request shape invalid | Bean Validation / `IllegalArgumentException` | `400` |
| Resource missing | `TicketNotFoundException` | `404` |
| Valid request conflicts with state | `NoValidSpotFoundException`, `TicketAlreadyClosedException` | `409` |
| Unexpected bug | fallback `Exception` handler | `500` |

Read next: [Spring Exception Handling](../exception_handling/)

## Spring wiring

```java
@Configuration
public class ParkingLotConfig {

    @Bean
    public ParkingLot parkingLot() {
        return new ParkingLot(...);
    }
}
```

- `@Service`, `@Repository`, `@Component`: classes Spring creates through component scan.
- `@Configuration` + `@Bean`: objects you create manually but still want Spring to inject.
- `@SpringBootApplication` should sit in a parent package of controllers, services, repositories, and config classes.

## Response shape

For practice, returning a domain object is acceptable. For API-quality code, return response DTOs:

```java
public class TicketResponse {
    private String ticketId;
    private String spotId;
    private Integer floorNumber;
    private Instant entryTime;
}
```

Reason: API response is a contract; domain model is internal state.

## Feign quick pointer

Feign is separate from the parking-lot app. Use it when your service calls another HTTP service.

Revise:
- `@FeignClient`
- method-level `@GetMapping` / `@PostMapping`
- `RequestInterceptor` for headers
- `ErrorDecoder` for upstream error mapping
- connect/read timeouts

Read next: [Feign / OpenFeign](../../spring_cloud/feign/)

## Quick recall

**Q. Path variable or request param for `vehicleType` availability?**
A. `@RequestParam`, because vehicle type is a filter, not a resource id.

**Q. Path variable or request body for `ticketId` exit?**
A. Prefer path variable: `/tickets/{ticketId}/exit`.

**Q. When do you create a custom exception?**
A. When the error has domain meaning, different HTTP status, or different handling.

**Q. What handles `@Valid @RequestBody` failures?**
A. `MethodArgumentNotValidException`, usually mapped in `@RestControllerAdvice`.

**Q. Why not return domain `Ticket` directly?**
A. Response DTOs keep the API contract separate from internal model shape.

**Q. Why add `@Bean ParkingLot`?**
A. `ParkingService` depends on `ParkingLot`; Spring can inject it only if it is a bean.
