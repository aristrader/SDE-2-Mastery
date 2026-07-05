<template>
  <nav class="exercise-nav" aria-label="Pagination">
    <div class="nav-links">
      <div class="theory-link">
        <a v-if="!isTheory" :href="modulePath" class="nav-btn">
          <span class="icon">←</span>
          <span class="text">Back to Theory</span>
        </a>
      </div>
      <div class="action-links">
        <a v-if="isTheory && hasExercise" :href="modulePath + 'exercise/'" class="nav-btn primary">
          <span class="text">Practice Exercise</span>
          <span class="icon">→</span>
        </a>
        <a v-if="isExercise && !isSystemDesign" :href="modulePath + 'solution/'" class="nav-btn success">
          <span class="text">View Solution</span>
          <span class="icon">→</span>
        </a>
        <a v-if="isExercise && isSystemDesign" :href="modulePath + 'design/'" class="nav-btn success">
          <span class="text">View Design</span>
          <span class="icon">→</span>
        </a>
      </div>
    </div>
    <div class="next-topic" v-if="nextTopic && (isSolution || isDesign)">
      <div class="next-label">Up Next</div>
      <a :href="nextTopic.link" class="next-link">
        <span class="text">{{ nextTopic.text }}</span>
        <span class="icon">→</span>
      </a>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useData } from 'vitepress'
import navData from '../../navigation_map.json'

const route = useRoute()

// Some basic routing states
const isExercise = computed(() => route.path.endsWith('/exercise/') || route.path.endsWith('/exercise/index.html'))
const isSolution = computed(() => route.path.endsWith('/solution/') || route.path.endsWith('/solution/index.html'))
const isDesign = computed(() => route.path.endsWith('/design/') || route.path.endsWith('/design/index.html'))
const isTheory = computed(() => !isExercise.value && !isSolution.value && !isDesign.value)

const modulePath = computed(() => {
  return route.path.replace(/\/(exercise|solution|design)\/?(index\.html)?$/, '/')
})

const isSystemDesign = computed(() => route.path.includes('/system_design/'))
const hasExercise = computed(() => true) // Assuming all modules have exercise due to our strict validation

const nextTopic = computed(() => {
  // Normalize modulePath to match what generate-homepage outputs
  const link = modulePath.value.replace(/\/index\.html$/, '/')
  const currentIndex = navData.navMap.findIndex(item => item.link === link)
  if (currentIndex !== -1 && currentIndex < navData.navMap.length - 1) {
    return navData.navMap[currentIndex + 1]
  }
  return null
})
</script>

<style scoped>
.exercise-nav {
  margin-top: 3rem;
  border-top: 1px solid var(--vp-c-divider);
  padding-top: 1.5rem;
}
.nav-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}
.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 500;
  text-decoration: none;
  background: var(--vp-c-bg-soft);
  color: var(--vp-c-text-1);
  border: 1px solid var(--vp-c-divider);
  transition: all 0.2s ease;
}
.nav-btn:hover {
  background: var(--vp-c-bg-mute);
  border-color: var(--vp-c-border-hover);
}
.nav-btn.primary {
  background: var(--vp-c-brand-1);
  color: white;
  border-color: var(--vp-c-brand-1);
}
.nav-btn.primary:hover {
  background: var(--vp-c-brand-2);
}
.nav-btn.success {
  background: var(--vp-c-green-1);
  color: white;
  border-color: var(--vp-c-green-1);
}
.nav-btn.success:hover {
  background: var(--vp-c-green-2);
}
.icon {
  font-family: var(--vp-font-family-mono);
}
.next-topic {
  margin-top: 2rem;
  padding: 1rem;
  background: var(--vp-c-bg-soft);
  border-radius: 8px;
  border: 1px solid var(--vp-c-divider);
}
.next-label {
  font-size: 12px;
  text-transform: uppercase;
  font-weight: 700;
  color: var(--vp-c-text-2);
  margin-bottom: 4px;
}
.next-link {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--vp-c-brand-1);
  text-decoration: none;
}
.next-link:hover {
  color: var(--vp-c-brand-2);
}
</style>
