<script setup lang="ts">
export interface Option {
  name: string
  value: Object
  description?: string
}
export interface Property {
  type: string
  name: string
  displayName?: string
  description?: string
  defaultValue?: any
  options?: Option[]
  properties?: Property[]
}
export interface FormProps {
  modelValue: Object
  layout?: 'inline' | 'horizontal' | 'vertical'
  properties: Property[]
}
const props = withDefaults(defineProps<FormProps>(), {
  layout: 'vertical'
})
const emits = defineEmits<{
  (e: 'update:value', item: Object): void
}>()

const form: Record<string, any> = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:value', value)
  }
})

function getComponentName(property: Property) {
  if (!property.type || property.type == 'String') {
    return 'AInput'
  }

  return 'ASwitch'
}
</script>
<template>
  <div class="from-renderer">
    <AForm :model="form" :layout="props.layout">
      <AFormItem v-for="property in properties" v-bind:key="property.name" :field="property.name"
        :label="property.displayName" :tooltip="property.description">
        <Component :is="getComponentName(property)" v-model="form[property.name]" v-bind="property" />
      </AFormItem>
    </AForm>
  </div>
</template>

<style scoped lang="scss"></style>
