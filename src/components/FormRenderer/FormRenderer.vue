<script setup lang="ts">
export interface Option {
  name: string
  value: Object
  description?: string | null
}
export interface Property {
  type: string
  name: string
  displayName?: string | null
  description?: string | null
  defaultValue?: any | null
  options?: Option[] | null
  properties?: Property[] | null
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

const form = computed({
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

  if (property.options) {
    return 'ASelect'
  }

  return 'ASwitch'
}
</script>
<template>
  <div class="from-renderer">
    <AForm :model="form" :layout="props.layout">
      <AFormItem
        v-for="property in properties"
        v-bind:key="property.name"
        :field="property.name"
        :label="property.displayName"
        :tooltip="property.description"
      >
        <Component
          :is="getComponentName(property)"
          v-model="form[property.name]"
          v-bind="property"
        />
      </AFormItem>
    </AForm>
  </div>
</template>

<style scoped lang="scss"></style>
