package org.example.backend_fundamentals.java.collections.lists.basics;

import java.util.LinkedList;
import java.util.List;

/**
 * Basic {@link LinkedList} usage — declare, add, print.
 *
 * <p>{@link LinkedList} is a doubly-linked list: O(1) insert/delete at both ends but O(n)
 * random access. Rarely the right choice in modern Java — see
 * {@code performance/ArrayListVsLinkedListRandomAccessRun} for the reason.</p>
 */
public class LinkedListBasicsRun {

    public static void main(String[] args) {
        List<String> list = new LinkedList<>();
        list.add("Hi");
        list.add("2nd element");
        System.out.println(list);
    }
}
