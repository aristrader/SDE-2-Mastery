<template>
  <div class="playground" v-if="files.length">
    <aside class="files">
      <div class="files-head">Files</div>
      <div
        v-for="f in files" :key="f.path"
        :class="['file', selected && selected.path === f.path ? 'active' : '']"
        @click="select(f)"
      >
        <span class="fname">{{ f.name }}</span>
        <button v-if="f.runnable" class="run" :disabled="running" @click.stop="run(f)" title="Compile & run this class">▶ Run</button>
      </div>
    </aside>

    <section class="main">
      <div class="editor-head">
        <span>{{ selected ? selected.name : '' }}</span>
        <span class="actions">
          <button :disabled="!dirty || saving" @click="save">{{ saving ? 'Saving…' : (dirty ? 'Save' : 'Saved') }}</button>
          <button v-if="selected && selected.runnable" :disabled="running" @click="run(selected)">
            {{ running ? 'Compiling…' : 'Run' }}
          </button>
        </span>
      </div>

      <ClientOnly>
        <CodeEditor v-if="selected" v-model="buffer" />
      </ClientOnly>

      <div v-if="output || runError" class="console">
        <div class="console-head">Output</div>
        <pre v-if="output" class="stdout">{{ output }}</pre>
        <pre v-if="runError" class="stderr">{{ runError }}</pre>
      </div>
      <p class="agent-hint">Use the ✦ Ask AI button (bottom-right) to discuss or edit this file with the agent.</p>
    </section>
  </div>
  <div v-else class="playground-empty">No Java files in this folder.</div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useData } from 'vitepress'
import CodeEditor from './CodeEditor.vue'
import { analyzeJavaFiles, filesForPage } from '../lib/fileDiscovery.mjs'

// Relative glob from this file (components/) up to repo root, then into the java tree.
const raw = import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.java', { query: '?raw', import: 'default', eager: true })
const grouped = analyzeJavaFiles(raw)

const { page } = useData()

function pageDir(relativePath) {
  const i = relativePath.lastIndexOf('/')
  return i === -1 ? '' : relativePath.slice(0, i)
}

const files = computed(() => {
  const dir = pageDir(page.value.relativePath)
  if (!dir) return []
  return filesForPage(grouped, page.value.relativePath, (d) => d.endsWith('/' + dir) || d.endsWith(dir))
})

const selected = ref(null)
const buffer = ref('')
const original = ref('')
const saving = ref(false)
const running = ref(false)
const output = ref('')
const runError = ref('')

const dirty = computed(() => !!selected.value && buffer.value !== original.value)

function relFromGlobPath(f) {
  const marker = 'backend_fundamentals/'
  const idx = f.path.indexOf(marker)
  return idx === -1 ? f.name : f.path.slice(idx + marker.length)
}

function select(f) {
  selected.value = f
  buffer.value = f.content
  original.value = f.content
  output.value = ''
  runError.value = ''
}

// auto-select first file when the folder changes
watch(files, (list) => {
  if (list.length) select(list[0])
  else selected.value = null
}, { immediate: true })

async function save() {
  if (!selected.value) return
  saving.value = true
  try {
    const rel = relFromGlobPath(selected.value)
    const res = await fetch('/api/save', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ relativePath: rel, content: buffer.value }),
    })
    if (!res.ok) throw new Error((await res.json()).error || 'save failed')
    original.value = buffer.value
  } catch (e) {
    runError.value = 'Save error: ' + e.message
  } finally {
    saving.value = false
  }
}

async function run(f) {
  running.value = true
  output.value = ''
  runError.value = ''
  try {
    const res = await fetch('/api/run', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ fqcn: f.fqcn }),
    })
    const data = await res.json()
    output.value = data.stdout || (data.error ? '' : 'Process finished (no output).')
    if (data.stderr) runError.value += data.stderr
    if (data.error) runError.value += '\n' + data.error
  } catch (e) {
    runError.value = 'Run error: ' + e.message + '\nIs the backend running? `npm run dev`'
  } finally {
    running.value = false
  }
}
</script>

<style scoped>
.playground { display: grid; grid-template-columns: 220px 1fr; gap: 16px; min-height: 60vh; }
.files { border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; height: fit-content; }
.files-head, .console-head { padding: 8px 12px; font-weight: 700; border-bottom: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft); }
.file { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 8px 10px; cursor: pointer; font-family: var(--vp-font-family-mono); font-size: 13px; }
.file:hover { background: var(--vp-c-bg-soft); }
.file.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); }
.fname { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file .run { flex: none; border: 1px solid var(--vp-c-brand-1); background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); cursor: pointer; font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 5px; line-height: 1; }
.file .run:hover:not(:disabled) { background: var(--vp-c-brand-1); color: #fff; }
.file .run:disabled { opacity: 0.5; cursor: not-allowed; }
.main { display: flex; flex-direction: column; min-width: 0; }
.editor-head { display: flex; justify-content: space-between; align-items: center; padding: 6px 10px; background: var(--vp-c-bg-soft); border: 1px solid var(--vp-c-divider); border-bottom: none; border-radius: 8px 8px 0 0; font-family: var(--vp-font-family-mono); }
.editor-head button { margin-left: 8px; padding: 4px 10px; border: none; border-radius: 6px; background: var(--vp-c-brand-1); color: #fff; cursor: pointer; }
.editor-head button:disabled { opacity: 0.5; cursor: not-allowed; }
.console { margin-top: 10px; border-radius: 8px; overflow: hidden; background: #000; }
.stdout { color: #a7f3d0; padding: 10px; margin: 0; overflow-x: auto; font-family: var(--vp-font-family-mono); font-size: 13px; }
.stderr { color: #fca5a5; padding: 10px; margin: 0; overflow-x: auto; font-family: var(--vp-font-family-mono); font-size: 13px; }
.agent-hint { margin: 10px 2px 0; font-size: 12px; color: var(--vp-c-text-3); }
.playground-empty { padding: 24px; color: var(--vp-c-text-2); }
@media (max-width: 960px) { .playground { grid-template-columns: 1fr; } }
</style>
