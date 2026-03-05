import { type EventSourceMessage, fetchEventSource } from '@microsoft/fetch-event-source'
import request from '@/utils/request'
import { useEnv } from '@/hooks/env'
import type { ChatSession, ChatMessage, CreateSessionRequest, SendMessageRequest } from '@/types/chat'

const { VITE_BASE_URL } = useEnv()

const BASE_URL = '/chat/sessions'

interface SSECallbacks {
  onMessage?: (event: ChatMessageEvent) => void
  onError?: (error: Error) => void
  onClose?: () => void
}

interface ChatMessageEvent {
  event: string
  id?: string
  data?: unknown
  delta?: string
  error?: string
}

const chatApi = {
  /**
   * Create a new chat session
   */
  createSession: async (data?: CreateSessionRequest): Promise<ChatSession> => {
    return request.post<ChatSession>(BASE_URL, data || {})
  },

  /**
   * Get all chat sessions
   */
  getSessions: async (): Promise<ChatSession[]> => {
    return request.get<ChatSession[]>(BASE_URL)
  },

  /**
   * Get a specific session by ID
   */
  getSession: async (sessionId: string): Promise<ChatSession> => {
    return request.get<ChatSession>(`${BASE_URL}/${sessionId}`)
  },

  /**
   * Delete a chat session
   */
  deleteSession: async (sessionId: string): Promise<void> => {
    return request.delete<void>(`${BASE_URL}/${sessionId}`)
  },

  /**
   * Get messages for a session
   */
  getMessages: async (sessionId: string): Promise<ChatMessage[]> => {
    return request.get<ChatMessage[]>(`${BASE_URL}/${sessionId}/messages`)
  },

  /**
   * Send a message and receive SSE stream
   * Returns an AbortController to cancel the stream
   */
  sendMessage: (
    sessionId: string,
    content: string,
    provider?: string,
    callbacks?: SSECallbacks
  ): AbortController => {
    const ctrl = new AbortController()
    const url = `${VITE_BASE_URL || ''}${BASE_URL}/${sessionId}/messages`

    const requestBody: SendMessageRequest = { content }
    if (provider) {
      requestBody.provider = provider
    }

    fetchEventSource(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody),
      signal: ctrl.signal,
      async onmessage(message: EventSourceMessage) {
        const event: ChatMessageEvent = {
          event: message.event,
          id: message.id,
          data: message.data ? JSON.parse(message.data) : undefined
        }

        // Handle delta content
        if (message.data) {
          try {
            const parsed = JSON.parse(message.data)
            if (parsed.delta) {
              event.delta = parsed.delta
            }
            if (parsed.error) {
              event.error = parsed.error
            }
          } catch {
            // If not JSON, treat as plain text delta
            event.delta = message.data
          }
        }

        callbacks?.onMessage?.(event)
      },
      onclose() {
        ctrl.abort()
        callbacks?.onClose?.()
      },
      onerror(error: Error) {
        callbacks?.onError?.(error)
        throw error
      }
    })

    return ctrl
  },

  /**
   * Send a message without streaming (simple POST)
   */
  sendMessageSync: async (sessionId: string, content: string, provider?: string): Promise<ChatMessage> => {
    const body: SendMessageRequest = { content }
    if (provider) {
      body.provider = provider
    }
    return request.post<ChatMessage>(
      `${BASE_URL}/${sessionId}/messages`,
      body
    )
  }
}

export default chatApi
