<script lang="ts" setup>
import type { Elements } from '@vue-flow/core'
import { Panel, VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import {
  IconSunFill,
  IconMoonFill,
  IconCloudDownload,
  IconImport
} from '@arco-design/web-vue/es/icon'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import NodeRenderer from '@/components/NodeRenderer/NodeRenderer.vue'

const nodeTypes = {
  custom: markRaw(NodeRenderer)
}

const elements = ref<Elements>([
  { id: '1', type: 'custom', label: 'Node 1', position: { x: 250, y: 5 }, class: 'light' },
  { id: '2', type: 'custom', label: 'Node 2', position: { x: 100, y: 100 }, class: 'light' },
  { id: '3', type: 'custom', label: 'Node 3', position: { x: 400, y: 100 }, class: 'light' },
  { id: '4', type: 'custom', label: 'Node 4', position: { x: 400, y: 200 }, class: 'light' },
  { id: '5', type: 'custom', label: 'Node 5', position: { x: 400, y: 200 }, class: 'light' },
  { id: 'e1-2', source: '1', target: '2', animated: true, markerEnd: MarkerType.ArrowClosed },
  { id: 'e1-3', source: '1', target: '3', markerEnd: MarkerType.ArrowClosed }
])

const { onConnect, addEdges } = useVueFlow({
  minZoom: 0.2,
  maxZoom: 4
})

onConnect((param) => {
  addEdges({ ...param, markerEnd: MarkerType.ArrowClosed })
})

const dark = ref(false)

function toggleClass() {
  return (dark.value = !dark.value)
}
</script>

<template>
  <VueFlow v-model="elements" :class="{ dark }" class="vue-flow-basic" :node-types="nodeTypes">
    <Background :pattern-color="dark ? '#FFFFFB' : '#aaa'" gap="8" />
    <Controls />
    <Panel
      class="flow-designer-panel"
      position="top-right"
      style="display: flex; align-items: center"
    >
      <ASwitch class="panel-item" type="line" @change="toggleClass" size="large">
        <template #checked-icon>
          <IconMoonFill />
        </template>
        <template #unchecked-icon>
          <IconSunFill />
        </template>
      </ASwitch>
      <ADivider direction="vertical" margin="5px" />
      <AButton class="panel-item" type="text">
        <template #icon>
          <IconCloudDownload size="22px" />
        </template>
      </AButton>
      <ADivider direction="vertical" margin="5px" />
      <AButton class="panel-item" type="text">
        <template #icon>
          <IconImport size="22px" />
        </template>
      </AButton>
    </Panel>
  </VueFlow>
</template>

<style scoped lang="scss">
@import 'flow-designer';
</style>
