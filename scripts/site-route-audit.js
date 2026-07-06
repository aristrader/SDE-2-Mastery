const assert = require('node:assert');
const { chromium } = require('playwright');
const navData = require('../docs/.vitepress/navigation_map.json');

const BASE_URL = process.env.SITE_AUDIT_BASE_URL || 'http://127.0.0.1:5173';
const LIMIT = process.env.SITE_AUDIT_LIMIT ? Number(process.env.SITE_AUDIT_LIMIT) : Infinity;
const VIEWPORT = (process.env.SITE_AUDIT_VIEWPORT || '1366x900').split('x').map(Number);

function routeList() {
  const routes = new Set(['/']);
  for (const item of navData.navMap || []) routes.add(item.link);
  for (const link of Object.keys(navData.pageMeta || {})) routes.add(link);
  return [...routes].sort().slice(0, LIMIT);
}

async function visibleTexts(page) {
  return page.locator('a,button').evaluateAll((els) =>
    els
      .filter((el) => {
        const style = getComputedStyle(el);
        const rect = el.getBoundingClientRect();
        return style.visibility !== 'hidden' && style.display !== 'none' && rect.width > 0 && rect.height > 0;
      })
      .map((el) => el.innerText.trim())
      .filter(Boolean)
  );
}

function counts(texts) {
  return {
    practice: texts.filter((t) => /Practice Exercise/i.test(t)).length,
    scenario: texts.filter((t) => /Design Scenario/i.test(t)).length,
    solution: texts.filter((t) => /View Solution/i.test(t)).length,
    design: texts.filter((t) => /View Design/i.test(t)).length,
    back: texts.filter((t) => /Back to Theory/i.test(t)).length,
    read: texts.filter((t) => /^Read$/i.test(t)).length,
    code: texts.filter((t) => /Code/i.test(t)).length,
  };
}

async function main() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: VIEWPORT[0], height: VIEWPORT[1] } });
  const failures = [];
  const summary = [];

  for (const route of routeList()) {
    const consoleIssues = [];
    page.removeAllListeners('console');
    page.on('console', (msg) => {
      if (['error', 'warning'].includes(msg.type())) consoleIssues.push(`${msg.type()}: ${msg.text()}`);
    });

    try {
      const response = await page.goto(`${BASE_URL}${route}`, { waitUntil: 'domcontentloaded', timeout: 10000 });
      await page.waitForTimeout(150);
      const status = response ? response.status() : 0;
      const h1 = await page.locator('h1').first().innerText({ timeout: 5000 }).catch(() => '');
      const texts = await visibleTexts(page);
      const editorCount = await page.locator('.monaco-editor').count();
      const horizontalOverflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth + 4);
      const record = { route, status, h1, counts: counts(texts), editorCount, horizontalOverflow, consoleIssues: consoleIssues.slice(0, 3) };
      summary.push(record);

      if (status !== 200) failures.push({ route, reason: `status ${status}` });
      if (consoleIssues.length) failures.push({ route, reason: consoleIssues[0] });
      if (horizontalOverflow) failures.push({ route, reason: 'horizontal overflow' });
    } catch (err) {
      failures.push({ route, reason: err.message });
    }
  }

  await browser.close();
  console.log(JSON.stringify({
    checked: summary.length,
    viewport: { width: VIEWPORT[0], height: VIEWPORT[1] },
    failures,
    samples: summary.filter((row) => row.counts.code || row.counts.scenario || row.counts.design).slice(0, 20),
  }, null, 2));
  assert.deepStrictEqual(failures, []);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
