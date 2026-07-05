package org.example.backend_fundamentals.java.collections.lists.playground.performance;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Benchmarks random-access reads on {@link ArrayList} (O(1)) vs {@link LinkedList} (O(n)).
 *
 * <p>{@link ArrayList} is array-backed — {@code getValue(i)} indexes directly. {@link LinkedList}
 * needs to walk pointers from one end; with size 10,000 and uniformly random indices, each
 * {@code getValue} averages ~5,000 hops. ArrayList wins decisively.</p>
 *
 * <p>Methodology: 100 outer rounds of 10,000 random reads each; count rounds won by each.
 * Not a principled microbenchmark (no JIT warmup, GC variability) — sufficient as a Big-O lesson.
 * For serious work reach for JMH.</p>
 */
public class ArrayListVsLinkedListRandomAccessRun {

    public static void main(String[] args) {
        int size = 10_000;
        int innerLoops = 10_000;
        int outerLoops = 100;

        List<String> arrayList = new ArrayList<>();
        List<String> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add("element" + i);
            linkedList.add("element" + i);
        }

        Random random = new Random();
        int arrayListWins = 0;
        int linkedListWins = 0;

        for (int i = 0; i < outerLoops; i++) {
            long start = System.nanoTime();
            for (int j = 0; j < innerLoops; j++) {
                arrayList.get(random.nextInt(size));
            }
            long arrayListTime = System.nanoTime() - start;

            start = System.nanoTime();
            for (int j = 0; j < innerLoops; j++) {
                linkedList.get(random.nextInt(size));
            }
            long linkedListTime = System.nanoTime() - start;

            if (arrayListTime < linkedListTime) {
                arrayListWins++;
            } else {
                linkedListWins++;
            }
        }

        System.out.println("ArrayList wins:  " + arrayListWins + " / " + outerLoops);
        System.out.println("LinkedList wins: " + linkedListWins + " / " + outerLoops);
    }
}
