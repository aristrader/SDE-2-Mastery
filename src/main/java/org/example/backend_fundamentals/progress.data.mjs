import fs from 'fs';
import path from 'path';

export default {
  watch: ['./todo/study_plan/parts/*.md'],
  load() {
    const partsDir = path.resolve(process.cwd(), 'src/main/java/org/example/backend_fundamentals/todo/study_plan/parts');
    const files = fs.readdirSync(partsDir).filter(f => f.startsWith('Part_') && f.endsWith('.md'));

    const progressData = [];
    let globalTotalTopics = 0;
    let globalCompletedTopics = 0;
    const globalTiers = { red: { total: 0, done: 0 }, orange: { total: 0, done: 0 }, yellow: { total: 0, done: 0 }, green: { total: 0, done: 0 } };

    for (const file of files) {
      const content = fs.readFileSync(path.join(partsDir, file), 'utf-8');
      const lines = content.split('\n');

      let title = file.replace('.md', '').replace(/_/g, ' ');
      // Try to extract real title from H1
      const h1Line = lines.find(l => l.startsWith('# '));
      if (h1Line) {
        title = h1Line.replace('# ', '').trim();
      }

      let totalTopics = 0;
      let completedTopics = 0;
      
      const tiers = { red: { total: 0, done: 0 }, orange: { total: 0, done: 0 }, yellow: { total: 0, done: 0 }, green: { total: 0, done: 0 } };

      let inTable = false;

      for (const line of lines) {
        if (line.trim().startsWith('| # | Topic |')) {
          inTable = true;
          continue;
        }
        if (inTable && line.trim().startsWith('|---')) {
          continue;
        }
        if (inTable && line.trim() === '') {
          inTable = false;
          continue;
        }

        if (inTable && line.trim().startsWith('|')) {
          const cols = line.split('|');
          if (cols.length >= 7) {
            const numCell = cols[1].trim();
            if (/^\d+$/.test(numCell)) {
              totalTopics++;
              globalTotalTopics++;
              
              const tagsCell = cols[3] ? cols[3].trim() : '';
              let rowTier = null;
              if (tagsCell.includes('🔴')) rowTier = 'red';
              else if (tagsCell.includes('🟠')) rowTier = 'orange';
              else if (tagsCell.includes('🟡')) rowTier = 'yellow';
              else if (tagsCell.includes('🟢')) rowTier = 'green';
              
              if (rowTier) {
                tiers[rowTier].total++;
                globalTiers[rowTier].total++;
              }

              const doneCell = cols[6].trim();
              const isDone = doneCell.includes('[x]') || doneCell.includes('[X]');
              
              if (isDone) {
                completedTopics++;
                globalCompletedTopics++;
                if (rowTier) {
                  tiers[rowTier].done++;
                  globalTiers[rowTier].done++;
                }
              }
            }
          }
        }
      }

      progressData.push({
        id: file,
        title,
        totalTopics,
        completedTopics,
        percentage: totalTopics > 0 ? Math.round((completedTopics / totalTopics) * 100) : 0,
        tiers
      });
    }

    // Sort by Part number implicitly by filename
    progressData.sort((a, b) => a.id.localeCompare(b.id));

    return {
      parts: progressData,
      global: {
        totalTopics: globalTotalTopics,
        completedTopics: globalCompletedTopics,
        percentage: globalTotalTopics > 0 ? Math.round((globalCompletedTopics / globalTotalTopics) * 100) : 0,
        tiers: globalTiers
      }
    };
  }
};
