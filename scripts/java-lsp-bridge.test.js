const test = require('node:test')
const assert = require('node:assert/strict')

test('java LSP bridge rejects duplicate Java basenames in one session', async () => {
  const { assertUniqueJavaNames } = await import('../docs/.vitepress/dev/javaLspBridge.mjs')

  assert.throws(
    () => assertUniqueJavaNames([{ name: 'TaskRun.java' }, { name: 'TaskRun.java' }]),
    /Duplicate Java file name: TaskRun\.java/,
  )
})
