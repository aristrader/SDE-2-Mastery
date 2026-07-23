<template>
  <section v-if="isPracticePage && hasStructuredPractice" class="exercise-workspace structured" aria-label="Practice workspace">
    <div class="workspace-head">
      <div>
        <h2>Practice Workspace</h2>
        <p>Pick one exercise, edit the starter code, run it locally, then reveal the matching solution.</p>
      </div>
    </div>

    <aside class="question-list" aria-label="Practice questions">
      <button
        v-for="question in questions"
        :key="question.id"
        type="button"
        :class="['question-item', selectedQuestion?.id === question.id ? 'active' : '']"
        @click="selectQuestion(question)"
      >
        <span class="question-id">{{ question.id }}</span>
        <span class="question-title">{{ question.title }}</span>
      </button>
    </aside>

    <div :class="['structured-grid', showReference ? 'with-reference' : '']">
      <section class="question-panel">
        <div v-if="selectedQuestion" class="vp-doc question-doc" v-html="selectedQuestion.exerciseHtml"></div>
      </section>

      <section class="practice-code">
        <div class="editor-head">
          <div class="file-status">
            <span>{{ scratchFileName }}</span>
            <span :class="['run-state', scratchRunnable ? 'runnable' : 'support']">
              {{ scratchRunnable ? 'runnable' : 'scratch' }}
            </span>
            <span v-if="javaLspEnabled" :class="['lsp-state', lspStatus]">{{ lspLabel }}</span>
          </div>
          <button class="run-btn" type="button" @click="runScratch" :disabled="!canRunScratch">
            {{ runButtonLabel }}
          </button>
        </div>
        <p class="run-warning">{{ runnerNotice }}</p>
        <ClientOnly>
          <CodeEditor
            ref="scratchEditorRef"
            v-model="scratchCode"
            :readonly="false"
            :diagnostics="visibleDiagnostics"
            :semantic-completion-provider="lspCompletionProvider"
            :semantic-completion-resolver="lspCompletionResolver"
            @run="runScratch"
          />
        </ClientOnly>
        <div class="diagnostics" v-if="visibleDiagnostics.length">
          <button
            v-for="(item, index) in visibleDiagnostics"
            :key="`${item.file}:${item.line}:${index}`"
            type="button"
            class="diagnostic"
            @click="scratchEditorRef?.focusLine(item.line)"
          >
            {{ item.file }}:{{ item.line }} - {{ item.message }}
          </button>
        </div>
        <div class="solution-row">
          <button type="button" class="solution-toggle" @click="showReference = !showReference">
            {{ showReference ? 'Hide Solution' : 'View Solution' }}
          </button>
        </div>
        <div class="console" v-if="output">
          <div class="console-head">{{ outputLabel }}</div>
          <pre class="console-out">{{ output }}</pre>
        </div>
      </section>

      <aside v-if="showReference && selectedQuestion" class="reference-panel structured-reference" aria-label="Selected solution">
        <div class="reference-head">Solution: {{ selectedQuestion.title }}</div>
        <div class="vp-doc reference-doc" v-html="selectedQuestion.solutionHtml"></div>
      </aside>
    </div>
  </section>

  <section v-else-if="isPracticePage && hasPlayground" class="exercise-workspace" aria-label="Practice workspace">
    <div class="workspace-head">
      <div>
        <h2>Code Workspace</h2>
        <p>Edit and run the topic code, then compare against the reference page.</p>
      </div>
      <button v-if="referenceHref" type="button" class="solution-toggle" @click="showReference = !showReference">
        {{ showReference ? 'Hide Reference' : referenceLabel }}
      </button>
    </div>
    <div :class="['workspace-grid', showReference && referenceHref ? 'with-reference' : '']">
      <Playground />
      <aside v-if="showReference && referenceHref" class="reference-panel" :aria-label="referenceLabel">
        <div class="reference-head">{{ referenceLabel }}</div>
        <div v-if="referenceError" class="reference-error">{{ referenceError }}</div>
        <div v-else-if="referenceHtml" class="vp-doc reference-doc" v-html="referenceHtml"></div>
        <div v-else class="reference-loading">Loading reference...</div>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vitepress'
import Playground from './Playground.vue'
import CodeEditor from './CodeEditor.vue'
import navData from '../../navigation_map.json'

const route = useRoute()
const showReference = ref(false)
const referenceHtml = ref('')
const referenceError = ref('')
const selectedQuestion = ref(null)
const scratchCode = ref('')
const scratchEditorRef = ref(null)
const output = ref('')
const outputLabel = ref('Console')
const diagnostics = ref([])
const lspDiagnostics = ref([])
const lspStatus = ref('off')
const lspMessage = ref('')
const isRunning = ref(false)
const isRateLimited = ref(false)
const rateLimitSeconds = ref(0)
let referenceToken = 0
let abortController = null
let rateLimitInterval = null
let runTimeout = null
let runToken = 0
const sessionId = `java-lsp-scratch-${Date.now()}-${Math.random().toString(36).slice(2)}`
const lspPending = new Map()
let lspRequestId = 0
let lspChangeTimer = null
let lspHeartbeatTimer = null
const lspHandlers = []
const RUN_TIMEOUT_MS = 15000
const OUTPUT_LIMIT = 10000
const PISTON_EXECUTE_URL = import.meta.env.VITE_PISTON_EXECUTE_URL || ''
const JAVA_RUNNER_URL = import.meta.env.VITE_JAVA_RUNNER_URL || '/api/run-java'
const usesPiston = computed(() => Boolean(PISTON_EXECUTE_URL))
const javaRunnerEnabled = computed(() => usesPiston.value || import.meta.env.VITE_ENABLE_JAVA_RUNNER === '1')

function normalizeRoutePath(path) {
  let normalized = path.split('#')[0].split('?')[0]
  normalized = normalized.replace(/\/index\.html$/, '/')
  if (!normalized.endsWith('/')) normalized += '/'
  return normalized
}

const currentPath = computed(() => normalizeRoutePath(route.path))
const pageMeta = computed(() => navData.pageMeta?.[currentPath.value] || null)
const modulePath = computed(() => pageMeta.value?.parentLink || currentPath.value.replace(/\/(exercise|solution|design)\/$/, '/'))
const moduleMeta = computed(() => navData.pageMeta?.[modulePath.value] || pageMeta.value)
const capabilities = computed(() => moduleMeta.value?.capabilities || {})

const isPracticePage = computed(() => pageMeta.value?.pageType === 'exercise')
const hasPlayground = computed(() => capabilities.value.playground === true)
const practiceSet = computed(() => pageMeta.value?.practiceSet || null)
const questions = computed(() => practiceSet.value?.questions || [])
const hasStructuredPractice = computed(() => questions.value.length > 0)
const referenceHref = computed(() => {
  if (capabilities.value.solution) return modulePath.value + 'solution/'
  if (capabilities.value.design) return modulePath.value + 'design/'
  return ''
})
const referenceLabel = computed(() => capabilities.value.design ? 'View Design' : 'View Solution')
const javaLspEnabled = computed(() => import.meta.env.DEV && import.meta.env.VITE_ENABLE_JAVA_LSP === '1' && currentPath.value.startsWith('/java/'))
const visibleDiagnostics = computed(() => [...lspDiagnostics.value, ...diagnostics.value])
const lspLabel = computed(() => {
  if (lspStatus.value === 'ready') return 'LSP ready'
  if (lspStatus.value === 'failed') return 'LSP failed'
  if (lspStatus.value === 'starting') return 'LSP starting'
  return 'LSP off'
})
const scratchClassName = computed(() => {
  const match = scratchCode.value.match(/public\s+class\s+([A-Za-z_$][\w$]*)/)
  return match ? match[1] : 'PracticeScratch'
})
const scratchPackage = computed(() => {
  const match = scratchCode.value.match(/package\s+([\w.]+)\s*;/)
  return match ? match[1] : ''
})
const scratchFileName = computed(() => `${scratchClassName.value}.java`)
const scratchMainClass = computed(() => scratchPackage.value ? `${scratchPackage.value}.${scratchClassName.value}` : scratchClassName.value)
const scratchRunnable = computed(() => /public\s+static\s+void\s+main\s*\(/.test(scratchCode.value))
const canRunScratch = computed(() => javaRunnerEnabled.value && scratchCode.value.trim() && scratchRunnable.value && !isRunning.value && !isRateLimited.value)
const runButtonLabel = computed(() => {
  if (isRunning.value) return 'Running...'
  if (isRateLimited.value) return `Wait ${rateLimitSeconds.value}s`
  if (!javaRunnerEnabled.value) return 'Run off'
  if (!scratchCode.value.trim()) return 'No code'
  if (!scratchRunnable.value) return 'No main'
  return 'Run'
})
const runnerNotice = computed(() => {
  if (!javaRunnerEnabled.value) {
    return 'Light mode: Java execution is disabled. Use npm run start:java when you want local runs.'
  }
  if (!usesPiston.value) {
    return 'Local study mode: edited Java is compiled in a temporary directory and run through the local JDK. Do not run untrusted code.'
  }
  return 'Hosted execution uses a remote sandbox. Do not submit proprietary code, API keys, credentials, or PII.'
})

function boundedOutput(text) {
  if (!text) return ''
  if (text.length <= OUTPUT_LIMIT) return text
  return text.slice(0, OUTPUT_LIMIT) + '\n...[Output truncated]...'
}

function formatLocalOutput(data) {
  if (data.phase === 'compile' && !data.ok) {
    return { label: 'Compile Error', text: data.stderr || data.stdout || data.error || 'Compilation failed.' }
  }
  if (!data.ok) {
    return {
      label: data.error && /timed out/i.test(data.error) ? 'Timeout' : 'Runtime Error',
      text: [data.stderr, data.stdout, data.error].filter(Boolean).join('\n') || 'Execution failed.',
    }
  }
  return { label: 'Success', text: data.stdout || data.stderr || 'Process finished with no output.' }
}

function formatPistonOutput(data) {
  const compile = data.compile || {}
  const run = data.run || {}
  const serviceText = run.output || run.stderr || data.message || ''
  if (compile.code && compile.code !== 0) {
    return { label: 'Compile Error', text: compile.stderr || compile.output || data.message || 'Compilation failed.' }
  }
  if (run.code && run.code !== 0) {
    return { label: 'Runtime Error', text: run.stderr || run.output || data.message || 'Execution failed.' }
  }
  return { label: 'Success', text: run.output || run.stdout || compile.output || data.message || 'No output.' }
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

function sendLsp(event, data = {}) {
  if (!javaLspEnabled.value || !import.meta.hot) return
  import.meta.hot.send(event, { sessionId, ...data })
}

function lspBalancedContent(code) {
  const text = code || 'public class PracticeScratch {\n}\n'
  const opens = (text.match(/{/g) || []).length
  const closes = (text.match(/}/g) || []).length
  return closes < opens ? `${text}\n${'}\n'.repeat(opens - closes)}` : text
}

function scratchLspContent() {
  return lspBalancedContent(scratchCode.value)
}

function startLsp() {
  if (!javaLspEnabled.value || !hasStructuredPractice.value) return
  lspStatus.value = 'starting'
  lspDiagnostics.value = []
  sendLsp('java-lsp:start', {
    route: currentPath.value,
    files: [{ name: scratchFileName.value, content: scratchLspContent() }],
  })
  clearInterval(lspHeartbeatTimer)
  lspHeartbeatTimer = setInterval(() => sendLsp('java-lsp:heartbeat'), 5000)
}

function stopLsp() {
  if (!import.meta.env.DEV || import.meta.env.VITE_ENABLE_JAVA_LSP !== '1') return
  sendLsp('java-lsp:stop')
  clearInterval(lspHeartbeatTimer)
  lspHeartbeatTimer = null
  clearTimeout(lspChangeTimer)
  lspPending.forEach(({ reject }) => reject(new Error('Java LSP stopped.')))
  lspPending.clear()
  lspDiagnostics.value = []
  lspStatus.value = 'off'
}

function scheduleLspChange() {
  if (!javaLspEnabled.value || lspStatus.value !== 'ready') return
  clearTimeout(lspChangeTimer)
  lspChangeTimer = setTimeout(() => {
    sendLsp('java-lsp:change', { file: scratchFileName.value, content: scratchLspContent() })
  }, 250)
}

function syncLspModel(model, content = lspBalancedContent(model.getValue())) {
  if (!javaLspEnabled.value || lspStatus.value !== 'ready') return Promise.resolve()
  clearTimeout(lspChangeTimer)
  sendLsp('java-lsp:change', { file: scratchFileName.value, content })
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

function lspPosition(position) {
  return { line: position.lineNumber - 1, character: position.column - 1 }
}

function isMemberAccessPosition(model, position) {
  const word = model.getWordUntilPosition(position)
  const previous = model.getValueInRange({
    startLineNumber: position.lineNumber,
    startColumn: Math.max(1, word.startColumn - 1),
    endLineNumber: position.lineNumber,
    endColumn: word.startColumn,
  })
  return previous === '.'
}

function lspCompletionContent(model, position) {
  const content = model.getValue()
  const word = model.getWordUntilPosition(position)
  if (!isMemberAccessPosition(model, position) || word.word) return lspBalancedContent(content)
  const offset = model.getOffsetAt(position)
  return lspBalancedContent(`${content.slice(0, offset)}toString();${content.slice(offset)}`)
}

function toMonacoRange(range, monaco) {
  return new monaco.Range(
    (range?.start?.line ?? 0) + 1,
    (range?.start?.character ?? 0) + 1,
    (range?.end?.line ?? range?.start?.line ?? 0) + 1,
    (range?.end?.character ?? range?.start?.character ?? 0) + 1,
  )
}

async function lspCompletionProvider(model, position, fallbackRange, monaco) {
  const actualContent = lspBalancedContent(model.getValue())
  const completionContent = lspCompletionContent(model, position)
  await syncLspModel(model, completionContent)
  let result = null
  try {
    result = await lspRequest('textDocument/completion', {
      textDocument: { uri: `file:///${scratchFileName.value}` },
      position: lspPosition(position),
      context: { triggerKind: 1 },
    })
  } finally {
    if (completionContent !== actualContent) {
      setTimeout(() => sendLsp('java-lsp:change', { file: scratchFileName.value, content: actualContent }), 5000)
    }
  }
  const items = Array.isArray(result) ? result : (result?.items || [])
  const forceFallbackRange = completionContent !== actualContent
  const suggestions = pruneNoisyLspItems(items).slice(0, 120).map((item, index) => completionItemToMonaco(item, fallbackRange, monaco, index, forceFallbackRange))
  if (import.meta.env.DEV && forceFallbackRange && typeof window !== 'undefined') {
    window.__javaMemberCompletionLabels = suggestions.map(item => item.label)
  }
  return suggestions
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

function completionItemToMonaco(item, fallbackRange, monaco, index = 0, forceFallbackRange = false) {
  const label = forceFallbackRange ? String(item.label || '').split('(')[0] : item.label
  if (forceFallbackRange) {
    return {
      label,
      kind: monaco.languages.CompletionItemKind.Method,
      detail: item.label,
      insertText: label,
      range: fallbackRange,
      filterText: label,
      sortText: `0-${String(index).padStart(4, '0')}`,
    }
  }
  return {
    label,
    kind: lspCompletionKind(item.kind, monaco),
    detail: forceFallbackRange ? item.label : item.detail,
    documentation: typeof item.documentation === 'string' ? item.documentation : item.documentation?.value,
    insertText: forceFallbackRange ? label : item.textEdit?.newText || item.insertText || item.label,
    range: !forceFallbackRange && item.textEdit?.range ? toMonacoRange(item.textEdit.range, monaco) : fallbackRange,
    additionalTextEdits: forceFallbackRange ? [] : (item.additionalTextEdits || []).map(edit => ({
      range: toMonacoRange(edit.range, monaco),
      text: edit.newText || '',
    })),
    commitCharacters: forceFallbackRange ? undefined : item.commitCharacters,
    filterText: forceFallbackRange ? label : item.filterText,
    sortText: forceFallbackRange ? `0-${String(index).padStart(4, '0')}` : lspSortText(item, index),
    lspItem: item,
  }
}

async function lspCompletionResolver(item, monaco) {
  if (!item?.lspItem) return item
  const resolved = await lspRequest('completionItem/resolve', item.lspItem).catch(() => null)
  return resolved ? { ...item, ...completionItemToMonaco(resolved, item.range, monaco), lspItem: resolved } : item
}

function selectQuestion(question) {
  selectedQuestion.value = question
  scratchCode.value = question?.starterCode || ''
  showReference.value = false
  resetRunState()
  startLsp()
}

function canToggleReference() {
  if (!isPracticePage.value) return false
  if (hasStructuredPractice.value) return Boolean(selectedQuestion.value)
  return Boolean(referenceHref.value)
}

function toggleReferenceShortcut(event) {
  if (!(event.metaKey || event.ctrlKey) || event.key.toLowerCase() !== 's') return
  if (!canToggleReference()) return
  event.preventDefault()
  showReference.value = !showReference.value
}

watch(currentPath, () => {
  showReference.value = false
  referenceHtml.value = ''
  referenceError.value = ''
  selectedQuestion.value = null
  scratchCode.value = ''
  resetRunState()
  stopLsp()
})

watch(questions, (list) => {
  if (list.length) {
    const existing = list.find(question => question.id === selectedQuestion.value?.id)
    selectQuestion(existing || list[0])
  }
}, { immediate: true })

watch(scratchCode, scheduleLspChange)

if (import.meta.env.DEV && import.meta.hot) {
  const onLsp = (event, handler) => {
    import.meta.hot.on(event, handler)
    lspHandlers.push([event, handler])
  }
  onLsp('java-lsp:status', data => {
    if (data.sessionId !== sessionId) return
    lspStatus.value = data.status || 'failed'
    lspMessage.value = data.message || ''
  })
  onLsp('java-lsp:diagnostics', data => {
    if (data.sessionId !== sessionId) return
    lspDiagnostics.value = data.diagnostics || []
  })
  onLsp('java-lsp:response', data => {
    if (data.sessionId !== sessionId) return
    const pending = lspPending.get(data.requestId)
    if (!pending) return
    lspPending.delete(data.requestId)
    data.error ? pending.reject(new Error(data.error)) : pending.resolve(data.result)
  })
}

async function runScratch() {
  if (!javaRunnerEnabled.value) return
  if (isRunning.value || isRateLimited.value) return
  if (!scratchCode.value.trim()) {
    outputLabel.value = 'Input Required'
    output.value = 'Add Java code before running.'
    return
  }
  if (!scratchRunnable.value) {
    outputLabel.value = 'Not Runnable'
    output.value = 'This starter has no public static void main method yet.'
    return
  }

  const token = ++runToken
  isRunning.value = true
  outputLabel.value = 'Running'
  output.value = 'Compiling and running...'
  diagnostics.value = []

  if (abortController) abortController.abort()
  abortController = new AbortController()
  let timedOut = false
  clearRunTimeout()
  runTimeout = setTimeout(() => {
    timedOut = true
    abortController?.abort()
  }, RUN_TIMEOUT_MS)

  try {
    const files = [{ name: scratchFileName.value, content: scratchCode.value }]
    const res = await fetch(usesPiston.value ? PISTON_EXECUTE_URL : JAVA_RUNNER_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: abortController.signal,
      body: JSON.stringify(usesPiston.value
        ? { language: 'java', version: '15.0.2', files }
        : { mainClass: scratchMainClass.value, files })
    })

    if (res.status === 429) {
      if (token !== runToken) return
      const retryAfter = parseInt(res.headers.get('Retry-After') || '10', 10)
      isRateLimited.value = true
      rateLimitSeconds.value = retryAfter
      outputLabel.value = 'Rate Limited'
      output.value = `Rate limited. Please wait ${retryAfter} seconds.`
      rateLimitInterval = setInterval(() => {
        rateLimitSeconds.value--
        if (rateLimitSeconds.value <= 0) clearRateLimit()
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

watch([showReference, referenceHref], async ([open, href]) => {
  if (hasStructuredPractice.value) return
  referenceHtml.value = ''
  referenceError.value = ''
  if (!open || !href) return
  const token = ++referenceToken
  try {
    const html = navData.pageMeta?.[href]?.referenceHtml || ''
    if (!html) throw new Error('Reference content was not found.')
    if (token !== referenceToken) return
    referenceHtml.value = html
  } catch (err) {
    if (token === referenceToken) referenceError.value = err.message || 'Reference failed to load.'
  }
})

onMounted(() => {
  window.addEventListener('keydown', toggleReferenceShortcut)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', toggleReferenceShortcut)
  resetRunState()
  stopLsp()
  if (import.meta.hot?.off) {
    lspHandlers.forEach(([event, handler]) => import.meta.hot.off(event, handler))
    lspHandlers.length = 0
  }
})
</script>

<style scoped>
.exercise-workspace {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--vp-c-divider);
}
.workspace-head {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: 16px;
  margin-bottom: 14px;
}
.workspace-head h2 {
  margin: 0 0 4px;
  padding: 0;
  border: 0;
  font-size: 1.25rem;
}
.workspace-head h2::before {
  display: none;
}
.workspace-head p {
  margin: 0;
  color: var(--vp-c-text-2);
  font-size: 0.92rem;
}
.solution-toggle {
  flex: none;
  min-height: 34px;
  padding: 5px 13px;
  border: 1px solid var(--vp-c-brand-1);
  border-radius: 6px;
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
  font-weight: 700;
  cursor: pointer;
}
.workspace-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
}
.workspace-grid.with-reference {
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
}
.reference-panel {
  height: 76vh;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.reference-head {
  flex: none;
  padding: 8px 12px;
  border-bottom: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  font-weight: 700;
  font-size: 13px;
}
.reference-doc {
  flex: 1;
  overflow: auto;
  padding: 16px 18px 28px;
  max-width: none;
}
.reference-loading,
.reference-error {
  padding: 16px;
  color: var(--vp-c-text-2);
  font-size: 0.92rem;
}
.reference-error {
  color: #b91c1c;
}
.structured-grid {
  display: grid;
  grid-template-columns: minmax(360px, 0.82fr) minmax(520px, 1.18fr);
  gap: 16px;
  align-items: start;
}
.structured-grid.with-reference {
  grid-template-columns: minmax(520px, 1fr) minmax(420px, 0.95fr);
}
.structured-grid.with-reference .question-panel {
  display: none;
}
.question-list {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding: 8px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  overflow-x: auto;
  background: var(--vp-c-bg);
}
.question-item {
  flex: 0 0 220px;
  display: grid;
  gap: 3px;
  padding: 10px 12px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  text-align: left;
  cursor: pointer;
}
.question-item:hover,
.question-item.active {
  background: var(--vp-c-brand-soft);
  border-color: var(--vp-c-brand-1);
}
.question-id {
  color: var(--vp-c-brand-1);
  font-family: var(--vp-font-family-mono);
  font-size: 11px;
  font-weight: 700;
}
.question-title {
  font-size: 13px;
  font-weight: 650;
  line-height: 1.35;
}
.question-panel,
.practice-code,
.structured-reference {
  min-width: 0;
}
.question-doc {
  max-width: none;
  max-height: 72vh;
  overflow: auto;
  padding: 14px 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg);
}
.practice-code {
  display: flex;
  flex-direction: column;
}
.editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 6px 10px;
  background: var(--vp-c-bg-soft);
  border: 1px solid var(--vp-c-divider);
  border-bottom: none;
  border-radius: 8px 8px 0 0;
  font-family: var(--vp-font-family-mono);
  font-size: 12px;
}
.file-status {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 8px;
}
.file-status > span:first-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.run-state {
  flex: none;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  border-radius: 4px;
  padding: 1px 5px;
}
.run-state.runnable {
  color: #166534;
  background: rgba(34, 197, 94, 0.14);
}
.run-state.support {
  color: #854d0e;
  background: rgba(245, 158, 11, 0.16);
}
.lsp-state {
  flex: none;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  border-radius: 4px;
  padding: 1px 5px;
  color: var(--vp-c-text-2);
  background: var(--vp-c-bg);
  border: 1px solid var(--vp-c-divider);
}
.lsp-state.ready {
  color: #166534;
  background: rgba(34, 197, 94, 0.14);
}
.lsp-state.failed {
  color: #991b1b;
  background: rgba(239, 68, 68, 0.14);
}
.run-btn {
  background: var(--vp-c-brand-1);
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
.run-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.run-warning {
  margin: 0 0 8px;
  padding: 7px 10px;
  border: 1px solid var(--vp-c-divider);
  border-top: none;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-2);
  font-size: 12px;
  line-height: 1.45;
}
.solution-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.diagnostics {
  margin-top: 12px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  overflow: hidden;
}
.diagnostic {
  display: block;
  width: 100%;
  text-align: left;
  border: 0;
  border-bottom: 1px solid var(--vp-c-divider);
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  padding: 7px 10px;
  font-family: var(--vp-font-family-mono);
  font-size: 12px;
  cursor: pointer;
}
.diagnostic:last-child {
  border-bottom: 0;
}
.diagnostic:hover {
  background: var(--vp-c-brand-soft);
}
.console {
  margin-top: 12px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  overflow: hidden;
  background: #1e1e1e;
  color: #d4d4d4;
}
.console-head {
  padding: 4px 10px;
  background: #2d2d2d;
  font-size: 12px;
  font-family: var(--vp-font-family-mono);
}
.console-out {
  padding: 12px;
  margin: 0;
  font-family: var(--vp-font-family-mono);
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 250px;
  overflow-y: auto;
}
.structured-reference {
  height: 72vh;
}
@media (max-width: 1100px) {
  .structured-grid {
    grid-template-columns: 1fr;
  }
  .structured-grid.with-reference {
    grid-template-columns: 1fr;
  }
  .structured-reference {
    height: 65vh;
  }
  .question-item {
    flex-basis: 190px;
  }
  .workspace-grid.with-reference {
    grid-template-columns: 1fr;
  }
  .reference-panel {
    height: 65vh;
  }
}
@media (max-width: 640px) {
  .workspace-head {
    display: block;
  }
  .solution-toggle {
    width: 100%;
    margin-top: 12px;
  }
}
</style>
