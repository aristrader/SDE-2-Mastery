<template>
  <div class="architecture-board">
    <iframe
      class="drawio-iframe"
      title="Architecture diagram"
      sandbox=""
      :srcdoc="frameDocument"
    ></iframe>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useData } from 'vitepress'

const props = defineProps({
  src: { type: String, required: true }
})

const { isDark, page } = useData()
const rawXml = ref('')
const loadError = ref('')

const drawioFiles = import.meta.glob('../../../../src/main/java/org/example/backend_fundamentals/**/*.drawio', { query: '?raw', import: 'default' })

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

function findDrawioFile() {
  const currentDir = page.value.relativePath.slice(0, page.value.relativePath.lastIndexOf('/'))
  const localPath = `${currentDir}/../assets/${props.src}`.replace('/design/../', '/')
  return Object.keys(drawioFiles).find((key) => key.endsWith(`/${localPath}`) || key.endsWith(`/assets/${props.src}`) || key.endsWith(`/${props.src}`))
}

async function loadDiagram() {
  const fileKey = findDrawioFile()
  if (!fileKey) {
    rawXml.value = ''
    loadError.value = `Diagram asset not found: ${props.src}`
    return
  }

  try {
    rawXml.value = await drawioFiles[fileKey]()
    loadError.value = ''
  } catch (err) {
    rawXml.value = ''
    loadError.value = `Unable to load diagram asset: ${props.src}`
  }
}

function parseDiagram(xml) {
  if (!xml || typeof DOMParser === 'undefined') return { nodes: [], edges: [], error: 'Diagram unavailable during server rendering.' }

  const doc = new DOMParser().parseFromString(xml, 'text/xml')
  const parserError = doc.querySelector('parsererror')
  if (parserError) return { nodes: [], edges: [], error: 'Diagram XML could not be parsed.' }

  const cells = [...doc.querySelectorAll('mxCell')]
  const nodes = cells
    .filter((cell) => cell.getAttribute('vertex') === '1')
    .map((cell) => {
      const geometry = cell.querySelector('mxGeometry')
      return {
        id: cell.getAttribute('id'),
        label: cell.getAttribute('value') || '',
        x: Number(geometry?.getAttribute('x') || 0),
        y: Number(geometry?.getAttribute('y') || 0),
        width: Number(geometry?.getAttribute('width') || 120),
        height: Number(geometry?.getAttribute('height') || 56),
      }
    })

  const byId = new Map(nodes.map((node) => [node.id, node]))
  const edges = cells
    .filter((cell) => cell.getAttribute('edge') === '1')
    .map((cell) => ({
      source: byId.get(cell.getAttribute('source')),
      target: byId.get(cell.getAttribute('target')),
    }))
    .filter((edge) => edge.source && edge.target)

  return { nodes, edges, error: nodes.length ? '' : 'Diagram has no renderable nodes.' }
}

const diagram = computed(() => parseDiagram(rawXml.value))

const frameDocument = computed(() => {
  const dark = isDark.value
  const bg = dark ? '#161618' : '#ffffff'
  const text = dark ? '#f4f4f5' : '#161a22'
  const muted = dark ? '#a1a1aa' : '#64748b'
  const border = dark ? '#3f3f46' : '#cbd5e1'
  const nodeFill = dark ? '#1e293b' : '#f8fafc'
  const nodeStroke = dark ? '#60a5fa' : '#2563eb'
  const nodes = diagram.value.nodes
  const edges = diagram.value.edges
  const error = loadError.value || diagram.value.error

  const maxX = Math.max(720, ...nodes.map((node) => node.x + node.width + 80))
  const maxY = Math.max(420, ...nodes.map((node) => node.y + node.height + 80))
  const edgeMarkup = edges.map((edge) => {
    const x1 = edge.source.x + edge.source.width
    const y1 = edge.source.y + edge.source.height / 2
    const x2 = edge.target.x
    const y2 = edge.target.y + edge.target.height / 2
    return `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${muted}" stroke-width="2" marker-end="url(#arrow)" />`
  }).join('')
  const nodeMarkup = nodes.map((node) => `
    <g>
      <rect x="${node.x}" y="${node.y}" width="${node.width}" height="${node.height}" rx="10" fill="${nodeFill}" stroke="${nodeStroke}" stroke-width="2" />
      <text x="${node.x + node.width / 2}" y="${node.y + node.height / 2 + 5}" text-anchor="middle" fill="${text}" font-family="Inter, system-ui, sans-serif" font-size="15" font-weight="700">${escapeHtml(node.label)}</text>
    </g>
  `).join('')

  const body = error
    ? `<div class="state"><strong>${escapeHtml(error)}</strong><span>${escapeHtml(props.src)}</span></div>`
    : `<svg viewBox="0 0 ${maxX} ${maxY}" role="img" aria-label="Architecture diagram">
        <defs>
          <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
            <path d="M0,0 L0,6 L9,3 z" fill="${muted}" />
          </marker>
        </defs>
        ${edgeMarkup}
        ${nodeMarkup}
      </svg>`

  return `<!doctype html>
    <html>
      <head>
        <meta charset="utf-8">
        <style>
          html, body { margin: 0; min-height: 100%; background: ${bg}; color: ${text}; }
          body { font-family: Inter, ui-sans-serif, system-ui, sans-serif; }
          svg { display: block; width: 100%; height: 100vh; min-height: 420px; }
          .state { min-height: 420px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: ${text}; border: 1px dashed ${border}; box-sizing: border-box; }
          .state span { color: ${muted}; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 13px; }
        </style>
      </head>
      <body>${body}</body>
    </html>`
})

onMounted(loadDiagram)
watch(() => [props.src, page.value.relativePath], loadDiagram)
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
  border: 0;
}
</style>
