<script lang="ts" setup>
import type { Node, Edge } from '@vue-flow/core'
import { toGraphNode, toGraphEdge } from '@/utils/converter'
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
  type NodeAction,
  type Props,
  type ToolBarData
} from '@/components/ServiceNode/SeviceNode.vue'
import type { FileItem } from '@arco-design/web-vue'
import type { Flow } from '@/types/flow'
import { type Property } from '@/components/FormRenderer/FormRenderer.vue'
import NodeFormModel from '@/components/NodeFormModal/NodeFormModal.vue'
import json from './defaultFlow.json'
import { computed } from 'vue'
const nodeTypes = {
  service: markRaw(ServiceNode)
}

const nodes = ref<Node<ElementData & ToolBarData, NodeAction>[]>()
const edges = ref<Edge[]>()
const selectedNodeId = ref<string>();
const [formVisible, toggleForm] = useToggle(false)
const [dark, toggleClass] = useToggle(false)
const properties = ref<Property[]>([
  {
    type: 'Map',
    name: 'headers',
    displayName: null,
    description: null,
    defaultValue: null,
    options: null,
    properties: null
  },
  {
    type: 'String',
    name: 'url',
    displayName: null,
    description: null,
    defaultValue: null,
    options: null,
    properties: null
  },
  {
    type: 'Method',
    name: 'method',
    displayName: null,
    description: null,
    defaultValue: 'GET',
    options: [
      {
        name: 'GET',
        value: 'GET',
        description: null
      },
      {
        name: 'POST',
        value: 'POST',
        description: null
      },
      {
        name: 'HEAD',
        value: 'HEAD',
        description: null
      },
      {
        name: 'OPTIONS',
        value: 'OPTIONS',
        description: null
      },
      {
        name: 'PUT',
        value: 'PUT',
        description: null
      },
      {
        name: 'DELETE',
        value: 'DELETE',
        description: null
      },
      {
        name: 'TRACE',
        value: 'TRACE',
        description: null
      },
      {
        name: 'CONNECT',
        value: 'CONNECT',
        description: null
      },
      {
        name: 'PATCH',
        value: 'PATCH',
        description: null
      }
    ],
    properties: null
  },
  {
    type: 'Map',
    name: 'params',
    displayName: null,
    description: null,
    defaultValue: null,
    options: null,
    properties: null
  }])


const defaultEditFunc = (node: Props) => {
  selectedNodeId.value = node.id
  toggleForm()
}

function defaultRun(){

}

const defaultEvents = {
  edit: defaultEditFunc,
  run: defaultRun
}

const { onConnect, addEdges, getNodes, getEdges, findNode } = useVueFlow({
  minZoom: 0.2,
  maxZoom: 4
})

const selectedNode = computed(() => findNode(selectedNodeId.value))

onConnect((param) => {
  addEdges({ ...param, markerEnd: MarkerType.ArrowClosed })
})

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
      :description="'## HTTP Request'" />
  </VueFlow>
</template>

<style scoped lang="scss">
@import 'flow-designer';
</style>
