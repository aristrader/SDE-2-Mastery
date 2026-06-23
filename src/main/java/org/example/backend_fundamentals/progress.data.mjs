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
    let globalTotalMinutes = 0;
    let globalCompletedMinutes = 0;
    const globalTiers = { 
      red: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 }, 
      orange: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 }, 
      yellow: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 }, 
      green: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 } 
    };

    function parseTime(timeStr) {
      if (!timeStr) return 0;
      let m = 0;
      const str = timeStr.toLowerCase();
      const hrsMatch = str.match(/([\d.]+)\s*hr/);
      if (hrsMatch) m += parseFloat(hrsMatch[1]) * 60;
      const minMatch = str.match(/(\d+)\s*min/);
      if (minMatch) m += parseInt(minMatch[1], 10);
      return m;
    }

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
      let partialTopics = 0;
      let totalMinutes = 0;
      let completedMinutes = 0;
      
      const tiers = { 
        red: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 }, 
        orange: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 }, 
        yellow: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 }, 
        green: { total: 0, done: 0, totalMinutes: 0, doneMinutes: 0 } 
      };
      const topics = [];

      let inTable = false;
      let timeColIdx = -1;
      let doneColIdx = -1;
      let partialColIdx = -1;

      for (const line of lines) {
        if (line.trim().startsWith('| # | Topic |')) {
          inTable = true;
          const headers = line.split('|').map(h => h.trim().toLowerCase());
          timeColIdx = headers.indexOf('time');
          doneColIdx = headers.indexOf('done');
          partialColIdx = headers.indexOf('partial');
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
              
              const timeCell = timeColIdx > -1 && cols[timeColIdx] ? cols[timeColIdx].trim() : '';
              const mins = parseTime(timeCell);
              totalMinutes += mins;
              globalTotalMinutes += mins;

              if (rowTier) {
                tiers[rowTier].total++;
                globalTiers[rowTier].total++;
                tiers[rowTier].totalMinutes += mins;
                globalTiers[rowTier].totalMinutes += mins;
              }

              const doneCell = doneColIdx > -1 && cols[doneColIdx] ? cols[doneColIdx].trim() : '';
              const partialCell = partialColIdx > -1 && cols[partialColIdx] ? cols[partialColIdx].trim() : '';
              const isDone = doneCell.includes('[x]') || doneCell.includes('[X]');
              const isPartial = partialCell.includes('[x]') || partialCell.includes('[X]');
              
              let status = 'left';
              if (isDone) {
                status = 'done';
                completedTopics++;
                globalCompletedTopics++;
                completedMinutes += mins;
                globalCompletedMinutes += mins;
                if (rowTier) {
                  tiers[rowTier].done++;
                  globalTiers[rowTier].done++;
                  tiers[rowTier].doneMinutes += mins;
                  globalTiers[rowTier].doneMinutes += mins;
                }
              } else if (isPartial) {
                status = 'partial';
                partialTopics++;
              }

              topics.push({
                num: numCell,
                name: cols[2].trim(),
                status: status,
                tier: rowTier,
                minutes: mins
              });
            }
          }
        }
      }

      progressData.push({
        id: file,
        title,
        totalTopics,
        completedTopics,
        partialTopics,
        totalMinutes,
        completedMinutes,
        percentage: totalTopics > 0 ? Math.round((completedTopics / totalTopics) * 100) : 0,
        timePercentage: totalMinutes > 0 ? Math.round((completedMinutes / totalMinutes) * 100) : 0,
        tiers,
        topics
      });
    }

    // Sort by Part number implicitly by filename
    progressData.sort((a, b) => a.id.localeCompare(b.id));

    return {
      parts: progressData,
      global: {
        totalTopics: globalTotalTopics,
        completedTopics: globalCompletedTopics,
        totalMinutes: globalTotalMinutes,
        completedMinutes: globalCompletedMinutes,
        percentage: globalTotalTopics > 0 ? Math.round((globalCompletedTopics / globalTotalTopics) * 100) : 0,
        timePercentage: globalTotalMinutes > 0 ? Math.round((globalCompletedMinutes / globalTotalMinutes) * 100) : 0,
        tiers: globalTiers
      }
    };
  }
};
