---
order: 20
---

# CompletableFuture Composition

Composition is the main reason to use `CompletableFuture`: start independent work early, chain dependent work clearly, and block only at the boundary.

Before choosing a method, classify the next step:

```text
Does the next step need the previous result?
Does the next step return a plain value or another CompletableFuture?
Are two calls independent and can run in parallel?
Do I have a fixed pair or a dynamic list?
```

## `thenApply`

Use `thenApply()` when the next step is a normal in-memory transformation.

```java
CompletableFuture<UserDto> dtoFuture =
        fetchUserAsync(userId)
                .thenApply(this::toDto);
```

Shape:

```text
CompletableFuture<User> -> UserDto
= CompletableFuture<UserDto>
```

`thenApply()` is like `Stream.map()`.

## `thenCompose`

Use `thenCompose()` when the next step depends on the previous result and itself returns a `CompletableFuture`.

```java
CompletableFuture<List<Order>> ordersFuture =
        fetchUserAsync(userId)
                .thenCompose(user -> fetchOrdersAsync(user.id()));
```

Shape:

```text
CompletableFuture<User> -> CompletableFuture<List<Order>>
= CompletableFuture<List<Order>>
```

`thenCompose()` is like `Stream.flatMap()`.

Wrong:

```java
CompletableFuture<CompletableFuture<List<Order>>> nested =
        fetchUserAsync(userId)
                .thenApply(user -> fetchOrdersAsync(user.id()));
```

That creates a nested future. The outer future only means "we started the order fetch", not "orders are available".

## `thenCompose` vs `thenCombine`

Use `thenCompose()` for dependent calls:

```text
fetch user -> use user id to fetch orders
```

Use `thenCombine()` for independent calls whose results must be merged:

```text
fetch user         \
                   -> build profile
fetch preferences /
```

```java
CompletableFuture<User> userFuture = fetchUserAsync(userId);
CompletableFuture<Preferences> prefsFuture = fetchPreferencesAsync(userId);

CompletableFuture<Profile> profileFuture =
        userFuture.thenCombine(prefsFuture, Profile::new);
```

The two futures are created before combining, so they can run in parallel.

## Accidental serialization

This is slower than it looks:

```java
CompletableFuture<Profile> profileFuture =
        fetchUserAsync(userId)
                .thenCompose(user ->
                        fetchPreferencesAsync(userId)
                                .thenApply(prefs -> new Profile(user, prefs)));
```

If preferences do not depend on user, this waits for user before starting preferences. Latency becomes roughly `user latency + preferences latency`.

Better:

```java
CompletableFuture<User> userFuture = fetchUserAsync(userId);
CompletableFuture<Preferences> prefsFuture = fetchPreferencesAsync(userId);

CompletableFuture<Profile> profileFuture =
        userFuture.thenCombine(prefsFuture, Profile::new);
```

Latency becomes roughly `max(user latency, preferences latency)`.

## `allOf`

Use `allOf()` for a dynamic list of independent futures.

```java
List<CompletableFuture<User>> futures = userIds.stream()
        .map(this::fetchUserAsync)
        .toList();

CompletableFuture<List<User>> usersFuture =
        CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
```

`allOf()` returns `CompletableFuture<Void>` because Java cannot know the result type of an arbitrary list of futures. After `allOf()` completes, each individual future is complete, so `join()` in that final `thenApply()` does not block one-by-one in the normal success case.

The result order above follows the original `futures` list order, not completion order.

## `anyOf`

`anyOf()` completes when the first future completes, successfully or exceptionally.

```java
CompletableFuture<Object> first =
        CompletableFuture.anyOf(primaryFetch(), backupFetch());
```

It returns `CompletableFuture<Object>` because inputs can have different types. Use it for races, fallback sources, or first-response-wins patterns. Be careful: losing tasks are not automatically cancelled.

## Boundary rule

Do not join too early.

Bad:

```java
User user = fetchUserAsync(id).join();
Orders orders = fetchOrdersAsync(id).join();
return new Page(user, orders);
```

This serializes the calls.

Better:

```java
CompletableFuture<User> user = fetchUserAsync(id);
CompletableFuture<Orders> orders = fetchOrdersAsync(id);

return user.thenCombine(orders, Page::new);
```

Block only where the synchronous framework boundary requires a final result.

## Quick recall

**Q. When do you use `thenApply()`?**
A. The next function returns a plain value.

**Q. When do you use `thenCompose()`?**
A. The next function returns another `CompletableFuture` and depends on the previous result.

**Q. When do you use `thenCombine()`?**
A. Two futures are independent and their results need merging.

**Q. What does `allOf()` return?**
A. `CompletableFuture<Void>`; read values from the original futures after it completes.

**Q. What is accidental serialization?**
A. Starting independent async work only after another async step finishes, or joining too early.

**Q. Does `anyOf()` cancel the slower tasks?**
A. No. It completes with the first result/failure; cancellation of losers must be handled separately.

