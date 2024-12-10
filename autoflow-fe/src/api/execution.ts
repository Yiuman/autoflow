import type {ExecutionResult, Flow, FlowExecutionResult, Node} from '@/types/flow'
import request from '@/utils/request'

export interface WorkflowInst {
  id: string,
  workflowId: string,
  submitTime?: number,
  startTime?: number,
  endTime?: number,
  durationMs?: number,
  flowState: string,
  flowStr?: string
}

export function executeFlow(flow: Flow) {
  return request.post<FlowExecutionResult>('/executions', flow)
}


export function getExecutableFlowInst(flow: Flow) {
  return request.post<WorkflowInst>('/executions/inst', {...flow, flowStr: JSON.stringify(flow)})
}

export function executeNode(node: Node) {
  return request.post<ExecutionResult<any>[]>('/executions/node', node)
}


export function stopExecution(flowInst: { id: string }) {
  return request.post<ExecutionResult<any>[]>('/executions/stop', flowInst)
}
