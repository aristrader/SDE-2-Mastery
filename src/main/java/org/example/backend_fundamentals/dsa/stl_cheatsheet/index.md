---
order: 30
---

# C++ STL Cheat Sheet

Use this for quick syntax recall during DSA practice.

## Headers

```cpp
#include <bits/stdc++.h>
using namespace std;
```

## Vector

```cpp
vector<int> v;
v.push_back(10);
v.pop_back();

int first = v.front();
int last = v.back();
int n = v.size();
bool empty = v.empty();

sort(v.begin(), v.end());              // ascending
sort(v.rbegin(), v.rend());            // descending
```

## Stack

LIFO: last in, first out.

```cpp
stack<int> st;
st.push(10);
st.push(20);

int x = st.top();   // 20
st.pop();           // removes 20, returns nothing

bool empty = st.empty();
int n = st.size();
```

## Queue

FIFO: first in, first out.

```cpp
queue<int> q;
q.push(10);
q.push(20);

int x = q.front();  // 10
int y = q.back();   // 20
q.pop();            // removes 10, returns nothing

bool empty = q.empty();
int n = q.size();
```

## Priority queue

By default, C++ priority queue is a max heap.

```cpp
priority_queue<int> pq;
pq.push(10);
pq.push(30);
pq.push(20);

int largest = pq.top();  // 30
pq.pop();                // removes 30
```

Min heap syntax:

```cpp
priority_queue<int, vector<int>, greater<int>> minHeap;
minHeap.push(10);
minHeap.push(30);
minHeap.push(20);

int smallest = minHeap.top();  // 10
```

Priority queue of pairs:

```cpp
priority_queue<pair<int, int>> maxHeap; // sorts by first, then second
maxHeap.push({5, 100});

priority_queue<pair<int, int>, vector<pair<int, int>>, greater<pair<int, int>>> minHeap;
minHeap.push({5, 100});
```

## Deque

Useful when you need push/pop from both ends.

```cpp
deque<int> dq;
dq.push_back(10);
dq.push_front(5);

int a = dq.front();
int b = dq.back();

dq.pop_front();
dq.pop_back();
```

## Set

Stores unique values in sorted order.

```cpp
set<int> s;
s.insert(10);
s.insert(5);

bool exists = s.find(10) != s.end();
s.erase(10);

int smallest = *s.begin();
int largest = *s.rbegin();
```

## Unordered set

Average `O(1)` lookup. No sorting.

```cpp
unordered_set<int> s;
s.insert(10);

if (s.count(10)) {
    // exists
}
```

## Map

Stores key-value pairs sorted by key.

```cpp
map<string, int> mp;
mp["alice"] = 10;
mp["bob"]++;

bool exists = mp.find("alice") != mp.end();
mp.erase("alice");

for (auto &[key, value] : mp) {
    cout << key << " " << value << "\n";
}
```

## Unordered map

Average `O(1)` lookup. No key sorting.

```cpp
unordered_map<string, int> freq;
freq["apple"]++;

if (freq.count("apple")) {
    cout << freq["apple"];
}
```

## Common traps

| Trap | Correct point |
| --- | --- |
| `pop()` returns value | No. Read with `top()` or `front()` first, then call `pop()` |
| max heap syntax | `priority_queue<int> pq;` |
| min heap syntax | `priority_queue<int, vector<int>, greater<int>> pq;` |
| `map[key]` only reads | No. It inserts default value if key is missing |
| `set.find(x)` returns bool | No. Compare with `s.end()` |

## Quick recall

**Q. Stack access function?**  
A. `top()`.

**Q. Queue access functions?**  
A. `front()` and `back()`.

**Q. Priority queue access function?**  
A. `top()`.

**Q. Does `pop()` return the removed value?**  
A. No.
