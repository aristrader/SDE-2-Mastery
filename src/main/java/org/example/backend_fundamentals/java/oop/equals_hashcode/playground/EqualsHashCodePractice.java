package org.example.backend_fundamentals.java.oop.equals_hashcode.playground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class EqualsHashCodePractice {

    /**
     * Demo class implementing all three ordering contracts:
     * {@link Object#equals}/{@link Object#hashCode} for hash-based collections,
     * and {@link Comparable} for sorted collections.
     */
    public static class Person implements Comparable<Person> {
        final String name;
        final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object p) {
            if (p == this) return true;
            if (!(p instanceof Person temp)) return false;
            return temp.age == this.age && Objects.equals(temp.name, this.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        /**
         * Natural order: age ascending.
         *
         * For more fields, chain inside compareTo to avoid if/else:
         *   Comparator.comparingInt((Person p) -> p.age)
         *       .thenComparing(p -> p.name)
         *       .compare(this, other)
         *
         * WARNING: compareTo must be consistent with equals. If compareTo uses only age,
         * TreeSet treats two people with the same age as duplicates — even if names differ.
         */
        @Override
        public int compareTo(Person other) {
            return Integer.compare(this.age, other.age);
        }
    }

    public static void main(String[] args) {
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Alice", 30);
        Person p3 = new Person("Bob", 25);

        // equals — five contract rules
        System.out.println(p1.equals(p1));       // true  — reflexive
        System.out.println(p1.equals(p2));       // true  — symmetric
        System.out.println(p2.equals(p1));       // true  — symmetric
        System.out.println(p1.equals(p3));       // false — different values
        System.out.println(p1.equals(null));     // false — null-safe
        System.out.println(p1.equals("Alice"));  // false — type check

        // hashCode — put with p1, look up with p2
        HashMap<Person, String> map = new HashMap<>();
        map.put(p1, "engineer");
        System.out.println(map.get(p2));         // engineer — same bucket, equals confirms match

        HashSet<Person> set = new HashSet<>();
        set.add(p1);
        System.out.println(set.contains(p2));    // true

        // Comparable — Collections.sort uses compareTo (age ascending)
        List<Person> people = new ArrayList<>();
        people.add(new Person("Charlie", 40));
        people.add(new Person("Zlice", 30));
        people.add(new Person("Alice", 30));
        people.add(new Person("Blice", 30));
        people.add(new Person("Bob", 25));

        System.out.println("--- sort by age (Comparable)");
        Collections.sort(people);
        people.forEach(p -> System.out.println(p.name + " " + p.age));

        // TreeSet uses compareTo for deduplication — age-only compareTo drops same-age duplicates
        System.out.println("--- TreeSet (compareTo inconsistent with equals — drops same-age entries)");
        TreeSet<Person> sorted = new TreeSet<>(people);
        sorted.forEach(p -> System.out.println(p.name + " " + p.age));

        // Comparator — sort by name only
        System.out.println("--- sort by name (Comparator.comparing)");
        people.sort(Comparator.comparing(p -> p.name));
        people.forEach(p -> System.out.println(p.name + " " + p.age));

        // Comparator — sort by age descending
        System.out.println("--- sort by age descending (.reversed)");
        people.sort(Comparator.comparingInt((Person p) -> p.age).reversed());
        people.forEach(p -> System.out.println(p.name + " " + p.age));

        // Comparator — age ascending, name as tiebreaker
        System.out.println("--- sort by age then name (.thenComparing)");
        people.sort(Comparator.comparingInt((Person p) -> p.age).thenComparing(p -> p.name));
        people.forEach(p -> System.out.println(p.name + " " + p.age));
    }
}
