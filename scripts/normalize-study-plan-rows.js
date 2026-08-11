const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..');
const partsDir = path.join(
  repoRoot,
  'src/main/java/org/example/backend_fundamentals/todo/study_plan/parts'
);
function scanStudyPlanRows({ write = false } = {}) {
  const mapping = {};
  const problems = [];

  for (const file of fs.readdirSync(partsDir).filter(name => /^Part_.*\.md$/.test(name)).sort()) {
    const filePath = path.join(partsDir, file);
    const lines = fs.readFileSync(filePath, 'utf8').split('\n');
    const fileMapping = {};
    let inInventory = false;
    let next = 1;

    for (let index = 0; index < lines.length; index++) {
      const trimmed = lines[index].trim();
      if (trimmed.startsWith('| # | Topic |')) {
        inInventory = true;
        continue;
      }
      if (inInventory && trimmed === '') break;
      if (!inInventory || !trimmed.startsWith('|') || trimmed.startsWith('|---')) continue;

      const cells = lines[index].split('|');
      const oldId = cells[1]?.trim();
      if (!oldId || oldId === '#') continue;

      const newId = String(next++);
      if (fileMapping[oldId]) {
        problems.push(`${file}: duplicate row ${oldId}`);
        continue;
      }
      fileMapping[oldId] = newId;
      if (oldId !== newId) {
        problems.push(`${file}: expected ${newId}, found ${oldId}`);
        if (write) {
          cells[1] = ` ${newId} `;
          lines[index] = cells.join('|');
        }
      }
    }

    mapping[file] = fileMapping;
    if (write) fs.writeFileSync(filePath, lines.join('\n'));
  }

  return { mapping, problems };
}

function assertSequentialStudyPlanRows() {
  const { problems } = scanStudyPlanRows();
  if (problems.length) {
    throw new Error(`Study-plan rows must be sequential:\n${problems.join('\n')}`);
  }
}

if (require.main === module) {
  const write = process.argv.includes('--write');
  const { mapping, problems } = scanStudyPlanRows({ write });
  if (write) {
    console.log(`Normalized ${Object.keys(mapping).length} Part files.`);
  } else if (problems.length) {
    console.error(problems.join('\n'));
    process.exitCode = 1;
  } else {
    console.log(`All ${Object.keys(mapping).length} Part files use sequential row numbers.`);
  }
}

module.exports = { assertSequentialStudyPlanRows, scanStudyPlanRows };
