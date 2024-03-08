// @ts-ignore
import type { GraphEdge } from '@vue-flow/core/dist/types/edge'
// @ts-ignore
import type { GraphNode } from '@vue-flow/core/dist/types/node'
import type { Flow, Connection, Node } from '@/types/flow'
import { uuid } from '@/utils/util-func'

export function toNode(graphNode: GraphNode): Node {
  const position = graphNode.position
  const parameters= graphNode.data.parameters || {};
  const serviceName= graphNode.data.serviceName || '';
  return {
    id: graphNode.id,
    label: graphNode.label,
    serviceName: serviceName,
    type: graphNode.type,
    position: { ...position },
    data: parameters
  }
}

export function toGraphNode(node:Node):GraphNode{
  const nodeData:Record<string,any> =  {};
  nodeData.serviceName = node.serviceName
  nodeData.parameters = node.data
  return {
    ...node,
    data:nodeData
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

export function toGraphEdge(connect:Connection):GraphEdge{
  return {
   ...connect
  }
}

export function toFlow(nodes: GraphNode[], edges: GraphEdge[]): Flow {
  const buildUUID = uuid(32)
  return {
    id: buildUUID,
    name: `autoflow_${buildUUID}`,
    nodes: nodes.map((node) => toNode(node)),
    connections: edges.map((edge) => toConnect(edge))
  }
}
