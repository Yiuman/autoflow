<script lang="ts" setup>
import { ref, watch, nextTick } from 'vue'
import type { ChatMessage } from '@/types/chat'
import MessageItem from './MessageItem.vue'
import { IconMessage } from '@arco-design/web-vue/es/icon'

interface Props {
  messages: ChatMessage[]
  isLoading?: boolean
  isStreaming?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isLoading: false,
  isStreaming: false
})

const messageListRef = ref<HTMLElement | null>(null)

// Auto-scroll to bottom when new messages arrive
watch(
  () => props.messages.length,
  async () => {
    await nextTick()
    scrollToBottom()
  }
)

// Also scroll when streaming content changes
watch(
  () => props.isStreaming,
  async (newVal) => {
    if (newVal) {
      await nextTick()
      scrollToBottom()
    }
  }
)

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// Expose scroll method for parent components
defineExpose({
  scrollToBottom
})
</script>

<template>
  <div ref="messageListRef" class="message-list">
    <!-- Empty State -->
    <div v-if="messages.length === 0 && !isLoading" class="empty-state">
      <div class="empty-icon">
        <IconMessage />
      </div>
      <h3 class="empty-title">Start a Conversation</h3>
      <p class="empty-description">
        Send a message to begin chatting with the AI assistant.
      </p>
    </div>

    <!-- Loading State -->
    <div v-else-if="isLoading && messages.length === 0" class="loading-state">
      <ASpin size="32" />
      <p class="loading-text">Loading messages...</p>
    </div>

    <!-- Messages -->
    <TransitionGroup v-else name="message" tag="div" class="messages-container">
      <MessageItem
        v-for="(message, index) in messages"
        :key="message.id || index"
        :message="message"
        :is-streaming="isStreaming && index === messages.length - 1 && message.role === 'assistant'"
      />
    </TransitionGroup>
  </div>
</template>

<style scoped lang="scss">
.message-list {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;

  /* Custom scrollbar */
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--color-text-4);
    border-radius: 3px;

    &:hover {
      background-color: var(--color-text-3);
    }
  }
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Message transition animations */
.message-enter-active {
  animation: slideIn 0.3s ease-out;
}

.message-leave-active {
  animation: slideOut 0.2s ease-in;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

.empty-state,
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px 20px;
}

.empty-state {
  .empty-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(var(--primary-6), 0.1), rgba(var(--primary-6), 0.05));
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;

    .arco-icon {
      font-size: 36px;
      color: rgb(var(--primary-6));
    }
  }

  .empty-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--color-text-1);
    margin: 0 0 8px 0;
  }

  .empty-description {
    font-size: 14px;
    color: var(--color-text-3);
    margin: 0;
    max-width: 300px;
    line-height: 1.6;
  }
}

.loading-state {
  .loading-text {
    font-size: 14px;
    color: var(--color-text-3);
    margin-top: 16px;
  }
}
</style>
