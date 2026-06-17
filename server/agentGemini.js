// server/agentGemini.js
// Conversational AI backend using the Google Gemini API (Generative Language API).
// Streams text deltas via SSE. Unlike the Claude CLI agent (see agentClaude.js, kept
// for later use), this is a plain LLM: it explains / discusses / quizzes about a page
// but does not edit or run files itself — the Playground's Run/Save buttons cover that.
const { randomUUID } = require('node:crypto')

// In-memory conversation history per session id: sessionId -> [{role, parts:[{text}]}]
const sessions = new Map()

const DEFAULT_MODEL = 'gemini-2.5-flash'
const MAX_SESSIONS = 50      // evict oldest beyond this (bounded memory)
const MAX_HISTORY_MSGS = 20  // keep last ~10 turns (also caps tokens re-sent per call)

/** Build the Gemini request body from prior history + the new user message. */
function buildRequestBody(history, message, context) {
  const body = {
    contents: [...history, { role: 'user', parts: [{ text: message }] }],
  }
  if (context) {
    body.systemInstruction = { parts: [{ text: context }] }
  }
  return body
}

/** Parse one SSE line ("data: {json}"); return the JSON object or null. */
function parseSseLine(line) {
  const trimmed = line.trim()
  if (!trimmed.startsWith('data:')) return null
  const payload = trimmed.slice(5).trim()
  if (!payload || payload === '[DONE]') return null
  try {
    return JSON.parse(payload)
  } catch {
    return null
  }
}

/** Pull the concatenated text out of one Gemini stream chunk. */
function extractText(obj) {
  const parts = obj && obj.candidates && obj.candidates[0] &&
    obj.candidates[0].content && obj.candidates[0].content.parts
  if (!Array.isArray(parts)) return ''
  return parts.map((p) => p.text || '').join('')
}

/**
 * Stream one turn from Gemini. Calls onEvent(evt) for each event, shaped to match what
 * AgentChat.vue already understands ({type:'assistant', message:{content:[{type:'text'}]}},
 * 'result', 'error', 'exit'), with session_id on every event so the client can resume.
 */
async function streamAgent({ message, sessionId, context }, onEvent) {
  const key = process.env.GEMINI_API_KEY
  const model = process.env.GEMINI_MODEL || DEFAULT_MODEL

  let sid = sessionId
  if (!sid || !sessions.has(sid)) {
    sid = randomUUID()
    if (sessions.size >= MAX_SESSIONS) sessions.delete(sessions.keys().next().value)
    sessions.set(sid, [])
  }
  const history = sessions.get(sid)

  onEvent({ type: 'session', session_id: sid })

  if (!key) {
    onEvent({ type: 'error', session_id: sid, text: 'GEMINI_API_KEY is not set in the backend environment. Add it to .env or export it before `npm run dev`.' })
    onEvent({ type: 'exit', code: 1 })
    return
  }

  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:streamGenerateContent?alt=sse`
  const body = buildRequestBody(history, message, context)
  let full = ''

  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'content-type': 'application/json', 'x-goog-api-key': key },
      body: JSON.stringify(body),
    })
    if (!res.ok) {
      const text = await res.text()
      onEvent({ type: 'error', session_id: sid, text: `Gemini ${res.status}: ${text.slice(0, 400)}` })
      onEvent({ type: 'exit', code: 1 })
      return
    }
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buf = ''
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buf += decoder.decode(value, { stream: true })
      const lines = buf.split('\n')
      buf = lines.pop()
      for (const line of lines) {
        const obj = parseSseLine(line)
        if (!obj) continue
        const delta = extractText(obj)
        if (delta) {
          full += delta
          onEvent({ type: 'assistant', session_id: sid, message: { content: [{ type: 'text', text: delta }] } })
        }
      }
    }
    history.push({ role: 'user', parts: [{ text: message }] })
    history.push({ role: 'model', parts: [{ text: full }] })
    if (history.length > MAX_HISTORY_MSGS) history.splice(0, history.length - MAX_HISTORY_MSGS)
    onEvent({ type: 'result', session_id: sid, result: full })
  } catch (e) {
    onEvent({ type: 'error', session_id: sid, text: e.message })
  }
  onEvent({ type: 'exit', code: 0 })
}

module.exports = { buildRequestBody, parseSseLine, extractText, streamAgent }
