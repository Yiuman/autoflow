<script setup lang="ts">
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
      <ASelect size="mini" v-model="dataCalcType" :options="options">
        <template #label="{ data }">
          <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
        </template>
      </ASelect>
      <ExpressInput placeholder="expressValue" v-model="dataValue.value" />
    </template>
    <template v-else-if="dataValue.calcType === 'NotEmpty' || dataValue.calcType === 'Empty'">
      <ExpressInput placeholder="value" v-model="dataValue.value" />
      <ASelect size="mini" v-model="dataCalcType" :options="options">
        <template #label="{ data }">
          <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
        </template>
      </ASelect>
    </template>
    <template v-else>
      <ExpressInput placeholder="value1" v-model="dataValue.dataKey" />
      <ASelect size="mini" v-model="dataCalcType" :options="options">
        <template #label="{ data }">
          <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
        </template>
      </ASelect>
      <ExpressInput placeholder="value2" v-model="dataValue.value" />
    </template>
  </div>
</template>

<style scoped lang="scss">
@import 'condition-item';
</style>
