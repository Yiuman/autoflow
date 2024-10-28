<script setup lang="ts">
import type { JSONDataType } from 'vue-json-pretty/types/utils'
import type { Property, VueFlowNode } from '@/types/flow'
import { getResultData, getResultFirst, getResultFirstData } from '@/utils/flow'
import { objectKeysToColumn, propertyToColumn } from '@/utils/converter'
import DataItemTable from '@/components/NodeFormModal/DataItemTable.vue'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'

interface ResultDataViewerProps {
  node?: VueFlowNode
}

const props = defineProps<ResultDataViewerProps>()
const data = computed(() => {
  return getResultData(props?.node?.data?.executionResult)
})

const result = computed(() => {
  return getResultFirst(props?.node?.data?.executionResult)
})

const dataColumns = computed(() => {
  if (props?.node?.data?.service?.outputType && props?.node?.data?.service?.outputType.length) {
    return propertyToColumn(props?.node?.data?.service?.outputType as Property[])
  }
  const firstData = getResultFirstData(props?.node?.data?.executionResult)
  if (firstData instanceof Array) {
    return objectKeysToColumn(firstData[0])
  }
  return objectKeysToColumn(firstData)
})
</script>

<template>
  <div class="result-data-viewer">
    <div class="result-box" v-if="result">
      <template v-if="data instanceof Array">
        <DataItemTable :data="data" :columns="dataColumns" />
      </template>
      <template v-else-if="result.error">
        <VueJsonPretty :virtual="true" :data="result.error as JSONDataType" :show-icon="true" />
      </template>
      <template v-else>
        <VueJsonPretty :virtual="true" :data="data as JSONDataType" :show-icon="true" />
      </template>
    </div>
    <div class="result-data-empty" v-else>
      <AEmpty />
    </div>
  </div>
</template>

<style scoped lang="scss">
.result-data-viewer {
  height: 100%;

  .result-box {
    height: 100%;

    :deep(.vjs-tree) {
      height: 100%;
    }

    :deep(.vjs-tree-list) {
      height: 100% !important;
    }
  }
}
</style>
