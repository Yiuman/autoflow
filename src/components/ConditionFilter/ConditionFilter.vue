<script setup lang="ts">
import { CalcType, Clause, type Condition } from './condition-filter'
import { pull } from 'lodash'
import { IconDelete, IconPlus } from '@arco-design/web-vue/es/icon'
import ConditionItem from '@/components/ConditionFilter/ConditionItem.vue'

interface PropType {
  modelValue?: Condition,
  parent?: Condition
}

const props = withDefaults(defineProps<PropType>(), {
  modelValue: () => ({
    children: [{ dataKey: '', calcType: CalcType.Equal, value: '', clause: Clause.AND }],
    clause: Clause.AND,
    root: true
  })
})

const emits = defineEmits<{
  (e: 'update:modelValue', item: Condition): void;
  (e: 'removeChild', item: Condition): void;
  (e: 'addChild', item: Condition): void;
}>()

const modelValue = computed({
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
  const newCondition: Condition = { children: [], clause: Clause.AND, root: true }
  const currentModel = props.modelValue
  currentModel.root = false
  newCondition.children?.push(currentModel as Condition)
  newCondition.children?.push(
    { children: [{ dataKey: '', calcType: CalcType.Equal, value: '', clause: Clause.AND }], clause: Clause.AND }
  )
  emits('update:modelValue', newCondition)
}


function addCondition(child: Condition) {
  const currentModel = props.modelValue
  if (!currentModel) {
    return
  }
  const children = currentModel.children || []
  children.splice(children.indexOf(child) + 1, 0, {
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
  emits('addChild', child)
}

function emitRemove(child: Condition) {
  emits('removeChild', child)
}


const isAnd = computed({
    get() {
      return modelValue.value.clause === Clause.AND
    },
    set(value) {
      modelValue.value.clause = value ? Clause.AND : Clause.OR
    }
  }
)

</script>
<template>
  <div class="condition-filter">
    <template v-if="props.modelValue && modelValueChildren && modelValueChildren.length">
      <div class="condition-groups">
        <div v-if="modelValueChildren.length > 1" class="condition-clause-switch">
          <ASwitch v-model="isAnd" type="round" unchecked-color="coral">
            <template #checked-icon>
              <span>且</span>
            </template>
            <template #unchecked-icon>
              <span style="color:black">或</span>
            </template>
          </ASwitch>
          <div class="condition-clause-line"></div>
          <AButton v-if="modelValueChildren.length > 1" class="condition-group-add" size="mini"
                   @click="addGroup">
            <IconPlus />
          </AButton>
        </div>
        <div class="condition-group-children">
          <template v-for="(child, index) in modelValueChildren" :key="index">
            <ConditionFilter v-model="modelValueChildren[index]" :parent="props.modelValue"
                             @remove-child="removeChild" @add-child="addCondition" />
          </template>
        </div>


      </div>


    </template>
    <template v-else>
      <div class="condition-filter-item">
        <ConditionItem v-model="modelValue" />
        <IconPlus class="add-btn" @click="() => emitAddChild(modelValue)" />
        <IconDelete class="delete-btn" @click="() => emitRemove(modelValue)" />
      </div>
    </template>

  </div>
</template>

<style scoped lang="scss">
@import 'condition-filter';
</style>
