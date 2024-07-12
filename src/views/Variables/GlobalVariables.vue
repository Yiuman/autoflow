<script setup lang="ts">
import Curd from '@/components/Crud/Crud.vue'
import { IconSearch } from '@arco-design/web-vue/es/icon'
import type { TableColumnData } from '@arco-design/web-vue'
import useCRUD from '@/hooks/crud'

const queryObj = ref<Record<string, any>>({})
const columns: TableColumnData[] = [
  { dataIndex: 'key', title: 'key' },
  { dataIndex: 'value', title: 'value' },
  { dataIndex: 'desc', title: 'desc' }
]

interface Variable {
  key: string
  value: string
  desc?: string
}

const DEFAULT_INSTANCE = {
  key: '',
  value: ''
}
const variableInstance = ref<Variable>({ ...DEFAULT_INSTANCE })
const [formVisible, toggleFormVisible] = useToggle(false)

const { save, fetch } = useCRUD({ uri: '/variables', autoFetch: false })

function resetInstance() {
  variableInstance.value = { ...DEFAULT_INSTANCE }
}

async function saveVariable() {
  await save(variableInstance.value)
  resetInstance()
  await fetch()
}
</script>

<template>
  <div class="variables-table">
    <div class="variables-table-top-box">
      <div class="top-box-left">
        <AInput v-model="queryObj.name" allow-clear placeholder="搜索">
          <template #prefix>
            <IconSearch />
          </template>
        </AInput>
      </div>

      <div class="top-box-right">
        <AButton type="primary" @click="() => toggleFormVisible()">添加</AButton>
      </div>
    </div>
    <div class="variables-list">
      <Curd :uri="'/variables'" :query-object="queryObj" :columns="columns" />
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
}
</style>
