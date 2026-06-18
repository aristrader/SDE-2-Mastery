// server/agentAgy.js
// AI backend using the local `agy` agent CLI (multi-model: Gemini/Claude/GPT).
// Like Claude Code it is AGENTIC — with --add-dir + --dangerously-skip-permissions it
// can read/edit/run files in the repo — but uses agy's own (cheaper) auth. Default model
// is agy's own; override with AGY_MODEL. Conversation history is kept per session in the
// sidecar and embedded in each prompt (so per-page chats stay isolated).
const { spawn } = require('node:child_process')
const { randomUUID } = require('node:crypto')
const { REPO_ROOT } = require('./paths')

const sessions = new Map()
const AGY_BIN = process.env.AGY_BIN || 'agy'
const MAX_SESSIONS = 50        // evict oldest beyond this (bounded memory)
const MAX_HISTORY_MSGS = 20    // keep last ~10 turns per session
const AGENT_TIMEOUT_MS = Number(process.env.AGENT_TIMEOUT_MS) || 180000 // watchdog kill

function getSession(sessionId) {
  let sid = sessionId
  if (!sid || !sessions.has(sid)) {
    sid = randomUUID()
    if (sessions.size >= MAX_SESSIONS) {
      sessions.delete(sessions.keys().next().value) // drop oldest (insertion order)
    }
    sessions.set(sid, [])
  }
  return { sid, history: sessions.get(sid) }
}

function pushTurn(history, userText, assistantText) {
  history.push({ role: 'user', text: userText })
  history.push({ role: 'assistant', text: assistantText })
  if (history.length > MAX_HISTORY_MSGS) history.splice(0, history.length - MAX_HISTORY_MSGS)
}

/** Build the single prompt string from page context + prior turns + the new message. */
function buildPrompt(history, message, context) {
  const parts = []
  if (context) parts.push(context)
  if (history.length) {
    parts.push('Conversation so far:')
    for (const turn of history) {
      parts.push(`${turn.role === 'user' ? 'User' : 'Assistant'}: ${turn.text}`)
    }
  }
  parts.push(`User: ${message}`)
  return parts.join('\n\n')
}

/** Build the agy argv for one non-interactive turn. */
function buildArgs(prompt, model) {
  const args = ['-p', prompt, '--add-dir', REPO_ROOT, '--dangerously-skip-permissions']
  if (model) args.push('--model', model)
  return args
}

/**
 * Stream one turn from agy. Emits events shaped for AgentChat.vue
 * ({type:'assistant', message:{content:[{type:'text'}]}}, 'result', 'error', 'exit'),
 * with session_id on every event.
 */
function streamAgent({ message, sessionId, context, signal }, onEvent) {
  return new Promise((resolve) => {
    const { sid, history } = getSession(sessionId)
    onEvent({ type: 'session', session_id: sid })

    let full = ''
    let settled = false
    const child = spawn(AGY_BIN, buildArgs(buildPrompt(history, message, context), process.env.AGY_MODEL), {
      cwd: REPO_ROOT,
      env: process.env,
      detached: true,                     // own process group → kill the whole tree
      stdio: ['ignore', 'pipe', 'pipe'],  // close stdin so the CLI doesn't wait on it
    })
    const killTree = (s) => { try { process.kill(-child.pid, s) } catch { /* gone */ } }

    const onAbort = () => killTree('SIGKILL') // client disconnect / Stop
    if (signal) {
      if (signal.aborted) onAbort()
      else signal.addEventListener('abort', onAbort, { once: true })
    }
    const watchdog = setTimeout(() => killTree('SIGKILL'), AGENT_TIMEOUT_MS)

    const finish = (code) => {
      if (settled) return
      settled = true
      clearTimeout(watchdog)
      if (signal) signal.removeEventListener('abort', onAbort)
      if (full) pushTurn(history, message, full) // don't persist empty/aborted turns
      onEvent({ type: 'result', session_id: sid, result: full })
      onEvent({ type: 'exit', code })
      resolve()
    }

    child.stdout.on('data', (chunk) => {
      const text = chunk.toString()
      full += text
      onEvent({ type: 'assistant', session_id: sid, message: { content: [{ type: 'text', text }] } })
    })
    child.stderr.on('data', (chunk) => {
      onEvent({ type: 'stderr', session_id: sid, text: chunk.toString() })
    })
    child.on('error', (err) => {
      const hint = err.code === 'ENOENT'
        ? `Could not launch "${AGY_BIN}". Is the agy CLI on PATH? (set AGY_BIN to its absolute path)`
        : err.message
      onEvent({ type: 'error', session_id: sid, text: hint })
      finish(err.code === 'ENOENT' ? 127 : 1) // resolve even if 'close' never fires
    })
    child.on('close', (code) => finish(code))
  })
}

module.exports = { buildPrompt, buildArgs, streamAgent }
