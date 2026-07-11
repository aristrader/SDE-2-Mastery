<template>
  <div class="code-editor">
    <VueMonacoEditor
      :value="modelValue"
      language="java"
      :theme="isDark ? 'vs-dark' : 'vs'"
      :options="editorOptions"
      @mount="onMount"
      @change="onChange"
    />
  </div>
</template>

<script setup>
import { computed, watch, onBeforeUnmount, shallowRef } from 'vue'
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'
import { useData } from 'vitepress'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readonly: { type: Boolean, default: false },
  completionWords: { type: Array, default: () => [] },
  diagnostics: { type: Array, default: () => [] },
  semanticCompletionProvider: { type: Function, default: null },
  semanticCompletionResolver: { type: Function, default: null },
  semanticHoverProvider: { type: Function, default: null },
  semanticDefinitionProvider: { type: Function, default: null },
  semanticFormatProvider: { type: Function, default: null },
  semanticCodeActionProvider: { type: Function, default: null },
})
const emit = defineEmits(['update:modelValue', 'run'])

const { isDark } = useData()
const editorRef = shallowRef()
const monacoRef = shallowRef()
let providerDisposables = []

const editorOptions = computed(() => ({
  readOnly: props.readonly,
  automaticLayout: true,
  minimap: { enabled: false },
  tabSize: 4,
  insertSpaces: true,
  detectIndentation: false,
  autoIndent: 'full',
  formatOnPaste: true,
  formatOnType: true,
  wordWrap: 'on',
  quickSuggestions: true,
  suggestOnTriggerCharacters: true,
  suggest: {
    selectionMode: 'always',
    snippetsPreventQuickSuggestions: false,
    showWords: false,
  },
  snippetSuggestions: 'bottom',
  autoClosingBrackets: 'always',
  autoClosingQuotes: 'always',
  fontSize: 14,
  lineHeight: 22,
  'bracketPairColorization.enabled': true,
}))

const javaKeywords = [
  'class', 'interface', 'enum', 'record', 'public', 'private', 'protected',
  'static', 'final', 'return', 'new', 'void', 'int', 'boolean', 'String',
  'if', 'else', 'for', 'while', 'try', 'catch', 'throw', 'throws',
]

const javaSnippets = [
  ['psvm', 'public static void main(String[] args) {\n    $0\n}'],
  ['sout', 'System.out.println($0);'],
  ['fori', 'for (int ${1:i} = 0; ${1:i} < ${2:size}; ${1:i}++) {\n    $0\n}'],
  ['fore', 'for (${1:String} ${2:item} : ${3:items}) {\n    $0\n}'],
  ['trycatch', 'try {\n    $0\n} catch (${1:Exception} ${2:e}) {\n    ${2:e}.printStackTrace();\n}'],
]

function classNamesFrom(text) {
  return [...text.matchAll(/\b(?:class|interface|enum|record)\s+([A-Z][A-Za-z0-9_$]*)/g)].map(match => match[1])
}

function completionRange(model, position) {
  const word = model.getWordUntilPosition(position)
  return {
    startLineNumber: position.lineNumber,
    endLineNumber: position.lineNumber,
    startColumn: word.startColumn,
    endColumn: word.endColumn,
  }
}

function previousText(model, position, chars = 1) {
  return model.getValueInRange({
    startLineNumber: position.lineNumber,
    startColumn: Math.max(1, position.column - chars),
    endLineNumber: position.lineNumber,
    endColumn: position.column,
  })
}

function fallbackSort(prefix, label, offset) {
  const lowerPrefix = prefix.toLowerCase()
  const lowerLabel = String(label).toLowerCase()
  const match = lowerPrefix && lowerLabel.startsWith(lowerPrefix) ? '1' : '9'
  return `z${match}${String(offset).padStart(3, '0')}`
}

function registerJavaCompletions(monaco) {
  providerDisposables.forEach(item => item.dispose())
  providerDisposables = []
  providerDisposables.push(monaco.languages.registerCompletionItemProvider('java', {
    async provideCompletionItems(model, position) {
      const range = completionRange(model, position)
      const word = model.getWordUntilPosition(position)
      const isMemberAccess = previousText(model, position, 1) === '.'
      const classWords = new Set([...props.completionWords, ...classNamesFrom(model.getValue())])
      let semantic = []
      if (props.semanticCompletionProvider) {
        try {
          semantic = await props.semanticCompletionProvider(model, position, range, monaco) || []
        } catch {
          semantic = []
        }
      }
      const local = isMemberAccess ? [] : [
        ...javaKeywords.map((label, index) => ({
          label,
          kind: monaco.languages.CompletionItemKind.Keyword,
          insertText: label,
          range,
          sortText: fallbackSort(word.word, label, index),
        })),
        ...javaSnippets.map(([label, insertText], index) => ({
          label,
          kind: monaco.languages.CompletionItemKind.Snippet,
          insertText,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          range,
          sortText: fallbackSort(word.word, label, index + 100),
        })),
        ...[...classWords].map((label, index) => ({
          label,
          kind: monaco.languages.CompletionItemKind.Class,
          insertText: label,
          range,
          sortText: fallbackSort(word.word, label, index + 200),
        })),
      ]
      return {
        suggestions: [...semantic, ...local],
      }
    },
    resolveCompletionItem(item) {
      return props.semanticCompletionResolver ? props.semanticCompletionResolver(item, monaco) : item
    },
  }))
  if (props.semanticHoverProvider) {
    providerDisposables.push(monaco.languages.registerHoverProvider('java', {
      provideHover: (model, position) => props.semanticHoverProvider(model, position, monaco),
    }))
  }
  if (props.semanticDefinitionProvider) {
    providerDisposables.push(monaco.languages.registerDefinitionProvider('java', {
      provideDefinition: (model, position) => props.semanticDefinitionProvider(model, position, monaco),
    }))
  }
  if (props.semanticFormatProvider) {
    providerDisposables.push(monaco.languages.registerDocumentFormattingEditProvider('java', {
      provideDocumentFormattingEdits: (model) => props.semanticFormatProvider(model, monaco),
    }))
  }
  if (props.semanticCodeActionProvider) {
    providerDisposables.push(monaco.languages.registerCodeActionProvider('java', {
      provideCodeActions: (model, range) => props.semanticCodeActionProvider(model, range, monaco),
    }))
  }
}

function applyDiagnostics() {
  const editor = editorRef.value
  const monaco = monacoRef.value
  const model = editor?.getModel()
  if (!monaco || !model) return
  const markers = props.diagnostics.map(item => ({
    startLineNumber: item.line || 1,
    endLineNumber: item.line || 1,
    startColumn: item.column || 1,
    endColumn: (item.column || 1) + 1,
    message: item.message || 'Java compile error',
    severity: item.severity === 'warning' ? monaco.MarkerSeverity.Warning : monaco.MarkerSeverity.Error,
  }))
  monaco.editor.setModelMarkers(model, 'java-runner', markers)
}

const onMount = (editor, monaco) => {
  editorRef.value = editor
  monacoRef.value = monaco
  registerJavaCompletions(monaco)
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => emit('run'))
  editor.addCommand(monaco.KeyMod.Shift | monaco.KeyMod.Alt | monaco.KeyCode.KeyF, () => {
    editor.getAction('editor.action.formatDocument')?.run()
  })
  applyDiagnostics()
}

const onChange = (value) => {
  emit('update:modelValue', value)
}

watch(() => props.modelValue, (val) => {
  if (editorRef.value && val !== editorRef.value.getValue()) {
    editorRef.value.setValue(val)
  }
  applyDiagnostics()
})

watch(() => props.diagnostics, applyDiagnostics, { deep: true })

function focusLine(line) {
  if (!editorRef.value || !line) return
  editorRef.value.setPosition({ lineNumber: line, column: 1 })
  editorRef.value.revealLineInCenter(line)
  editorRef.value.focus()
}

defineExpose({ focusLine })

onBeforeUnmount(() => {
  providerDisposables.forEach(item => item.dispose())
  providerDisposables = []
  if (editorRef.value) {
    const model = editorRef.value.getModel()
    if (model) monacoRef.value?.editor.setModelMarkers(model, 'java-runner', [])
    editorRef.value.getModel()?.dispose()
    editorRef.value.dispose()
  }
})
</script>

<style scoped>
.code-editor { border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; height: 65vh; max-height: 75vh; }
</style>
