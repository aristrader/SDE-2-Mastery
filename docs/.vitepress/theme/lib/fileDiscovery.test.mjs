// docs/.vitepress/theme/lib/fileDiscovery.test.mjs
import { test } from 'node:test'
import assert from 'node:assert'
import { analyzeJavaFiles, mdFolderSet, ownerFolder, filesForPage, pageHasCode, toRel } from './fileDiscovery.mjs'

const P = (rel) => `../../../../src/main/java/org/example/backend_fundamentals/${rel}`

const FILES = {
  [P('design_patterns/creational/singleton/BillPughSingleton.java')]:
    'package org.example.singleton;\npublic class BillPughSingleton { public static void main(String[] a){} }',
  [P('design_patterns/creational/singleton/NoSingleton.java')]:
    'package org.example.singleton;\npublic class NoSingleton {}',
  [P('design_patterns/creational/prototype/simple/ShapeRun.java')]:
    'package org.example.proto.simple;\npublic class ShapeRun { public static void main(String[] a){} }',
  [P('design_patterns/creational/prototype/polymorphic/PolyRun.java')]:
    'package org.example.proto.poly;\npublic class PolyRun { public static void main(String[] a){} }',
}
// Pages (md folders): prototype has a page; singleton has a page; the prototype
// subfolders do NOT; the creational roadmap folder has a page.
const MD = mdFolderSet([
  P('design_patterns/creational/CreationalPatternsRoadmap.md'),
  P('design_patterns/creational/singleton/Singleton.md'),
  P('design_patterns/creational/prototype/Prototype.md'),
])

test('toRel normalizes glob keys to repo-relative paths', () => {
  assert.strictEqual(toRel(P('a/b/C.java')), 'a/b/C.java')
})

test('analyzeJavaFiles groups by rel dir and detects mains + rel path', () => {
  const g = analyzeJavaFiles(FILES)
  const sdir = 'design_patterns/creational/singleton'
  assert.strictEqual(g[sdir].length, 2)
  const bp = g[sdir].find((f) => f.name === 'BillPughSingleton.java')
  assert.strictEqual(bp.runnable, true)
  assert.strictEqual(bp.rel, sdir + '/BillPughSingleton.java')
  assert.strictEqual(g[sdir].find((f) => f.name === 'NoSingleton.java').fqcn, null)
})

test('ownerFolder finds nearest md-bearing ancestor', () => {
  assert.strictEqual(ownerFolder('design_patterns/creational/prototype/simple', MD), 'design_patterns/creational/prototype')
  assert.strictEqual(ownerFolder('design_patterns/creational/singleton', MD), 'design_patterns/creational/singleton')
})

test('Prototype page surfaces its subfolder demos', () => {
  const g = analyzeJavaFiles(FILES)
  const files = filesForPage(g, MD, 'design_patterns/creational/prototype')
  assert.strictEqual(files.length, 2)
  assert.deepStrictEqual(files.map((f) => f.name).sort(), ['PolyRun.java', 'ShapeRun.java'])
  assert.ok(files.every((f) => f.group.startsWith('simple') || f.group.startsWith('polymorphic')))
})

test('roadmap page does NOT swallow code owned by deeper pages', () => {
  const g = analyzeJavaFiles(FILES)
  // creational folder has a page but singleton/prototype code is owned by their own pages
  const files = filesForPage(g, MD, 'design_patterns/creational')
  assert.strictEqual(files.length, 0)
  assert.strictEqual(pageHasCode(g, MD, 'design_patterns/creational'), false)
})

test('singleton page surfaces only its own folder', () => {
  const g = analyzeJavaFiles(FILES)
  const files = filesForPage(g, MD, 'design_patterns/creational/singleton')
  assert.strictEqual(files.length, 2)
  assert.ok(files.every((f) => f.group === ''))
})
