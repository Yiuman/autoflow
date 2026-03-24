<script lang="ts" setup>
import RestCrud from '@/components/Crud/RestCrud.vue'
import ModelForm from './ModelForm.vue'
import { IconSearch } from '@arco-design/web-vue/es/icon'
import type { TableColumnData } from '@arco-design/web-vue'
import type { Model } from '@/api/model'
import { I18N } from '@/locales/i18n'

const queryObj = ref<Record<string, any>>({})
const columns: TableColumnData[] = [
  { dataIndex: 'name', title: I18N('model.name', 'Name'), width: 200 },
  { dataIndex: 'baseUrl', title: I18N('model.baseUrl', 'Base URL'), width: 300 },
  {
    title: I18N('model.optional', 'Optional'),
    slotName: 'optional',
    width: 120,
    align: 'center'
  }
]

const DEFAULT_INSTANCE: Partial<Model> = {
  name: '',
  baseUrl: '',
  apiKey: '',
  config: ''
}
const modelInstance = ref<Partial<Model>>({ ...DEFAULT_INSTANCE })
const [formVisible, toggleFormVisible] = useToggle(false)
const [isEditing, setIsEditing] = useToggle(false)
const modelCrud = ref()
const editingId = ref<string | null>(null)

function resetInstance() {
  modelInstance.value = { ...DEFAULT_INSTANCE }
  editingId.value = null
  setIsEditing(false)
}

async function saveModel() {
  if (isEditing.value && editingId.value) {
    await modelCrud.value.crud.save({ ...modelInstance.value, id: editingId.value })
  } else {
    await modelCrud.value.crud.save(modelInstance.value)
  }
  resetInstance()
  await modelCrud.value.crud.fetch()
}

function editModel(record: Model) {
  modelInstance.value = { ...record }
  editingId.value = record.id
  setIsEditing(true)
  toggleFormVisible()
}

async function deleteModel(record: Model) {
  await modelCrud.value.crud.delete(record.id)
  await modelCrud.value.crud.fetch()
}

function openCreateModal() {
  resetInstance()
  toggleFormVisible()
}

const dialogTitle = computed(() =>
  isEditing.value ? I18N('model.form.title.edit', 'Edit Model') : I18N('model.form.title.create', 'Create Model')
)
</script>

<template>
  <div class="model-list">
    <div class="model-list-top-box">
      <div class="top-box-left">
        <AInput v-model="queryObj.name" :placeholder="I18N('search')" allow-clear>
          <template #prefix>
            <IconSearch />
          </template>
        </AInput>
      </div>

      <div class="top-box-right">
        <AButton type="primary" @click="openCreateModal">{{ I18N('create') }}</AButton>
      </div>
    </div>
    <div class="model-list-content">
      <RestCrud ref="modelCrud" :columns="columns" :query-object="queryObj" uri="/models">
        <template #optional="{ record }">
          <div class="optional-column">
            <AButton size="small" type="text" @click="() => editModel(record)">
              {{ I18N('edit') }}
            </AButton>
            <AButton size="small" type="text" status="danger" @click="() => deleteModel(record)">
              {{ I18N('delete') }}
            </AButton>
          </div>
        </template>
      </RestCrud>
    </div>

    <AModal
      v-model:visible="formVisible"
      draggable
      :title="dialogTitle"
      @cancel="resetInstance"
      @ok="saveModel"
    >
      <ModelForm v-model="modelInstance" />
    </AModal>
  </div>
</template>

<style lang="scss">
.model-list {
  padding: 20px;

  .model-list-top-box {
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

  .model-list-content {
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
