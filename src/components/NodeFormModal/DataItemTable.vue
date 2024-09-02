<script setup lang="ts">
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import FileDataViewer from '@/components/FileDataViewer/FileDataViewer.vue'
import type { TableColumnData, TableData } from '@arco-design/web-vue/es/table/interface'

interface Props {
  columns: TableColumnData[]
  data: TableData[]
}

const props = defineProps<Props>()
</script>

<template>
  <ATable
    :stripe="true"
    :bordered="false"
    style="padding: 5px 10px"
    :columns="props.columns"
    :data="props.data"
  >
    <template #typeMapColumn="{ record, column }">
      <VueJsonPretty
        class="output-json"
        :data="record[column.dataIndex]"
        :collapsedNodeLength="3"
        :show-icon="true"
      />
    </template>
    <template #typeObjectColumn="{ record, column }">
      <VueJsonPretty
        class="output-json"
        :data="record[column.dataIndex]"
        :collapsedNodeLength="3"
        v-if="record[column.dataIndex] instanceof Object"
        :show-icon="true"
      />
      <template v-else>{{ record[column.dataIndex] }}</template>
    </template>
    <template #typeFileDataColumn="{ record, column }">
      <FileDataViewer :data="record[column.dataIndex]" />
    </template>
  </ATable>
</template>

<style scoped lang="scss"></style>
