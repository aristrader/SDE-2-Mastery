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

function assertNoManualHubNavigation(indexFile, content) {
    if (/<AutoTopicGrid\b/.test(content)) {
        throw new Error(`Hub topic lists are generated by Layout.vue; remove manual <AutoTopicGrid> from: ${indexFile}`);
    }
    const manualLocalList = /^\s*-\s+\[[^\]]+\]\((?:\/|\.\/|\.\.\/)[^)]+\)/m;
    if (manualLocalList.test(content)) {
        throw new Error(`Hub pages with child topics must not contain manual local topic lists; use generated navigation only: ${indexFile}`);
    }
    const manualPathTable = /^\|.*`[^`]+\.md`.*\|/m;
    if (manualPathTable.test(content)) {
        throw new Error(`Hub pages with child topics must not contain manual path tables; use generated navigation only: ${indexFile}`);
    }
    const manualOrderHeading = /^##\s+(Recommended sequence|All docs at a glance|Suggested path|Study order)\s*$/mi;
    if (manualOrderHeading.test(content)) {
        throw new Error(`Hub pages with child topics must not contain manual study-order sections; use frontmatter order and generated navigation only: ${indexFile}`);
    }
}

function assertNoManualGeneratedWorkspace(indexFile, content) {
    const manualComponent = content.match(/<(Playground|ExerciseNav|ExerciseWorkspace|AutoTopicGrid)\b/);
    if (manualComponent) {
        throw new Error(`Generated study UI must not be embedded manually; remove <${manualComponent[1]}> from: ${indexFile}`);
    }
}

function assertTopicIndexIsNotWorksheet(indexFile, content) {
    const parentDir = path.basename(path.dirname(indexFile));
    if (['exercise', 'solution', 'design'].includes(parentDir)) return;

    if (/^#\s+.+Coding Exercises\s*$/mi.test(content)) {
        throw new Error(`Topic index.md must not be titled as a coding exercise sheet; move prompts to exercise/index.md: ${indexFile}`);
    }
    if (/^##\s+(Exercise|Exercises|Practice exercises)\b/mi.test(content)) {
        throw new Error(`Topic index.md must not contain exercise sections; move prompts to exercise/index.md: ${indexFile}`);
    }
}

function assertNoManualGeneratedWorkspaceInMarkdown(dirPath) {
    const entries = fs.readdirSync(dirPath, { withFileTypes: true });
    for (const entry of entries) {
        if (entry.name.startsWith('.')) continue;
        const entryPath = path.join(dirPath, entry.name);
        if (entry.isDirectory()) {
            assertNoManualGeneratedWorkspaceInMarkdown(entryPath);
            continue;
        }
        if (entry.isFile() && path.extname(entry.name) === '.md') {
            assertNoManualGeneratedWorkspace(entryPath, fs.readFileSync(entryPath, 'utf-8'));
        }
    }
}

function assertNoLooseMarkdownFiles(dirPath, ignored = false) {
    const entries = fs.readdirSync(dirPath, { withFileTypes: true });
    for (const entry of entries) {
        if (entry.name.startsWith('.')) continue;
        const entryPath = path.join(dirPath, entry.name);
        const nextIgnored = ignored || (entry.isDirectory() && ['todo', 'playground', 'assets'].includes(entry.name));
        if (entry.isDirectory()) {
            assertNoLooseMarkdownFiles(entryPath, nextIgnored);
            continue;
        }
        if (!nextIgnored && entry.isFile() && path.extname(entry.name) === '.md' && entry.name !== 'index.md') {
            throw new Error(`Curriculum markdown must be routed as index.md, or moved under todo/archive: ${entryPath}`);
        }
    }
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function inlineMarkdown(value) {
    return escapeHtml(value)
        .replace(/`([^`]+)`/g, '<code>$1</code>')
        .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
}

function markdownToHtml(markdown) {
    const body = String(markdown).replace(/^---[\s\S]*?---\s*/, '');
    const lines = body.split(/\r?\n/);
    const html = [];
    let inCode = false;
    let code = [];
    let inList = false;

    function closeList() {
        if (inList) {
            html.push('</ul>');
            inList = false;
        }
    }

    for (const line of lines) {
        const fence = line.match(/^```(\w+)?/);
        if (fence) {
            if (inCode) {
                html.push(`<pre><code>${escapeHtml(code.join('\n'))}</code></pre>`);
                code = [];
                inCode = false;
            } else {
                closeList();
                inCode = true;
            }
            continue;
        }
        if (inCode) {
            code.push(line);
            continue;
        }
        if (!line.trim()) {
            closeList();
            continue;
        }
        const heading = line.match(/^(#{1,4})\s+(.+)$/);
        if (heading) {
            closeList();
            const level = heading[1].length;
            html.push(`<h${level}>${inlineMarkdown(heading[2])}</h${level}>`);
            continue;
        }
        const bullet = line.match(/^\s*[-*]\s+(.+)$/);
        if (bullet) {
            if (!inList) {
                html.push('<ul>');
                inList = true;
            }
            html.push(`<li>${inlineMarkdown(bullet[1])}</li>`);
            continue;
        }
        closeList();
        html.push(`<p>${inlineMarkdown(line)}</p>`);
    }
    closeList();
    return html.join('\n');
}

function referenceHtmlForRoute(route) {
    const rel = route.replace(/^\/|\/$/g, '');
    const filePath = path.join(BASE_DIR, rel, 'index.md');
    return fs.existsSync(filePath) ? markdownToHtml(fs.readFileSync(filePath, 'utf-8')) : '';
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
    assertNoManualGeneratedWorkspace(indexFile, fileContent);
    assertTopicIndexIsNotWorksheet(indexFile, fileContent);
    if (children.length > 0) {
        assertNoManualHubNavigation(indexFile, fileContent);
    }
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
        assertNoManualGeneratedWorkspaceInMarkdown(BASE_DIR);
        assertNoLooseMarkdownFiles(BASE_DIR);
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
                pageMeta[node.link + 'solution/'] = {
                    ...toMeta(node),
                    link: node.link + 'solution/',
                    parentLink: node.link,
                    pageType: 'solution',
                    referenceHtml: referenceHtmlForRoute(node.link + 'solution/')
                };
            }
            if (node.capabilities?.design) {
                pageMeta[node.link + 'design/'] = {
                    ...toMeta(node),
                    link: node.link + 'design/',
                    parentLink: node.link,
                    pageType: 'design',
                    referenceHtml: referenceHtmlForRoute(node.link + 'design/')
                };
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

        function sidebarNode(node, depth = 0) {
            const out = {
                text: node.text,
                link: node.link,
            };
            if (node.items?.length) {
                out.items = node.items.map(child => sidebarNode(child, depth + 1));
                if (depth > 0) out.collapsed = true;
            }
            return out;
        }
        
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
                        items: (child.items || []).map(item => sidebarNode(item, 1))
                    }
                ];
            }
        }
        
        fs.mkdirSync(path.dirname(OUT_FILE), { recursive: true });
        fs.writeFileSync(OUT_FILE, JSON.stringify({
            sidebar: multiSidebar,
            nav: topNav,
            tree,
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
    assertNoManualGeneratedWorkspaceInMarkdown,
    assertNoLooseMarkdownFiles,
};
