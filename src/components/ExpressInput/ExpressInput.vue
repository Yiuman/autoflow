<script setup lang="ts">
import { computed, inject, type Ref } from 'vue'
import { INCOMER_DATA } from '@/symbols'
import { useVueFlow } from '@vue-flow/core'
import { EditorContent, type JSONContent, useEditor } from '@tiptap/vue-3'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Text from '@tiptap/extension-text'
import Mention from '@tiptap/extension-mention'
import createMentionSuggestion from './suggestion'
import type { NodeFlatData } from '@/types/flow'
import { flatten } from 'lodash'
import { type Option } from '@/components/ExpressInput/MentionList.vue'

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
const nodeFlatDataArray = inject<Ref<NodeFlatData[]>>(INCOMER_DATA)
const prefix = '$.'
const expressRegexStr = /^\$\{(.*)}$/

const selectOptions = computed<Option[]>(() => {
  const nodeSelectOptions = nodeFlatDataArray?.value.map((nodeFlatData) => {
    const varKeys = Object.keys(nodeFlatData.variables)
      .map((varKey) => {
        if (!varKey) {
          return null
        }
        const value = nodeFlatData.variables[varKey]
        return {
          type: `${nodeFlatData.node.label}`,
          key: `$.variables.${nodeFlatData.node.id}.${varKey}`,
          label: varKey,
          value: value
        }
      })
      .filter((i) => i)

    const inputDataKeys = Object.keys(nodeFlatData.inputData)
      .map((varKey) => {
        if (!varKey) {
          return null
        }
        const value = nodeFlatData.inputData[varKey]
        return {
          type: `${nodeFlatData.node.label}`,
          key: `$.inputData.${nodeFlatData.node.id}${varKey}`,
          label: varKey,
          value: value
        }
      })
      .filter((i) => i)
    return [...varKeys, ...inputDataKeys]
  })
  return flatten(nodeSelectOptions) as Option[]
})

//处理样式
const expressClassName = computed<string>(() => {
  const dataValue = (data.value || '').trimEnd()
  if (selectOptions.value?.find((option) => option.key === dataValue)) {
    return 'jsonpath'
  } else if (dataValue.match(expressRegexStr)) {
    return 'expression'
  } else {
    return ''
  }
})

const nodeIdRegex = /inputData|variable\.(.+?)[\\.[]/
const descData = computed(() => {
  const dataValue = data.value
  const nodeIdMatch = dataValue?.match(nodeIdRegex)
  const dataKey = dataValue?.replace(prefix, '')

  if (nodeIdMatch) {
    const nodeId = nodeIdMatch[1]
    const node = findNode(nodeId)
    const findValue = selectOptions.value.filter((option) => {
      return dataKey === option.key
    })[0]
    return [
      { label: 'node', value: node?.label },
      { label: 'nodeId', value: nodeId },
      { label: 'value', value: findValue?.value }
    ]
  } else {
    return []
  }
})

const popoverVariable = computed(() => expressClassName.value === 'jsonpath')

const suggestion = createMentionSuggestion({
  char: prefix,
  items: async ({ query }: { query: string }) => {
    return selectOptions.value?.filter(
      (option) => option?.key?.indexOf(query.replace(prefix, '')) > -1
    )
  }
})

function convertToJSONContent() {
  const docJSONContent: JSONContent[] = (data.value || '')
    .split(' ')
    .filter((item) => item)
    .map((item) => {
      const findOption = selectOptions.value?.find((option) => option.key === item)
      if (findOption) {
        return {
          type: 'mention',
          attrs: {
            id: {
              type: `${findOption.type}`,
              key: `${findOption.key}`,
              label: `${findOption.label}`,
              value: null
            }
          }
        }
      }
      return {
        type: 'text',
        text: item
      }
    })
  return {
    type: 'doc',
    content: [
      {
        type: 'paragraph',
        content: docJSONContent
      }
    ]
  }
}

const editor = useEditor({
  extensions: [
    Document,
    Paragraph,
    Text,
    Mention.configure({
      HTMLAttributes: {
        class: 'mention'
      },
      deleteTriggerWithBackspace: false,
      renderText({ node }) {
        const optionValue = node.attrs.id
        return optionValue.key
      },
      renderHTML({ node }) {
        const optionValue = node.attrs.id
        if (!optionValue) {
          return ''
        }
        const innerHTML = [
          [
            'span',
            {
              class: 'node-mention-type'
            },
            optionValue.type
          ],
          [
            'span',
            {
              class: 'node-mention-label'
            },
            optionValue.label
          ]
        ]
        return [
          'span',
          {
            class: 'mention',
            id: optionValue.key,
            datatype: 'mention',
            'data-id': optionValue.key,
            'data-value': optionValue.value
          },
          ...innerHTML
        ]
      },
      suggestion
    })
  ],
  onUpdate: ({ editor }) => {
    const jsonData = editor.getJSON()
    const valueArray = jsonData?.content?.[0]?.content
    data.value = valueArray
      ?.map((contentItem: JSONContent) => {
        if (contentItem.type === 'mention') {
          return contentItem?.attrs?.id.key
        } else {
          return contentItem.text
        }
      })
      .join(' ')
  },
  content: convertToJSONContent()
})

watch(
  () => props.modelValue,
  () => {
    editor.value?.commands.setContent(convertToJSONContent(), false)
  }
)
</script>

<template>
  <div class="express-input" :class="expressClassName">
    <EditorContent class="editor-content" :editor="editor" />
    <div v-if="popoverVariable && descData" class="jsonpath-desc">
      <ADescriptions :data="descData" size="mini" :column="1"></ADescriptions>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import 'express-input';
</style>
