<script lang="ts" setup>
import { computed, ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import MessageGroup from './MessageGroup.vue'
import ChatInputBar from '../ChatInputBar.vue'

const chatStore = useChatStore()
const messagesContainerRef = ref<HTMLElement | null>(null)
const updateTrigger = ref(0)

const messages = computed(() => {
  // Trigger re-computation when blockEntities changes
  void chatStore.blockEntities
  void updateTrigger.value
  return chatStore.activeMessages
})

// Force re-render when block entities change
watch(
  () => JSON.stringify(chatStore.blockEntities),
  () => {
    updateTrigger.value++
  }
)

// Auto-scroll to bottom when new messages arrive
watch(
  () => messages.value.length,
  async () => {
    await nextTick()
    if (messagesContainerRef.value) {
      messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
    }
  }
)

function getBlocksForMessage(messageId: string) {
  void updateTrigger.value
  const message = chatStore.getMessageById(messageId)
  if (!message) return []
  return message.blocks
    .map(blockId => chatStore.getBlockById(blockId))
    .filter((block): block is NonNullable<typeof block> => block != null)
}
</script>

<template>
  <div class="chat-message-area">
    <div class="messages-container" ref="messagesContainerRef">
      <div v-if="messages.length === 0" class="empty-messages">
        <p>Send a message to start chatting</p>
      </div>
      
      <div v-else class="messages-list">
        <MessageGroup
          v-for="message in messages"
          :key="message.id"
          :message="message"
          :blocks="getBlocksForMessage(message.id)"
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
