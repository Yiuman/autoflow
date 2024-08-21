import { computed, inject, type Ref } from 'vue'
import { flatten } from 'lodash'
import { INCOMER_DATA } from '@/symbols'
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import type { NodeFlatData } from '@/types/flow'

export function useSelectOptions() {
  const nodeFlatDataArray = inject<Ref<NodeFlatData[]>>(INCOMER_DATA)
  const selectOptions = computed<Option[]>(() => {
    const nodeSelectOptions = nodeFlatDataArray?.value.map((nodeFlatData) => {
      const varKeys = Object.keys(nodeFlatData.variables)
        .map((varKey) => {
          if (!varKey) {
            return null
          }
          const value = nodeFlatData.variables[varKey]
          return {
            type: `${nodeFlatData.node.label}`,
            key: `$.variables.${nodeFlatData.node.id}.${varKey}`,
            label: varKey,
            value: value
          }
        })
        .filter((i) => i)

      const inputDataKeys = Object.keys(nodeFlatData.inputData)
        .map((varKey) => {
          if (!varKey) {
            return null
          }
          const value = nodeFlatData.inputData[varKey]
          return {
            type: `${nodeFlatData.node.label}`,
            key: `$.inputData.${nodeFlatData.node.id}${varKey}`,
            label: varKey,
            value: value
          }
        })
        .filter((i) => i)
      return [...varKeys, ...inputDataKeys]
    })
    return flatten(nodeSelectOptions) as Option[]
  })

  return {
    selectOptions
  }
}
