<script lang="ts" setup>
import { computed, ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import ChatMessageGroup from '@/components/Chat/layout/ChatMessageGroup.vue'
import ChatInputBar from '../ChatInputBar.vue'
import type { Message, MessageBlock } from '@/types/chat'

const chatStore = useChatStore()
const messagesContainerRef = ref<HTMLElement | null>(null)
const updateTrigger = ref(0)

interface RenderItem {
  message: Message
  blocks: MessageBlock[]
}

  const renderItems = computed(() => {
  void chatStore.blockEntities
  void updateTrigger.value
  const messages = chatStore.activeMessages
  
  const result: RenderItem[] = []
  let currentAssistantBlocks: MessageBlock[] = []
  let lastConvId: string | null = null
  let lastAssistantStatus: string | null = null
  
  for (const msg of messages) {
    const convId = msg.conversationId || msg.id
    const msgBlocks = (msg.blocks || [])
      .map(blockId => chatStore.getBlockById(blockId))
      .filter((block): block is NonNullable<typeof block> => block != null)
    
    if (msg.role === 'user') {
      // Flush pending assistant blocks
      if (currentAssistantBlocks.length > 0) {
        result.push({
          message: { id: 'merged-assistant', role: 'assistant', status: lastAssistantStatus ?? undefined, blocks: [], conversationId: lastConvId } as Message,
          blocks: currentAssistantBlocks
        })
        currentAssistantBlocks = []
        lastAssistantStatus = null
      }
      // USER message as separate item
      result.push({ message: msg, blocks: msgBlocks })
      lastConvId = null
    } else {
      // ASSISTANT: merge blocks if same convId
      if (lastConvId === convId) {
        currentAssistantBlocks.push(...msgBlocks)
      } else {
        // Flush previous
        if (currentAssistantBlocks.length > 0) {
          result.push({
            message: { id: 'merged-assistant', role: 'assistant', status: lastAssistantStatus ?? undefined, blocks: [], conversationId: lastConvId } as Message,
            blocks: currentAssistantBlocks
          })
        }
        currentAssistantBlocks = msgBlocks
        lastConvId = convId
      }
      lastAssistantStatus = msg.status
    }
  }
  
  // Flush remaining — push assistant if it exists, even with no blocks (for streaming placeholder)
  if (currentAssistantBlocks.length > 0 || lastAssistantStatus === 'streaming') {
    result.push({
      message: { id: 'merged-assistant', role: 'assistant', status: lastAssistantStatus ?? undefined, blocks: [], conversationId: lastConvId } as Message,
      blocks: currentAssistantBlocks
    })
  }
  
  return result
})

// Force re-render when block entities change
watch(
  () => JSON.stringify(chatStore.blockEntities),
  () => {
    updateTrigger.value++
  }
)

// Auto-scroll to bottom when messages change or blocks update
watch(
  () => JSON.stringify(chatStore.blockEntities),
  async () => {
    await nextTick()
    if (messagesContainerRef.value) {
      messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
    }
  }
)
</script>

<template>
  <div class="chat-message-area">
    <div class="messages-container" ref="messagesContainerRef">
      <div v-if="renderItems.length === 0" class="empty-messages">
        <p>Send a message to start chatting</p>
      </div>
      
      <div v-else class="messages-list">
        <ChatMessageGroup
          v-for="(item, index) in renderItems"
          :key="index"
          :message="item.message"
          :blocks="item.blocks"
        />
      </div>
    </div>
    
    <ChatInputBar />
  </div>
</template>

<style scoped lang="scss">
.chat-message-area {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100%;
  overflow: hidden;
  
  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    
    &::-webkit-scrollbar {
      display: none;
    }
  }
  
  .empty-messages {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-text-3);
  }
  
  .messages-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
}
</style>
