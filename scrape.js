const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('http://localhost:5173');
  const text = await page.evaluate(() => document.body.innerText);
  console.log("HOMEPAGE TEXT:");
  console.log(text);
  
  await page.goto('http://localhost:5173/design_patterns/creational/singleton/Singleton.html');
  const singletonText = await page.evaluate(() => document.body.innerText);
  console.log("\nSINGLETON PAGE TEXT:");
  console.log(singletonText);
  await browser.close();
})();
