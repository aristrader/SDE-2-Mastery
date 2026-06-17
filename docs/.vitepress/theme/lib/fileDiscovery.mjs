// docs/.vitepress/theme/lib/fileDiscovery.mjs
// Folder-aware discovery of the Java files a doc page should surface.
//
// Ownership model: a .java file belongs to the page whose folder is its NEAREST
// markdown-bearing ancestor. So a topic page picks up code in its own folder AND
// descendant subfolders (e.g. prototype/ surfaces prototype/simple/, prototype/
// polymorphic/), while a high-level page does NOT swallow code that a deeper page
// already owns (e.g. the creational roadmap page won't list singleton/builder code,
// since those folders have their own pages).

const MARKER = 'backend_fundamentals/'

function dirname(p) {
  const i = p.lastIndexOf('/')
  return i === -1 ? '' : p.slice(0, i)
}
function basename(p) {
  const i = p.lastIndexOf('/')
  return i === -1 ? p : p.slice(i + 1)
}

/** Normalize a Vite glob key to a repo path relative to backend_fundamentals/. */
export function toRel(globPath) {
  const i = globPath.indexOf(MARKER)
  return i === -1 ? globPath.replace(/^.*?\/?/, '') : globPath.slice(i + MARKER.length)
}

/** Turn a { globPath: content } map into { relDir: [ {name, rel, content, runnable, fqcn} ] }. */
export function analyzeJavaFiles(filesMap) {
  const grouped = {}
  for (const path of Object.keys(filesMap)) {
    const content = filesMap[path]
    const rel = toRel(path)                       // e.g. design_patterns/.../X.java
    const relDir = dirname(rel)
    const name = basename(rel)
    const className = name.replace(/\.java$/, '')
    const pkgMatch = content.match(/package\s+([\w.]+)\s*;/)
    const pkg = pkgMatch ? pkgMatch[1] : ''
    const runnable = /public\s+static\s+void\s+main\s*\(/.test(content)
    const fqcn = pkg ? `${pkg}.${className}` : className
    const kind = /Practice\.java$/.test(name) ? 'exercise' : 'example'
    if (!grouped[relDir]) grouped[relDir] = []
    grouped[relDir].push({ name, rel, content, runnable, fqcn: runnable ? fqcn : null, kind })
  }
  for (const d of Object.keys(grouped)) grouped[d].sort((a, b) => a.name.localeCompare(b.name))
  return grouped
}

/** Build the set of folders that contain at least one markdown page, from md glob keys. */
export function mdFolderSet(mdPaths) {
  const s = new Set()
  for (const p of mdPaths) s.add(dirname(toRel(p)))
  return s
}

/** The nearest ancestor folder (incl. self) that contains a markdown page, or null. */
export function ownerFolder(relDir, mdFolders) {
  let d = relDir
  while (true) {
    if (mdFolders.has(d)) return d
    const i = d.lastIndexOf('/')
    if (i === -1) return null
    d = d.slice(0, i)
  }
}

/**
 * Files owned by the page at `pageDir`: those in pageDir or a descendant whose nearest
 * markdown-bearing ancestor is pageDir. Each file gets a `group` (the subfolder path
 * under pageDir, or '' if it lives directly in pageDir) for display grouping.
 */
export function filesForPage(grouped, mdFolders, pageDir) {
  const out = []
  for (const relDir of Object.keys(grouped)) {
    if (relDir !== pageDir && !relDir.startsWith(pageDir + '/')) continue
    if (ownerFolder(relDir, mdFolders) !== pageDir) continue
    const group = relDir === pageDir ? '' : relDir.slice(pageDir.length + 1)
    for (const f of grouped[relDir]) out.push({ ...f, group })
  }
  // Stable order: own-folder files first, then grouped subfolders, each alphabetical.
  return out.sort((a, b) => (a.group || '').localeCompare(b.group || '') || a.name.localeCompare(b.name))
}

/** Whether the page at pageDir surfaces any java files. */
export function pageHasCode(grouped, mdFolders, pageDir) {
  return filesForPage(grouped, mdFolders, pageDir).length > 0
}
