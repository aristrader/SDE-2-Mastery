<template>
  <div class="playground" v-if="files.length">
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
        <span class="hint">read-only · run it in your IDE</span>
      </div>
      <ClientOnly>
        <CodeEditor v-if="selected" :model-value="selected.content" :readonly="true" />
      </ClientOnly>
    </section>
  </div>
  <div v-else class="playground-empty">No Java files in this folder.</div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useData } from 'vitepress'
import CodeEditor from './CodeEditor.vue'
import { analyzeJavaFiles, mdFolderSet, filesForPage } from '../lib/fileDiscovery.mjs'

// Eager raw glob of the whole java tree (static — no execution, no backend).
const raw = import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.java', { query: '?raw', import: 'default', eager: true })
const grouped = analyzeJavaFiles(raw)
const mdFolders = mdFolderSet(Object.keys(import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.md')))

const { page } = useData()

function pageDir(relativePath) {
  const i = relativePath.lastIndexOf('/')
  return i === -1 ? '' : relativePath.slice(0, i)
}

const files = computed(() => {
  const dir = pageDir(page.value.relativePath)
  if (!dir) return []
  return filesForPage(grouped, mdFolders, dir)
})

const selected = ref(null)
function select(f) { selected.value = f }

watch(files, (list) => { selected.value = list.length ? list[0] : null }, { immediate: true })
</script>

<style scoped>
.playground { display: grid; grid-template-columns: 240px 1fr; gap: 16px; min-height: 60vh; }
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
.editor-head .hint { color: var(--vp-c-text-3); white-space: nowrap; }
.playground-empty { padding: 24px; color: var(--vp-c-text-2); }
@media (max-width: 960px) { .playground { grid-template-columns: 1fr; } }
</style>
