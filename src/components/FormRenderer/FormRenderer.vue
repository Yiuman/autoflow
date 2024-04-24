<script setup lang="ts">
import MapEditor from '@/components/MapEditor/MapEditor.vue'
import ListEditor, { type CmpAttr } from '@/components/ListEditor/ListEditor.vue'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import ConditionFilter from '@/components/ConditionFilter/ConditionFilter.vue'
import { ScriptHelper } from '@/utils/util-func'
import type { FieldRule } from '@arco-design/web-vue/es/form/interface'
import type { TableColumnData } from '@arco-design/web-vue'


export interface Option {
  name: string
  value: Object
  description?: string | null
}

export interface ValidateRule {
  field: string,
  required?: boolean
  message?: string,
  fieldType?: string,
  script?: string,
  validateType?: string,
  attributes: Record<string, any>
}

export interface Property {
  type: string
  name: string
  displayName?: string | null
  description?: string | null
  defaultValue?: any | null
  options?: Option[] | null
  properties?: Property[] | null
  validateRules?: ValidateRule[] | null
}

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

function getComponentName(property: Property) {
  if (property.type === 'Condition') {
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

  if (property.type === 'List' || property.type === 'Set') {
    return ListEditor;
  }

  return ExpressInput
}

function getBindAttr(property: Property) {
  if (property.type === 'List' || property.type === 'Set') {
    const columns: TableColumnData[] = [];
    const columnCmp: Record<string, CmpAttr> = {}
    property.properties?.forEach(child => {
      columns.push({
        title: child.displayName || child.name,
        dataIndex: child.name
      })

      const childBindAttr = getBindAttr(child);
      columnCmp[child.name] = { cmp: getComponentName(child) as string, attr: childBindAttr }

    })

    return { columns, columnCmp };
  }
  return property;
}

const rules = computed(() => {
  if (!props.properties) {
    return {};
  }
  const validateRules: Record<string, FieldRule[]> = {}
  props.properties.forEach(child => {
    if (child.validateRules) {
      const fieldRules: FieldRule[] = child.validateRules.map(validateRule => {
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

      })
      validateRules[child.name] = fieldRules;
    }
  })
  return validateRules;
})

watchEffect(() => {
  props.properties?.forEach(property => {
    setDefaultValue(props.modelValue, property)
  })
})

function getDefaultList(property: Property) {
  const newObj: Record<string, any> = {};
  property.properties?.forEach(child => {
    newObj[child.name as string] = ''
  })
  return [newObj]
}

function setDefaultValue(form: Record<string, any> | undefined, property: Property) {
  if (!form) {
    return
  }
  if (!form[property.name]) {
    form[property.name] = property?.defaultValue || (property.type === 'List' ? getDefaultList(property) : null)
  }
}


</script>
<template>
  <div class="from-renderer">
    <AForm :model="form" :layout="props.layout" :rules="rules">
      <AFormItem v-for="property in properties" v-bind:key="property.name" :field="property.name"
        :label="property.displayName || property.name" :tooltip="property.description || undefined">
        <Component :is="getComponentName(property)" v-model="form[property.name]" v-bind="getBindAttr(property)" />
      </AFormItem>
    </AForm>
  </div>
</template>

<style scoped lang="scss"></style>
