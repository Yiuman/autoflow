import { defineStore } from 'pinia'
import chatApi from '@/api/chat'
import type { ChatSession, ChatMessage, ToolCall } from '@/types/chat'

interface ChatStoreState {
  sessions: ChatSession[]
  currentSessionId: string | null
  messages: Record<string, ChatMessage[]>
  isLoading: boolean
  isStreaming: boolean
  streamingContent: string
  abortController: AbortController | null
}

export const useChatStore = defineStore('chat', {
  state: (): ChatStoreState => ({
    sessions: [],
    currentSessionId: null,
    messages: {},
    isLoading: false,
    isStreaming: false,
    streamingContent: '',
    abortController: null
  }),

  getters: {
    currentSession: (state): ChatSession | undefined => {
      return state.sessions.find((s) => s.id === state.currentSessionId)
    },

    currentMessages: (state): ChatMessage[] => {
      if (!state.currentSessionId) return []
      return state.messages[state.currentSessionId] || []
    },

    hasSessions: (state): boolean => {
      return state.sessions.length > 0
    }
  },

  actions: {
    /**
     * Load all chat sessions
     */
    async loadSessions() {
      this.isLoading = true
      try {
        this.sessions = await chatApi.getSessions()
      } finally {
        this.isLoading = false
      }
    },

    /**
     * Create a new chat session
     */
    async createSession(title?: string): Promise<ChatSession> {
      const session = await chatApi.createSession({ title })
      this.sessions.unshift(session)
      this.messages[session.id] = []
      this.currentSessionId = session.id
      return session
    },

    /**
     * Delete a chat session
     */
    async deleteSession(sessionId: string) {
      await chatApi.deleteSession(sessionId)
      const index = this.sessions.findIndex((s) => s.id === sessionId)
      if (index !== -1) {
        this.sessions.splice(index, 1)
      }
      delete this.messages[sessionId]
      if (this.currentSessionId === sessionId) {
        this.currentSessionId = this.sessions.length > 0 ? this.sessions[0].id : null
      }
    },

    /**
     * Switch to a different session
     */
    async switchSession(sessionId: string) {
      this.currentSessionId = sessionId
      if (!this.messages[sessionId]) {
        await this.loadMessages(sessionId)
      }
    },

    /**
     * Load messages for a session
     */
    async loadMessages(sessionId: string) {
      this.isLoading = true
      try {
        const messages = await chatApi.getMessages(sessionId)
        this.messages[sessionId] = messages
      } finally {
        this.isLoading = false
      }
    },

    /**
     * Send a message with SSE streaming
     */
    async sendMessage(content: string, provider?: string) {
      if (!this.currentSessionId || !content.trim()) return

      const sessionId = this.currentSessionId

      // Get provider from localStorage if not provided
      const selectedProvider = provider || localStorage.getItem('chat-provider') || 'openai'

      // Add user message immediately
      const userMessage: ChatMessage = {
        id: `temp-user-${Date.now()}`,
        sessionId,
        role: 'user',
        content: content.trim(),
        createdAt: new Date().toISOString()
      }

      if (!this.messages[sessionId]) {
        this.messages[sessionId] = []
      }
      this.messages[sessionId].push(userMessage)

      // Create placeholder for assistant response
      const assistantMessage: ChatMessage = {
        id: `temp-assistant-${Date.now()}`,
        sessionId,
        role: 'assistant',
        content: '',
        createdAt: new Date().toISOString()
      }
      this.messages[sessionId].push(assistantMessage)

      // Start streaming
      this.isStreaming = true
      this.streamingContent = ''

      this.abortController = chatApi.sendMessage(sessionId, content, selectedProvider, {
        onMessage: (event) => {
          this.handleStreamEvent(sessionId, assistantMessage.id, event)
        },
        onError: (error) => {
          console.error('Stream error:', error)
          this.isStreaming = false
          assistantMessage.content = 'An error occurred while processing your request.'
        },
        onClose: () => {
          this.isStreaming = false
          this.abortController = null
        }
      })
    },

    /**
     * Handle SSE stream events
     */
    handleStreamEvent(
      sessionId: string,
      messageId: string,
      event: { event: string; delta?: string; data?: unknown; error?: string }
    ) {
      const messages = this.messages[sessionId]
      const message = messages?.find((m) => m.id === messageId)
      if (!message) return

      switch (event.event) {
        case 'content_delta':
          if (event.delta) {
            message.content += event.delta
            this.streamingContent = message.content
          }
          break

        case 'tool_call':
          if (event.data) {
            const toolCall = event.data as ToolCall
            if (!message.toolCalls) {
              message.toolCalls = []
            }
            const existingIndex = message.toolCalls.findIndex((tc) => tc.id === toolCall.id)
            if (existingIndex !== -1) {
              message.toolCalls[existingIndex] = toolCall
            } else {
              message.toolCalls.push(toolCall)
            }
          }
          break

        case 'tool_result':
          if (event.data) {
            const toolCall = event.data as ToolCall
            if (message.toolCalls) {
              const index = message.toolCalls.findIndex((tc) => tc.id === toolCall.id)
              if (index !== -1) {
                message.toolCalls[index] = toolCall
              }
            }
          }
          break

        case 'message_end':
          // Update with final message ID if provided
          if (event.data && typeof event.data === 'object' && 'id' in event.data) {
            message.id = (event.data as { id: string }).id
          }

          // Auto-generate title from first user message
          const session = this.sessions.find((s) => s.id === sessionId)
          if (session && session.title === 'New Chat') {
            const userMessages = messages?.filter((m) => m.role === 'user')
            if (userMessages && userMessages.length > 0) {
              const firstUserContent = userMessages[0].content
              const truncatedTitle = firstUserContent.length > 50
                ? firstUserContent.substring(0, 50) + '...'
                : firstUserContent
              session.title = truncatedTitle
            }
          }
          break

        case 'error':
          message.content = event.error || 'An error occurred'
          this.isStreaming = false
          break
      }
    },

    /**
     * Cancel the current streaming request
     */
    cancelStream() {
      if (this.abortController) {
        this.abortController.abort()
        this.abortController = null
      }
      this.isStreaming = false
    },

    /**
     * Initialize store with sessions
     */
    async init() {
      await this.loadSessions()
      if (this.sessions.length > 0 && !this.currentSessionId) {
        await this.switchSession(this.sessions[0].id)
      }
      // If no sessions exist, create a new one automatically
      if (this.sessions.length === 0) {
        const newSession = await this.createSession()
        await this.switchSession(newSession.id)
      }
    },

    /**
     * Clear all messages for current session
     */
    clearCurrentMessages() {
      if (this.currentSessionId) {
        this.messages[this.currentSessionId] = []
      }
    }
  }
})
