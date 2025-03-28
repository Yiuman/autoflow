import {
    type Elements,
    type GraphEdge,
    type GraphNode,
    isEdge,
    isNode,
    MarkerType,
    type Node as VueFlowNode
} from '@vue-flow/core'
import type {TableColumnData} from '@arco-design/web-vue'

import type {ComponentAttr, Connection, Flow, GenericType, Node, NodeElementData, Property, Service} from '@/types/flow'
import {uuid} from '@/utils/util-func'
import {uniq} from 'lodash'
import type {Position} from '@vueuse/core' //需要使用的组件
import {I18N} from '@/locales/i18n'
import {toComponentAttr} from '@/utils/cmp'

//获取当前节点所有的前置节点
export function getAllIncomers(
  nodeId: string | undefined,
  getIncomers: (nodeOrId: Node | string) => GraphNode[]
): VueFlowNode[] {
  if (!nodeId) {
    return []
  }
  let nodeIncomers = getIncomers(nodeId)
  if (!nodeIncomers || nodeIncomers.length === 0) {
    return []
  }

  if (nodeIncomers && nodeIncomers.length > 0) {
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
  nodeData.label = node.label || I18N(`${node.serviceId}.name`, node.label)
  nodeData.parameters = node.data
  nodeData.service = nodeData.loop =
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
  nodeData.service = service
  nodeData.label = I18N(`${service.id}.name`, service.name)
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

export function objectKeysToColumn(obj: any): TableColumnData[] {
  if (!obj) {
    return []
  }
  return Object.keys(obj).map((key) => {
    let slotName = undefined
    if (obj[key] instanceof Object) {
      slotName = 'typeObjectColumn'
    }
    return {
      title: key,
      dataIndex: key,
      align: 'center',
      ellipsis: true,
      tooltip: true,
      slotName
    }
  })
}

export function flattenProperties(
  properties: Property[],
  parentName: string = ''
): Record<string, any> {
  const result: Record<string, any> = {}

  properties.forEach((property) => {
    const genericType = extractGenericTypes(property.type)
    let propertyName = property.name
    if (!propertyName && isArrayType(genericType)) {
      propertyName = '*'
    }
    if (!propertyName) {
      return
    }
    const fullName = parentName ? `${parentName}.${propertyName}` : propertyName
    // 如果有嵌套的 properties，则递归处理
    result[fullName] = property.defaultValue
    if (property.properties && property.properties.length > 0) {
      Object.assign(result, flattenProperties(property.properties, fullName))
    }
  })

  return result
}

export function getColumnType(property: Property) {
  const genericType = extractGenericTypes(property.type)
  if (isUnknownType(genericType) && property.properties?.length) {
    return 'Object'
  }

  return property.type
}

export function propertyToColumn(properties: Property[]): TableColumnData[] {
  if (!properties || !properties.length) {
    return []
  }
  if (properties.length === 1) {
    const property = properties[0]
    const genericType = extractGenericTypes(property.type)
    if (!property.name && isArrayType(genericType)) {
      return propertyToColumn(property.properties as Property[])
    }
  }

  return properties.map((property) => {
    const columnType = getColumnType(property)
    return {
      title: property.displayName || property.name,
      dataIndex: property.name,
      align: 'center',
      slotName: `type${columnType}Column`,
      ellipsis: true,
      tooltip: true
    }
  })
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
    id: connection.id || `e${connection.source}_${connection.target}`,
    markerEnd: MarkerType.ArrowClosed,
    sourceHandle: connection.sourcePointType,
    targetHandle: connection.targetPointType,
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

export function toComponentAttrs(properties: Property[]): ComponentAttr[] {
  return properties.map((property) => toComponentAttr(property))
}

export function extractGenericTypes(typeString: string): GenericType {
  // 检查是否有泛型结构（是否有 '<' 和 '>'）
  if (!typeString.includes('<') || !typeString.includes('>')) {
    // 如果没有泛型，直接返回普通类型
    return { mainType: typeString.trim(), genericTypes: [] }
  }

  const stack: GenericType[] = []
  let currentType: GenericType | null = null
  let buffer = ''

  for (let i = 0; i < typeString.length; i++) {
    const char = typeString[i]

    if (char === '<') {
      // 在遇到 '<' 之前的 buffer 是主类型
      const mainType = buffer.trim()
      currentType = { mainType, genericTypes: [] } // 初始化当前泛型
      stack.push(currentType) // 将当前类型推入栈
      buffer = '' // 清空 buffer
    } else if (char === '>') {
      // 遇到 '>'，意味着当前泛型解析完成
      if (buffer.trim()) {
        currentType?.genericTypes.push(buffer.trim())
        buffer = ''
      }
      const completedType = stack.pop()! // 当前完成的泛型结构
      if (stack.length > 0) {
        currentType = stack[stack.length - 1] // 返回到上一层泛型
        currentType.genericTypes.push(completedType) // 将完成的泛型作为子类型
      } else {
        return completedType // 如果栈为空，解析完成，返回整个结构
      }
    } else if (char === ',') {
      // 处理多个泛型参数之间的逗号
      if (buffer.trim()) {
        currentType?.genericTypes.push(buffer.trim())
        buffer = ''
      }
    } else {
      buffer += char // 收集字符
    }
  }

  // 如果处理过程中存在 buffer 但未被处理
  if (buffer.trim() && currentType) {
    currentType.mainType = buffer.trim()
  }

  return currentType!
}

export function isUnknownType(genericType: GenericType) {
  return !(isBasicType(genericType) || isArrayType(genericType) || isSpecialType(genericType))
}

export function isBasicType(genericType: GenericType) {
  return (
    isNumberType(genericType) ||
    genericType.mainType === 'String' ||
    genericType.mainType === 'Date'
  )
}

export function isSpecialType(genericType: GenericType) {
  return (
    genericType.mainType === 'Condition' ||
    genericType.mainType == 'FileData' ||
    genericType.mainType === 'Map' ||
    genericType.mainType === 'Linkage' ||
    genericType.mainType === 'ChatMessage'
  )
}

export function isFileDataList(property: Property) {
  const genericType = extractGenericTypes(property.type)
  return (isArrayType(genericType) && (genericType.genericTypes[0] as string) === 'FileData')
}


export function isNumberType(genericType: GenericType) {
  return ['Integer', 'Float', 'Double', 'Number', 'BigDecimal'].indexOf(genericType.mainType) > -1
}

export function isArrayType(genericType: GenericType) {
  return (
    genericType.mainType === 'List' ||
    genericType.mainType === 'Set' ||
    genericType.mainType === 'Array'
  )
}
