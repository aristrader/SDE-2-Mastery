// server/index.test.js
const { test, after } = require('node:test')
const assert = require('node:assert')
const { createServer } = require('./index')

const server = createServer()
const base = new Promise((resolve) => {
  server.listen(0, () => resolve(`http://127.0.0.1:${server.address().port}`))
})
after(() => server.close())

test('GET /api/health returns ok', async () => {
  const url = await base
  const res = await fetch(`${url}/api/health`)
  const json = await res.json()
  assert.strictEqual(res.status, 200)
  assert.strictEqual(json.ok, true)
})

test('POST /api/save rejects out-of-tree path with 400', async () => {
  const url = await base
  const res = await fetch(`${url}/api/save`, {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ relativePath: '../../etc/passwd.java', content: 'x' }),
  })
  assert.strictEqual(res.status, 400)
})

test('unknown route returns 404', async () => {
  const url = await base
  const res = await fetch(`${url}/api/nope`)
  assert.strictEqual(res.status, 404)
})
