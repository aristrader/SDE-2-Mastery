# Rate Limiter Implementation Traps and Learnings

This note records the small Java decisions that can quietly break an otherwise correct token-bucket design.
The active playground is an in-progress learning draft. It now uses a concurrent map and a per-bucket monitor;
the full reasoning and remaining concurrency boundary are in [concurrency intuition](concurrency_intuition.md).
The base algorithm remains lazy refill: calculate elapsed allowance when a request arrives rather than running a
worker for every client.

## 1. Lombok constructor: field type is not the deciding factor

`@Data` provides getters, setters, `equals`, `hashCode`, `toString`, and Lombok's
`@RequiredArgsConstructor`. Required means a field is `final` or is marked `@NonNull`; it does **not** mean
"every field," and it has nothing to do with whether a field is primitive.

```java
@Data
class Bucket {
  private double availableToken;
  private long lastRefillNanos;
}
```

That class does not get a two-argument constructor from `@Data`, because neither field is required. If the
caller must supply both initial values, use `@AllArgsConstructor` or write that constructor explicitly:

```java
new Bucket(maxTokens, System.nanoTime());
```

The same rule applies if the fields were `Double` and `Long`. Primitive fields cannot be `null`, but that
does not make Lombok generate an all-fields constructor.

## 2. `double` is primitive, but it is not always an ordinary number

A primitive `double` cannot be `null`. It can still hold IEEE-754 special values:

```java
double positiveInfinity = 1.0 / 0.0;
double negativeInfinity = -1.0 / 0.0;
double notANumber = 0.0 / 0.0;
```

Those expressions do not throw `ArithmeticException`. Integer division behaves differently:

```java
int zero = 0;
int crashes = 1 / zero; // ArithmeticException at runtime
```

That matters at a configuration boundary. This check is not a null check; it rejects values that would make
rate arithmetic nonsensical:

```java
if (maxTokens <= 0
    || !Double.isFinite(refillTokensPerSecond)
    || refillTokensPerSecond <= 0) {
  throw new IllegalArgumentException("Capacity and refill rate must be finite positive values");
}
```

`Double.isFinite(...)` rejects `NaN`, positive infinity, and negative infinity. Without it, `NaN <= 0` is
`false`, so a naïve positive-number check accepts `NaN`; later calculations become `NaN` and permit checks
silently fail. A positive infinity rate can refill a bucket immediately. Keep the finite check when a
`double` value enters the limiter from configuration, a request, or another system.

**Regression trap:** the failure branch needs `!Double.isFinite(rate)`. The active playground has the correct
negated check; losing the `!` in a later edit would reject every ordinary finite rate and let positive infinity
through.

## 3. Convert nanoseconds to seconds before doing rate arithmetic

`System.nanoTime()` is appropriate for elapsed time within one running JVM. Its raw value is not a date; use
only the difference between two calls.

```java
long elapsedNanos = now - bucket.getLastRefillNanos();
double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
```

The `.0` is essential. With an integer denominator, Java performs integer division first and discards the
fraction:

```java
double wrong = elapsedNanos / 1_000_000_000;  // 500 ms becomes 0
double right = elapsedNanos / 1_000_000_000.0; // 500 ms becomes 0.5
```

This is why IntelliJ's **Integer division in floating-point context** inspection is useful. It warns when a
truncated integer result is later used as a floating-point value. In IntelliJ, enable it under
`Settings/Preferences → Editor → Inspections → Java → Numeric issues`.

## 4. Refill before deciding, then consume

For a request at `now`, the correct single-threaded order is:

```text
elapsed = now - lastRefill
earned = elapsedSeconds × refillTokensPerSecond
available = min(maxTokens, available + earned)
lastRefill = now
if available >= 1: subtract one and allow
otherwise: reject
```

Two common mistakes change the limiter's behaviour:

- Replacing `available` with `earned` loses tokens already in the bucket; earned tokens must be **added**.
- Updating `lastRefillNanos` after a consume without first applying elapsed refill discards tokens the
  client already earned.
- Adding the full elapsed interval repeatedly without advancing `lastRefillNanos` awards the same elapsed
  time again on every loop. Advance the timestamp as part of the one refill calculation.

The current implementation creates a full bucket and lets the normal refill-and-consume path handle the
first request too. The alternative—initializing `maxTokens - 1`—is correct only when that creation call
returns immediately after accounting for the first allowed request.

## 5. Do not add a worker thread per client

A token bucket needs the correct allowance when a request arrives; it does not require a thread per client.
A thread-per-key loop creates unbounded busy work and a second writer for every bucket. It is not a useful base
design.

Lazy refill performs no idle work and calculates all elapsed allowance on the next request. If a scheduled
refill worker is later added, use one shared worker—not one infinite loop per key—and protect its updates and
`tryConsume` as the same per-client read-modify-write operation.

## Quick recall

**Q. Does `@Data` create a constructor for every primitive field?**
A. No. It supplies a required-args constructor; use `@AllArgsConstructor` when the caller must provide all
fields.

**Q. Can a primitive `double` be invalid even though it cannot be null?**
A. Yes. It can be `NaN` or infinity; validate externally supplied rates with `Double.isFinite` and a positive
value check.

**Q. Why use `1_000_000_000.0` rather than `1_000_000_000`?**
A. The decimal makes the division floating-point before the fraction is discarded.

**Q. What must stay together when concurrency is added?**
A. Refill, cap, availability check, consume, and timestamp update for one client bucket.
