import { withMermaid } from 'vitepress-plugin-mermaid'
import { generateSidebar } from 'vitepress-sidebar'

const commonSidebarConfig = {
  documentRootPath: 'src/main/java/org/example/backend_fundamentals',
  useTitleFromFileHeading: true,
  useTitleFromFrontmatter: true,
  collapseDepth: 2,
  capitalizeFirst: true,
  sortMenusByFrontmatterOrder: true,
}

export default withMermaid({
  title: 'SDE-2 Mastery',
  description: 'Study Plan & Backend Fundamentals',
  srcDir: '../src/main/java/org/example/backend_fundamentals',
  // Calm, neutral diagrams that don't fight the teal theme (default mermaid is purple).
  // The plugin still swaps to dark automatically on the .dark class.
  mermaid: {
    theme: 'base',
    themeVariables: {
      primaryColor: '#e8efff',
      primaryBorderColor: '#2563eb',
      primaryTextColor: '#161a22',
      secondaryColor: '#f3e8ff',
      tertiaryColor: '#e8fff4',
      lineColor: '#94a3b8',
      fontFamily: "'IBM Plex Sans', sans-serif",
    },
  },
  vite: {
    server: {
      proxy: { '/api': 'http://localhost:5174' },
    },
  },
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Study Plan', link: '/todo/study_plan/README' },
      { text: 'Java & JVM', link: '/java/' },
      { text: 'Spring', link: '/spring/' },
      { text: 'System Design', link: '/system_design/' },
      { text: 'Design Patterns', link: '/design_patterns/' },
      { text: 'Networking', link: '/networking/' },
      { text: 'Databases', link: '/databases/' },
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
      { ...commonSidebarConfig, scanStartPath: 'databases', resolvePath: '/databases/' },
    ]),
  },
})
