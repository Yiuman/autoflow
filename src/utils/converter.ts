// @ts-ignore
import type { GraphEdge } from '@vue-flow/core/dist/types/edge'
// @ts-ignore
import type { GraphNode } from '@vue-flow/core/dist/types/node'
import type { Flow, Connection, Node } from '@/types/flow'
import { uuid } from '@/utils/util-func'

export function toNode(graphNode: GraphNode): Node {
  const position = graphNode.position
  return {
    id: graphNode.id,
    label: graphNode.label,
    serviceName: '',
    type: graphNode.type,
    position: { ...position },
    data: graphNode.data
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

export function toFlow(nodes: GraphNode[], edges: GraphEdge[]): Flow {
  const buildUUID = uuid(32)
  return {
    id: buildUUID,
    name: `autoflow_${buildUUID}`,
    nodes: nodes.map((node) => toNode(node)),
    connections: edges.map((edge) => toConnect(edge))
  }
}
