<script lang="ts" setup>
import type { ComponentAttr } from '@/types/flow'
import type { TableColumnData } from '@arco-design/web-vue'
import { IconDelete, IconPlus } from '@arco-design/web-vue/es/icon'
import type { Component } from 'vue'

export interface ListEditorProps {
  columns: TableColumnData[]
  columnCmp?: Record<string, ComponentAttr>
  modelValue: Record<string, any>[]
  showHeader?: boolean
}

function newRecord(): Record<string, any> {
  const newObj: Record<string, any> = {}
  props.columns.forEach((column) => {
    newObj[column.dataIndex as string] = ''
  })
  return newObj
}

const props = withDefaults(defineProps<ListEditorProps>(), {
  modelValue: (prop: { columns: any[] }) => {
    const newObj: Record<string, any> = {}
    prop.columns.forEach((column: { dataIndex: string }) => {
      newObj[column.dataIndex as string] = ''
    })
    return [newObj]
  },
  showHeader: true
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Record<string, any>[]): void
  (e: 'change', record: Record<string, any>, val: string): void
}>()

function doEmitModelValue(values: Record<string, any>[]): void {
  emits('update:modelValue', values)
}

const data = computed(() => props.modelValue)
const [stopWatchData, toggleStopWatchData] = useToggle(false)
watch(
  () => data.value,
  (newVal) => {
    if (!stopWatchData.value) {
      doEmitModelValue(newVal)
    }
  },
  { deep: true }
)
watch(
  () => props.modelValue,
  async () => {
    toggleStopWatchData()
    data.value.splice(0, data.value.length, ...props.modelValue)
    await nextTick()
    toggleStopWatchData()
  },
  { deep: true }
)

function deleteRecord(record: Record<string, any>) {
  data.value.splice(data.value.indexOf(record), 1)
}

function addRecord() {
  data.value.push(newRecord())
}

function getColumnDataIndex(column: TableColumnData): string {
  return column.dataIndex || ''
}

function getColumnTitle(column: TableColumnData): string {
  return column.title as string
}

function doEmitChange(record: Record<string, any>, val: string) {
  emits('change', record, val)
}

function getColumnComponent(dataIndex: string): Component | string {
  if (!props.columnCmp || !props.columnCmp[dataIndex]) {
    return 'AInput'
  }
  return props.columnCmp[dataIndex].cmp
}

function getBindAttr(dataIndex: string): Record<string, any> | undefined {
  if (!props.columnCmp || !props.columnCmp[dataIndex]) {
    return undefined
  }
  return props.columnCmp[dataIndex].attrs
}
</script>

<template>
  <div class="list-editor">
    <ATable
      v-if="data && data.length"
      :data="data"
      :pagination="false"
      :show-header="showHeader"
      :stripe="true"
      size="mini"
    >
      <template #columns>
        <ATableColumn
          v-for="column in columns"
          :key="column.dataIndex"
          :title="getColumnTitle(column)"
          align="center"
          cellClass="list-editor-cell "
        >
          <template #cell="{ record }">
            <Component
              :is="getColumnComponent(column.dataIndex as string)"
              v-model="record[getColumnDataIndex(column)]"
              v-bind="getBindAttr(column.dataIndex as string)"
              @change="(val: any) => doEmitChange(record, val)"
            />
          </template>
        </ATableColumn>
        <ATableColumn
          :width="30"
          align="center"
          cellClass="list-editor-cell map-editor-opt-cell"
          title=""
        >
          <template #cell="{ record }">
            <IconDelete
              :size="15"
              class="list-editor-del-btn"
              @click="() => deleteRecord(record)"
            />
          </template>
        </ATableColumn>
      </template>
    </ATable>
    <div class="list-editor-add-btn">
      <AButton size="mini" @click="() => addRecord()">
        <template #icon>
          <IconPlus />
        </template>
      </AButton>
    </div>
  </div>
</template>

<style lang="scss">
@use 'list-editor';
</style>