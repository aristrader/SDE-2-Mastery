// server/agentGemini.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { buildRequestBody, parseSseLine, extractText } = require('./agentGemini')

test('buildRequestBody appends the user message after history', () => {
  const history = [{ role: 'user', parts: [{ text: 'hi' }] }, { role: 'model', parts: [{ text: 'hello' }] }]
  const body = buildRequestBody(history, 'next question', 'page context')
  assert.strictEqual(body.contents.length, 3)
  assert.strictEqual(body.contents[2].role, 'user')
  assert.strictEqual(body.contents[2].parts[0].text, 'next question')
  assert.strictEqual(body.systemInstruction.parts[0].text, 'page context')
})

test('buildRequestBody omits systemInstruction when no context', () => {
  const body = buildRequestBody([], 'q')
  assert.strictEqual(body.systemInstruction, undefined)
})

test('parseSseLine parses a data line and ignores others', () => {
  assert.deepStrictEqual(parseSseLine('data: {"a":1}'), { a: 1 })
  assert.strictEqual(parseSseLine(': comment'), null)
  assert.strictEqual(parseSseLine(''), null)
  assert.strictEqual(parseSseLine('data: [DONE]'), null)
})

test('extractText pulls concatenated parts text', () => {
  const chunk = { candidates: [{ content: { parts: [{ text: 'Hel' }, { text: 'lo' }] } }] }
  assert.strictEqual(extractText(chunk), 'Hello')
  assert.strictEqual(extractText({}), '')
})
