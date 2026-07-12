#!/usr/bin/env node
const fs = require('node:fs')
const path = require('node:path')
const { spawn } = require('node:child_process')

const root = path.join(__dirname, '..')
const tmp = path.join(root, '.tmp')
const pidFile = path.join(tmp, 'docs-dev.pid')
const logFile = path.join(tmp, 'docs-dev.log')
if (process.argv.includes('--help') || process.argv.includes('-h')) {
  console.log('Usage: npm run start        # light docs: no Java runner, no Java LSP')
  console.log('       npm run start:java   # docs with local Java run button')
  console.log('       npm run start:lsp    # docs with local Java runner and Java LSP')
  console.log('       PORT=5174 npm run start:light')
  process.exit(0)
}
const requestedMode = process.argv[2] || 'light'
const mode = ['light', 'java', 'lsp'].includes(requestedMode) ? requestedMode : 'light'
const port = process.env.PORT || (mode === 'lsp' ? '5176' : '5173')

function alive(pid) {
  try {
    process.kill(pid, 0)
    return true
  } catch {
    return false
  }
}

fs.mkdirSync(tmp, { recursive: true })
if (fs.existsSync(pidFile)) {
  const pid = Number(fs.readFileSync(pidFile, 'utf8'))
  if (pid && alive(pid)) {
    console.error(`Docs server already running: pid ${pid}. Run npm run end first.`)
    process.exit(1)
  }
}

const log = fs.openSync(logFile, 'a')
const env = {
  ...process.env,
  VITE_ENABLE_JAVA_RUNNER: mode === 'light' ? '0' : '1',
  VITE_ENABLE_JAVA_LSP: mode === 'lsp' ? '1' : '0',
}
const child = spawn('npm', ['run', 'docs:dev', '--', '--host', '127.0.0.1', '--port', port], {
  cwd: root,
  env,
  detached: true,
  stdio: ['ignore', log, log],
})

fs.writeFileSync(pidFile, String(child.pid))
let exited = false
child.once('exit', (code) => {
  exited = true
  fs.rmSync(pidFile, { force: true })
  console.error(`Docs server failed to start. Exit code: ${code}. Log: ${logFile}`)
  process.exit(code || 1)
})

setTimeout(() => {
  if (exited) return
  child.unref()
  const label = mode === 'light'
    ? 'lightweight (no Java runner, no LSP)'
    : mode === 'java'
      ? 'with Java runner, without LSP'
      : 'with Java runner and LSP'
  console.log(`Started docs ${label} at http://127.0.0.1:${port}/`)
  console.log(`Log: ${logFile}`)
}, 1500)
