# Documentation Site Navigation & Execution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement context-aware sidebars and an embedded IDE component for local Java execution.

**Architecture:** We will use `vitepress-sidebar` array configuration for multi-sidebars. We will build a `<CodePlayground>` Vue component that leverages `import.meta.glob` to automatically discover and display raw `.java` files in the current directory, communicating with the existing `/api/run-java` Vite endpoint.

**Tech Stack:** Vue 3, VitePress, Node.js, Java.

## Global Constraints
- No external APIs or online execution environments.
- Sidebars must map exactly to `/todo/`, `/system_design/`, and `/design_patterns/`.

---

### Task 1: Reconfigure vitepress-sidebar for Contextual Sidebars

**Files:**
- Modify: `/Users/swapnilagarwal/IdeaProjects/TestingTesting/docs/.vitepress/config.mjs`

**Interfaces:**
- Consumes: Existing VitePress config.
- Produces: Contextual sidebars mapping `documentRootPath` + `scanStartPath` to specific routes.

- [ ] **Step 1: Write the updated configuration**

```javascript
import { defineConfig } from 'vitepress'
import { generateSidebar } from 'vitepress-sidebar'
import { exec } from 'child_process'

const javaRunnerPlugin = {
  name: 'java-runner',
  configureServer(server) {
    server.middlewares.use('/api/run-java', (req, res) => {
      if (req.method === 'POST') {
        let body = ''
        req.on('data', chunk => { body += chunk.toString() })
        req.on('end', () => {
          try {
            const { mainClass } = JSON.parse(body)
            if (!mainClass) throw new Error("mainClass is required")
            
            const cmd = `mvn -q compile && mvn -q exec:java -Dexec.mainClass="${mainClass}"`
            exec(cmd, { cwd: process.cwd() }, (error, stdout, stderr) => {
              res.setHeader('Content-Type', 'application/json')
              res.end(JSON.stringify({ 
                stdout: stdout || '', 
                stderr: stderr || '', 
                error: error ? error.message : null 
              }))
            })
          } catch (e) {
            res.statusCode = 400
            res.end("Bad Request")
          }
        })
      } else {
        res.statusCode = 405
        res.end()
      }
    })
  }
}

export default defineConfig({
  title: "SDE-2 Mastery",
  description: "Study Plan & Backend Fundamentals",
  srcDir: '../src/main/java/org/example/backend_fundamentals',
  vite: { plugins: [javaRunnerPlugin] },
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Study Plan', link: '/todo/study_plan/README' },
      { text: 'Design Patterns', link: '/design_patterns/creational/CreationalPatternsRoadmap' },
      { text: 'System Design', link: '/system_design/clustering/Clustering' }
    ],
    search: { provider: 'local' },
    sidebar: generateSidebar([
      {
        documentRootPath: 'src/main/java/org/example/backend_fundamentals',
        scanStartPath: 'todo',
        resolvePath: '/todo/',
        useTitleFromFileHeading: true,
        useTitleFromFrontmatter: true,
        collapseDepth: 2,
        capitalizeFirst: true,
        sortMenusByFrontmatterOrder: true
      },
      {
        documentRootPath: 'src/main/java/org/example/backend_fundamentals',
        scanStartPath: 'design_patterns',
        resolvePath: '/design_patterns/',
        useTitleFromFileHeading: true,
        useTitleFromFrontmatter: true,
        collapseDepth: 2,
        capitalizeFirst: true,
        sortMenusByFrontmatterOrder: true
      },
      {
        documentRootPath: 'src/main/java/org/example/backend_fundamentals',
        scanStartPath: 'system_design',
        resolvePath: '/system_design/',
        useTitleFromFileHeading: true,
        useTitleFromFrontmatter: true,
        collapseDepth: 2,
        capitalizeFirst: true,
        sortMenusByFrontmatterOrder: true
      }
    ])
  }
})
```

- [ ] **Step 2: Build the site to verify**

Run: `npm run docs:build`
Expected: PASS

---

### Task 2: Implement CodePlayground Component

**Files:**
- Create: `/Users/swapnilagarwal/IdeaProjects/TestingTesting/docs/.vitepress/theme/components/CodePlayground.vue`

**Interfaces:**
- Consumes: `import.meta.glob('./*.java', { as: 'raw', eager: true })`
- Produces: A Vue component that displays files and runs code via `/api/run-java`.

- [ ] **Step 1: Write the component**

```vue
<template>
  <div class="playground-wrapper">
    <div v-if="files.length === 0" class="no-files">No Java files found in this directory.</div>
    <div v-else class="playground-container">
      <div class="sidebar">
        <div class="sidebar-header">Java Files</div>
        <div 
          v-for="file in files" 
          :key="file.name"
          @click="selectedFile = file"
          :class="['file-item', selectedFile.name === file.name ? 'active' : '']"
        >
          {{ file.name }}
        </div>
      </div>
      <div class="main-area">
        <div class="editor-header">
          <span>{{ selectedFile.name }}</span>
          <button @click="runCode" :disabled="loading" class="run-btn">
            {{ loading ? 'Running...' : 'Run Code Locally' }}
          </button>
        </div>
        <pre class="editor-content">{{ selectedFile.content }}</pre>
        <div v-if="output || error" class="console">
          <div class="console-header">Terminal Output</div>
          <pre v-if="output" class="stdout">{{ output }}</pre>
          <pre v-if="error" class="stderr">{{ error }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef } from 'vue'

const rawFiles = import.meta.glob('./*.java', { as: 'raw', eager: true })

const files = Object.keys(rawFiles).map(path => {
  const content = rawFiles[path]
  const name = path.split('/').pop()
  
  const packageMatch = content.match(/package\s+([^;]+);/)
  const packageName = packageMatch ? packageMatch[1].trim() : ''
  const className = name.replace('.java', '')
  const fqcn = packageName ? `${packageName}.${className}` : className
  
  return { name, content, fqcn }
})

const selectedFile = shallowRef(files[0] || {})
const loading = ref(false)
const output = ref('')
const error = ref('')

const runCode = async () => {
  if (!selectedFile.value.fqcn) return
  
  loading.value = true
  output.value = ''
  error.value = ''
  
  try {
    const res = await fetch('/api/run-java', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mainClass: selectedFile.value.fqcn })
    })
    const data = await res.json()
    output.value = data.stdout
    if (data.stderr) error.value += data.stderr
    if (data.error) error.value += '\n' + data.error
    if (!output.value && !error.value) output.value = "Process finished with exit code 0"
  } catch (err) {
    error.value = 'Execution failed: ' + err.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.playground-wrapper { margin: 20px 0; border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; }
.playground-container { display: flex; height: 500px; }
.sidebar { width: 250px; background: var(--vp-c-bg-alt); border-right: 1px solid var(--vp-c-divider); overflow-y: auto; }
.sidebar-header { padding: 12px; font-weight: bold; border-bottom: 1px solid var(--vp-c-divider); }
.file-item { padding: 10px 12px; cursor: pointer; font-family: monospace; font-size: 13px; }
.file-item:hover { background: var(--vp-c-bg-soft); }
.file-item.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); border-left: 3px solid var(--vp-c-brand-1); }
.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.editor-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 16px; background: var(--vp-c-bg-soft); border-bottom: 1px solid var(--vp-c-divider); font-family: monospace; }
.run-btn { background: var(--vp-c-brand-1); color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.run-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.editor-content { flex: 1; margin: 0; padding: 16px; overflow: auto; font-family: var(--vp-font-family-mono); font-size: 14px; background: #1e1e1e; color: #d4d4d4; }
.console { height: 150px; display: flex; flex-direction: column; border-top: 1px solid #333; background: #000; }
.console-header { padding: 4px 12px; background: #222; color: #888; font-size: 11px; text-transform: uppercase; }
.stdout, .stderr { margin: 0; padding: 8px 12px; overflow: auto; font-family: var(--vp-font-family-mono); font-size: 13px; }
.stdout { color: #a7f3d0; }
.stderr { color: #fca5a5; }
</style>
```

- [ ] **Step 2: Register globally**

Modify `/Users/swapnilagarwal/IdeaProjects/TestingTesting/docs/.vitepress/theme/index.js`:
```javascript
import DefaultTheme from 'vitepress/theme'
import CodePlayground from './components/CodePlayground.vue'

export default {
  ...DefaultTheme,
  enhanceApp({ app }) {
    app.component('CodePlayground', CodePlayground)
  }
}
```

- [ ] **Step 3: Test integration**

Replace `<RunCode ... />` in `Singleton.md` with `<CodePlayground />`.
```bash
sed -i '' 's/<RunCode.*>/<CodePlayground \/>/g' src/main/java/org/example/backend_fundamentals/design_patterns/creational/singleton/Singleton.md
```

- [ ] **Step 4: Commit**
```bash
git add docs/.vitepress src/main/java/org/example/backend_fundamentals/design_patterns/creational/singleton/Singleton.md
git commit -m "feat: implement multi-sidebar and CodePlayground embedded IDE"
```
