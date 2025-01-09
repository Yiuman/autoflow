<script lang="ts" setup>
import { Codemirror } from 'vue-codemirror'
import { javascript } from '@codemirror/lang-javascript'
import { sql } from '@codemirror/lang-sql'
import { python } from '@codemirror/lang-python'
import { java } from '@codemirror/lang-java'
import { html } from '@codemirror/lang-html'
import { xml } from '@codemirror/lang-xml'
import { dracula, noctisLilac } from 'thememirror'
import { darkTheme } from '@/hooks/theme'
import { LanguageSupport } from '@codemirror/language'

interface Props {
  modelValue: string
  lang?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ''
})
const emits = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
const data = computed({
  get() {
    return props.modelValue
  },
  set(value: string) {
    emits('update:modelValue', value)
  }
})

const langMap: Record<string, LanguageSupport> = {
  sql: sql(),
  javascript: javascript(),
  java: java(),
  python: python(),
  html: html(),
  xml: xml()
}

const extensions = computed(() => {
  const theme = darkTheme.value ? dracula : noctisLilac
  if (props.lang) {
    const lang = langMap[props.lang] || langMap['xml']
    return [lang, theme]
  }
  return [theme]
})
</script>

<template>
  <Codemirror v-model="data" :extensions="extensions" :lang="'sql'" class="autoflow-code-input" />
</template>

<style lang="scss" scoped>
.autoflow-code-input {
  :deep(.cm-editor) {
    height: 300px;
    width: 100%;
    border-radius: var(--border-radius-small);
  }

  :deep(.cm-focused) {
    outline: 1px solid rgb(var(--primary-6));
  }
}
</style>