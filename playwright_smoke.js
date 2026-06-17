const { chromium } = require('playwright')
;(async () => {
  const browser = await chromium.launch()
  const page = await browser.newPage()
  const fails = []

  // Health: backend reachable through the vite proxy
  try {
    const health = await page.request.get('http://localhost:5173/api/health')
    if (health.status() !== 200) fails.push('health not 200 (got ' + health.status() + ')')
  } catch (e) { fails.push('health request threw: ' + e.message) }

  // Singleton page: has Playground tab, lists its files, and exposes multiple run targets
  await page.goto('http://localhost:5173/design_patterns/creational/singleton/Singleton.html')
  await page.waitForLoadState('networkidle')
  const tabBtn = page.locator('.mode-tabs button', { hasText: 'Playground' })
  if (!(await tabBtn.count())) fails.push('no Playground tab on singleton page')
  else {
    await tabBtn.click()
    await page.waitForTimeout(800)
    const fileCount = await page.locator('.file').count()
    if (fileCount < 4) fails.push('singleton folder should list >= 4 java files, got ' + fileCount)
    // Singleton has multiple variants each with a main() -> multiple Run buttons
    const runButtons = await page.locator('.file .run').count()
    if (runButtons < 3) fails.push('singleton should expose >= 3 run targets, got ' + runButtons)
  }

  // A pure-doc page (study plan README) shows NO Playground tab
  await page.goto('http://localhost:5173/todo/study_plan/README.html')
  await page.waitForLoadState('networkidle')
  if (await page.locator('.mode-tabs').count()) fails.push('doc-only page should not show mode tabs')

  await browser.close()
  if (fails.length) { console.error('SMOKE FAILED:\n' + fails.join('\n')); process.exit(1) }
  console.log('SMOKE PASSED')
})()
