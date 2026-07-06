<template>
  <section v-if="isPracticePage && hasPlayground" class="exercise-workspace" aria-label="Practice workspace">
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
      <iframe
        v-if="showReference && referenceHref"
        class="reference-frame"
        :src="referenceHref"
        :title="referenceLabel"
      />
    </div>
  </section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vitepress'
import Playground from './Playground.vue'
import navData from '../../navigation_map.json'

const route = useRoute()
const showReference = ref(false)

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
const referenceHref = computed(() => {
  if (capabilities.value.solution) return modulePath.value + 'solution/'
  if (capabilities.value.design) return modulePath.value + 'design/'
  return ''
})
const referenceLabel = computed(() => capabilities.value.design ? 'View Design' : 'View Solution')

watch(currentPath, () => {
  showReference.value = false
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
.reference-frame {
  width: 100%;
  height: 76vh;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg);
}
@media (max-width: 1100px) {
  .workspace-grid.with-reference {
    grid-template-columns: 1fr;
  }
  .reference-frame {
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
