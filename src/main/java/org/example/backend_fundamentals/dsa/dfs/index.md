---
order: 20
---

# DFS

DFS means **Depth-First Search**. It goes as deep as possible before coming back.

Use DFS when the problem talks about connected components, cycle detection, path existence, flood fill, islands, backtracking-style traversal, or exploring an entire graph/tree branch.

## Template

For an adjacency list `adj`, where `adj[u]` contains all neighbours of node `u`:

```cpp
void dfs(vector<vector<int>>& adj, vector<int>& ans, vector<int>& vis, int num) {
    ans.push_back(num);
    vis[num] = 1;

    for (int i = 0; i < adj[num].size(); i++) {
        if (!vis[adj[num][i]]) {
            dfs(adj, ans, vis, adj[num][i]);
        }
    }
}

vector<int> dfs(vector<vector<int>>& adj) {
    vector<int> ans;
    vector<int> vis(adj.size(), 0);

    dfs(adj, ans, vis, 0);
    return ans;
}
```

If the platform expects a class:

```cpp
class Solution {
public:
    void dfsHelper(vector<vector<int>>& adj, vector<int>& ans, vector<int>& vis, int node) {
        ans.push_back(node);
        vis[node] = 1;

        for (int neighbour : adj[node]) {
            if (!vis[neighbour]) {
                dfsHelper(adj, ans, vis, neighbour);
            }
        }
    }

    vector<int> dfs(vector<vector<int>>& adj) {
        vector<int> ans;
        vector<int> vis(adj.size(), 0);

        dfsHelper(adj, ans, vis, 0);
        return ans;
    }
};
```

## Important points

- Mark the node visited before recursively calling neighbours.
- This template starts from node `0`. If the graph can be disconnected, call DFS from every unvisited node.
- Recursive DFS uses the call stack.
- Time: `O(V + E)`
- Space: `O(V)` for `vis` plus recursion stack.

## Disconnected graph

```cpp
vector<int> dfsDisconnected(vector<vector<int>>& adj) {
    vector<int> ans;
    vector<int> vis(adj.size(), 0);

    for (int start = 0; start < adj.size(); start++) {
        if (!vis[start]) {
            dfs(adj, ans, vis, start);
        }
    }

    return ans;
}
```

## Quick recall

**Q. Which mechanism does recursive DFS use?**  
A. Function call stack.

**Q. When do we mark visited?**  
A. Before exploring neighbours.

**Q. DFS time complexity?**  
A. `O(V + E)` for adjacency list.
