import { defineStore } from 'pinia'
import {
  type Message,
  type MessageBlock,
  type Session,
  type FileMetadata,
  MessageBlockType,
  MessageBlockStatus
} from '@/types/chat'
import { uuid } from '@/utils/util-func'
import { createChatSession, getChatSessions, deleteChatSession, getChatMessages } from '@/api/chat'

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

  blockSequence: number
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
    files: [],
    blockSequence: 0
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

    async loadSessionMessages(sessionId: string): Promise<void> {
      try {
        const messages = await getChatMessages(sessionId)
        console.log('[loadSessionMessages] sessionId:', sessionId, 'messages count:', messages.length, 'first msg conversationId:', messages[0]?.conversationId)
        
        if (!this.messagesBySession[sessionId]) {
          this.messagesBySession[sessionId] = []
        }
        
        for (const msg of messages) {
          const messageId = msg.id || uuid(8, true)
          const messageEntity: Message = {
            id: messageId,
            conversationId: msg.conversationId,
            role: msg.role?.toLowerCase() === 'user' ? 'user' : 'assistant',
            type: msg.type,
            blocks: [],
            createdAt: msg.createTime ? new Date(msg.createTime).toISOString() : (msg.createdAt || new Date().toISOString()),
            updatedAt: msg.updatedAt,
            status: msg.status,
            model: msg.model,
            assistantId: msg.assistantId,
            agentSessionId: msg.agentSessionId
          }
          
          this.messageEntities[messageId] = messageEntity
          this.messagesBySession[sessionId].push(messageId)
          
          if (msg.blocks && Array.isArray(msg.blocks) && msg.blocks.length > 0) {
            for (const block of msg.blocks) {
              this.blockSequence++
              const blockId = block.id || uuid(8, true)
              
              const blockEntity: MessageBlock = {
                id: blockId,
                messageId: messageId,
                type: block.type || MessageBlockType.UNKNOWN,
                status: block.status || MessageBlockStatus.SUCCESS,
                content: block.content,
                createdAt: block.createTime ? new Date(block.createTime).toISOString() : (block.createdAt || new Date().toISOString()),
                updatedAt: block.updatedAt,
                sequence: this.blockSequence,
                ...block
              } as MessageBlock
              
              this.blockEntities[blockId] = blockEntity
              messageEntity.blocks.push(blockId)
            }
          } else {
            const role = msg.role?.toUpperCase()
            const content = msg.content || ''
            const thinkingContent = msg.thinkingContent
            
            if (role === 'USER') {
              this.blockSequence++
              const blockId = uuid(8, true)
              const blockEntity: MessageBlock = {
                id: blockId,
                messageId: messageId,
                type: MessageBlockType.MAIN_TEXT,
                status: MessageBlockStatus.SUCCESS,
                content: content,
                createdAt: messageEntity.createdAt,
                sequence: this.blockSequence
              } as MessageBlock
              
              this.blockEntities[blockId] = blockEntity
              messageEntity.blocks.push(blockId)
            } else if (role === 'ASSISTANT') {
              if (thinkingContent) {
                this.blockSequence++
                const thinkBlockId = uuid(8, true)
                const thinkBlockEntity: MessageBlock = {
                  id: thinkBlockId,
                  messageId: messageId,
                  type: MessageBlockType.THINKING,
                  status: MessageBlockStatus.SUCCESS,
                  content: thinkingContent,
                  createdAt: messageEntity.createdAt,
                  sequence: this.blockSequence
                } as MessageBlock
                
                this.blockEntities[thinkBlockId] = thinkBlockEntity
                messageEntity.blocks.push(thinkBlockId)
              }
              
              if (msg.metadata) {
                try {
                  const metadata = typeof msg.metadata === 'string' ? JSON.parse(msg.metadata) : msg.metadata
                  if (metadata.toolCalls && Array.isArray(metadata.toolCalls)) {
                    for (const toolCall of metadata.toolCalls) {
                      this.blockSequence++
                      const toolBlockId = uuid(8, true)
                      const toolBlockEntity: MessageBlock = {
                        id: toolBlockId,
                        messageId: messageId,
                        type: MessageBlockType.TOOL,
                        status: MessageBlockStatus.SUCCESS,
                        toolName: toolCall.toolName,
                        arguments: toolCall.arguments,
                        content: toolCall.result,
                        createdAt: messageEntity.createdAt,
                        sequence: this.blockSequence
                      } as MessageBlock
                      
                      this.blockEntities[toolBlockId] = toolBlockEntity
                      messageEntity.blocks.push(toolBlockId)
                    }
                  }
                } catch (e) {
                  console.error('Failed to parse metadata.toolCalls:', e)
                }
              }
              
              if (content) {
                this.blockSequence++
                const textBlockId = uuid(8, true)
                const textBlockEntity: MessageBlock = {
                  id: textBlockId,
                  messageId: messageId,
                  type: MessageBlockType.MAIN_TEXT,
                  status: MessageBlockStatus.SUCCESS,
                  content: content,
                  createdAt: messageEntity.createdAt,
                  sequence: this.blockSequence
                } as MessageBlock
                
                this.blockEntities[textBlockId] = textBlockEntity
                messageEntity.blocks.push(textBlockId)
              }
            }
          }
        }
      } catch (error) {
        console.error('Failed to load session messages:', error)
      }
    },

    setActiveSession(sessionId: string) {
      this.activeSessionId = sessionId
      if (!this.messagesBySession[sessionId]?.length) {
        this.loadSessionMessages(sessionId)
      }
    },

    updateSession(sessionId: string, updates: Partial<Session>) {
      const index = this.sessions.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        this.sessions[index] = { ...this.sessions[index], ...updates }
      }
    },

    async deleteSession(sessionId: string) {
      try {
        await deleteChatSession(sessionId)
      } catch (error) {
        console.error('Failed to delete session from backend:', error)
      }
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

    addBlock(messageId: string, block: Omit<MessageBlock, 'id' | 'messageId' | 'createdAt'>, append = true): MessageBlock {
      this.blockSequence++
      const newBlock: MessageBlock = {
        ...block,
        id: uuid(8, true),
        messageId,
        createdAt: new Date().toISOString(),
        sequence: this.blockSequence
      } as MessageBlock

      this.blockEntities[newBlock.id] = newBlock

      const message = this.messageEntities[messageId]
      if (message) {
        if (append) {
          message.blocks.push(newBlock.id)
        } else {
          message.blocks.unshift(newBlock.id)
        }
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
