<template>
  <div ref="host" class="code-editor"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { EditorView, basicSetup } from 'codemirror'
import { EditorState } from '@codemirror/state'
import { java } from '@codemirror/lang-java'
import { oneDark } from '@codemirror/theme-one-dark'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readonly: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue'])

const host = ref(null)
let view = null

function makeState(doc) {
  return EditorState.create({
    doc,
    extensions: [
      basicSetup,
      java(),
      oneDark,
      EditorView.editable.of(!props.readonly),
      EditorView.updateListener.of((v) => {
        if (v.docChanged) emit('update:modelValue', v.state.doc.toString())
      }),
    ],
  })
}

onMounted(() => {
  view = new EditorView({ state: makeState(props.modelValue), parent: host.value })
})

// Reset the document when the selected file changes from the parent.
watch(() => props.modelValue, (val) => {
  if (view && val !== view.state.doc.toString()) {
    view.setState(makeState(val))
  }
})

onBeforeUnmount(() => view && view.destroy())
</script>

<style scoped>
.code-editor { border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; }
.code-editor :deep(.cm-editor) { max-height: 480px; }
.code-editor :deep(.cm-scroller) { overflow: auto; }
</style>
