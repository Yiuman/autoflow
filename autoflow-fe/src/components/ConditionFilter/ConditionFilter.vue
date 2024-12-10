<script setup lang="ts">
import {CalcType, Clause, type Condition} from './condition-filter'
import {pull} from 'lodash'
import {IconDelete, IconPlus} from '@arco-design/web-vue/es/icon'
import ConditionItem from '@/components/ConditionFilter/ConditionItem.vue'

interface PropType {
    modelValue: Condition
    parent?: Condition
}

const props = withDefaults(defineProps<PropType>(), {
    modelValue: () => ({
        children: [{dataKey: '', calcType: CalcType.Equal, value: '', clause: Clause.AND}],
    clause: Clause.AND,
    root: true
  })
})

const emits = defineEmits<{
  (e: 'update:modelValue', item: Condition): void
  (e: 'removeChild', item: Condition): void
  (e: 'addChild', item: Condition): void
}>()

const dataValue = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    if (value) {
      emits('update:modelValue', value)
    }
  }
})

const modelValueChildren = computed(() => {
  return props.modelValue?.children || []
})

function addGroup() {
  const currentModel = dataValue.value
  currentModel.root = false
  const resetCurrentModel = currentModel.children
    ? currentModel
    : {
          clause: Clause.AND,
          children: [currentModel]
      }
  const newCondition: Condition = {
    children: [
      resetCurrentModel,
      {
        children: [
          {
            dataKey: '',
            calcType: CalcType.Equal,
            value: '',
            clause: Clause.AND
          }
        ],
        clause: Clause.AND
      }
    ],
    clause: Clause.AND,
    root: true
  }
  emits('update:modelValue', newCondition)
}

function addCondition(child: Condition) {
  const currentModel = dataValue.value
  if (!currentModel) {
    return
  }
  const children = currentModel.children || []
  children.splice(children.indexOf(child) + 1, 0, {
    children: [],
    dataKey: '',
    calcType: CalcType.Equal,
    value: '',
    clause: Clause.AND
  })
  currentModel.children = children
  emits('update:modelValue', currentModel)
}

function removeChild(child: Condition) {
  let currentModel = props.modelValue
  pull(currentModel.children || [], child)
  if (!currentModel.children?.length) {
    emits('removeChild', currentModel)
  } else if (currentModel.children?.length == 1) {
    currentModel = currentModel.children[0]
    emits('update:modelValue', currentModel)
  } else {
    emits('update:modelValue', currentModel)
  }
}

function emitAddChild(child: Condition) {
  props.parent ? emits('addChild', child) : addGroup()
}

function emitRemove(child: Condition) {
  emits('removeChild', child)
}

const isAnd = computed({
  get() {
    return dataValue.value.clause === Clause.AND
  },
  set(value) {
    dataValue.value.clause = value ? Clause.AND : Clause.OR
  }
})
</script>
<template>
  <div class="condition-filter">
    <template v-if="dataValue && modelValueChildren && modelValueChildren.length">
      <div class="condition-groups">
        <div v-if="modelValueChildren.length > 1" class="condition-clause-switch">
          <ASwitch v-model="isAnd" type="line" unchecked-color="coral">
            <template #checked-icon>
              <span>且</span>
            </template>
            <template #unchecked-icon>
              <span style="color: black">或</span>
            </template>
          </ASwitch>
          <div class="condition-clause-line"></div>
          <AButton
            v-if="modelValueChildren.length > 1"
            class="condition-group-add"
            size="mini"
            @click="addGroup"
          >
            <IconPlus />
          </AButton>
        </div>
        <div class="condition-group-children">
          <template v-for="(child, index) in modelValueChildren" :key="index">
            <ConditionFilter
              v-model="modelValueChildren[index]"
              :parent="dataValue"
              @remove-child="removeChild"
              @add-child="addCondition"
            />
          </template>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="condition-filter-item">
        <ConditionItem v-model="dataValue" />
        <div class="condition-item-btn">
          <IconPlus class="add-btn" @click="() => emitAddChild(dataValue)" />
          <IconDelete class="delete-btn" @click="() => emitRemove(dataValue)" />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
@use 'condition-filter';
</style>
