<script lang="ts" setup>
import type { JSONDataType } from 'vue-json-pretty/types/utils'
import type { Property, VueFlowNode } from '@/types/flow'
import { getResultData, getResultFirst, getResultFirstData } from '@/utils/flow'
import { objectKeysToColumn, propertyToColumn } from '@/utils/converter'
import DataItemTable from '@/components/NodeFormModal/DataItemTable.vue'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import { MdPreview } from 'md-editor-v3'
import useTheme from '@/hooks/theme'

const [darkTheme] = useTheme()

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
  if (
    props?.node?.data?.service?.outputProperties &&
    props?.node?.data?.service?.outputProperties.length
  ) {
    return propertyToColumn(props?.node?.data?.service?.outputProperties as Property[])
  }
  const firstData = getResultFirstData(props?.node?.data?.executionResult)

  if (firstData instanceof Array) {
    return objectKeysToColumn(firstData[0])
  }
  return objectKeysToColumn(firstData)
})

const isStringValue = computed(() => {
  const outputProperties = props?.node?.data?.service?.outputProperties as Property[]
  return (
    outputProperties.length === 1 &&
    !outputProperties[0].properties &&
    outputProperties[0].type === 'String'
  )
})

const stringData = computed(() => {
  const outputProperties = props?.node?.data?.service?.outputProperties as Property[]
  return isStringValue.value ? (data?.value as any)[outputProperties[0].name] : ''
})

const resultView = ref(null)
const { height } = useElementSize(resultView)
</script>

<template>
  <div ref="resultView" class="result-data-viewer">
    <div v-if="result" class="result-box">
      <template v-if="data instanceof Array">
        <DataItemTable :columns="dataColumns" :data="data" />
      </template>
      <template v-else-if="result.error">
        <VueJsonPretty :data="result.error as JSONDataType" :show-icon="true" :virtual="true" />
      </template>
      <template v-else-if="isStringValue">
        <MdPreview :model-value="stringData" :theme="darkTheme ? 'dark' : 'light'" />
      </template>
      <template v-else>
        <VueJsonPretty
          :data="data as JSONDataType"
          :height="height"
          :show-icon="true"
          :virtual="true"
        />
      </template>
    </div>
    <div v-else class="result-data-empty">
      <AEmpty />
    </div>
  </div>
</template>

<style lang="scss" scoped>
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