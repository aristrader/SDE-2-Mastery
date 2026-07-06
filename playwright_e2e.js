const assert = require('node:assert');
const { spawn } = require('node:child_process');
const { chromium } = require('playwright');
const waitOn = require('wait-on');

const BASE_URL = process.env.PLAYWRIGHT_BASE_URL || 'http://127.0.0.1:5173';
const USE_EXISTING_SERVER = process.env.PLAYWRIGHT_USE_EXISTING_SERVER === '1';

let server = null;

function startServer() {
  if (USE_EXISTING_SERVER) return;
  server = spawn('npm', ['run', 'docs:dev', '--', '--host', '127.0.0.1'], {
    stdio: ['ignore', 'pipe', 'pipe'],
    env: process.env,
  });
  server.stdout.on('data', (chunk) => process.stdout.write(chunk));
  server.stderr.on('data', (chunk) => process.stderr.write(chunk));
}

async function stopServer() {
  if (!server) return;
  server.kill('SIGINT');
  await new Promise((resolve) => setTimeout(resolve, 500));
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

function count(texts, pattern) {
  return texts.filter((text) => pattern.test(text)).length;
}

async function assertActions(page, route, expected) {
  const errors = [];
  page.removeAllListeners('console');
  page.on('console', (msg) => {
    if (['error', 'warning'].includes(msg.type())) errors.push(`${msg.type()}: ${msg.text()}`);
  });

  const response = await page.goto(`${BASE_URL}${route}`, { waitUntil: 'networkidle', timeout: 30000 });
  assert.strictEqual(response.status(), 200, `${route} should load`);
  const texts = await visibleTexts(page);

  for (const [name, value] of Object.entries(expected)) {
    const pattern = {
      practice: /Practice Exercise/i,
      scenario: /Design Scenario/i,
      solution: /View Solution/i,
      design: /View Design/i,
      back: /Back to Theory/i,
      read: /^Read$/i,
      code: /Code/i,
    }[name];
    assert.strictEqual(count(texts, pattern), value, `${route} ${name} count`);
  }

  assert.deepStrictEqual(errors, [], `${route} should not log console errors`);
}

async function openCodeMode(page) {
  await page.goto(`${BASE_URL}/java/nested_classes/#code`, { waitUntil: 'networkidle' });
  await page.locator('.monaco-editor').waitFor({ timeout: 15000 });
}

async function mockJavaRunner(page, handler) {
  await page.route('**/api/run-java', handler);
}

async function unmockJavaRunner(page) {
  await page.unroute('**/api/run-java');
}

async function runCode(page) {
  await page.getByRole('button', { name: /^Run$/ }).click();
}

async function assertConsole(page, label, textPattern) {
  await page.locator('.console').waitFor({ timeout: 15000 });
  await page.waitForFunction((source) => {
    const head = document.querySelector('.console-head');
    return head && new RegExp(source).test(head.innerText);
  }, label.source, { timeout: 15000 });
  await assert.match(await page.locator('.console-head').innerText(), label);
  await assert.match(await page.locator('.console-out').innerText(), textPattern);
}

async function assertArchitectureBoardLifecycle(page) {
  await page.goto(`${BASE_URL}/system_design/concepts/availability/design/`, { waitUntil: 'networkidle' });
  const iframe = page.locator('.drawio-iframe');
  await iframe.waitFor({ timeout: 15000 });
  assert.strictEqual(await iframe.count(), 1, 'availability design page should render one Draw.io iframe');
  assert.match(await iframe.getAttribute('srcdoc'), /Load Balancer/, 'Draw.io iframe should render the local fixture');

  await page.goto(`${BASE_URL}/system_design/concepts/availability/`, { waitUntil: 'networkidle' });
  assert.strictEqual(await page.locator('.drawio-iframe').count(), 0, 'diagram iframe should be removed after leaving design page');

  await page.goto(`${BASE_URL}/system_design/concepts/availability/design/`, { waitUntil: 'networkidle' });
  await page.locator('.drawio-iframe').waitFor({ timeout: 15000 });
  assert.strictEqual(await page.locator('.drawio-iframe').count(), 1, 'diagram iframe should render once after returning');
}

async function main() {
  startServer();
  await waitOn({ resources: [BASE_URL], timeout: 90000 });

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 1366, height: 900 } });

  await assertActions(page, '/', {});
  await assertActions(page, '/java/', {});
  await assertActions(page, '/java/coding_fluency/', { practice: 0, scenario: 0, solution: 0, design: 0, read: 0, code: 0 });
  await assertActions(page, '/java/oop/encapsulation/', { practice: 1, read: 1, code: 1 });
  await assertActions(page, '/java/oop/encapsulation/exercise/', { solution: 1, back: 1 });
  await assertActions(page, '/java/oop/encapsulation/solution/', { back: 1 });
  await assertActions(page, '/system_design/concepts/availability/', { scenario: 1 });
  await assertActions(page, '/system_design/concepts/availability/exercise/', { design: 1, back: 1 });
  await assertActions(page, '/system_design/concepts/availability/design/', { back: 1 });
  await assertActions(page, '/java/concurrency/jmm/', { practice: 1 });
  await assertArchitectureBoardLifecycle(page);

  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(`${BASE_URL}/system_design/concepts/availability/design/`, { waitUntil: 'networkidle' });
  await page.locator('.drawio-iframe').waitFor({ timeout: 15000 });
  assert.strictEqual(await page.locator('.drawio-iframe').count(), 1, 'mobile design page should render one Draw.io iframe');
  await page.goto(`${BASE_URL}/java/oop/encapsulation/#code`, { waitUntil: 'networkidle' });
  await page.locator('.monaco-editor').waitFor({ timeout: 15000 });
  assert.strictEqual(await page.locator('.monaco-editor').count(), 1, 'mobile code mode should have one Monaco editor');
  assert.strictEqual(await page.locator('.run-warning').count(), 1, 'run warning should be visible');
  await page.setViewportSize({ width: 1366, height: 900 });

  await mockJavaRunner(page, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ok: true, phase: 'run', stdout: 'hello from local runner\n' }),
    });
  });
  await openCodeMode(page);
  await runCode(page);
  await assertConsole(page, /Success/, /hello from local runner/);
  await unmockJavaRunner(page);

  await mockJavaRunner(page, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ok: false, phase: 'compile', stderr: 'javac failed', error: 'compile failed' }),
    });
  });
  await openCodeMode(page);
  await runCode(page);
  await assertConsole(page, /Compile Error/, /javac failed/);
  await unmockJavaRunner(page);

  await mockJavaRunner(page, async (route) => {
    await route.fulfill({
      status: 429,
      headers: { 'Retry-After': '2', 'Access-Control-Expose-Headers': 'Retry-After' },
      body: 'rate limit'
    });
  });
  await openCodeMode(page);
  await runCode(page);
  await assertConsole(page, /Rate Limited/, /wait 2 seconds/i);
  assert.match(await page.getByRole('button').filter({ hasText: /Wait/ }).innerText(), /Wait/);
  await unmockJavaRunner(page);

  await mockJavaRunner(page, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ok: true, phase: 'run', stdout: 'x'.repeat(50000) }),
    });
  });
  await openCodeMode(page);
  await runCode(page);
  await assertConsole(page, /Success/, /Output truncated/);
  assert.ok((await page.locator('.console-out').innerText()).length < 11000, 'output should be bounded');
  await unmockJavaRunner(page);

  await mockJavaRunner(page, async (route) => {
    await new Promise((resolve) => setTimeout(resolve, 2000));
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ ok: true, phase: 'run', stdout: 'late output' }),
    });
  });
  await openCodeMode(page);
  await runCode(page);
  await page.goto(`${BASE_URL}/java/oop/encapsulation/`, { waitUntil: 'networkidle' });
  assert.strictEqual(await page.locator('.console-out').count(), 0, 'stale run output should not land after route change');
  await unmockJavaRunner(page);

  await browser.close();
  await stopServer();
}

main().catch(async (err) => {
  await stopServer();
  console.error(err);
  process.exit(1);
});
