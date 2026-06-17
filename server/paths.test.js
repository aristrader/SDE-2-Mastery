// server/paths.test.js
const { test } = require('node:test')
const assert = require('node:assert')
const { resolveInsideJavaTree, JAVA_ROOT } = require('./paths')

test('resolves a valid in-tree path to an absolute path', () => {
  const abs = resolveInsideJavaTree('design_patterns/creational/singleton/BillPughSingleton.java')
  assert.ok(abs.startsWith(JAVA_ROOT))
  assert.ok(abs.endsWith('BillPughSingleton.java'))
})

test('rejects path traversal', () => {
  assert.throws(() => resolveInsideJavaTree('../../../../../etc/passwd'))
})

test('rejects absolute paths outside the tree', () => {
  assert.throws(() => resolveInsideJavaTree('/etc/passwd'))
})
