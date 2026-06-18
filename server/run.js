// server/run.js
const { spawn } = require('node:child_process')
const { REPO_ROOT } = require('./paths')

const DEFAULT_TIMEOUT_MS = 120000
const MAX_OUTPUT = 10 * 1024 * 1024 // cap captured stdout/stderr

/**
 * Compile the module and run a main class via Maven.
 * Spawns in its own process group (detached) so the whole tree — mvn AND the JVM it
 * forks for exec:java — can be killed on timeout or client disconnect (via opts.signal).
 * Resolves with { stdout, stderr, error } (error is a message or null); rejects only on
 * a bad/invalid fqcn.
 */
function runJava(fqcn, { signal, timeoutMs = DEFAULT_TIMEOUT_MS } = {}) {
  return new Promise((resolve, reject) => {
    if (!fqcn || typeof fqcn !== 'string') return reject(new Error('fqcn is required'))
    if (!/^[\w.]+$/.test(fqcn)) return reject(new Error('invalid fqcn'))

    const cmd = `mvn -q compile && mvn -q exec:java -Dexec.mainClass="${fqcn}"`
    const child = spawn('sh', ['-c', cmd], {
      cwd: REPO_ROOT,
      detached: true,                      // new process group → killable as a tree
      stdio: ['ignore', 'pipe', 'pipe'],
    })

    let out = ''
    let err = ''
    let settled = false
    let killedReason = null

    const killTree = (sig) => { try { process.kill(-child.pid, sig) } catch { /* already gone */ } }
    const onAbort = () => { killedReason = 'cancel'; killTree('SIGKILL') }
    if (signal) {
      if (signal.aborted) onAbort()
      else signal.addEventListener('abort', onAbort, { once: true })
    }
    const timer = setTimeout(() => { killedReason = 'timeout'; killTree('SIGKILL') }, timeoutMs)

    child.stdout.on('data', (d) => { if (out.length < MAX_OUTPUT) out += d.toString() })
    child.stderr.on('data', (d) => { if (err.length < MAX_OUTPUT) err += d.toString() })

    const finish = (code, spawnErr) => {
      if (settled) return
      settled = true
      clearTimeout(timer)
      if (signal) signal.removeEventListener('abort', onAbort)
      let error = null
      if (spawnErr) error = spawnErr.message
      else if (killedReason === 'timeout') error = `Run timed out after ${Math.round(timeoutMs / 1000)}s and was stopped.`
      else if (killedReason === 'cancel') error = 'Run cancelled.'
      else if (code) error = `Process exited with code ${code}`
      resolve({ stdout: out, stderr: err, error })
    }

    child.on('error', (e) => finish(null, e))
    child.on('close', (code) => finish(code, null))
  })
}

module.exports = { runJava }
