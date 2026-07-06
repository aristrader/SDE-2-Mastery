import { withMermaid } from 'vitepress-plugin-mermaid'
import navData from './navigation_map.json'
import monacoEditorPlugin from 'vite-plugin-monaco-editor'
import path from 'node:path'
import fs from 'node:fs'
import os from 'node:os'
import { execFile } from 'node:child_process'

const monacoPlugin = monacoEditorPlugin.default ? monacoEditorPlugin.default : monacoEditorPlugin
const monacoPublicPath = 'monacoeditorwork'
const RUN_TIMEOUT_MS = 10000

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

function execFileAsync(command, args, options) {
  return new Promise((resolve) => {
    execFile(command, args, options, (error, stdout, stderr) => {
      resolve({
        error,
        stdout: stdout || '',
        stderr: stderr || '',
        timedOut: error?.killed === true || error?.signal === 'SIGTERM',
      })
    })
  })
}

function packagePath(content) {
  const match = String(content).match(/^\s*package\s+([\w.]+)\s*;/m)
  return match ? match[1].split('.') : []
}

function safeJavaName(name) {
  if (!/^[A-Za-z_$][\w$]*\.java$/.test(name || '')) {
    throw new Error(`Invalid Java file name: ${name}`)
  }
  return name
}

async function runJavaLocally({ mainClass, files }) {
  if (!/^[A-Za-z_$][\w$]*(\.[A-Za-z_$][\w$]*)*$/.test(mainClass || '')) {
    throw new Error('mainClass is required and must be a fully-qualified Java class name.')
  }
  if (!Array.isArray(files) || files.length === 0) {
    throw new Error('At least one Java file is required.')
  }
  if (files.length > 40) {
    throw new Error('Too many files in one run.')
  }

  const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'lms-java-run-'))
  const srcDir = path.join(tempDir, 'src')
  const classesDir = path.join(tempDir, 'classes')
  fs.mkdirSync(srcDir, { recursive: true })
  fs.mkdirSync(classesDir, { recursive: true })

  try {
    const sourceFiles = []
    for (const file of files) {
      const content = String(file.content ?? '')
      if (content.length > 100_000) throw new Error(`${file.name} is too large to run.`)
      const targetDir = path.join(srcDir, ...packagePath(content))
      fs.mkdirSync(targetDir, { recursive: true })
      const targetFile = path.join(targetDir, safeJavaName(file.name))
      fs.writeFileSync(targetFile, content, 'utf8')
      sourceFiles.push(targetFile)
    }

    const compile = await execFileAsync('javac', ['-d', classesDir, ...sourceFiles], {
      timeout: RUN_TIMEOUT_MS,
      maxBuffer: 1024 * 1024,
    })
    if (compile.error) {
      return {
        ok: false,
        phase: 'compile',
        stdout: compile.stdout,
        stderr: compile.stderr,
        error: compile.timedOut ? 'Compilation timed out.' : compile.error.message,
      }
    }

    const run = await execFileAsync('java', ['-cp', classesDir, mainClass], {
      timeout: RUN_TIMEOUT_MS,
      maxBuffer: 1024 * 1024,
    })
    return {
      ok: !run.error,
      phase: 'run',
      stdout: run.stdout,
      stderr: run.stderr,
      error: run.error ? (run.timedOut ? 'Execution timed out.' : run.error.message) : '',
    }
  } finally {
    fs.rmSync(tempDir, { recursive: true, force: true })
  }
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
    plugins: [
      localJavaRunnerPlugin(),
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
