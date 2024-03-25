import type { Node, Flow, ExecutionData } from '@/types/flow';
import request from '@/utils/request';

export function executeFlow(flow: Flow) {
    return request.post<Record<string, ExecutionData[]>>("/executions", flow);
}

export function executeNode(node: Node) {
    return request.post<ExecutionData[]>("/executions/node", node)
}

type ExecutionType = 'FLOW' | 'NODE';

export function stopExecution(node: { id: string, type: ExecutionType }) {
    return request.post<ExecutionData[]>("/executions/stop", node)
}


