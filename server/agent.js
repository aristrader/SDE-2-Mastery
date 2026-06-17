// server/agent.js
const { spawn } = require('node:child_process')
const { REPO_ROOT } = require('./paths')

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
function streamAgent({ message, sessionId, context }, onEvent) {
  return new Promise((resolve) => {
    const child = spawn('claude', buildArgs({ message, sessionId, context }), {
      cwd: REPO_ROOT,
      env: process.env,
    })
    let buffer = ''
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
      onEvent({ type: 'error', text: err.message })
    })
    child.on('close', (code) => {
      const evt = parseStreamLine(buffer)
      if (evt) onEvent(evt)
      onEvent({ type: 'exit', code })
      resolve()
    })
  })
}

module.exports = { buildArgs, parseStreamLine, streamAgent }
