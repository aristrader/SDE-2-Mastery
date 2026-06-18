// server/save.js
const fs = require('node:fs/promises')
const path = require('node:path')
const { resolveInsideJavaTree, JAVA_ROOT } = require('./paths')

/**
 * Write content to a .java file inside the java tree. Throws on out-of-tree paths,
 * non-.java targets, or attempts to write through a symlink that escapes the tree.
 */
async function saveJava(relativePath, content) {
  if (!relativePath || typeof content !== 'string') {
    throw new Error('relativePath and content are required')
  }
  if (!relativePath.endsWith('.java')) {
    throw new Error('only .java files may be saved')
  }
  const abs = resolveInsideJavaTree(relativePath) // lexical guard against ../ traversal

  // Defense-in-depth: the lexical guard can be defeated by a symlink planted inside the
  // tree. Resolve the REAL parent dir and re-assert it's inside JAVA_ROOT, and refuse to
  // overwrite a target that is itself a symlink.
  let realDir
  try {
    realDir = await fs.realpath(path.dirname(abs))
  } catch {
    throw new Error('target directory does not exist')
  }
  if (realDir !== JAVA_ROOT && !realDir.startsWith(JAVA_ROOT + path.sep)) {
    throw new Error('target escapes the java tree')
  }
  try {
    const st = await fs.lstat(abs)
    if (st.isSymbolicLink()) throw new Error('refusing to write through a symlink')
  } catch (e) {
    if (e.code !== 'ENOENT') throw e // ENOENT = new file, fine
  }

  await fs.writeFile(abs, content, 'utf8')
  return { ok: true, path: relativePath }
}

module.exports = { saveJava }
