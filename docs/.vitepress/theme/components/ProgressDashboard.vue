<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  data: Object
})

const viewMode = ref('sprint') // 'sprint' or 'journey'

// --- 9-Month Journey Calcs ---
const overallPercentage = computed(() => props.data.global.percentage)
const overallOffset = computed(() => 283 - (283 * overallPercentage.value) / 100)

// --- 3-Month Sprint Calcs (🔴 MUST only) ---
const sprintPercentage = computed(() => {
  const red = props.data.global.tiers.red;
  return red.total > 0 ? Math.round((red.done / red.total) * 100) : 0;
})
const sprintOffset = computed(() => 283 - (283 * sprintPercentage.value) / 100)

// --- Sprint Timeline Calcs ---
const sprintStart = new Date('2026-05-18T00:00:00')
const sprintLengthDays = 12 * 7 // 84 days
const sprintEnd = new Date(sprintStart.getTime() + sprintLengthDays * 24 * 60 * 60 * 1000)

const today = new Date()
const elapsedDays = Math.max(0, Math.min(sprintLengthDays, Math.floor((today - sprintStart) / (1000 * 60 * 60 * 24))))
const expectedPercentage = Math.round((elapsedDays / sprintLengthDays) * 100)

const sprintStatus = computed(() => {
  const diff = sprintPercentage.value - expectedPercentage
  if (diff >= 5) return { text: 'Ahead of Schedule', color: '#10b981' } // Green
  if (diff >= -5) return { text: 'On Track', color: '#3b82f6' } // Blue
  return { text: 'Behind Schedule', color: '#ef4444' } // Red
})

function getCircleColor(percentage) {
  if (percentage >= 100) return '#10b981'; // green
  if (percentage >= 50) return '#3b82f6'; // blue
  if (percentage > 0) return '#f59e0b'; // amber
  return '#e5e7eb'; // gray
}
</script>

<template>
  <div class="progress-dashboard">
    
    <!-- View Toggle -->
    <div class="toggle-container">
      <div class="toggle-switch">
        <button 
          :class="['toggle-btn', { active: viewMode === 'sprint' }]" 
          @click="viewMode = 'sprint'"
        >
          ⏱️ Sprint Focus (First 3 Months)
        </button>
        <button 
          :class="['toggle-btn', { active: viewMode === 'journey' }]" 
          @click="viewMode = 'journey'"
        >
          🏔️ Full Journey (9 Months)
        </button>
      </div>
    </div>

    <!-- SPRINT VIEW -->
    <div v-if="viewMode === 'sprint'" class="view-section fade-in">
      
      <!-- Sprint Timeline Tracker -->
      <div class="timeline-card">
        <div class="timeline-header">
          <h3>Sprint 1 Timeline Tracker</h3>
          <span class="status-badge" :style="{ backgroundColor: sprintStatus.color }">{{ sprintStatus.text }}</span>
        </div>
        
        <div class="timeline-bar-wrapper">
          <div class="timeline-labels">
            <span>Start: May 18</span>
            <span>Day {{ elapsedDays }} / 84</span>
            <span>End: Aug 10</span>
          </div>
          <div class="progress-bar-bg timeline-bg">
            <div class="progress-bar-fill bg-blue" :style="{ width: expectedPercentage + '%' }"></div>
          </div>
          <div class="expected-text">Expected Progress by Today: <strong>{{ expectedPercentage }}%</strong></div>
        </div>
      </div>

      <div class="phase-card sprint">
        <div class="card-header">
          <h2>Sprint Report Card</h2>
          <span class="subtitle">Core Fundamentals (🔴 MUST topics only)</span>
        </div>
        <div class="card-body">
          <div class="circle-container">
            <svg class="circular-chart" viewBox="0 0 100 100">
              <path class="circle-bg" d="M50 5 a 45 45 0 0 1 0 90 a 45 45 0 0 1 0 -90" />
              <path class="circle stroke-red" :stroke-dasharray="283" :stroke-dashoffset="sprintOffset" d="M50 5 a 45 45 0 0 1 0 90 a 45 45 0 0 1 0 -90" />
              <text x="50" y="50" class="percentage" dominant-baseline="middle" text-anchor="middle">{{ sprintPercentage }}%</text>
            </svg>
          </div>
          <div class="stats-text">
            <div class="stat-item"><span class="label">Target Sprint Topics:</span><span class="value">{{ data.global.tiers.red.total }}</span></div>
            <div class="stat-item"><span class="label">Completed:</span><span class="value text-red">{{ data.global.tiers.red.done }}</span></div>
            <div class="stat-item"><span class="label">Remaining:</span><span class="value">{{ data.global.tiers.red.total - data.global.tiers.red.done }}</span></div>
          </div>
        </div>
      </div>

      <h3 class="section-title">Sprint Progress by Part (🔴 MUST only)</h3>
      <div class="parts-grid">
        <div v-for="part in data.parts" :key="'sprint-'+part.id" class="part-card" v-show="part.tiers.red.total > 0">
          <h3>{{ part.title }}</h3>
          <div class="part-progress-wrapper">
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" :style="{ width: (part.tiers.red.done / part.tiers.red.total * 100) + '%', backgroundColor: getCircleColor((part.tiers.red.done / part.tiers.red.total * 100)) }"></div>
            </div>
            <span class="part-percentage">{{ Math.round((part.tiers.red.done / part.tiers.red.total) * 100) }}%</span>
          </div>
          <div class="part-details">
            <span>{{ part.tiers.red.done }} / {{ part.tiers.red.total }} Core Topics</span>
          </div>
        </div>
      </div>
    </div>

    <!-- JOURNEY VIEW -->
    <div v-if="viewMode === 'journey'" class="view-section fade-in">
      <div class="phase-card journey">
        <div class="card-header">
          <h2>9-Month Journey</h2>
          <span class="subtitle">Complete Syllabus (All Tiers)</span>
        </div>
        <div class="card-body">
          <div class="circle-container">
            <svg class="circular-chart" viewBox="0 0 100 100">
              <path class="circle-bg" d="M50 5 a 45 45 0 0 1 0 90 a 45 45 0 0 1 0 -90" />
              <path class="circle stroke-brand" :stroke-dasharray="283" :stroke-dashoffset="overallOffset" d="M50 5 a 45 45 0 0 1 0 90 a 45 45 0 0 1 0 -90" />
              <text x="50" y="50" class="percentage" dominant-baseline="middle" text-anchor="middle">{{ overallPercentage }}%</text>
            </svg>
          </div>
          <div class="stats-text">
            <div class="stat-item"><span class="label">Total Topics:</span><span class="value">{{ data.global.totalTopics }}</span></div>
            <div class="stat-item"><span class="label">Completed:</span><span class="value text-green">{{ data.global.completedTopics }}</span></div>
            <div class="stat-item"><span class="label">Remaining:</span><span class="value">{{ data.global.totalTopics - data.global.completedTopics }}</span></div>
          </div>
        </div>
      </div>

      <!-- Tier Breakdown Bar -->
      <div class="tier-breakdown-card">
        <h3>Tier Breakdown</h3>
        <div class="tier-stats-row">
          <div class="tier-item" v-if="data.global.tiers.red.total > 0">
            <span class="tier-label">🔴 MUST (Core)</span>
            <div class="tier-progress">
              <div class="tier-bar-bg"><div class="tier-bar-fill bg-red" :style="{ width: (data.global.tiers.red.done / data.global.tiers.red.total) * 100 + '%' }"></div></div>
              <span class="tier-text">{{ data.global.tiers.red.done }} / {{ data.global.tiers.red.total }}</span>
            </div>
          </div>
          <div class="tier-item" v-if="data.global.tiers.orange.total > 0">
            <span class="tier-label">🟠 HIGH (Conversant)</span>
            <div class="tier-progress">
              <div class="tier-bar-bg"><div class="tier-bar-fill bg-orange" :style="{ width: (data.global.tiers.orange.done / data.global.tiers.orange.total) * 100 + '%' }"></div></div>
              <span class="tier-text">{{ data.global.tiers.orange.done }} / {{ data.global.tiers.orange.total }}</span>
            </div>
          </div>
          <div class="tier-item" v-if="data.global.tiers.yellow.total > 0">
            <span class="tier-label">🟡 MEDIUM</span>
            <div class="tier-progress">
              <div class="tier-bar-bg"><div class="tier-bar-fill bg-yellow" :style="{ width: (data.global.tiers.yellow.done / data.global.tiers.yellow.total) * 100 + '%' }"></div></div>
              <span class="tier-text">{{ data.global.tiers.yellow.done }} / {{ data.global.tiers.yellow.total }}</span>
            </div>
          </div>
          <div class="tier-item" v-if="data.global.tiers.green.total > 0">
            <span class="tier-label">🟢 LOW</span>
            <div class="tier-progress">
              <div class="tier-bar-bg"><div class="tier-bar-fill bg-green" :style="{ width: (data.global.tiers.green.done / data.global.tiers.green.total) * 100 + '%' }"></div></div>
              <span class="tier-text">{{ data.global.tiers.green.done }} / {{ data.global.tiers.green.total }}</span>
            </div>
          </div>
        </div>
      </div>

      <h3 class="section-title">Overall Progress by Part</h3>
      <div class="parts-grid">
        <div v-for="part in data.parts" :key="'journey-'+part.id" class="part-card">
          <h3>{{ part.title }}</h3>
          <div class="part-progress-wrapper">
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" :style="{ width: part.percentage + '%', backgroundColor: getCircleColor(part.percentage) }"></div>
            </div>
            <span class="part-percentage">{{ part.percentage }}%</span>
          </div>
          <div class="part-details">
            <span>{{ part.completedTopics }} / {{ part.totalTopics }} total topics</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.progress-dashboard {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  margin-top: 1rem;
}

/* Toggle Switch */
.toggle-container {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.toggle-switch {
  display: flex;
  background: var(--vp-c-bg-soft);
  border-radius: 20px;
  padding: 4px;
  border: 1px solid var(--vp-c-divider);
}

.toggle-btn {
  padding: 0.5rem 1.5rem;
  border-radius: 16px;
  border: none;
  background: transparent;
  color: var(--vp-c-text-2);
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-btn.active {
  background: var(--vp-c-brand);
  color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.view-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Timeline Card */
.timeline-card {
  background: var(--vp-c-bg-soft);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  border: 1px solid var(--vp-c-divider);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.timeline-header h3 {
  margin: 0;
  font-size: 1.25rem;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  color: white;
  font-weight: bold;
  font-size: 0.85rem;
}

.timeline-labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: var(--vp-c-text-2);
  margin-bottom: 0.5rem;
}

.timeline-bg { height: 12px; border-radius: 6px; }
.bg-blue { background-color: #3b82f6; }

.expected-text {
  margin-top: 0.8rem;
  font-size: 0.95rem;
  text-align: right;
  color: var(--vp-c-text-2);
}

/* Common Phase Card */
.phase-card {
  background: var(--vp-c-bg-soft);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--vp-c-divider);
}

.sprint { border-top: 4px solid #ef4444; }
.journey { border-top: 4px solid var(--vp-c-brand); }

.card-header {
  text-align: center;
  margin-bottom: 2rem;
}
.card-header h2 { margin: 0 0 0.5rem 0; font-size: 1.5rem; font-weight: bold; }
.subtitle { color: var(--vp-c-text-2); font-size: 0.95rem; }

.card-body {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
}

.circle-container { width: 140px; height: 140px; }
.circular-chart { display: block; margin: 0 auto; max-width: 100%; max-height: 250px; }
.circle-bg { fill: none; stroke: var(--vp-c-divider); stroke-width: 8; }
.circle { fill: none; stroke-width: 8; stroke-linecap: round; transition: stroke-dashoffset 1s ease-out; }

.stroke-brand { stroke: var(--vp-c-brand); }
.stroke-red { stroke: #ef4444; }

.percentage { fill: var(--vp-c-text-1); font-family: inherit; font-size: 22px; font-weight: bold; }

.stats-text { display: flex; flex-direction: column; gap: 0.75rem; font-size: 1.05rem; min-width: 150px; }
.stat-item { display: flex; justify-content: space-between; gap: 1rem; }
.stat-item .label { color: var(--vp-c-text-2); }
.stat-item .value { font-weight: bold; }
.text-green { color: #10b981; }
.text-red { color: #ef4444; }

.section-title {
  margin: 1rem 0 0 0;
  font-size: 1.3rem;
}

/* Tier Breakdown Card */
.tier-breakdown-card {
  background: var(--vp-c-bg-soft);
  border-radius: 8px;
  padding: 1.5rem;
  border: 1px solid var(--vp-c-divider);
}
.tier-breakdown-card h3 { margin: 0 0 1rem 0; font-size: 1.2rem; }
.tier-stats-row { display: flex; gap: 2rem; flex-wrap: wrap; }
.tier-item { flex: 1; min-width: 200px; display: flex; flex-direction: column; gap: 0.4rem; }
.tier-label { font-weight: bold; color: var(--vp-c-text-2); font-size: 0.9rem; }
.tier-progress { display: flex; align-items: center; gap: 0.75rem; }
.tier-bar-bg { flex-grow: 1; height: 6px; background: var(--vp-c-divider); border-radius: 3px; overflow: hidden; }
.tier-bar-fill { height: 100%; border-radius: 3px; transition: width 0.5s ease-out; }
.tier-text { font-size: 0.85rem; font-variant-numeric: tabular-nums; min-width: 45px; text-align: right; }

.bg-red { background-color: #ef4444; }
.bg-orange { background-color: #f97316; }
.bg-yellow { background-color: #eab308; }
.bg-green { background-color: #10b981; }

/* Parts Grid */
.parts-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; }
.part-card { background: var(--vp-c-bg-soft); border-radius: 8px; padding: 1.5rem; border: 1px solid var(--vp-c-divider); }
.part-card h3 { margin: 0 0 1rem 0; font-size: 1.1rem; line-height: 1.4; }
.part-progress-wrapper { display: flex; align-items: center; gap: 1rem; margin-bottom: 0.5rem; }
.progress-bar-bg { flex-grow: 1; height: 8px; background: var(--vp-c-divider); border-radius: 4px; overflow: hidden; }
.progress-bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease-out; }
.part-percentage { font-weight: bold; font-size: 0.9rem; min-width: 40px; text-align: right; }
.part-details { font-size: 0.85rem; color: var(--vp-c-text-2); }

@media (max-width: 480px) {
  .card-body { flex-direction: column; gap: 1.5rem; }
  .toggle-switch { flex-direction: column; border-radius: 12px; }
  .toggle-btn { border-radius: 8px; }
}
</style>
