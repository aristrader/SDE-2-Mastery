const assert = require('node:assert');
const { spawn } = require('node:child_process');
const { chromium } = require('playwright');
const waitOn = require('wait-on');

const BASE_URL = process.env.PLAYWRIGHT_BASE_URL || 'http://127.0.0.1:5173';
const USE_EXISTING_SERVER = process.env.PLAYWRIGHT_USE_EXISTING_SERVER === '1';
const RUN_MOBILE_CHECKS = process.env.PLAYWRIGHT_RUN_MOBILE === '1';

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

async function assertStudyTabs(page, route, expectedTabs) {
  const errors = [];
  page.removeAllListeners('console');
  page.on('console', (msg) => {
    if (['error', 'warning'].includes(msg.type())) errors.push(`${msg.type()}: ${msg.text()}`);
  });

  const response = await page.goto(`${BASE_URL}${route}`, { waitUntil: 'networkidle', timeout: 30000 });
  assert.strictEqual(response.status(), 200, `${route} should load`);
  const tabs = await page.locator('.study-tabs .study-tab').evaluateAll((els) => els.map((el) => el.textContent.trim()));
  assert.deepStrictEqual(tabs, expectedTabs, `${route} generated study tabs`);

  assert.deepStrictEqual(errors, [], `${route} should not log console errors`);
}

async function openCodeMode(page) {
  await page.goto(`${BASE_URL}/java/oop/nested_classes/#code`, { waitUntil: 'networkidle' });
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

async function assertExerciseWorkspace(page) {
  await page.goto(`${BASE_URL}/java/oop/encapsulation/exercise/`, { waitUntil: 'networkidle' });
  await page.locator('.exercise-workspace').waitFor({ timeout: 15000 });
  await page.locator('.monaco-editor').waitFor({ timeout: 15000 });
  assert.strictEqual(await page.locator('.reference-panel').count(), 0, 'reference panel should start closed');
  await page.getByRole('button', { name: /View Solution/ }).click();
  await page.locator('.reference-panel').waitFor({ timeout: 15000 });
  await page.locator('.reference-doc').waitFor({ timeout: 15000 });
  assert.match(await page.locator('.reference-doc h1').innerText(), /Solution/);
  assert.strictEqual(await page.locator('.reference-panel .VPSidebar').count(), 0, 'reference panel should not embed the full site shell');
}

async function assertStructuredExerciseWorkspace(page) {
  await page.goto(`${BASE_URL}/java/foundations/generics/exercise/`, { waitUntil: 'networkidle' });
  await page.locator('.exercise-workspace.structured').waitFor({ timeout: 15000 });
  assert.strictEqual(await page.locator('.question-item').count(), 5, 'structured practice should list each question');
  assert.strictEqual(
    await page.locator('main > .vp-doc').evaluate((el) => getComputedStyle(el).display),
    'none',
    'structured practice should hide the raw worksheet body'
  );
  assert.strictEqual(
    await page.locator('.VPDoc .aside').evaluate((el) => getComputedStyle(el).display),
    'none',
    'structured practice should hide the aside column'
  );
  assert.ok(
    await page.locator('.exercise-workspace').evaluate((el) => el.getBoundingClientRect().width > 1200),
    'structured practice should use the available desktop width'
  );
  await page.getByRole('button', { name: /bounded-max-sum/i }).click();
  await page.locator('.monaco-editor').waitFor({ timeout: 15000 });
  assert.match(await page.locator('.question-item.active .question-id').innerText(), /bounded-max-sum/);
  assert.strictEqual(await page.locator('.structured-reference').count(), 0, 'selected solution should start closed');
  await page.getByRole('button', { name: /View Solution/ }).click();
  await page.locator('.structured-reference').waitFor({ timeout: 15000 });
  assert.match(await page.locator('.reference-head').innerText(), /Bounds with extends/);
}

async function assertCodeTabActive(page) {
  await page.goto(`${BASE_URL}/java/oop/encapsulation/#code`, { waitUntil: 'networkidle' });
  await page.locator('.monaco-editor').waitFor({ timeout: 15000 });
  const tabs = page.locator('.study-tabs .study-tab');
  const activeTabs = await tabs.evaluateAll((els) =>
    els.filter((el) => el.classList.contains('active')).map((el) => el.textContent.trim())
  );
  assert.deepStrictEqual(activeTabs, ['Code'], 'Code tab should be active for #code routes');
}

async function main() {
  startServer();
  await waitOn({ resources: [BASE_URL], timeout: 90000 });

  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 1366, height: 900 } });

  await assertStudyTabs(page, '/', []);
  await assertStudyTabs(page, '/java/', []);
  await assertStudyTabs(page, '/java/coding_fluency/', []);
  await assertStudyTabs(page, '/java/oop/encapsulation/', ['Read', 'Code', 'Practice', 'Solution']);
  await assertStudyTabs(page, '/java/oop/encapsulation/exercise/', ['Read', 'Code', 'Practice']);
  await assertStudyTabs(page, '/java/oop/encapsulation/solution/', ['Read', 'Code', 'Practice', 'Solution']);
  await assertStudyTabs(page, '/system_design/concepts/availability/', ['Read', 'Scenario', 'Design']);
  await assertStudyTabs(page, '/system_design/concepts/availability/exercise/', ['Read', 'Scenario']);
  await assertStudyTabs(page, '/system_design/concepts/availability/design/', ['Read', 'Scenario', 'Design']);
  await assertStudyTabs(page, '/java/concurrency/jmm/', ['Read', 'Practice', 'Solution']);
  await assertStudyTabs(page, '/java/foundations/control_flow/', ['Read', 'Practice', 'Solution']);
  await assertArchitectureBoardLifecycle(page);
  await assertExerciseWorkspace(page);
  await assertStructuredExerciseWorkspace(page);
  await assertCodeTabActive(page);

  if (RUN_MOBILE_CHECKS) {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto(`${BASE_URL}/system_design/concepts/availability/design/`, { waitUntil: 'networkidle' });
    await page.locator('.drawio-iframe').waitFor({ timeout: 15000 });
    assert.strictEqual(await page.locator('.drawio-iframe').count(), 1, 'mobile design page should render one Draw.io iframe');
    await page.goto(`${BASE_URL}/java/oop/encapsulation/#code`, { waitUntil: 'networkidle' });
    await page.locator('.monaco-editor').waitFor({ timeout: 15000 });
    assert.strictEqual(await page.locator('.monaco-editor').count(), 1, 'mobile code mode should have one Monaco editor');
    assert.strictEqual(await page.locator('.run-warning').count(), 1, 'run warning should be visible');
    await page.setViewportSize({ width: 1366, height: 900 });
  }

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
