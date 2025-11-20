<script lang="ts" setup>
import { ScriptHelper } from '@/utils/util-func'
import type { FieldRule } from '@arco-design/web-vue/es/form/interface'

import type { ComponentAttr, Property } from '@/types/flow'
import { extractGenericTypes, isArrayType, toComponentAttrs } from '@/utils/converter'
import { I18N } from '@/locales/i18n'
import { cloneDeep } from 'lodash'

export interface FormProps {
  modelValue?: Record<string, any>
  layout?: 'inline' | 'horizontal' | 'vertical'
  properties?: Property[]
  keyPrefix?: string
}

const props = withDefaults(defineProps<FormProps>(), {
  layout: 'vertical',
  keyPrefix: ''
})

const emits = defineEmits<{
  (e: 'update:modelValue', item: Object): void
}>()

const form = computed<any>({
  get() {
    return props.modelValue
  },
  set(value: Object) {
    emits('update:modelValue', value)
  }
})

// ------------------- 表单校验规则处理 -------------------
const formRef = ref()

const rules = computed(() => {
  if (!props.properties) {
    return {}
  }
  const validateRules: Record<string, FieldRule[]> = {}
  props.properties.forEach((child) => {
    if (child.validateRules) {
      validateRules[child.name] = child.validateRules.map((validateRule) => {
        const fieldRule: FieldRule = {
          required: validateRule.required,
          message: validateRule.message
        }
        if (validateRule.script) {
          fieldRule.validator = (value, callback) => {
            const validated = ScriptHelper.execute(validateRule.script as string, value)
            if (!validated) {
              callback(validateRule.message)
            } else {
              callback()
            }
          }
        }

        return fieldRule
      })
    }
  })
  return validateRules
})
let unwatchList: (() => void)[] = []
function cleanupWatchers() {
  unwatchList.forEach((unwatch) => unwatch())
  unwatchList = []
}
//------------------- 默认值处理  -------------------
watchEffect(() => {
  cleanupWatchers()
  props.properties?.forEach((property) => {
    setDefaultValue(props.modelValue, property)
    if (property.validateRules) {
      const unwatch = watch(
        () => form.value[property.name],
        () => {
          formRef.value?.validate(property.name)
        }
      )
      unwatchList.push(unwatch)
    }
  })
})

function getDefaultList(property: Property) {
  if (property.defaultValue) {
    return cloneDeep(property.defaultValue)
  }
  if (property.properties?.length <= 1) {
    return []
  }

  const newObj: Record<string, any> = {}
  property.properties?.forEach((child) => {
    newObj[child.name as string] = child.defaultValue || ''
  })
  return [newObj]
}

function setDefaultValue(form: Record<string, any> | undefined, property: Property) {
  if (!form) {
    return
  }
  if (form[property.name] === undefined) {
    form[property.name] = buildDefaultValue(property)
  }
}

function buildDefaultValue(property: Property) {
  if (!property) {
    return undefined
  }
  if (property.defaultValue) {
    return cloneDeep(property.defaultValue)
  }

  const genericType = extractGenericTypes(property.type)
  if (isArrayType(genericType)) {
    return getDefaultList(property)
  }

  if (isBasicType(genericType.mainType)) {
    return undefined
  }

  return undefined
}

function isBasicType(type: string) {
  return ['Integer', 'BigDecimal', 'Double', 'Float', 'String', 'Long', 'Date'].includes(type)
}

const componentAttrs = computed<ComponentAttr[]>(() => {
  return toComponentAttrs(props.properties as Property[])
})

function getFieldItemLabel(cmpAttr: ComponentAttr) {
  return cmpAttr.property.displayName || I18N(cmpAttr.property.id, cmpAttr.property.name)
}

function getToolTip(cmpAttr: ComponentAttr): string {
  const i18nDescription = `${cmpAttr.property.id}.description`
  const result = I18N(i18nDescription)
  if (i18nDescription === result) {
    return cmpAttr.property.description as string
  }
  return result
}

onBeforeUnmount(cleanupWatchers)
</script>
<template>
  <div class="form-renderer">
    <AForm
      ref="formRef"
      :auto-label-width="true"
      :layout="props.layout"
      :model="form"
      :rules="rules"
    >
      <AFormItem
        v-for="cmpAttr in componentAttrs"
        :key="`${keyPrefix}_${cmpAttr.property.id}`"
        :field="cmpAttr.property.name"
        :label="getFieldItemLabel(cmpAttr)"
        :tooltip="getToolTip(cmpAttr)"
        :validate-trigger="['change', 'blur']"
      >
        <Component
          class="no-drag"
          :class="`${keyPrefix}_form-item-cmp_${cmpAttr.property.name}`"
          :is="cmpAttr.cmp"
          :key="`${keyPrefix}_${cmpAttr.property.id}`"
          v-model="form[cmpAttr.property.name]"
          v-bind="cmpAttr.attrs"
        />
      </AFormItem>
    </AForm>
  </div>
</template>

<style lang="scss" scoped>
@use 'form-renderer';
</style>