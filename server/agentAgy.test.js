// server/agentAgy.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { buildPrompt, buildArgs } = require('./agentAgy')

test('buildPrompt includes context, history, and the new message in order', () => {
  const history = [{ role: 'user', text: 'hi' }, { role: 'assistant', text: 'hello' }]
  const out = buildPrompt(history, 'what is a singleton?', 'Page: Singleton.md')
  assert.ok(out.startsWith('Page: Singleton.md'))
  assert.ok(out.includes('Conversation so far:'))
  assert.ok(out.includes('User: hi'))
  assert.ok(out.includes('Assistant: hello'))
  assert.ok(out.trim().endsWith('User: what is a singleton?'))
})

test('buildPrompt omits history section when empty', () => {
  const out = buildPrompt([], 'q', 'ctx')
  assert.ok(!out.includes('Conversation so far:'))
})

test('buildArgs has core flags and adds model only when given', () => {
  const a = buildArgs('hi')
  assert.deepStrictEqual(a.slice(0, 2), ['-p', 'hi'])
  assert.ok(a.includes('--add-dir'))
  assert.ok(a.includes('--dangerously-skip-permissions'))
  assert.ok(!a.includes('--model'))
  const b = buildArgs('hi', 'Gemini 3.5 Flash (Medium)')
  assert.ok(b.includes('--model'))
  assert.ok(b.includes('Gemini 3.5 Flash (Medium)'))
})
