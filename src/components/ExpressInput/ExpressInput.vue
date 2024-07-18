<script setup lang="ts">
import { computed, inject, type Ref } from 'vue'
import { INPUT_DATA_FLAT } from '@/symbols'
import { useVueFlow } from '@vue-flow/core'
import { debounce } from 'lodash'
import { Editor, EditorContent } from '@tiptap/vue-3'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Text from '@tiptap/extension-text'
import Mention from '@tiptap/extension-mention'
import suggestion from './suggestion'

interface ExpressInputProps {
  modelValue?: string
  placeholder?: string
  allowClear?: boolean
  type?: 'input' | 'textarea' | undefined
}

const props = withDefaults(defineProps<ExpressInputProps>(), {
  allowClear: true,
  type: 'input'
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: string): void
}>()

const { findNode } = useVueFlow()
const data = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value as string)
  }
})

//----------------------- 处理提及  --------------------------------
const inputDataFlat = inject<Ref<Record<string, any>>>(INPUT_DATA_FLAT)
const prefix = '$.'
const expressRegexStr = /^\$\{(.*)}$/

const inputDataKeys = computed(() => {
  return Object.keys(inputDataFlat?.value || {})
})
//处理样式
const expressClassName = computed<string>(() => {
  const dataValue = data.value || ''
  if (inputDataKeys?.value.includes(dataValue.replace(prefix, '') || '')) {
    return 'jsonpath'
  } else if (dataValue.match(expressRegexStr)) {
    return 'expression'
  } else {
    return ''
  }
})

const searchOptions = ref<string[]>()

function doSearch(value: string, prefix: string) {
  searchOptions.value = inputDataKeys?.value.filter(
    (key) => key.indexOf(value.replace(prefix, '')) > -1
  )
}

const debounceSearch = debounce(doSearch, 300)

function handleSearch(value: string, prefix: string) {
  debounceSearch(value, prefix)
}

const nodeIdRegex = /inputData\.(.+?)[\\.[]/
const descData = computed(() => {
  const dataValue = data.value
  const nodeIdMatch = dataValue?.match(nodeIdRegex)
  const dataKey = dataValue?.replace(prefix, '')
  if (nodeIdMatch) {
    const nodeId = nodeIdMatch[1]
    const node = findNode(nodeId)
    return [
      { label: 'node', value: node?.label },
      { label: 'nodeId', value: nodeId },
      { label: 'value', value: inputDataFlat?.value[dataKey || ''] }
    ]
  } else {
    return []
  }
})

const popoverVariable = computed(() => expressClassName.value === 'jsonpath')

const editor = new Editor({
  extensions: [
    Document,
    Paragraph,
    Text,
    Mention.configure({
      deleteTriggerWithBackspace: false,
      HTMLAttributes: {
        class: 'mention'
      },
      suggestion
    })
  ],
  onUpdate: ({ editor }) => {
    data.value = editor.getText()
  },
  content: data.value
})

onBeforeUnmount(() => {
  editor.destroy()
})
</script>

<template>
  <div class="express-input" :class="expressClassName">
    <EditorContent class="editor-content" :editor="editor" />
    <AMention
      :allow-clear="props.allowClear"
      :type="props.type"
      :placeholder="props.placeholder"
      v-model="data"
      :prefix="prefix"
      @search="handleSearch"
      :data="searchOptions"
    />
    <div v-if="popoverVariable && descData" class="jsonpath-desc">
      <ADescriptions :data="descData" size="mini" :column="1"></ADescriptions>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import 'express-input';
</style>
