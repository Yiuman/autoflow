import type { ExecutionData, ExecutionResult, Flow, FlowExecutionResult, Node } from '@/types/flow'
import request from '@/utils/request'

export function executeFlow(flow: Flow) {
  return request.post<FlowExecutionResult>('/executions', flow)
}

export function executeNode(node: Node) {
  return request.post<ExecutionResult<ExecutionData>[]>('/executions/node', node)
}

type ExecutionType = 'FLOW' | 'NODE'

export function stopExecution(node: { id: string; type: ExecutionType }) {
  return request.post<ExecutionResult<ExecutionData>[]>('/executions/stop', node)
}
