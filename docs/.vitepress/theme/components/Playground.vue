<template>
  <div class="playground">
    <template v-if="files.length">
      <aside class="files">
        <div class="files-head">Files</div>
        <div
          v-for="f in files" :key="f.rel"
          :class="['file', selected && selected.rel === f.rel ? 'active' : '']"
          @click="select(f)"
        >
          <span class="fname"><span v-if="f.group" class="grp">{{ f.group }}/</span>{{ f.name }}<span v-if="f.kind === 'exercise'" class="badge">exercise</span></span>
        </div>
      </aside>

      <section class="main">
        <div class="editor-head">
          <span>{{ selected ? selected.rel : '' }}</span>
          <button class="run-btn" @click="runCode" :disabled="isRunning || isRateLimited">
            {{ isRunning ? 'Running...' : (isRateLimited ? `Wait ${rateLimitSeconds}s` : 'Run') }}
          </button>
        </div>
        <ClientOnly>
          <CodeEditor v-if="selected" v-model="selectedCode" :readonly="false" />
        </ClientOnly>
        <div class="console" v-if="output">
          <div class="console-head">Console</div>
          <pre class="console-out">{{ output }}</pre>
        </div>
      </section>
    </template>
    <div v-else class="playground-empty">No Java files in this folder.</div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { useData, useRoute } from 'vitepress'
import CodeEditor from './CodeEditor.vue'
import { analyzeJavaFiles, mdFolderSet, filesForPage } from '../lib/fileDiscovery.mjs'

const raw = import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.java', { query: '?raw', import: 'default', eager: true })
const grouped = analyzeJavaFiles(raw)
const mdFolders = mdFolderSet(Object.keys(import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.md')))

const props = defineProps({
  targetDir: { type: String, default: '' }
})

const { page } = useData()
const route = useRoute()

function pageDir(relativePath) {
  const i = relativePath.lastIndexOf('/')
  return i === -1 ? '' : relativePath.slice(0, i)
}

const files = computed(() => {
  let dir = props.targetDir
  if (!dir) {
    dir = pageDir(page.value.relativePath)
    // If we're on an exercise or solution page, resolve to the parent directory
    if (dir.endsWith('/exercise') || dir.endsWith('/solution')) {
      dir = dir.split('/').slice(0, -1).join('/')
    }
  }
  if (!dir) return []
  const result = filesForPage(grouped, mdFolders, dir)
  console.log("PLAYGROUND MOUNTED. DIR:", dir, "FILES:", result.length)
  return result
})

const selected = ref(null)
const selectedCode = ref('')
const output = ref('')
const isRunning = ref(false)
const isRateLimited = ref(false)
const rateLimitSeconds = ref(0)
let abortController = null

async function select(f) {
  selected.value = f
  output.value = ''
  selectedCode.value = 'Loading...'
  
  if (typeof f.content === 'function') {
    selectedCode.value = await f.content()
  } else {
    selectedCode.value = f.content
  }
}

watch(files, async (list) => { 
  if (list.length) {
    await select(list[0])
  } else {
    selected.value = null
    selectedCode.value = ''
  }
}, { immediate: true })

watch(() => route.path, () => {
  if (abortController) {
    abortController.abort()
  }
  output.value = ''
  isRunning.value = false
})

onBeforeUnmount(() => {
  if (abortController) {
    abortController.abort()
  }
})

async function runCode() {
  if (!selected.value || isRunning.value || isRateLimited.value) return
  
  isRunning.value = true
  output.value = 'Compiling and running...'
  
  if (abortController) {
    abortController.abort()
  }
  abortController = new AbortController()

  try {
    const res = await fetch('https://emkc.org/api/v2/piston/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: abortController.signal,
      body: JSON.stringify({
        language: 'java',
        version: '15.0.2',
        files: [{
          name: selected.value.name,
          content: selectedCode.value
        }]
      })
    })

    if (res.status === 429) {
      const retryAfter = parseInt(res.headers.get('Retry-After') || '10', 10)
      isRateLimited.value = true
      rateLimitSeconds.value = retryAfter
      output.value = `Rate limited. Please wait ${retryAfter} seconds.`
      
      const interval = setInterval(() => {
        rateLimitSeconds.value--
        if (rateLimitSeconds.value <= 0) {
          clearInterval(interval)
          isRateLimited.value = false
        }
      }, 1000)
      return
    }

    const data = await res.json()
    let textOut = data.run ? (data.run.output || data.run.stderr) : data.message
    
    // OOM Truncation limit (resilience against 50k chars)
    if (textOut && textOut.length > 10000) {
      textOut = textOut.slice(0, 10000) + '\n...[Output truncated]...'
    }
    
    output.value = textOut || 'No output.'
  } catch (err) {
    if (err.name === 'AbortError') {
      output.value = 'Execution aborted.'
    } else {
      output.value = `Error: ${err.message}`
    }
  } finally {
    isRunning.value = false
  }
}
</script>

<style scoped>
.playground { display: grid; grid-template-columns: 240px 1fr; gap: 16px; min-height: 60vh; margin-top: 1rem; }
.files { border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; height: fit-content; }
.files-head { padding: 8px 12px; font-weight: 700; border-bottom: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft); }
.file { display: flex; align-items: center; padding: 8px 10px; cursor: pointer; font-family: var(--vp-font-family-mono); font-size: 13px; }
.file:hover { background: var(--vp-c-bg-soft); }
.file.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); }
.fname { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.fname .grp { color: var(--vp-c-text-3); }
.fname .badge { margin-left: 6px; font-size: 9px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; color: #b45309; background: rgba(245, 158, 11, 0.16); padding: 1px 5px; border-radius: 4px; vertical-align: middle; }
.main { display: flex; flex-direction: column; min-width: 0; }
.editor-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; padding: 6px 10px; background: var(--vp-c-bg-soft); border: 1px solid var(--vp-c-divider); border-bottom: none; border-radius: 8px 8px 0 0; font-family: var(--vp-font-family-mono); font-size: 12px; }
.run-btn { background: var(--vp-c-brand-1); color: white; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.run-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.console { margin-top: 16px; border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; background: #1e1e1e; color: #d4d4d4; }
.console-head { padding: 4px 10px; background: #2d2d2d; font-size: 12px; font-family: var(--vp-font-family-mono); }
.console-out { padding: 12px; margin: 0; font-family: var(--vp-font-family-mono); font-size: 13px; white-space: pre-wrap; word-break: break-all; max-height: 250px; overflow-y: auto; }
.playground-empty { padding: 24px; color: var(--vp-c-text-2); }
@media (max-width: 960px) { .playground { grid-template-columns: 1fr; } }
</style>
