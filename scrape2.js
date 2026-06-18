const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  await page.goto('http://localhost:5174/design_patterns/creational/singleton/Singleton.html');
  const singletonText = await page.evaluate(() => document.body.innerText);
  console.log("SINGLETON PAGE TEXT (5174):");
  console.log(singletonText);
  await browser.close();
})();
