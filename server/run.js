// server/run.js
const { exec } = require('node:child_process')
const { REPO_ROOT } = require('./paths')

/**
 * Compile the module and run a main class via Maven exec.
 * Resolves with { stdout, stderr, error } — error is the process error message or null.
 */
function runJava(fqcn) {
  return new Promise((resolve, reject) => {
    if (!fqcn || typeof fqcn !== 'string') {
      return reject(new Error('fqcn is required'))
    }
    if (!/^[\w.]+$/.test(fqcn)) {
      return reject(new Error('invalid fqcn'))
    }
    const cmd = `mvn -q compile && mvn -q exec:java -Dexec.mainClass="${fqcn}"`
    exec(cmd, { cwd: REPO_ROOT, maxBuffer: 10 * 1024 * 1024 }, (error, stdout, stderr) => {
      resolve({
        stdout: stdout || '',
        stderr: stderr || '',
        error: error ? error.message : null,
      })
    })
  })
}

module.exports = { runJava }
