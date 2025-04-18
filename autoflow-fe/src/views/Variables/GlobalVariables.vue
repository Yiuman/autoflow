<script lang="ts" setup>
import RestCrud from '@/components/Crud/RestCrud.vue'
import { IconSearch } from '@arco-design/web-vue/es/icon'
import type { TableColumnData } from '@arco-design/web-vue'
import type { Variable } from '@/types/flow'
import { I18N } from '@/locales/i18n'

const queryObj = ref<Record<string, any>>({})
const columns: TableColumnData[] = [
  { dataIndex: 'key', title: I18N('globalVariable.key', 'Key'), width: 300 },
  { dataIndex: 'value', title: I18N('globalVariable.value', 'Value'), width: 300 },
  { dataIndex: 'description', title: I18N('globalVariable.description', 'Description') },
  {
    title: I18N('globalVariable.optional', 'Optional'),
    slotName: 'optional',
    width: 80,
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
        <AInput v-model="queryObj.keyword" :placeholder="I18N('search')" allow-clear>
          <template #prefix>
            <IconSearch />
          </template>
        </AInput>
      </div>

      <div class="top-box-right">
        <AButton type="primary" @click="() => toggleFormVisible()">{{ I18N('create') }}</AButton>
      </div>
    </div>
    <div class="variables-list">
      <RestCrud ref="variableCrud" :columns="columns" :query-object="queryObj" :uri="'/variables'">
        <template #optional="{ record }">
          <div class="optional-column">
            <AButton size="small" type="text" @click="() => editVariable(record)"
              >{{ I18N('edit') }}
            </AButton>
            <AButton size="small" type="text" @click="() => deleteVariable(record)"
              >{{ I18N('delete') }}
            </AButton>
          </div>
        </template>
      </RestCrud>
    </div>

    <AModal v-model:visible="formVisible" draggable @cancel="resetInstance" @ok="saveVariable">
      <template #title> {{ I18N('variable.form.title', 'Create a new variable') }}</template>
      <AForm :model="variableInstance" layout="vertical">
        <AFormItem
          :label="I18N('variable.form.field.key', 'variable name')"
          field="key"
          required
          validate-trigger="input"
        >
          <AInput v-model="variableInstance.key" />
        </AFormItem>
        <AFormItem
          :label="I18N('variable.form.field.value', 'variable value')"
          field="value"
          validate-trigger="input"
        >
          <AInput v-model="variableInstance.value" />
        </AFormItem>
        <AFormItem
          :label="I18N('variable.form.field.desc', 'description')"
          field="desc"
          validate-trigger="input"
        >
          <ATextarea v-model="variableInstance.desc" />
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