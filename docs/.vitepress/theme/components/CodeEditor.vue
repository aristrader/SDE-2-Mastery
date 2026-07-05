<template>
  <div class="code-editor">
    <VueMonacoEditor
      :value="modelValue"
      language="java"
      :theme="isDark ? 'vs-dark' : 'vs'"
      :options="{ readOnly: readonly, automaticLayout: true, minimap: { enabled: false } }"
      @mount="onMount"
      @change="onChange"
    />
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount, shallowRef } from 'vue'
import { VueMonacoEditor } from '@guolao/vue-monaco-editor'
import { useData } from 'vitepress'

const props = defineProps({
  modelValue: { type: String, default: '' },
  readonly: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue'])

const { isDark } = useData()
const editorRef = shallowRef()

const onMount = (editor, monaco) => {
  editorRef.value = editor
}

const onChange = (value) => {
  emit('update:modelValue', value)
}

watch(() => props.modelValue, (val) => {
  if (editorRef.value && val !== editorRef.value.getValue()) {
    editorRef.value.setValue(val)
  }
})

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.dispose()
  }
})
</script>

<style scoped>
.code-editor { border: 1px solid var(--vp-c-divider); border-radius: 8px; overflow: hidden; height: 65vh; max-height: 75vh; }
</style>
