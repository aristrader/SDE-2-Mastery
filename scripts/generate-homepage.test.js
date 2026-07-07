const { execFileSync } = require('node:child_process');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const test = require('node:test');
const assert = require('node:assert');

const SCRIPT = path.join(__dirname, 'generate-homepage.js');

function write(filePath, content) {
    fs.mkdirSync(path.dirname(filePath), { recursive: true });
    fs.writeFileSync(filePath, content);
}

function frontmatter(order, extra = '') {
    return `---\norder: ${order}\n${extra}---\n# Page\n`;
}

function makeBase() {
    const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'lms-nav-'));
    write(path.join(dir, 'index.md'), '# Root\n');
    return dir;
}

function runGenerator(baseDir, outFile) {
    execFileSync(process.execPath, [SCRIPT], {
        env: { ...process.env, LMS_BASE_DIR: baseDir, LMS_OUT_FILE: outFile },
        stdio: 'pipe',
    });
}

test('generator emits pageMeta for theory, exercise, solution, and design routes', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'java', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'oop', 'playground', 'Example.java'), 'class Example {}\n');
    write(path.join(base, 'java', 'oop', 'exercise', 'index.md'), frontmatter(10, 'search: false\n'));
    write(path.join(base, 'java', 'oop', 'solution', 'index.md'), `${frontmatter(20, 'search: false\n')}\n## Answer\nUse encapsulation.\n`);

    write(path.join(base, 'system_design', 'index.md'), frontmatter(20));
    write(path.join(base, 'system_design', 'availability', 'index.md'), frontmatter(10));
    write(path.join(base, 'system_design', 'availability', 'exercise', 'index.md'), frontmatter(10, 'search: false\n'));
    write(path.join(base, 'system_design', 'availability', 'design', 'index.md'), frontmatter(20, 'search: false\n'));

    runGenerator(base, out);
    const data = JSON.parse(fs.readFileSync(out, 'utf8'));

    assert.strictEqual(data.pageMeta['/java/oop/'].schema, 'interactive-code');
    assert.strictEqual(data.pageMeta['/java/oop/'].pageType, 'theory');
    assert.strictEqual(data.pageMeta['/java/oop/exercise/'].pageType, 'exercise');
    assert.strictEqual(data.pageMeta['/java/oop/exercise/'].parentLink, '/java/oop/');
    assert.strictEqual(data.pageMeta['/java/oop/solution/'].pageType, 'solution');
    assert.match(data.pageMeta['/java/oop/solution/'].referenceHtml, /<h2>Answer<\/h2>/);
    assert.strictEqual(data.pageMeta['/system_design/availability/'].schema, 'system-design');
    assert.strictEqual(data.pageMeta['/system_design/availability/design/'].pageType, 'design');
});

test('generator emits structured practice metadata when exercise and solution ids match', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'java', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'generics', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'generics', 'exercise', 'index.md'), `---
order: 10
search: false
---
# Practice

## Exercise: generic-pair - Generic pair

### Goal
Use two type parameters.

\`\`\`java
public class GenericPairPractice {
    public static void main(String[] args) {
        System.out.println("ok");
    }
}
\`\`\`
`);
    write(path.join(base, 'java', 'generics', 'solution', 'index.md'), `---
order: 20
search: false
---
# Solutions

## Solution: generic-pair - Generic pair

Return the typed values.
`);

    runGenerator(base, out);
    const data = JSON.parse(fs.readFileSync(out, 'utf8'));
    const practiceSet = data.pageMeta['/java/generics/exercise/'].practiceSet;

    assert.strictEqual(practiceSet.questions.length, 1);
    assert.strictEqual(practiceSet.questions[0].id, 'generic-pair');
    assert.match(practiceSet.questions[0].starterCode, /public class GenericPairPractice/);
    assert.match(practiceSet.questions[0].solutionHtml, /Return the typed values/);
});

test('generator rejects structured practice id mismatches and duplicates', () => {
    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'java', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'generics', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'generics', 'exercise', 'index.md'), `${frontmatter(10, 'search: false\n')}\n## Exercise: one - One\n`);
        write(path.join(base, 'java', 'generics', 'solution', 'index.md'), `${frontmatter(20, 'search: false\n')}\n## Solution: two - Two\n`);

        assert.throws(() => runGenerator(base, out), /has no matching solution/);
    }

    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'java', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'generics', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'generics', 'exercise', 'index.md'), `${frontmatter(10, 'search: false\n')}\n## Exercise: one - One\n## Exercise: one - Duplicate\n`);
        write(path.join(base, 'java', 'generics', 'solution', 'index.md'), `${frontmatter(20, 'search: false\n')}\n## Solution: one - One\n`);

        assert.throws(() => runGenerator(base, out), /Duplicate structured exercise id 'one'/);
    }
});

test('generator marks nested sidebar groups collapsed by default', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'java', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'collections', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'collections', 'maps', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'collections', 'maps', 'hashmap', 'index.md'), frontmatter(10));

    runGenerator(base, out);
    const data = JSON.parse(fs.readFileSync(out, 'utf8'));
    const collections = data.sidebar['/java/'][0].items[0];
    const maps = collections.items[0];

    assert.strictEqual(collections.text, 'Page');
    assert.strictEqual(collections.collapsed, true);
    assert.strictEqual(maps.collapsed, true);
});

test('generator accepts theory-only leaf pages', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'networking', 'index.md'), frontmatter(10));
    write(path.join(base, 'networking', 'dns', 'index.md'), frontmatter(10));

    runGenerator(base, out);
    const data = JSON.parse(fs.readFileSync(out, 'utf8'));
    assert.strictEqual(data.pageMeta['/networking/dns/'].schema, 'theory');
    assert.deepStrictEqual(data.pageMeta['/networking/dns/'].capabilities, {
        exercise: false,
        solution: false,
        design: false,
        playground: false,
        assets: false,
    });
});

test('generator rejects manual hub navigation lists', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'databases', 'index.md'), `${frontmatter(10)}\n- [SQL vs NoSQL](/databases/sql_vs_nosql/)\n`);
    write(path.join(base, 'databases', 'sql_vs_nosql', 'index.md'), frontmatter(10));

    assert.throws(() => runGenerator(base, out), /manual local topic lists/);
});

test('generator rejects manual hub path tables and study-order sections', () => {
    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');

        write(path.join(base, 'design_patterns', 'index.md'), `${frontmatter(10)}\n| Topic | Doc |\n| --- | --- |\n| Factory | \`factory/simple_factory/index.md\` |\n`);
        write(path.join(base, 'design_patterns', 'factory', 'index.md'), frontmatter(10));

        assert.throws(() => runGenerator(base, out), /manual path tables/);
    }

    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');

        write(path.join(base, 'design_patterns', 'index.md'), `${frontmatter(10)}\n## Recommended sequence\n`);
        write(path.join(base, 'design_patterns', 'factory', 'index.md'), frontmatter(10));

        assert.throws(() => runGenerator(base, out), /manual study-order sections/);
    }
});

test('generator rejects manual AutoTopicGrid markers in hub pages', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'java', 'index.md'), `${frontmatter(10)}\n<AutoTopicGrid />\n`);
    write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));

    assert.throws(() => runGenerator(base, out), /remove <AutoTopicGrid>/);
});

test('generator rejects manual generated workspace components in curriculum markdown', () => {
    for (const [component, fileName] of [
        ['Playground', 'index.md'],
        ['ExerciseNav', 'notes.md'],
        ['ExerciseWorkspace', 'index.md'],
    ]) {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'java', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'oop', 'exercise', 'index.md'), frontmatter(10, 'search: false\n'));
        write(path.join(base, 'java', 'oop', 'solution', 'index.md'), frontmatter(20, 'search: false\n'));
        write(path.join(base, 'java', 'oop', 'playground', 'Example.java'), 'class Example {}\n');
        write(path.join(base, 'java', 'oop', fileName), `${frontmatter(30)}\n<${component} />\n`);

        assert.throws(() => runGenerator(base, out), new RegExp(`remove <${component}>`));
    }
});

test('generator rejects worksheet content in topic index pages', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'java', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'records', 'index.md'), `${frontmatter(10)}\n# Records — Coding Exercises\n\n## Exercise 1: Build a DTO\n`);
    write(path.join(base, 'java', 'records', 'exercise', 'index.md'), frontmatter(10, 'search: false\n'));
    write(path.join(base, 'java', 'records', 'solution', 'index.md'), frontmatter(20, 'search: false\n'));

    assert.throws(() => runGenerator(base, out), /must not be titled as a coding exercise sheet/);
});

test('generator rejects loose markdown files outside todo', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'java', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'oop', 'Notes.md'), '# Hidden note\n');
    write(path.join(base, 'todo', 'draft.md'), '# Draft ok\n');

    assert.throws(() => runGenerator(base, out), /Curriculum markdown must be routed as index\.md/);
});

test('generator rejects Java files outside playground directories', () => {
    const base = makeBase();
    const out = path.join(base, 'navigation_map.json');

    write(path.join(base, 'java', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));
    write(path.join(base, 'java', 'oop', 'Example.java'), 'class Example {}\n');
    write(path.join(base, 'java', 'oop', 'exercise', 'index.md'), frontmatter(10, 'search: false\n'));
    write(path.join(base, 'java', 'oop', 'solution', 'index.md'), frontmatter(20, 'search: false\n'));

    assert.throws(() => runGenerator(base, out), /Java playground code must live under playground/);
});

test('generator rejects missing order, non-numeric order, and non-increment order', () => {
    for (const [name, content, message] of [
        ['missing_order', '---\n---\n# Page\n', /Missing or invalid 'order: X'/],
        ['bad_order', '---\norder: abc\n---\n# Page\n', /Missing or invalid 'order: X'/],
        ['wrong_increment', frontmatter(11), /must use increments of 10/],
    ]) {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'java', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', name, 'index.md'), content);
        assert.throws(() => runGenerator(base, out), message);
    }
});

test('generator rejects duplicate sibling orders and invalid folder names', () => {
    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'java', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'jvm', 'index.md'), frontmatter(10));
        assert.throws(() => runGenerator(base, out), /Duplicate order 10/);
    }

    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'bad folder!', 'index.md'), frontmatter(10));
        assert.throws(() => runGenerator(base, out), /Invalid folder name/);
    }
});

test('generator rejects invalid child frontmatter and schema contents', () => {
    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'java', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'oop', 'playground', 'Example.java'), 'class Example {}\n');
        write(path.join(base, 'java', 'oop', 'exercise', 'index.md'), frontmatter(20, 'search: false\n'));
        write(path.join(base, 'java', 'oop', 'solution', 'index.md'), frontmatter(20, 'search: false\n'));
        assert.throws(() => runGenerator(base, out), /order: 10/);
    }

    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'java', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'oop', 'index.md'), frontmatter(10));
        write(path.join(base, 'java', 'oop', 'playground', 'notes.md'), '# no\n');
        write(path.join(base, 'java', 'oop', 'exercise', 'index.md'), frontmatter(10, 'search: false\n'));
        write(path.join(base, 'java', 'oop', 'solution', 'index.md'), frontmatter(20, 'search: false\n'));
        assert.throws(() => runGenerator(base, out), /playground\/ may contain only \.java files/);
    }

    {
        const base = makeBase();
        const out = path.join(base, 'navigation_map.json');
        write(path.join(base, 'system_design', 'index.md'), frontmatter(10));
        write(path.join(base, 'system_design', 'availability', 'index.md'), frontmatter(10));
        write(path.join(base, 'system_design', 'availability', 'assets', 'notes.txt'), 'no\n');
        write(path.join(base, 'system_design', 'availability', 'exercise', 'index.md'), frontmatter(10, 'search: false\n'));
        write(path.join(base, 'system_design', 'availability', 'design', 'index.md'), frontmatter(20, 'search: false\n'));
        assert.throws(() => runGenerator(base, out), /assets\/ may contain only image or \.drawio files/);
    }
});
