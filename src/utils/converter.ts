import { MarkerType, type GraphEdge, type Node as VueFlowNode, type GraphNode } from '@vue-flow/core'

import type { Flow, Connection, Node, } from '@/types/flow'
import { uuid } from '@/utils/util-func'
import { uniq } from 'lodash';

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

export function toGraphNode(node: Node): VueFlowNode {
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
    targetY: edge.targetY,
    expression: edge.data.expression
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
  const buildUUID = uuid(32)
  return {
    id: buildUUID,
    name: `autoflow_${buildUUID}`,
    nodes: nodes?.map((node) => toNode(node)),
    connections: edges?.map((edge) => toConnect(edge))
  }
}
