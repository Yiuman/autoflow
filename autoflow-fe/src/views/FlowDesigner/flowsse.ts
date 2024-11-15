import type {Workflow} from '@/api/workflow'
import {type EventSourceMessage, fetchEventSource} from '@microsoft/fetch-event-source'
import {useEnv} from '@/hooks/env'
import type {FindNode, UpdateNodeData} from '@vue-flow/core'

const {VITE_BASE_URL} = useEnv()

export interface SseCallback {
    onClose?: () => void;
    onError?: () => void;
}

export function executeFlowSSE(flow: Workflow,
                               findNode: FindNode,
                               updateNodeData: UpdateNodeData,
                               callback?: SseCallback) {
    const ctrl = new AbortController()
    const url = VITE_BASE_URL || '/api' + '/executions/sse'
    fetchEventSource(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(flow),
        async onmessage(message: EventSourceMessage) {
            const currentNode = findNode(message.id)
            switch (message.event) {
                case 'SERVICE_START':
                    if (!currentNode || currentNode.data.running) {
                        break
                    }
                    updateNodeData(message.id, {running: true})
                    break
                case 'SERVICE_END':
                    if (message.data) {
                        const resultData = JSON.parse(message.data)
                        let currentNodeData = currentNode?.data?.executionResult
                        const executionResult = resultData.length > 1 ? resultData : resultData[0]

                        if (currentNodeData) {
                            if (Array.isArray(currentNodeData)) {
                                Array.isArray(executionResult)
                                    ? currentNodeData.push(...executionResult)
                                    : currentNodeData.push(executionResult)
                            } else {
                                currentNodeData = [currentNodeData, executionResult]
                            }
                        } else {
                            currentNodeData = executionResult
                        }

                        updateNodeData(message.id, {
                            executionResult: currentNodeData,
                            running: false
                        })
                    }
                    break
                default:
            }
        },
        signal: ctrl.signal,
        onclose() {
            ctrl.abort()
            callback && callback.onClose && callback.onClose()
        },
        onerror(error: Error) {
            callback && callback.onError && callback.onError()
            throw error
        }
    })
}
