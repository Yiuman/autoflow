<script setup lang="ts">
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
  allowClear: true,
  type: 'input'
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: string): void
}>()

const inputType = computed(() => {
  return props.type || 'input'
})

const data = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value as string)
  }
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
  placeholder: '$.插入变量、${}输入表达式'
})
</script>
<template>
  <div
    class="express-input"
    @click="() => editor?.commands.focus()"
    :class="[expressClassName, isFocused ? 'express-input-focus' : '', props.type || 'input']"
  >
    <slot name="top" />
    <EditorContent
      class="editor-content"
      :class="[expressClassName, `${inputType}-type`]"
      :editor="editor"
    />
    <!--    <div v-if="popoverVariable && descData" class="jsonpath-desc">-->
    <!--      <ADescriptions :data="descData" size="mini" :column="1" />-->
    <!--    </div>-->
  </div>
</template>

<style lang="scss" scoped>
@import 'express-input';
</style>
