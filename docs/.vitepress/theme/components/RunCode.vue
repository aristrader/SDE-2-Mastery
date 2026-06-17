<template>
  <div class="run-code-wrapper">
    <button @click="runCode" :disabled="loading" class="run-btn">
      <svg v-if="!loading" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 6px;"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg>
      {{ loading ? 'Compiling & Running...' : 'Run Code Locally' }}
    </button>
    <div v-if="output || error" class="console-window">
      <div class="console-header">Terminal Output</div>
      <pre v-if="output" class="output-text">{{ output }}</pre>
      <pre v-if="error" class="error-text">{{ error }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  mainClass: {
    type: String,
    required: true
  }
})

const loading = ref(false)
const output = ref('')
const error = ref('')

const runCode = async () => {
  loading.value = true
  output.value = ''
  error.value = ''
  try {
    const res = await fetch('/api/run-java', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mainClass: props.mainClass })
    })
    const data = await res.json()
    output.value = data.stdout
    if (data.stderr) error.value += data.stderr
    if (data.error) error.value += '\n' + data.error
    
    if (!output.value && !error.value) {
      output.value = "Process finished with exit code 0 (No output)"
    }
  } catch (err) {
    error.value = 'Failed to connect to local server: ' + err.message + '\nMake sure the dev server is running.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.run-code-wrapper {
  margin: 20px 0;
  font-family: var(--vp-font-family-base);
}
.run-btn {
  display: inline-flex;
  align-items: center;
  background-color: var(--vp-c-brand-1);
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}
.run-btn:hover:not(:disabled) {
  background-color: var(--vp-c-brand-2);
}
.run-btn:disabled {
  background-color: var(--vp-c-neutral-inverse);
  cursor: wait;
}
.console-window {
  margin-top: 12px;
  background-color: #1e1e20;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  border: 1px solid #333;
}
.console-header {
  background-color: #2b2b2d;
  color: #888;
  font-size: 12px;
  padding: 6px 12px;
  font-family: monospace;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #333;
}
.output-text {
  color: #a7f3d0;
  padding: 12px;
  margin: 0;
  overflow-x: auto;
  font-family: var(--vp-font-family-mono);
  font-size: 14px;
}
.error-text {
  color: #fca5a5;
  padding: 12px;
  margin: 0;
  overflow-x: auto;
  font-family: var(--vp-font-family-mono);
  font-size: 14px;
}
</style>
