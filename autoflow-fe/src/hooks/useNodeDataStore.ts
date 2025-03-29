// useCounterStore.ts
import { computed, ref } from 'vue'
import { createInjectionState } from '@vueuse/core'
import { flattenProperties, getAllIncomers } from '@/utils/converter'
import type { NodeFlatData, VueFlowNode } from '@/types/flow'
import { flatten } from '@/utils/util-func'
import { getResultData } from '@/utils/flow'
import { useVueFlow } from '@vue-flow/core'

const selectedNode = ref<VueFlowNode>()
const [useProvideNodeDataStore, useNodeDataStore] = createInjectionState(() => {
  const { getIncomers } = useVueFlow()
  const incomers = computed(() => getAllIncomers(selectedNode.value?.id, getIncomers))
  const inputDataFlat = computed<NodeFlatData[]>(() => {
    const nodeFlatDataArray: NodeFlatData[] = []
    if (incomers.value) {
      for (const incomer of incomers.value) {
        //前节点的入参
        let variableFlatData
        if (incomer.data.parameters) {
          variableFlatData = flatten(incomer.data.parameters)
        } else if (incomer.data?.service?.properties && incomer.data?.service?.propertie.length) {
          variableFlatData = flattenProperties(incomer.data.service.properties)
        }

        //前节点的输出选项
        const inputData = getResultData(incomer.data?.executionResult)
        let nodeExecutionDataFlatData
        if (inputData) {
          nodeExecutionDataFlatData = flatten(inputData)
        } else if (
          incomer.data?.service?.outputProperties &&
          incomer.data?.service?.outputProperties.length
        ) {
          nodeExecutionDataFlatData = flattenProperties(incomer.data?.service?.outputProperties)
        }

        nodeFlatDataArray.push({
          node: incomer,
          variables: variableFlatData,
          inputData: nodeExecutionDataFlatData
        })
      }
    }

    return nodeFlatDataArray as NodeFlatData[]
  })
  return { selectedNode, incomers, inputDataFlat }
})

export { useProvideNodeDataStore, useNodeDataStore }
