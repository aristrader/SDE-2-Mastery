const { chromium } = require('playwright');

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

async function waitForLsp(page) {
  await page.waitForFunction(
    () => document.body.innerText.includes('LSP READY') && window.monaco?.editor?.getEditors?.().length,
    null,
    { timeout: 25000 }
  );
}

async function replaceEditorText(page, text) {
  await page.locator('.monaco-editor').first().click({ position: { x: 120, y: 120 } });
  await page.keyboard.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
  await page.keyboard.type(text);
}

async function probe(page, prefix) {
  await replaceEditorText(page, CLASS_START + prefix);
  await page.keyboard.press('Control+Space');
  await page.waitForTimeout(2500);
  const suggestions = await page.locator('.suggest-widget .monaco-list-row').allInnerTexts();
  console.log(`\n${prefix}`, JSON.stringify(suggestions.slice(0, 8)));
  await page.keyboard.press('Escape');
}

async function main() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 1366, height: 900 } });

  await page.goto(`${BASE_URL}${ROUTE}`, { waitUntil: 'networkidle', timeout: 30000 });
  await waitForLsp(page);

  for (const prefix of PREFIXES) {
    await probe(page, prefix);
  }

  await replaceEditorText(page, CLASS_START + 'Li');
  await page.keyboard.press('Control+Space');
  await page.waitForTimeout(2500);
  await page.keyboard.press('Enter');
  await page.waitForTimeout(1000);

  const code = await page.evaluate(() => window.monaco.editor.getEditors()[0].getModel().getValue());
  console.log('\naccepted List code:', code.slice(0, 180).replace(/\n/g, '\\n'));

  await browser.close();
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
