const { chromium } = require('playwright');
const path = require('path');

(async () => {
  const browser = await chromium.launch();
  const context = await browser.newContext({
    viewport: { width: 1280, height: 1024 }
  });
  const page = await context.newPage();

  console.log("Navigating to Homepage...");
  await page.goto('http://localhost:5173');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: '/Users/swapnilagarwal/.gemini/antigravity-cli/brain/86ae27da-5573-4467-b010-0bd4f6535a57/homepage.png', fullPage: true });

  console.log("Navigating to Singleton page...");
  await page.goto('http://localhost:5173/design_patterns/creational/singleton/Singleton.html');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: '/Users/swapnilagarwal/.gemini/antigravity-cli/brain/86ae27da-5573-4467-b010-0bd4f6535a57/singleton_page.png', fullPage: true });

  await browser.close();
  console.log("Screenshots captured successfully.");
})();
