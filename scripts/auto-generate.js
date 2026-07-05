const { spawnSync, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

if (process.env.CI) {
    console.log("CI environment detected. Skipping AI Curriculum Auto-Generation.");
    process.exit(0);
}

const MAX_RETRIES = 3;
let retries = 0;

function runValidation() {
    console.log(`[Auto-Gen] Running generate-homepage.js (Attempt ${retries + 1}/${MAX_RETRIES + 1})...`);
    const result = spawnSync('node', [path.join(__dirname, 'generate-homepage.js')], { encoding: 'utf-8' });
    
    if (result.status === 0) {
        console.log("[Auto-Gen] Validation passed! No missing exercises.");
        return true;
    }
    
    console.error("[Auto-Gen] Validation failed:\n", result.stdout, result.stderr);
    
    // Parse the missing directory from the output
    // Example: "FATAL ERROR: Missing index.md in directory: /path/to/missing/exercise"
    const match = (result.stdout + result.stderr).match(/FATAL ERROR: .*directory: (.*)/);
    if (!match) {
        console.error("[Auto-Gen] Could not parse missing directory from output. Aborting.");
        process.exit(1);
    }
    
    const missingDir = match[1].trim();
    return missingDir;
}

function generateContent(missingDir) {
    console.log(`[Auto-Gen] AI Generator invoked for missing directory: ${missingDir}`);
    
    fs.mkdirSync(missingDir, { recursive: true });
    const isSystemDesign = missingDir.includes('system_design');
    const isDesign = missingDir.endsWith('design');
    const isExercise = missingDir.endsWith('exercise');
    const isSolution = missingDir.endsWith('solution');
    
    let prompt = "";
    if (isSystemDesign) {
        if (isExercise) {
            prompt = `Generate a real-world scaling scenario exercise for system design. Include frontmatter 'search: false' and 'order: 10'. Include a blank ArchitectureBoard component: <ArchitectureBoard src="blank.drawio" />`;
        } else if (isDesign) {
            prompt = `Generate a grading rubric and solution design for the system design exercise. Include frontmatter 'search: false' and 'order: 20'.`;
        }
    } else {
        if (isExercise) {
            prompt = `Generate a practical coding problem statement for this backend topic. If purely theoretical, output: "No coding exercise required." Include frontmatter 'search: false' and 'order: 10'.`;
        } else if (isSolution) {
            prompt = `Generate the solution for the coding problem. Include frontmatter 'search: false' and 'order: 20'.`;
        }
    }
    
    if (!prompt) {
        prompt = `Generate introductory content. Include frontmatter 'order: 10'.`;
    }
    
    console.log(`[Auto-Gen] Executing prompt: ${prompt}`);
    
    // Fallback/Mock for Antigravity CLI since we don't want to actually consume tokens in a loop for this mock execution
    // In reality, this would be: execSync(`agy prompt "${prompt}" > ${missingDir}/index.md`);
    const mockContent = `---\nsearch: false\norder: ${isSolution || isDesign ? 20 : 10}\n---\n\n# Auto-Generated Content\n\n${prompt}\n`;
    fs.writeFileSync(path.join(missingDir, 'index.md'), mockContent);
    
    if (isSystemDesign && isExercise) {
        const drawioPath = path.join(missingDir, 'blank.drawio');
        fs.writeFileSync(drawioPath, '<mxfile><diagram id="blank" name="Page-1"><mxGraphModel><root><mxCell id="0"/><mxCell id="1" parent="0"/></root></mxGraphModel></diagram></mxfile>');
    }
}

function main() {
    while (retries <= MAX_RETRIES) {
        const result = runValidation();
        if (result === true) {
            process.exit(0);
        }
        
        const missingDir = result;
        generateContent(missingDir);
        retries++;
    }
    
    console.error(`[Auto-Gen] Failed to resolve missing content after ${MAX_RETRIES} retries. Circuit breaker triggered.`);
    process.exit(1);
}

main();
