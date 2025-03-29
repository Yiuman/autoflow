<script lang="ts" setup>
import type { NodeMouseEvent } from '@vue-flow/core'
import {
  type Connection,
  ConnectionMode,
  type EdgeMouseEvent,
  type Elements,
  type GraphEdge,
  MarkerType,
  Panel,
  useVueFlow,
  type ViewportTransform,
  VueFlow,
  type XYPosition
} from '@vue-flow/core'
import { elementsToFlow, getAllIncomers, serviceToGraphNode, toGraphEdge, toGraphNode, toNode } from '@/utils/converter'
import {
  IconCloudDownload,
  IconPauseCircleFill,
  IconPlayCircleFill,
  IconSave,
  IconUpload
} from '@arco-design/web-vue/es/icon'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'

import EditableEdge from '@/components/EditableEdge/EditableEdge.vue'
import { type FileItem, Notification } from '@arco-design/web-vue'
import type { BoundingBox, Flow, NodeElementData, Position, Property, Service, VueFlowNode } from '@/types/flow'
import NodeFormModel from '@/components/NodeFormModal/NodeFormModal.vue'
import json from './defaultFlow.json'
import { computed } from 'vue'
import { downloadByData } from '@/utils/download'
import { getContainerClientXY } from '@/utils/util-func'
import { executeNode, getExecutableFlowInst, stopExecution, type WorkflowInst } from '@/api/execution'
import { useServiceStore } from '@/stores/service'
import ServiceNode from '@/components/ServiceNode/ServiceNode.vue'
import IfNode from '@/components/IfNode/IfNode.vue'
import LoopEachItemNode from '@/components/LoopEachItemNode/LoopEachItemNode.vue'
import SearchModal from '@/components/SearchModal/SearchModal.vue'
import useTheme from '@/hooks/theme'
import workflowApi from '@/api/workflow'
import { useRoute } from 'vue-router'
import { getResultData } from '@/utils/flow'
import { I18N } from '@/locales/i18n'
import { executeFlowSSE } from '@/views/FlowDesigner/flowsse'
import { useProvideNodeDataStore } from '@/hooks/useNodeDataStore'

const [theme] = useTheme()

const route = useRoute()
//---------------------------- 初始化定义数据 ----------------------------
const nodeTypes = {
  SERVICE: markRaw(ServiceNode),
  IF: markRaw(IfNode),
  LOOP_EACH_ITEM: markRaw(LoopEachItemNode)
}

const edgeTypes = {
  edge: markRaw(EditableEdge)
}

const serviceStore = useServiceStore()
const elements = ref<Elements<NodeElementData>>([])
const {
  onConnect,
  addEdges,
  addNodes,
  findNode,
  updateNodeData,
  getIncomers,
  onConnectStart,
  onConnectEnd,
  getViewport,
  fitView
} = useVueFlow({
  minZoom: 0.2,
  maxZoom: 4
})

onMounted(async () => {
  if (route.query.flowId) {
    const workflow = await workflowApi.get(route.query.flowId as string)
    doParseJson(workflow.flowStr || '{}')
  } else {
    doParseJson(JSON.stringify(json))
  }
  await nextTick()
  setTimeout(() => fitView({ maxZoom: 1 }), 0)
})

//---------------------------- 节点表单操作 ----------------------------
const selectedNodeId = ref<string>()
const [formVisible, toggleForm] = useToggle(false)
const { selectedNode } = useProvideNodeDataStore()

function onNodeClick(nodeMouseEvent: NodeMouseEvent) {
  selectedNodeId.value = nodeMouseEvent.node.id
  selectedNode.value = findNode<NodeElementData>(selectedNodeId.value)
}




const properties = computed<Property[]>(() => {
  if (!selectedNode.value) {
    return []
  }
  return serviceStore.getServiceById(selectedNode.value?.data.serviceId).properties
})

const description = computed<string | undefined>(
  () => serviceStore.getServiceById(selectedNode.value?.data.serviceId)?.description
)

//---------------------------- 节点事件 ----------------------------
const defaultEditFunc = (node: VueFlowNode) => {
  selectedNodeId.value = node.id
  toggleForm()
}

async function defaultRun(node: VueFlowNode) {
  selectedNodeId.value = node.id
  updateNodeData(node.id, { running: true })

  const executeNodeData = toNode(toRaw(node))
  const nodeData = executeNodeData.data || {}
  const allIncomers = getAllIncomers(node.id, getIncomers)
  const inputData: Record<string, any[]> = {}
  const variables: Record<string, any[]> = {}
  for (const incomer of allIncomers) {
    inputData[incomer.id] = getResultData(incomer.data?.executionResult)
    variables[incomer.id] = incomer.data?.parameters
  }
  nodeData._inputData = inputData
  nodeData._variables = variables
  executeNodeData.data = nodeData
  const executionResult = await executeNode(executeNodeData)
  updateNodeData(node.id, {
    executionResult: executionResult.length > 1 ? executionResult : executionResult[0],
    running: false
  })
}

const defaultEvents = {
  edit: defaultEditFunc,
  run: defaultRun
}

//---------------------------- 处理连线逻辑/添加节点逻辑 ----------------------------
const isConnect = ref<boolean>(false)
const basicEdgeProps = {
  data: {},
  type: 'edge',
  markerEnd: {
    type: MarkerType.ArrowClosed,
    // color: 'rgba(var(--primary-4))'
  },
  style: {
    // stroke: 'rgba(var(--primary-4))',
    'stroke-width':1.3
  }
}
function doConnect(connection: Connection) {
  isConnect.value = true
  const sourceNode = findNode<NodeElementData>(connection.source)
  const addEdge: GraphEdge = {
    ...connection,
    ...basicEdgeProps
  } as GraphEdge
  addEdge.data.sourcePointType = connection.sourceHandle
  addEdge.data.targetPointType = connection.targetHandle
  if (sourceNode && sourceNode.type === 'IF') {
    if (connection.sourceHandle == `IF_TRUE`) {
      addEdge.data.expression = '${inputData.' + sourceNode.id + '.result}'
    } else {
      addEdge.data.expression = '${!inputData.' + sourceNode.id + '.result}'
    }
  }

  addEdges(addEdge)
}

onConnect(doConnect)

const selectHandlerId = ref<string | null | undefined>()
const connectEndOffset = ref<XYPosition>()
onConnectStart((param) => {
  selectedNodeId.value = param.nodeId
  selectHandlerId.value = param.handleId
})
onConnectEnd((param) => {
  connectEndOffset.value = getContainerClientXY(param)
  const targetElement = param?.target as HTMLElement
  if (!isConnect.value && !targetElement?.dataset?.id) {
    toggleSearchModalVisible()
  }
  isConnect.value = false
})

//处理连线的toolbar显示与隐藏
function edgeMouseMove(edgeMouseEvent: EdgeMouseEvent) {
  const edgeToolBar = document.getElementById(`edge-toolbar-${edgeMouseEvent.edge.id}`)
  if (edgeMouseEvent.event.type === 'mousemove') {
    edgeToolBar?.classList.add('edge-toolbar-show')
  } else {
    edgeToolBar?.classList.remove('edge-toolbar-show')
  }
}

const vueFlow = ref()

function calculateDefaultPosition(
  viewport: ViewportTransform,
  bounding: BoundingBox,
  connectEndOffset?: Position
) {
  let defaultXy
  if (connectEndOffset && connectEndOffset.x) {
    // 如果存在连接结束偏移量，计算相对位置
    defaultXy = {
      x: connectEndOffset.x - viewport.x - bounding.left,
      y: connectEndOffset.y - viewport.y - bounding.top
    }
  } else if (selectedNode.value) {
    defaultXy = { x: selectedNode.value.position.x + 200, y: selectedNode.value.position.y }
  } else {
    // 如果没有连接结束偏移且没有现有节点，将新节点放置在 vueFlow 中心位置
    defaultXy = { x: bounding.right / 2, y: bounding.bottom / 2 }
  }

  return defaultXy
}

function addNode(node: Service) {
  const viewport = getViewport()
  const { top, bottom, left, right } = useElementBounding(vueFlow)
  const bounding: BoundingBox = {
    top: top.value,
    bottom: bottom.value,
    left: left.value,
    right: right.value
  }
  // 计算默认位置
  const defaultXy = calculateDefaultPosition(viewport, bounding, connectEndOffset?.value)
  // 创建新的节点
  const newNode = {
    ...serviceToGraphNode(node, defaultXy),
    events: defaultEvents
  }
  // 添加新的节点到流程图中
  addNodes(newNode)

  // 如果有选中的处理器ID，连接新节点和选中的节点
  if (selectHandlerId.value) {
    const isInputHandler = selectHandlerId.value === 'INPUT'
    const newConnect = isInputHandler
      ? {
          source: newNode.id,
          target: selectedNodeId.value as string,
          sourceHandle: 'OUTPUT',
          targetHandle: selectHandlerId.value
        }
      : {
          source: selectedNodeId.value as string,
          target: newNode.id,
          sourceHandle: selectHandlerId.value,
          targetHandle: 'INPUT'
        }
    doConnect(newConnect)
  }

  // 重置选中处理器和选中节点的ID
  selectHandlerId.value = undefined
  selectedNodeId.value = undefined
  isConnect.value = false
  // 切换搜索模态框的可见性
  toggleSearchModalVisible()
}

//---------------------------- 导入导出 ----------------------------
function exportJson() {
  const jsonStr = JSON.stringify(elementsToFlow(elements.value))
  downloadByData(
    new Blob([jsonStr], {
      type: 'text/plain'
    }),
    'config.json'
  )
}

async function saveWorkflow() {
  const flow: Flow = elementsToFlow(elements.value)
  const jsonStr = JSON.stringify(flow)
  await workflowApi.save({ id: route.query.flowId as string, flowStr: jsonStr })
  Notification.success('save success')
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
  const flowNodes = flowDefine.nodes
  if (!flowNodes || !flowNodes.length) {
    return
  }
  const nodes: VueFlowNode[] = flowNodes?.map((node) => {
    const graphNode = toGraphNode(node)
    graphNode.events = defaultEvents
    graphNode.data.service = serviceStore.getServiceById(graphNode.data.serviceId)
    return graphNode
  }) as VueFlowNode[]
  const edges: GraphEdge[] = flowDefine.connections?.map((connection) => ({
    ...toGraphEdge(connection), markerEnd: basicEdgeProps.markerEnd, style: basicEdgeProps.style
  })) as GraphEdge[]
  elements.value = [...nodes, ...edges]
}

//---------------------------- 节点搜索弹窗逻辑 ----------------------------

const searchModalValue = ref<string>()
const [searchModalVisible, toggleSearchModalVisible] = useToggle(false)
const matchServices = computed(() => {
  if (searchModalValue.value) {
    return serviceStore.getServices.filter((service) => {
      return service.name.toLowerCase().indexOf(searchModalValue.value || '') > -1
    })
  }
  return serviceStore.getServices
})

function searchModalInput(event: InputEvent) {
  searchModalValue.value = event.data as string
}

//---------------------------- 工作流执行 ----------------------------
const running = ref<boolean>(false)
const executeFlowInst = ref<WorkflowInst | undefined>()

async function runFlow() {
  running.value = true
  const flow = elementsToFlow(elements.value)
  executeFlowInst.value = await getExecutableFlowInst(flow)
  flow.nodes?.forEach((node) => {
    updateNodeData(node.id, { executionResult: null })
  })
  executeFlowSSE(executeFlowInst.value, findNode, updateNodeData, {
    onClose: () => {
      executeFlowInst.value = undefined
      running.value = false
    }
  })
}

async function stopFlow() {
  if (executeFlowInst.value) {
    await stopExecution({ id: executeFlowInst.value.id })
    executeFlowInst.value = undefined
  }

  running.value = false
}
</script>

<template>
  <VueFlow
    ref="vueFlow"
    v-model="elements"
    :class="{ theme }"
    :connection-mode="ConnectionMode.Strict"
    :edge-types="edgeTypes"
    :node-types="nodeTypes"
    class="vue-flow-basic"
    :no-drag-class-name="'no-drag'"
    @edge-mouse-move="edgeMouseMove"
    @edge-mouse-leave="edgeMouseMove"
    @node-click="onNodeClick"
  >
    <!-- 背景 -->
    <Background :gap="8" :pattern-color="theme ? '#FFFFFB' : '#aaa'" />
    <!-- 面板控制器 -->
    <Controls />
    <!-- 左上角的操作按钮 -->
    <Panel
      class="flow-designer-panel"
      position="top-right"
      style="display: flex; align-items: center"
    >
      <SearchModal
        v-model:visible="searchModalVisible"
        :placeholder="I18N('flowDesigner.searchAddNode', 'search and add node')"
        @input="(event) => searchModalInput(event as InputEvent)"
      >
        <AList>
          <AListItem
            v-for="serviceItem in matchServices"
            :key="serviceItem.name"
            @click="() => addNode(serviceItem)"
          >
            <AListItemMeta :title="I18N(`${serviceItem.id}.name`, serviceItem.name)">
              <template #avatar>
                <AImage
                  v-if="serviceItem.avatar"
                  :height="68"
                  :preview="false"
                  :src="serviceItem.avatar"
                  :width="68"
                />
                <AAvatar v-else :size="68" shape="square">
                  {{ I18N(`${serviceItem.id}.name`, serviceItem.name) }}
                </AAvatar>
              </template>
            </AListItemMeta>
          </AListItem>
        </AList>
      </SearchModal>
      <ADivider direction="vertical" margin="5px" />
      <AButton class="panel-item" type="text" @click="saveWorkflow">
        <template #icon>
          <IconSave size="22px" />
        </template>
      </AButton>
      <ADivider direction="vertical" margin="5px" />
      <AButton class="panel-item" type="text" @click="exportJson">
        <template #icon>
          <IconCloudDownload size="22px" />
        </template>
      </AButton>
      <ADivider direction="vertical" margin="5px" />
      <AUpload :auto-upload="false" :show-file-list="false" class="panel-item" @change="importJson">
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
    <div class="execute-flow-btn" @click="running ? stopFlow() : runFlow()">
      <AButton type="primary">
        <template #icon>
          <IconPauseCircleFill v-if="running" />
          <IconPlayCircleFill v-else />
        </template>
        Execute Flow
      </AButton>
    </div>

    <!-- 节点的表单弹窗 -->
    <NodeFormModel
      v-if="selectedNode"
      v-model="selectedNode"
      v-model:visible="formVisible"
      :description="description"
      :properties="properties"
    />
  </VueFlow>
</template>

<style scoped lang="scss">
@use 'flow-designer';
</style>
