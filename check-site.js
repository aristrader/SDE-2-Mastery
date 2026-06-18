const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  try {
    console.log("Navigating to http://localhost:5173/ ...");
    await page.goto('http://localhost:5173/');
    
    const title = await page.title();
    console.log("Page Title:", title);
    
    const linksCount = await page.evaluate(() => document.querySelectorAll('a').length);
    console.log("Links found on homepage:", linksCount);

    console.log("Checking navigation to Study Plan (click 'View Study Plan')...");
    await page.click('text=View Study Plan');
    
    await page.waitForLoadState('networkidle');
    const header = await page.textContent('h1');
    console.log("Successfully navigated to:", header.trim());
    
    // Let's also check the sidebar
    const sidebarItems = await page.evaluate(() => {
        return Array.from(document.querySelectorAll('.VPSidebarItem .text')).map(el => el.textContent);
    });
    console.log("Sidebar items found:", sidebarItems);
    
    console.log("All checks passed! The VitePress site is rendering correctly.");
  } catch (err) {
    console.error("Playwright test failed:", err);
  } finally {
    await browser.close();
  }
})();
