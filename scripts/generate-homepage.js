const fs = require('fs');
const path = require('path');
const readline = require('readline');

const BASE_DIR = process.env.LMS_BASE_DIR || path.join(__dirname, '../src/main/java/org/example/backend_fundamentals');
const OUT_FILE = process.env.LMS_OUT_FILE || path.join(__dirname, '../docs/.vitepress/navigation_map.json');

const EXCLUDED_DIRS = ['playground', 'exercise', 'solution', 'assets', 'design', 'todo', '.git'];
const ASSET_EXTENSIONS = new Set(['.png', '.jpg', '.jpeg', '.gif', '.webp', '.svg', '.drawio']);

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

async function getFrontmatter(filePath) {
    return new Promise((resolve, reject) => {
        if (!fs.existsSync(filePath)) {
            return resolve({});
        }

        const fileStream = fs.createReadStream(filePath);
        const rl = readline.createInterface({
            input: fileStream,
            crlfDelay: Infinity
        });

        let lineCount = 0;
        let inFrontmatter = false;
        const data = {};

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
                const match = line.match(/^([A-Za-z0-9_-]+):\s*(.*)$/);
                if (match) {
                    data[match[1]] = match[2].trim();
                }
            }
        });

        rl.on('close', () => resolve(data));
        rl.on('error', (err) => reject(err));
    });
}

async function assertIndexFrontmatter(filePath, expected) {
    const frontmatter = await getFrontmatter(filePath);
    for (const [key, value] of Object.entries(expected)) {
        if (frontmatter[key] !== value) {
            throw new Error(`${filePath} must declare '${key}: ${value}' in YAML frontmatter`);
        }
    }
}

function assertNoEmptyDirectories(dirPath) {
    const entries = fs.readdirSync(dirPath, { withFileTypes: true })
        .filter(entry => !entry.name.startsWith('.'));

    if (entries.length === 0) {
        throw new Error(`Empty directory is not allowed: ${dirPath}`);
    }

    for (const entry of entries) {
        if (entry.isDirectory()) {
            assertNoEmptyDirectories(path.join(dirPath, entry.name));
        }
    }
}

function assertPlaygroundFiles(dirPath) {
    if (!fs.existsSync(dirPath)) return;
    const entries = fs.readdirSync(dirPath, { withFileTypes: true });
    for (const entry of entries) {
        const entryPath = path.join(dirPath, entry.name);
        if (entry.isDirectory()) {
            assertPlaygroundFiles(entryPath);
            continue;
        }
        if (!entry.isFile() || path.extname(entry.name) !== '.java') {
            throw new Error(`playground/ may contain only .java files: ${entryPath}`);
        }
    }
}

function assertAssetFiles(dirPath) {
    if (!fs.existsSync(dirPath)) return;
    const entries = fs.readdirSync(dirPath, { withFileTypes: true });
    for (const entry of entries) {
        const entryPath = path.join(dirPath, entry.name);
        if (entry.isDirectory()) {
            assertAssetFiles(entryPath);
            continue;
        }
        if (!entry.isFile() || !ASSET_EXTENSIONS.has(path.extname(entry.name).toLowerCase())) {
            throw new Error(`assets/ may contain only image or .drawio files: ${entryPath}`);
        }
    }
}

function assertJavaFilesInPlayground(dirPath, inPlayground = false) {
    const entries = fs.readdirSync(dirPath, { withFileTypes: true });
    for (const entry of entries) {
        if (entry.name.startsWith('.')) continue;
        const entryPath = path.join(dirPath, entry.name);
        if (entry.isDirectory()) {
            assertJavaFilesInPlayground(entryPath, inPlayground || entry.name === 'playground');
            continue;
        }
        if (entry.isFile() && path.extname(entry.name) === '.java' && !inPlayground) {
            throw new Error(`Java playground code must live under playground/: ${entryPath}`);
        }
    }
}

function childDir(subdirNames, name) {
    return subdirNames.includes(name);
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
    if (!isRoot && order % 10 !== 0) {
        throw new Error(`'order: X' must use increments of 10 in: ${indexFile}`);
    }

    const entries = fs.readdirSync(dirPath, { withFileTypes: true });
    const subdirs = entries.filter(e => e.isDirectory() && !e.name.startsWith('.'));
    
    const subdirNames = subdirs.map(e => e.name);
    
    const hasExercise = childDir(subdirNames, 'exercise');
    const hasSolution = childDir(subdirNames, 'solution');
    const hasPlayground = childDir(subdirNames, 'playground');
    const hasDesign = childDir(subdirNames, 'design');
    const hasAssets = childDir(subdirNames, 'assets');
    const isModule = hasExercise || hasSolution || hasPlayground || hasDesign || hasAssets;
    const schema = hasDesign ? 'system-design' : (hasPlayground ? 'interactive-code' : (hasExercise || hasSolution ? 'practice' : 'theory'));

    if (isModule) {
        if (!hasExercise) {
            throw new Error(`Module is missing 'exercise/' directory: ${dirPath}`);
        }
        
        const exerciseIndex = path.join(dirPath, 'exercise', 'index.md');
        if (!fs.existsSync(exerciseIndex)) {
            throw new Error(`Missing index.md in ${path.join(dirPath, 'exercise')}`);
        }
        await assertIndexFrontmatter(exerciseIndex, { order: '10', search: 'false' });

        if (!hasSolution && !hasDesign) {
            throw new Error(`Module is missing 'solution/' (or 'design/') directory: ${dirPath}`);
        }
        
        if (hasSolution && hasDesign) {
            throw new Error(`Module cannot contain both solution/ and design/: ${dirPath}`);
        }

        const targetDir = hasSolution ? 'solution' : 'design';
        const targetIndex = path.join(dirPath, targetDir, 'index.md');
        if (!fs.existsSync(targetIndex)) {
            throw new Error(`Missing index.md in ${path.join(dirPath, targetDir)}`);
        }
        await assertIndexFrontmatter(targetIndex, { order: '20', search: 'false' });

        if (hasPlayground) assertPlaygroundFiles(path.join(dirPath, 'playground'));
        if (hasAssets) assertAssetFiles(path.join(dirPath, 'assets'));
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
        schema,
        capabilities: {
            exercise: hasExercise,
            solution: hasSolution,
            design: hasDesign,
            playground: hasPlayground,
            assets: hasAssets
        },
        items: children.length > 0 ? children : undefined
    };
}

async function main() {
    try {
        console.log("Starting strict schema validation and generation...");
        assertNoEmptyDirectories(BASE_DIR);
        assertJavaFilesInPlayground(BASE_DIR);
        const tree = await validateAndScan(BASE_DIR, true);
        
        // Flatten the tree for navigation_map.json
        const flatMap = [];
        
        const pageMeta = {};

        function toMeta(node) {
            return {
                text: node.text,
                link: node.link,
                pageType: 'theory',
                parentLink: node.link,
                schema: node.schema,
                capabilities: node.capabilities
            };
        }

        function flatten(node) {
            flatMap.push({ text: node.text, link: node.link });
            pageMeta[node.link] = toMeta(node);
            if (node.capabilities?.exercise) {
                pageMeta[node.link + 'exercise/'] = { ...toMeta(node), link: node.link + 'exercise/', parentLink: node.link, pageType: 'exercise' };
            }
            if (node.capabilities?.solution) {
                pageMeta[node.link + 'solution/'] = { ...toMeta(node), link: node.link + 'solution/', parentLink: node.link, pageType: 'solution' };
            }
            if (node.capabilities?.design) {
                pageMeta[node.link + 'design/'] = { ...toMeta(node), link: node.link + 'design/', parentLink: node.link, pageType: 'design' };
            }
            if (node.items) {
                for (const child of node.items) {
                    flatten(child);
                }
            }
        }
        
        flatten(tree);
        
        const multiSidebar = {};
        const topNav = [];
        
        if (tree.items) {
            for (const child of tree.items) {
                topNav.push({
                    text: child.text,
                    link: child.link,
                    activeMatch: child.link
                });
                // The VitePress multi-sidebar format maps the prefix to an array of items.
                // We wrap it in a single group so it looks nice.
                multiSidebar[child.link] = [
                    {
                        text: child.text,
                        items: child.items || []
                    }
                ];
            }
        }
        
        fs.mkdirSync(path.dirname(OUT_FILE), { recursive: true });
        fs.writeFileSync(OUT_FILE, JSON.stringify({
            sidebar: multiSidebar,
            nav: topNav,
            navMap: flatMap,
            pageMeta
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

if (require.main === module) {
    main();
}

module.exports = {
    validateAndScan,
    getFrontmatter,
    getFrontmatterOrder,
    assertNoEmptyDirectories,
    assertJavaFilesInPlayground,
    assertPlaygroundFiles,
    assertAssetFiles,
};
