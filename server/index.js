// server/index.js
const http = require('node:http')
const { runJava } = require('./run')
const { saveJava } = require('./save')

// AI provider is switchable via AI_PROVIDER:
//   'agy' (default) — local agy agent CLI; agentic. 'gemini' — Gemini API. 'claude' — Claude CLI.
const AI_PROVIDER = process.env.AI_PROVIDER || 'agy'
const providers = {
  agy: () => require('./agentAgy'),
  gemini: () => require('./agentGemini'),
  claude: () => require('./agentClaude'),
}
const { streamAgent } = (providers[AI_PROVIDER] || providers.agy)()

const PORT = process.env.SITE_BACKEND_PORT || 5174
const MAX_BODY = 5 * 1024 * 1024 // 5 MB request cap

// Concurrency caps: mvn is heavy (serialize it); allow a couple agents at once.
const runLimit = makeLimiter(1)
const agentLimit = makeLimiter(2)

/** Counting semaphore. acquire() resolves to a release() fn. */
function makeLimiter(max) {
  let active = 0
  const queue = []
  const pump = () => {
    if (active >= max || queue.length === 0) return
    active++
    const grant = queue.shift()
    grant(() => { active--; pump() })
  }
  return {
    acquire() { return new Promise((resolve) => { queue.push(resolve); pump() }) },
  }
}

/**
 * Reject requests whose Host is not loopback (defeats DNS-rebinding: a rebound
 * evil.com Host won't match) or whose Origin is a non-localhost site (blocks
 * cross-origin drive-by calls to these powerful endpoints).
 */
function originOk(req) {
  const host = (req.headers.host || '').split(':')[0]
  if (host !== '127.0.0.1' && host !== 'localhost') return false
  const origin = req.headers.origin
  if (origin) {
    try {
      const h = new URL(origin).hostname
      if (h !== '127.0.0.1' && h !== 'localhost') return false
    } catch { return false }
  }
  return true
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = ''
    let size = 0
    req.on('data', (c) => {
      size += c.length
      if (size > MAX_BODY) {
        req.destroy()
        reject(Object.assign(new Error('payload too large'), { statusCode: 413 }))
        return
      }
      data += c
    })
    req.on('end', () => { try { resolve(data ? JSON.parse(data) : {}) } catch (e) { reject(e) } })
    req.on('error', reject)
  })
}

function sendJson(res, status, obj) {
  res.writeHead(status, { 'content-type': 'application/json' })
  res.end(JSON.stringify(obj))
}

function createServer() {
  return http.createServer(async (req, res) => {
    const { url, method } = req
    try {
      if (!originOk(req)) return sendJson(res, 403, { error: 'forbidden' })

      if (method === 'GET' && url === '/api/health') {
        return sendJson(res, 200, { ok: true })
      }

      if (method === 'POST' && url === '/api/run') {
        const { fqcn } = await readBody(req)
        const release = await runLimit.acquire()
        const ac = new AbortController()
        res.on('close', () => ac.abort()) // client disconnect / Stop → kill mvn
        try {
          const result = await runJava(fqcn, { signal: ac.signal })
          return sendJson(res, 200, result)
        } finally { release() }
      }

      if (method === 'POST' && url === '/api/save') {
        const { relativePath, content } = await readBody(req)
        const result = await saveJava(relativePath, content)
        return sendJson(res, 200, result)
      }

      if (method === 'POST' && url === '/api/agent') {
        const { message, sessionId, context } = await readBody(req)
        const release = await agentLimit.acquire()
        const ac = new AbortController()
        res.on('close', () => ac.abort())  // client disconnect / Stop → kill agent child
        res.on('error', () => {})          // swallow benign EPIPE/ECONNRESET
        res.writeHead(200, { 'content-type': 'text/event-stream', 'cache-control': 'no-cache' })
        if (res.flushHeaders) res.flushHeaders()
        const write = (evt) => {
          if (!res.writableEnded && !res.destroyed) res.write(`data: ${JSON.stringify(evt)}\n\n`)
        }
        try {
          await streamAgent({ message, sessionId, context, signal: ac.signal }, write)
        } catch (streamErr) {
          write({ type: 'error', text: streamErr.message })
        } finally {
          release()
          if (!res.writableEnded) res.end()
        }
        return
      }

      sendJson(res, 404, { error: 'not found' })
    } catch (err) {
      const status = err.statusCode || 400
      if (!res.headersSent) sendJson(res, status, { error: err.message })
      else if (!res.writableEnded) res.end()
    }
  })
}

if (require.main === module) {
  // Loopback only: /api/run, /api/save and /api/agent (skip-permissions agent) are
  // powerful and unauthenticated — they must not be reachable from the LAN.
  createServer().listen(PORT, '127.0.0.1', () => {
    console.log(`[site-backend] listening on http://127.0.0.1:${PORT} (AI provider: ${AI_PROVIDER})`)
  })
}

module.exports = { createServer }
