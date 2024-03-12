<script lang="ts" setup>
import type { Node, GraphEdge } from '@vue-flow/core'
import { toGraphNode, toGraphEdge, toNode } from '@/utils/converter'
import { Panel, VueFlow, useVueFlow, MarkerType, type ElementData } from '@vue-flow/core'
import {
  IconSunFill,
  IconMoonFill,
  IconCloudDownload,
  IconImport
} from '@arco-design/web-vue/es/icon'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { toFlow } from '@/utils/converter'
import ServiceNode, {
  type ToolBarData
} from '@/components/ServiceNode/SeviceNode.vue'
import type { FileItem } from '@arco-design/web-vue'
import type { Flow, Property } from '@/types/flow'
import NodeFormModel from '@/components/NodeFormModal/NodeFormModal.vue'
import json from './defaultFlow.json'
import { computed } from 'vue'
import { downloadByUrl } from '@/utils/download'
import { executeNode } from '@/api/execution'
import { useServiceStore } from '@/stores/service'

type NodeElementData = ToolBarData & Record<string, ElementData>
type FlowNode = Node<NodeElementData>
const nodeTypes = {
  service: markRaw(ServiceNode)
}

const serviceStore = useServiceStore();
const nodes = ref<FlowNode[]>()
const edges = ref<GraphEdge[]>()
const selectedNodeId = ref<string>();
const [formVisible, toggleForm] = useToggle(false)
const [dark, toggleClass] = useToggle(false)
const properties = computed<Property[]>(() => {
  if (!selectedNode.value) {
    return [];
  }
  return serviceStore.getServiceByName(selectedNode.value?.data.serviceName).properties;
})

const description = computed<string | undefined>(() => serviceStore.getServiceByName(selectedNode.value?.data.serviceName)?.description)




const { onConnect, addEdges, findNode, updateNodeData } = useVueFlow({
  minZoom: 0.2,
  maxZoom: 4
})


const defaultEditFunc = (node: FlowNode) => {
  selectedNodeId.value = node.id
  toggleForm()
}

const defaultEvents = {
  edit: defaultEditFunc,
  run: defaultRun
}
async function defaultRun(node: FlowNode) {
  selectedNodeId.value = node.id
  const executionData = await executeNode(toNode(toRaw(node)));
  console.warn("executeData", toNode(toRaw(node)), executionData)
  updateNodeData(node.id, { executionData })
}


const selectedNode = computed(() => findNode<NodeElementData>(selectedNodeId.value))

onConnect((param) => {
  addEdges({ ...param, markerEnd: MarkerType.ArrowClosed })
})

function exportJson() {
  downloadByUrl({
    url: 'data:text/plain,' + JSON.stringify(toFlow(nodes.value, edges.value)),
    fileName: 'config.json'
  })
}

function importJson(fileList: FileItem[]): void {
  const reader = new FileReader()
  const fileItem = fileList[0]
  reader.readAsText(fileItem.file as Blob)
  reader.onload = function () {
    doParseJson(reader.result as string)
  }
}

function doParseJson(json: string) {
  const flowDefine: Flow = JSON.parse(json)
  const flowNodes = flowDefine.nodes;
  nodes.value = flowNodes?.map(node => ({ ...toGraphNode(node), events: defaultEvents }))
  edges.value = flowDefine.connections?.map((connection) => ({ ...toGraphEdge(connection) }))
}

onMounted(() => {
  doParseJson(JSON.stringify(json));
  serviceStore.fetchServices();
})
</script>

<template>
  <VueFlow :nodes="nodes" :edges="edges" :class="{ dark }" class="vue-flow-basic" :node-types="nodeTypes">
    <Background :pattern-color="dark ? '#FFFFFB' : '#aaa'" :gap="8" />
    <Controls />
    <Panel class="flow-designer-panel" position="top-right" style="display: flex; align-items: center">
      <ASwitch class="panel-item" type="line" @change="() => toggleClass()" checked-color="black" size="medium">
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
    <NodeFormModel v-if="selectedNode" v-model="selectedNode" v-model:visible="formVisible" :properties="properties"
      :description="description" />
  </VueFlow>
</template>

<style scoped lang="scss">
@import 'flow-designer';
</style>
