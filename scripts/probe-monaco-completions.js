const { chromium } = require('playwright');
const assert = require('node:assert/strict');

function option(name, fallback) {
  const index = process.argv.indexOf(`--${name}`);
  return index === -1 ? fallback : process.argv[index + 1];
}

const BASE_URL = option('base-url', process.env.PLAYWRIGHT_BASE_URL || 'http://127.0.0.1:5176');
const ROUTE = option('route', process.env.PLAYWRIGHT_ROUTE || '/java/generics/basics/exercise/');
const PREFIXES = option('prefixes', process.env.PLAYWRIGHT_PREFIXES || 'Li,Array,ArrayL,Tree,TreeS,Hash,LinkedH,Priori')
  .split(',')
  .map((prefix) => prefix.trim())
  .filter(Boolean);

const CLASS_START = 'public class PracticeScratch {\n    public static void main(String[] args) {\n        ';
const CLASS_END = '\n    }\n}\n';
const TYPE_EXPECTATIONS = new Map([
  ['Li', ['List']],
  ['Array', ['ArrayList']],
  ['ArrayL', ['ArrayList']],
  ['Tree', ['TreeSet']],
  ['TreeS', ['TreeSet']],
  ['Hash', ['HashMap']],
  ['LinkedH', ['LinkedHashMap']],
  ['Priori', ['PriorityQueue']],
]);
const MEMBER_EXPECTATIONS = ['add', 'get', 'size', 'remove', 'stream'];
const MEMBER_PREFIX = option('member-prefix', process.env.PLAYWRIGHT_MEMBER_PREFIX || '');

async function waitForLsp(page) {
  await page.waitForFunction(
    () => document.body.innerText.includes('LSP READY') && window.monaco?.editor?.getEditors?.().length,
    null,
    { timeout: 25000 }
  );
}

async function replaceEditorText(page, text) {
  const marker = '/*cursor*/';
  const markerIndex = text.indexOf(marker);
  const value = markerIndex === -1 ? text : text.replace(marker, '');
  const cursorOffset = markerIndex === -1 ? value.length : markerIndex;
  await page.waitForFunction(() => window.monaco?.editor?.getEditors?.().length);
  await page.evaluate(({ value, cursorOffset }) => {
    const editor = window.monaco.editor.getEditors()[0];
    const model = editor.getModel();
    model.setValue(value);
    const position = model.getPositionAt(cursorOffset);
    editor.setPosition(position);
    editor.focus();
  }, { value, cursorOffset });
  await page.waitForFunction(
    (expected) => window.monaco.editor.getEditors()[0]?.getModel()?.getValue() === expected,
    value,
    { timeout: 5000 },
  );
  await page.waitForTimeout(750);
}

function labelsFromSuggestionText(suggestions) {
  return suggestions
    .map((text) => String(text).split(/\n/)[0].split(' - ')[0].split('(')[0].trim())
    .filter(Boolean);
}

async function completionLabels(page, expected = []) {
  let labels = [];
  for (let attempt = 0; attempt < 3; attempt += 1) {
    await page.keyboard.press('Control+Space');
    await page.waitForTimeout(2500);
    const suggestions = await page.locator('.suggest-widget .monaco-list-row').allInnerTexts();
    labels = labelsFromSuggestionText(suggestions);
    await page.keyboard.press('Escape');
    if (expected.every((label) => labels.includes(label))) return labels;
    await page.waitForTimeout(500);
  }
  return labels;
}

function assertContains(prefix, labels, expected) {
  for (const label of expected) {
    assert(
      labels.includes(label),
      `${prefix} suggestions must include ${label}; got ${JSON.stringify(labels.slice(0, 12))}`,
    );
  }
}

function assertRanksBefore(prefix, labels, preferred, rejected) {
  const preferredIndex = labels.indexOf(preferred);
  const rejectedIndex = labels.indexOf(rejected);
  assert.notEqual(preferredIndex, -1, `${prefix} suggestions must include ${preferred}`);
  if (rejectedIndex !== -1) {
    assert(
      preferredIndex < rejectedIndex,
      `${prefix} must rank ${preferred} before ${rejected}; got ${JSON.stringify(labels.slice(0, 12))}`,
    );
  }
}

async function probeTypeCompletion(page, prefix) {
  await replaceEditorText(page, CLASS_START + prefix);
  const expected = TYPE_EXPECTATIONS.get(prefix) || [];
  const labels = await completionLabels(page, expected);
  console.log(`\n${prefix}`, JSON.stringify(labels.slice(0, 8)));
  assertContains(prefix, labels, expected);
  if (prefix === 'Li') assertRanksBefore(prefix, labels, 'List', 'ListResourceBundle');
  if (prefix === 'Array') assertRanksBefore(prefix, labels, 'ArrayList', 'Array');
}

async function probeMemberCompletion(page) {
  await replaceEditorText(
    page,
    'import java.util.ArrayList;\nimport java.util.List;\n\n'
      + CLASS_START
      + 'List<String> list = new ArrayList<>();\n        list.'
      + MEMBER_PREFIX
      + '/*cursor*/'
      + CLASS_END,
  );
  const labels = await completionLabels(page, MEMBER_EXPECTATIONS);
  if (!labels.length) {
    const widget = await page.locator('.suggest-widget').evaluateAll((nodes) => nodes.map((node) => ({
      className: node.className,
      text: node.innerText,
      html: node.outerHTML.slice(0, 300),
    })));
    console.log('\nsuggest widget debug', JSON.stringify(widget));
  }
  console.log(`\nlist.${MEMBER_PREFIX}`, JSON.stringify(labels.slice(0, 12)));
  const allMemberLabels = await page.evaluate(() => window.__javaMemberCompletionLabels || []);
  assertContains(`list.${MEMBER_PREFIX}`, allMemberLabels, MEMBER_EXPECTATIONS);
  assert(labels.includes('add'), `list.${MEMBER_PREFIX} visible suggestions must include add; got ${JSON.stringify(labels.slice(0, 12))}`);
  for (const localOnly of ['class', 'psvm', 'sout']) {
    assert(!labels.includes(localOnly), `list. suggestions must not include local fallback ${localOnly}`);
  }
}

async function probeImportInsertion(page) {
  await replaceEditorText(page, CLASS_START + 'Li');
  await page.keyboard.press('Control+Space');
  await page.waitForTimeout(2500);
  await page.keyboard.press('Enter');
  await page.waitForTimeout(1000);

  const code = await page.evaluate(() => window.monaco.editor.getEditors()[0].getModel().getValue());
  console.log('\naccepted List code:', code.slice(0, 180).replace(/\n/g, '\\n'));
  assert.match(code, /import java\.util\.List;/, 'Accepting List must import java.util.List');
  assert.doesNotMatch(code, /import java\.awt\.List;/, 'Accepting List must not import java.awt.List');
}

async function main() {
  const browser = await chromium.launch({ headless: true });
  try {
    const page = await browser.newPage({ viewport: { width: 1366, height: 900 } });
    page.on('console', (message) => {
      if (['log', 'warning', 'error'].includes(message.type())) console.log(`[browser ${message.type()}] ${message.text()}`);
    });

    await page.goto(`${BASE_URL}${ROUTE}`, { waitUntil: 'networkidle', timeout: 30000 });
    await waitForLsp(page);

    for (const prefix of PREFIXES) {
      await probeTypeCompletion(page, prefix);
    }

    await probeImportInsertion(page);
    await probeMemberCompletion(page);
  } finally {
    await browser.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
