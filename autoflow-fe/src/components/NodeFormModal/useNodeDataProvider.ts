import type {NodeFlatData, VueFlowNode} from '@/types/flow'
import {computed, provide, type Ref} from 'vue'
import {CURRENT_NODE, INCOMER, INCOMER_DATA} from '@/symbols'
import {flattenProperties, getAllIncomers} from '@/utils/converter'
import {flatten} from '@/utils/util-func'
import {getResultData} from '@/utils/flow'
import {useVueFlow} from '@vue-flow/core'

export function useNodeDataProvider(node: Ref<VueFlowNode>) {
    const {getIncomers} = useVueFlow()
    // 提供当前的有用变量
    provide(CURRENT_NODE, node.value)
    const incomers = computed(() => getAllIncomers(node.value.id, getIncomers))
    provide(INCOMER, incomers)

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
          } else if (incomer.data?.service?.outputProperties && incomer.data?.service?.outputProperties.length) {
              nodeExecutionDataFlatData = flattenProperties(incomer.data?.service?.outputProperties)
          }

          console.warn("incomer", incomer)
          nodeFlatDataArray.push({
              node: incomer,
              variables: variableFlatData,
              inputData: nodeExecutionDataFlatData
          })
      }
    }

    return nodeFlatDataArray as NodeFlatData[]
  })
  provide(INCOMER_DATA, inputDataFlat)

  return {
    node,
    incomers,
    inputDataFlat
  }
}
