package org.example.backend_fundamentals.java.generics.playground;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PecsPractice {

    // src produces values (read from) → ? extends T
    // dst consumes values (written into) → ? super T
    public static <T> void copy(List<? super T> dst, List<? extends T> src) {
        for (T item : src) {
            dst.add(item);
        }
    }

    // list consumes values → ? super Integer
    public static void addDefaults(List<? super Integer> list, int value, int count) {
        for (int i = 0; i < count; i++) {
            list.add(value);
        }
    }

    // list produces values (read only) → ? extends T; T bound for comparison
    public static <T extends Comparable<T>> T findMax(List<? extends T> list) {
        if (list.isEmpty()) return null;
        return list.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    public static void main(String[] args) {
        // copy: List<Integer> → List<Number>
        List<Integer> src = List.of(1, 2, 3);
        List<Number> dst = new ArrayList<>();
        copy(dst, src);
        System.out.println("copy:        " + dst);      // [1, 2, 3]

        // addDefaults: List<Number> is a consumer of Integer
        List<Number> nums = new ArrayList<>();
        addDefaults(nums, 0, 4);
        System.out.println("addDefaults: " + nums);     // [0, 0, 0, 0]

        // findMax: reads from List<Integer> and List<Double>
        System.out.println("max ints:    " + findMax(src));                    // 3
        System.out.println("max doubles: " + findMax(List.of(1.5, 9.0, 3.0))); // 9.0
    }
}
