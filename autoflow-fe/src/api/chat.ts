import { type EventSourceMessage, fetchEventSource } from '@microsoft/fetch-event-source'
import { useEnv } from '@/hooks/env'

const { VITE_BASE_URL } = useEnv()

export interface ChatSSECallbacks {
  onThinking?: (text: string) => void
  onToken?: (text: string) => void
  onToolStart?: (toolName: string, toolArgs: string) => void
  onToolEnd?: (toolName: string, result: any) => void
  onComplete?: (fullOutput: string) => void
  onError?: (message: string) => void
}

export function chatSSE(input: string, callbacks: ChatSSECallbacks): AbortController {
  const ctrl = new AbortController()
  const url = `${VITE_BASE_URL || '/api'}/chat`

  fetchEventSource(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ input }),
    async onmessage(message: EventSourceMessage) {
      switch (message.event) {
        case 'thinking':
          callbacks.onThinking?.(message.data)
          break
        case 'token':
          callbacks.onToken?.(message.data)
          break
        case 'tool_start': {
          const data = JSON.parse(message.data)
          callbacks.onToolStart?.(data.toolName, data.arguments)
          break
        }
        case 'tool_end': {
          const data = JSON.parse(message.data)
          callbacks.onToolEnd?.(data.toolName, data.result)
          break
        }
        case 'complete':
          callbacks.onComplete?.(message.data)
          break
        case 'error':
          callbacks.onError?.(message.data)
          break
      }
    },
    signal: ctrl.signal,
    onclose() {
      ctrl.abort()
    },
    onerror(error: Error) {
      callbacks.onError?.(error.message)
      throw error
    }
  })

  return ctrl
}
