<script setup lang="ts">
import FormRenderer from '@/components/FormRenderer/FormRenderer.vue'
import type { Linkage } from '@/types/flow'

interface Props {
  modelValue: Linkage
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({ value: null, parameter: {} })
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Linkage): void
}>()

const data = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})
const properties = computed(() => [])
</script>

<template>
  <div class="linkage-form">
    <ATrigger>
      <AButton>{{ data.value }}</AButton>
      <template #content>
        <AButton>{{ data.value }}</AButton>
        <FormRenderer v-model="data.parameter" :properties="properties" />
      </template>
    </ATrigger>
  </div>
</template>

<style lang="scss"></style>
