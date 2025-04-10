<script lang="ts" setup>
import { computed } from 'vue'
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
  allowClear: true
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: string): void
}>()

const data = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value as string)
  }
})

const inputType = computed(() => {
  if (!props.type) {
    return (data.value?.length || 0) > 50 ? 'textarea' : 'input'
  }
  return props.type || 'input'
})

//----------------------- 处理提及  --------------------------------
const expressRegexStr = /^\$\{(.*)}$/
const jsonPathRegexStr = /^\$((\.\w+)|(\['[^']+'])|(\[\d+])|(\[\*]))*$/

const { selectOptions } = useSelectOptions()

//处理样式
const expressClassName = computed<string>(() => {
  const dataValue = (data.value || '').trimEnd()
  if (dataValue.match(jsonPathRegexStr)) {
    return 'jsonpath'
  } else if (dataValue.match(expressRegexStr)) {
    return 'expression'
  } else {
    return ''
  }
})

const { editor, isFocused } = useTipTapEditor({
  selectOptions,
  data,
  placeholder: ''
})

</script>
<template>
  <div
    :class="[expressClassName, isFocused ? 'express-input-focus' : '', props.type || 'input']"
    class="express-input"
    @click="() => editor?.commands.focus()"
  >
    <slot name="top" />
    <EditorContent
      :class="[expressClassName, `${inputType}-type`]"
      :editor="editor"
      class="editor-content"
    />
  </div>
</template>

<style lang="scss" scoped>
@use 'express-input';
</style>