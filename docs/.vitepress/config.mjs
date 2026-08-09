import { withMermaid } from 'vitepress-plugin-mermaid'
import navData from './navigation_map.json'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'
import path from 'node:path'
import { javaLspBridgePlugin } from './dev/javaLspBridge.mjs'
import { runJavaLocally } from './dev/javaRunner.mjs'

const monacoPlugin = monacoEditorPlugin.default ? monacoEditorPlugin.default : monacoEditorPlugin
const monacoPublicPath = 'monacoeditorwork'
const JAVA_RUNNER_ENABLED = process.env.VITE_ENABLE_JAVA_RUNNER === '1'

function readJsonBody(req) {
  return new Promise((resolve, reject) => {
    let body = ''
    req.on('data', (chunk) => {
      body += chunk
      if (body.length > 512 * 1024) {
        reject(new Error('Request body is too large.'))
        req.destroy()
      }
    })
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {})
      } catch (err) {
        reject(new Error('Invalid JSON body.'))
      }
    })
    req.on('error', reject)
  })
}

function sendJson(res, status, data) {
  res.statusCode = status
  res.setHeader('content-type', 'application/json')
  res.end(JSON.stringify(data))
}

function localJavaRunnerPlugin() {
  return {
    name: 'lms-local-java-runner',
    configureServer(server) {
      server.middlewares.use('/api/run-java', async (req, res, next) => {
        if (req.method !== 'POST') return next()
        try {
          const body = await readJsonBody(req)
          const result = await runJavaLocally(body)
          sendJson(res, 200, result)
        } catch (err) {
          sendJson(res, 400, { ok: false, phase: 'request', stdout: '', stderr: '', error: err.message })
        }
      })
    },
  }
}

export default withMermaid({
  title: 'SDE-2 Mastery',
  description: 'Study Plan & Backend Fundamentals',
  srcDir: '../src/main/java/org/example/backend_fundamentals',
  srcExclude: ['**/*.java', '**/target/**'],
  ignoreDeadLinks: false,
  cleanUrls: true,
  vite: {
    server: { host: true },
    plugins: [
      JAVA_RUNNER_ENABLED && localJavaRunnerPlugin(),
      javaLspBridgePlugin(),
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
