// server/save.js
const fs = require('node:fs/promises')
const { resolveInsideJavaTree } = require('./paths')

/**
 * Write content to a .java file inside the java tree. Throws on out-of-tree paths
 * or non-.java targets.
 */
async function saveJava(relativePath, content) {
  if (!relativePath || typeof content !== 'string') {
    throw new Error('relativePath and content are required')
  }
  if (!relativePath.endsWith('.java')) {
    throw new Error('only .java files may be saved')
  }
  const abs = resolveInsideJavaTree(relativePath)
  await fs.writeFile(abs, content, 'utf8')
  return { ok: true, path: relativePath }
}

module.exports = { saveJava }
