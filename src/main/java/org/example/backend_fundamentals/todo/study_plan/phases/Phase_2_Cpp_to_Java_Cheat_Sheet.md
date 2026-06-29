# C++ → Java Transition Cheat Sheet (SDE-2 Backend Interviews)

> Goal: Bridge existing C++ DSA knowledge to Java for coding interviews. Focus only on high-frequency interview APIs, syntax, and patterns.

## 1. Program Skeleton
```cpp
#include <bits/stdc++.h>
using namespace std;

int main() {

}
```
```java
import java.util.*;

public class Main {

    public static void main(String[] args) {

    }
}

LeetCode:

class Solution {

}
```
## 2. Primitive Types
| C++ | Java |
|---|---|
| int | int |
| long long | long |
| float | float |
| double | double |
| bool | boolean |
| char | char |
| string | String |

### Useful String APIs

s.length();

s.charAt(i);

s.substring(l, r);

s.contains("ab");

s.split(",");

s.equals(other);

Never compare Strings using ==.

## 3. Arrays
```cpp
int arr[10];

vector<int> nums;
```
```java
int[] arr = new int[10];

int[] nums = {1,2,3};
```

### Length

arr.length

### Loop

`for(int i=0;i<arr.length;i++){`

}
## 4. Vector ↔ ArrayList
```cpp
vector<int> v;

v.push_back(x);

v.pop_back();

v.back();

v.size();
```
```java
ArrayList<Integer> list = new ArrayList<>();

list.add(x);

list.remove(list.size()-1);

list.get(list.size()-1);

list.size();
```

### Remember

Collections store wrapper classes.

int
↓

Integer
## 5. Pair & Common Interview Patterns
```cpp
pair<int,int>

Java has no built-in Pair.

Most interview solutions use

int[] p = {x,y};

or

class Pair{

    int first;
    int second;

}
```

### Very common:

`vector<pair<int,int>>`

↓

`List<int[]> edges = new ArrayList<>();`

edges.add(new int[]{u, w});

int node = edges.get(i)[0];
int weight = edges.get(i)[1];
## 6. Stack

### Prefer

`Deque<Integer> st = new ArrayDeque<>();`

st.push(x);

st.pop();

st.peek();

st.isEmpty();

### Avoid legacy

Stack
## 7. Queue
`Queue<Integer> q = new LinkedList<>();`

q.offer(x);

q.poll();

q.peek();

q.isEmpty();

### Level-order BFS pattern

int size = q.size();

`for(int i=0;i<size;i++){`

    int node = q.poll();

}
## 8. Deque
`Deque<Integer> dq = new ArrayDeque<>();`

dq.addFirst(x);

dq.addLast(x);

dq.pollFirst();

dq.pollLast();

dq.peekFirst();

dq.peekLast();
## 9. Priority Queue (Heap)
### Min Heap
`PriorityQueue<Integer> pq =`
    `new PriorityQueue<>();`
### Max Heap
`PriorityQueue<Integer> pq =`
    `new PriorityQueue<>(Collections.reverseOrder());`
### Custom Comparator

### Preferred

`PriorityQueue<int[]> pq =`
    `new PriorityQueue<>(`
        (a,b) -> Integer.compare(a[1], b[1])
    );

### Avoid

a[1]-b[1]

because of integer overflow.

## 10. HashMap
`HashMap<Integer,Integer> map =`
    `new HashMap<>();`

### Operations

map.put(k,v);

map.get(k);

map.getOrDefault(k,0);

map.containsKey(k);

map.remove(k);

map.size();

### Iteration

`for(Map.Entry<Integer,Integer> e`
        : map.entrySet()){

    int key = e.getKey();

    int val = e.getValue();
}
## 11. HashSet
`HashSet<Integer> set =`
    `new HashSet<>();`

set.add(x);

set.remove(x);

set.contains(x);

set.size();
## 12. Ordered Map / Set
| C++ | Java |
|---|---|
| map | TreeMap |
| set | TreeSet |

### Useful methods

ceilingKey()

floorKey()

higherKey()

lowerKey()

firstKey()

lastKey()
## 13. Graph Representation

### Adjacency List

`vector<vector<int>>`

↓

`List<List<Integer>> graph =`
    `new ArrayList<>();`

`for(int i=0;i<n;i++)`
    `graph.add(new ArrayList<>());`

### Edge

graph.get(u).add(v);

### Weighted graph

`List<List<int[]>> graph;`

Each edge

{neighbor, weight}

### Dynamic graph using HashMap

`Map<Integer,List<Integer>> graph =`
    `new HashMap<>();`

graph.computeIfAbsent(
    u,
    `k -> new ArrayList<>()`
).add(v);
## 14. 2D Arrays vs `List<List<Integer>>`

### Fixed grid

`vector<vector<int>>`

↓

int[][] grid;

### Dynamic adjacency list

`List<List<Integer>> graph;`

### Use

int[][] for matrices
`List<List<Integer>> for graphs`
## 15. Tree Node
class TreeNode{

    int val;

    TreeNode left;

    TreeNode right;

    TreeNode(int val){
        this.val = val;
    }
}
## 16. Linked List Node
class ListNode{

    int val;

    ListNode next;

    ListNode(int x){

        val = x;
    }
}
## 17. BFS
`Queue<Integer> q =`
    `new LinkedList<>();`

boolean[] vis =
    new boolean[n];

q.offer(src);

while(!q.isEmpty()){

    int node = q.poll();

    for(int nei : graph.get(node)){

        if(!vis[nei]){

            vis[nei]=true;

            q.offer(nei);
        }
    }
}
## 18. DFS
void dfs(int node){

    vis[node]=true;

    for(int nei : graph.get(node)){

        if(!vis[nei])

            dfs(nei);
    }
}
## 19. Sorting

### Arrays

Arrays.sort(arr);

### ArrayList

Collections.sort(list);

Reverse

Collections.sort(
    list,
    Collections.reverseOrder()
);

Objects

Arrays.sort(
    arr,
    (a,b)->Integer.compare(a.age,b.age)
);

Multiple fields

(a,b)->{

    if(a[0]!=b[0])

        return Integer.compare(a[0],b[0]);

    return Integer.compare(a[1],b[1]);
}
## 20. Binary Search

Manual implementation is preferred during interviews.

### Library

Arrays.binarySearch(arr,target);
## 21. Strings & Characters

### Characters

c-'0'

c-'a'

Character.isDigit(c)

Character.isLetter(c)

Character.toUpperCase(c)

Character.toLowerCase(c)

### Mutable strings

StringBuilder sb =
    new StringBuilder();

sb.append(x);

sb.reverse();

sb.toString();

### Avoid

ans += c;

inside loops.

## 22. Useful Utility APIs

### Arrays

Arrays.fill(arr,-1);

Arrays.copyOf(arr,n);

Arrays.toString(arr);

Arrays.deepToString(grid);

### Collections

Collections.max(list);

Collections.min(list);

Collections.reverse(list);

Collections.swap(list,i,j);

### Math

Math.max(a,b);

Math.min(a,b);

Math.abs(x);

Math.pow(a,b);

Math.sqrt(x);

Math.ceil(x);

Math.floor(x);

### Conversions

Integer.parseInt(s);

Long.parseLong(s);

String.valueOf(x);

Integer.toString(x);
## 23. Arrays.asList() vs List.of()
### Arrays.asList()

Returns a fixed-size list.

### List.of()

Returns an immutable list.

### Need a modifiable list?

`new ArrayList<>(Arrays.asList(...))`
## 24. Enhanced For Loop

### Vector

for(auto x : v)

↓

for(int x : list)

### Map

`for(Map.Entry<Integer,Integer> e`
        : map.entrySet())
## 25. Common Pitfalls
### Collections use wrapper classes
Integer

Long

Character
### String comparison

❌

==

✅

equals()
### Arrays
length
### ArrayList
size()
### Queue
offer()

poll()

peek()
### HashMap

### Prefer

map.getOrDefault(key,0);

instead of

map.get(key);
### Integer Overflow

### Use

long

when needed.

### Comparator

### Prefer

Integer.compare()

Long.compare()

instead of subtraction.

### Array Initialization
int[] arr =
    new int[n];

Defaults to

0
### NullPointerException

### Remember

map.get(key)

may return

null
## 26. C++ STL ↔ Java Collections Mapping
| C++ STL | Java |
|---|---|
| vector | ArrayList |
| unordered_map | HashMap |
| map | TreeMap |
| unordered_set | HashSet |
| set | TreeSet |
| queue | Queue (LinkedList) |
| deque | ArrayDeque |
| stack | ArrayDeque |
| priority_queue | PriorityQueue |
| pair | int[] / custom Pair |
| string | String |
| string builder | StringBuilder |
| sort | Arrays.sort / Collections.sort |
| lower_bound | Manual Binary Search |
| upper_bound | Manual Binary Search |
## 27. The 90% Interview Toolkit

These are the only classes you'll use for the vast majority of SDE-2 coding interviews:

import java.util.*;

int[]
int[][]

### ArrayList
### HashMap
HashSet
TreeMap
TreeSet

ArrayDeque
LinkedList
PriorityQueue

### Arrays
### Collections
StringBuilder
### Math

If you're already comfortable solving DSA problems in C++, mastering the APIs and patterns above is enough to write clean, idiomatic Java solutions for almost every LeetCode-style interview question.
