import { computed, inject, type Ref } from 'vue'
import { flatten } from 'lodash'
import { INCOMER_DATA } from '@/symbols'
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import type { NodeFlatData } from '@/types/flow'

export function useSelectOptions() {
  const nodeFlatDataArray = inject<Ref<NodeFlatData[]>>(INCOMER_DATA)
  const selectOptions = computed<Option[]>(() => {
    const nodeSelectOptions = nodeFlatDataArray?.value?.map((nodeFlatData) => {
      const varKeys = nodeFlatData.variables
        ? Object.keys(nodeFlatData.variables)
            .map((varKey) => {
              if (!varKey) {
                return null
              }
              const value = nodeFlatData.variables?.[varKey]
              return {
                type: `${nodeFlatData.node.data?.label}`,
                key: `$.variables.${nodeFlatData.node.id}.${varKey}`,
                label: varKey,
                value: value,
                iconFontCode: 'icon-variable'
              }
            })
            .filter((i) => i)
        : []

      const inputDataKeys = nodeFlatData.inputData
        ? Object.keys(nodeFlatData.inputData)
            .map((varKey) => {
              if (!varKey) {
                return null
              }
              const value = nodeFlatData?.inputData?.[varKey]
              const baseKey = `$.inputData.${nodeFlatData.node.id}`
              // 判断 varKey 是否是数组结构
              const isArrayKey = /\[\d+]/.test(varKey)
              const key = isArrayKey ? `${baseKey}${varKey}` : `${baseKey}.${varKey}`
              return {
                type: `${nodeFlatData.node.data?.label}`,
                key,
                label: varKey,
                value: value,
                iconFontCode: 'icon-Input'
              }
            })
            .filter((i) => i)
        : []
      return [...varKeys, ...inputDataKeys]
    })
    return flatten(nodeSelectOptions) as Option[]
  })

  return {
    selectOptions
  }
}
