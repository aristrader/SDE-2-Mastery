---
order: 20
search: false
---

# Solutions

## Solution: broken-hash-contract - The Missing HashCode

```java
class Money {
    int amount;
    String currency;

    Money(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount == money.amount && currency.equals(money.currency);
    }
}

Set<Money> set = new HashSet<>();
set.add(new Money(100, "USD"));
System.out.println(set.contains(new Money(100, "USD"))); // false
```

The second object is equal by `equals`, but it has a different identity-based `hashCode`, so `HashSet` searches the wrong bucket.

## Solution: manual-hashcode - Writing a good HashCode

```java
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + Integer.hashCode(amount);
    result = 31 * result + (currency == null ? 0 : currency.hashCode());
    return result;
}
```

With equal objects producing equal hash codes, lookup reaches the correct bucket and `contains()` returns `true`.
