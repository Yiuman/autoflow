import { computed, inject, type Ref } from 'vue'
import { flatten } from 'lodash'
import { INCOMER_DATA } from '@/symbols'
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import type { NodeFlatData, VueFlowNode } from '@/types/flow'

function createOptions(
  node: VueFlowNode,
  data: Record<string, any>,
  prefix: string,
  iconFontCode: string
): Option[] {
  return Object.keys(data)
    .map((key) => {
      if (!key) return undefined
      return {
        type: node.data?.label,
        key: `${prefix}.${key}`,
        label: key,
        value: data[key] || node.data?.executionResult?.data?.[key],
        nodeId: node.id,
        iconFontCode
      }
    })
    .filter(Boolean) as Option[]
}

export function useSelectOptions() {
  const nodeFlatDataArray = inject<Ref<NodeFlatData[]>>(INCOMER_DATA)

  const selectOptions = computed(() => {
    if (!nodeFlatDataArray?.value) return []
    return flatten(
      nodeFlatDataArray.value.map((nodeFlatData) => {
        const varOptions = createOptions(
          nodeFlatData.node,
          nodeFlatData.variables || {},
          `$.variables.${nodeFlatData.node.id}`,
          'icon-variable'
        )
        const inputOptions = createOptions(
          nodeFlatData.node,
          nodeFlatData.inputData || {},
          `$.inputData.${nodeFlatData.node.id}`,
          'icon-Input'
        )
        return [...varOptions, ...inputOptions]
      })
    )
  })

  return { selectOptions }
}
