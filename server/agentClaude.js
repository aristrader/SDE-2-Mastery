// server/agentClaude.js — Claude Code CLI agent provider (agentic). Used when AI_PROVIDER=claude.
const { spawn } = require('node:child_process')
const { REPO_ROOT } = require('./paths')

const AGENT_TIMEOUT_MS = Number(process.env.AGENT_TIMEOUT_MS) || 180000

/** Build the claude CLI argv for one turn. */
function buildArgs({ message, sessionId, context }) {
  const args = [
    '-p', message,
    '--output-format', 'stream-json',
    '--verbose',
    '--add-dir', REPO_ROOT,
    '--permission-mode', 'acceptEdits',
  ]
  if (context) {
    args.push('--append-system-prompt', context)
  }
  if (sessionId) {
    args.push('--resume', sessionId)
  }
  return args
}

/** Parse one NDJSON line from stream-json output; null if not JSON. */
function parseStreamLine(line) {
  const trimmed = line.trim()
  if (!trimmed) return null
  try {
    return JSON.parse(trimmed)
  } catch {
    return null
  }
}

/**
 * Spawn claude for one turn. Calls onEvent(evt) for each parsed stream-json event.
 * Resolves when the process exits.
 */
function streamAgent({ message, sessionId, context, signal }, onEvent) {
  return new Promise((resolve) => {
    const child = spawn('claude', buildArgs({ message, sessionId, context }), {
      cwd: REPO_ROOT,
      env: process.env,
      detached: true,                     // own process group → kill the whole tree
      stdio: ['ignore', 'pipe', 'pipe'], // close stdin so the CLI doesn't wait on it
    })
    const killTree = (s) => { try { process.kill(-child.pid, s) } catch { /* gone */ } }
    const onAbort = () => killTree('SIGKILL')
    if (signal) {
      if (signal.aborted) onAbort()
      else signal.addEventListener('abort', onAbort, { once: true })
    }
    const watchdog = setTimeout(() => killTree('SIGKILL'), AGENT_TIMEOUT_MS)

    let buffer = ''
    let settled = false
    const finish = (code) => {
      if (settled) return
      settled = true
      clearTimeout(watchdog)
      if (signal) signal.removeEventListener('abort', onAbort)
      const evt = parseStreamLine(buffer)
      if (evt) onEvent(evt)
      onEvent({ type: 'exit', code })
      resolve()
    }
    child.stdout.on('data', (chunk) => {
      buffer += chunk.toString()
      const lines = buffer.split('\n')
      buffer = lines.pop() // keep partial line
      for (const line of lines) {
        const evt = parseStreamLine(line)
        if (evt) onEvent(evt)
      }
    })
    child.stderr.on('data', (chunk) => {
      onEvent({ type: 'stderr', text: chunk.toString() })
    })
    child.on('error', (err) => {
      const hint = err.code === 'ENOENT' ? 'Could not launch "claude" (is it on PATH?)' : err.message
      onEvent({ type: 'error', text: hint })
      finish(err.code === 'ENOENT' ? 127 : 1) // resolve even if 'close' never fires
    })
    child.on('close', (code) => {
      finish(code)
    })
  })
}

module.exports = { buildArgs, parseStreamLine, streamAgent }
