<template>
  <div class="agent-chat">
    <div class="agent-log" ref="logEl">
      <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
        <div class="role">{{ m.role === 'user' ? 'You' : 'Agent' }}</div>
        <pre class="text">{{ m.text }}</pre>
      </div>
      <div v-if="busy" class="msg agent">
        <div class="role">Agent</div>
        <pre class="text">{{ streaming || '…' }}</pre>
      </div>
    </div>
    <form class="agent-input" @submit.prevent="send">
      <input v-model="draft" :disabled="busy" placeholder="Ask about this page, or tell the agent what to do…" />
      <button type="submit" :disabled="busy || !draft.trim()">Send</button>
      <button type="button" class="reset" @click="reset" :disabled="busy">New chat</button>
    </form>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  context: { type: String, default: '' }, // page context injected as system prompt
})

const messages = ref([])
const draft = ref('')
const busy = ref(false)
const streaming = ref('')
const sessionId = ref(null)
const logEl = ref(null)

async function scrollDown() {
  await nextTick()
  if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight
}

function reset() {
  messages.value = []
  sessionId.value = null
  streaming.value = ''
}

function handleEvent(evt) {
  if (evt.session_id && !sessionId.value) sessionId.value = evt.session_id
  // stream-json: assistant messages carry content blocks; result carries final text.
  if (evt.type === 'assistant' && evt.message && Array.isArray(evt.message.content)) {
    for (const block of evt.message.content) {
      if (block.type === 'text') streaming.value += block.text
    }
  } else if (evt.type === 'result' && typeof evt.result === 'string') {
    if (!streaming.value) streaming.value = evt.result
  } else if (evt.type === 'error' || evt.type === 'stderr') {
    streaming.value += (evt.text || '')
  }
}

async function send() {
  const text = draft.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', text })
  draft.value = ''
  busy.value = true
  streaming.value = ''
  await scrollDown()

  try {
    const res = await fetch('/api/agent', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ message: text, sessionId: sessionId.value, context: props.context }),
    })
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buf = ''
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buf += decoder.decode(value, { stream: true })
      const parts = buf.split('\n\n')
      buf = parts.pop()
      for (const part of parts) {
        const line = part.replace(/^data: /, '')
        if (!line) continue
        let evt
        try { evt = JSON.parse(line) } catch { continue }
        handleEvent(evt)
        await scrollDown()
      }
    }
  } catch (e) {
    messages.value.push({ role: 'agent', text: 'Connection error: ' + e.message + '\nIs the backend running? `npm run dev`' })
  } finally {
    if (streaming.value) messages.value.push({ role: 'agent', text: streaming.value })
    streaming.value = ''
    busy.value = false
    await scrollDown()
  }
}
</script>

<style scoped>
.agent-chat { display: flex; flex-direction: column; height: 100%; min-height: 320px; border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; }
.agent-log { flex: 1; overflow-y: auto; padding: 12px; background: var(--vp-c-bg-soft); }
.msg { margin-bottom: 12px; }
.msg .role { font-size: 11px; font-weight: 700; color: var(--vp-c-brand-1); text-transform: uppercase; }
.msg .text { margin: 2px 0 0; white-space: pre-wrap; word-break: break-word; font-family: var(--vp-font-family-base); font-size: 14px; }
.msg.user .text { color: var(--vp-c-text-1); }
.agent-input { display: flex; gap: 6px; padding: 8px; border-top: 1px solid var(--vp-c-divider); }
.agent-input input { flex: 1; padding: 6px 10px; border: 1px solid var(--vp-c-divider); border-radius: 6px; background: var(--vp-c-bg); color: var(--vp-c-text-1); }
.agent-input button { padding: 6px 12px; border: none; border-radius: 6px; background: var(--vp-c-brand-1); color: #fff; cursor: pointer; }
.agent-input button:disabled { opacity: 0.5; cursor: not-allowed; }
.agent-input .reset { background: var(--vp-c-bg-mute); color: var(--vp-c-text-2); }
</style>
