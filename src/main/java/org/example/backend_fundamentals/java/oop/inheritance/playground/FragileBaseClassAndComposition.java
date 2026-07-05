package org.example.backend_fundamentals.java.oop.inheritance.playground;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The fragile base class problem and the composition fix.
 * <p>
 * This is Bloch's canonical {@code InstrumentedHashSet} example (Effective Java
 * Item 18). The subclass {@link InstrumentedHashSetWrong} overrides {@code add}
 * and {@code addAll} to count additions, but {@code HashSet.addAll} internally
 * calls {@code this.add(e)} for each element — and because that call is virtual,
 * it dispatches back into the child's already-incremented {@code add}, counting
 * every element twice.
 * <p>
 * {@link InstrumentedSetRight} wraps a {@link Set} instead of extending one.
 * Calls go through the {@code delegate} field, so {@code delegate.addAll}
 * calls {@code delegate.add} (on the wrapped set itself), never back into our
 * class. The counter is exact.
 * <p>
 * The dependency in the wrong version is on an undocumented implementation
 * detail of {@code HashSet}: that {@code addAll} uses {@code add} internally.
 * Any future JDK that changes this would silently flip the counter behaviour.
 * That is exactly the "fragile" in fragile base class.
 */
public class FragileBaseClassAndComposition {

  static class InstrumentedHashSetWrong<E> extends HashSet<E> {
    private int addCount = 0;

    @Override
    public boolean add(E e) {
      addCount++;
      return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      addCount += c.size();
      // HashSet.addAll calls this.add(e) for each element — virtual dispatch lands
      // back in our overridden add() above, which already counted in this method.
      return super.addAll(c);
    }

    public int getAddCount() { return addCount; }
  }

  static class InstrumentedSetRight<E> {
    private final Set<E> delegate = new HashSet<>();
    private int addCount = 0;

    public boolean add(E e) {
      addCount++;
      return delegate.add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
      addCount += c.size();
      // delegate.addAll calls delegate.add — those are calls on the wrapped HashSet
      // itself. There is no virtual path back into THIS class, so no double-count.
      return delegate.addAll(c);
    }

    public int getAddCount() { return addCount; }
  }

  public static void main(String[] args) {
    InstrumentedHashSetWrong<String> wrong = new InstrumentedHashSetWrong<>();
    wrong.addAll(List.of("a", "b", "c"));
    System.out.println("Wrong (extends HashSet):  addCount = " + wrong.getAddCount()
        + "  (expected 3 — double-counted because addAll → add via virtual dispatch)");

    InstrumentedSetRight<String> right = new InstrumentedSetRight<>();
    right.addAll(List.of("a", "b", "c"));
    System.out.println("Right (composes a Set):   addCount = " + right.getAddCount()
        + "  (correct — delegate.addAll never dispatches back into us)");
  }
}
