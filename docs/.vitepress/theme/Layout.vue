<template>
  <DefaultTheme.Layout>
    <template #doc-top>
      <div v-if="domainLabel" class="page-eyebrow">{{ domainLabel }}</div>
      <div v-if="hasCode" class="mode-tabs">
        <button :class="{ active: mode === 'read' }" @click="setMode('read')">Read</button>
        <button :class="{ active: mode === 'play' }" @click="setMode('play')">⌗ Code</button>
      </div>
      <ClientOnly><ExerciseNav /></ClientOnly>
      <div v-if="hasCode && mode === 'play'" class="play-wrap">
        <ClientOnly><Playground /></ClientOnly>
      </div>
    </template>

    <template #doc-after>
      <ClientOnly><AutoTopicGrid /></ClientOnly>
    </template>

    <!-- Home: fill the empty hero side with a code-reference visual + a how-it-works strip. -->
    <template #home-hero-image>
      <div class="hero-visual">
        <div class="hv-bar"><span></span><span></span><span></span><em>BillPughSingleton.java</em></div>
        <div class="hv-body">
          <pre class="hv-code"><span class="kw">public class</span> BillPughSingleton {
  <span class="kw">private static class</span> Holder {
    <span class="kw">static final</span> var I = <span class="kw">new</span> BillPughSingleton();
  }
}</pre>
          <div class="hv-cap">design_patterns · creational · singleton</div>
        </div>
      </div>
    </template>

    <template #home-features-after>
      <div id="curriculum" class="home-curriculum">
        <h2 class="home-curriculum-title">Curriculum</h2>
        <ClientOnly><AutoTopicGrid root /></ClientOnly>
      </div>
      <div class="how">
        <h2 class="how-title">How it works</h2>
        <div class="how-grid">
          <div class="how-step"><div class="how-n">1</div><h3>Read</h3><p>Theory, diagrams and tables for each topic — clean and scannable.</p></div>
          <div class="how-step"><div class="how-n">2</div><h3>Browse the code</h3><p>See each topic's Java implementation right next to the theory, in the Code tab.</p></div>
          <div class="how-step"><div class="how-n">3</div><h3>Run locally</h3><p>Use the Code tab in local study mode to compile and run Java examples through your local JDK.</p></div>
        </div>
      </div>
    </template>
  </DefaultTheme.Layout>
</template>

<script setup>
import DefaultTheme from 'vitepress/theme'
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useData } from 'vitepress'
import mediumZoom from 'medium-zoom'
import Playground from './components/Playground.vue'
import ExerciseNav from './components/ExerciseNav.vue'
import AutoTopicGrid from './components/AutoTopicGrid.vue'
import { analyzeJavaFiles, mdFolderSet, pageHasCode } from './lib/fileDiscovery.mjs'

// Relative globs from this file (theme/) up to repo root, then into the java tree.
const raw = import.meta.glob('../../../src/main/java/org/example/backend_fundamentals/**/*.java', { query: '?raw', import: 'default' })
const grouped = analyzeJavaFiles(raw)
// md folders only need their paths (keys), not content — keep this non-eager.
const mdFolders = mdFolderSet(Object.keys(import.meta.glob('../../../src/main/java/org/example/backend_fundamentals/**/*.md')))

const { page } = useData()
const mode = ref('read')

function pageDir(rel) {
  const i = rel.lastIndexOf('/')
  return i === -1 ? '' : rel.slice(0, i)
}

const hasCode = computed(() => {
  const dir = pageDir(page.value.relativePath)
  if (!dir) return false
  return pageHasCode(grouped, mdFolders, dir)
})

// Colored domain kicker above the page title — orientation + a structural splash of colour.
const DOMAINS = {
  design_patterns: 'Design Patterns', java: 'Java & JVM', spring: 'Spring',
  spring_boot: 'Spring', system_design: 'System Design', networking: 'Networking',
  databases: 'Databases', todo: 'Study Plan',
}
const domainLabel = computed(() => {
  const rel = page.value.relativePath
  if (!rel || rel === 'index.md') return ''
  return DOMAINS[rel.split('/')[0]] || ''
})

// Hiding the prose in code mode is done with a class on <html> (see custom.css),
// because the markdown body lives outside this slot.
function applyBodyClass() {
  if (typeof document === 'undefined') return
  document.documentElement.classList.toggle('pg-play', hasCode.value && mode.value === 'play')
}

function setMode(m) {
  mode.value = m
  if (typeof location !== 'undefined') location.hash = m === 'play' ? 'code' : ''
  applyBodyClass()
}

function syncFromHash() {
  if (typeof location === 'undefined') return
  mode.value = location.hash === '#code' && hasCode.value ? 'play' : 'read'
  applyBodyClass()
}

// Click-to-zoom for diagrams and images (helpful for dense system-design mermaid).
let zoom = null
function applyZoom() {
  if (typeof document === 'undefined') return
  nextTick(() => {
    const targets = document.querySelectorAll('.vp-doc img, .vp-doc .mermaid svg')
    if (!zoom) zoom = mediumZoom(targets, { background: 'var(--vp-c-bg)', margin: 24 })
    else { zoom.detach(); zoom.attach(targets) }
  })
}

const stopZoomWatch = watch([() => page.value.relativePath, hasCode], () => {
  syncFromHash()
  applyZoom()
})
watch(mode, () => {
  applyBodyClass()
})
onMounted(() => {
  applyZoom()
  if (typeof window !== 'undefined') window.addEventListener('hashchange', syncFromHash)
})
onBeforeUnmount(() => {
  if (zoom) { zoom.detach(); zoom = null }
  stopZoomWatch()
  if (typeof window !== 'undefined') window.removeEventListener('hashchange', syncFromHash)
})
syncFromHash()
</script>

<style scoped>
.page-eyebrow { font-family: var(--vp-font-family-mono); font-size: 12px; font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase; color: var(--vp-c-brand-1); margin-bottom: 4px; }
.mode-tabs { display: flex; gap: 8px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--vp-c-divider); }
.mode-tabs button { padding: 4px 12px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg-soft); color: var(--vp-c-text-1); cursor: pointer; }
.mode-tabs button.active { background: var(--vp-c-brand-soft); color: var(--vp-c-brand-1); border-color: var(--vp-c-brand-1); }
.play-wrap { margin-top: 4px; }
.home-curriculum { max-width: 1152px; margin: 10px auto 0; padding: 8px 24px 0; }
.home-curriculum-title { font-size: 1.4rem; font-weight: 700; margin: 0 0 16px; border: none; padding: 0; }

/* Home hero visual — a code-reference card that fills the otherwise-empty hero side */
.hero-visual { width: 100%; max-width: 420px; border-radius: 14px; overflow: hidden; border: 1px solid var(--vp-c-divider); box-shadow: 0 18px 50px rgba(37, 99, 235, 0.18); background: #0f1117; }
.hv-bar { display: flex; align-items: center; gap: 7px; padding: 10px 14px; background: #1a1d27; border-bottom: 1px solid #2a2e3a; }
.hv-bar span { width: 11px; height: 11px; border-radius: 50%; background: #3a3f4d; }
.hv-bar span:nth-child(1) { background: #ef5f56; } .hv-bar span:nth-child(2) { background: #f6bd3b; } .hv-bar span:nth-child(3) { background: #5fce6a; }
.hv-bar em { margin-left: 8px; color: #8b90a0; font-style: normal; font-family: var(--vp-font-family-mono); font-size: 12px; }
.hv-body { padding: 16px 18px; }
.hv-code { margin: 0; color: #d4d7e0; font-family: var(--vp-font-family-mono); font-size: 13px; line-height: 1.6; white-space: pre; overflow-x: auto; }
.hv-code .kw { color: #7aa2ff; }
.hv-cap { margin-top: 14px; color: #8b90a0; font-family: var(--vp-font-family-mono); font-size: 11px; letter-spacing: 0.04em; }

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
