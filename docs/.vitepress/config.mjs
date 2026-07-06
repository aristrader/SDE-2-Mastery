import { withMermaid } from 'vitepress-plugin-mermaid'
import navData from './navigation_map.json'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'
import path from 'node:path'

const monacoPlugin = monacoEditorPlugin.default ? monacoEditorPlugin.default : monacoEditorPlugin
const monacoPublicPath = 'monacoeditorwork'

export default withMermaid({
  title: 'SDE-2 Mastery',
  description: 'Study Plan & Backend Fundamentals',
  srcDir: '../src/main/java/org/example/backend_fundamentals',
  srcExclude: ['**/*.java', '**/target/**'],
  ignoreDeadLinks: false,
  cleanUrls: true,
  vite: {
    plugins: [
      monacoPlugin({
        publicPath: monacoPublicPath,
        customDistPath: (root, buildOutDir) => {
          const outDir = path.isAbsolute(buildOutDir) ? buildOutDir : path.join(root, buildOutDir)
          return path.join(outDir, monacoPublicPath)
        },
      })
    ]
  },
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
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Curriculum', items: navData.nav || [] }
    ],
    search: { provider: 'local' },
    sidebar: navData.sidebar,
  },
})
