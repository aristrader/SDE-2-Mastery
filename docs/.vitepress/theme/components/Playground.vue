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
          <div class="file-status">
            <span>{{ selected ? selected.rel : '' }}</span>
            <span v-if="selected" :class="['run-state', selectedRunnable ? 'runnable' : 'support']">
              {{ selectedRunnable ? 'runnable' : 'support file' }}
            </span>
          </div>
          <button class="run-btn" @click="runCode" :disabled="!canRun">
            {{ runButtonLabel }}
          </button>
        </div>
        <p class="run-warning">Hosted execution uses a remote sandbox. Do not submit proprietary code, API keys, credentials, or PII.</p>
        <ClientOnly>
          <CodeEditor v-if="selected" v-model="selectedCode" :readonly="false" />
        </ClientOnly>
        <div class="console" v-if="output">
          <div class="console-head">{{ outputLabel }}</div>
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

const raw = import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.java', { query: '?raw', import: 'default' })
const grouped = analyzeJavaFiles(raw)
const mdFolders = mdFolderSet(Object.keys(import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.md')))

const props = defineProps({
  targetDir: { type: String, default: '' },
  files: { type: String, default: '' },
})

const { page } = useData()
const route = useRoute()

function pageDir(relativePath) {
  const i = relativePath.lastIndexOf('/')
  return i === -1 ? '' : relativePath.slice(0, i)
}

function normalizeTargetDir(dir) {
  return dir.replace(/^\/+/, '').replace(/\/+$/, '').replace(/\/playground$/, '')
}

async function loadContent(file) {
  if (typeof file.content !== 'function') {
    return file.content
  }
  const loaded = await file.content()
  return typeof loaded === 'string' ? loaded : loaded.default
}

const files = computed(() => {
  let dir = props.targetDir || props.files
  if (!dir) {
    dir = pageDir(page.value.relativePath)
    // If we're on an exercise or solution page, resolve to the parent directory
    if (dir.endsWith('/exercise') || dir.endsWith('/solution') || dir.endsWith('/design')) {
      dir = dir.split('/').slice(0, -1).join('/')
    }
  }
  dir = normalizeTargetDir(dir)
  if (!dir) return []
  return filesForPage(grouped, mdFolders, dir)
})

const selected = ref(null)
const selectedCode = ref('')
const output = ref('')
const outputLabel = ref('Console')
const isRunning = ref(false)
const isRateLimited = ref(false)
const rateLimitSeconds = ref(0)
const selectedRunnable = ref(false)
let abortController = null
let rateLimitInterval = null
let runTimeout = null
let selectionToken = 0
let runToken = 0
const RUN_TIMEOUT_MS = 15000
const OUTPUT_LIMIT = 10000
const PISTON_EXECUTE_URL = import.meta.env.VITE_PISTON_EXECUTE_URL || 'https://emkc.org/api/v2/piston/execute'

function javaMeta(name, content) {
  const className = name.replace(/\.java$/, '')
  const pkgMatch = content.match(/package\s+([\w.]+)\s*;/)
  const pkg = pkgMatch ? pkgMatch[1] : ''
  const runnable = /public\s+static\s+void\s+main\s*\(/.test(content)
  return {
    runnable,
    fqcn: runnable ? (pkg ? `${pkg}.${className}` : className) : null,
  }
}

function clearRunTimeout() {
  if (runTimeout) {
    clearTimeout(runTimeout)
    runTimeout = null
  }
}

function clearRateLimit() {
  if (rateLimitInterval) {
    clearInterval(rateLimitInterval)
    rateLimitInterval = null
  }
  isRateLimited.value = false
  rateLimitSeconds.value = 0
}

function resetRunState() {
  runToken++
  if (abortController) {
    abortController.abort()
    abortController = null
  }
  clearRunTimeout()
  clearRateLimit()
  output.value = ''
  outputLabel.value = 'Console'
  isRunning.value = false
}

function boundedOutput(text) {
  if (!text) return ''
  if (text.length <= OUTPUT_LIMIT) return text
  return text.slice(0, OUTPUT_LIMIT) + '\n...[Output truncated]...'
}

function formatPistonOutput(data) {
  const compile = data.compile || {}
  const run = data.run || {}
  const serviceText = run.output || run.stderr || data.message || ''
  if (/piston api is now whitelist only/i.test(serviceText)) {
    return {
      label: 'Execution Unavailable',
      text: `${serviceText}\n\nConfigure VITE_PISTON_EXECUTE_URL with an approved or self-hosted Piston endpoint, or use a hardened local sidecar in local study mode.`,
    }
  }
  if (compile.code && compile.code !== 0) {
    return {
      label: 'Compile Error',
      text: compile.stderr || compile.output || data.message || 'Compilation failed.',
    }
  }
  if (run.code && run.code !== 0) {
    return {
      label: 'Runtime Error',
      text: run.stderr || run.output || data.message || 'Execution failed.',
    }
  }
  return {
    label: 'Success',
    text: run.output || run.stdout || compile.output || data.message || 'No output.',
  }
}

async function filesForRun() {
  if (!selected.value) return []
  const runFiles = [{
    name: selected.value.name,
    content: selectedCode.value
  }]

  for (const file of files.value) {
    if (file.rel === selected.value.rel) continue
    try {
      runFiles.push({
        name: file.name,
        content: await loadContent(file)
      })
    } catch (err) {
      // Keep the edited runnable file executable even if a support file fails to load.
    }
  }

  return runFiles
}

const canRun = computed(() => {
  return selected.value && selectedRunnable.value && selectedCode.value.trim() && !isRunning.value && !isRateLimited.value
})

const runButtonLabel = computed(() => {
  if (isRunning.value) return 'Running...'
  if (isRateLimited.value) return `Wait ${rateLimitSeconds.value}s`
  if (!selected.value) return 'Run'
  if (!selectedCode.value.trim()) return 'No code'
  if (!selectedRunnable.value) return 'No main'
  return 'Run'
})

async function select(f) {
  const token = ++selectionToken
  selected.value = f
  output.value = ''
  outputLabel.value = 'Console'
  selectedCode.value = 'Loading...'
  selectedRunnable.value = false

  try {
    const content = await loadContent(f)
    if (token === selectionToken && selected.value?.rel === f.rel) {
      const meta = javaMeta(f.name, content)
      selected.value = { ...f, ...meta }
      selectedRunnable.value = meta.runnable
      selectedCode.value = content
    }
  } catch (err) {
    if (token === selectionToken) {
      selectedCode.value = `// Failed to load ${f.name}: ${err.message}`
    }
  }
}

watch(files, async (list) => { 
  if (list.length) {
    await select(list[0])
  } else {
    selected.value = null
    selectedCode.value = ''
    selectedRunnable.value = false
  }
}, { immediate: true })

watch(() => route.path, () => {
  resetRunState()
})

onBeforeUnmount(() => {
  selectionToken++
  resetRunState()
})

async function runCode() {
  if (isRunning.value || isRateLimited.value) return
  if (!selected.value) return
  if (!selectedCode.value.trim()) {
    outputLabel.value = 'Input Required'
    output.value = 'Add Java code before running.'
    return
  }
  if (!selectedRunnable.value) {
    outputLabel.value = 'Not Runnable'
    output.value = 'This file has no public static void main method. You can browse or edit it as a support file, but it cannot run by itself.'
    return
  }
  
  const token = ++runToken
  isRunning.value = true
  outputLabel.value = 'Running'
  output.value = 'Compiling and running...'
  
  if (abortController) {
    abortController.abort()
  }
  abortController = new AbortController()
  let timedOut = false
  clearRunTimeout()
  runTimeout = setTimeout(() => {
    timedOut = true
    abortController?.abort()
  }, RUN_TIMEOUT_MS)

  try {
    const payloadFiles = await filesForRun()
    if (token !== runToken) return

    const res = await fetch(PISTON_EXECUTE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: abortController.signal,
      body: JSON.stringify({
        language: 'java',
        version: '15.0.2',
        files: payloadFiles
      })
    })

    if (res.status === 429) {
      if (token !== runToken) return
      const retryAfter = parseInt(res.headers.get('Retry-After') || '10', 10)
      isRateLimited.value = true
      rateLimitSeconds.value = retryAfter
      outputLabel.value = 'Rate Limited'
      output.value = `Rate limited. Please wait ${retryAfter} seconds.`
      
      if (rateLimitInterval) clearInterval(rateLimitInterval)
      rateLimitInterval = setInterval(() => {
        rateLimitSeconds.value--
        if (rateLimitSeconds.value <= 0) {
          clearInterval(rateLimitInterval)
          rateLimitInterval = null
          isRateLimited.value = false
        }
      }, 1000)
      return
    }

    const data = await res.json()
    if (token !== runToken) return
    const formatted = formatPistonOutput(data)
    outputLabel.value = formatted.label
    output.value = boundedOutput(formatted.text)
  } catch (err) {
    if (token !== runToken) return
    if (err.name === 'AbortError' && timedOut) {
      outputLabel.value = 'Timeout'
      output.value = 'Execution timed out. Try a smaller example or rerun later.'
    } else if (err.name === 'AbortError') {
      outputLabel.value = 'Aborted'
      output.value = 'Execution aborted.'
    } else {
      outputLabel.value = 'Error'
      output.value = `Error: ${err.message}`
    }
  } finally {
    if (token === runToken) {
      isRunning.value = false
      clearRunTimeout()
    }
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
.file-status { display: flex; align-items: center; min-width: 0; gap: 8px; }
.file-status > span:first-child { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.run-state { flex: none; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; border-radius: 4px; padding: 1px 5px; }
.run-state.runnable { color: #166534; background: rgba(34, 197, 94, 0.14); }
.run-state.support { color: #854d0e; background: rgba(245, 158, 11, 0.16); }
.run-btn { background: var(--vp-c-brand-1); color: white; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.run-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.run-warning { margin: 0 0 8px; padding: 7px 10px; border: 1px solid var(--vp-c-divider); border-top: none; background: var(--vp-c-bg-soft); color: var(--vp-c-text-2); font-size: 12px; line-height: 1.45; }
.console { margin-top: 16px; border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; background: #1e1e1e; color: #d4d4d4; }
.console-head { padding: 4px 10px; background: #2d2d2d; font-size: 12px; font-family: var(--vp-font-family-mono); }
.console-out { padding: 12px; margin: 0; font-family: var(--vp-font-family-mono); font-size: 13px; white-space: pre-wrap; word-break: break-all; max-height: 250px; overflow-y: auto; }
.playground-empty { padding: 24px; color: var(--vp-c-text-2); }
@media (max-width: 960px) { .playground { grid-template-columns: 1fr; } }
</style>
