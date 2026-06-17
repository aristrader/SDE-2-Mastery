// server/run.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { runJava } = require('./run')

test('runs a known main and returns output', { timeout: 180000 }, async () => {
  const fqcn = 'org.example.backend_fundamentals.design_patterns.creational.singleton.BillPughSingleton'
  const res = await runJava(fqcn)
  assert.strictEqual(typeof res.stdout, 'string')
  assert.strictEqual(typeof res.stderr, 'string')
  assert.strictEqual(res.error, null)
})

test('rejects a missing fqcn', async () => {
  await assert.rejects(() => runJava(''))
})

test('rejects an invalid fqcn', async () => {
  await assert.rejects(() => runJava('foo; rm -rf /'))
})
