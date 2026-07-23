import fs from 'node:fs'
import os from 'node:os'
import path from 'node:path'
import { execFile } from 'node:child_process'

const RUN_TIMEOUT_MS = 10000

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

function parseJavacDiagnostics(stderr, fileNamesByPath) {
  const lines = String(stderr || '').split(/\r?\n/)
  const diagnostics = []
  for (let i = 0; i < lines.length; i++) {
    const match = lines[i].match(/^(.+\.java):(\d+):\s*(?:(error|warning):\s*)?(.*)$/)
    if (!match) continue
    const [, rawPath, line, severity = 'error', message] = match
    const caret = lines[i + 2]?.match(/^(\s*)\^/)
    diagnostics.push({
      file: fileNamesByPath.get(path.resolve(rawPath)) || path.basename(rawPath),
      line: Number(line),
      column: caret ? caret[1].length + 1 : 1,
      severity,
      message: message || lines[i],
    })
  }
  return diagnostics
}

export async function runJavaLocally({ mainClass, files }) {
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
    const fileNamesByPath = new Map()
    for (const file of files) {
      const content = String(file.content ?? '')
      if (content.length > 100_000) throw new Error(`${file.name} is too large to run.`)
      const targetDir = path.join(srcDir, ...packagePath(content))
      fs.mkdirSync(targetDir, { recursive: true })
      const targetFile = path.join(targetDir, safeJavaName(file.name))
      fs.writeFileSync(targetFile, content, 'utf8')
      sourceFiles.push(targetFile)
      fileNamesByPath.set(path.resolve(targetFile), file.name)
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
        diagnostics: parseJavacDiagnostics(compile.stderr, fileNamesByPath),
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
