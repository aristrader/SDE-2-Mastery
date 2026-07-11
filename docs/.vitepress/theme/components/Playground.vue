<template>
  <div class="playground">
    <template v-if="workspaceFiles.length">
      <aside class="files">
        <div class="files-head">Files</div>
        <div
          v-for="f in workspaceFiles" :key="f.rel"
          :class="['file', selected && selected.rel === f.rel ? 'active' : '']"
          @click="select(f)"
        >
          <span class="fname"><span v-if="f.group" class="grp">{{ f.group }}/</span>{{ f.name }}<span v-if="f.kind === 'exercise'" class="badge">exercise</span><span v-if="isDirty(f)" class="dirty">modified</span></span>
        </div>
        <div class="file-actions">
          <button type="button" @click="createFile">New class</button>
          <button type="button" @click="renameFile" :disabled="!selected?.createdByUser">Rename</button>
          <button type="button" @click="deleteFile" :disabled="!selected?.createdByUser">Delete</button>
          <button type="button" @click="resetFile" :disabled="!selected || !isDirty(selected)">Reset file</button>
          <button type="button" @click="resetWorkspace" :disabled="!hasWorkspaceChanges">Reset all</button>
        </div>
      </aside>

      <section class="main">
        <div class="editor-head">
          <div class="file-status">
          <span>{{ selected ? selected.rel : '' }}</span>
            <span v-if="selected" :class="['run-state', selectedRunnable ? 'runnable' : 'support']">
              {{ selectedRunnable ? 'runnable' : 'support file' }}
            </span>
            <span v-if="javaLspEnabled" :class="['lsp-state', lspStatus]">{{ lspLabel }}</span>
          </div>
          <button class="run-btn" @click="runCode" :disabled="!canRun">
            {{ runButtonLabel }}
          </button>
        </div>
        <p class="run-warning">{{ runnerNotice }}</p>
        <ClientOnly>
          <CodeEditor
            v-if="selected"
            ref="codeEditorRef"
            v-model="selectedCode"
            :readonly="false"
            :completion-words="completionWords"
            :diagnostics="selectedDiagnostics"
            :semantic-completion-provider="lspCompletionProvider"
            :semantic-completion-resolver="lspCompletionResolver"
            :semantic-hover-provider="lspHoverProvider"
            :semantic-definition-provider="lspDefinitionProvider"
            :semantic-format-provider="lspFormatProvider"
            :semantic-code-action-provider="lspCodeActionProvider"
            @run="runCode"
          />
        </ClientOnly>
        <div class="diagnostics" v-if="visibleDiagnostics.length">
          <button
            v-for="(item, index) in visibleDiagnostics"
            :key="`${item.file}:${item.line}:${index}`"
            type="button"
            class="diagnostic"
            @click="openDiagnostic(item)"
          >
            {{ item.file }}:{{ item.line }} - {{ item.message }}
          </button>
        </div>
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
const STORAGE_PREFIX = 'java-workspace:v1:'
const STORAGE_LIMIT = 200_000
const MAX_USER_FILES = 12

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

function dirname(path) {
  const i = path.lastIndexOf('/')
  return i === -1 ? '' : path.slice(0, i)
}

function simpleHash(text) {
  let hash = 0
  for (let i = 0; i < text.length; i++) hash = ((hash << 5) - hash + text.charCodeAt(i)) | 0
  return String(hash)
}

function escapeRegExp(text) {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
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
  let pageType = 'theory'
  if (!dir) {
    dir = pageDir(page.value.relativePath)
    // If we're on an exercise or solution page, resolve to the parent directory
    if (dir.endsWith('/exercise') || dir.endsWith('/solution') || dir.endsWith('/design')) {
      pageType = dir.split('/').pop()
      dir = dir.split('/').slice(0, -1).join('/')
    }
  }
  dir = normalizeTargetDir(dir)
  if (!dir) return []
  const pageFiles = filesForPage(grouped, mdFolders, dir)
  if (pageType === 'theory' && !props.targetDir && !props.files) {
    return pageFiles.filter((file) => file.kind !== 'exercise')
  }
  return pageFiles
})

const storageKey = computed(() => `${STORAGE_PREFIX}${route.path.split('#')[0]}`)
const workspaceFiles = ref([])

const selected = ref(null)
const selectedCode = ref('')
const codeEditorRef = ref(null)
const output = ref('')
const outputLabel = ref('Console')
const isRunning = ref(false)
const isRateLimited = ref(false)
const rateLimitSeconds = ref(0)
const selectedRunnable = ref(false)
const diagnostics = ref([])
let abortController = null
let rateLimitInterval = null
let runTimeout = null
let selectionToken = 0
let runToken = 0
const RUN_TIMEOUT_MS = 15000
const OUTPUT_LIMIT = 10000
const PISTON_EXECUTE_URL = import.meta.env.VITE_PISTON_EXECUTE_URL || ''
const JAVA_RUNNER_URL = import.meta.env.VITE_JAVA_RUNNER_URL || '/api/run-java'
const usesPiston = computed(() => Boolean(PISTON_EXECUTE_URL))
const completionWords = computed(() => workspaceFiles.value.map(file => file.name.replace(/\.java$/, '')))
const hasWorkspaceChanges = computed(() => workspaceFiles.value.some(file => isDirty(file) || file.createdByUser))
const javaLspEnabled = computed(() => import.meta.env.DEV && import.meta.env.VITE_ENABLE_JAVA_LSP === '1' && route.path.startsWith('/java/'))
const lspStatus = ref('off')
const lspMessage = ref('')
const lspDiagnostics = ref([])
const visibleDiagnostics = computed(() => [...lspDiagnostics.value, ...diagnostics.value])
const selectedDiagnostics = computed(() => visibleDiagnostics.value.filter(item => item.file === selected.value?.name))
const lspLabel = computed(() => {
  if (lspStatus.value === 'ready') return 'LSP ready'
  if (lspStatus.value === 'failed') return 'LSP failed'
  if (lspStatus.value === 'starting') return 'LSP starting'
  return 'LSP off'
})
const sessionId = `java-lsp-${Date.now()}-${Math.random().toString(36).slice(2)}`
const lspPending = new Map()
let lspRequestId = 0
let lspChangeTimer = null
let lspHeartbeatTimer = null
const lspHandlers = []
let lspUnloadHandler = null

function isDirty(file) {
  return Boolean(file && !file.createdByUser && file.content !== file.starterContent)
}

function workspaceSignature(list) {
  return simpleHash(list.map(file => `${file.rel}:${file.starterContent.length}:${simpleHash(file.starterContent)}`).join('|'))
}

function loadSavedWorkspace(signature) {
  if (typeof window === 'undefined') return null
  try {
    const raw = window.localStorage.getItem(storageKey.value)
    if (!raw) return null
    if (raw.length > STORAGE_LIMIT) {
      window.localStorage.removeItem(storageKey.value)
      return null
    }
    const parsed = JSON.parse(raw)
    return parsed?.signature === signature ? parsed : null
  } catch {
    return null
  }
}

function saveWorkspace() {
  if (typeof window === 'undefined' || !workspaceFiles.value.length) return
  const starters = workspaceFiles.value.filter(file => !file.createdByUser)
  const payload = {
    signature: workspaceSignature(starters),
    selectedRel: selected.value?.rel || '',
    edits: Object.fromEntries(starters
      .filter(file => file.content !== file.starterContent)
      .map(file => [file.rel, file.content])),
    created: workspaceFiles.value
      .filter(file => file.createdByUser)
      .map(({ name, rel, group, kind, content, starterContent }) => ({ name, rel, group, kind, content, starterContent })),
  }
  const json = JSON.stringify(payload)
  if (json.length <= STORAGE_LIMIT) {
    window.localStorage.setItem(storageKey.value, json)
  } else {
    window.localStorage.removeItem(storageKey.value)
  }
}

function packageFrom(content) {
  const match = content.match(/^\s*package\s+([\w.]+)\s*;/m)
  return match ? match[1] : ''
}

function validJavaName(name) {
  return /^[A-Za-z_$][\w$]*\.java$/.test(name)
}

function lspBalancedContent(code) {
  const text = code || ''
  const opens = (text.match(/{/g) || []).length
  const closes = (text.match(/}/g) || []).length
  return closes < opens ? `${text}\n${'}\n'.repeat(opens - closes)}` : text
}

function currentLspFiles() {
  return workspaceFiles.value.map(file => ({
    name: file.name,
    content: lspBalancedContent(file.rel === selected.value?.rel ? selectedCode.value : file.content),
  }))
}

function sendLsp(event, data = {}) {
  if (!javaLspEnabled.value || !import.meta.hot) return
  import.meta.hot.send(event, { sessionId, ...data })
}

function startLsp() {
  if (!javaLspEnabled.value || !workspaceFiles.value.length) return
  lspStatus.value = 'starting'
  lspMessage.value = ''
  lspDiagnostics.value = []
  sendLsp('java-lsp:start', {
    route: route.path,
    files: currentLspFiles(),
  })
  clearInterval(lspHeartbeatTimer)
  lspHeartbeatTimer = setInterval(() => sendLsp('java-lsp:heartbeat'), 5000)
}

function stopLsp() {
  if (!import.meta.env.DEV || import.meta.env.VITE_ENABLE_JAVA_LSP !== '1') return
  sendLsp('java-lsp:stop')
  clearInterval(lspHeartbeatTimer)
  lspHeartbeatTimer = null
  lspPending.forEach(({ reject }) => reject(new Error('Java LSP stopped.')))
  lspPending.clear()
  lspDiagnostics.value = []
  lspStatus.value = 'off'
}

function stopLspKeepalive() {
  if (!import.meta.env.DEV || import.meta.env.VITE_ENABLE_JAVA_LSP !== '1' || typeof window === 'undefined') return
  const body = JSON.stringify({ sessionId })
  if (navigator.sendBeacon) {
    navigator.sendBeacon('/api/java-lsp-stop', new Blob([body], { type: 'application/json' }))
    return
  }
  fetch('/api/java-lsp-stop', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body,
    keepalive: true,
  }).catch(() => {})
}

function scheduleLspChange() {
  if (!javaLspEnabled.value || lspStatus.value !== 'ready' || !selected.value) return
  clearTimeout(lspChangeTimer)
  lspChangeTimer = setTimeout(() => {
    sendLsp('java-lsp:change', { file: selected.value.name, content: lspBalancedContent(selectedCode.value) })
  }, 250)
}

function syncLspModel(model) {
  if (!javaLspEnabled.value || lspStatus.value !== 'ready' || !selected.value) return Promise.resolve()
  clearTimeout(lspChangeTimer)
  sendLsp('java-lsp:change', { file: selected.value.name, content: lspBalancedContent(model.getValue()) })
  return new Promise(resolve => setTimeout(resolve, 350))
}

function lspRequest(method, params) {
  if (!javaLspEnabled.value || lspStatus.value !== 'ready') return Promise.resolve(null)
  const requestId = ++lspRequestId
  sendLsp('java-lsp:request', { requestId, method, params })
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {
      lspPending.delete(requestId)
      resolve(null)
    }, 5000)
    lspPending.set(requestId, {
      resolve: value => {
        clearTimeout(timer)
        resolve(value)
      },
      reject: err => {
        clearTimeout(timer)
        reject(err)
      },
    })
  })
}

function onLsp(event, handler) {
  if (!import.meta.hot) return
  import.meta.hot.on(event, handler)
  lspHandlers.push([event, handler])
}

function lspTextDocument(model) {
  return { uri: `file:///${selected.value?.name || 'Current.java'}` }
}

function lspPosition(position) {
  return { line: position.lineNumber - 1, character: position.column - 1 }
}

function toMonacoRange(range, monaco) {
  return new monaco.Range(
    (range?.start?.line ?? 0) + 1,
    (range?.start?.character ?? 0) + 1,
    (range?.end?.line ?? range?.start?.line ?? 0) + 1,
    (range?.end?.character ?? range?.start?.character ?? 0) + 1,
  )
}

function lspEditToMonaco(edit, monaco) {
  return {
    range: toMonacoRange(edit.range, monaco),
    text: edit.newText || '',
  }
}

function lspWorkspaceEditToMonaco(edit, monaco, model) {
  const changes = edit?.changes || {}
  const uri = Object.keys(changes).find(item => decodeURIComponent(item.split('/').pop() || '') === selected.value?.name)
  if (uri) return { edits: changes[uri].map(item => lspEditToMonaco(item, monaco)) }
  const docChange = (edit?.documentChanges || []).find(item => decodeURIComponent(item.textDocument?.uri?.split('/').pop() || '') === selected.value?.name)
  if (!docChange?.edits) return null
  return { edits: docChange.edits.map(item => lspEditToMonaco(item, monaco)) }
}

async function hydrateWorkspace(starters) {
  const loaded = []
  for (const file of starters) {
    const content = await loadContent(file)
    loaded.push({ ...file, content, starterContent: content, createdByUser: false })
  }
  const signature = workspaceSignature(loaded)
  const saved = loadSavedWorkspace(signature)
  if (saved) {
    for (const file of loaded) {
      if (typeof saved.edits?.[file.rel] === 'string') file.content = saved.edits[file.rel]
    }
    const created = Array.isArray(saved.created) ? saved.created.slice(0, MAX_USER_FILES) : []
    for (const file of created) {
      if (validJavaName(file.name) && typeof file.content === 'string' && file.content.length <= 100_000 && !loaded.some(item => item.rel === file.rel)) {
        loaded.push({ ...file, createdByUser: true, starterContent: file.starterContent || file.content, kind: file.kind || 'example' })
      }
    }
  }
  workspaceFiles.value = loaded
  const savedSelection = saved?.selectedRel ? loaded.find(file => file.rel === saved.selectedRel) : null
  await select(savedSelection || loaded[0])
  startLsp()
}

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
  diagnostics.value = []
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

function formatLocalOutput(data) {
  if (data.phase === 'compile' && !data.ok) {
    return {
      label: 'Compile Error',
      text: data.stderr || data.stdout || data.error || 'Compilation failed.',
    }
  }
  if (!data.ok) {
    return {
      label: data.error && /timed out/i.test(data.error) ? 'Timeout' : 'Runtime Error',
      text: [data.stderr, data.stdout, data.error].filter(Boolean).join('\n') || 'Execution failed.',
    }
  }
  return {
    label: 'Success',
    text: data.stdout || data.stderr || 'Process finished with no output.',
  }
}

async function filesForRun() {
  return workspaceFiles.value.map(file => ({
    name: file.name,
    content: file.rel === selected.value?.rel ? selectedCode.value : file.content,
  }))
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

const runnerNotice = computed(() => {
  if (!usesPiston.value) {
    return 'Local study mode: edited Java is compiled in a temporary directory and run through the local JDK. Do not run untrusted code.'
  }
  return 'Hosted execution uses a remote sandbox. Do not submit proprietary code, API keys, credentials, or PII.'
})

async function select(f) {
  const token = ++selectionToken
  if (!f) {
    selected.value = null
    selectedCode.value = ''
    selectedRunnable.value = false
    return
  }
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
      saveWorkspace()
    }
  } catch (err) {
    if (token === selectionToken) {
      selectedCode.value = `// Failed to load ${f.name}: ${err.message}`
    }
  }
}

watch(files, async (list) => { 
  if (list.length) {
    await hydrateWorkspace(list)
  } else {
    workspaceFiles.value = []
    selected.value = null
    selectedCode.value = ''
    selectedRunnable.value = false
  }
}, { immediate: true })

watch(selectedCode, (code) => {
  if (!selected.value || code === 'Loading...') return
  const file = workspaceFiles.value.find(item => item.rel === selected.value.rel)
  if (!file) return
  file.content = code
  const meta = javaMeta(file.name, code)
  Object.assign(file, meta)
  selected.value = { ...file }
  selectedRunnable.value = meta.runnable
  saveWorkspace()
  scheduleLspChange()
})

watch(() => route.path, () => {
  resetRunState()
  if (javaLspEnabled.value) startLsp()
  else stopLsp()
})

onBeforeUnmount(() => {
  selectionToken++
  resetRunState()
  stopLsp()
  clearTimeout(lspChangeTimer)
  clearInterval(lspHeartbeatTimer)
  if (import.meta.hot?.off) {
    lspHandlers.forEach(([event, handler]) => import.meta.hot.off(event, handler))
    lspHandlers.length = 0
  }
  if (lspUnloadHandler && typeof window !== 'undefined') {
    window.removeEventListener('pagehide', lspUnloadHandler)
    window.removeEventListener('beforeunload', lspUnloadHandler)
    lspUnloadHandler = null
  }
})

if (import.meta.env.DEV && import.meta.hot) {
  onLsp('java-lsp:status', data => {
    if (data.sessionId !== sessionId) return
    lspStatus.value = data.status || 'failed'
    lspMessage.value = data.message || ''
  })
  onLsp('java-lsp:diagnostics', data => {
    if (data.sessionId !== sessionId) return
    lspDiagnostics.value = [
      ...lspDiagnostics.value.filter(item => item.file !== data.file),
      ...(data.diagnostics || []),
    ]
  })
  onLsp('java-lsp:response', data => {
    if (data.sessionId !== sessionId) return
    const pending = lspPending.get(data.requestId)
    if (!pending) return
    lspPending.delete(data.requestId)
    data.error ? pending.reject(new Error(data.error)) : pending.resolve(data.result)
  })
  onLsp('java-lsp:log', data => {
    if (data.sessionId !== sessionId) return
    lspMessage.value = data.message || lspMessage.value
  })
  if (typeof window !== 'undefined') {
    lspUnloadHandler = () => stopLspKeepalive()
    window.addEventListener('pagehide', lspUnloadHandler)
    window.addEventListener('beforeunload', lspUnloadHandler)
  }
}

function currentFolder() {
  const base = selected.value || workspaceFiles.value[0]
  return base ? dirname(base.rel) : ''
}

function classSkeleton(name) {
  const className = name.replace(/\.java$/, '')
  const pkg = packageFrom(selectedCode.value) || packageFrom(workspaceFiles.value[0]?.content || '')
  return `${pkg ? `package ${pkg};\n\n` : ''}public class ${className} {\n}\n`
}

async function createFile() {
  if (workspaceFiles.value.filter(file => file.createdByUser).length >= MAX_USER_FILES) return
  const rawName = window.prompt('New Java class name')
  if (!rawName) return
  const name = rawName.endsWith('.java') ? rawName : `${rawName}.java`
  if (!validJavaName(name)) return window.alert('Use a valid Java file name, for example Helper.java.')
  const folder = currentFolder()
  const rel = `${folder}/${name}`
  if (workspaceFiles.value.some(file => file.rel === rel)) return window.alert(`${name} already exists here.`)
  const group = selected.value?.group || ''
  const content = classSkeleton(name)
  const file = { name, rel, group, kind: 'example', content, starterContent: content, createdByUser: true, runnable: false, fqcn: null }
  workspaceFiles.value.push(file)
  await select(file)
  saveWorkspace()
}

async function renameFile() {
  if (!selected.value?.createdByUser) return
  const rawName = window.prompt('Rename Java class', selected.value.name)
  if (!rawName) return
  const name = rawName.endsWith('.java') ? rawName : `${rawName}.java`
  if (!validJavaName(name)) return window.alert('Use a valid Java file name, for example Helper.java.')
  const rel = `${dirname(selected.value.rel)}/${name}`
  if (workspaceFiles.value.some(file => file.rel === rel && file.rel !== selected.value.rel)) return window.alert(`${name} already exists here.`)
  const file = workspaceFiles.value.find(item => item.rel === selected.value.rel)
  if (!file) return
  const oldClassName = file.name.replace(/\.java$/, '')
  const newClassName = name.replace(/\.java$/, '')
  file.name = name
  file.rel = rel
  file.content = file.content.replace(new RegExp(`\\bpublic\\s+class\\s+${escapeRegExp(oldClassName)}\\b`), `public class ${newClassName}`)
  selected.value = { ...file }
  selectedCode.value = file.content
  saveWorkspace()
}

async function deleteFile() {
  if (!selected.value?.createdByUser) return
  workspaceFiles.value = workspaceFiles.value.filter(file => file.rel !== selected.value.rel)
  await select(workspaceFiles.value[0] || null)
  saveWorkspace()
}

async function resetFile() {
  if (!selected.value) return
  const file = workspaceFiles.value.find(item => item.rel === selected.value.rel)
  if (!file || file.createdByUser) return
  file.content = file.starterContent
  await select(file)
  saveWorkspace()
}

async function resetWorkspace() {
  if (typeof window !== 'undefined') window.localStorage.removeItem(storageKey.value)
  await hydrateWorkspace(files.value)
}

async function lspCompletionProvider(model, position, fallbackRange, monaco) {
  await syncLspModel(model)
  const triggerCharacter = model.getValueInRange({
    startLineNumber: position.lineNumber,
    startColumn: Math.max(1, position.column - 1),
    endLineNumber: position.lineNumber,
    endColumn: position.column,
  })
  const result = await lspRequest('textDocument/completion', {
    textDocument: lspTextDocument(model),
    position: lspPosition(position),
    context: triggerCharacter === '.' ? { triggerKind: 2, triggerCharacter: '.' } : { triggerKind: 1 },
  })
  const items = Array.isArray(result) ? result : (result?.items || [])
  return pruneNoisyLspItems(items).slice(0, 120).map((item, index) => completionItemToMonaco(item, fallbackRange, monaco, index))
}

function pruneNoisyLspItems(items) {
  const hasStudyPackage = items.some(item => /^(java\.lang|java\.util|java\.io|java\.nio)\b/.test(String(item.detail || '')))
  if (!hasStudyPackage) return items
  return items.filter(item => !/^(com\.sun|sun\.|jdk\.|javax\.|org\.w3c|java\.awt|java\.lang\.classfile|java\.lang\.reflect\.Array|java\.sql\.Array)\b/.test(String(item.detail || '')))
}

function lspCompletionKind(kind, monaco) {
  const map = {
    2: monaco.languages.CompletionItemKind.Method,
    3: monaco.languages.CompletionItemKind.Function,
    5: monaco.languages.CompletionItemKind.Field,
    6: monaco.languages.CompletionItemKind.Variable,
    7: monaco.languages.CompletionItemKind.Class,
    8: monaco.languages.CompletionItemKind.Interface,
    9: monaco.languages.CompletionItemKind.Module,
    10: monaco.languages.CompletionItemKind.Property,
    14: monaco.languages.CompletionItemKind.Keyword,
    15: monaco.languages.CompletionItemKind.Snippet,
    20: monaco.languages.CompletionItemKind.Enum,
    21: monaco.languages.CompletionItemKind.Constant,
  }
  return map[kind] || monaco.languages.CompletionItemKind.Text
}

function lspSortText(item, index) {
  const detail = String(item.detail || '')
  const label = String(item.label || '').split(/\s+-/)[0]
  const commonUtil = /^(List|ArrayList|LinkedList|ArrayDeque|Deque|Queue|PriorityQueue|Map|HashMap|LinkedHashMap|TreeMap|Set|HashSet|LinkedHashSet|TreeSet|Iterator|ListIterator|Collections|Arrays)$/
  const commonLang = /^(String|Integer|Long|Double|Float|Boolean|Character|Byte|Short|Object|System|Math|Exception|RuntimeException)$/
  const internalPenalty = /^(com\.sun|jdk\.internal)\b/.test(detail) ? 8 : 0
  const packageRank = commonUtil.test(label) && detail.startsWith('java.util') ? 0 : commonLang.test(label) && detail.startsWith('java.lang') ? 1 : detail.startsWith('java.util') ? 2 : detail.startsWith('java.io') ? 3 : detail.startsWith('java.nio') ? 4 : detail.startsWith('java.lang.classfile') ? 8 : detail.startsWith('java.awt') ? 8 : detail.startsWith('java.lang') ? 5 : 6
  return `${packageRank + internalPenalty}-${String(label.length).padStart(3, '0')}-${item.sortText || String(index).padStart(4, '0')}`
}

function completionItemToMonaco(item, fallbackRange, monaco, index = 0) {
  return {
    label: item.label,
    kind: lspCompletionKind(item.kind, monaco),
    detail: item.detail,
    documentation: typeof item.documentation === 'string' ? item.documentation : item.documentation?.value,
    insertText: item.textEdit?.newText || item.insertText || item.label,
    range: item.textEdit?.range ? toMonacoRange(item.textEdit.range, monaco) : fallbackRange,
    additionalTextEdits: (item.additionalTextEdits || []).map(edit => lspEditToMonaco(edit, monaco)),
    commitCharacters: item.commitCharacters,
    filterText: item.filterText,
    sortText: lspSortText(item, index),
    lspItem: item,
  }
}

async function lspCompletionResolver(item, monaco) {
  if (!item?.lspItem) return item
  const resolved = await lspRequest('completionItem/resolve', item.lspItem).catch(() => null)
  return resolved ? { ...item, ...completionItemToMonaco(resolved, item.range, monaco), lspItem: resolved } : item
}

async function lspHoverProvider(model, position) {
  const result = await lspRequest('textDocument/hover', {
    textDocument: lspTextDocument(model),
    position: lspPosition(position),
  })
  if (!result?.contents) return null
  const raw = Array.isArray(result.contents) ? result.contents : [result.contents]
  return { contents: raw.map(item => ({ value: typeof item === 'string' ? item : item.value || String(item) })) }
}

async function lspDefinitionProvider(model, position, monaco) {
  const result = await lspRequest('textDocument/definition', {
    textDocument: lspTextDocument(model),
    position: lspPosition(position),
  })
  const location = Array.isArray(result) ? result[0] : result
  if (!location?.uri) return []
  const fileName = decodeURIComponent(location.uri.split('/').pop() || '')
  const file = workspaceFiles.value.find(item => item.name === fileName)
  if (file && file.rel !== selected.value?.rel) {
    await select(file)
    setTimeout(() => codeEditorRef.value?.focusLine((location.range?.start?.line ?? 0) + 1), 0)
    return []
  }
  return [{
    uri: model.uri,
    range: toMonacoRange(location.range, monaco),
  }]
}

async function lspFormatProvider(model, monaco) {
  const edits = await lspRequest('textDocument/formatting', {
    textDocument: lspTextDocument(model),
    options: { tabSize: 4, insertSpaces: true },
  })
  return Array.isArray(edits) ? edits.map(edit => lspEditToMonaco(edit, monaco)) : []
}

async function lspCodeActionProvider(model, range, monaco) {
  const result = await lspRequest('textDocument/codeAction', {
    textDocument: lspTextDocument(model),
    range: {
      start: { line: range.startLineNumber - 1, character: range.startColumn - 1 },
      end: { line: range.endLineNumber - 1, character: range.endColumn - 1 },
    },
    context: { diagnostics: [] },
  })
  const resolved = await Promise.all((Array.isArray(result) ? result : []).map(async action => {
    if (action.edit) return action
    return await lspRequest('codeAction/resolve', action) || action
  }))
  return {
    actions: resolved.map(action => ({
      title: action.title,
      kind: action.kind || 'quickfix',
      edit: lspWorkspaceEditToMonaco(action.edit, monaco, model),
    })).filter(action => action.edit),
    dispose() {},
  }
}

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
  diagnostics.value = []
  
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

    const res = await fetch(usesPiston.value ? PISTON_EXECUTE_URL : JAVA_RUNNER_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: abortController.signal,
      body: JSON.stringify(usesPiston.value
        ? { language: 'java', version: '15.0.2', files: payloadFiles }
        : { mainClass: selected.value.fqcn, files: payloadFiles })
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
    diagnostics.value = Array.isArray(data.diagnostics) ? data.diagnostics : []
    const formatted = usesPiston.value ? formatPistonOutput(data) : formatLocalOutput(data)
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

async function openDiagnostic(item) {
  const file = workspaceFiles.value.find(candidate => candidate.name === item.file)
  if (file && file.rel !== selected.value?.rel) await select(file)
  setTimeout(() => codeEditorRef.value?.focusLine(item.line), 0)
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
.fname .dirty { margin-left: 6px; font-size: 9px; font-weight: 700; text-transform: uppercase; color: var(--vp-c-brand-1); }
.file-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; padding: 10px; border-top: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft); }
.file-actions button { border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg); color: var(--vp-c-text-1); border-radius: 4px; padding: 5px 6px; font-size: 12px; cursor: pointer; }
.file-actions button:disabled { opacity: 0.45; cursor: not-allowed; }
.main { display: flex; flex-direction: column; min-width: 0; }
.editor-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; padding: 6px 10px; background: var(--vp-c-bg-soft); border: 1px solid var(--vp-c-divider); border-bottom: none; border-radius: 8px 8px 0 0; font-family: var(--vp-font-family-mono); font-size: 12px; }
.file-status { display: flex; align-items: center; min-width: 0; gap: 8px; }
.file-status > span:first-child { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.run-state { flex: none; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; border-radius: 4px; padding: 1px 5px; }
.run-state.runnable { color: #166534; background: rgba(34, 197, 94, 0.14); }
.run-state.support { color: #854d0e; background: rgba(245, 158, 11, 0.16); }
.lsp-state { flex: none; font-size: 10px; font-weight: 700; text-transform: uppercase; border-radius: 4px; padding: 1px 5px; color: var(--vp-c-text-2); background: var(--vp-c-bg); border: 1px solid var(--vp-c-divider); }
.lsp-state.ready { color: #166534; background: rgba(34, 197, 94, 0.14); }
.lsp-state.failed { color: #991b1b; background: rgba(239, 68, 68, 0.14); }
.run-btn { background: var(--vp-c-brand-1); color: white; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.run-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.run-warning { margin: 0 0 8px; padding: 7px 10px; border: 1px solid var(--vp-c-divider); border-top: none; background: var(--vp-c-bg-soft); color: var(--vp-c-text-2); font-size: 12px; line-height: 1.45; }
.diagnostics { margin-top: 12px; border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; }
.diagnostic { display: block; width: 100%; text-align: left; border: 0; border-bottom: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft); color: var(--vp-c-text-1); padding: 7px 10px; font-family: var(--vp-font-family-mono); font-size: 12px; cursor: pointer; }
.diagnostic:last-child { border-bottom: 0; }
.diagnostic:hover { background: var(--vp-c-brand-soft); }
.console { margin-top: 16px; border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; background: #1e1e1e; color: #d4d4d4; }
.console-head { padding: 4px 10px; background: #2d2d2d; font-size: 12px; font-family: var(--vp-font-family-mono); }
.console-out { padding: 12px; margin: 0; font-family: var(--vp-font-family-mono); font-size: 13px; white-space: pre-wrap; word-break: break-all; max-height: 250px; overflow-y: auto; }
.playground-empty { padding: 24px; color: var(--vp-c-text-2); }
@media (max-width: 960px) { .playground { grid-template-columns: 1fr; } }
</style>
