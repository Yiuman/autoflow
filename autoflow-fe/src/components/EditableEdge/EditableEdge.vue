<script lang="ts" setup>
import type { EdgeProps } from '@vue-flow/core'
import { BezierEdge, EdgeLabelRenderer, getBezierPath, useVueFlow } from '@vue-flow/core'
import type { StyleValue } from 'vue'
import { IconClose } from '@arco-design/web-vue/es/icon'

const props = defineProps<EdgeProps<any>>()
const { removeEdges } = useVueFlow()

const toolBarStyle = computed(() => {
  const path = getBezierPath(props)
  return {
    pointerEvents: 'all',
    position: 'absolute',
    transform: `translate(-50%, -50%) translate(${path[1]}px,${path[2]}px)`
  } as StyleValue
})
</script>

<template>
  <BezierEdge v-bind="props" />
  <EdgeLabelRenderer>
    <div :id="`edge-toolbar-${id}`" :style="toolBarStyle" class="edge-toolbar">
      <IconClose class="edge-delete-btn" @click="removeEdges(id)" />
    </div>
  </EdgeLabelRenderer>
</template>
<style lang="scss">
@use 'editable-edge';
</style>