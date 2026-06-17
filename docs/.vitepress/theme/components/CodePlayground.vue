<template>
  <div class="playground-wrapper">
    <div v-if="files.length === 0" class="no-files">
      <div class="no-files-icon">☕</div>
      <div class="no-files-text">No Java files found in this directory.</div>
    </div>
    <div v-else class="playground-container">
      <div class="sidebar">
        <div class="sidebar-header">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px;"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>
          Java Explorer
        </div>
        <div 
          v-for="file in files" 
          :key="file.name"
          @click="selectedFile = file"
          :class="['file-item', selectedFile && selectedFile.name === file.name ? 'active' : '']"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="file-icon"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
          {{ file.name }}
        </div>
      </div>
      <div class="main-area">
        <div class="editor-header">
          <div class="file-title">{{ selectedFile ? selectedFile.name : '' }}</div>
          <button @click="runCode" :disabled="loading || !selectedFile" class="run-btn">
            <svg v-if="!loading" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px;"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
            <svg v-else class="spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px;"><line x1="12" y1="2" x2="12" y2="6"></line><line x1="12" y1="18" x2="12" y2="22"></line><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line><line x1="2" y1="12" x2="6" y2="12"></line><line x1="18" y1="12" x2="22" y2="12"></line><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line></svg>
            {{ loading ? 'Compiling & Running...' : 'Run Code Locally' }}
          </button>
        </div>
        <pre class="editor-content"><code>{{ selectedFile ? selectedFile.content : '' }}</code></pre>
        <div v-if="output || error" class="console">
          <div class="console-header">
            <span>Terminal Output</span>
            <button @click="output = ''; error = ''" class="clear-btn">Clear</button>
          </div>
          <div class="console-body">
            <pre v-if="output" class="stdout">{{ output }}</pre>
            <pre v-if="error" class="stderr">{{ error }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef, computed, watchEffect } from 'vue'
import { useRoute } from 'vitepress'

const route = useRoute()

// Fetch all Java files globally. We use a deeply nested relative path to reach the src directory
// Since this component is at docs/.vitepress/theme/components/
const rawFiles = import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.java', { as: 'raw', eager: true })

const allFiles = Object.keys(rawFiles).map(path => {
  const content = rawFiles[path]
  const name = path.split('/').pop()
  
  const packageMatch = content.match(/package\s+([^;]+);/)
  const packageName = packageMatch ? packageMatch[1].trim() : ''
  const className = name.replace('.java', '')
  const fqcn = packageName ? `${packageName}.${className}` : className
  
  // Extract the relative directory path from backend_fundamentals
  const relativeDirMatch = path.match(/backend_fundamentals\/(.*?)\/[^\/]+\.java$/)
  const directory = relativeDirMatch ? relativeDirMatch[1] : ''
  
  return { name, content, fqcn, directory }
})

const files = computed(() => {
  const currentPath = route.path
  
  // Try to match the directory structure from the current URL
  let routeDir = ''
  if (currentPath.endsWith('.html')) {
    const match = currentPath.match(/^\/(.*?)\/[^\/]+\.html$/)
    if (match) routeDir = match[1]
  } else if (currentPath.endsWith('/')) {
    routeDir = currentPath.replace(/^\//, '').replace(/\/$/, '')
  } else {
    const parts = currentPath.replace(/^\//, '').split('/')
    parts.pop()
    routeDir = parts.join('/')
  }
  
  return allFiles.filter(f => f.directory === routeDir)
})

const selectedFile = shallowRef(null)
const loading = ref(false)
const output = ref('')
const error = ref('')

watchEffect(() => {
  if (files.value.length > 0 && !selectedFile.value) {
    selectedFile.value = files.value[0]
  }
})

const runCode = async () => {
  if (!selectedFile.value || !selectedFile.value.fqcn) return
  
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
.playground-wrapper {
  margin: 32px 0;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(30, 30, 34, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  font-family: var(--vp-font-family-base);
}

.no-files {
  padding: 40px;
  text-align: center;
  color: #a0a0ab;
  background: rgba(20, 20, 24, 0.8);
}
.no-files-icon { font-size: 32px; margin-bottom: 12px; opacity: 0.5; }
.no-files-text { font-size: 15px; font-weight: 500; }

.playground-container { display: flex; height: 600px; }

.sidebar { 
  width: 260px; 
  background: rgba(18, 18, 22, 0.95); 
  border-right: 1px solid rgba(255, 255, 255, 0.05); 
  overflow-y: auto; 
}
.sidebar-header { 
  padding: 16px; 
  font-weight: 600; 
  font-size: 12px;
  color: #a0a0ab;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
}

.file-item { 
  padding: 12px 16px; 
  cursor: pointer; 
  font-family: var(--vp-font-family-mono); 
  font-size: 13px;
  color: #94a3b8;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
}
.file-icon { margin-right: 10px; opacity: 0.6; }
.file-item:hover { background: rgba(255, 255, 255, 0.03); color: #e2e8f0; }
.file-item.active { 
  background: linear-gradient(90deg, rgba(56, 189, 248, 0.1) 0%, transparent 100%);
  color: #38bdf8; 
  border-left: 3px solid #38bdf8; 
}
.file-item.active .file-icon { opacity: 1; stroke: #38bdf8; }

.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #1e1e1e; }

.editor-header { 
  display: flex; justify-content: space-between; align-items: center; 
  padding: 12px 20px; 
  background: rgba(24, 24, 28, 0.95); 
  border-bottom: 1px solid rgba(255, 255, 255, 0.05); 
}
.file-title {
  font-family: var(--vp-font-family-mono);
  font-size: 14px;
  color: #e2e8f0;
  font-weight: 500;
}

.run-btn { 
  background: linear-gradient(135deg, #0284c7, #2563eb);
  color: white; 
  border: none; 
  padding: 8px 16px; 
  border-radius: 6px; 
  cursor: pointer; 
  font-weight: 600;
  font-size: 13px;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(2, 132, 199, 0.3);
  display: flex;
  align-items: center;
}
.run-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(2, 132, 199, 0.4);
}
.run-btn:disabled { 
  opacity: 0.6; 
  cursor: not-allowed; 
  background: #334155;
  box-shadow: none; 
  transform: none;
}

.editor-content { 
  flex: 1; margin: 0; padding: 20px; overflow: auto; 
  font-family: var(--vp-font-family-mono); 
  font-size: 14px; 
  line-height: 1.6;
  color: #d4d4d4; 
  white-space: pre;
  tab-size: 4;
}

.console { 
  height: 200px; display: flex; flex-direction: column; 
  border-top: 1px solid #000; 
  background: #0d0d0d; 
}
.console-header { 
  padding: 8px 16px; 
  background: #151515; 
  color: #888; 
  font-size: 11px; text-transform: uppercase; letter-spacing: 1px; font-weight: 600;
  border-bottom: 1px solid #222;
  display: flex; justify-content: space-between; align-items: center;
}
.clear-btn {
  background: transparent; color: #888; border: none; font-size: 11px; cursor: pointer; text-transform: uppercase;
}
.clear-btn:hover { color: #fff; }

.console-body {
  flex: 1;
  overflow: auto;
  padding: 16px;
}
.stdout, .stderr { margin: 0; font-family: var(--vp-font-family-mono); font-size: 13px; line-height: 1.5; white-space: pre-wrap; }
.stdout { color: #34d399; }
.stderr { color: #f87171; }

@keyframes spin { 100% { transform: rotate(360deg); } }
.spinner { animation: spin 1s linear infinite; }
</style>
