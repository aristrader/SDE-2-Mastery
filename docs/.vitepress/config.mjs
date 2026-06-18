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
      // Bind to IPv4 loopback explicitly (NOT 0.0.0.0 — no LAN exposure) instead
      // of relying on how Node resolves `localhost`. Node 17+ resolves localhost to
      // IPv6 ::1 first, which made the dev server bind IPv6-only — so the Antigravity
      // preview (which connects over IPv4 127.0.0.1) got connection-refused and only
      // the SSR shell painted (partial render). 127.0.0.1 keeps it loopback-only.
      host: '127.0.0.1',
      // Pin the proxy target to IPv4 to match the backend's 127.0.0.1 bind, so the
      // /api proxy can't silently break on the same localhost-resolution ambiguity.
      proxy: { '/api': 'http://127.0.0.1:5174' },
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
