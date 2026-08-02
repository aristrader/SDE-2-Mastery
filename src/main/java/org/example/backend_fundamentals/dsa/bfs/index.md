---
order: 10
---

# BFS

BFS means **Breadth-First Search**. It visits nodes level by level using a queue.

Use BFS when the problem talks about shortest path in an unweighted graph, level order traversal, minimum moves, nearest cell, or spreading step by step.

## Template

For an adjacency list `adj`, where `adj[u]` contains all neighbours of node `u`:

```cpp
vector<int> bfs(vector<vector<int>> &adj) {
    vector<int> ans;
    vector<int> visited(adj.size(), 0);
    queue<int> q;

    q.push(0);
    visited[0] = 1;

    while (!q.empty()) {
        int node = q.front();
        q.pop();
        ans.push_back(node);

        for (int i = 0; i < adj[node].size(); i++) {
            int neighbour = adj[node][i];
            if (!visited[neighbour]) {
                visited[neighbour] = 1;
                q.push(neighbour);
            }
        }
    }

    return ans;
}
```

If the platform expects a class:

```cpp
class Solution {
public:
    vector<int> bfs(vector<vector<int>> &adj) {
        vector<int> ans;
        vector<int> visited(adj.size(), 0);
        queue<int> q;

        q.push(0);
        visited[0] = 1;

        while (!q.empty()) {
            int node = q.front();
            q.pop();
            ans.push_back(node);

            for (int neighbour : adj[node]) {
                if (!visited[neighbour]) {
                    visited[neighbour] = 1;
                    q.push(neighbour);
                }
            }
        }

        return ans;
    }
};
```

## Important points

- Mark a node visited when pushing it into the queue, not when popping it. This avoids pushing the same node multiple times.
- This template starts from node `0`. If the graph can be disconnected, run BFS from every unvisited node.
- Time: `O(V + E)`
- Space: `O(V)`

## Disconnected graph

```cpp
vector<int> bfsDisconnected(vector<vector<int>> &adj) {
    vector<int> ans;
    vector<int> visited(adj.size(), 0);
    queue<int> q;

    for (int start = 0; start < adj.size(); start++) {
        if (visited[start]) continue;

        q.push(start);
        visited[start] = 1;

        while (!q.empty()) {
            int node = q.front();
            q.pop();
            ans.push_back(node);

            for (int neighbour : adj[node]) {
                if (!visited[neighbour]) {
                    visited[neighbour] = 1;
                    q.push(neighbour);
                }
            }
        }
    }

    return ans;
}
```

## Quick recall

**Q. Which data structure does BFS use?**  
A. Queue.

**Q. When do we mark visited?**  
A. When pushing into the queue.

**Q. BFS time complexity?**  
A. `O(V + E)` for adjacency list.

