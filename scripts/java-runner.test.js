const test = require('node:test')
const assert = require('node:assert/strict')

test('local Java runner compiles and runs a packaged multi-file workspace', async () => {
  const { runJavaLocally } = await import('../docs/.vitepress/dev/javaRunner.mjs')

  const result = await runJavaLocally({
    mainClass: 'smoke.RunnerSmoke',
    files: [
      {
        name: 'RunnerSmoke.java',
        content: 'package smoke; public class RunnerSmoke { public static void main(String[] args) { System.out.println(new Helper().message()); } }',
      },
      {
        name: 'Helper.java',
        content: 'package smoke; class Helper { String message() { return "multi-file ok"; } }',
      },
    ],
  })

  assert.equal(result.ok, true)
  assert.match(result.stdout, /multi-file ok/)
})

test('local Java runner maps support-file compile diagnostics to the support file', async () => {
  const { runJavaLocally } = await import('../docs/.vitepress/dev/javaRunner.mjs')

  const result = await runJavaLocally({
    mainClass: 'smoke.RunnerSmoke',
    files: [
      {
        name: 'RunnerSmoke.java',
        content: 'package smoke; public class RunnerSmoke { public static void main(String[] args) { new Helper().message(); } }',
      },
      {
        name: 'Helper.java',
        content: 'package smoke; class Helper { String message() { return missingSymbol; } }',
      },
    ],
  })

  assert.equal(result.ok, false)
  assert.equal(result.phase, 'compile')
  assert.equal(result.diagnostics[0]?.file, 'Helper.java')
})
