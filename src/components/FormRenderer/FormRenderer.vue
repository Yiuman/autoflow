<script setup lang="ts">
import MapEditor from '@/components/MapEditor/MapEditor.vue'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import ConditionFilter from '@/components/ConditionFilter/ConditionFilter.vue'
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
  modelValue?: Object
  layout?: 'inline' | 'horizontal' | 'vertical'
  properties?: Property[]
}
const props = withDefaults(defineProps<FormProps>(), {
  layout: 'vertical'
})
const emits = defineEmits<{
  (e: 'update:value', item: Object): void
}>()

const form = computed<any>({
  get() {
    return props.modelValue
  },
  set(value: Object) {
    emits('update:value', value)
  }
})

function getComponentName(property: Property) {
  if (property.name === 'express') {
    return ConditionFilter
  }
  if (!property.type || property.type == 'String') {
    return ExpressInput;
  }

  if (property.options) {
    return 'ASelect'
  }

  if (property.type === 'Map') {
    return MapEditor
  }

  return 'ASwitch'
}
</script>
<template>
  <div class="from-renderer">
    <AForm :model="form" :layout="props.layout">
      <AFormItem v-for="property in properties" v-bind:key="property.name" :field="property.name"
        :label="property.displayName || property.name" :tooltip="property.description || undefined">
        <Component :is="getComponentName(property)" v-model="form[property.name]" v-bind="property" />
      </AFormItem>
    </AForm>
  </div>
</template>

<style scoped lang="scss"></style>
