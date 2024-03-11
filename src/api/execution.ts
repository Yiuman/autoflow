import type { Node, Flow, ExecutionData } from '@/types/flow';
import request from '@/utils/request';
enum API {
    EXEC_FLOW = "/executions",
    EXEC_NODE = "/executions/node",
}

export function executeFlow(flow: Flow) {
    return request.post<Record<string, ExecutionData[]>>(API.EXEC_FLOW, flow);
}

export function executionNode(node:Node){
    return request.post<ExecutionData[]>(API.EXEC_NODE,node)
}


