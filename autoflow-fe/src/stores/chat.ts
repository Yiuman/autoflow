import { defineStore } from 'pinia'
import type {
  Message,
  MessageBlock,
  Topic,
  MessageBlockType,
  MessageBlockStatus,
  FileMetadata
} from '@/types/chat'
import { uuid } from '@/utils/util-func'

interface ChatState {
  // Topics (chat sessions)
  topics: Topic[]
  activeTopicId: string | null

  // Messages by topic
  messagesByTopic: Record<string, string[]> // topicId -> messageIds
  messageEntities: Record<string, Message> // messageId -> Message

  // Blocks by message
  blockEntities: Record<string, MessageBlock> // blockId -> MessageBlock

  // UI State
  isStreaming: boolean
  isLoading: boolean
  streamingMessageId: string | null

  // Selected model (for new messages)
  selectedModelId: string | null

  // Files (attachments)
  files: FileMetadata[]
}

export const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    topics: [],
    activeTopicId: null,
    messagesByTopic: {},
    messageEntities: {},
    blockEntities: {},
    isStreaming: false,
    isLoading: false,
    streamingMessageId: null,
    selectedModelId: null,
    files: []
  }),

  getters: {
    activeTopic(state): Topic | undefined {
      return state.topics.find(t => t.id === state.activeTopicId)
    },

    activeMessages(state): Message[] {
      if (!state.activeTopicId) return []
      const messageIds = state.messagesByTopic[state.activeTopicId] || []
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
    // Topic actions
    createTopic(assistantId: string, name?: string): Topic {
      const topic: Topic = {
        id: uuid(8, true),
        name: name || 'New Chat',
        assistantId,
        messages: [],
        createdAt: new Date().toISOString()
      }
      this.topics.push(topic)
      return topic
    },

    setActiveTopic(topicId: string) {
      this.activeTopicId = topicId
    },

    updateTopic(topicId: string, updates: Partial<Topic>) {
      const index = this.topics.findIndex(t => t.id === topicId)
      if (index !== -1) {
        this.topics[index] = { ...this.topics[index], ...updates }
      }
    },

    deleteTopic(topicId: string) {
      // Delete all messages and blocks for this topic
      const messageIds = this.messagesByTopic[topicId] || []
      messageIds.forEach(msgId => {
        const msg = this.messageEntities[msgId]
        if (msg) {
          msg.blocks.forEach(blockId => {
            delete this.blockEntities[blockId]
          })
        }
        delete this.messageEntities[msgId]
      })
      delete this.messagesByTopic[topicId]
      this.topics = this.topics.filter(t => t.id !== topicId)
      if (this.activeTopicId === topicId) {
        this.activeTopicId = this.topics[0]?.id || null
      }
    },

    // Message actions
    addMessage(topicId: string, message: Omit<Message, 'id' | 'createdAt' | 'blocks'>): Message {
      const msg: Message = {
        ...message,
        id: uuid(8, true),
        createdAt: new Date().toISOString(),
        blocks: []
      }
      this.messageEntities[msg.id] = msg
      if (!this.messagesByTopic[topicId]) {
        this.messagesByTopic[topicId] = []
      }
      this.messagesByTopic[topicId].push(msg.id)
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
        // Delete associated blocks
        message.blocks.forEach(blockId => {
          delete this.blockEntities[blockId]
        })
        // Remove from topic
        for (const tid in this.messagesByTopic) {
          this.messagesByTopic[tid] = this.messagesByTopic[tid].filter(id => id !== messageId)
        }
        delete this.messageEntities[messageId]
      }
    },

    // Block actions
    addBlock(messageId: string, block: Omit<MessageBlock, 'id' | 'messageId' | 'createdAt'>): MessageBlock {
      const newBlock: MessageBlock = {
        ...block,
        id: uuid(8, true),
        messageId,
        createdAt: new Date().toISOString()
      } as MessageBlock

      this.blockEntities[newBlock.id] = newBlock

      // Add block to message
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
        // Remove from message
        const message = this.messageEntities[block.messageId]
        if (message) {
          message.blocks = message.blocks.filter(id => id !== blockId)
        }
        delete this.blockEntities[blockId]
      }
    },

    // Streaming actions
    startStreaming(messageId: string) {
      this.isStreaming = true
      this.streamingMessageId = messageId
    },

    stopStreaming() {
      this.isStreaming = false
      this.streamingMessageId = null
    },

    // Helper to create a complete streaming message with blocks
    createStreamingMessage(topicId: string, role: 'user' | 'assistant'): Message {
      const message = this.addMessage(topicId, {
        role,
        status: 'streaming'
      })
      this.startStreaming(message.id)
      return message
    },

    // Complete streaming and finalize message
    completeStreaming(finalStatus: 'success' | 'error' = 'success') {
      if (this.streamingMessageId) {
        this.updateMessage(this.streamingMessageId, {
          status: finalStatus === 'success' ? 'done' : 'error'
        })
      }
      this.stopStreaming()
    },

    // Clear all data
    clearAll() {
      this.topics = []
      this.activeTopicId = null
      this.messagesByTopic = {}
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
