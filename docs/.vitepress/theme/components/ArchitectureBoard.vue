<template>
  <div class="architecture-board">
    <iframe
      ref="iframeRef"
      class="drawio-iframe"
      frameborder="0"
      :src="iframeSrc"
    ></iframe>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, computed } from 'vue'
import { useData, useRoute } from 'vitepress'

const props = defineProps({
  src: { type: String, required: true }
})

const { isDark, page } = useData()
const route = useRoute()
const iframeRef = ref(null)

// Dynamically load the drawio xml file based on the prop
// Using eager: false to prevent bundling all drawio files
const drawioFiles = import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.drawio', { query: '?raw', import: 'default' })

const iframeSrc = computed(() => {
  const theme = isDark.value ? 'dark' : 'kennedy'
  return `https://app.diagrams.net/?embed=1&ui=${theme}&spin=1&proto=json`
})

let messageHandler = null

function setupListener() {
  if (messageHandler) {
    window.removeEventListener('message', messageHandler)
  }

  messageHandler = async (e) => {
    // Basic validation of origin
    if (!e.origin.includes('diagrams.net')) return

    try {
      const msg = JSON.parse(e.data)
      if (msg.event === 'init') {
        // Find the absolute path for the requested drawio file
        // The src might be relative to the current page.
        const currentDir = page.value.relativePath.substring(0, page.value.relativePath.lastIndexOf('/'))
        // For simplicity, we search the glob keys for a path ending with props.src
        const fileKey = Object.keys(drawioFiles).find(k => k.endsWith('/' + props.src) || k.endsWith(props.src))
        
        let xmlContent = '<mxfile><diagram id="blank" name="Page-1"><mxGraphModel><root><mxCell id="0"/><mxCell id="1" parent="0"/></root></mxGraphModel></diagram></mxfile>' // default blank
        
        if (fileKey && drawioFiles[fileKey]) {
           xmlContent = await drawioFiles[fileKey]()
        }
        
        iframeRef.value?.contentWindow?.postMessage(JSON.stringify({
          action: 'load',
          autosave: 1,
          xml: xmlContent
        }), '*')
      }
    } catch (err) {
      // ignore JSON parse errors from other messages
    }
  }

  window.addEventListener('message', messageHandler)
}

function resetBoard() {
  if (iframeRef.value) {
    // The "about:blank Flush" for GC
    iframeRef.value.src = 'about:blank'
    setTimeout(() => {
      if (iframeRef.value) {
        iframeRef.value.src = iframeSrc.value
      }
    }, 50)
  }
  setupListener()
}

onMounted(() => {
  setupListener()
})

watch(() => route.path, () => {
  resetBoard()
})

watch(isDark, () => {
  resetBoard()
})

onBeforeUnmount(() => {
  if (messageHandler) {
    window.removeEventListener('message', messageHandler)
  }
  if (iframeRef.value) {
    iframeRef.value.src = 'about:blank'
  }
})
</script>

<style scoped>
.architecture-board {
  margin-top: 1rem;
  width: 100%;
  height: 60vh;
  min-height: 500px;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  overflow: hidden;
  background: var(--vp-c-bg-soft);
}
.drawio-iframe {
  width: 100%;
  height: 100%;
}
</style>
