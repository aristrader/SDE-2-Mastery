<template>
  <DefaultTheme.Layout>
    <template #doc-top>
      <div v-if="hasCode" class="mode-tabs">
        <button :class="{ active: mode === 'read' }" @click="setMode('read')">Read</button>
        <button :class="{ active: mode === 'play' }" @click="setMode('play')">⚙ Playground</button>
      </div>
      <div v-if="hasCode && mode === 'play'" class="play-wrap">
        <ClientOnly><Playground /></ClientOnly>
      </div>
    </template>

    <!-- Home: fill the empty hero side with a mini-playground visual + a how-it-works strip. -->
    <template #home-hero-image>
      <div class="hero-visual">
        <div class="hv-bar"><span></span><span></span><span></span><em>Singleton.java</em></div>
        <div class="hv-body">
          <pre class="hv-code"><span class="kw">public class</span> BillPughSingleton {
  <span class="kw">private static class</span> Holder {
    <span class="kw">static final</span> var I = <span class="kw">new</span> BillPughSingleton();
  }
}</pre>
          <div class="hv-run"><span class="hv-btn">▶ Run</span><span class="hv-out">▸ instance ok</span></div>
        </div>
      </div>
    </template>

    <template #home-features-after>
      <div class="how">
        <h2 class="how-title">How it works</h2>
        <div class="how-grid">
          <div class="how-step"><div class="how-n">1</div><h3>Read</h3><p>Theory, diagrams and tables for each topic — clean and scannable.</p></div>
          <div class="how-step"><div class="how-n">2</div><h3>Run</h3><p>Open the Playground on any code page, edit a class, and run it locally.</p></div>
          <div class="how-step"><div class="how-n">3</div><h3>Ask AI</h3><p>Discuss the page or get quizzed — the agent is one click away, on every page.</p></div>
        </div>
      </div>
    </template>
  </DefaultTheme.Layout>

  <!-- Available on every page so you can discuss the current page with the agent. -->
  <button v-if="!drawer" class="ai-fab" @click="drawer = true" title="Ask the AI about this page">✦ Ask AI</button>

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
import { useData } from 'vitepress'
import Playground from './components/Playground.vue'
import AgentChat from './components/AgentChat.vue'
import { analyzeJavaFiles, mdFolderSet, pageHasCode } from './lib/fileDiscovery.mjs'

// Relative globs from this file (theme/) up to repo root, then into the java tree.
const raw = import.meta.glob('../../../src/main/java/org/example/backend_fundamentals/**/*.java', { query: '?raw', import: 'default', eager: true })
const grouped = analyzeJavaFiles(raw)
// md folders only need their paths (keys), not content — keep this non-eager.
const mdFolders = mdFolderSet(Object.keys(import.meta.glob('../../../src/main/java/org/example/backend_fundamentals/**/*.md')))

const { page } = useData()
const mode = ref('read')
const drawer = ref(false)

function pageDir(rel) {
  const i = rel.lastIndexOf('/')
  return i === -1 ? '' : rel.slice(0, i)
}

const hasCode = computed(() => {
  const dir = pageDir(page.value.relativePath)
  if (!dir) return false
  return pageHasCode(grouped, mdFolders, dir)
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

// React to route AND hasCode (a computed that may settle a tick after the route),
// so play-mode/body-class never reflects the previous page.
watch([() => page.value.relativePath, hasCode], () => syncFromHash())
watch(mode, applyBodyClass)
syncFromHash()
</script>

<style scoped>
.mode-tabs { display: flex; gap: 8px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--vp-c-divider); }
.mode-tabs button { padding: 4px 12px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg-soft); color: var(--vp-c-text-1); cursor: pointer; }
.mode-tabs button.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); border-color: var(--vp-c-brand-1); }
.play-wrap { margin-top: 4px; }
.ai-fab { position: fixed; right: 20px; bottom: 20px; z-index: 90; padding: 10px 16px; border: none; border-radius: 999px; background: var(--vp-c-brand-1); color: #fff; font-weight: 700; cursor: pointer; box-shadow: 0 4px 14px rgba(0,0,0,0.25); }
.ai-fab:hover { background: var(--vp-c-brand-2); }
.ai-drawer { position: fixed; top: 0; right: 0; width: 380px; max-width: 90vw; height: 100vh; background: var(--vp-c-bg); border-left: 1px solid var(--vp-c-divider); box-shadow: -4px 0 16px rgba(0,0,0,0.15); z-index: 100; display: flex; flex-direction: column; }
.ai-drawer-head { display: flex; justify-content: space-between; align-items: center; padding: 12px; border-bottom: 1px solid var(--vp-c-divider); font-weight: 700; }
.ai-drawer-head button { border: none; background: transparent; cursor: pointer; font-size: 16px; color: var(--vp-c-text-2); }
.ai-drawer :deep(.agent-chat) { flex: 1; border: none; border-radius: 0; }

/* Home hero visual — a mini playground that fills the otherwise-empty hero side */
.hero-visual { width: 100%; max-width: 420px; border-radius: 14px; overflow: hidden; border: 1px solid var(--vp-c-divider); box-shadow: 0 18px 50px rgba(67, 56, 202, 0.18); background: #0f1117; }
.hv-bar { display: flex; align-items: center; gap: 7px; padding: 10px 14px; background: #1a1d27; border-bottom: 1px solid #2a2e3a; }
.hv-bar span { width: 11px; height: 11px; border-radius: 50%; background: #3a3f4d; }
.hv-bar span:nth-child(1) { background: #ef5f56; } .hv-bar span:nth-child(2) { background: #f6bd3b; } .hv-bar span:nth-child(3) { background: #5fce6a; }
.hv-bar em { margin-left: 8px; color: #8b90a0; font-style: normal; font-family: var(--vp-font-family-mono); font-size: 12px; }
.hv-body { padding: 16px 18px; }
.hv-code { margin: 0; color: #d4d7e0; font-family: var(--vp-font-family-mono); font-size: 13px; line-height: 1.6; white-space: pre; overflow-x: auto; }
.hv-code .kw { color: #5cc6b4; }
.hv-run { display: flex; align-items: center; gap: 14px; margin-top: 16px; }
.hv-btn { background: var(--vp-c-brand-3); color: #fff; font-weight: 700; font-size: 12px; padding: 5px 12px; border-radius: 6px; }
.hv-out { color: #5fce6a; font-family: var(--vp-font-family-mono); font-size: 12px; }

/* "How it works" strip below the feature cards */
.how { max-width: 1152px; margin: 8px auto 0; padding: 16px 24px 8px; }
.how-title { font-size: 1.4rem; font-weight: 700; margin-bottom: 20px; border: none; padding: 0; }
.how-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.how-step { padding: 20px; border: 1px solid var(--vp-c-divider); border-radius: 12px; background: var(--vp-c-bg-soft); }
.how-step .how-n { width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; background: var(--vp-c-brand-3); margin-bottom: 12px; }
.how-step h3 { margin: 0 0 6px; font-size: 1.05rem; }
.how-step p { margin: 0; color: var(--vp-c-text-2); font-size: 0.9rem; line-height: 1.6; }
@media (max-width: 880px) { .how-grid { grid-template-columns: 1fr; } }
</style>
