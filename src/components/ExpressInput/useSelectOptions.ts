import { computed, inject, type Ref } from 'vue'
import { flatten } from 'lodash'
import { INCOMER_DATA } from '@/symbols'
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import type { NodeFlatData } from '@/types/flow'

function createOptions(data: Record<string, any>, prefix: string, type: string): Option[] {
  return Object.keys(data)
    .map((key) => {
      if (!key) return undefined
      return {
        type,
        key: `${prefix}.${key}`,
        label: key,
        value: data[key],
        iconFontCode: type === 'variable' ? 'icon-variable' : 'icon-Input'
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
          nodeFlatData.variables || {},
          `$.variables.${nodeFlatData.node.id}`,
          nodeFlatData.node.data?.label
        )
        const inputOptions = createOptions(
          nodeFlatData.inputData || {},
          `$.inputData.${nodeFlatData.node.id}`,
          nodeFlatData.node.data?.label
        )
        return [...varOptions, ...inputOptions]
      })
    )
  })

  return { selectOptions }
}
