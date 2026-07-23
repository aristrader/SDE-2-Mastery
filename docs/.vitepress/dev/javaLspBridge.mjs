import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { spawn, spawnSync } from 'node:child_process'

const ENABLED = process.env.VITE_ENABLE_JAVA_LSP === '1'
const COMMAND = process.env.JAVA_LSP_COMMAND || 'jdtls'
const MAX_SESSIONS = 2
const MAX_FILES = 40
const MAX_FILE_BYTES = 100_000
const IDLE_MS = 5 * 60 * 1000
const CLIENT_STALE_MS = 15_000

function safeJavaName(name) {
  if (!/^[A-Za-z_$][\w$]*\.java$/.test(name || '')) throw new Error(`Invalid Java file name: ${name}`)
  return name
}

export function assertUniqueJavaNames(files) {
  const seen = new Set()
  for (const file of files || []) {
    const name = safeJavaName(file?.name)
    if (seen.has(name)) throw new Error(`Duplicate Java file name: ${name}`)
    seen.add(name)
  }
}

function packagePath(content) {
  const match = String(content).match(/^\s*package\s+([\w.]+)\s*;/m)
  if (!match) return []
  return match[1].split('.').map(part => {
    if (!/^[A-Za-z_$][\w$]*$/.test(part)) throw new Error(`Invalid package part: ${part}`)
    return part
  })
}

function uriFor(file) {
  return `file://${file.absPath}`
}

function remapUris(value, session) {
  if (Array.isArray(value)) return value.map(item => remapUris(item, session))
  if (!value || typeof value !== 'object') return value
  const mapped = {}
  for (const [key, item] of Object.entries(value)) {
    if (key === 'uri' && typeof item === 'string') {
      const name = decodeURIComponent(item.split('/').pop() || '')
      mapped[key] = session.docsByName.get(name)?.uri || item
    } else {
      mapped[key] = remapUris(item, session)
    }
  }
  return mapped
}

function sendRpc(proc, message) {
  const json = JSON.stringify(message)
  proc.stdin.write(`Content-Length: ${Buffer.byteLength(json, 'utf8')}\r\n\r\n${json}`)
}

function readRpc(stream, onMessage) {
  let buffer = Buffer.alloc(0)
  stream.on('data', chunk => {
    buffer = Buffer.concat([buffer, chunk])
    while (true) {
      const headerEnd = buffer.indexOf('\r\n\r\n')
      if (headerEnd === -1) return
      const header = buffer.slice(0, headerEnd).toString('utf8')
      const match = header.match(/Content-Length:\s*(\d+)/i)
      if (!match) {
        buffer = buffer.slice(headerEnd + 4)
        continue
      }
      const length = Number(match[1])
      const bodyStart = headerEnd + 4
      if (buffer.length < bodyStart + length) return
      const body = buffer.slice(bodyStart, bodyStart + length).toString('utf8')
      buffer = buffer.slice(bodyStart + length)
      try {
        onMessage(JSON.parse(body))
      } catch {
        // Ignore malformed LSP output; the process will keep streaming valid frames.
      }
    }
  })
}

function splitCommand(command) {
  const parts = command.split(/\s+/).filter(Boolean)
  return { bin: parts[0], args: parts.slice(1) }
}

function writeWorkspace(files) {
  if (!Array.isArray(files) || files.length === 0) throw new Error('At least one Java file is required.')
  if (files.length > MAX_FILES) throw new Error('Too many Java files for LSP.')
  assertUniqueJavaNames(files)
  const root = fs.mkdtempSync(path.join(os.tmpdir(), 'lms-java-lsp-'))
  const sourceRoot = path.join(root, 'src')
  const workspaceRoot = path.join(root, 'workspace')
  const configRoot = path.join(root, 'configuration')
  fs.mkdirSync(sourceRoot, { recursive: true })
  fs.mkdirSync(workspaceRoot, { recursive: true })
  fs.mkdirSync(configRoot, { recursive: true })
  fs.mkdirSync(path.join(sourceRoot, 'bin'), { recursive: true })
  fs.writeFileSync(path.join(sourceRoot, '.project'), `<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
  <name>lms-java-lsp</name>
  <buildSpec>
    <buildCommand>
      <name>org.eclipse.jdt.core.javabuilder</name>
    </buildCommand>
  </buildSpec>
  <natures>
    <nature>org.eclipse.jdt.core.javanature</nature>
  </natures>
</projectDescription>
`, 'utf8')
  fs.writeFileSync(path.join(sourceRoot, '.classpath'), `<?xml version="1.0" encoding="UTF-8"?>
<classpath>
  <classpathentry kind="src" path=""/>
  <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
  <classpathentry kind="output" path="bin"/>
</classpath>
`, 'utf8')

  const docs = []
  for (const file of files) {
    const content = String(file.content ?? '')
    if (Buffer.byteLength(content, 'utf8') > MAX_FILE_BYTES) throw new Error(`${file.name} is too large for LSP.`)
    const dir = path.join(sourceRoot, ...packagePath(content))
    fs.mkdirSync(dir, { recursive: true })
    const absPath = path.join(dir, safeJavaName(file.name))
    fs.writeFileSync(absPath, content, 'utf8')
    docs.push({ name: file.name, content, absPath, uri: `file://${absPath}`, version: 1 })
  }
  return { root, sourceRoot, workspaceRoot, configRoot, docs }
}

function writeDoc(session, name, content) {
  const dir = path.join(session.sourceRoot, ...packagePath(content))
  fs.mkdirSync(dir, { recursive: true })
  const absPath = path.join(dir, safeJavaName(name))
  fs.writeFileSync(absPath, content, 'utf8')
  const doc = {
    name,
    content,
    absPath,
    uri: `file://${absPath}`,
    version: 1,
  }
  session.docsByName.set(name, doc)
  session.namesByUri.set(doc.uri, name)
  return doc
}

function killSession(session) {
  clearTimeout(session.idleTimer)
  clearTimeout(session.clientTimer)
  killMatchingWorkspace(session.workspaceRoot, 'TERM')
  if (session.proc && !session.proc.killed) {
    try {
      process.kill(-session.proc.pid, 'SIGTERM')
    } catch {
      session.proc.kill('SIGTERM')
    }
  }
  setTimeout(() => {
    killMatchingWorkspace(session.workspaceRoot, 'KILL')
    if (session.proc && !session.proc.killed) {
      try {
        process.kill(-session.proc.pid, 'SIGKILL')
      } catch {
        session.proc.kill('SIGKILL')
      }
    }
    cleanupTemp(session.root)
  }, 1500).unref?.()
  cleanupTemp(session.root)
}

function cleanupTemp(dir, attempt = 0) {
  try {
    fs.rmSync(dir, { recursive: true, force: true, maxRetries: 2, retryDelay: 100 })
  } catch {
    if (attempt < 5) {
      setTimeout(() => cleanupTemp(dir, attempt + 1), 500).unref?.()
    }
  }
}

function killMatchingWorkspace(workspaceRoot, signal) {
  if (!workspaceRoot) return
  spawnSync('pkill', [`-${signal}`, '-f', workspaceRoot], { stdio: 'ignore' })
}

function readJsonBody(req) {
  return new Promise((resolve) => {
    let body = ''
    req.on('data', chunk => {
      body += chunk
      if (body.length > 32 * 1024) req.destroy()
    })
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {})
      } catch {
        resolve({})
      }
    })
    req.on('error', () => resolve({}))
  })
}

export function javaLspBridgePlugin() {
  const sessions = new Map()
  let viteServer = null
  const hookKey = Symbol.for('lms-java-lsp-process-hooks')

  function emit(event, data) {
    viteServer?.ws.send(event, data)
  }

  function touch(session) {
    clearTimeout(session.idleTimer)
    session.idleTimer = setTimeout(() => {
      sessions.delete(session.id)
      emit('java-lsp:status', { sessionId: session.id, status: 'stopped', message: 'LSP idle timeout.' })
      killSession(session)
    }, IDLE_MS)
  }

  function touchClient(session) {
    clearTimeout(session.clientTimer)
    session.clientTimer = setTimeout(() => {
      sessions.delete(session.id)
      emit('java-lsp:status', { sessionId: session.id, status: 'stopped', message: 'LSP client disconnected.' })
      killSession(session)
    }, CLIENT_STALE_MS)
  }

  function close(sessionId, reason = 'closed') {
    const session = sessions.get(sessionId)
    if (!session) return
    sessions.delete(sessionId)
    killSession(session)
    emit('java-lsp:status', { sessionId, status: 'stopped', message: reason })
  }

  function closeAll(reason = 'closed') {
    for (const id of [...sessions.keys()]) close(id, reason)
  }

  async function start(data) {
    if (!ENABLED) throw new Error('Java LSP is disabled. Set VITE_ENABLE_JAVA_LSP=1.')
    if (!String(data.route || '').startsWith('/java/')) throw new Error('Java LSP is limited to Java study pages.')
    if (sessions.size >= MAX_SESSIONS && !sessions.has(data.sessionId)) {
      close(sessions.keys().next().value, 'replaced by newer LSP session')
    }
    close(data.sessionId, 'restarting')

    const workspace = writeWorkspace(data.files)
    const command = splitCommand(COMMAND)
    const proc = spawn(command.bin, [...command.args, '-configuration', workspace.configRoot, '-data', workspace.workspaceRoot], {
      cwd: workspace.sourceRoot,
      stdio: ['pipe', 'pipe', 'pipe'],
      env: { ...process.env },
      detached: true,
    })
    const session = {
      id: data.sessionId,
      proc,
      root: workspace.root,
      sourceRoot: workspace.sourceRoot,
      workspaceRoot: workspace.workspaceRoot,
      docsByName: new Map(workspace.docs.map(doc => [doc.name, doc])),
      namesByUri: new Map(workspace.docs.map(doc => [doc.uri, doc.name])),
      nextId: 1,
      pending: new Map(),
      initialized: false,
      idleTimer: null,
      clientTimer: null,
    }
    sessions.set(session.id, session)
    touch(session)
    touchClient(session)

    proc.on('error', err => emit('java-lsp:status', { sessionId: session.id, status: 'failed', message: err.message }))
    proc.on('exit', () => close(session.id, 'process exited'))
    proc.stderr.on('data', chunk => emit('java-lsp:log', { sessionId: session.id, message: String(chunk).slice(0, 2000) }))
    readRpc(proc.stdout, message => handleRpc(session, message))

    const init = await request(session, 'initialize', {
      processId: process.pid,
      rootUri: `file://${workspace.sourceRoot}`,
      capabilities: {
        textDocument: {
          synchronization: { didSave: true },
          completion: { contextSupport: true, completionItem: { snippetSupport: false, resolveSupport: { properties: ['detail', 'documentation', 'additionalTextEdits'] } } },
          hover: {},
          definition: {},
          formatting: {},
          codeAction: {},
          publishDiagnostics: {},
        },
        workspace: { applyEdit: true },
      },
    })
    session.initialized = true
    sendRpc(proc, { jsonrpc: '2.0', method: 'initialized', params: {} })
    for (const doc of workspace.docs) {
      sendRpc(proc, { jsonrpc: '2.0', method: 'textDocument/didOpen', params: { textDocument: { uri: doc.uri, languageId: 'java', version: doc.version, text: doc.content } } })
    }
    emit('java-lsp:status', { sessionId: session.id, status: 'ready', capabilities: init?.capabilities || {} })
  }

  function handleRpc(session, message) {
    touch(session)
    if (message.id && session.pending.has(message.id)) {
      const { resolve, reject } = session.pending.get(message.id)
      session.pending.delete(message.id)
      message.error ? reject(new Error(message.error.message || 'LSP request failed.')) : resolve(message.result)
      return
    }
    if (message.method === 'textDocument/publishDiagnostics') {
      const uri = message.params?.uri
      emit('java-lsp:diagnostics', {
        sessionId: session.id,
        file: session.namesByUri.get(uri) || path.basename(uri || ''),
        diagnostics: (message.params?.diagnostics || []).map(item => ({
          file: session.namesByUri.get(uri) || path.basename(uri || ''),
          line: (item.range?.start?.line ?? 0) + 1,
          column: (item.range?.start?.character ?? 0) + 1,
          severity: item.severity === 2 ? 'warning' : 'error',
          message: item.message || 'Java language-server diagnostic',
        })),
      })
      return
    }
    if (message.method === 'workspace/applyEdit') {
      sendRpc(session.proc, { jsonrpc: '2.0', id: message.id, result: { applied: false, failureReason: 'Browser applies edits.' } })
    }
  }

  function request(session, method, params) {
    const id = session.nextId++
    sendRpc(session.proc, { jsonrpc: '2.0', id, method, params })
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        session.pending.delete(id)
        reject(new Error(`${method} timed out.`))
      }, 10_000)
      session.pending.set(id, {
        resolve: value => {
          clearTimeout(timer)
          resolve(value)
        },
        reject: err => {
          clearTimeout(timer)
          reject(err)
        },
      })
    })
  }

  async function handleRequest(data) {
    const session = sessions.get(data.sessionId)
    if (!session?.initialized) throw new Error('Java LSP is not ready.')
    touch(session)
    return request(session, data.method, remapUris(data.params || {}, session))
  }

  function change(data) {
    const session = sessions.get(data.sessionId)
    if (!session?.initialized) return
    let doc = session.docsByName.get(data.file)
    if (typeof data.content !== 'string') return
    if (!doc) {
      doc = writeDoc(session, data.file, data.content)
      sendRpc(session.proc, {
        jsonrpc: '2.0',
        method: 'textDocument/didOpen',
        params: { textDocument: { uri: doc.uri, languageId: 'java', version: doc.version, text: doc.content } },
      })
      touch(session)
      return
    }
    doc.content = data.content
    doc.version += 1
    fs.writeFileSync(doc.absPath, doc.content, 'utf8')
    sendRpc(session.proc, {
      jsonrpc: '2.0',
      method: 'textDocument/didChange',
      params: {
        textDocument: { uri: doc.uri, version: doc.version },
        contentChanges: [{ text: doc.content }],
      },
    })
    sendRpc(session.proc, {
      jsonrpc: '2.0',
      method: 'textDocument/didSave',
      params: { textDocument: { uri: doc.uri }, text: doc.content },
    })
    touch(session)
  }

  return {
    name: 'lms-java-lsp-bridge',
    configureServer(server) {
      viteServer = server
      if (!process[hookKey]) {
        process[hookKey] = true
        process.once('SIGINT', () => {
          closeAll('process interrupted')
          process.exit(130)
        })
        process.once('SIGTERM', () => {
          closeAll('process terminated')
          process.exit(143)
        })
        process.once('exit', () => closeAll('process exited'))
      }
      server.middlewares.use('/api/java-lsp-stop', async (req, res, next) => {
        if (req.method !== 'POST') return next()
        const body = await readJsonBody(req)
        close(body.sessionId, 'page closed')
        res.statusCode = 204
        res.end()
      })
      server.ws.on('java-lsp:start', async data => {
        try {
          await start(data)
        } catch (err) {
          emit('java-lsp:status', { sessionId: data.sessionId, status: 'failed', message: err.message })
        }
      })
      server.ws.on('java-lsp:change', change)
      server.ws.on('java-lsp:heartbeat', data => {
        const session = sessions.get(data.sessionId)
        if (session) touchClient(session)
      })
      server.ws.on('java-lsp:request', async data => {
        try {
          const result = await handleRequest(data)
          emit('java-lsp:response', { sessionId: data.sessionId, requestId: data.requestId, result })
        } catch (err) {
          emit('java-lsp:response', { sessionId: data.sessionId, requestId: data.requestId, error: err.message })
        }
      })
      server.ws.on('java-lsp:stop', data => close(data.sessionId))
      server.httpServer?.on('close', () => {
        closeAll('server stopped')
      })
    },
  }
}
