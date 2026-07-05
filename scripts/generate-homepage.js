const fs = require('fs');
const path = require('path');
const readline = require('readline');

const BASE_DIR = path.join(__dirname, '../src/main/java/org/example/backend_fundamentals');
const OUT_FILE = path.join(__dirname, '../docs/.vitepress/navigation_map.json');

const EXCLUDED_DIRS = ['playground', 'exercise', 'solution', 'assets', 'design', 'todo', '.git'];

async function getFrontmatterOrder(filePath) {
    return new Promise((resolve, reject) => {
        if (!fs.existsSync(filePath)) {
            return resolve(null);
        }
        
        const fileStream = fs.createReadStream(filePath);
        const rl = readline.createInterface({
            input: fileStream,
            crlfDelay: Infinity
        });

        let lineCount = 0;
        let inFrontmatter = false;
        let order = null;

        rl.on('line', (line) => {
            lineCount++;
            if (lineCount === 1 && line.trim() === '---') {
                inFrontmatter = true;
                return;
            }
            if (inFrontmatter && line.trim() === '---') {
                rl.close();
                return;
            }
            if (inFrontmatter) {
                const match = line.match(/^order:\s*(\d+)/);
                if (match) {
                    order = parseInt(match[1], 10);
                }
            }
        });

        rl.on('close', () => {
            resolve(order);
        });
        
        rl.on('error', (err) => {
            reject(err);
        });
    });
}

async function validateAndScan(dirPath, isRoot = false) {
    const dirName = path.basename(dirPath);
    
    if (!isRoot) {
        if (!/^[a-zA-Z0-9_-]+$/.test(dirName)) {
            throw new Error(`Invalid folder name (regex /^[a-zA-Z0-9_-]+$/ failed): ${dirPath}`);
        }
    }

    const indexFile = path.join(dirPath, 'index.md');
    
    // Assert case-sensitive index.md
    if (!fs.existsSync(indexFile)) {
        throw new Error(`Missing index.md in directory: ${dirPath}`);
    }
    const realIndexName = fs.readdirSync(dirPath).find(f => f.toLowerCase() === 'index.md');
    if (realIndexName !== 'index.md') {
        throw new Error(`index.md must be exactly lowercase in: ${dirPath}`);
    }

    const order = await getFrontmatterOrder(indexFile);
    if (!isRoot && (order === null || isNaN(order))) {
        throw new Error(`Missing or invalid 'order: X' frontmatter in: ${indexFile}`);
    }

    const entries = fs.readdirSync(dirPath, { withFileTypes: true });
    const subdirs = entries.filter(e => e.isDirectory() && !e.name.startsWith('.'));
    
    const subdirNames = subdirs.map(e => e.name);
    
    const isModule = subdirNames.includes('exercise') || subdirNames.includes('solution') || subdirNames.includes('playground') || subdirNames.includes('design') || subdirNames.includes('assets');

    if (isModule) {
        // Pedagogy (Strict): Assert exercise/ and solution/ folders exist and contain index.md
        if (!subdirNames.includes('exercise')) {
            throw new Error(`Module is missing 'exercise/' directory: ${dirPath}`);
        }
        
        const exerciseIndex = path.join(dirPath, 'exercise', 'index.md');
        if (!fs.existsSync(exerciseIndex)) {
            throw new Error(`Missing index.md in ${path.join(dirPath, 'exercise')}`);
        }
        const exOrder = await getFrontmatterOrder(exerciseIndex);
        if (exOrder === null || isNaN(exOrder)) {
             throw new Error(`Missing or invalid 'order: X' frontmatter in: ${exerciseIndex}`);
        }

        // Check for solution/ or design/
        if (!subdirNames.includes('solution') && !subdirNames.includes('design')) {
            throw new Error(`Module is missing 'solution/' (or 'design/') directory: ${dirPath}`);
        }
        
        const targetDir = subdirNames.includes('solution') ? 'solution' : 'design';
        const targetIndex = path.join(dirPath, targetDir, 'index.md');
        if (!fs.existsSync(targetIndex)) {
            throw new Error(`Missing index.md in ${path.join(dirPath, targetDir)}`);
        }
        const targetOrder = await getFrontmatterOrder(targetIndex);
        if (targetOrder === null || isNaN(targetOrder)) {
             throw new Error(`Missing or invalid 'order: X' frontmatter in: ${targetIndex}`);
        }
    }

    let children = [];
    for (const subdir of subdirs) {
        if (!EXCLUDED_DIRS.includes(subdir.name)) {
            const childNode = await validateAndScan(path.join(dirPath, subdir.name), false);
            children.push(childNode);
        }
    }

    // Check for duplicate orders
    const orders = new Set();
    for (const child of children) {
        if (orders.has(child.order)) {
            throw new Error(`Duplicate order ${child.order} found in siblings of ${dirPath}`);
        }
        orders.add(child.order);
    }
    
    children.sort((a, b) => a.order - b.order);

    let route = path.relative(BASE_DIR, dirPath).split(path.sep).join('/');
    if (route === '') route = '/';
    else route = '/' + route + '/';

    // Get the title from index.md H1
    let title = dirName.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
    const fileContent = fs.readFileSync(indexFile, 'utf-8');
    const h1Match = fileContent.match(/^#\s+(.+)$/m);
    if (h1Match) {
        title = h1Match[1];
    } else if (isRoot) {
        title = "Backend Fundamentals";
    }

    return {
        text: title,
        link: route,
        order: order,
        items: children.length > 0 ? children : undefined
    };
}

async function main() {
    try {
        console.log("Starting strict schema validation and generation...");
        const tree = await validateAndScan(BASE_DIR, true);
        
        // Flatten the tree for navigation_map.json
        const flatMap = [];
        
        function flatten(node) {
            flatMap.push({ text: node.text, link: node.link });
            if (node.items) {
                for (const child of node.items) {
                    flatten(child);
                }
            }
        }
        
        flatten(tree);
        
        fs.mkdirSync(path.dirname(OUT_FILE), { recursive: true });
        fs.writeFileSync(OUT_FILE, JSON.stringify({
            sidebar: tree.items || [],
            navMap: flatMap
        }, null, 2));
        
        console.log("Validation passed! Generated navigation_map.json.");
    } catch (err) {
        if (process.env.NODE_ENV === 'development') {
            console.warn("WARNING: " + err.message);
        } else {
            console.error("FATAL ERROR: " + err.message);
            process.exit(1);
        }
    }
}

main();
