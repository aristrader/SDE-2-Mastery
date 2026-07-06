<template>
  <nav v-if="hasAnyAction" class="study-tabs" aria-label="Study workspace">
    <a :href="modulePath" :class="['study-tab', isReadActive ? 'active' : '']">Read</a>
    <a v-if="hasPlayground" :href="modulePath + '#code'" :class="['study-tab', isCodeActive ? 'active' : '']">Code</a>
    <a v-if="hasExercise" :href="modulePath + 'exercise/'" :class="['study-tab', isExercise ? 'active' : '']">
      {{ exerciseLabel }}
    </a>
    <a v-if="hasSolution" :href="modulePath + 'solution/'" :class="['study-tab', isSolution ? 'active' : '']">Solution</a>
    <a v-if="hasDesign" :href="modulePath + 'design/'" :class="['study-tab', isDesign ? 'active' : '']">Design</a>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vitepress'
import navData from '../../navigation_map.json'

const route = useRoute()

function normalizeRoutePath(path) {
  let normalized = path.split('#')[0].split('?')[0]
  normalized = normalized.replace(/\/index\.html$/, '/')
  if (!normalized.endsWith('/')) normalized += '/'
  return normalized
}

const currentPath = computed(() => normalizeRoutePath(route.path))
const pageMeta = computed(() => navData.pageMeta?.[currentPath.value] || null)
const pageType = computed(() => pageMeta.value?.pageType || 'theory')
const modulePath = computed(() => {
  if (pageMeta.value?.parentLink) return pageMeta.value.parentLink
  return currentPath.value.replace(/\/(exercise|solution|design)\/$/, '/')
})
const moduleMeta = computed(() => navData.pageMeta?.[modulePath.value] || pageMeta.value)

const isExercise = computed(() => pageType.value === 'exercise')
const isSolution = computed(() => pageType.value === 'solution')
const isDesign = computed(() => pageType.value === 'design')
const isTheory = computed(() => !isExercise.value && !isSolution.value && !isDesign.value)

const capabilities = computed(() => moduleMeta.value?.capabilities || {})
const hasExercise = computed(() => capabilities.value.exercise === true)
const hasSolution = computed(() => capabilities.value.solution === true)
const hasDesign = computed(() => capabilities.value.design === true)
const hasPlayground = computed(() => capabilities.value.playground === true)
const exerciseLabel = computed(() => moduleMeta.value?.schema === 'system-design' ? 'Scenario' : 'Practice')
const hasAnyAction = computed(() => {
  if (!moduleMeta.value) return false
  return hasPlayground.value || hasExercise.value || hasSolution.value || hasDesign.value
})
const isCodeActive = computed(() => isTheory.value && route.path.includes('#code'))
const isReadActive = computed(() => isTheory.value && !route.path.includes('#code'))
</script>

<style scoped>
.study-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--vp-c-divider);
}
.study-tab {
  min-height: 34px;
  display: inline-grid;
  place-items: center;
  padding: 5px 13px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 6px;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.2;
  text-decoration: none;
  white-space: nowrap;
}
.study-tab:hover {
  border-color: var(--vp-c-brand-1);
  color: var(--vp-c-brand-1);
}
.study-tab.active {
  background: var(--vp-c-brand-soft);
  color: var(--vp-c-brand-1);
  border-color: var(--vp-c-brand-1);
}
@media (max-width: 520px) {
  .study-tabs {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .study-tab {
    white-space: normal;
  }
}
</style>
