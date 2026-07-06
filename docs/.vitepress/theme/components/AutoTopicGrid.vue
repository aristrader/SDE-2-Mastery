<template>
  <section v-if="items.length" class="auto-topic-grid" aria-label="Available topics">
    <a v-for="item in items" :key="item.link" class="topic-card" :href="item.link">
      <span class="topic-title">{{ item.text }}</span>
      <span v-if="item.items?.length" class="topic-meta">{{ item.items.length }} subtopics</span>
      <span v-else-if="item.capabilities?.playground" class="topic-meta">Code + practice</span>
      <span v-else-if="item.capabilities?.exercise" class="topic-meta">Practice</span>
    </a>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vitepress'
import navData from '../../navigation_map.json'

const props = defineProps({
  root: { type: Boolean, default: false },
})

const route = useRoute()

function normalizePath(path) {
  let normalized = path.split('#')[0].split('?')[0].replace(/\/index\.html$/, '/')
  if (!normalized.endsWith('/')) normalized += '/'
  return normalized
}

function findNode(items, link) {
  for (const item of items || []) {
    if (item.link === link) return item
    const found = findNode(item.items, link)
    if (found) return found
  }
  return null
}

const items = computed(() => {
  const rootItems = navData.tree?.items || navData.nav || []
  if (props.root) return rootItems
  const node = findNode(rootItems, normalizePath(route.path))
  return node?.items || []
})
</script>

<style scoped>
.auto-topic-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 12px;
  margin: 1.25rem 0 0;
}

.topic-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 84px;
  padding: 14px 16px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  text-decoration: none;
  transition: border-color 0.16s ease, background 0.16s ease;
}

.topic-card:hover {
  border-color: var(--vp-c-brand-1);
  background: var(--vp-c-brand-soft);
}

.topic-title {
  font-weight: 700;
  line-height: 1.35;
}

.topic-meta {
  color: var(--vp-c-text-2);
  font-size: 13px;
}
</style>
