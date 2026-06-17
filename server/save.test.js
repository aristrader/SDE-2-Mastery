// server/save.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const fs = require('node:fs')
const { saveJava } = require('./save')
const { resolveInsideJavaTree } = require('./paths')

const REL = 'design_patterns/creational/singleton/BillPughSingleton.java'

test('round-trips file content', async () => {
  const abs = resolveInsideJavaTree(REL)
  const original = fs.readFileSync(abs, 'utf8')
  try {
    const modified = original + '\n// scratch-test-comment\n'
    await saveJava(REL, modified)
    assert.strictEqual(fs.readFileSync(abs, 'utf8'), modified)
  } finally {
    fs.writeFileSync(abs, original, 'utf8') // restore
  }
})

test('rejects out-of-tree paths', async () => {
  await assert.rejects(() => saveJava('../../../../etc/passwd.java', 'x'))
})

test('rejects non-java files', async () => {
  await assert.rejects(() => saveJava('design_patterns/notes.txt', 'x'))
})
