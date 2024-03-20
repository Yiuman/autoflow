<script lang="ts" setup>
import type { EdgeMouseEvent, GraphEdge } from '@vue-flow/core'
import { toGraphNode, toGraphEdge, toNode } from '@/utils/converter'
import { Panel, VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import {
  IconSunFill,
  IconMoonFill,
  IconCloudDownload,
  IconUpload,
  IconPlayCircleFill,
  IconPauseCircleFill
} from '@arco-design/web-vue/es/icon'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { toFlow, getAllIncomers } from '@/utils/converter'
import ServiceNode from '@/components/ServiceNode/SeviceNode.vue'
import EditableEdge from '@/components/EditableEdge/EditableEdge.vue'
import { type FileItem } from '@arco-design/web-vue'
import type { Flow, Property, VueFlowNode, NodeElementData, ExecutionData, Service } from '@/types/flow'
import NodeFormModel from '@/components/NodeFormModal/NodeFormModal.vue'
import json from './defaultFlow.json'
import { computed } from 'vue'
import { downloadByUrl } from '@/utils/download'
import { executeNode } from '@/api/execution'
import { useServiceStore } from '@/stores/service'
import SwitchNode from '@/components/SwitchNode/SwitchNode.vue'
import SearchModal from '@/components/SearchModal/SearchModal.vue'


const nodeTypes = {
  SERVICE: markRaw(ServiceNode),
  SWITCH: markRaw(SwitchNode)
}

const edgeTypes = {
  edge: markRaw(EditableEdge)
}

const serviceStore = useServiceStore();
serviceStore.fetchServices();
const nodes = ref<VueFlowNode[]>()
const edges = ref<GraphEdge[]>()
const selectedNodeId = ref<string>();
const [formVisible, toggleForm] = useToggle(false)
const properties = computed<Property[]>(() => {
  if (!selectedNode.value) {
    return [];
  }
  return serviceStore.getServiceByName(selectedNode.value?.data.serviceName).properties;
})

const description = computed<string | undefined>(() => serviceStore.getServiceByName(selectedNode.value?.data.serviceName)?.description)

const { onConnect, addEdges, findNode, updateNodeData, getIncomers } = useVueFlow({
  minZoom: 0.2,
  maxZoom: 4
})

//切换主题
const [dark, toggleClass] = useToggle(false)
watch(dark, () => {
  if (dark.value) {
    document.body.setAttribute('arco-theme', 'dark')
  } else {
    document.body.removeAttribute('arco-theme');
  }
})


const defaultEditFunc = (node: VueFlowNode) => {
  selectedNodeId.value = node.id
  toggleForm()
}


async function defaultRun(node: VueFlowNode) {
  selectedNodeId.value = node.id
  updateNodeData(node.id, { running: true })

  const executeNodeData = toNode(toRaw(node));
  const nodeData = executeNodeData.data || {};
  const allIncomers = getAllIncomers(node.id, getIncomers);
  const inputData: Record<string, ExecutionData[]> = {}
  for (const incomer of allIncomers) {
    inputData[incomer.id] = [incomer.data.executionData]
  }
  nodeData.inputData = inputData;
  executeNodeData.data = nodeData;
  const executionData = await executeNode(executeNodeData);
  updateNodeData(node.id, { executionData, running: false })
}

const defaultEvents = {
  edit: defaultEditFunc,
  run: defaultRun
}


const selectedNode = computed(() => findNode<NodeElementData>(selectedNodeId.value))

//处理连线逻辑
onConnect((param) => {
  const sourceNode = findNode<NodeElementData>(param.source);
  const addEdge: GraphEdge = { ...param, markerEnd: MarkerType.ArrowClosed, type: 'edge', data: {} } as GraphEdge;
  if (sourceNode && sourceNode.type === 'SWITCH') {
    if (param.sourceHandle == `${sourceNode.id}__handel-top`) {
      addEdge.data.expression = "${" + `inputeData[${sourceNode.id}].json.result` + "}"
    } else {
      addEdge.data.expression = "${!" + `inputeData[${sourceNode.id}].json.result` + "}"
    }
  }
  addEdges(addEdge)
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

//处理连线的toolbar显示与隐藏
function edgeMouseMove(edgeMouseEvent: EdgeMouseEvent) {
  const edgeToolBar = document.getElementById(`edge-toolbar-${edgeMouseEvent.edge.id}`);
  if (edgeMouseEvent.event.type === 'mousemove') {
    edgeToolBar?.classList.add('edge-toolbar-show')
  } else {
    edgeToolBar?.classList.remove('edge-toolbar-show')
  }
}


const [executeFlow, toggelExecute] = useToggle(false);

const searchModalValue = ref<string>()
const matchServices = computed(() => {
  if (searchModalValue.value) {
    return serviceStore.getServices.filter(service => {
      return service.name.toLowerCase().indexOf(searchModalValue.value || '') > -1;
    })
  }
  return serviceStore.getServices;
})

function searchModalInput(event: InputEvent) {
  searchModalValue.value = (event.data) as string
}

function addNode(node: Service) {

}

</script>

<template>
  <VueFlow :nodes="nodes" :edges="edges" @edge-mouse-move="edgeMouseMove" @edge-mouse-leave="edgeMouseMove"
    :class="{ dark }" class="vue-flow-basic" :node-types="nodeTypes" :edge-types="edgeTypes">
    <!-- 背景 -->
    <Background :pattern-color="dark ? '#FFFFFB' : '#aaa'" :gap="8" />
    <!-- 面板控制器 -->
    <Controls />
    <!-- 左上角的操作按钮 -->
    <Panel class="flow-designer-panel" position="top-right" style="display: flex; align-items: center">
      <SearchModal @input="(event) => searchModalInput(event as InputEvent)">
        <AList>
          <AListItem v-for="serviceItem in matchServices" :key="serviceItem.name" @click="() => addNode(serviceItem)">
            <AListItemMeta :title="serviceItem.name">
              <template #avatar>
                <AAvatar shape="square" :size="68">
                  <div class="node-switch-label">{{ serviceItem.name }}</div>
                </AAvatar>
              </template>
            </AListItemMeta>
          </AListItem>
        </AList>
      </SearchModal>
      <ADivider direction="vertical" margin="5px" />
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
              <IconUpload size="22px" />
            </template>
          </AButton>
        </template>
      </AUpload>

    </Panel>

    <!-- 执行按钮 -->
    <div class="execute-flow-btn" @click="() => toggelExecute()">
      <AButton type="primary">
        <template #icon>
          <IconPauseCircleFill v-if="executeFlow" />
          <IconPlayCircleFill v-else />
        </template>
        Execute Flow
      </AButton>
    </div>

    <!-- 节点的表单弹窗 -->
    <NodeFormModel v-if="selectedNode" v-model="selectedNode" v-model:visible="formVisible" :properties="properties"
      :description="description" />
  </VueFlow>
</template>

<style scoped lang="scss">
@import 'flow-designer';
</style>
