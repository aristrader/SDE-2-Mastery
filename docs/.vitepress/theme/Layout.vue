<template>
  <DefaultTheme.Layout>
    <template #doc-top>
      <div v-if="hasCode" class="mode-tabs">
        <button :class="{ active: mode === 'read' }" @click="setMode('read')">Read</button>
        <button :class="{ active: mode === 'play' }" @click="setMode('play')">⚙ Playground</button>
        <button class="ai-toggle" @click="drawer = !drawer">✦ Ask AI</button>
      </div>
      <div v-if="hasCode && mode === 'play'" class="play-wrap">
        <ClientOnly><Playground /></ClientOnly>
      </div>
    </template>
  </DefaultTheme.Layout>

  <div v-if="drawer" class="ai-drawer">
    <div class="ai-drawer-head">
      <span>✦ Agent — {{ shortPath }}</span>
      <button @click="drawer = false">✕</button>
    </div>
    <ClientOnly><AgentChat :context="drawerContext" /></ClientOnly>
  </div>
</template>

<script setup>
import DefaultTheme from 'vitepress/theme'
import { ref, computed, watch } from 'vue'
import { useData, useRoute } from 'vitepress'
import Playground from './components/Playground.vue'
import AgentChat from './components/AgentChat.vue'
import { analyzeJavaFiles } from './lib/fileDiscovery.mjs'

// Relative glob from this file (theme/) up to repo root, then into the java tree.
const raw = import.meta.glob('../../../src/main/java/org/example/backend_fundamentals/**/*.java', { query: '?raw', import: 'default', eager: true })
const grouped = analyzeJavaFiles(raw)

const { page } = useData()
const route = useRoute()
const mode = ref('read')
const drawer = ref(false)

function pageDir(rel) {
  const i = rel.lastIndexOf('/')
  return i === -1 ? '' : rel.slice(0, i)
}

const hasCode = computed(() => {
  const dir = pageDir(page.value.relativePath)
  if (!dir) return false
  return Object.keys(grouped).some((d) => d.endsWith('/' + dir) || d.endsWith(dir))
})

const shortPath = computed(() => page.value.relativePath.split('/').pop())
const drawerContext = computed(() => `The user is reading ${page.value.relativePath}. Discuss or act on this page's content.`)

// Hiding the prose in play mode is done with a class on <html> (see custom.css),
// because the markdown body lives outside this slot.
function applyBodyClass() {
  if (typeof document === 'undefined') return
  document.documentElement.classList.toggle('pg-play', hasCode.value && mode.value === 'play')
}

function setMode(m) {
  mode.value = m
  if (typeof location !== 'undefined') {
    location.hash = m === 'play' ? 'playground' : ''
  }
  applyBodyClass()
}

function syncFromHash() {
  if (typeof location === 'undefined') return
  mode.value = location.hash === '#playground' && hasCode.value ? 'play' : 'read'
  applyBodyClass()
}

watch(() => route.path, () => { syncFromHash() })
watch(mode, applyBodyClass)
syncFromHash()
</script>

<style scoped>
.mode-tabs { display: flex; gap: 8px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--vp-c-divider); }
.mode-tabs button { padding: 4px 12px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg-soft); color: var(--vp-c-text-1); cursor: pointer; }
.mode-tabs button.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); border-color: var(--vp-c-brand-1); }
.mode-tabs .ai-toggle { margin-left: auto; }
.play-wrap { margin-top: 4px; }
.ai-drawer { position: fixed; top: 0; right: 0; width: 380px; max-width: 90vw; height: 100vh; background: var(--vp-c-bg); border-left: 1px solid var(--vp-c-divider); box-shadow: -4px 0 16px rgba(0,0,0,0.15); z-index: 100; display: flex; flex-direction: column; }
.ai-drawer-head { display: flex; justify-content: space-between; align-items: center; padding: 12px; border-bottom: 1px solid var(--vp-c-divider); font-weight: 700; }
.ai-drawer-head button { border: none; background: transparent; cursor: pointer; font-size: 16px; color: var(--vp-c-text-2); }
.ai-drawer :deep(.agent-chat) { flex: 1; border: none; border-radius: 0; }
</style>
