import { computed, inject, type Ref } from 'vue'
import { flatten } from 'lodash'
import { INCOMER_DATA } from '@/symbols'
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import type { NodeFlatData } from '@/types/flow'

function createOptions(
  nodeId: string,
  data: Record<string, any>,
  prefix: string,
  type: string,
  iconFontCode: string
): Option[] {
  return Object.keys(data)
    .map((key) => {
      if (!key) return undefined
      return {
        type,
        key: `${prefix}.${key}`,
        label: key,
        value: data[key],
        nodeId,
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
          nodeFlatData.node.id,
          nodeFlatData.variables || {},
          `$.variables.${nodeFlatData.node.id}`,
          nodeFlatData.node.data?.label,
          'icon-variable'
        )
        const inputOptions = createOptions(
          nodeFlatData.node.id,
          nodeFlatData.inputData || {},
          `$.inputData.${nodeFlatData.node.id}`,
          nodeFlatData.node.data?.label,
          'icon-Input'
        )
        return [...varOptions, ...inputOptions]
      })
    )
  })

  return { selectOptions }
}
