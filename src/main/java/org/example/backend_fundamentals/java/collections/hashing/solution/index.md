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
    // Missing hashCode!
}

// In main:
HashSet<Money> set = new HashSet<>();
set.add(new Money(100, "USD"));
System.out.println(set.contains(new Money(100, "USD"))); // Prints FALSE
```
The missing `hashCode` means `Money` inherits the default identity-based hash from `Object`. The two instances have different identity hashes, so they land in different buckets. `HashSet` checks the wrong bucket, finds nothing, and returns `false`.

## Solution: manual-hashcode - Writing a good HashCode

```java
    // Add to the Money class:
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Integer.hashCode(amount);
        result = 31 * result + (currency == null ? 0 : currency.hashCode());
        return result;
    }
```
With the contract restored, equal objects produce equal hash codes. They land in the same bucket, where `equals` correctly identifies them as a match, and `contains()` now returns `true`.
