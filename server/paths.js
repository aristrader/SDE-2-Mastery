// server/paths.js
const path = require('node:path')

// server/ lives at repo root, so repo root is one level up.
const REPO_ROOT = path.resolve(__dirname, '..')
const JAVA_ROOT = path.join(REPO_ROOT, 'src', 'main', 'java', 'org', 'example', 'backend_fundamentals')

/**
 * Resolve a path relative to JAVA_ROOT to an absolute path, guaranteeing the
 * result stays inside JAVA_ROOT. Throws otherwise.
 */
function resolveInsideJavaTree(relativePath) {
  const abs = path.resolve(JAVA_ROOT, relativePath)
  if (abs !== JAVA_ROOT && !abs.startsWith(JAVA_ROOT + path.sep)) {
    throw new Error(`Path escapes java tree: ${relativePath}`)
  }
  return abs
}

module.exports = { REPO_ROOT, JAVA_ROOT, resolveInsideJavaTree }
