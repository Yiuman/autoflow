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
import { toFlow } from '@/utils/converter'
import type { FileItem } from '@arco-design/web-vue'
import type { Flow } from '@/types/flow'

const nodeTypes = {
  service: markRaw(NodeRenderer)
}

const elements = ref<Elements>([
  { id: '1', type: 'service', label: 'Node 1', position: { x: 250, y: 5 }, class: 'light' },
  { id: '2', type: 'service', label: 'Node 2', position: { x: 100, y: 100 }, class: 'light' },
  { id: '3', type: 'service', label: 'Node 3', position: { x: 400, y: 100 }, class: 'light' },
  { id: '4', type: 'service', label: 'Node 4', position: { x: 400, y: 200 }, class: 'light' },
  { id: '5', type: 'service', label: 'Node 5', position: { x: 400, y: 200 }, class: 'light' },
  { id: 'e1-2', source: '1', target: '2', animated: true, markerEnd: MarkerType.ArrowClosed },
  { id: 'e1-3', source: '1', target: '3' }
])

const { onConnect, addEdges, getNodes, getEdges } = useVueFlow({
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

function exportJson() {
  let link = document.createElement('a')
  link.download = 'config.json'
  link.href = 'data:text/plain,' + JSON.stringify(toFlow(getNodes.value, getEdges.value))
  link.click()
}

function importJson(fileList: FileItem[]): void {
  const reader = new FileReader()
  const fileItem = fileList[0]
  reader.readAsText(fileItem.file as Blob)
  reader.onload = function () {
    const flowDefine: Flow = JSON.parse(reader.result as string)
    const edges = flowDefine.connections?.map((connection) => ({
      ...connection,
      id: `e${connection.source}_${connection.target}`,
      markerEnd: MarkerType.ArrowClosed
    }))
    elements.value = [...flowDefine.nodes, ...edges]
  }
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
      <ASwitch
        class="panel-item"
        type="line"
        @change="toggleClass"
        checked-color="black"
        size="large"
      >
        <template #checked-icon>
          <IconMoonFill style="color: yellow" />
        </template>
        <template #unchecked-icon>
          <IconSunFill style="color: orange" />
        </template>
      </ASwitch>
      <ADivider direction="vertical" margin="5px" />
      <AButton class="panel-item" type="text" @click="exportJson">
        <template #icon>
          <IconCloudDownload size="22px" />
        </template>
      </AButton>
      <ADivider direction="vertical" margin="5px" />
      <AUpload class="panel-item" @change="importJson" :auto-upload="false" :show-file-list="false">
        <template #upload-button>
          <AButton class="panel-item" type="text">
            <template #icon>
              <IconImport size="22px" />
            </template>
          </AButton>
        </template>
      </AUpload>
    </Panel>
  </VueFlow>
</template>

<style scoped lang="scss">
@import 'flow-designer';
</style>
