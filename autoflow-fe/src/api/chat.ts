import { type EventSourceMessage, fetchEventSource } from '@microsoft/fetch-event-source'
import axios from 'axios'
import { useEnv } from '@/hooks/env'
import { useChatStore } from '@/stores/chat'

const { VITE_BASE_URL } = useEnv()
const BASE_URL = VITE_BASE_URL || '/api'

export interface ChatSession {
  id: string
  title?: string
  modelId?: string
  systemPrompt?: string
  status: string
  createTime?: string
}

export async function createChatSession(modelId?: string): Promise<string> {
  const response = await axios.post(`${BASE_URL}/chat/sessions`, { modelId })
  return response.data.data
}

export async function getChatSessions(): Promise<ChatSession[]> {
  const response = await axios.get(`${BASE_URL}/chat/sessions`)
  return response.data.data?.records || []
}

export async function deleteChatSession(sessionId: string): Promise<void> {
  await axios.delete(`${BASE_URL}/chat/sessions/${sessionId}`)
}

export async function getChatMessages(sessionId: string): Promise<any[]> {
  const response = await axios.get(`${BASE_URL}/chat/messages`, { params: { sessionId } })
  return response.data.data?.records || []
}

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
  const url = `${BASE_URL}/chat`

  const chatStore = useChatStore()
  const sessionId = chatStore.activeSession?.id
  const modelId = chatStore.activeSession?.modelId

  if (!sessionId) {
    callbacks.onError?.('No active session. Please create a new session first.')
    ctrl.abort()
    return ctrl
  }

  const body: Record<string, string> = { input, sessionId }
  if (modelId) {
    body.modelId = modelId
  }

  fetchEventSource(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    async onmessage(message: EventSourceMessage) {
      switch (message.event) {
        case 'thinking': {
          const data = JSON.parse(message.data)
          callbacks.onThinking?.(data.content)
          break
        }
        case 'token': {
          const data = JSON.parse(message.data)
          callbacks.onToken?.(data.content)
          break
        }
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
        case 'complete': {
          const data = JSON.parse(message.data)
          callbacks.onComplete?.(data.content)
          break
        }
        case 'error': {
          const data = JSON.parse(message.data)
          callbacks.onError?.(data.content)
          break
        }
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
