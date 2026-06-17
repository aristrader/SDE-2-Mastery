// server/index.js
const http = require('node:http')
const { runJava } = require('./run')
const { saveJava } = require('./save')
const { streamAgent } = require('./agent')

const PORT = process.env.SITE_BACKEND_PORT || 5174

function readBody(req) {
  return new Promise((resolve, reject) => {
    let data = ''
    req.on('data', (c) => { data += c })
    req.on('end', () => {
      try { resolve(data ? JSON.parse(data) : {}) }
      catch (e) { reject(e) }
    })
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
      if (method === 'GET' && url === '/api/health') {
        return sendJson(res, 200, { ok: true })
      }
      if (method === 'POST' && url === '/api/run') {
        const { fqcn } = await readBody(req)
        const result = await runJava(fqcn)
        return sendJson(res, 200, result)
      }
      if (method === 'POST' && url === '/api/save') {
        const { relativePath, content } = await readBody(req)
        const result = await saveJava(relativePath, content)
        return sendJson(res, 200, result)
      }
      if (method === 'POST' && url === '/api/agent') {
        const { message, sessionId, context } = await readBody(req)
        res.writeHead(200, {
          'content-type': 'text/event-stream',
          'cache-control': 'no-cache',
          connection: 'keep-alive',
        })
        await streamAgent({ message, sessionId, context }, (evt) => {
          res.write(`data: ${JSON.stringify(evt)}\n\n`)
        })
        return res.end()
      }
      sendJson(res, 404, { error: 'not found' })
    } catch (err) {
      sendJson(res, 400, { error: err.message })
    }
  })
}

if (require.main === module) {
  createServer().listen(PORT, () => {
    console.log(`[site-backend] listening on http://localhost:${PORT}`)
  })
}

module.exports = { createServer }
