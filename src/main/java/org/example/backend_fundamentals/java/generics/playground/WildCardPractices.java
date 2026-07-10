package org.example.backend_fundamentals.java.generics.playground;

import java.util.ArrayList;
import java.util.List;

public class WildCardPractices {

    // Exercise 1A — bounded type parameter: T is named, can be reused
    public static <T> void printAllBound(List<T> list) {
        for (T t : list) {
            System.out.println(t);
        }
    }

    // Exercise 1B — wildcard: type is anonymous, just need to iterate
    public static void printAllWild(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

    // Exercise 2 — upper bounded wildcard: read from list as Number, cannot add
    public static double sumList(List<? extends Number> list) {
        return list.stream().mapToDouble(Number::doubleValue).sum();
    }

    // Exercise 3 — lower bounded wildcard: can add Integer (or subtype), reading gives Object
    public static void addNumbers(List<? super Integer> list) {
        for (int i = 1; i <= 5; i++) {
            list.add(i);
        }
        // Integer x = list.get(0); // COMPILE ERROR — list could be List<Number> or List<Object>;
        //                           // compiler can only guarantee Object, not Integer
    }

    // Exercise 3b — upper bounded: can read as Number, cannot add anything
    public static void printNumbers(List<? extends Number> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).doubleValue());
        }
        // list.add(5); // COMPILE ERROR — list could be List<Double> at runtime;
        //              // adding Integer into it would corrupt the list, so compiler refuses all adds
    }

    // Exercise 5 — fill: adds value into any list that can hold Integer
    public static void fill(List<? super Integer> list, int value, int count) {
        for (int i = 0; i < count; i++) {
            list.add(value);
        }
    }

    public static void main(String[] args) {
        List<Integer> ints = List.of(1, 2, 30);
        List<String> strings = List.of("a", "b");
        List<Double> doubles = List.of(1.5, 2.5, 3.0);

        // Exercise 1 — both versions work identically at the call site
        printAllBound(ints);
        printAllBound(strings);
        System.out.println("---");
        printAllWild(ints);
        printAllWild(strings);

        // Invariance demo — List<Integer> is not List<Number>, but IS List<? extends Number>
        // List<Number> wrong = ints;              // COMPILE ERROR — invariance
        List<? extends Number> numbers = ints;     // works — wildcard restores the is-a relationship
        printAllBound(numbers);
        printAllWild(numbers);

        System.out.println("---");

        // Exercise 2 — sumList accepts List<Integer> and List<Double>
        System.out.println("sum ints:    " + sumList(ints));     // 33.0
        System.out.println("sum doubles: " + sumList(doubles));  // 7.0

        System.out.println("---");

        // Exercise 3 — addNumbers works with List<Integer> and List<Number>
        List<Integer> intDest = new ArrayList<>();
        addNumbers(intDest);
        System.out.println("intDest:    " + intDest);   // [1, 2, 3, 4, 5]

        List<Number> numDest = new ArrayList<>();
        addNumbers(numDest);
        System.out.println("numDest:    " + numDest);   // [1, 2, 3, 4, 5]

        // addNumbers(strings); // COMPILE ERROR — String is not a supertype of Integer

        System.out.println("---");

        // Exercise 3b — printNumbers accepts List<Integer> and List<Double>
        printNumbers(ints);
        printNumbers(doubles);

        System.out.println("---");

        // Exercise 5 — fill works with List<Number> and List<Object>
        List<Number> fillDest = new ArrayList<>();
        fill(fillDest, 7, 3);
        System.out.println("fillDest:   " + fillDest);  // [7, 7, 7]
    }
}
