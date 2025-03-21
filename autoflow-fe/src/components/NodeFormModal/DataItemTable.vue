<script lang="ts" setup>
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import FileDataViewer from '@/components/FileDataViewer/FileDataViewer.vue'
import type { TableColumnData, TableData } from '@arco-design/web-vue/es/table/interface'
import type { FileData } from '@/types/flow'

interface Props {
  columns: TableColumnData[]
  data: TableData[]
  isFileList?: boolean
}

const props = defineProps<Props>()
</script>

<template>
  <AList v-if="isFileList">
    <AListItem v-for="(item, index) in props.data" :key="index">
      <FileDataViewer :data="item as FileData" />
    </AListItem>
  </AList>
  <ATable
    v-else
    :bordered="{ cell: true }"
    :columns="props.columns"
    :data="props.data"
    :pagination="true"
    :stripe="true"
    column-resizable
    style="padding: 5px 10px"
  >
    <template #typeMapColumn="{ record, column }">
      <VueJsonPretty
        :collapsedNodeLength="3"
        :data="record[column.dataIndex]"
        :show-icon="true"
        class="output-json"
      />
    </template>
    <template #typeObjectColumn="{ record, column }">
      <VueJsonPretty
        v-if="record[column.dataIndex] instanceof Object"
        :collapsedNodeLength="3"
        :data="record[column.dataIndex]"
        :show-icon="true"
        class="output-json"
      />
      <template v-else>{{ record[column.dataIndex] }}</template>
    </template>
    <template #typeFileDataColumn="{ record, column }">
      <FileDataViewer :data="record[column.dataIndex]" />
    </template>
  </ATable>
</template>

<style lang="scss" scoped></style>