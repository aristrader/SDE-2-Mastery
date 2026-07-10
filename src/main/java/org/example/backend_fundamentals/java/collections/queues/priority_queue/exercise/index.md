---
order: 10
search: false
---

# Practice

## Exercise: basic-min-heap - Basic Min-Heap

Create a `PriorityQueue<Integer>`. Insert `10, 4, 7, 1, 8, 3`.

Print the queue directly, print `peek()`, then print every element by repeatedly calling `poll()`. Observe the difference between internal heap order and priority order.

## Exercise: empty-priority-queue - Empty PriorityQueue

Create an empty priority queue. Call `peek()` and `poll()`, then call `element()` and `remove()`.

Check which methods return `null` and which methods throw.

## Exercise: max-heap - Max-Heap

Create a max-heap using `Comparator.reverseOrder()`. Insert `10, 4, 7, 1, 8, 3` and poll all elements.

Expected removal order: `10, 8, 7, 4, 3, 1`.

## Exercise: string-natural-order - Strings

Create a `PriorityQueue<String>`. Insert `banana`, `apple`, `orange`, `grape`, then poll all elements.

Observe natural lexicographical ordering.

## Exercise: string-length-priority - String Length

Create a priority queue that removes the shortest string first. Insert `elephant`, `cat`, `tiger`, `ox`, `giraffe`.

Use `Comparator.comparingInt(String::length)` and add alphabetical ordering as a tie-breaker.

## Exercise: student-by-cgpa - Student by CGPA

Create a `Student` class with `id`, `name`, and `cgpa`.

Create a priority queue where the highest CGPA is removed first. Insert at least six students, then poll and print all students.

## Exercise: comparator-tie-breaking - Comparator Tie-Breaking

Using the same `Student` class, define priority as:

1. Higher CGPA first
2. Name ascending
3. ID ascending

Use `comparing`, `reversed`, `thenComparing`, and `thenComparingInt`. Test equal CGPAs and duplicate names.

## Exercise: duplicate-elements - Duplicate Elements

Insert `5, 5, 5, 2, 2, 8` and poll every element.

Verify duplicates are retained. Explain why this differs from `TreeSet`.

## Exercise: iteration-trap - Iteration Trap

Insert `20, 5, 15, 1, 10, 8`.

Print elements using a `for` loop, then print elements using repeated `poll()`. Explain why the outputs may differ.

## Exercise: kth-largest-element - Kth Largest Element

Given `[3, 2, 1, 5, 6, 4]` and `k = 2`, find the kth largest element using a min-heap of size `k`.

Insert each number. If heap size exceeds `k`, remove the smallest. At the end, the root is the kth largest.

Expected answer: `5`. Complexity target: O(n log k).

## Exercise: k-smallest-elements - K Smallest Elements

Given `[9, 4, 7, 1, 3, 6, 2]` and `k = 3`, return the `k` smallest elements using a max-heap of size `k`.

Final result should be sorted: `1, 2, 3`.

## Exercise: top-k-frequent-numbers - Top K Frequent Numbers

Given `[1, 1, 1, 2, 2, 3]` and `k = 2`, build a frequency map, use a min-heap of size `k`, and return the two most frequent values.

Expected result: `1, 2`.

## Exercise: merge-sorted-arrays - Merge Sorted Arrays

Given `[1, 4, 7]`, `[2, 5, 8]`, and `[3, 6, 9]`, merge them into `[1, 2, 3, 4, 5, 6, 7, 8, 9]`.

Use a priority queue containing entries with value, array index, and element index. Do not insert every element at once; heap size should stay at most equal to the number of arrays.

## Exercise: task-scheduler-simulation - Task Scheduler Simulation

Create a `Task` class with `id`, `name`, `priority`, and `createdAt`.

Ordering:

1. Higher priority first
2. Earlier creation time first
3. Smaller ID first

Insert several tasks and process them by repeatedly polling.

## Exercise: mutation-trap - Mutation Trap

Create a mutable `Task` with a priority field. Insert several tasks, change the priority of one task already inside the queue, call `peek()`, and poll all elements.

Then fix the problem by removing the task, updating it, and reinserting it.

## Exercise: arbitrary-removal - Arbitrary Removal

Insert several integers and call `pq.remove(value)`.

Answer: what is its time complexity, and why is it slower than `poll()`?

## Exercise: build-from-collection - Build from a Collection

Create a list of integers and construct the priority queue directly with `new PriorityQueue<>(list)`.

Poll all values. Compare this with inserting every element individually. Know the theoretical complexities: collection constructor O(n), repeated insertion O(n log n).

## Exercise: two-heaps-median - Two Heaps

Maintain a stream of integers using a max-heap for the lower half and a min-heap for the upper half.

After each insertion in `5, 15, 1, 3`, print the median. Expected medians: `5`, `10`, `5`, `4`.
