#!/usr/bin/env node
const fs = require('node:fs')
const path = require('node:path')
const { spawnSync } = require('node:child_process')

const root = path.join(__dirname, '..')
const pidFile = path.join(root, '.tmp', 'docs-dev.pid')

function killGroup(pid, signal) {
  try {
    process.kill(-pid, signal)
    return true
  } catch {
    try {
      process.kill(pid, signal)
      return true
    } catch {
      return false
    }
  }
}

if (fs.existsSync(pidFile)) {
  const pid = Number(fs.readFileSync(pidFile, 'utf8'))
  if (pid) {
    killGroup(pid, 'SIGTERM')
    setTimeout(() => killGroup(pid, 'SIGKILL'), 1500).unref?.()
  }
  fs.rmSync(pidFile, { force: true })
}

spawnSync('pkill', ['-TERM', '-f', 'lms-java-lsp-'], { stdio: 'ignore' })
setTimeout(() => {
  spawnSync('pkill', ['-KILL', '-f', 'lms-java-lsp-'], { stdio: 'ignore' })
}, 1500)
console.log('Stopped docs server and any Java LSP temp sessions.')
