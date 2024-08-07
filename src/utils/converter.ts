import type { Elements, GraphEdge, GraphNode, Node as VueFlowNode } from '@vue-flow/core'
import { isEdge, isNode, MarkerType } from '@vue-flow/core'
import type { TableColumnData } from '@arco-design/web-vue'

import type {
  ComponentAttr,
  Connection,
  Flow,
  Node,
  NodeElementData,
  Property,
  Service,
  ValidateRule
} from '@/types/flow'
import { uuid } from '@/utils/util-func'
import { uniq } from 'lodash'
import type { Position } from '@vueuse/core' //需要使用的组件
import ConditionFilter from '@/components/ConditionFilter/ConditionFilter.vue'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import MapEditor from '@/components/MapEditor/MapEditor.vue'
import ListEditor from '@/components/ListEditor/ListEditor.vue'
import BasicTypeListEditor from '@/components/BasicTypeListEditor/BasicTypeListEditor.vue' //获取当前节点所有的前置节点

//获取当前节点所有的前置节点
export function getAllIncomers(
  nodeId: string | undefined,
  getIncomers: (nodeOrId: Node | string) => GraphNode[]
): VueFlowNode[] {
  if (!nodeId) {
    return []
  }
  let nodeIncomers = getIncomers(nodeId)
  if (nodeIncomers.length) {
    for (const node of nodeIncomers) {
      const preIncomers = getAllIncomers(node.id, getIncomers) as GraphNode[]
      nodeIncomers = nodeIncomers.concat(preIncomers)
    }
  }
  return uniq(nodeIncomers)
}

export function toNode(graphNode: VueFlowNode): Node {
  const position = graphNode.position
  const parameters = { ...graphNode.data.parameters } || {}
  const loop = graphNode.data?.loop
  //删除输入
  delete parameters['inputData']
  const serviceId = graphNode.data.serviceId || ''
  return {
    id: graphNode.id,
    label: graphNode.data.label || (graphNode.label as string),
    serviceId: serviceId,
    type: graphNode.type as string,
    position: { ...position },
    data: parameters,
    loop
  }
}

export function toGraphNode(node: Node): VueFlowNode {
  const nodeData: Record<string, any> = {}
  nodeData.serviceId = node.serviceId
  nodeData.label = node.label
  nodeData.parameters = node.data
  nodeData.loop =
    node.loop && Object.keys(node.loop).length
      ? node.loop
      : {
          loopCardinality: null,
          collectionString: null,
          elementVariable: null,
          sequential: false,
          completionCondition: null
        }
  return {
    ...node,
    data: nodeData
  }
}

const nodeTypeMap: Record<string, string> = {
  IF: 'IF',
  LoopEachItem: 'LOOP_EACH_ITEM'
}

export function serviceToGraphNode(service: Service, position?: Position): VueFlowNode {
  const nodeData: Record<string, any> = {}
  nodeData.serviceId = service.id
  nodeData.label = service.name
  nodeData.parameters = {}
  nodeData.loop = {}
  nodeData.avatar = service.avatar
  return {
    type: nodeTypeMap[service.name] || 'SERVICE',
    id: uuid(8, true),
    position: position || { x: 0, y: 0 },
    data: nodeData,
    label: service.name
  }
}

export function toConnect(edge: GraphEdge): Connection {
  return {
    id: edge.id,
    source: edge.source,
    target: edge.target,
    sourceX: edge.sourceX,
    sourceY: edge.sourceY,
    targetX: edge.targetX,
    targetY: edge.targetY,
    sourcePointType: edge.data?.sourcePointType,
    targetPointType: edge.data?.targetPointType,
    expression: edge.data?.expression
  }
}

export function toGraphEdge(connection: Connection): GraphEdge {
  return {
    ...connection,
    id: `e${connection.source}_${connection.target}`,
    markerEnd: MarkerType.ArrowClosed,
    data: {
      sourcePointType: connection.sourcePointType,
      targetPointType: connection.targetPointType
    },
    type: 'edge'
  } as GraphEdge
}

export function toFlow<N extends VueFlowNode, E extends GraphEdge>(
  nodes: N[] | undefined,
  edges: E[] | undefined
): Flow {
  const id = uuid(8, true)
  return {
    id: id,
    name: id,
    nodes: nodes?.map((node) => toNode(node)),
    connections: edges?.map((edge) => toConnect(edge))
  }
}

export function elementsToFlow(elements: Elements<NodeElementData>): Flow {
  const id = uuid(8, true)
  return {
    id: id,
    name: id,
    nodes: getNodes(elements)?.map((node) => toNode(node)),
    connections: getEdges(elements)?.map((edge) => toConnect(edge))
  }
}

export function getNodes(elements: Elements<NodeElementData>): VueFlowNode[] {
  return elements.filter((item) => isNode(item)) as VueFlowNode[]
}

export function getEdges(elements: Elements<NodeElementData>): GraphEdge[] {
  return elements.filter((item) => isEdge(item)) as GraphEdge[]
}

export function toComponentAttr(property: Property): ComponentAttr {
  if (property.type === 'Condition') {
    return {
      cmp: ConditionFilter,
      property: property
    }
  }

  if (property.options) {
    return {
      cmp: 'ASelect',
      attrs: { options: property.options },
      property: property
    }
  }

  if (!property.type || property.type == 'String') {
    return {
      cmp: ExpressInput,
      property: property
    }
  }

  if (property.type === 'Map') {
    return {
      cmp: MapEditor,
      property: property
    }
  }

  if (['Integer', 'Float', 'Double', 'Number', 'BigDecimel'].indexOf(property.type) > -1) {
    if (property.validateRules) {
      const ruleMap: Record<string, ValidateRule> = {}
      property.validateRules.forEach((rule) => {
        ruleMap[rule.validateType as string] = rule
      })
      if (Object.keys(ruleMap).indexOf('Min')) {
        return {
          cmp: 'ASlider',
          attrs: {
            step: property.type === 'Integet' ? 1 : 0.1,
            showInput: true,
            showTooltip: true,
            min: Number((ruleMap['Min'] || ruleMap['DecimalMin']).attributes['value']),
            max: Number((ruleMap['Max'] || ruleMap['DecimalMax']).attributes['value'])
          },
          property: property
        }
      }
    }
    return {
      cmp: 'AInputNumber',
      property: property
    }
  }

  if (property.type === 'List' || property.type === 'Set') {
    const columns: TableColumnData[] = []
    const columnCmp: Record<string, ComponentAttr> = {}
    if (property.properties?.length || 0 > 1) {
      property.properties?.forEach((child) => {
        columns.push({
          title: child.displayName || child.name,
          dataIndex: child.name
        })

        columnCmp[child.name] = toComponentAttr(child)
      })
    } else {
      columns.push({
        title: '',
        dataIndex: 'value'
      })
    }

    return {
      cmp: property.properties?.length == 1 ? BasicTypeListEditor : ListEditor,
      attrs: { columns, columnCmp },
      property: property
    }
  }

  return {
    cmp: ExpressInput,
    property: property
  }
}

export function toComponentAttrs(properties: Property[]): ComponentAttr[] {
  return properties.map((property) => toComponentAttr(property))
}
