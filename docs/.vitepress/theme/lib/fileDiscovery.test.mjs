// docs/.vitepress/theme/lib/fileDiscovery.test.mjs
import { test } from 'node:test'
import assert from 'node:assert'
import { analyzeJavaFiles, filesForPage } from './fileDiscovery.mjs'

const FILES = {
  '/src/x/singleton/BillPughSingleton.java':
    'package org.example.singleton;\npublic class BillPughSingleton { public static void main(String[] a){} }',
  '/src/x/singleton/NoSingleton.java':
    'package org.example.singleton;\npublic class NoSingleton {}',
  '/src/x/maps/basics/HashMapBasicsRun.java':
    'package org.example.maps;\npublic class HashMapBasicsRun { public static void main(String[] a){} }',
}

test('groups files by directory', () => {
  const grouped = analyzeJavaFiles(FILES)
  assert.ok(grouped['/src/x/singleton'])
  assert.strictEqual(grouped['/src/x/singleton'].length, 2)
})

test('detects main classes by content and builds fqcn', () => {
  const grouped = analyzeJavaFiles(FILES)
  const billpugh = grouped['/src/x/singleton'].find((f) => f.name === 'BillPughSingleton.java')
  const nosingleton = grouped['/src/x/singleton'].find((f) => f.name === 'NoSingleton.java')
  assert.strictEqual(billpugh.runnable, true)
  assert.strictEqual(billpugh.fqcn, 'org.example.singleton.BillPughSingleton')
  assert.strictEqual(nosingleton.runnable, false)
  assert.strictEqual(nosingleton.fqcn, null)
})

test('filesForPage matches the markdown folder', () => {
  const grouped = analyzeJavaFiles(FILES)
  const matched = filesForPage(grouped, 'maps/basics/MapsDoc.md', (dir) => dir.endsWith('/maps/basics'))
  assert.strictEqual(matched.length, 1)
  assert.strictEqual(matched[0].name, 'HashMapBasicsRun.java')
})
