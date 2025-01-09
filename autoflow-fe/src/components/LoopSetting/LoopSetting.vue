<script lang="ts" setup>
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import { type Loop } from '@/types/flow'
import { I18N } from '@/locales/i18n'

interface LoopProps {
  modelValue?: Loop
}

const props = withDefaults(defineProps<LoopProps>(), {
  modelValue: () => ({ sequential: false })
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Loop): void
}>()

const data = computed<Loop>({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})
</script>

<template>
  <div class="loop-setting">
    <AForm :model="data" layout="vertical">
      <AFormItem
        :label="I18N('loop.loopCardinality', 'loopCardinality')"
        :tooltip="I18N('loop.loopCardinality.tooltip', 'Must be greater than 1')"
        field="loopCardinality"
      >
        <AInputNumber v-model="data.loopCardinality" :min="1" mode="button" />
      </AFormItem>
      <AFormItem
        :label="I18N('loop.collectionString', 'collectionString')"
        field="collectionString"
      >
        <ExpressInput v-model="data.collectionString" />
      </AFormItem>
      <AFormItem :label="I18N('loop.elementVariable', 'elementVariable')" field="elementVariable">
        <ExpressInput v-model="data.elementVariable" />
      </AFormItem>
      <AFormItem :label="I18N('loop.sequential', 'sequential')" field="sequential">
        <ASwitch v-model="data.sequential" type="line" />
      </AFormItem>
      <AFormItem
        :label="I18N('loop.completionCondition', 'completionCondition')"
        field="completionCondition"
      >
        <ExpressInput v-model="data.completionCondition" />
      </AFormItem>
    </AForm>
  </div>
</template>

<style lang="scss">
@use 'loop-setting';
</style>