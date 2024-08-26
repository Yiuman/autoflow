<script setup lang="ts">
import { computed } from 'vue'
import { useVueFlow } from '@vue-flow/core'
import { EditorContent } from '@tiptap/vue-3'
import { useSelectOptions } from '@/components/ExpressInput/useSelectOptions'
import { useTipTapEditor } from '@/components/ExpressInput/useTiptapEditor'

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

const inputType = computed(() => {
  return props.type || 'input'
})

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
const prefix = '$.'
const expressRegexStr = /^\$\{(.*)}$/

const { selectOptions } = useSelectOptions()

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

const { editor, isFocused } = useTipTapEditor(selectOptions, data)
</script>
<template>
  <div
    class="express-input"
    @click="() => editor?.commands.focus()"
    :class="[expressClassName, isFocused ? 'express-input-focus' : '']"
  >
    <EditorContent
      class="editor-content"
      :class="[expressClassName, `${inputType}-type`]"
      :editor="editor"
    />
    <div v-if="popoverVariable && descData" class="jsonpath-desc">
      <ADescriptions :data="descData" size="mini" :column="1"></ADescriptions>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@import 'express-input';
</style>
