<script setup lang="ts">
import { ScriptHelper } from '@/utils/util-func'
import type { FieldRule } from '@arco-design/web-vue/es/form/interface'

import type { ComponentAttr, Property } from '@/types/flow'
import { toComponentAttrs } from '@/utils/converter'


export interface FormProps {
  modelValue?: Record<string, any>
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


// ------------------- 表单校验规则处理 -------------------
const rules = computed(() => {
  if (!props.properties) {
    return {};
  }
  const validateRules: Record<string, FieldRule[]> = {}
  props.properties.forEach(child => {
    if (child.validateRules) {
      validateRules[child.name] = child.validateRules.map(validateRule => {
        const fieldRule: FieldRule = {
          required: validateRule.required,
          message: validateRule.message,
        };
        if (validateRule.script) {
          fieldRule.validator = (value, callback) => {
            const validated = ScriptHelper.execute(validateRule.script as string, value);
            if (!validated) {
              callback(validateRule.message)
            } else {
              callback()
            }
          }
        }

        return fieldRule;

      });
    }
  })
  return validateRules;
})

//------------------- 默认值处理  -------------------
watchEffect(() => {
  props.properties?.forEach(property => {
    setDefaultValue(props.modelValue, property)
  })
})

function getDefaultList(property: Property) {
  if (property.properties?.length == 1) {
    return [''];
  }
  const newObj: Record<string, any> = {};
  property.properties?.forEach(child => {
    newObj[child.name as string] = child.defaultValue || ''
  })
  return [newObj]
}

function setDefaultValue(form: Record<string, any> | undefined, property: Property) {
  if (!form) {
    return
  }
  if (!form[property.name]) {
    form[property.name] = property?.defaultValue || (property.type === 'List' ? getDefaultList(property) : undefined)
  }
}

const componentAttrs = computed<ComponentAttr[]>(() => {
  return toComponentAttrs(props.properties as Property[])
})


</script>
<template>
  <div class="from-renderer">
    <AForm :model="form" :layout="props.layout" :rules="rules">
      <AFormItem v-for="cmpAttr in componentAttrs" v-bind:key="cmpAttr.property.name" :field="cmpAttr.property.name"
        :label="cmpAttr.property.displayName || cmpAttr.property.name"
        :tooltip="cmpAttr.property.description || undefined">
        <Component :is="cmpAttr.cmp" v-model="form[cmpAttr.property.name]" v-bind="cmpAttr.attrs" />
      </AFormItem>
    </AForm>
  </div>
</template>

<style scoped lang="scss">
@import 'form-renderer';
</style>
