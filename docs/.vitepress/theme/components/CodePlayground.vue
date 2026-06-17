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
