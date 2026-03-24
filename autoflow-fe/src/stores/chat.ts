import { defineStore } from 'pinia'
import type {
  Message,
  MessageBlock,
  Session,
  MessageBlockType,
  MessageBlockStatus,
  FileMetadata
} from '@/types/chat'
import { uuid } from '@/utils/util-func'
import { createChatSession, getChatSessions } from '@/api/chat'

interface ChatState {
  sessions: Session[]
  activeSessionId: string | null

  messagesBySession: Record<string, string[]>
  messageEntities: Record<string, Message>

  blockEntities: Record<string, MessageBlock>

  isStreaming: boolean
  isLoading: boolean
  streamingMessageId: string | null

  selectedModelId: string | null

  files: FileMetadata[]
}

export const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    sessions: [],
    activeSessionId: null,
    messagesBySession: {},
    messageEntities: {},
    blockEntities: {},
    isStreaming: false,
    isLoading: false,
    streamingMessageId: null,
    selectedModelId: null,
    files: []
  }),

  getters: {
    activeSession(state): Session | undefined {
      return state.sessions.find(s => s.id === state.activeSessionId)
    },

    activeMessages(state): Message[] {
      if (!state.activeSessionId) return []
      const messageIds = state.messagesBySession[state.activeSessionId] || []
      return messageIds
        .map(id => state.messageEntities[id])
        .filter(Boolean)
    },

    getMessageById: (state) => (messageId: string): Message | undefined => {
      return state.messageEntities[messageId]
    },

    getBlocksByMessage: (state) => (messageId: string): MessageBlock[] => {
      const message = state.messageEntities[messageId]
      if (!message) return []
      return message.blocks
        .map(blockId => state.blockEntities[blockId])
        .filter(Boolean)
    },

    getBlockById: (state) => (blockId: string): MessageBlock | undefined => {
      return state.blockEntities[blockId]
    }
  },

  actions: {
    async createSession(assistantId: string, title?: string): Promise<Session> {
      let modelId: string | undefined
      try {
        modelId = localStorage.getItem('lastUsedModelId') || undefined
      } catch (e) {
        // localStorage not available
      }

      const sessionId = await createChatSession(modelId)
      const session: Session = {
        id: sessionId,
        title: title || 'New Chat',
        assistantId,
        modelId,
        messages: [],
        createdAt: new Date().toISOString()
      }
      this.sessions.unshift(session)
      return session
    },

    async loadSessions(): Promise<void> {
      try {
        const sessions = await getChatSessions()
        this.sessions = sessions.map(s => ({
          id: s.id,
          title: s.title || 'New Chat',
          assistantId: 'default-assistant',
          modelId: s.modelId,
          messages: [],
          createdAt: s.createTime || new Date().toISOString()
        }))
      } catch (error) {
        console.error('Failed to load sessions:', error)
      }
    },

    setActiveSession(sessionId: string) {
      this.activeSessionId = sessionId
    },

    updateSession(sessionId: string, updates: Partial<Session>) {
      const index = this.sessions.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        this.sessions[index] = { ...this.sessions[index], ...updates }
      }
    },

    deleteSession(sessionId: string) {
      const messageIds = this.messagesBySession[sessionId] || []
      messageIds.forEach(msgId => {
        const msg = this.messageEntities[msgId]
        if (msg) {
          msg.blocks.forEach(blockId => {
            delete this.blockEntities[blockId]
          })
        }
        delete this.messageEntities[msgId]
      })
      delete this.messagesBySession[sessionId]
      this.sessions = this.sessions.filter(s => s.id !== sessionId)
      if (this.activeSessionId === sessionId) {
        this.activeSessionId = this.sessions[0]?.id || null
      }
    },

    addMessage(sessionId: string, message: Omit<Message, 'id' | 'createdAt' | 'blocks'>): Message {
      const msg: Message = {
        ...message,
        id: uuid(8, true),
        createdAt: new Date().toISOString(),
        blocks: []
      }
      this.messageEntities[msg.id] = msg
      if (!this.messagesBySession[sessionId]) {
        this.messagesBySession[sessionId] = []
      }
      this.messagesBySession[sessionId].push(msg.id)
      return msg
    },

    updateMessage(messageId: string, updates: Partial<Message>) {
      if (this.messageEntities[messageId]) {
        this.messageEntities[messageId] = {
          ...this.messageEntities[messageId],
          ...updates
        }
      }
    },

    deleteMessage(messageId: string) {
      const message = this.messageEntities[messageId]
      if (message) {
        message.blocks.forEach(blockId => {
          delete this.blockEntities[blockId]
        })
        for (const sid in this.messagesBySession) {
          this.messagesBySession[sid] = this.messagesBySession[sid].filter(id => id !== messageId)
        }
        delete this.messageEntities[messageId]
      }
    },

    addBlock(messageId: string, block: Omit<MessageBlock, 'id' | 'messageId' | 'createdAt'>): MessageBlock {
      const newBlock: MessageBlock = {
        ...block,
        id: uuid(8, true),
        messageId,
        createdAt: new Date().toISOString()
      } as MessageBlock

      this.blockEntities[newBlock.id] = newBlock

      const message = this.messageEntities[messageId]
      if (message) {
        message.blocks.push(newBlock.id)
      }

      return newBlock
    },

    updateBlock(blockId: string, updates: Partial<MessageBlock>) {
      if (this.blockEntities[blockId]) {
        this.blockEntities[blockId] = {
          ...this.blockEntities[blockId],
          ...updates
        } as MessageBlock
      }
    },

    deleteBlock(blockId: string) {
      const block = this.blockEntities[blockId]
      if (block) {
        const message = this.messageEntities[block.messageId]
        if (message) {
          message.blocks = message.blocks.filter(id => id !== blockId)
        }
        delete this.blockEntities[blockId]
      }
    },

    startStreaming(messageId: string) {
      this.isStreaming = true
      this.streamingMessageId = messageId
    },

    stopStreaming() {
      this.isStreaming = false
      this.streamingMessageId = null
    },

    createStreamingMessage(sessionId: string, role: 'user' | 'assistant'): Message {
      const message = this.addMessage(sessionId, {
        role,
        status: 'streaming'
      })
      this.startStreaming(message.id)
      return message
    },

    completeStreaming(finalStatus: 'success' | 'error' = 'success') {
      if (this.streamingMessageId) {
        this.updateMessage(this.streamingMessageId, {
          status: finalStatus === 'success' ? 'done' : 'error'
        })
      }
      this.stopStreaming()
    },

    clearAll() {
      this.sessions = []
      this.activeSessionId = null
      this.messagesBySession = {}
      this.messageEntities = {}
      this.blockEntities = {}
      this.isStreaming = false
      this.isLoading = false
      this.streamingMessageId = null
      this.files = []
    },

    addFile(file: FileMetadata) {
      this.files.push(file)
    },

    removeFile(fileId: string) {
      this.files = this.files.filter(f => f.id !== fileId)
    },

    clearFiles() {
      this.files = []
    }
  }
})
