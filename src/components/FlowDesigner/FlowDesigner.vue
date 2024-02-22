<script lang="ts" setup>
import type { ElementData } from '@vue-flow/core'
import { Panel, VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import {
  IconSunFill,
  IconMoonFill,
  IconCloudDownload,
  IconImport
} from '@arco-design/web-vue/es/icon'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import Node, { type Props } from '@/components/Node/Node.vue'
import { toFlow } from '@/utils/converter'
import type { FileItem } from '@arco-design/web-vue'
import type { Flow } from '@/types/flow'
import FromRenderer, { type Property } from '@/components/FromRenderer/FromRenderer.vue'

const nodeTypes = {
  service: markRaw(Node)
}

const nodeData = ref({})
const [formVisible, toggleForm] = useToggle(false)

const properties = ref<Property[]>([
  { name: 'test_input', displayName: '测试输入', type: 'String' }
])

const defaultEditFunc = (node: Props) => {
  nodeData.value = node.data
  toggleForm()
}

const elements:ElementData = ref([
  {
    id: '1',
    type: 'service',
    label: 'Node 1',
    position: { x: 250, y: 5 },
    class: 'light',
    events: {
      edit: defaultEditFunc
    }
  }
])

const { onConnect, addEdges, getNodes, getEdges } = useVueFlow({
  minZoom: 0.2,
  maxZoom: 4
})

onConnect((param) => {
  addEdges({ ...param, markerEnd: MarkerType.ArrowClosed })
})

const [dark, toggleClass] = useToggle(false)

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
    const nodes= flowDefine.nodes || []
    const edges = flowDefine.connections?.map((connection) => ({
      ...connection,
      id: `e${connection.source}_${connection.target}`,
      markerEnd: MarkerType.ArrowClosed
    })) ||[]
    elements.value = [...nodes, ...edges]
  }
}
</script>

<template>
  <VueFlow v-model="elements" :class="{ dark }" class="vue-flow-basic" :node-types="nodeTypes">
    <Background :pattern-color="dark ? '#FFFFFB' : '#aaa'" :gap="8" />
    <Controls />
    <Panel class="flow-designer-panel" position="top-right" style="display: flex; align-items: center">
      <ASwitch class="panel-item" type="line" @change="(v) => toggleClass(v as boolean)" checked-color="black">
        <template #checked-icon>
          <IconMoonFill style="color: orange" />
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
    <AModal v-model:visible="formVisible">
      <FromRenderer v-model="nodeData" :properties="properties" />
    </AModal>
  </VueFlow>
</template>

<style scoped lang="scss">
@import 'flow-designer';
</style>
