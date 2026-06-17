// docs/.vitepress/theme/lib/fileDiscovery.mjs
function dirname(p) {
  const i = p.lastIndexOf('/')
  return i === -1 ? '' : p.slice(0, i)
}
function basename(p) {
  const i = p.lastIndexOf('/')
  return i === -1 ? p : p.slice(i + 1)
}

/** Turn a { path: content } map into { dir: [ {name, path, content, runnable, fqcn} ] }. */
export function analyzeJavaFiles(filesMap) {
  const grouped = {}
  for (const path of Object.keys(filesMap)) {
    const content = filesMap[path]
    const dir = dirname(path)
    const name = basename(path)
    const className = name.replace(/\.java$/, '')
    const pkgMatch = content.match(/package\s+([\w.]+)\s*;/)
    const pkg = pkgMatch ? pkgMatch[1] : ''
    const runnable = /public\s+static\s+void\s+main\s*\(/.test(content)
    const fqcn = pkg ? `${pkg}.${className}` : className
    if (!grouped[dir]) grouped[dir] = []
    grouped[dir].push({ name, path, content, runnable, fqcn: runnable ? fqcn : null })
  }
  for (const dir of Object.keys(grouped)) {
    grouped[dir].sort((a, b) => a.name.localeCompare(b.name))
  }
  return grouped
}

/**
 * Given grouped files and the current markdown relativePath, return the files whose
 * directory matches the markdown's directory. `matcher(dir)` decides the match so the
 * caller can adapt to the glob key shape.
 */
export function filesForPage(grouped, relativePath, matcher) {
  for (const dir of Object.keys(grouped)) {
    if (matcher(dir)) return grouped[dir]
  }
  return []
}
