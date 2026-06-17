import { defineConfig } from 'vitepress'
import { generateSidebar } from 'vitepress-sidebar'
import { exec } from 'child_process'

const javaRunnerPlugin = {
  name: 'java-runner',
  configureServer(server) {
    server.middlewares.use('/api/run-java', (req, res) => {
      if (req.method === 'POST') {
        let body = ''
        req.on('data', chunk => { body += chunk.toString() })
        req.on('end', () => {
          try {
            const { mainClass } = JSON.parse(body)
            if (!mainClass) throw new Error("mainClass is required")
            
            const cmd = `mvn -q compile && mvn -q exec:java -Dexec.mainClass="${mainClass}"`
            exec(cmd, { cwd: process.cwd() }, (error, stdout, stderr) => {
              res.setHeader('Content-Type', 'application/json')
              res.end(JSON.stringify({ 
                stdout: stdout || '', 
                stderr: stderr || '', 
                error: error ? error.message : null 
              }))
            })
          } catch (e) {
            res.statusCode = 400
            res.end("Bad Request")
          }
        })
      } else {
        res.statusCode = 405
        res.end()
      }
    })
  }
}

const commonSidebarConfig = {
  documentRootPath: 'src/main/java/org/example/backend_fundamentals',
  useTitleFromFileHeading: true,
  useTitleFromFrontmatter: true,
  collapseDepth: 2,
  capitalizeFirst: true,
  sortMenusByFrontmatterOrder: true
}

export default defineConfig({
  title: "SDE-2 Mastery",
  description: "Study Plan & Backend Fundamentals",
  srcDir: '../src/main/java/org/example/backend_fundamentals',
  vite: { plugins: [javaRunnerPlugin] },
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Study Plan', link: '/todo/study_plan/README' },
      { text: 'Java & JVM', link: '/java/foundations/generics/Generics' },
      { text: 'Spring', link: '/spring/ioc_container/IoCContainer' },
      { text: 'System Design', link: '/system_design/clustering/Clustering' },
      { text: 'Design Patterns', link: '/design_patterns/creational/CreationalPatternsRoadmap' },
      { text: 'Networking', link: '/networking/ip_addressing/IpAddressingNatDhcp' },
      { text: 'Databases', link: '/databases/graph_and_graphql/GraphDbAndGraphQl' }
    ],
    search: { provider: 'local' },
    sidebar: generateSidebar([
      { ...commonSidebarConfig, scanStartPath: 'todo', resolvePath: '/todo/' },
      { ...commonSidebarConfig, scanStartPath: 'java', resolvePath: '/java/' },
      { ...commonSidebarConfig, scanStartPath: 'spring', resolvePath: '/spring/' },
      { ...commonSidebarConfig, scanStartPath: 'spring_boot', resolvePath: '/spring_boot/' },
      { ...commonSidebarConfig, scanStartPath: 'system_design', resolvePath: '/system_design/' },
      { ...commonSidebarConfig, scanStartPath: 'design_patterns', resolvePath: '/design_patterns/' },
      { ...commonSidebarConfig, scanStartPath: 'networking', resolvePath: '/networking/' },
      { ...commonSidebarConfig, scanStartPath: 'databases', resolvePath: '/databases/' }
    ])
  }
})
