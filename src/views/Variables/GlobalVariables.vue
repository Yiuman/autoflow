<script setup lang="ts">
import RestCrud from '@/components/Crud/RestCrud.vue'
import { IconSearch } from '@arco-design/web-vue/es/icon'
import type { TableColumnData } from '@arco-design/web-vue'
import type { Variable } from '@/types/flow'

const queryObj = ref<Record<string, any>>({})
const columns: TableColumnData[] = [
  { dataIndex: 'key', title: 'key', width: 300 },
  { dataIndex: 'value', title: 'value', width: 300 },
  { dataIndex: 'desc', title: 'desc' },
  {
    title: 'Optional',
    slotName: 'optional',
    width: 10,
    align: 'center'
  }
]

const DEFAULT_INSTANCE = {
  key: '',
  value: ''
}
const variableInstance = ref<Variable>({ ...DEFAULT_INSTANCE })
const [formVisible, toggleFormVisible] = useToggle(false)
const variableCrud = ref()

function resetInstance() {
  variableInstance.value = { ...DEFAULT_INSTANCE }
}

async function saveVariable() {
  await variableCrud.value.crud.save(variableInstance.value)
  resetInstance()
  await variableCrud.value.crud.fetch()
}

function editVariable(record: Variable) {
  variableInstance.value = record
  toggleFormVisible()
}

async function deleteVariable(record: Variable) {
  await variableCrud.value.crud.delete(record.id)
  await variableCrud.value.crud.fetch()
}
</script>

<template>
  <div class="variables-table">
    <div class="variables-table-top-box">
      <div class="top-box-left">
        <AInput v-model="queryObj.keyword" allow-clear placeholder="搜索">
          <template #prefix>
            <IconSearch />
          </template>
        </AInput>
      </div>

      <div class="top-box-right">
        <AButton type="primary" @click="() => toggleFormVisible()">新增</AButton>
      </div>
    </div>
    <div class="variables-list">
      <RestCrud ref="variableCrud" :uri="'/variables'" :query-object="queryObj" :columns="columns">
        <template #optional="{ record }">
          <div class="optional-column">
            <AButton size="small" type="text" @click="() => editVariable(record)">编辑</AButton>
            <AButton size="small" type="text" @click="() => deleteVariable(record)">删除</AButton>
          </div>
        </template>
      </RestCrud>
    </div>

    <AModal v-model:visible="formVisible" @ok="saveVariable" @cancel="resetInstance" draggable>
      <template #title> 添加变量</template>
      <AForm :model="variableInstance" layout="vertical">
        <AFormItem field="key" label="变量名称" validate-trigger="input" required>
          <AInput v-model="variableInstance.key" placeholder="输入变量名称" />
        </AFormItem>
        <AFormItem field="value" label="变量值" validate-trigger="input">
          <AInput v-model="variableInstance.value" placeholder="输入变量值" />
        </AFormItem>
        <AFormItem field="desc" label="描述" validate-trigger="input">
          <ATextarea v-model="variableInstance.desc" placeholder="输入变量值" />
        </AFormItem>
      </AForm>
    </AModal>
  </div>
</template>

<style lang="scss">
.variables-table {
  padding: 20px;

  .variables-table-top-box {
    display: flex;

    .top-box-left {
      width: 250px;
      align-items: center;
      padding: 5px;
      background-color: var(--color-bg-2);
    }

    .top-box-right {
      display: flex;
      flex: 1;
      padding: 5px;
      flex-direction: column;

      > * {
        align-self: flex-end;
      }
    }
  }

  .variables-list {
    padding: 5px 0;
  }

  .optional-column {
    display: flex;

    > * {
      flex: 1;
    }
  }
}
</style>
