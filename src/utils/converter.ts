import { MarkerType, isNode, isEdge } from '@vue-flow/core'
import type { GraphEdge, Node as VueFlowNode, GraphNode, Elements } from '@vue-flow/core'

import type { Flow, Connection, Node, Service, NodeElementData } from '@/types/flow'
import { uuid } from '@/utils/util-func'
import { uniq } from 'lodash';
import type { Position } from '@vueuse/core';
//获取当前节点所有的前置节点
export function getAllIncomers(nodeId: string | undefined, getIncomers: (nodeOrId: Node | string) => GraphNode[]): VueFlowNode[] {
  if (!nodeId) {
    return [];
  }
  let nodeIncomers = getIncomers(nodeId);
  if (nodeIncomers.length) {
    for (const node of nodeIncomers) {
      const preIncomers = getIncomers(node.id);
      nodeIncomers = [...preIncomers, ...nodeIncomers,]
    }
  }
  return uniq(nodeIncomers);

}

export function toNode(graphNode: VueFlowNode): Node {
  const position = graphNode.position
  const parameters = {...graphNode.data.parameters} || {};
  //删除输入
  delete parameters['inputData']
  const serviceId = graphNode.data.serviceId || '';
  return {
    id: graphNode.id,
    label: graphNode.data.label || graphNode.label  as string,
    serviceId: serviceId,
    type: graphNode.type as string,
    position: { ...position },
    data: parameters
  }
}

export function toGraphNode(node: Node): VueFlowNode {
  const nodeData: Record<string, any> = {};
  nodeData.serviceId = node.serviceId
  nodeData.label = node.label;
  nodeData.parameters = node.data
  return {
    ...node,
    data: nodeData
  }
}

export function serviceToGraphNode(service: Service, position?: Position): VueFlowNode {
  const nodeData: Record<string, any> = {};
  nodeData.serviceId = service.id
  nodeData.label = service.name
  nodeData.parameters = {};
  return {
    type: service.name === 'Switch' ? 'SWITCH' : 'SERVICE',
    id: `node_${uuid(32)}`,
    position: position || { x: 0, y: 0 },
    data: nodeData
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
    expression: edge.data?.expression
  }
}

export function toGraphEdge(connection: Connection): GraphEdge {
  return {
    ...connection,
    id: `e${connection.source}_${connection.target}`,
    markerEnd: MarkerType.ArrowClosed,
    type: 'edge'
  } as GraphEdge
}

export function toFlow<N extends VueFlowNode, E extends GraphEdge>(nodes: N[] | undefined, edges: E[] | undefined): Flow {
  const id = `autoflow_${uuid(32)}`
  return {
    id: id,
    name: id,
    nodes: nodes?.map((node) => toNode(node)),
    connections: edges?.map((edge) => toConnect(edge))
  }
}

export function elementsToFlow(elements: Elements<NodeElementData>): Flow {
  const id = `autoflow_${uuid(32)}`
  return {
    id: id,
    name: id,
    nodes: getNodes(elements)?.map((node) => toNode(node)),
    connections: getEdges(elements)?.map((edge) => toConnect(edge))
  }
}

export function getNodes(elements: Elements<NodeElementData>): VueFlowNode[] {
  return elements.filter(item => isNode(item)) as VueFlowNode[];
}

export function getEdges(elements: Elements<NodeElementData>): GraphEdge[] {
  return elements.filter(item => isEdge(item)) as GraphEdge[];
}
