<script lang="ts" setup>
import { CalcType, Clause, type Condition } from './condition-filter'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'

interface PropType {
  modelValue?: Condition
}

const props = withDefaults(defineProps<PropType>(), {
  modelValue: () => ({ dataKey: '', value: '', calcType: CalcType.Equal, clause: Clause.AND })
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Condition): void
}>()

const options = computed(() => {
  return Object.keys(CalcType)
})

const dataValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})

const dataCalcType = computed({
  get() {
    return props.modelValue.calcType || CalcType.Equal
  },
  set(value) {
    dataValue.value.calcType = value
  }
})
</script>
<template>
  <div class="condition-item">
    <template v-if="dataValue.calcType === 'Express'">
      <ASelect v-model="dataCalcType" :options="options" size="mini">
        <template #label="{ data }">
          <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
        </template>
      </ASelect>
      <ExpressInput v-model="dataValue.value" placeholder="expressValue" />
    </template>
    <template v-else-if="dataValue.calcType === 'NotEmpty' || dataValue.calcType === 'Empty'">
      <ExpressInput v-model="dataValue.value" placeholder="value" />
      <ASelect v-model="dataCalcType" :options="options" size="mini">
        <template #label="{ data }">
          <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
        </template>
      </ASelect>
    </template>
    <template v-else>
      <ExpressInput v-model="dataValue.dataKey" placeholder="value1" />
      <ASelect v-model="dataCalcType" :options="options" size="mini">
        <template #label="{ data }">
          <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
        </template>
      </ASelect>
      <ExpressInput v-model="dataValue.value" placeholder="value2" />
    </template>
  </div>
</template>

<style lang="scss" scoped>
@use 'condition-item';
</style>