import { MarkerType, type GraphEdge, type Node as FlowNode } from '@vue-flow/core'

import type { Flow, Connection, Node, } from '@/types/flow'
import { uuid } from '@/utils/util-func'

export function toNode(graphNode: FlowNode): Node {
  const position = graphNode.position
  const parameters = graphNode.data.parameters || {};
  const serviceName = graphNode.data.serviceName || '';
  return {
    id: graphNode.id,
    label: graphNode.label as string,
    serviceName: serviceName,
    type: graphNode.type as string,
    position: { ...position },
    data: parameters
  }
}

export function toGraphNode(node: Node): FlowNode {
  const nodeData: Record<string, any> = {};
  nodeData.serviceName = node.serviceName
  nodeData.parameters = node.data
  return {
    ...node,
    data: nodeData
  } 
}

export function toConnect(edge: GraphEdge): Connection {
  return {
    source: edge.source,
    target: edge.target,
    sourceX: edge.sourceX,
    sourceY: edge.sourceY,
    targetX: edge.targetX,
    targetY: edge.targetY
  }
}

export function toGraphEdge(connection: Connection): GraphEdge {
  return {
    ...connection,
    id: `e${connection.source}_${connection.target}`,
    markerEnd: MarkerType.ArrowClosed,
  } as GraphEdge
}

export function toFlow(nodes: FlowNode[], edges: GraphEdge[]): Flow {
  const buildUUID = uuid(32)
  return {
    id: buildUUID,
    name: `autoflow_${buildUUID}`,
    nodes: nodes.map((node) => toNode(node)),
    connections: edges.map((edge) => toConnect(edge))
  }
}
