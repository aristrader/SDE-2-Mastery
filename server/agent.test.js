// server/agent.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { buildArgs, parseStreamLine } = require('./agent')

test('buildArgs includes core flags and add-dir', () => {
  const args = buildArgs({ message: 'hi', sessionId: null })
  assert.ok(args.includes('-p'))
  assert.ok(args.includes('--output-format'))
  assert.ok(args.includes('stream-json'))
  assert.ok(args.includes('--verbose'))
  assert.ok(args.includes('--add-dir'))
})

test('buildArgs adds --resume when sessionId present', () => {
  const args = buildArgs({ message: 'hi', sessionId: 'abc-123' })
  assert.ok(args.includes('--resume'))
  assert.ok(args.includes('abc-123'))
})

test('buildArgs adds --append-system-prompt when context present', () => {
  const args = buildArgs({ message: 'hi', context: 'page ctx' })
  assert.ok(args.includes('--append-system-prompt'))
  assert.ok(args.includes('page ctx'))
})

test('parseStreamLine returns null for non-JSON', () => {
  assert.strictEqual(parseStreamLine('not json'), null)
})

test('parseStreamLine parses a JSON event', () => {
  const evt = parseStreamLine('{"type":"assistant","session_id":"s1"}')
  assert.strictEqual(evt.type, 'assistant')
  assert.strictEqual(evt.session_id, 's1')
})
