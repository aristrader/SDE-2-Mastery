// Static-site smoke test. Needs `npm run dev` running (http://localhost:5173).
const { chromium } = require('playwright')
;(async () => {
  const browser = await chromium.launch()
  const page = await browser.newPage()
  const fails = []
  const errors = []
  page.on('pageerror', (e) => errors.push(e.message.slice(0, 140)))

  // Home renders
  await page.goto('http://localhost:5173/')
  await page.waitForLoadState('networkidle')
  if (!(await page.locator('h1, .VPHero .name').count())) fails.push('home did not render a heading')

  // Code page: has the Read/Code tab, the Code tab lists the folder's files, and there
  // are NO run/save controls (read-only) and NO Ask-AI button.
  await page.goto('http://localhost:5173/design_patterns/creational/singleton/Singleton.html')
  await page.waitForLoadState('networkidle')
  const tab = page.locator('.mode-tabs button', { hasText: 'Code' })
  if (!(await tab.count())) fails.push('no Code tab on singleton page')
  else {
    await tab.click()
    await page.waitForTimeout(700)
    if ((await page.locator('.file').count()) < 4) fails.push('singleton folder should list >= 4 files')
    if (await page.locator('.file .run').count()) fails.push('Run buttons should be gone (read-only)')
  }
  if (await page.locator('.ai-fab').count()) fails.push('Ask-AI button should be gone')

  // Doc-only page (study plan README) shows no tab
  await page.goto('http://localhost:5173/todo/study_plan/README.html')
  await page.waitForLoadState('networkidle')
  if (await page.locator('.mode-tabs').count()) fails.push('doc-only page should not show mode tabs')

  if (errors.length) fails.push('console/pageerrors: ' + errors.join(' | '))

  await browser.close()
  if (fails.length) { console.error('SMOKE FAILED:\n' + fails.join('\n')); process.exit(1) }
  console.log('SMOKE PASSED')
})()
